package com.example.mamunbingoapp.scanner

import android.content.Context
import android.net.Uri
import android.util.Log

/**
 * OCR for [com.example.mamunbingoapp.domain.model.BingoScanType.PLAY_PAPER] only.
 * Delegates to legacy [ImportTicketImageOcr] until a dedicated implementation exists.
 */
object PlayPaperBingoOcr {

    private const val TAG = "PlayPaperBingoOcr"

    fun analyzeUri(
        context: Context,
        uri: Uri,
        bypassInternalGridCrop: Boolean = false,
        preCropCameraForStripOcr: Boolean = false,
    ): HistoryImportOcrOutcome {
        Log.d(TAG, "PlayPaperBingoOcr selected")
        return ImportTicketImageOcr.analyzeUri(
            context,
            uri,
            bypassInternalGridCrop = bypassInternalGridCrop,
            preCropCameraForStripOcr = preCropCameraForStripOcr,
        )
    }
}
