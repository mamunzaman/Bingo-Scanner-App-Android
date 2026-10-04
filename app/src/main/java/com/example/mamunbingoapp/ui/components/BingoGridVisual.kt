package com.example.mamunbingoapp.ui.components

import androidx.compose.runtime.compositionLocalOf

/** Scoped polish; default leaves Live Play, History, and Ticket Detail unchanged. */
enum class BingoGridVisualVariant {
    Default,
    /** Manual Entry: empty-cell tint + stronger active cell; default header/grid shape. */
    ManualEntrySheet,
    /** Premium live/sheet ticket: cream cell background, warm border, gold marked-cell border. */
    PremiumTicket,
}

val LocalBingoGridVisualVariant = compositionLocalOf { BingoGridVisualVariant.Default }
