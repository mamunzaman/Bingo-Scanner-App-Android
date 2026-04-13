package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.example.mamunbingoapp.ui.core.interaction.appClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.LiveFonts

private val pillHeight get() = Dimens.buttonHeight
private val pillShape = RoundedCornerShape(Dimens.radiusButtonPill)
private val pillHpad get() = Dimens.spacing12
private val pillVpad get() = Dimens.spacing8
private val pillBorder = 1.dp
private val iconSize = 18.dp
private val rowGap get() = Dimens.spacing12

private fun progressFraction(current: Int, total: Int): Float =
    (current.coerceAtMost(total).toFloat() / total.coerceAtLeast(1))

@Composable
private fun RoundPillButton(
    onClick: () -> Unit,
    containerColor: Color,
    borderColor: Color,
    contentColor: Color,
    leadingIcon: ImageVector,
    text: String? = null,
    modifier: Modifier = Modifier
) {
    val shape = pillShape
    val baseModifier = modifier
        .clip(shape)
        .background(containerColor)
        .border(pillBorder, borderColor, shape)
        .appClickable(rippleColor = contentColor, onClick = onClick)
    if (text == null) {
        Box(
            modifier = baseModifier
                .size(pillHeight)
                .padding(pillVpad),
            contentAlignment = Alignment.Center
        ) {
            Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(iconSize), tint = contentColor)
        }
    } else {
        Box(
            modifier = baseModifier
                .heightIn(min = pillHeight)
                .padding(horizontal = pillHpad, vertical = pillVpad),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(iconSize), tint = contentColor)
                Text(text, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp), color = contentColor)
            }
        }
    }
}

@Composable
fun RoundProgressVariant(
    title: String,
    current: Int,
    total: Int,
    onFinishClick: () -> Unit,
    onResetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, cs.outline.copy(alpha = 0.2f), RoundedCornerShape(Dimens.radiusLarge)),
        shape = RoundedCornerShape(Dimens.radiusLarge),
        color = cs.surface,
        shadowElevation = 2.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacing16),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.12.sp
                    ),
                    color = cs.onSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(Dimens.progressBarHeight)
                            .clip(RoundedCornerShape(Dimens.progressBarRadius))
                            .background(cs.outlineVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .height(Dimens.progressBarHeight)
                                .fillMaxWidth(progressFraction(current, total))
                                .clip(RoundedCornerShape(Dimens.progressBarRadius))
                                .background(cs.primary)
                        )
                    }
                    Text(
                        text = "$current / $total",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = LiveFonts.DMMono,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        color = cs.primary
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(rowGap),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoundPillButton(
                    onClick = onFinishClick,
                    containerColor = cs.primary,
                    borderColor = cs.primary.copy(alpha = 0.30f),
                    contentColor = cs.onPrimary,
                    leadingIcon = Icons.Default.Star,
                    text = "Finish"
                )
                RoundPillButton(
                    onClick = onResetClick,
                    containerColor = cs.primary.copy(alpha = 0.10f),
                    borderColor = cs.primary.copy(alpha = 0.22f),
                    contentColor = cs.primary,
                    leadingIcon = Icons.Default.Refresh
                )
            }
        }
    }
}

@Composable
fun RoundProgressReversed(
    current: Int,
    total: Int,
    onFinishClick: () -> Unit,
    onResetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.radiusLarge),
        color = cs.primary,
        shadowElevation = 2.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacing16),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(
                    text = "ROUND PROGRESS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.12.sp
                    ),
                    color = cs.onPrimary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(Dimens.progressBarHeight)
                            .clip(RoundedCornerShape(Dimens.progressBarRadius))
                            .background(cs.onPrimary.copy(alpha = 0.25f))
                    ) {
                        Box(
                            modifier = Modifier
                                .height(Dimens.progressBarHeight)
                                .fillMaxWidth(progressFraction(current, total))
                                .clip(RoundedCornerShape(Dimens.progressBarRadius))
                                .background(cs.onPrimary)
                        )
                    }
                    Text(
                        text = "$current / $total",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = LiveFonts.DMMono,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        color = cs.onPrimary
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(rowGap),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoundPillButton(
                    onClick = onFinishClick,
                    containerColor = cs.surface,
                    borderColor = cs.onPrimary.copy(alpha = 0.18f),
                    contentColor = cs.primary,
                    leadingIcon = Icons.Default.Star,
                    text = "Finish"
                )
                RoundPillButton(
                    onClick = onResetClick,
                    containerColor = cs.onPrimary.copy(alpha = 0.12f),
                    borderColor = cs.onPrimary.copy(alpha = 0.22f),
                    contentColor = cs.onPrimary,
                    leadingIcon = Icons.Default.Refresh
                )
            }
        }
    }
}
