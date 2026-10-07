package com.example.mamunbingoapp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.annotation.StringRes
import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.OnPrimary
import com.example.mamunbingoapp.theme.Primary
import com.example.mamunbingoapp.theme.PrimaryDark
import com.example.mamunbingoapp.ui.core.interaction.AppMotion
import com.example.mamunbingoapp.ui.core.interaction.livePlayHaloFrame
import com.example.mamunbingoapp.ui.core.interaction.rememberAppAnimationsEnabled

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

private val JackpotDiamondSize = 50.dp
private val JackpotHaloSize = 76.dp
private val JackpotDiamondDrop = 4.dp
val AppBottomBarFadeHeight = AppMotion.BottomBarFadeHeightDp.dp
private val BottomBarHeight = 72.dp
val AppBottomBarControlHeight: Dp = BottomBarHeight
val AppBottomBarContentPeek = 0.dp

/** Overlay height: fade + tab row (system nav inset owned by the window / shell). */
val AppBottomBarShellHeight: Dp = AppBottomBarFadeHeight + BottomBarHeight

/** Jackpot diamond overlap above the control row. Decorative; not used in scroll padding. */
val AppBottomBarDiamondProtrusion: Dp = JackpotDiamondSize / 2 - JackpotDiamondDrop

/**
 * One shared scroll-end clearance for main tabs: control row + modest gap.
 * Does not include fade height, halo diameter, or system navigation inset.
 */
val AppBottomBarScrollExtraPadding: Dp = BottomBarHeight + Dimens.spacing12

/** Space reserved above [AppBottomBar] for Home floating scan FAB. */
val AppBottomBarFabClearance: Dp =
    BottomBarHeight + AppBottomBarDiamondProtrusion + Dimens.spacing16

/** Same as [AppBottomBarScrollExtraPadding]; system inset is owned by MainShell + the bar. */
@Composable
fun appBottomBarTotalScrollBottomPadding(): Dp = AppBottomBarScrollExtraPadding

fun Modifier.appBottomBarScrollExtraPadding(): Modifier =
    padding(bottom = AppBottomBarScrollExtraPadding)
private val BottomBarTabIconSize = 22.dp
private val BottomBarTabHeight = 52.dp
private val BottomBarLabelBottomPad = Dimens.spacing4
private val BottomBarPillShape = RoundedCornerShape(Dimens.radiusSearchField)
private const val BottomBarSelectedPillAlpha = 0.42f
private const val SCAN_PIPELINE_BOTTOM_BAR_LOG = "ScanPipelineBusy"

@Composable
fun AppBottomBar(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier,
    showTopShadow: Boolean = true,
    tabsEnabled: Boolean = true,
) {
    val selectTab: (AppTab) -> Unit = selectTab@{ tab ->
        if (!tabsEnabled) {
            Log.d(SCAN_PIPELINE_BOTTOM_BAR_LOG, "bottom tab ignored (scan busy): ${tab.name}")
            return@selectTab
        }
        onTabSelected(tab)
    }
    val cs = MaterialTheme.colorScheme
    val jackpotLabel = stringResource(AppTab.Jackpot.labelResId)
    val jackpotSelected = selectedTab == AppTab.Jackpot
    val animationsEnabled = rememberAppAnimationsEnabled()
    val haloPulse = if (animationsEnabled) {
        val haloTransition = rememberInfiniteTransition(label = "livePlayHalo")
        haloTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(AppMotion.HaloMs, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "livePlayHaloPulse",
        ).value
    } else {
        0f
    }
    val surface = cs.surface
    val fadeBrush = Brush.verticalGradient(
        0f to Color.Transparent,
        0.30f to surface.copy(alpha = AppMotion.BottomBarFadeFirstAlpha),
        0.65f to surface.copy(alpha = AppMotion.BottomBarFadeMidAlpha),
        1f to surface.copy(alpha = AppMotion.BottomBarFadeEndAlpha),
    )
    val barFill = surface.copy(alpha = AppMotion.BottomBarControlAlpha)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { clip = false },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .graphicsLayer { clip = false },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppBottomBarFadeHeight)
                    .background(fadeBrush),
            )
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(BottomBarHeight)
                    .background(barFill)
                    .padding(horizontal = Dimens.spacing8),
            ) {
                val itemWidth = maxWidth / AppTab.entries.size
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = Dimens.spacing8),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    AppTab.entries.forEach { tab ->
                        val selected = selectedTab == tab
                        val tabLabel = stringResource(tab.labelResId)
                        if (tab == AppTab.Jackpot) {
                            BottomBarJackpotLabelTab(
                                selected = selected,
                                label = tabLabel,
                                onClick = { selectTab(tab) },
                                tabsEnabled = tabsEnabled,
                                modifier = Modifier.width(itemWidth),
                            )
                        } else {
                            BottomBarStandardTab(
                                tab = tab,
                                selected = selected,
                                label = tabLabel,
                                onClick = { selectTab(tab) },
                                tabsEnabled = tabsEnabled,
                                modifier = Modifier.width(itemWidth),
                            )
                        }
                    }
                }
            }
        }

        BottomBarJackpotDiamondOverlay(
            selected = jackpotSelected,
            label = jackpotLabel,
            onClick = { selectTab(AppTab.Jackpot) },
            tabsEnabled = tabsEnabled,
            haloPulse = haloPulse,
            animationsEnabled = animationsEnabled,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .offset(y = -(BottomBarHeight - JackpotDiamondSize / 2) + JackpotDiamondDrop)
                .zIndex(1f)
                .graphicsLayer { clip = false },
        )
    }
}

