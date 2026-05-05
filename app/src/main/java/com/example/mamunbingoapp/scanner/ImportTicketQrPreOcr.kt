package com.example.mamunbingoapp.scanner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.example.mamunbingoapp.domain.qr.QrTicketCodec
import com.example.mamunbingoapp.viewmodel.MIN_VALID_CELLS_FOR_MANUAL_ENTRY_NAV
import com.example.mamunbingoapp.viewmodel.validFilledCellCount
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

private const val TAG = "ImportTicketQr"
private const val DECODE_MAX_SIDE = 2400

/**
 * Tries to read a Mamun Bingo ticket QR from [uri] before running OCR.
 * [NoBingoQr] means caller should run normal OCR; logs are per outcome.
 */
sealed class ImportTicketQrPreOcr {
    data class Decoded(
        val numbers: List<Int>,
        val serial: String?,
        val los: String?,
    ) : ImportTicketQrPreOcr()

    data object NoBingoQrContinueOcr : ImportTicketQrPreOcr()
}

private fun decodeBitmapForQr(context: Context, uri: Uri, maxSide: Int): Bitmap? {
    val cr = context.contentResolver
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    cr.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    var sample = 1
    while (maxOf(bounds.outWidth, bounds.outHeight) / sample > maxSide) sample *= 2
    val opts = BitmapFactory.Options().apply { inSampleSize = sample }
    return cr.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
}

private val barcodeOptions = BarcodeScannerOptions.Builder()
    .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
    .build()

private val sharedBarcodeScanner = BarcodeScanning.getClient(barcodeOptions)

private fun decodeBingoFromBarcodes(
    task: List<Barcode>,
    onValidBingo: () -> Unit,
    logNoBingo: () -> Unit,
): ImportTicketQrPreOcr {
    if (task.isEmpty()) {
        logNoBingo()
        return ImportTicketQrPreOcr.NoBingoQrContinueOcr
    }
    var hadMamunCandidate = false
    for (b in task) {
        val raw = b.rawValue?.trim().orEmpty()
        if (raw.isEmpty()) continue
        if (!QrTicketCodec.isLikelyBingoTicketQrString(raw)) continue
        hadMamunCandidate = true
        val payload = QrTicketCodec.decode(raw)
        if (payload.isFailure) {
            Log.w(TAG, "app prefix present, decode failed: ${payload.exceptionOrNull()?.message}")
            continue
        }
        val p = payload.getOrThrow()
        val rowMajor = QrTicketCodec.rowMajorFromQrGrid5x5(p.grid)
        val count = validFilledCellCount(rowMajor)
        if (count < MIN_VALID_CELLS_FOR_MANUAL_ENTRY_NAV) {
            Log.w(TAG, "valid structure but too few numbers ($count), continue OCR")
            return ImportTicketQrPreOcr.NoBingoQrContinueOcr
        }
        onValidBingo()
        Log.d("QR_DEBUG", "decoded cells rowMajor=${rowMajor.joinToString()}")
        return ImportTicketQrPreOcr.Decoded(
            numbers = rowMajor,
            serial = p.serial?.takeIf { it.isNotBlank() },
            los = p.los?.takeIf { it.isNotBlank() },
        )
    }
    if (hadMamunCandidate) {
        return ImportTicketQrPreOcr.NoBingoQrContinueOcr
    }
    logNoBingo()
    return ImportTicketQrPreOcr.NoBingoQrContinueOcr
}

/**
 * @return [ImportTicketQrPreOcr.Decoded] when a valid app QR is present and has enough cells; otherwise
 * [ImportTicketQrPreOcr.NoBingoQrContinueOcr] and OCR should run. QR decode failures (bad payload) are ignored
 * in favor of OCR without treating it as a successful scan.
 */
fun tryDecodeBingoQrFromImageUri(
    context: Context,
    uri: Uri,
): ImportTicketQrPreOcr {
    val bitmap = decodeBitmapForQr(context, uri, DECODE_MAX_SIDE)
    if (bitmap == null) {
        Log.d(TAG, "no bingo QR, continue OCR")
        return ImportTicketQrPreOcr.NoBingoQrContinueOcr
    }
    val image = InputImage.fromBitmap(bitmap, 0)
    val processResult = runCatching { Tasks.await(sharedBarcodeScanner.process(image)) }
    if (!bitmap.isRecycled) bitmap.recycle()
    val task = processResult.getOrElse { e ->
        Log.w(TAG, "barcode scan failed, continue OCR: ${e.message}")
        return ImportTicketQrPreOcr.NoBingoQrContinueOcr
    }
    return decodeBingoFromBarcodes(
        task = task,
        onValidBingo = { Log.d(TAG, "valid bingo QR detected") },
        logNoBingo = { Log.d(TAG, "no bingo QR, continue OCR") },
    )
}

/**
 * Barcode pass on a camera frame. Caller must not recycle [inputImage] resources (use [imageProxy] lifecycle elsewhere).
 */
fun tryDecodeBingoQrFromInputImage(
    inputImage: InputImage,
    onLiveNoBingoFrame: () -> Unit,
): ImportTicketQrPreOcr {
    val task = runCatching { Tasks.await(sharedBarcodeScanner.process(inputImage)) }
        .getOrElse { e ->
            Log.w(TAG, "live camera barcode process failed: ${e.message}")
            onLiveNoBingoFrame()
            return ImportTicketQrPreOcr.NoBingoQrContinueOcr
        }
    return decodeBingoFromBarcodes(
        task = task,
        onValidBingo = { Log.d(TAG, "live camera bingo QR detected") },
        logNoBingo = onLiveNoBingoFrame,
    )
}
