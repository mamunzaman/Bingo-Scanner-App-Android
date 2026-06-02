package com.example.mamunbingoapp.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.history.HistoryOcrSource
import com.example.mamunbingoapp.scanner.BingoNumberAnalyzer
import com.example.mamunbingoapp.domain.model.BingoScanType
import com.example.mamunbingoapp.scanner.ImportTicketQrPreOcr
import com.example.mamunbingoapp.scanner.MainSheetBingoOcr
import com.example.mamunbingoapp.scanner.MainSheetScanAnalyzer
import com.example.mamunbingoapp.scanner.OnlineBingoOcr
import com.example.mamunbingoapp.scanner.PlayPaperBingoOcr
import com.example.mamunbingoapp.scanner.tryDecodeBingoQrFromImageUri
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max

private const val IMPORT_TICKET_GALLERY_LOG = "ImportTicketGallery"
private const val IMPORT_TICKET_LOG = "ImportTicket"
private const val TOO_ZOOMED_GRID_AREA_THRESHOLD = 0.88f

/** Gallery-only manual trim (percent per edge, max [TRIM_SLIDER_MAX]); shared by preview and apply. */
object GalleryManualTrim {
    const val TRIM_SLIDER_MAX = 0.25f
    private const val PREVIEW_MAX_SIDE = 720
    private const val APPLY_MAX_SIDE = 3200

    fun decodeMaxSide(context: Context, uri: Uri, maxSide: Int): Bitmap? {
        val cr = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        cr.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / sample > maxSide) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        return cr.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
    }

    fun trimRect(w: Int, h: Int, l: Float, t: Float, r: Float, b: Float): Rect {
        var lf = (w * l.coerceIn(0f, TRIM_SLIDER_MAX)).toInt()
        var tf = (h * t.coerceIn(0f, TRIM_SLIDER_MAX)).toInt()
        var rf = (w * r.coerceIn(0f, TRIM_SLIDER_MAX)).toInt()
        var bf = (h * b.coerceIn(0f, TRIM_SLIDER_MAX)).toInt()
        lf = lf.coerceIn(0, max(0, w - 1))
        tf = tf.coerceIn(0, max(0, h - 1))
        rf = rf.coerceIn(0, max(0, w - 1 - lf))
        bf = bf.coerceIn(0, max(0, h - 1 - tf))
        val cw = (w - lf - rf).coerceAtLeast(1)
        val ch = (h - tf - bf).coerceAtLeast(1)
        return Rect(lf, tf, lf + cw, tf + ch)
    }

    fun croppedBitmap(src: Bitmap, rect: Rect): Bitmap =
        Bitmap.createBitmap(src, rect.left, rect.top, rect.width(), rect.height())

    fun previewBitmap(context: Context, uri: Uri, l: Float, t: Float, r: Float, b: Float): Bitmap? {
        val full = decodeMaxSide(context, uri, PREVIEW_MAX_SIDE) ?: return null
        val rect = trimRect(full.width, full.height, l, t, r, b)
        if (rect.width() >= full.width && rect.height() >= full.height) return full
        val out = croppedBitmap(full, rect)
        if (out !== full && !full.isRecycled) full.recycle()
        return out
    }

    fun buildApplyTempJpegUri(context: Context, uri: Uri, l: Float, t: Float, r: Float, b: Float): Uri? {
        val bitmap = decodeMaxSide(context, uri, APPLY_MAX_SIDE) ?: return null
        val rect = trimRect(bitmap.width, bitmap.height, l, t, r, b)
        if (rect.width() < 32 || rect.height() < 32) {
            if (!bitmap.isRecycled) bitmap.recycle()
            return null
        }
        val cropped = croppedBitmap(bitmap, rect)
        if (cropped !== bitmap && !bitmap.isRecycled) bitmap.recycle()
        return try {
            val file = File.createTempFile("import_gallery_manual_trim_", ".jpg", context.cacheDir)
            FileOutputStream(file).use { out -> cropped.compress(Bitmap.CompressFormat.JPEG, 92, out) }
            if (!cropped.isRecycled) cropped.recycle()
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            if (!cropped.isRecycled) cropped.recycle()
            Log.e(IMPORT_TICKET_GALLERY_LOG, "write trim jpeg", e)
            null
        }
    }
}

