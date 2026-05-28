package com.example.mamunbingoapp.domain.model

/**
 * Direct-scan target for camera/OCR routing (layout differs per format).
 */
enum class BingoScanType {
    PLAY_PAPER,
    ONLINE,
    MAIN_SHEET,
    ;

    val title: String
        get() = when (this) {
            PLAY_PAPER -> "Player Sheet"
            ONLINE -> "Digital Sheet"
            MAIN_SHEET -> "Master Sheet"
        }

    val subtitle: String
        get() = when (this) {
            PLAY_PAPER -> "Physical bingo ticket paper"
            ONLINE -> "Online/digital bingo format"
            MAIN_SHEET -> "Official/master bingo proof"
        }

    companion object {
        fun fromRouteValue(value: String?): BingoScanType =
            entries.firstOrNull { it.name == value } ?: PLAY_PAPER
    }
}
