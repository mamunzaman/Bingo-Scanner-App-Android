package com.example.mamunbingoapp.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * LOS / serial were merged into [ScanResultSummaryCard] (horizontal metric scroller). This composable is a no-op
 * so older call sites can be removed without breaking references during refactors.
 */
@Composable
fun ImportTicketLosSerialCard(
    losNumber: String?,
    serialNumber: String?,
    modifier: Modifier = Modifier,
) {
}
