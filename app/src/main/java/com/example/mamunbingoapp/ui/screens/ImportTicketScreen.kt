package com.example.mamunbingoapp.ui.screens

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.mamunbingoapp.ui.importgallery.MamunUcropActivity
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.UCropActivity
import com.yalantis.ucrop.model.AspectRatio
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.theme.Background
import com.example.mamunbingoapp.theme.HeaderGradientEnd
import com.example.mamunbingoapp.theme.CardBorderGreen
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.IconContainerBg
import com.example.mamunbingoapp.theme.Scrim
import com.example.mamunbingoapp.theme.OnPrimary
import com.example.mamunbingoapp.theme.Primary
import com.example.mamunbingoapp.theme.PrimaryContainer
import com.example.mamunbingoapp.theme.PrimaryDark
import com.example.mamunbingoapp.theme.Slate200
import com.example.mamunbingoapp.theme.MamunBingoTheme
import com.example.mamunbingoapp.theme.WarningText
import com.example.mamunbingoapp.ui.components.AppConfirmDialog
import com.example.mamunbingoapp.ui.components.APP_SECTION_BORDER_ALPHA
import com.example.mamunbingoapp.ui.components.AppBottomBar
import com.example.mamunbingoapp.ui.components.AppHeaderPageLayout
import com.example.mamunbingoapp.ui.components.AppTab
import com.example.mamunbingoapp.ui.components.AppTopBar
import com.example.mamunbingoapp.ui.components.ImportTicketFailedScanContent
import com.example.mamunbingoapp.ui.components.ImportTicketPhotoActionRow
import com.example.mamunbingoapp.ui.components.AppSectionSurface
import com.example.mamunbingoapp.ui.components.ProcessingDataCard
import com.example.mamunbingoapp.ui.components.ScanningAnalysisAnimation
import com.example.mamunbingoapp.viewmodel.GalleryManualTrim
import com.example.mamunbingoapp.viewmodel.ImportTicketViewModel
import com.example.mamunbingoapp.viewmodel.ScanResultUiState
import com.example.mamunbingoapp.viewmodel.finalUiGridRowMajor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/** Set on `main` before navigating to `historyPhotoImport`; destination removes it and starts OCR once. */
const val PENDING_HISTORY_PHOTO_IMPORT_URI_KEY = "pendingHistoryPhotoImportUri"

/** Optional [com.example.mamunbingoapp.domain.model.BingoScanType] name from camera handoff. */
const val PENDING_HISTORY_PHOTO_IMPORT_SCAN_TYPE_KEY = "pendingHistoryPhotoImportScanType"

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private const val IMPORT_GALLERY_UCROP_LOG = "ImportTicketGalleryUcrop"

/** Gallery pending screen: post-uCrop edge trim sliders (hidden while uCrop freestyle handles framing). */
private const val SHOW_POST_UCROP_GALLERY_TRIM_SLIDERS = false

private fun copyGalleryPickToCacheSourceUri(context: Context, pickerUri: Uri): Uri? =
    try {
        val destFile = File.createTempFile("import_gallery_src_", ".jpg", context.cacheDir)
        val ok = context.contentResolver.openInputStream(pickerUri)?.use { input ->
            destFile.outputStream().use { output -> input.copyTo(output) }
            true
        } ?: false
        if (!ok) {
            destFile.delete()
            null
        } else {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                destFile,
            )
        }
    } catch (e: Exception) {
        Log.e(IMPORT_GALLERY_UCROP_LOG, "copy gallery pick", e)
        null
    }

private fun deleteAppCacheContentUri(context: Context, uri: Uri) {
    try {
        if (uri.scheme == "content" && uri.authority == "${context.packageName}.fileprovider") {
            context.contentResolver.delete(uri, null, null)
        }
    } catch (e: Exception) {
        Log.w(IMPORT_GALLERY_UCROP_LOG, "delete cache uri", e)
    }
}