/**
 * Minimum non-zero cells in the final 5×5 grid before ML Kit OCR is treated as strong enough for
 * auto Manual Entry; below this, [ScanResultUiState.Error] is used and the import screen stays visible.
 *
 * **20** (not 24): still blocks clearly broken scans, but allows strong partial grids (e.g. 20–24 cells)
 * to reach Manual Entry quickly; full 25-cell reads behave the same as before.
 */
internal const val MIN_VALID_CELLS_FOR_MANUAL_ENTRY_NAV = 20

internal fun validFilledCellCount(numbers: List<Int>): Int =
    finalUiGridRowMajor(numbers).count { it != 0 }

/** Guidance when ML Kit returns fewer than [MIN_VALID_CELLS_FOR_MANUAL_ENTRY_NAV] filled cells (not for Success). */
private fun Application.weakScanFailureMessage(validCount: Int): String {
    require(validCount < MIN_VALID_CELLS_FOR_MANUAL_ENTRY_NAV)
    return when {
        validCount <= 5 -> getString(R.string.import_ticket_error_no_grid)
        else -> getString(R.string.import_ticket_error_partial_scan)
    }
}

private fun isLikelyTooZoomedForOcr(context: Context, uri: Uri): Boolean {
    val bitmap = GalleryManualTrim.decodeMaxSide(context, uri, maxSide = 1600) ?: return false
    return try {
        val rect = BingoNumberAnalyzer.tryDetectBingoGridCropRectForOcr(bitmap)
        if (rect == null) {
            return true
        }
        val imageArea = (bitmap.width * bitmap.height).toFloat().coerceAtLeast(1f)
        val gridArea = (rect.width() * rect.height()).toFloat()
        val areaFrac = gridArea / imageArea
        areaFrac >= TOO_ZOOMED_GRID_AREA_THRESHOLD
    } catch (_: Exception) {
        false
    } finally {
        if (!bitmap.isRecycled) bitmap.recycle()
    }
}

/** Row-major 5×5 list used everywhere import/review/manual prefill reads [ScanResultUiState.Success.numbers]. */
internal fun finalUiGridRowMajor(numbers: List<Int>): List<Int> {
    val t = numbers.take(25)
    return if (t.size < 25) t + List(25 - t.size) { 0 } else t
}

/**
 * Set on [ScanResultUiState.Success]. [QR] is used for static-image decodes, gallery auto-probe, and
 * the same data shape as [com.example.mamunbingoapp.ui.screens.camera.BingoLiveCameraImportScreen] (navigates
 * to Manual Entry directly, not through this VM).
 */
enum class ImportScanSource {
    PHOTO_OCR,
    QR,
}

enum class DetectionStatus { Checking, Found, NotFound }

data class ImportOcrProgressUiState(
    val stageLabel: String = "",
    val detectedGridCells: Int = 0,
    val totalGridCells: Int = 25,
    val losStatus: DetectionStatus = DetectionStatus.Checking,
    val serialStatus: DetectionStatus = DetectionStatus.Checking,
)

sealed class ScanResultUiState {
    data object Idle : ScanResultUiState()
    data object Loading : ScanResultUiState()
    /** Prefill from live-scan route args or on-device ML Kit OCR. */
    data class Success(
        val numbers: List<Int>,
        val losNumber: String? = null,
        val serialNumber: String? = null,
        val sheetName: String? = null,
        val ocrSource: HistoryOcrSource? = null,
        val scanSource: ImportScanSource = ImportScanSource.PHOTO_OCR,
    ) : ScanResultUiState()
    data class Error(
        val message: String,
        val detectedValidCount: Int? = null,
        val losNumber: String? = null,
        val serialNumber: String? = null,
    ) : ScanResultUiState()
}

/** URI and scan state for `historyPhotoImport` and scan-tab import. */
class ImportTicketViewModel(application: Application) : AndroidViewModel(application) {

