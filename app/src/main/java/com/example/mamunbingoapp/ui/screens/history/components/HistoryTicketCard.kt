package com.example.mamunbingoapp.ui.screens.history.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.ui.components.ShareCalledNumbersButton
import com.example.mamunbingoapp.ui.components.home.ActiveTicketLosSerieRow

/**
 * Live/history bingo sheet card metadata: share action (top-right), LOS/SERIE row, scanned date row.
 * Grid and marked styling stay in [com.example.mamunbingoapp.ui.components.home.BingoSheetTicketCard].
 */
@Composable
fun HistoryTicketSheetTrailingShare(
    onShareCalledNumbers: (() -> Unit)?,
    shareEnabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    if (onShareCalledNumbers == null) return
    ShareCalledNumbersButton(
        onClick = onShareCalledNumbers,
        enabled = shareEnabled,
        modifier = modifier,
    )
}

@Composable
fun HistoryTicketSheetMetaBlock(
    losNumber: String?,
    serieNumber: String?,
    scannedDateText: String,
    modifier: Modifier = Modifier,
    losSerieLabelStyle: TextStyle = MaterialTheme.typography.labelSmall.copy(
        fontSize = 11.sp,
        lineHeight = 12.sp,
        fontWeight = FontWeight.Medium,
    ),
    losSerieValueStyle: TextStyle = MaterialTheme.typography.labelLarge.copy(
        fontSize = 16.sp,
        lineHeight = 17.sp,
        fontWeight = FontWeight.Bold,
    ),
    scannedDateStyle: TextStyle = MaterialTheme.typography.labelSmall.copy(
        fontSize = 12.sp,
        lineHeight = 14.sp,
    ),
) {
    val cs = MaterialTheme.colorScheme
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacing8),
    ) {
        ActiveTicketLosSerieRow(
            losNumber = losNumber,
            serieNumber = serieNumber,
            modifier = Modifier.fillMaxWidth(),
            labelStyle = losSerieLabelStyle,
            valueStyle = losSerieValueStyle,
        )
        Text(
            text = scannedDateText,
            modifier = Modifier.fillMaxWidth(),
            style = scannedDateStyle,
            color = cs.onSurfaceVariant.copy(alpha = 0.55f),
            maxLines = 2,
            overflow = TextOverflow.Clip,
        )
    }
}