private fun buildImportTicketUcropIntent(context: Context, sourceUri: Uri, destUri: Uri): Intent {
    val toolbar = ContextCompat.getColor(context, R.color.ucrop_toolbar_green)
    val statusBar = ContextCompat.getColor(context, R.color.ucrop_status_bar_green)
    val toolbarContent = ContextCompat.getColor(context, R.color.ucrop_toolbar_content_light)
    val canvasBg = ContextCompat.getColor(context, R.color.ucrop_canvas_background)
    val cropFrameColor = ContextCompat.getColor(context, R.color.white)
    val cropGridColor = AndroidColor.argb(175, 255, 255, 255)
    val density = context.resources.displayMetrics.density
    val frameStrokePx = (4f * density).toInt().coerceIn(4, 10)
    val gridStrokePx = (2f * density).toInt().coerceIn(2, 5)
    val options = UCrop.Options().apply {
        setCompressionFormat(Bitmap.CompressFormat.JPEG)
        setCompressionQuality(92)
        setFreeStyleCropEnabled(true)
        setAllowedGestures(UCropActivity.ALL, UCropActivity.ALL, UCropActivity.ALL)
        setMaxScaleMultiplier(28f)
        setImageToCropBoundsAnimDuration(280)
        setHideBottomControls(false)
        setShowCropFrame(true)
        setShowCropGrid(true)
        setCircleDimmedLayer(false)
        setAspectRatioOptions(
            0,
            AspectRatio("3:4", 3f, 4f),
            AspectRatio("4:3", 4f, 3f),
            AspectRatio("1:1", 1f, 1f),
            AspectRatio("9:16", 9f, 16f),
            AspectRatio("16:9", 16f, 9f),
        )
        setDimmedLayerColor(AndroidColor.argb(200, 0, 0, 0))
        setCropFrameColor(cropFrameColor)
        setCropFrameStrokeWidth(frameStrokePx)
        setCropGridColor(cropGridColor)
        setCropGridStrokeWidth(gridStrokePx)
        setToolbarTitle(context.getString(R.string.import_ticket_ucrop_title))
        setToolbarColor(toolbar)
        setStatusBarColor(statusBar)
        setToolbarWidgetColor(toolbarContent)
        setActiveControlsWidgetColor(toolbar)
        setRootViewBackgroundColor(canvasBg)
    }
    val intent = UCrop.of(sourceUri, destUri)
        .withOptions(options)
        .withMaxResultSize(4096, 4096)
        .getIntent(context)
        .setClass(context, MamunUcropActivity::class.java)
    Log.d(
        IMPORT_GALLERY_UCROP_LOG,
        "uCropConfig freestyle=true hideBottomControls=false gestures=ALL dynamicRatio=true rotateVisible=true",
    )
    Log.d(IMPORT_GALLERY_UCROP_LOG, "uCropVisuals dimmed=strong frame=strong grid=visible")
    return intent
}

private fun prepareImportTicketUcropIntentForLaunch(
    context: Context,
    sourceUri: Uri,
    destUri: Uri,
): Intent {
    val intent = buildImportTicketUcropIntent(context, sourceUri, destUri)
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    val clip = ClipData.newUri(context.contentResolver, "source", sourceUri)
    clip.addItem(ClipData.Item(destUri))
    intent.clipData = clip
    val resolved = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
    Log.d(
        IMPORT_GALLERY_UCROP_LOG,
        "uCropLaunch activity=${intent.component?.className} (expect ${MamunUcropActivity::class.java.name})",
    )
    val targetPkg = resolved?.activityInfo?.packageName
    if (targetPkg != null) {
        try {
            context.grantUriPermission(targetPkg, sourceUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.grantUriPermission(
                targetPkg,
                destUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
            )
        } catch (e: SecurityException) {
            Log.w(IMPORT_GALLERY_UCROP_LOG, "grantUriPermission ucrop", e)
        }
    }
    return intent
}

/**
 * **Gallery only:** [PickVisualMedia] → copy to cache → [uCrop] (3:4) → [onCroppedImageReady].
 * Picker cancel = no callback. uCrop cancel/error/copy/launch failure = idle, no fallback to original pick.
 * Take Photo / GMS unchanged. Caller maps to [ImportTicketViewModel.setGalleryPendingEdit].
 */
@Composable
fun rememberImportTicketGalleryImagePickLauncher(
    onCroppedImageReady: (Uri) -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val appCtx = context.applicationContext
    val onReady by rememberUpdatedState(onCroppedImageReady)
    val scope = rememberCoroutineScope()
    val pendingSourceCopyUri = remember { mutableStateOf<Uri?>(null) }
    val pendingDestUri = remember { mutableStateOf<Uri?>(null) }
    val cropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val srcCopy = pendingSourceCopyUri.value
        val dest = pendingDestUri.value
        pendingSourceCopyUri.value = null
        pendingDestUri.value = null
        fun cleanupBoth() {
            srcCopy?.let { deleteAppCacheContentUri(appCtx, it) }
            dest?.let { deleteAppCacheContentUri(appCtx, it) }
        }
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                val out = result.data?.let { UCrop.getOutput(it) }
                if (out != null) {
                    srcCopy?.let { deleteAppCacheContentUri(appCtx, it) }
                    onReady(out)
                } else {
                    Log.e(IMPORT_GALLERY_UCROP_LOG, "uCrop RESULT_OK but no output URI")
                    cleanupBoth()
                }
            }
            Activity.RESULT_CANCELED -> cleanupBoth()
            else -> {
                val err = result.data?.let { UCrop.getError(it) }
                Log.e(IMPORT_GALLERY_UCROP_LOG, "uCrop failed or dismissed", err)
                cleanupBoth()
            }
        }
    }
    val pickLauncher = rememberLauncherForActivityResult(
        contract = PickVisualMedia(),
    ) { picked: Uri? ->
        if (picked == null) return@rememberLauncherForActivityResult
        scope.launch {
            val sourceCopyUri = withContext(Dispatchers.IO) {
                copyGalleryPickToCacheSourceUri(appCtx, picked)
            }
            if (sourceCopyUri == null) {
                Log.e(IMPORT_GALLERY_UCROP_LOG, "copy pick to cache failed; staying idle")
                return@launch
            }
            val destFile = try {
                File.createTempFile("import_gallery_crop_", ".jpg", appCtx.cacheDir)
            } catch (e: Exception) {
                Log.e(IMPORT_GALLERY_UCROP_LOG, "temp dest file", e)
                deleteAppCacheContentUri(appCtx, sourceCopyUri)
                return@launch
            }
            val outputUri = FileProvider.getUriForFile(
                appCtx,
                "${appCtx.packageName}.fileprovider",
                destFile,
            )
            pendingSourceCopyUri.value = sourceCopyUri
            pendingDestUri.value = outputUri
            try {
                Toast.makeText(
                    context,
                    context.getString(R.string.import_ticket_gallery_crop_toast),
                    Toast.LENGTH_LONG,
                ).show()
                val cropIntent = prepareImportTicketUcropIntentForLaunch(appCtx, sourceCopyUri, outputUri)
                cropLauncher.launch(cropIntent)
            } catch (e: Exception) {
                Log.e(IMPORT_GALLERY_UCROP_LOG, "launch uCrop", e)
                pendingSourceCopyUri.value = null
                pendingDestUri.value = null
                deleteAppCacheContentUri(appCtx, sourceCopyUri)
                deleteAppCacheContentUri(appCtx, outputUri)
            }
        }
    }
    return remember(pickLauncher, cropLauncher, scope) {
        {
            pickLauncher.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        }
    }
}