    private val app get() = getApplication<Application>()

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    /** Gallery pick awaiting Apply; cleared on Apply, Cancel, or [clear]. Not used for Take Photo / GMS. */
    private val _galleryPendingEditUri = MutableStateFlow<Uri?>(null)
    val galleryPendingEditUri: StateFlow<Uri?> = _galleryPendingEditUri.asStateFlow()

    private val _scanResult = MutableStateFlow<ScanResultUiState>(ScanResultUiState.Idle)
    val scanResult: StateFlow<ScanResultUiState> = _scanResult.asStateFlow()

    private val _candidateNumbers = MutableStateFlow<List<Int>>(emptyList())
    val candidateNumbers: StateFlow<List<Int>> = _candidateNumbers.asStateFlow()

    private val _candidateBuckets = MutableStateFlow<Map<String, List<Int>>>(emptyMap())
    val candidateBuckets: StateFlow<Map<String, List<Int>>> = _candidateBuckets.asStateFlow()

    private val _prefilledSource = MutableStateFlow<String?>(null)
    val prefilledSource: StateFlow<String?> = _prefilledSource.asStateFlow()

    private val _prefilledConfidence = MutableStateFlow<Float?>(null)
    val prefilledConfidence: StateFlow<Float?> = _prefilledConfidence.asStateFlow()

    private val _ocrProgress = MutableStateFlow<ImportOcrProgressUiState?>(null)
    val ocrProgress: StateFlow<ImportOcrProgressUiState?> = _ocrProgress.asStateFlow()

    private var analysisJob: Job? = null
    private var galleryApplyJob: Job? = null
    /** Probes gallery pick for app QR before Apply; cancelled on Apply, cancel, or new pick. */
    private var galleryQrProbeJob: Job? = null

    private val _pendingScanType = MutableStateFlow<BingoScanType?>(null)

    /**
     * Scan type in effect when the gallery pipeline produced the pending crop URI (picker + uCrop).
     * Survives async handoff so Apply still routes MAIN_SHEET even if [_pendingScanType] was disturbed.
     */
    private var galleryImportScanTypeSnapshot: BingoScanType? = null

    /** Set before gallery pick or camera handoff so [analyzeTicketFromUri] routes OCR by scan target. */
    fun setPendingScanType(scanType: BingoScanType) {
        _pendingScanType.value = scanType
    }

    fun peekPendingScanType(): BingoScanType? = _pendingScanType.value

    /**
     * Updates selection to [ScanResultUiState.Idle] (image preview only). UI shows analyzing overlay only after [analyzeTicketFromUri] sets [ScanResultUiState.Loading].
     */
    fun onPhotoTaken(uri: Uri, scanType: BingoScanType? = null) {
        if (scanType != null) _pendingScanType.value = scanType
        analysisJob?.cancel()
        galleryQrProbeJob?.cancel()
        galleryQrProbeJob = null
        _galleryPendingEditUri.value = null
        _selectedImageUri.value = uri
        _candidateNumbers.value = emptyList()
        _candidateBuckets.value = emptyMap()
        _scanResult.value = ScanResultUiState.Idle
    }

    /** After gallery flow (picker + optional external crop). Tries app QR in the background; on success clears pending and sets [ImportScanSource.QR] without Apply. */
    fun setGalleryPendingEdit(uri: Uri) {
        analysisJob?.cancel()
        galleryQrProbeJob?.cancel()
        galleryImportScanTypeSnapshot = _pendingScanType.value
        _galleryPendingEditUri.value = uri
        _scanResult.value = ScanResultUiState.Idle
        _candidateNumbers.value = emptyList()
        _candidateBuckets.value = emptyMap()
        val app = getApplication<Application>().applicationContext
        galleryQrProbeJob = viewModelScope.launch {
            val qrPre = withContext(Dispatchers.IO) {
                tryDecodeBingoQrFromImageUri(app, uri)
            }
            if (_galleryPendingEditUri.value != uri) return@launch
            if (qrPre !is ImportTicketQrPreOcr.Decoded) return@launch
            _galleryPendingEditUri.value = null
            galleryImportScanTypeSnapshot = null
            _selectedImageUri.value = uri
            setScanResult(
                ScanResultUiState.Success(
                    numbers = qrPre.numbers,
                    losNumber = qrPre.los,
                    serialNumber = qrPre.serial,
                    sheetName = qrPre.sheetName,
                    ocrSource = null,
                    scanSource = ImportScanSource.QR,
                ),
            )
        }
    }

