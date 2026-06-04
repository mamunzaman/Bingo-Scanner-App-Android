package com.example.mamunbingoapp.ui.screens.scan

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.domain.model.BingoScanType
import com.example.mamunbingoapp.theme.AppTextStyles
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.IconContainerBg
import com.example.mamunbingoapp.ui.components.AppBottomBarScrollExtraPadding
import com.example.mamunbingoapp.ui.components.AppConfirmDialog
import com.example.mamunbingoapp.ui.components.AppTopBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val ScanPrimaryCtaHeight = 56.dp
private val ScanSecondaryCtaHeight = 52.dp
private val ScanCtaIconSize = 26.dp
private val ScanCtaIconGap = Dimens.spacing12
private val ScanManualBadgeMaxSize = 64.dp
private val ScanManualBadgeMinSize = 52.dp
private val ScanLayoutReferenceHeight = 720.dp
private val ScanHeroCurveClearance = Dimens.spacing16

private const val CameraTapBounceScale = 0.96f
private const val CameraTapBounceLegMs = 60

private data class ScanLayoutMetrics(
    val illustrationScale: Float,
    val heroGap: Dp,
    val manualGap: Dp,
    val badgeSize: Dp,
    val illustrationMaxHeight: Dp,
    val fabSafePadding: Dp,
)

@Composable
private fun rememberScanLayoutMetrics(screenHeight: Dp): ScanLayoutMetrics {
    val heightScale = (screenHeight / ScanLayoutReferenceHeight).coerceIn(0.62f, 1f)
    val fabSafePadding = AppBottomBarScrollExtraPadding + Dimens.spacing16

    val heroGap = lerpDp(Dimens.spacing4, Dimens.spacing12, heightScale)
    val manualGap = lerpDp(Dimens.spacing4, Dimens.spacing8, heightScale)
    val badgeSize = lerpDp(ScanManualBadgeMinSize, ScanManualBadgeMaxSize, heightScale)

    val manualBlock = badgeSize +
        manualGap * 4 +
        ScanSecondaryCtaHeight +
        Dimens.spacing8 +
        fabSafePadding +
        88.dp

    val heroBlock = Dimens.topBarHeight +
        heroGap * 4 +
        ScanPrimaryCtaHeight +
        ScanHeroCurveClearance +
        ScanHeroBottomCurveHeight +
        96.dp

    val illustrationMaxHeight = (screenHeight - manualBlock - heroBlock).coerceAtLeast(72.dp)
    val illustrationScale = (illustrationMaxHeight / 160.dp).coerceIn(0.52f, 1f)

    return ScanLayoutMetrics(
        illustrationScale = illustrationScale,
        heroGap = heroGap,
        manualGap = manualGap,
        badgeSize = badgeSize,
        illustrationMaxHeight = illustrationMaxHeight,
        fabSafePadding = fabSafePadding,
    )
}

@Composable
fun ScanScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onLaunchCamera: (BingoScanType) -> Unit,
    onOpenNumberPad: () -> Unit,
    requestShowScanTypeSheet: Boolean = false,
    onScanTypeSheetRequestConsumed: () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    val heroGreen = colors.primaryContainer
    var showScanTypeSheet by remember { mutableStateOf(requestShowScanTypeSheet) }
    LaunchedEffect(requestShowScanTypeSheet) {
        if (requestShowScanTypeSheet) {
            showScanTypeSheet = true
            onScanTypeSheetRequestConsumed()
        }
    }
    var showScanTipsDialog by remember { mutableStateOf(false) }
    val animationsEnabled = rememberScanAnimationsEnabled()

    AppConfirmDialog(
        visible = showScanTipsDialog,
        title = stringResource(R.string.import_ticket_scan_tips_title),
        message = "",
        showCancelButton = false,
        confirmText = stringResource(R.string.import_ticket_scan_tips_dialog_ok),
        onConfirm = { showScanTipsDialog = false },
        onCancel = { showScanTipsDialog = false },
        onDismiss = { showScanTipsDialog = false },
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacing8)) {
                listOf(
                    R.string.import_ticket_scan_tip_1,
                    R.string.import_ticket_scan_tip_2,
                    R.string.import_ticket_scan_tip_3,
                ).forEach { resId ->
                    Text(
                        text = "• ${stringResource(resId)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(Dimens.spacing4))
                Text(
                    text = stringResource(R.string.import_ticket_scan_tips_footer),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }
        },
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(colors.surface),
    ) {
        val metrics = rememberScanLayoutMetrics(maxHeight)

        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(heroGreen),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AppTopBar(
                        title = stringResource(R.string.scan_screen_title),
                        titleContent = {
                            Text(
                                text = stringResource(R.string.scan_screen_title),
                                style = MaterialTheme.typography.titleLarge,
                                color = colors.primary,
                                modifier = Modifier.semantics { heading() },
                            )
                        },
                        actions = {
                            IconButton(
                                onClick = { showScanTipsDialog = true },
                                modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                                    contentDescription = stringResource(R.string.import_ticket_scan_tips_title),
                                    tint = colors.primary,
                                    modifier = Modifier.size(Dimens.iconDefault),
                                )
                            }
                        },
                    )
                    ScanAutoSection(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        animationsEnabled = animationsEnabled,
                        metrics = metrics,
                        onLaunchCamera = { showScanTypeSheet = true },
                    )
                }
                ScanHeroBottomCurve(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    fillColor = colors.surface,
                )
            }
            ScanManualSection(
                onOpenNumberPad = onOpenNumberPad,
                metrics = metrics,
            )
        }
    }

    if (showScanTypeSheet) {
        ScanTypeSelectionSheet(
            onDismiss = { showScanTypeSheet = false },
            onScanTypeSelected = { type ->
                showScanTypeSheet = false
                onLaunchCamera(type)
            },
        )
    }
}