@Composable
fun ImportTicketScreen(
    onBack: () -> Unit,
    onDiscardAndBack: () -> Unit,
    onTakePhoto: () -> Unit,
    onReviewClicked: () -> Unit,
    onSaveAndRoomClicked: () -> Unit = onReviewClicked,
    showBottomBar: Boolean = true,
    imageSource: String? = null,
    viewModel: ImportTicketViewModel = viewModel()
) {
    val context = LocalContext.current
    val idleHint = stringResource(R.string.import_ticket_idle_hint)
    val selectedUri by viewModel.selectedImageUri.collectAsState()
    val galleryPendingUri by viewModel.galleryPendingEditUri.collectAsState()
    val displayUri = galleryPendingUri ?: selectedUri
    val scanResult by viewModel.scanResult.collectAsState()
    val pickImageFromGallery = rememberImportTicketGalleryImagePickLauncher { uri ->
        viewModel.setGalleryPendingEdit(uri)
    }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var pendingReplaceAction by remember { mutableStateOf<PendingReplace?>(null) }
    val isDirty = viewModel.hasActiveImportSession()
    val isAnalyzing = scanResult is ScanResultUiState.Loading
    val allowScroll = scanResult !is ScanResultUiState.Error
    val scrollState = rememberScrollState()
    var previousScanResult by remember { mutableStateOf<ScanResultUiState?>(null) }
    var userTouchedScroll by remember { mutableStateOf(false) }
    var programmaticScrollInProgress by remember { mutableStateOf(false) }
    var scrollValueObservedOnce by remember { mutableStateOf(false) }

    LaunchedEffect(scrollState.value) {
        if (!scrollValueObservedOnce) {
            scrollValueObservedOnce = true
            return@LaunchedEffect
        }
        if (!programmaticScrollInProgress) userTouchedScroll = true
    }
    LaunchedEffect(scanResult) {
        val prev = previousScanResult
        if (allowScroll &&
            (scanResult is ScanResultUiState.Success || scanResult is ScanResultUiState.Error) &&
            prev is ScanResultUiState.Loading &&
            !userTouchedScroll
        ) {
            programmaticScrollInProgress = true
            scrollState.animateScrollTo(scrollState.maxValue)
            programmaticScrollInProgress = false
        }
        previousScanResult = scanResult
    }
    LaunchedEffect(selectedUri, galleryPendingUri) {
        if (selectedUri != null || galleryPendingUri != null) {
            userTouchedScroll = false
            if (!userTouchedScroll) {
                programmaticScrollInProgress = true
                scrollState.animateScrollTo(0)
                programmaticScrollInProgress = false
            }
        }
    }

    BackHandler(enabled = true) {
        when {
            galleryPendingUri != null -> viewModel.cancelGalleryPendingEdit()
            isDirty -> showDiscardDialog = true
            else -> onBack()
        }
    }

    AppHeaderPageLayout(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.import_ticket_title),
                showBack = true,
                onBackClick = {
                    when {
                        galleryPendingUri != null -> viewModel.cancelGalleryPendingEdit()
                        isDirty -> showDiscardDialog = true
                        else -> onBack()
                    }
                }
            )
        },
        content = {
            val contentPadding = Modifier
                .padding(horizontal = Dimens.screenHorizontalPadding)
                .padding(top = Dimens.spacing16, bottom = Dimens.spacing24)
            val scrollModifier = if (allowScroll) Modifier.verticalScroll(scrollState) else Modifier
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .then(scrollModifier)
                    .then(contentPadding)
            ) {
                ImportTicketMainContent(
                    selectedUri = displayUri,
                    galleryPendingEditUri = galleryPendingUri,
                    scanResult = scanResult,
                    isAnalyzing = isAnalyzing,
                    imageSource = imageSource,
                    idleHint = idleHint,
                    onTakePhoto = { if (isDirty) pendingReplaceAction = PendingReplace.TakePhoto else onTakePhoto() },
                    onPickFromGallery = { if (isDirty) pendingReplaceAction = PendingReplace.PickGallery else pickImageFromGallery() },
                    onSecondaryOutlinedClick = { if (isDirty) pendingReplaceAction = PendingReplace.PickGallery else pickImageFromGallery() },
                    onGalleryApply = { l, t, r, b -> viewModel.applyGalleryPendingEdit(context, l, t, r, b) },
                    onGalleryCancel = { viewModel.cancelGalleryPendingEdit() },
                    modifier = Modifier.fillMaxSize()
                )
            }
            if (showBottomBar) {
                AppBottomBar(selectedTab = AppTab.Scan, onTabSelected = {})
            }
        }
    )
    AppConfirmDialog(
        visible = showDiscardDialog,
        title = stringResource(R.string.import_ticket_discard_scan_title),
        message = stringResource(R.string.import_ticket_discard_scan_message),
        confirmText = stringResource(R.string.import_ticket_gallery_discard),
        cancelText = stringResource(R.string.common_stay),
        onConfirm = {
            viewModel.clear()
            showDiscardDialog = false
            onDiscardAndBack()
        },
        onCancel = { showDiscardDialog = false },
        onDismiss = { showDiscardDialog = false },
    )
    AppConfirmDialog(
        visible = pendingReplaceAction != null,
        title = stringResource(R.string.import_ticket_replace_scan_title),
        message = stringResource(R.string.import_ticket_replace_scan_message),
        confirmText = stringResource(R.string.common_replace),
        cancelText = stringResource(R.string.settings_cancel),
        onConfirm = {
            when (pendingReplaceAction) {
                PendingReplace.TakePhoto -> onTakePhoto()
                PendingReplace.PickGallery -> pickImageFromGallery()
                null -> { }
            }
            pendingReplaceAction = null
        },
        onCancel = { pendingReplaceAction = null },
        onDismiss = { pendingReplaceAction = null },
    )
}