    fun cancelGalleryPendingEdit() {
        galleryApplyJob?.cancel()
        galleryApplyJob = null
        galleryQrProbeJob?.cancel()
        galleryQrProbeJob = null
        galleryImportScanTypeSnapshot = null
        _galleryPendingEditUri.value = null
    }

    /** Resolves OCR scan type for gallery Apply: prefers snapshot taken when uCrop delivered the image. */
    private fun resolveGalleryApplyScanType(): BingoScanType {
        val snap = galleryImportScanTypeSnapshot
        val pend = _pendingScanType.value
        val merged = snap ?: pend
        return if (merged != null) {
            _pendingScanType.value = merged
            merged
        } else {
            Log.w(IMPORT_TICKET_GALLERY_LOG, "gallery apply missing scan type; defaulting PLAY_PAPER")
            BingoScanType.PLAY_PAPER
        }
    }

    fun applyGalleryPendingEdit(context: Context, trimLeft: Float, trimTop: Float, trimRight: Float, trimBottom: Float) {
        galleryQrProbeJob?.cancel()
        galleryQrProbeJob = null
        val uri = _galleryPendingEditUri.value ?: return
        val scanTypeForApply = resolveGalleryApplyScanType()
        val app = context.applicationContext
        val l = trimLeft.coerceIn(0f, GalleryManualTrim.TRIM_SLIDER_MAX)
        val t = trimTop.coerceIn(0f, GalleryManualTrim.TRIM_SLIDER_MAX)
        val r = trimRight.coerceIn(0f, GalleryManualTrim.TRIM_SLIDER_MAX)
        val b = trimBottom.coerceIn(0f, GalleryManualTrim.TRIM_SLIDER_MAX)
        if (l <= 1e-5f && t <= 1e-5f && r <= 1e-5f && b <= 1e-5f) {
            _galleryPendingEditUri.value = null
            onPhotoTaken(uri, scanTypeForApply)
            analyzeTicketFromUri(
                app,
                uri,
                bypassInternalGridCrop = true,
                scanType = scanTypeForApply,
            )
            return
        }
        if (galleryApplyJob?.isActive == true) return
        galleryApplyJob = viewModelScope.launch {
            try {
                val trimmedUri = withContext(Dispatchers.IO) {
                    GalleryManualTrim.buildApplyTempJpegUri(app, uri, l, t, r, b)
                }
                if (_galleryPendingEditUri.value != uri) return@launch
                val finalUri = trimmedUri ?: uri
                _galleryPendingEditUri.value = null
                onPhotoTaken(finalUri, scanTypeForApply)
                analyzeTicketFromUri(
                    app,
                    finalUri,
                    bypassInternalGridCrop = true,
                    scanType = scanTypeForApply,
                )
            } finally {
                galleryApplyJob = null
            }
        }
    }

