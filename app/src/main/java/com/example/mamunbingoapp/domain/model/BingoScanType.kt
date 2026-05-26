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
            PLAY_PAPER -> "Bingo Play Paper"
            ONLINE -> "Bingo Online"
            MAIN_SHEET -> "Bingo Main Sheet"
        }

    val subtitle: String
        get() = when (this) {
            PLAY_PAPER -> "Physical bingo ticket paper"
            ONLINE -> "Online/digital bingo format"
            MAIN_SHEET -> "Main/master bingo sheet"
        }

    companion object {
        fun fromRouteValue(value: String?): BingoScanType =
            entries.firstOrNull { it.name == value } ?: PLAY_PAPER
    }
}