private enum class PendingReplace { TakePhoto, PickGallery }

private const val FULL_GRID_COUNT = 25

private data class ScanSummaryUi(val numbers: String, val grid: String, val confidence: Float)

private fun scanSummaryMetrics(scanResult: ScanResultUiState): ScanSummaryUi = when (scanResult) {
    is ScanResultUiState.Success -> {
        val grid = finalUiGridRowMajor(scanResult.numbers)
        val count = grid.count { it != 0 }
        ScanSummaryUi(
            numbers = count.toString(),
            grid = if (count > 0) "5×5" else "—",
            confidence = (count / 25f).coerceIn(0f, 1f),
        )
    }
    is ScanResultUiState.Error -> ScanSummaryUi("—", "—", 0f)
    else -> ScanSummaryUi("—", "—", 0f)
}

@Composable
private fun ctaHelperLine(scanResult: ScanResultUiState, idleHint: String): Pair<String, Boolean>? = when (scanResult) {
    is ScanResultUiState.Idle -> Pair(idleHint, false)
    is ScanResultUiState.Loading -> null
    is ScanResultUiState.Error -> null
    is ScanResultUiState.Success -> when (finalUiGridRowMajor(scanResult.numbers).count { it != 0 }) {
        0 -> Pair(stringResource(R.string.import_ticket_cta_no_numbers), false)
        in 1..(FULL_GRID_COUNT - 1) -> Pair(stringResource(R.string.import_ticket_cta_partial_numbers), true)
        else -> null
    }
}