    /**
     * Runs on-device ML Kit text recognition and maps results into [ScanResultUiState.Success]
     * (row-major numbers for manual entry when [HistoryPhotoImport] uses row-major prefill).
     */
    fun analyzeTicketFromUri(
        context: Context,
        uri: Uri,
        bypassInternalGridCrop: Boolean = false,
        scanType: BingoScanType? = null,
    ) {
        val effectiveScanType = scanType ?: _pendingScanType.value ?: run {
            Log.w(IMPORT_TICKET_LOG, "analyzeTicketFromUri missing scan type; defaulting PLAY_PAPER")
            BingoScanType.PLAY_PAPER
        }
        analysisJob?.cancel()
        analysisJob = viewModelScope.launch {
            _scanResult.value = ScanResultUiState.Loading
            _ocrProgress.value = ImportOcrProgressUiState(stageLabel = app.getString(R.string.ocr_stage_checking_qr))
            val qrPre = withContext(Dispatchers.IO) {
                tryDecodeBingoQrFromImageUri(context.applicationContext, uri)
            }
            if (uri != _selectedImageUri.value) {
                Log.w(IMPORT_TICKET_LOG, "analyze aborted: uri no longer matches selected image")
                return@launch
            }
            when (qrPre) {
                is ImportTicketQrPreOcr.Decoded -> {
                    setScanResult(
                        ScanResultUiState.Success(
                            numbers = qrPre.numbers,
                            losNumber = qrPre.los,
                            serialNumber = qrPre.serial,
                            sheetName = qrPre.sheetName,
                            ocrSource = null,
                            scanSource = ImportScanSource.QR,
                        ),
                    )
                    return@launch
                }
                is ImportTicketQrPreOcr.NoBingoQrContinueOcr -> Unit
            }
            _ocrProgress.value = (_ocrProgress.value ?: ImportOcrProgressUiState())
                .copy(stageLabel = app.getString(R.string.ocr_stage_detecting_grid))
            if (effectiveScanType == BingoScanType.PLAY_PAPER) {
                val tooZoomed = withContext(Dispatchers.IO) {
                    isLikelyTooZoomedForOcr(context.applicationContext, uri)
                }
                if (uri != _selectedImageUri.value) {
                    Log.w(IMPORT_TICKET_LOG, "analyze aborted after zoom check: stale uri")
                    return@launch
                }
                if (tooZoomed) {
                    Log.w(
                        IMPORT_TICKET_LOG,
                        "PLAY_PAPER scan blocked too-zoomed; user should widen frame",
                    )
                    _scanResult.value = ScanResultUiState.Error(
                        message = app.getString(R.string.import_ticket_zoom_hint),
                    )
                    return@launch
                }
            }
            _ocrProgress.value = (_ocrProgress.value ?: ImportOcrProgressUiState())
                .copy(stageLabel = app.getString(R.string.ocr_stage_reading_numbers))
            // Advance to "Reading ticket markers…" after 1.8 s if OCR is still running.
            // Cancelled immediately when OCR returns — does not delay the result.
            val markerStageJob = launch {
                delay(1800)
                if (_scanResult.value is ScanResultUiState.Loading) {
                    _ocrProgress.value = (_ocrProgress.value ?: ImportOcrProgressUiState())
                        .copy(stageLabel = app.getString(R.string.ocr_stage_reading_markers))
                }
            }
            val result = withContext(Dispatchers.IO) {
                when (effectiveScanType) {
                    BingoScanType.ONLINE -> runCatching {
                        OnlineBingoOcr.analyzeUri(context.applicationContext, uri)
                    }
                    BingoScanType.MAIN_SHEET -> try {
                        Result.success(
                            MainSheetScanAnalyzer.analyzeUri(
                                context.applicationContext,
                                uri,
                                bypassInternalGridCrop = true,
                                preCropCameraForStripOcr = false,
                            ),
                        )
                    } catch (e: Exception) {
                        Result.failure(e)
                    }
                    BingoScanType.PLAY_PAPER -> runCatching {
                        PlayPaperBingoOcr.analyzeUri(
                            context.applicationContext,
                            uri,
                            bypassInternalGridCrop = bypassInternalGridCrop,
                            preCropCameraForStripOcr = false,
                        )
                    }
                }
            }
            markerStageJob.cancel()
            if (uri != _selectedImageUri.value) {
                Log.w(IMPORT_TICKET_LOG, "analyze aborted after OCR: uri no longer selected")
                return@launch
            }
            val minValidForScan = when (effectiveScanType) {
                BingoScanType.ONLINE -> OnlineBingoOcr.MIN_VALID_CELLS_FOR_SUCCESS
                BingoScanType.MAIN_SHEET -> MainSheetBingoOcr.MIN_VALID_CELLS_FOR_SUCCESS
                else -> MIN_VALID_CELLS_FOR_MANUAL_ENTRY_NAV
            }
            result.fold(
                onSuccess = { outcome ->
                    val validCount = validFilledCellCount(outcome.numbersRowMajor)
                    _ocrProgress.value = ImportOcrProgressUiState(
                        stageLabel = app.getString(R.string.ocr_stage_finalizing),
                        detectedGridCells = validCount,
                        losStatus = if (!outcome.losNumber.isNullOrBlank()) DetectionStatus.Found else DetectionStatus.NotFound,
                        serialStatus = if (!outcome.serialNumber.isNullOrBlank()) DetectionStatus.Found else DetectionStatus.NotFound,
                    )
                    when {
                        validCount < minValidForScan -> {
                            val message = app.weakScanFailureMessage(validCount)
                            Log.w(
                                IMPORT_TICKET_LOG,
                                "OCR weak result type=${effectiveScanType.name} validCount=$validCount",
                            )
                            _scanResult.value = ScanResultUiState.Error(
                                message = message,
                                detectedValidCount = validCount,
                                losNumber = outcome.losNumber?.takeIf { it.isNotBlank() },
                                serialNumber = outcome.serialNumber?.takeIf { it.isNotBlank() },
                            )
                        }
                        else ->
                            setScanResult(
                                ScanResultUiState.Success(
                                    numbers = outcome.numbersRowMajor,
                                    losNumber = outcome.losNumber?.takeIf { it.isNotBlank() },
                                    serialNumber = outcome.serialNumber?.takeIf { it.isNotBlank() },
                                    ocrSource = HistoryOcrSource.ML_KIT,
                                    scanSource = ImportScanSource.PHOTO_OCR,
                                ),
                            )
                    }
                },
                onFailure = { e ->
                    Log.e(IMPORT_TICKET_LOG, "OCR failed type=${effectiveScanType.name}", e)
                    _scanResult.value = ScanResultUiState.Error(e.message ?: app.getString(R.string.import_ticket_error_ocr_failed))
                },
            )
        }
    }

