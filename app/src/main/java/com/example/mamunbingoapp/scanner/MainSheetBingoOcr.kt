package com.example.mamunbingoapp.scanner

import android.content.Context
import android.net.Uri
import android.util.Log

/**
 * OCR for [com.example.mamunbingoapp.domain.model.BingoScanType.MAIN_SHEET] only.
 * Delegates to legacy [ImportTicketImageOcr] until a dedicated implementation exists.
 */
object MainSheetBingoOcr {

    private const val TAG = "MainSheetBingoOcr"

    fun analyzeUri(
        context: Context,
        uri: Uri,
        bypassInternalGridCrop: Boolean = false,
        preCropCameraForStripOcr: Boolean = false,
    ): HistoryImportOcrOutcome {
        Log.d(TAG, "MainSheetBingoOcr selected")
        return ImportTicketImageOcr.analyzeUri(
            context,
            uri,
            bypassInternalGridCrop = bypassInternalGridCrop,
            preCropCameraForStripOcr = preCropCameraForStripOcr,
        )
    }
}
