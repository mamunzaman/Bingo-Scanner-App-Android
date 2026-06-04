package com.example.mamunbingoapp.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.ui.model.BingoCellUi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
fun BingoCell(
    cell: BingoCellUi,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    isWinning: Boolean = false,
    animateWinningPulse: Boolean = false,
    isNearWinning: Boolean = false,
    animateNearWin: Boolean = false,
    numberFontWeight: FontWeight = FontWeight.SemiBold,
) {
    val isClickable = onClick != null && cell.isEditable && !cell.isDisabled
    val winningStrokeColor = MaterialTheme.colorScheme.error
    val nearWinStrokeColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
    val scaleAnim = remember { Animatable(1f) }
    val alphaAnim = remember { Animatable(1f) }
    val nearWinAlphaAnim = remember { Animatable(1f) }
    val hasPulsedForWinning = remember { mutableStateOf(false) }
    val hasEmphasizedNearWin = remember { mutableStateOf(false) }
    LaunchedEffect(animateNearWin) {
        if (animateNearWin && !hasEmphasizedNearWin.value) {
            hasEmphasizedNearWin.value = true
            nearWinAlphaAnim.snapTo(0.96f)
            nearWinAlphaAnim.animateTo(1f, tween<Float>(250, easing = FastOutSlowInEasing))
        } else if (!animateNearWin) {
            hasEmphasizedNearWin.value = false
        }
    }
    LaunchedEffect(animateWinningPulse) {
        if (animateWinningPulse && !hasPulsedForWinning.value) {
            hasPulsedForWinning.value = true
            alphaAnim.snapTo(0.92f)
            coroutineScope {
                launch { scaleAnim.animateTo(1.06f, tween<Float>(150, easing = FastOutSlowInEasing)) }
                launch { alphaAnim.animateTo(1f, tween<Float>(350, easing = FastOutSlowInEasing)) }
            }
            scaleAnim.animateTo(1f, tween<Float>(200, easing = FastOutSlowInEasing))
        } else if (!animateWinningPulse) {
            hasPulsedForWinning.value = false
        }
    }
    val stateDesc = when {
        isWinning -> "winning cell"
        isNearWinning -> "near winning cell"
        else -> null
    }
    Box(
        modifier = modifier
            .then(if (stateDesc != null) Modifier.semantics { stateDescription = stateDesc } else Modifier)
            .scale(scaleAnim.value)
            .alpha(alphaAnim.value * nearWinAlphaAnim.value)
            .aspectRatio(1f)
            .then(if (isNearWinning && !isWinning) Modifier.drawWithContent {
                drawContent()
                drawRect(SolidColor(nearWinStrokeColor), style = Stroke(width = 1.2.dp.toPx()))
            } else Modifier)
            .bingoWinningMarker(isWinning, winningStrokeColor)
            .then(if (isClickable) Modifier.clickable(onClick = onClick!!) else Modifier)
    ) {
        BingoNumberBox(
            numberText = cell.number ?: "",
            isMarked = cell.isMarked,
            isSelected = cell.isSelected,
            modifier = Modifier.fillMaxSize(),
            size = null,
            showBorderWhenMarked = true,
            numberFontWeight = numberFontWeight,
        )
    }
}

@Composable
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
private fun BingoCellMarkedPreview() {
    com.example.mamunbingoapp.theme.MamunBingoTheme {
        Box(Modifier.size(Dimens.bingoCellSize)) {
            BingoCell(
                cell = com.example.mamunbingoapp.ui.model.BingoCellUi("42", isMarked = true, isCalled = false, isEditable = false, isDisabled = false),
                modifier = Modifier.fillMaxSize(),
                isWinning = false
            )
        }
    }
}

@Composable
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
private fun BingoCellMarkedWinningPreview() {
    com.example.mamunbingoapp.theme.MamunBingoTheme {
        Box(Modifier.size(Dimens.bingoCellSize)) {
            BingoCell(
                cell = com.example.mamunbingoapp.ui.model.BingoCellUi("42", isMarked = true, isCalled = false, isEditable = false, isDisabled = false),
                modifier = Modifier.fillMaxSize(),
                isWinning = true
            )
        }
    }
}