@Composable
private fun BottomBarStandardTab(
    tab: AppTab,
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    tabsEnabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1f else 0.94f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "bottomBarIconScale",
    )
    val pillColor by animateColorAsState(
        targetValue = if (selected) {
            cs.primaryContainer.copy(alpha = BottomBarSelectedPillAlpha)
        } else {
            Color.Transparent
        },
        animationSpec = tween(durationMillis = 180),
        label = "bottomBarPillColor",
    )
    val iconTint by animateColorAsState(
        targetValue = if (selected) {
            cs.primary.copy(alpha = 0.88f)
        } else {
            cs.onSurfaceVariant.copy(alpha = 0.65f)
        },
        animationSpec = tween(durationMillis = 180),
        label = "bottomBarIconTint",
    )
    val labelTint by animateColorAsState(
        targetValue = if (selected) {
            cs.primary.copy(alpha = 0.86f)
        } else {
            cs.onSurfaceVariant.copy(alpha = 0.7f)
        },
        animationSpec = tween(durationMillis = 180),
        label = "bottomBarLabelTint",
    )
    Column(
        modifier = modifier
            .height(BottomBarTabHeight)
            .semantics { this[SemanticsProperties.Selected] = selected }
            .clickable(
                enabled = tabsEnabled,
                indication = null,
                interactionSource = interactionSource,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(
            modifier = Modifier
                .background(pillColor, BottomBarPillShape)
                .padding(horizontal = Dimens.spacing8, vertical = 3.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = label,
                modifier = Modifier
                    .size(BottomBarTabIconSize)
                    .scale(iconScale),
                tint = iconTint,
            )
        }
        Text(
            text = label,
            modifier = Modifier.padding(bottom = BottomBarLabelBottomPad),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            ),
            color = labelTint,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Center slot in the tab row — reserves space for the floating diamond; label only. */
@Composable
private fun BottomBarJackpotLabelTab(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    tabsEnabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val labelTint by animateColorAsState(
        targetValue = if (selected) cs.primary else cs.primary.copy(alpha = 0.78f),
        animationSpec = tween(durationMillis = 180),
        label = "jackpotLabelTint",
    )
    Column(
        modifier = modifier
            .height(BottomBarTabHeight)
            .semantics { this[SemanticsProperties.Selected] = selected }
            .clickable(
                enabled = tabsEnabled,
                indication = null,
                interactionSource = interactionSource,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
    ) {
        Text(
            text = label,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = BottomBarLabelBottomPad),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            textAlign = TextAlign.Center,
            color = labelTint,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Floating diamond — center on bar top edge; overlaps content above. */
@Composable
private fun BottomBarJackpotDiamondOverlay(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    tabsEnabled: Boolean = true,
    haloPulse: Float,
    animationsEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val diamondShape = RoundedCornerShape(Dimens.radiusMedium)
    val gradientTop by animateColorAsState(
        targetValue = if (selected) Primary else Primary.copy(alpha = 0.92f),
        animationSpec = tween(durationMillis = 180),
        label = "jackpotGradTop",
    )
    val gradientBottom by animateColorAsState(
        targetValue = if (selected) PrimaryDark else PrimaryDark.copy(alpha = 0.92f),
        animationSpec = tween(durationMillis = 180),
        label = "jackpotGradBottom",
    )
    Box(
        modifier = modifier
            .size(JackpotHaloSize)
            .graphicsLayer { clip = false },
        contentAlignment = Alignment.Center,
    ) {
        LivePlayHalo(
            selected = selected,
            pulse = haloPulse,
            animationsEnabled = animationsEnabled,
        )
        Box(
            modifier = Modifier
                .size(JackpotDiamondSize)
                .semantics { this[SemanticsProperties.Selected] = selected }
                .clickable(
                    enabled = tabsEnabled,
                    indication = null,
                    interactionSource = interactionSource,
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(JackpotDiamondSize)
                    .shadow(
                        elevation = 4.dp,
                        shape = diamondShape,
                        clip = false,
                        ambientColor = Color.Black.copy(alpha = 0.08f),
                        spotColor = Color.Black.copy(alpha = 0.14f),
                    )
                    .rotate(45f)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(gradientTop, gradientBottom),
                        ),
                        shape = diamondShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier.rotate(-45f),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = label,
                        modifier = Modifier.size(22.dp),
                        tint = OnPrimary,
                    )
                }
            }
        }
    }
}

@Composable
private fun LivePlayHalo(
    selected: Boolean,
    pulse: Float,
    animationsEnabled: Boolean,
) {
    val frame = livePlayHaloFrame(pulse, selected, animationsEnabled)
    Box(
        modifier = Modifier
            .size(JackpotHaloSize)
            .clearAndSetSemantics { }
            .graphicsLayer {
                scaleX = frame.scale
                scaleY = frame.scale
                clip = false
            }
            .rotate(45f)
            .background(
                color = Primary.copy(alpha = frame.alpha),
                shape = RoundedCornerShape(Dimens.radiusLarge),
            ),
    )
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
