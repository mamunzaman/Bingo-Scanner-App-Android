package com.example.mamunbingoapp.theme

import androidx.compose.ui.unit.dp

object Dimens {
    val spacing4 = 4.dp
    val spacing5 = 5.dp
    val spacing8 = 8.dp
    val spacing10 = 10.dp
    val spacing12 = 12.dp
    val spacing14 = 14.dp
    val spacing16 = 16.dp
    val spacing20 = 20.dp
    val spacing24 = 24.dp
    val spacing32 = 32.dp

    val radiusXSmall = 3.dp
    val radiusSmall = 12.dp
    val radiusSearchField = 14.dp
    val radiusMedium = 18.dp
    val radiusLarge = 20.dp
    val radiusXL = 28.dp
    val radiusBingoCell = 8.dp
    val radiusPill = 20.dp
    val radiusButtonPill = 22.dp
    val progressBarHeight = 7.dp
    val progressBarRadius = 10.dp
    val inputBarHeight = 58.dp
    val inputBarIconSize = 54.dp
    val bingoCellSize = 56.dp
    val borderBingoUnmarked = 1.5.dp
    val borderBingoMarked = 2.dp
    val radiusCard = 16.dp
    val cardElevationDefault = 6.dp
    val cardElevationSubtle = 2.dp
    val cardBorderDefault = 1.dp
    val liveCardNumberMinHeight = 180.dp
    val buttonHeight = 48.dp
    val textFieldHeight = 56.dp

    val iconDefault = 24.dp
    val iconCompact = 20.dp
    val iconAlert = 16.dp
    val iconAlertBox = 36.dp
    val iconEmptyState = 48.dp
    val miniBingoPreviewSize = 52.dp

    val screenHorizontalPadding = spacing16
    val topBarHeight = 64.dp

    /** Fixed leading slot in [com.example.mamunbingoapp.ui.components.AppPageTopBar] (back [IconButton] centered, 48dp touch). */
    val topBarLeadingSlotWidth = 40.dp

    /** Gap between leading slot and title on back navigation pages. */
    val topBarTitleAfterLeadingGap = spacing8

    /** Gap between [com.example.mamunbingoapp.ui.components.AppPageTopBar] and scrollable body on main screens. */
    val pageContentTopPadding = spacing16

    /** Base scroll bottom inset above shell bottom bar; add [com.example.mamunbingoapp.ui.components.AppBottomBarScrollExtraPadding] on tab roots. */
    val pageContentBottomPadding = spacing16

    /**
     * Divider/border opacity on outlineVariant — delegates to [AppAlpha] (canonical source).
     * Names retained for backward compatibility at existing call sites.
     */
    const val outlineDividerAlpha = AppAlpha.AlphaDivider
    const val outlineBorderAlpha = AppAlpha.AlphaBorder
}
