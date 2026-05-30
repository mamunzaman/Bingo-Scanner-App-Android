package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.core.MAX_LIVE_CALLS
import com.example.mamunbingoapp.theme.Dimens

/**
 * Bottom sheet: **Called numbers** — title/count pill, then [TvBingoBoard].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalledNumbersDetailSheet(
    onDismiss: () -> Unit,
    calledNumbers: List<Int>,
    maxCalls: Int = MAX_LIVE_CALLS,
    title: String? = null,
    countPillText: String? = null,
    footerText: String? = null,
    onOverflowMenuClick: (() -> Unit)? = null,
) {
    val sheetState = rememberAppBottomSheetState(skipPartiallyExpanded = true)
    val resolvedTitle = title ?: stringResource(R.string.live_play_called_numbers_label)
    val scheme = MaterialTheme.colorScheme
    val orderedCalls = remember(calledNumbers) { calledNumbers.distinct() }
    val numbersByColumn = remember(calledNumbers) { groupCalledNumbersByColumn(calledNumbers) }
    val boardLatest = calledNumbers.lastOrNull()
    val distinctCallCount = orderedCalls.size
    AppBottomSheetSurface(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.screenHorizontalPadding)
                .padding(top = Dimens.spacing20, bottom = Dimens.spacing16)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.spacing16),
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                if (onOverflowMenuClick != null) {
                    IconButton(
                        onClick = onOverflowMenuClick,
                        modifier = Modifier.align(Alignment.TopEnd),
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = null,
                            tint = scheme.onSurfaceVariant,
                        )
                    }
                }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacing8),
                ) {
                    Text(
                        text = resolvedTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface,
                        textAlign = TextAlign.Center,
                    )
                    Surface(
                        shape = RoundedCornerShape(Dimens.radiusPill),
                        color = scheme.primary.copy(alpha = 0.14f),
                        border = BorderStroke(Dimens.cardBorderDefault, scheme.primary.copy(alpha = 0.28f)),
                    ) {
                        Text(
                            text = countPillText ?: "${calledNumbers.size} / $maxCalls",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = scheme.primary,
                            modifier = Modifier.padding(horizontal = Dimens.spacing12, vertical = Dimens.spacing5),
                        )
                    }
                }
            }
            TvBingoBoard(
                numbersByColumn = numbersByColumn,
                latest = boardLatest,
                boardGreen = scheme.primary,
                letterRed = scheme.secondary,
                lineColor = Color.Black.copy(alpha = 0.38f),
                callSequence = calledNumbers,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = footerText ?: "$distinctCallCount called numbers",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = scheme.onSurfaceVariant.copy(alpha = 0.68f),
                modifier = Modifier.padding(top = Dimens.spacing4),
            )
        }
    }
}

