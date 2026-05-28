package com.example.mamunbingoapp.ui.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.theme.CardBorderGreen
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.EmptyHistoryCardBg
import com.example.mamunbingoapp.theme.IconContainerBg
import com.example.mamunbingoapp.theme.OnPrimary
import com.example.mamunbingoapp.theme.Primary
import com.example.mamunbingoapp.theme.PrimaryContainer
import com.example.mamunbingoapp.theme.PrimaryDark
import com.example.mamunbingoapp.ui.components.iosElevatedShadow

data class QuickActionItem(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val emphasized: Boolean = false,
)

private val pillShape = RoundedCornerShape(Dimens.radiusMedium)
private val pillHeight = 44.dp
private val pillMinWidth = 136.dp
private val fabSize = 56.dp

@Composable
fun QuickActionsScrollRow(
    items: List<QuickActionItem>,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(end = Dimens.spacing12),
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing12),
    ) {
        items(items, key = { it.label }) { item ->
            QuickActionPillCard(
                label = item.label,
                icon = item.icon,
                onClick = item.onClick,
                emphasized = item.emphasized,
            )
        }
    }
}

@Composable
fun QuickActionPillCard(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
) {
    val surfaceColor = if (emphasized) PrimaryContainer.copy(alpha = 0.72f) else EmptyHistoryCardBg
    val borderColor = if (emphasized) {
        Primary.copy(alpha = 0.34f)
    } else {
        CardBorderGreen.copy(alpha = 0.62f)
    }
    val iconBg = if (emphasized) Primary.copy(alpha = 0.14f) else IconContainerBg
    Row(
        modifier = modifier
            .defaultMinSize(minWidth = pillMinWidth, minHeight = pillHeight)
            .height(pillHeight)
            .iosElevatedShadow(elevation = if (emphasized) 4.dp else 3.dp, shape = pillShape)
            .clip(pillShape)
            .background(surfaceColor)
            .border(Dimens.cardBorderDefault, borderColor, pillShape)
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.spacing12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(Dimens.radiusSmall))
                .background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(16.dp),
            )
        }
        Spacer(modifier = Modifier.width(Dimens.spacing8))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (emphasized) FontWeight.Bold else FontWeight.SemiBold,
            color = PrimaryDark,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun HomeQuickScanFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = CircleShape
    Box(
        modifier = modifier
            .size(fabSize)
            .iosElevatedShadow(elevation = 8.dp, shape = shape)
            .clip(shape)
            .background(PrimaryDark)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Scan ticket",
            tint = OnPrimary,
            modifier = Modifier.size(26.dp),
        )
    }
}
