package com.example.mamunbingoapp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.theme.Dimens
import androidx.compose.ui.text.style.TextOverflow
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.tooling.preview.Preview

enum class AppTab(
    val route: String,
    @StringRes val labelResId: Int,
    val icon: ImageVector
) {
    Home("home", R.string.tab_home, Icons.Filled.Home),
    Scan("scan", R.string.tab_scan, Icons.Filled.QrCodeScanner),
    Jackpot("jackpot", R.string.tab_jackpot, Icons.Filled.EmojiEvents),
    Projects("projects", R.string.tab_projects, Icons.Filled.Newspaper),
    Profile("profile", R.string.tab_profile, Icons.Filled.Person)
}

/** Space reserved above [AppBottomBar] for Home floating scan FAB. */
val AppBottomBarFabClearance: Dp = 80.dp

@Composable
fun AppBottomBar(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier,
    showTopShadow: Boolean = true
) {
    val cs = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(if (showTopShadow) Modifier.shadow(Dimens.cardElevationSubtle) else Modifier)
    ) {
        Column(modifier = Modifier.background(cs.surface)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(cs.outlineVariant.copy(alpha = Dimens.outlineDividerAlpha))
            )
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .defaultMinSize(minHeight = 56.dp)
                    .padding(horizontal = Dimens.spacing8, vertical = 4.dp)
                    .navigationBarsPadding()
            ) {
                val itemWidth = maxWidth / AppTab.entries.size
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppTab.entries.forEach { tab ->
                        val selected = selectedTab == tab
                        val tabLabel = stringResource(tab.labelResId)
                        val pillShape = RoundedCornerShape(Dimens.radiusPill)
                        val interactionSource = remember { MutableInteractionSource() }
                        val iconScale by animateFloatAsState(
                            targetValue = if (selected) 1f else 0.94f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "bottomBarIconScale"
                        )
                        val contentLift by animateDpAsState(
                            targetValue = if (selected) (-1).dp else 0.dp,
                            animationSpec = tween(durationMillis = 180),
                            label = "bottomBarContentLift"
                        )
                        val pillColor by animateColorAsState(
                            targetValue = if (selected) cs.primaryContainer else Color.Transparent,
                            animationSpec = tween(durationMillis = 180),
                            label = "bottomBarPillColor"
                        )
                        val iconTint by animateColorAsState(
                            targetValue = if (selected) cs.onPrimaryContainer else cs.onSurfaceVariant.copy(alpha = 0.65f),
                            animationSpec = tween(durationMillis = 180),
                            label = "bottomBarIconTint"
                        )
                        val labelTint by animateColorAsState(
                            targetValue = if (selected) cs.onPrimaryContainer else cs.onSurfaceVariant.copy(alpha = 0.7f),
                            animationSpec = tween(durationMillis = 180),
                            label = "bottomBarLabelTint"
                        )
                        Column(
                            modifier = Modifier
                                .width(itemWidth)
                                .wrapContentHeight()
                                .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                                .offset(y = contentLift)
                                .semantics { this[SemanticsProperties.Selected] = selected }
                                .clickable(
                                    indication = null,
                                    interactionSource = interactionSource
                                ) { onTabSelected(tab) },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        pillColor,
                                        pillShape
                                    )
                                    .padding(horizontal = Dimens.spacing12, vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tabLabel,
                                    modifier = Modifier
                                        .size(Dimens.iconDefault)
                                        .scale(iconScale),
                                    tint = iconTint
                                )
                            }
                            Text(
                                text = tabLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = labelTint,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppBottomBarPreview() {
    com.example.mamunbingoapp.theme.MamunBingoTheme {
        AppBottomBar(selectedTab = AppTab.Home, onTabSelected = {})
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AppBottomBarDarkPreview() {
    com.example.mamunbingoapp.theme.MamunBingoTheme(darkTheme = true) {
        AppBottomBar(selectedTab = AppTab.Jackpot, onTabSelected = {})
    }
}