/**
 * Import ticket column: hero preview, metrics when not success, photo actions; auto Manual Entry is handled in NavGraph when the scan is strong enough.
 * Analyzing feedback uses `ImportTicketHeroAnalyzingOverlay` on the hero only when [ScanResultUiState.Loading] (no separate card below).
 * @param selectedUri Hero preview (committed image or gallery pending preview).
 * @param galleryPendingEditUri When non-null, shows Apply/Discard before OCR; Gallery-only.
 * @param suppressHeroImage when true, hero preview URI is not drawn (e.g. strong scan handoff before nav).
 * @param onGalleryApply Trim fractions 0…[GalleryManualTrim.TRIM_SLIDER_MAX] per edge (gallery pending only).
 */
@Composable
fun ImportTicketMainContent(
    selectedUri: Uri?,
    scanResult: ScanResultUiState,
    isAnalyzing: Boolean,
    imageSource: String?,
    idleHint: String,
    onTakePhoto: () -> Unit,
    onPickFromGallery: () -> Unit,
    onSecondaryOutlinedClick: () -> Unit,
    galleryPendingEditUri: Uri? = null,
    onGalleryApply: (trimLeft: Float, trimTop: Float, trimRight: Float, trimBottom: Float) -> Unit = { _, _, _, _ -> },
    onGalleryCancel: () -> Unit = {},
    suppressHeroImage: Boolean = false,
    modifier: Modifier = Modifier,
) {
    if (scanResult is ScanResultUiState.Error) {
        val err = scanResult
        val scanFailedFallback = stringResource(R.string.import_ticket_scan_failed_fallback)
        val primary = err.message.takeIf { it.isNotBlank() } ?: scanFailedFallback
        val errHeroUri = if (suppressHeroImage) null else selectedUri
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            if (errHeroUri != null) {
                HeroBannerCard(
                    selectedImageUri = errHeroUri,
                    asyncImageModel = null,
                    isAnalyzing = false,
                    expandVertically = false,
                    scanAgainOnError = null,
                    heroOverlay = null,
                    compactPadding = false,
                    appPreviewStyle = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 128.dp, max = 168.dp),
                )
                Spacer(modifier = Modifier.height(Dimens.spacing8))
            }
            ImportTicketFailedScanContent(
                primaryMessage = primary,
                helpText = stringResource(R.string.import_ticket_scan_tips_footer),
                onTakePhoto = onTakePhoto,
                onPickFromGallery = onPickFromGallery,
                enabled = !isAnalyzing,
                modifier = Modifier.fillMaxWidth(),
                detectedValidCount = err.detectedValidCount,
                losNumber = err.losNumber,
                serialNumber = err.serialNumber,
            )
            Spacer(modifier = Modifier.height(Dimens.spacing12))
        }
    } else {
    val galleryCtx = LocalContext.current.applicationContext
    var trimLeft by remember { mutableFloatStateOf(0f) }
    var trimTop by remember { mutableFloatStateOf(0f) }
    var trimRight by remember { mutableFloatStateOf(0f) }
    var trimBottom by remember { mutableFloatStateOf(0f) }
    var heroAsyncImage by remember { mutableStateOf<Any?>(null) }
    LaunchedEffect(galleryPendingEditUri) {
        trimLeft = 0f
        trimTop = 0f
        trimRight = 0f
        trimBottom = 0f
        heroAsyncImage = null
    }
    LaunchedEffect(galleryPendingEditUri, trimLeft, trimTop, trimRight, trimBottom) {
        val u = galleryPendingEditUri
        if (u == null) {
            heroAsyncImage = null
            return@LaunchedEffect
        }
        if (trimLeft <= 1e-5f && trimTop <= 1e-5f && trimRight <= 1e-5f && trimBottom <= 1e-5f) {
            heroAsyncImage = null
            return@LaunchedEffect
        }
        val bmp = withContext(Dispatchers.IO) {
            GalleryManualTrim.previewBitmap(galleryCtx, u, trimLeft, trimTop, trimRight, trimBottom)
        }
        if (galleryPendingEditUri == u) heroAsyncImage = bmp
    }
    val heroUri = if (suppressHeroImage) null else selectedUri
    val galleryHeroModel = if (galleryPendingEditUri != null) heroAsyncImage else null
    val preScanIdle = scanResult is ScanResultUiState.Idle && selectedUri == null
    val showSaveRow = scanResult is ScanResultUiState.Success
    val fullGridSuccess = (scanResult as? ScanResultUiState.Success)?.let {
        finalUiGridRowMajor(it.numbers).count { n -> n != 0 } == FULL_GRID_COUNT
    } == true
    val useExpandHero = scanResult !is ScanResultUiState.Success &&
        !preScanIdle &&
        selectedUri != null &&
        (scanResult is ScanResultUiState.Idle || scanResult is ScanResultUiState.Loading)
    val compactHero = heroUri != null
    val summary = scanSummaryMetrics(scanResult)
    val successForGrid = scanResult as? ScanResultUiState.Success
    val showLargePhotoRow = scanResult !is ScanResultUiState.Success
    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (preScanIdle) {
                    Modifier.verticalScroll(rememberScrollState())
                } else {
                    Modifier.fillMaxHeight()
                },
            )
            .then(if (showSaveRow) Modifier else Modifier.animateContentSize()),
    ) {
        if (useExpandHero) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 140.dp)
            ) {
                HeroBannerCard(
                    selectedImageUri = heroUri,
                    asyncImageModel = galleryHeroModel,
                    isAnalyzing = isAnalyzing,
                    expandVertically = true,
                    scanAgainOnError = null,
                    heroOverlay = null,
                    compactPadding = false,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            HeroBannerCard(
                selectedImageUri = heroUri,
                asyncImageModel = galleryHeroModel,
                isAnalyzing = isAnalyzing,
                expandVertically = false,
                scanAgainOnError = null,
                heroOverlay = null,
                compactPadding = false,
                appPreviewStyle = preScanIdle,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        when {
                            preScanIdle -> Modifier.heightIn(min = 120.dp, max = 152.dp)
                            compactHero -> Modifier.heightIn(max = 156.dp)
                            else -> Modifier
                        },
                    ),
            )
        }
        Spacer(modifier = Modifier.height(if (preScanIdle) Dimens.spacing8 else Dimens.spacing10))
        if (showLargePhotoRow) {
            if (galleryPendingEditUri != null) {
                Text(
                    text = stringResource(R.string.import_ticket_gallery_preview_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (SHOW_POST_UCROP_GALLERY_TRIM_SLIDERS) {
                    Spacer(modifier = Modifier.height(Dimens.spacing8))
                    ImportTicketGalleryTrimSliders(
                        trimLeft = trimLeft,
                        trimTop = trimTop,
                        trimRight = trimRight,
                        trimBottom = trimBottom,
                        onTrimLeft = { trimLeft = it },
                        onTrimTop = { trimTop = it },
                        onTrimRight = { trimRight = it },
                        onTrimBottom = { trimBottom = it },
                        enabled = !isAnalyzing,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Spacer(modifier = Modifier.height(Dimens.spacing12))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spacing12),
                ) {
                    OutlinedButton(
                        onClick = onGalleryCancel,
                        enabled = !isAnalyzing,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.import_ticket_gallery_discard))
                    }
                    Button(
                        onClick = { onGalleryApply(trimLeft, trimTop, trimRight, trimBottom) },
                        enabled = !isAnalyzing,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    ) {
                        Text(stringResource(R.string.import_ticket_gallery_apply))
                    }
                }
            } else {
                ImportTicketPhotoActionRow(
                    onTakePhoto = onTakePhoto,
                    onPickFromGallery = onPickFromGallery,
                    enabled = !isAnalyzing,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (preScanIdle) {
                Spacer(modifier = Modifier.height(Dimens.spacing8))
                ImportTicketPrescanTipsCard(modifier = Modifier.fillMaxWidth())
            } else {
                Spacer(modifier = Modifier.height(Dimens.spacing8))
            }
        }
        if (scanResult is ScanResultUiState.Success && imageSource != null) {
            Text(
                text = stringResource(R.string.import_ticket_source_format, imageSource),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(Dimens.spacing16))
        }
        ctaHelperLine(scanResult, idleHint)?.let { (text, useWarningColor) ->
            val gap = when {
                preScanIdle -> Dimens.spacing8
                fullGridSuccess -> Dimens.spacing8
                else -> Dimens.spacing16
            }
            Spacer(modifier = Modifier.height(gap))
            Text(
                text = text,
                style = if (preScanIdle) {
                    MaterialTheme.typography.labelSmall
                } else {
                    MaterialTheme.typography.bodySmall
                },
                color = when {
                    useWarningColor -> WarningText
                    preScanIdle -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(if (preScanIdle) Dimens.spacing12 else gap))
        }
    }
    }
}

