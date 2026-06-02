package com.example.mamunbingoapp.scanner

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.CancellationException

/**
 * Master Sheet scan pipeline: Gemini Vision first, [MainSheetBingoOcr] fallback.
 * Used only for [com.example.mamunbingoapp.domain.model.BingoScanType.MAIN_SHEET].
 */
object MainSheetScanAnalyzer {

    private const val TAG = "MainSheetAiOcr"

    suspend fun analyzeUri(
        context: Context,
        uri: Uri,
        bypassInternalGridCrop: Boolean = false,
        preCropCameraForStripOcr: Boolean = false,
    ): MainSheetAnalyzeResult {
        val appContext = context.applicationContext
        val canTryAi = MasterSheetGeminiVisionOcr.isConfigured() &&
            MasterSheetGeminiVisionOcr.hasInternet(appContext)

        if (canTryAi) {
            Log.i(TAG, "AI request started")
            val aiOutcome = try {
                MasterSheetGeminiVisionOcr.analyzeUri(appContext, uri)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "AI request failed: ${e.message}")
                null
            }
            if (aiOutcome != null && isValidAiOutcome(aiOutcome.outcome)) {
                val o = aiOutcome.outcome
                Log.i(TAG, "AI success serie=${o.serialNumber} los=${o.losNumber}")
                return MainSheetAnalyzeResult(
                    outcome = o,
                    usedAi = true,
                    usedLocalFallback = false,
                    aiConfidence = aiOutcome.confidence,
                )
            }
            if (aiOutcome != null) {
                val o = aiOutcome.outcome
                Log.w(
                    TAG,
                    "AI validation failed serie=${o.serialNumber} los=${o.losNumber} " +
                        "gridSize=${o.numbersRowMajor.size}",
                )
            }
        } else {
            val reason = when {
                !MasterSheetGeminiVisionOcr.isConfigured() -> "no_api_key"
                !MasterSheetGeminiVisionOcr.hasInternet(appContext) -> "no_internet"
                else -> "unknown"
            }
            Log.i(TAG, "AI skipped ($reason)")
        }

        Log.i(TAG, "Fallback activated → MainSheetBingoOcr")
        val local = MainSheetBingoOcr.analyzeUri(
            appContext,
            uri,
            bypassInternalGridCrop = bypassInternalGridCrop,
            preCropCameraForStripOcr = preCropCameraForStripOcr,
        )
        return MainSheetAnalyzeResult(
            outcome = local,
            usedAi = false,
            usedLocalFallback = true,
            aiConfidence = null,
        )
    }

    internal fun isValidAiOutcome(outcome: HistoryImportOcrOutcome): Boolean {
        val serie = outcome.serialNumber?.filter { it.isDigit() }.orEmpty()
        val los = outcome.losNumber?.filter { it.isDigit() }.orEmpty()
        if (serie.isEmpty() || los.isEmpty()) return false
        if (outcome.numbersRowMajor.size != 25) return false
        val grid = outcome.numbersRowMajor
        if (grid.any { it !in 0..75 }) return false
        val filled = grid.count { it in 1..75 }
        return filled >= MainSheetBingoOcr.MIN_VALID_CELLS_FOR_SUCCESS
    }
}
