package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.example.mamunbingoapp.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.OnPrimary
import com.example.mamunbingoapp.theme.WarningBorder
import com.example.mamunbingoapp.theme.WarningContainer
import com.example.mamunbingoapp.theme.WarningIcon
import com.example.mamunbingoapp.theme.WarningSubText
import com.example.mamunbingoapp.theme.WarningText

@Composable
fun CompactAlmostBingoRow(
    lineType: String,
    filled: Int,
    total: Int,
    markedCells: Set<Int>,
    compactVertical: Boolean = false,
) {
    val need = total - filled
    val subtitle = pluralStringResource(R.plurals.live_play_almost_bingo_need, need, lineType, need)
    val alertShape = RoundedCornerShape(Dimens.radiusCard)
    val rowPadV = if (compactVertical) Dimens.spacing4 else Dimens.spacing8
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(alertShape)
            .background(WarningContainer, alertShape)
            .border(
                Dimens.cardBorderDefault,
                WarningBorder.copy(alpha = 0.2f),
                alertShape,
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(WarningBorder.copy(alpha = 0.35f)),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = rowPadV, horizontal = Dimens.spacing12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(Dimens.radiusBingoCell))
                    .background(WarningIcon)
                    .padding(2.dp),
                contentAlignment = Alignment.Center,
            ) {
                MiniBingoPreview(
                    markedCells = markedCells,
                    nearCells = emptySet(),
                    modifier = Modifier.fillMaxSize(),
                    gap = 1.dp,
                    cellRadius = 2.dp,
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = Dimens.spacing8),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                Text(
                    text = stringResource(R.string.live_play_almost_bingo),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = WarningText,
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = WarningSubText,
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(Dimens.progressBarRadius))
                    .background(WarningIcon)
                    .padding(vertical = 2.dp, horizontal = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "$filled/$total",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = OnPrimary,
                )
            }
        }
    }
}
