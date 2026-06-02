package com.example.mamunbingoapp.scanner

/**
 * Master Sheet scan pipeline result (AI and/or local OCR).
 */
data class MainSheetAnalyzeResult(
    val outcome: HistoryImportOcrOutcome,
    val usedAi: Boolean,
    val usedLocalFallback: Boolean,
    val aiConfidence: Float?,
)