@Composable
private fun ImportTicketGalleryTrimSliders(
    trimLeft: Float,
    trimTop: Float,
    trimRight: Float,
    trimBottom: Float,
    onTrimLeft: (Float) -> Unit,
    onTrimTop: (Float) -> Unit,
    onTrimRight: (Float) -> Unit,
    onTrimBottom: (Float) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val max = GalleryManualTrim.TRIM_SLIDER_MAX
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimens.spacing4),
    ) {
        GalleryTrimRow(
            label = stringResource(R.string.import_ticket_trim_left),
            value = trimLeft,
            onValueChange = onTrimLeft,
            valueRangeEnd = max,
            enabled = enabled,
        )
        GalleryTrimRow(
            label = stringResource(R.string.import_ticket_trim_top),
            value = trimTop,
            onValueChange = onTrimTop,
            valueRangeEnd = max,
            enabled = enabled,
        )
        GalleryTrimRow(
            label = stringResource(R.string.import_ticket_trim_right),
            value = trimRight,
            onValueChange = onTrimRight,
            valueRangeEnd = max,
            enabled = enabled,
        )
        GalleryTrimRow(
            label = stringResource(R.string.import_ticket_trim_bottom),
            value = trimBottom,
            onValueChange = onTrimBottom,
            valueRangeEnd = max,
            enabled = enabled,
        )
    }
}

