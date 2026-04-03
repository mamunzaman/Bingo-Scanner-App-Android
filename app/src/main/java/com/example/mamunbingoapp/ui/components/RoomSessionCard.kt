package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTonalElevationEnabled
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.mamunbingoapp.core.AlmostBingoInfo
import com.example.mamunbingoapp.core.MAX_CALLED_NUMBERS
import com.example.mamunbingoapp.ui.components.BingoWinBanner
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.ui.model.RoomStatus
import com.example.mamunbingoapp.ui.model.SheetStatus

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RoomSessionCard(
    title: String,
    statusText: String,
    statusDotColor: Color,
    ticketsCount: Int,
    calledCount: Int,
    totalCount: Int = MAX_CALLED_NUMBERS,
    actionText: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    onCardClick: (() -> Unit)? = null,
    roomStatus: RoomStatus? = null,
    sheetStatus: SheetStatus? = null,
    addedToRoomName: String? = null,
    addedAtMillis: Long? = null,
    markedCount: Int = 0,
    markedCells: List<Boolean>? = null,
    almostBingo: AlmostBingoInfo? = null,
    bingoWinLineCount: Int? = null,
    actionIcon: ImageVector? = null,
    onViewClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
    onLeaveRoomClick: (() -> Unit)? = null,
    ocrSource: String? = null,
    editedAfterOcr: Boolean = false,
    ocrCorrectionCount: Int = 0
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showLeaveConfirm by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(Dimens.radiusLarge)
    val clampedCount = calledCount.coerceAtMost(totalCount)
    val progressValue = if (totalCount > 0) (clampedCount.toFloat() / totalCount).coerceIn(0f, 1f) else 0f
    val footerBgColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    val footerBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)

    val summary = "$title, $ticketsCount tickets, $clampedCount of $totalCount called. $actionText."
    CompositionLocalProvider(LocalTonalElevationEnabled provides false) {
    val cs = MaterialTheme.colorScheme
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { contentDescription = summary },
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = cs.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.cardElevationDefault),
        onClick = onCardClick ?: {}
    ) {
        Column(modifier = Modifier.fillMaxWidth().background(cs.surface)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = Dimens.spacing16,
                    end = Dimens.spacing16,
                    top = Dimens.spacing16,
                    bottom = 0.dp
                )
        ) {
            Column(Modifier.fillMaxWidth()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(Dimens.spacing8))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8, Alignment.End),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacing8, Alignment.Top)
                ) {
                    if (onViewClick != null) {
                        Button(
                            onClick = onViewClick,
                            modifier = Modifier.height(36.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(100.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Text("View", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Button(
                        onClick = onActionClick,
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(100.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        if (actionIcon != null) {
                            Icon(
                                imageVector = actionIcon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                        }
                        Text(
                            text = actionText,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            Spacer(Modifier.height(Dimens.spacing16))
            val gridCells = if (markedCells != null && markedCells.size == 25) markedCells else List(25) { it < markedCount.coerceIn(0, 25) }
            val markedSet = gridCells.mapIndexed { i, b -> i.takeIf { b } }.filterNotNull().toSet()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimens.spacing4),
                verticalAlignment = Alignment.Bottom
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(min = 96.dp, max = 120.dp)
                        .padding(top = Dimens.spacing4)
                ) {
                    if (addedAtMillis != null) {
                        val dateStr = remember(addedAtMillis) {
                            SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(addedAtMillis))
                        }
                        val timeStr = remember(addedAtMillis) {
                            SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(addedAtMillis))
                        }
                        Text(
                            text = "Added $dateStr at $timeStr",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            softWrap = true
                        )
                    }
                    Spacer(Modifier.height(Dimens.spacing8))
Row(
                            modifier = Modifier
                                .wrapContentWidth()
                                .clip(RoundedCornerShape(100.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .padding(horizontal = Dimens.spacing8, vertical = Dimens.spacing4),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(statusDotColor)
                        )
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelMedium,
                            color = statusDotColor,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (ocrSource == "GEMINI" || ocrSource == "ML_KIT") {
                        Spacer(Modifier.height(Dimens.spacing4))
                        Text(
                            text = if (ocrSource == "GEMINI") "Gemini OCR" else "ML Kit OCR",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                            modifier = Modifier
                                .wrapContentWidth()
                                .clip(RoundedCornerShape(100.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .padding(horizontal = Dimens.spacing8, vertical = Dimens.spacing4)
                        )
                        if (editedAfterOcr) {
                            Spacer(Modifier.height(Dimens.spacing4))
                            Text(
                                text = "Edited after OCR",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                    .padding(horizontal = Dimens.spacing8, vertical = Dimens.spacing4)
                            )
                            if (ocrCorrectionCount > 0) {
                                Spacer(Modifier.height(Dimens.spacing4))
                                Text(
                                    text = if (ocrCorrectionCount == 1) "1 OCR change" else "$ocrCorrectionCount OCR changes",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                                    modifier = Modifier
                                        .wrapContentWidth()
                                        .clip(RoundedCornerShape(100.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                        .padding(horizontal = Dimens.spacing8, vertical = Dimens.spacing4)
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.width(Dimens.spacing12))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8)
                    ) {
                        val metaColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                        if (addedToRoomName != null) {
                            Icon(
                                imageVector = Icons.Default.MeetingRoom,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = metaColor
                            )
                            Text(
                                text = "Added to $addedToRoomName",
                                style = MaterialTheme.typography.labelMedium,
                                color = metaColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.ConfirmationNumber,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = metaColor
                            )
                            Text(
                                text = "$ticketsCount Tickets",
                                style = MaterialTheme.typography.labelMedium,
                                color = metaColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    if (bingoWinLineCount == null && almostBingo == null) {
                        Spacer(modifier = Modifier.height(Dimens.spacing8))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            MiniBingoGrid(cells = gridCells)
                        }
                    }
                }
            }
            if (bingoWinLineCount != null && bingoWinLineCount >= 1) {
                Spacer(modifier = Modifier.height(Dimens.spacing12))
                BingoWinBanner(lineCount = bingoWinLineCount, modifier = Modifier.fillMaxWidth())
            }
            if (almostBingo != null) {
                Spacer(modifier = Modifier.height(Dimens.spacing12))
                AlmostBingoAlertRowV2(
                    lineType = almostBingo.lineLabel,
                    filled = almostBingo.marked,
                    total = almostBingo.total,
                    markedCells = markedSet,
                    nearCells = emptySet(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.spacing16))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(footerBorderColor)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(footerBgColor)
                .padding(horizontal = Dimens.spacing16, vertical = Dimens.spacing10),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val value = "$clampedCount\u00A0/\u00A0$totalCount"
            val footerTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
            Text(
                text = buildAnnotatedString {
                    append("How many called: ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(value)
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = footerTextColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8)
            ) {
                if (onLeaveRoomClick != null) {
                    Text(
                        text = "leave room",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(
                            indication = rememberRipple(),
                            interactionSource = remember { MutableInteractionSource() }
                        ) { showLeaveConfirm = true }
                    )
                }
                if (onDeleteClick != null) {
                    Box(
                        modifier = Modifier
                            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                            .clickable(
                                indication = rememberRipple(),
                                interactionSource = remember { MutableInteractionSource() }
                            ) { showDeleteConfirm = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete room",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Dimens.spacing4))

        LinearProgressIndicator(
            progress = { progressValue },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(999.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        }
    }
    AppConfirmDialog(
        visible = showDeleteConfirm,
        title = "Delete Bingo Sheet?",
        message = "This will delete this sheet from history. This cannot be undone.",
        confirmText = "Delete",
        cancelText = "Cancel",
        showCancelButton = true,
        onConfirm = {
            onDeleteClick?.invoke()
            showDeleteConfirm = false
        },
        onCancel = { showDeleteConfirm = false },
        onDismiss = { showDeleteConfirm = false }
    )
    AppConfirmDialog(
        visible = showLeaveConfirm,
        title = "Leave room?",
        message = "This sheet will be removed from the room but stay in your history.",
        confirmText = "Leave",
        cancelText = "Cancel",
        showCancelButton = true,
        onConfirm = {
            onLeaveRoomClick?.invoke()
            showLeaveConfirm = false
        },
        onCancel = { showLeaveConfirm = false },
        onDismiss = { showLeaveConfirm = false }
    )
    }
}

@Composable
private fun MiniBingoGrid(cells: List<Boolean>) {
    val shape = RoundedCornerShape(2.dp)
    val list = if (cells.size >= 25) cells.take(25) else cells + List(25 - cells.size) { false }
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(4.dp))
            .padding(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        list.chunked(5).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                row.forEach { matched ->
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(if (matched) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, shape)
                    )
                }
            }
        }
    }
}