    fun setScanResult(state: ScanResultUiState) {
        val toSet = when (state) {
            is ScanResultUiState.Success -> {
                val grid = finalUiGridRowMajor(state.numbers)
                state.copy(numbers = grid)
            }
            else -> state
        }
        if (toSet is ScanResultUiState.Success) {
            setCandidateNumbers(toSet.numbers.filter { it != 0 })
        }
        _scanResult.value = toSet
    }

    fun setCandidateNumbers(values: List<Int>) {
        val buckets = BingoNumberAnalyzer.bucketizeCandidateNumbers(values)
        _candidateNumbers.value = buckets.flat
        _candidateBuckets.value = mapOf(
            "B" to buckets.b,
            "I" to buckets.i,
            "N" to buckets.n,
            "G" to buckets.g,
            "O" to buckets.o,
        )
    }

    fun prefillFromLiveScan(
        numbers: List<Int>,
        source: String?,
        confidence: Float?,
        losNumber: String? = null,
        serialNumber: String? = null,
    ) {
        if (numbers.isEmpty()) return
        _galleryPendingEditUri.value = null
        _selectedImageUri.value = null
        setScanResult(
            ScanResultUiState.Success(
                numbers,
                losNumber = losNumber?.takeIf { it.isNotBlank() },
                serialNumber = serialNumber?.takeIf { it.isNotBlank() },
                ocrSource = null,
                scanSource = ImportScanSource.PHOTO_OCR,
            ),
        )
        _prefilledSource.value = source
        _prefilledConfidence.value = confidence
    }

    fun clear() {
        analysisJob?.cancel()
        analysisJob = null
        galleryApplyJob?.cancel()
        galleryApplyJob = null
        galleryQrProbeJob?.cancel()
        galleryQrProbeJob = null
        _galleryPendingEditUri.value = null
        _selectedImageUri.value = null
        _scanResult.value = ScanResultUiState.Idle
        _candidateNumbers.value = emptyList()
        _candidateBuckets.value = emptyMap()
        _prefilledSource.value = null
        _prefilledConfidence.value = null
        _ocrProgress.value = null
        galleryImportScanTypeSnapshot = null
        _pendingScanType.value = null
    }

    fun hasActiveImportSession(): Boolean =
        _galleryPendingEditUri.value != null ||
            _selectedImageUri.value != null ||
            _scanResult.value !is ScanResultUiState.Idle

    override fun onCleared() {
        analysisJob?.cancel()
        galleryQrProbeJob?.cancel()
        super.onCleared()
    }
}
