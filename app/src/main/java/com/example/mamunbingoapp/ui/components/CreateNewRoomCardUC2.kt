package com.example.mamunbingoapp.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.theme.Dimens

@Composable
fun CreateNewRoomCardUC2(
    roomsCount: Int,
    activeCount: Int,
    onClick: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val deco = primaryContainer.copy(alpha = 0.22f)
    val decoSoft = primaryContainer.copy(alpha = 0.14f)
    val decoDots = primaryContainer.copy(alpha = 0.18f)
    val shape = RoundedCornerShape(Dimens.radiusCard)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clip(shape)
            .background(primary)
            .drawBehind {
                val w = size.width
                val h = size.height
                val topRight = Offset(w - 8.dp.toPx(), 28.dp.toPx())
                drawCircle(deco, radius = 56.dp.toPx(), center = topRight)
                val bottomLeft = Offset(28.dp.toPx(), h - 24.dp.toPx())
                drawCircle(decoSoft, radius = 36.dp.toPx(), center = bottomLeft)
                listOf(
                    Offset(w - 36.dp.toPx(), 40.dp.toPx()),
                    Offset(w - 52.dp.toPx(), 48.dp.toPx()),
                    Offset(w - 44.dp.toPx(), 56.dp.toPx())
                ).forEach { drawCircle(decoDots, radius = 2.dp.toPx(), center = it) }
            }
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.spacing14, vertical = Dimens.spacing12)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing12)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(Dimens.radiusSmall))
                    .border(1.5.dp, primaryContainer.copy(alpha = 0.7f), RoundedCornerShape(Dimens.radiusSmall))
                    .background(primaryContainer.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    modifier = Modifier.size(22.dp),
                    tint = onPrimary
                )
            }
            Text(
                text = stringResource(R.string.live_nav_create_new_room),
                modifier = Modifier.fillMaxWidth(1f),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = onPrimary
            )
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(Dimens.radiusPill))
                    .background(primaryContainer.copy(alpha = 0.5f))
                    .padding(horizontal = Dimens.spacing8, vertical = Dimens.spacing4),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacing4)
            ) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(primaryContainer)
                )
                Text(
                    text = stringResource(R.string.live_nav_new),
                    style = MaterialTheme.typography.labelSmall,
                    color = onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Dimens.spacing10)
                .clip(RoundedCornerShape(Dimens.radiusPill))
                .background(primaryContainer.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.spacing10, horizontal = Dimens.spacing12),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(0.48f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    content = {
                        Text(
                            text = roomsCount.toString(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = onPrimary
                        )
                        Text(
                            text = " ${stringResource(R.string.live_nav_rooms)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = onPrimary.copy(alpha = 0.85f)
                        )
                    }
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(28.dp)
                        .background(primaryContainer.copy(alpha = 0.6f))
                )
                Row(
                    modifier = Modifier.fillMaxWidth(0.48f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    content = {
                        Text(
                            text = activeCount.toString(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = primaryContainer
                        )
                        Text(
                            text = " ${stringResource(R.string.live_nav_active)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = onPrimary.copy(alpha = 0.85f)
                        )
                    }
                )
            }
        }
    }
}