@Composable
private fun GalleryTrimRow(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRangeEnd: Float,
    enabled: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.widthIn(min = 52.dp),
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..valueRangeEnd,
            enabled = enabled,
            modifier = Modifier.weight(1f),
        )
    }
}

/** Compact help before any photo or OCR; caller shows only when idle with no image yet. */
@Composable
private fun ImportTicketPrescanTipsCard(modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(Dimens.radiusCard)
    AppSectionSurface(modifier = modifier, shape = shape) {
        Column(
            modifier = Modifier.padding(
                horizontal = Dimens.spacing12,
                vertical = Dimens.spacing10,
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacing4),
        ) {
            Text(
                text = stringResource(R.string.import_ticket_prescan_tips_title),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = cs.onSurface.copy(alpha = 0.92f),
            )
            listOf(
                R.string.import_ticket_prescan_tip_1,
                R.string.import_ticket_prescan_tip_2,
                R.string.import_ticket_prescan_tip_3,
            ).forEach { tipId ->
                Text(
                    text = "• ${stringResource(tipId)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant.copy(alpha = 0.68f),
                )
            }
        }
    }
}

private fun Modifier.importHeroDashedBorder(
    color: androidx.compose.ui.graphics.Color,
    cornerRadius: Dp,
): Modifier = drawBehind {
    val strokeWidth = 2.dp.toPx()
    drawRoundRect(
        color = color,
        topLeft = androidx.compose.ui.geometry.Offset.Zero,
        size = size,
        cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
        style = Stroke(
            width = strokeWidth,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f),
        ),
    )
}

@Composable
private fun ImportTicketHeroAnalyzingOverlay(
    imageClipShape: RoundedCornerShape,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(imageClipShape)
            .background(Color.Black.copy(alpha = 0.50f)),
    ) {
        ScanningAnalysisAnimation(modifier = Modifier.fillMaxSize())
        ProcessingDataCard(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    start = Dimens.spacing16,
                    end = Dimens.spacing16,
                    bottom = Dimens.spacing16,
                )
                .fillMaxWidth(),
        )
    }
}

@Composable
private fun HeroThumbnailQuickActions(
    modifier: Modifier = Modifier,
    onTakePhoto: () -> Unit,
    onPickGallery: () -> Unit,
    enabled: Boolean,
) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            onClick = onTakePhoto,
            enabled = enabled,
            shape = CircleShape,
            color = PrimaryDark,
            shadowElevation = 4.dp,
            modifier = Modifier.size(42.dp),
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Filled.PhotoCamera,
                    contentDescription = stringResource(R.string.import_ticket_take_photo_cd),
                    modifier = Modifier.size(22.dp),
                    tint = OnPrimary,
                )
            }
        }
        Surface(
            onClick = onPickGallery,
            enabled = enabled,
            shape = CircleShape,
            color = cs.surface,
            border = BorderStroke(1.dp, CardBorderGreen.copy(alpha = 0.85f)),
            shadowElevation = 4.dp,
            modifier = Modifier.size(42.dp),
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Filled.PhotoLibrary,
                    contentDescription = stringResource(R.string.import_ticket_gallery_cd),
                    modifier = Modifier.size(22.dp),
                    tint = Primary,
                )
            }
        }
    }
}

