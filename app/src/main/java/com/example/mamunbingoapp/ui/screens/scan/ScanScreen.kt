package com.example.mamunbingoapp.ui.screens.scan

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.domain.model.BingoScanType
import com.example.mamunbingoapp.theme.Dimens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val SlantGreenBottomLeftYFrac = 0.50f
private val SlantGreenBottomRightYFrac = 0.58f

private val ScanLaunchCameraButtonHeight = 44.dp
private val NumberPadCtaHeight = 40.dp
private val NumberPadBorderStroke = 0.75.dp

private val ScanIconCircleDiameter = 72.dp
private val ScanIconClusterSize = 84.dp
private val ScanIconCircleRingStroke = 1.dp
private val ScanStepBadgeSize = 26.dp
private val ScanStepBadgeOffsetX = 3.dp
private val ScanStepBadgeOffsetY = 2.dp
private val ScanStepBadgeShadowElevation = 1.dp
private val OrChipHalfHeightDp = 18.dp
private val OrChipSeamNudgeY = 1.dp

private const val ScanIconPressScaleDurationMs = 100
private const val ScanIconPressScalePressed = 0.97f

private const val CameraTapBounceScale = 0.96f
private const val CameraTapBounceLegMs = 60

private fun ScanNumberPadPillShape() = RoundedCornerShape(999.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onLaunchCamera: (BingoScanType) -> Unit,
    onOpenNumberPad: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var showScanTypeSheet by remember { mutableStateOf(false) }
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val h = maxHeight
        val seamCenterYFrac = (SlantGreenBottomLeftYFrac + SlantGreenBottomRightYFrac) / 2f
        val seamCenterDp = h * seamCenterYFrac
        Box(Modifier.fillMaxSize().background(colors.surfaceVariant))
        Box(
            Modifier
                .fillMaxSize()
                .drawBehind {
                    val yL = size.height * SlantGreenBottomLeftYFrac
                    val yR = size.height * SlantGreenBottomRightYFrac
                    val path = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(size.width, 0f)
                        lineTo(size.width, yR)
                        lineTo(0f, yL)
                        close()
                    }
                    drawPath(path, colors.primary)
                }
        )
        Column(Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        text = "Scan",
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.onPrimary,
                        modifier = Modifier.semantics { heading() }
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = colors.onPrimary,
                    navigationIconContentColor = colors.onPrimary,
                    actionIconContentColor = colors.onPrimary
                ),
                windowInsets = WindowInsets.statusBars
            )
            Column(Modifier.weight(1f).fillMaxWidth()) {
                Box(Modifier.weight(0.55f).fillMaxWidth()) {
                    ScanDirectTopSection(
                        onLaunchCamera = { showScanTypeSheet = true },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Box(Modifier.weight(0.45f).fillMaxWidth()) {
                    ScanEnterNumbersBottomSection(
                        onOpenNumberPad = onOpenNumberPad,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = seamCenterDp - OrChipHalfHeightDp + OrChipSeamNudgeY),
            shape = RoundedCornerShape(50),
            color = colors.surface,
            tonalElevation = 1.dp,
            shadowElevation = 2.dp
        ) {
            Text(
                text = "OR",
                modifier = Modifier.padding(horizontal = Dimens.spacing14, vertical = Dimens.spacing8),
                style = MaterialTheme.typography.labelLarge,
                color = colors.onSurface
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
private fun ScanStepBadge(
    label: String,
    background: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.size(ScanStepBadgeSize),
        shape = CircleShape,
        color = background,
        tonalElevation = 0.dp,
        shadowElevation = ScanStepBadgeShadowElevation
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

@Composable
private fun ScanDirectTopSection(
    onLaunchCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val launchCameraLatest by rememberUpdatedState(onLaunchCamera)
    var tapTarget by remember { mutableFloatStateOf(1f) }
    val tapBounceScale by animateFloatAsState(
        targetValue = tapTarget,
        animationSpec = tween(CameraTapBounceLegMs, easing = FastOutSlowInEasing),
        label = "cameraTapBounce"
    )
    val launchCameraWithHaptic = remember(haptics, scope) {
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
    val cameraIconInteraction = remember { MutableInteractionSource() }
    val cameraPressed by cameraIconInteraction.collectIsPressedAsState()
    val cameraPressScale by animateFloatAsState(
        targetValue = if (cameraPressed) ScanIconPressScalePressed else 1f,
        animationSpec = tween(
            durationMillis = ScanIconPressScaleDurationMs,
            easing = FastOutSlowInEasing
        ),
        label = "scanCameraIconPressScale"
    )
    val cameraCircleScale = cameraPressScale * tapBounceScale
    Column(
        modifier = modifier
            .padding(horizontal = Dimens.screenHorizontalPadding)
            .padding(top = Dimens.spacing8)
            .padding(bottom = Dimens.spacing24),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Box(
            modifier = Modifier.size(ScanIconClusterSize),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .size(ScanIconCircleDiameter)
                    .graphicsLayer {
                        scaleX = cameraCircleScale
                        scaleY = cameraCircleScale
                    }
                    .border(
                        width = ScanIconCircleRingStroke,
                        color = colors.onPrimary.copy(alpha = 0.28f),
                        shape = CircleShape
                    )
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = cameraIconInteraction,
                        indication = rememberRipple(
                            color = colors.onPrimary.copy(alpha = 0.30f),
                            bounded = true
                        ),
                        role = Role.Button,
                        onClickLabel = "Launch camera",
                        onClick = launchCameraWithHaptic
                    ),
                shape = CircleShape,
                color = colors.onPrimary.copy(alpha = 0.22f),
                shadowElevation = 0.dp
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Filled.PhotoCamera,
                        contentDescription = null,
                        tint = colors.onPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
            ScanStepBadge(
                label = "1",
                background = colors.onPrimary,
                contentColor = colors.primary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = ScanStepBadgeOffsetX, y = ScanStepBadgeOffsetY)
            )
        }
        Spacer(modifier = Modifier.height(Dimens.spacing12))
        Text(
            text = "Direct Scan",
            style = MaterialTheme.typography.headlineSmall,
            color = colors.onPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(Dimens.spacing5))
        Text(
            text = "Camera reads your ticket instantly",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onPrimary.copy(alpha = 0.92f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(Dimens.spacing16))
        Button(
            onClick = launchCameraWithHaptic,
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = tapBounceScale
                    scaleY = tapBounceScale
                }
                .height(ScanLaunchCameraButtonHeight),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.onPrimary,
                contentColor = colors.primary
            )
        ) {
            Icon(
                imageVector = Icons.Filled.PhotoCamera,
                contentDescription = null,
                modifier = Modifier.size(Dimens.iconDefault)
            )
            Spacer(modifier = Modifier.width(Dimens.spacing8))
            Text(
                text = "Launch camera",
                style = MaterialTheme.typography.labelLarge
            )
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ScanEnterNumbersBottomSection(
    onOpenNumberPad: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val numberPadRowInteraction = remember { MutableInteractionSource() }
    val penIconInteraction = remember { MutableInteractionSource() }
    val penPressed by penIconInteraction.collectIsPressedAsState()
    val penPressScale by animateFloatAsState(
        targetValue = if (penPressed) ScanIconPressScalePressed else 1f,
        animationSpec = tween(
            durationMillis = ScanIconPressScaleDurationMs,
            easing = FastOutSlowInEasing
        ),
        label = "scanPenIconPressScale"
    )
    Column(
        modifier = modifier
            .padding(horizontal = Dimens.screenHorizontalPadding)
            .padding(top = Dimens.spacing16)
            .padding(bottom = Dimens.spacing16),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(ScanIconClusterSize),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(ScanIconCircleDiameter)
                    .graphicsLayer {
                        scaleX = penPressScale
                        scaleY = penPressScale
                    }
                    .clip(CircleShape)
                    .background(colors.surface, CircleShape)
                    .border(
                        width = ScanIconCircleRingStroke,
                        color = colors.outline.copy(alpha = 0.64f),
                        shape = CircleShape
                    )
                    .clickable(
                        interactionSource = penIconInteraction,
                        indication = rememberRipple(
                            color = colors.primary.copy(alpha = 0.14f),
                            bounded = true
                        ),
                        role = Role.Button,
                        onClickLabel = "Open number pad",
                        onClick = onOpenNumberPad
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(36.dp)
                )
            }
            ScanStepBadge(
                label = "2",
                background = colors.primary,
                contentColor = colors.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = ScanStepBadgeOffsetX, y = ScanStepBadgeOffsetY)
            )
        }
        Spacer(modifier = Modifier.height(Dimens.spacing12))
        Text(
            text = "Enter Numbers",
            style = MaterialTheme.typography.headlineSmall,
            color = colors.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(Dimens.spacing8))
        Text(
            text = "Type all 25 numbers yourself",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(Dimens.spacing16))
        val numberPadShape = ScanNumberPadPillShape()
        val numberPadBorderColor = colors.outlineVariant.copy(alpha = 0.9f)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(NumberPadCtaHeight)
                .clip(numberPadShape)
                .background(
                    color = colors.primary.copy(alpha = 0.06f),
                    shape = numberPadShape
                )
                .border(
                    width = NumberPadBorderStroke,
                    color = numberPadBorderColor,
                    shape = numberPadShape
                )
                .clickable(
                    interactionSource = numberPadRowInteraction,
                    indication = rememberRipple(color = colors.primary.copy(alpha = 0.10f)),
                    role = Role.Button,
                    onClick = onOpenNumberPad
                )
                .padding(horizontal = Dimens.spacing12),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = null,
                tint = colors.primary.copy(alpha = 0.88f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(Dimens.spacing8))
            Text(
                text = "Open number pad",
                style = MaterialTheme.typography.labelLarge,
                color = colors.primary.copy(alpha = 0.88f)
            )
        }
    }
}