@Composable
private fun ScanAutoSection(
    modifier: Modifier,
    animationsEnabled: Boolean,
    metrics: ScanLayoutMetrics,
    onLaunchCamera: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val launchCameraLatest by rememberUpdatedState(onLaunchCamera)
    var tapTarget by remember { mutableFloatStateOf(1f) }
    val tapBounceScale by animateFloatAsState(
        targetValue = tapTarget,
        animationSpec = tween(CameraTapBounceLegMs, easing = FastOutSlowInEasing),
        label = "cameraTapBounce",
    )
    val launchCameraLabel = stringResource(R.string.scan_launch_camera)
    val launchCameraWithHaptic = remember(haptics, scope, launchCameraLabel) {
        {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            scope.launch {
                tapTarget = CameraTapBounceScale
                delay(CameraTapBounceLegMs.toLong())
                tapTarget = 1f
            }
            launchCameraLatest()
        }
    }
    val ctaShape = RoundedCornerShape(Dimens.radiusMedium)

    Column(
        modifier = modifier
            .padding(horizontal = Dimens.screenHorizontalPadding)
            .padding(bottom = ScanHeroBottomCurveHeight + ScanHeroCurveClearance),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ScanScreenHeroIllustration(
            animationsEnabled = animationsEnabled,
            contentScale = metrics.illustrationScale,
            maxIllustrationHeight = metrics.illustrationMaxHeight,
            modifier = Modifier
                .weight(1f, fill = false)
                .fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(metrics.heroGap))
        Text(
            text = stringResource(R.string.scan_direct_scan_title),
            style = AppTextStyles.sectionTitle,
            color = colors.primary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(metrics.heroGap))
        Text(
            text = stringResource(R.string.scan_direct_scan_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier
                .widthIn(max = 320.dp)
                .fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(metrics.heroGap))
        Button(
            onClick = launchCameraWithHaptic,
            modifier = Modifier
                .fillMaxWidth()
                .height(ScanPrimaryCtaHeight)
                .shadow(Dimens.cardElevationSubtle, ctaShape, clip = false)
                .graphicsLayer {
                    scaleX = tapBounceScale
                    scaleY = tapBounceScale
                },
            shape = ctaShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = colors.onPrimary,
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = Dimens.cardElevationSubtle,
                pressedElevation = Dimens.spacing4,
            ),
        ) {
            Icon(
                imageVector = Icons.Filled.PhotoCamera,
                contentDescription = null,
                modifier = Modifier.size(ScanCtaIconSize),
            )
            Spacer(modifier = Modifier.width(ScanCtaIconGap))
            Text(
                text = launchCameraLabel,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun ScanManualSection(
    onOpenNumberPad: () -> Unit,
    metrics: ScanLayoutMetrics,
) {
    val colors = MaterialTheme.colorScheme
    val openNumberPadLabel = stringResource(R.string.scan_open_number_pad)
    val secondaryShape = RoundedCornerShape(Dimens.radiusButtonPill)
    val badgeIconSize = metrics.badgeSize * 0.44f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface)
            .padding(horizontal = Dimens.screenHorizontalPadding)
            .padding(top = Dimens.spacing8)
            .padding(bottom = metrics.fabSafePadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            modifier = Modifier.size(metrics.badgeSize),
            shape = CircleShape,
            color = IconContainerBg,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = Icons.Outlined.GridOn,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(badgeIconSize),
                )
            }
        }
        Spacer(modifier = Modifier.height(metrics.manualGap))
        Text(
            text = stringResource(R.string.scan_enter_numbers_title),
            style = AppTextStyles.sectionTitle,
            color = colors.primary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(metrics.manualGap))
        Text(
            text = stringResource(R.string.scan_enter_numbers_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier
                .widthIn(max = 320.dp)
                .fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(metrics.manualGap))
        OutlinedButton(
            onClick = onOpenNumberPad,
            modifier = Modifier
                .fillMaxWidth()
                .height(ScanSecondaryCtaHeight),
            shape = secondaryShape,
            border = BorderStroke(Dimens.cardBorderDefault, colors.primary),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primary),
        ) {
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = null,
                modifier = Modifier.size(ScanCtaIconSize),
            )
            Spacer(modifier = Modifier.width(ScanCtaIconGap))
            Text(
                text = openNumberPadLabel,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

private fun lerpDp(start: Dp, stop: Dp, fraction: Float): Dp =
    start + (stop - start) * fraction.coerceIn(0f, 1f)

private fun Dp.coerceAtLeast(minimumValue: Dp): Dp =
    if (this >= minimumValue) this else minimumValue