/** Ticket preview frame: soft tinted surface, **outline** border, light dashed inner line; no elevation shadow. */
@Composable
private fun HeroBannerCard(
    selectedImageUri: Uri?,
    asyncImageModel: Any? = null,
    isAnalyzing: Boolean = false,
    expandVertically: Boolean = false,
    scanAgainOnError: (() -> Unit)? = null,
    heroOverlay: (@Composable BoxScope.() -> Unit)? = null,
    compactPadding: Boolean = false,
    appPreviewStyle: Boolean = false,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(Dimens.radiusCard)
    val imageShape = RoundedCornerShape(if (appPreviewStyle) Dimens.radiusMedium else Dimens.radiusBingoCell)
    val cs = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    val heroContainerBg = when {
        appPreviewStyle -> cs.surface
        dark -> lerp(cs.surface, cs.surfaceVariant, 0.12f)
        else -> lerp(Background, HeaderGradientEnd, 0.06f)
    }
    val outerBorderColor = when {
        appPreviewStyle -> cs.primary.copy(alpha = APP_SECTION_BORDER_ALPHA)
        else -> cs.outline.copy(alpha = if (dark) 0.1f else 0.065f)
    }
    val pad = when {
        appPreviewStyle -> Dimens.spacing12
        compactPadding -> Dimens.spacing8
        else -> Dimens.spacing16
    }
    Box(
        modifier = modifier
            .clip(shape)
            .border(BorderStroke(Dimens.cardBorderDefault, outerBorderColor), shape)
            .background(heroContainerBg)
            .then(
                if (appPreviewStyle) Modifier
                else Modifier.importHeroDashedBorder(CardBorderGreen.copy(alpha = 0.14f), Dimens.radiusCard),
            )
            .padding(pad)
    ) {
        val imageModel = asyncImageModel ?: selectedImageUri
        if (selectedImageUri != null || asyncImageModel != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (expandVertically) Modifier.fillMaxHeight() else Modifier)
            ) {
                val previewAspect = if (appPreviewStyle) 16f / 10f else 16f / 9f
                val imgMod = Modifier
                    .fillMaxWidth()
                    .then(
                        if (expandVertically) Modifier.fillMaxHeight()
                        else Modifier.aspectRatio(previewAspect),
                    )
                    .clip(imageShape)
                AsyncImage(
                    model = imageModel,
                    contentDescription = null,
                    modifier = imgMod,
                    contentScale = ContentScale.Crop
                )
                if (!isAnalyzing && scanAgainOnError != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(Dimens.spacing8)
                            .clip(RoundedCornerShape(Dimens.radiusSmall))
                            .background(Scrim.copy(alpha = 0.55f))
                            .clickable(onClick = scanAgainOnError)
                            .padding(horizontal = Dimens.spacing10, vertical = Dimens.spacing8)
                    ) {
                        Text(
                            text = stringResource(R.string.import_ticket_scan_again),
                            style = MaterialTheme.typography.labelLarge,
                            color = PrimaryContainer,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                if (isAnalyzing) {
                    ImportTicketHeroAnalyzingOverlay(
                        imageClipShape = imageShape,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                if (heroOverlay != null) {
                    heroOverlay()
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (expandVertically && !appPreviewStyle) Modifier.fillMaxSize()
                        else Modifier.padding(vertical = if (appPreviewStyle) Dimens.spacing8 else 0.dp),
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = when {
                    expandVertically && !appPreviewStyle -> Arrangement.Center
                    appPreviewStyle -> Arrangement.spacedBy(Dimens.spacing8)
                    else -> Arrangement.spacedBy(Dimens.spacing12)
                },
            ) {
                Box(
                    modifier = Modifier
                        .size(if (appPreviewStyle) Dimens.iconAlertBox else 44.dp)
                        .clip(RoundedCornerShape(Dimens.radiusSmall))
                        .background(IconContainerBg)
                        .border(
                            Dimens.cardBorderDefault,
                            cs.primary.copy(alpha = APP_SECTION_BORDER_ALPHA),
                            RoundedCornerShape(Dimens.radiusSmall),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.PhotoCamera,
                        contentDescription = null,
                        modifier = Modifier.size(if (appPreviewStyle) Dimens.iconCompact else Dimens.iconDefault),
                        tint = Primary,
                    )
                }
                Text(
                    text = stringResource(R.string.import_ticket_no_photo_selected),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = if (appPreviewStyle) FontWeight.SemiBold else FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.94f),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.import_ticket_hero_empty_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = if (appPreviewStyle) 0.68f else 0.72f,
                    ),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun ImportTicketScreenPreview() {
    MamunBingoTheme {
        ImportTicketScreen(
            onBack = {},
            onDiscardAndBack = {},
            onTakePhoto = {},
            onReviewClicked = {},
            showBottomBar = true
        )
    }
}
