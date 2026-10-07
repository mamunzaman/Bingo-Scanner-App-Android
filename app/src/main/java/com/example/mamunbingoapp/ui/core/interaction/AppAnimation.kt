package com.example.mamunbingoapp.ui.core.interaction

import android.provider.Settings
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.currentStateAsState

object AppAnimation {
    const val FAST = 150
    const val MEDIUM = 220
    const val SLOW = 350

    val Easing = FastOutSlowInEasing

    fun <T> fast(): androidx.compose.animation.core.TweenSpec<T> =
        tween(FAST, easing = Easing)

    fun <T> medium(): androidx.compose.animation.core.TweenSpec<T> =
        tween(MEDIUM, easing = Easing)

    fun <T> slow(): androidx.compose.animation.core.TweenSpec<T> =
        tween(SLOW, easing = Easing)
}

/** Shared decorative-loop timings. Perpetual motion must honor [rememberAppAnimationsEnabled]. */
object AppMotion {
    const val AmbientFarMs = 12_000
    const val AmbientNearMs = 10_000
    const val AmbientAlphaMs = 11_000
    const val HaloMs = 2_200
    const val ScanLineMs = 2_400

    val AmbientFarDrift = 6.dp
    val AmbientNearDrift = 3.5.dp

    const val HaloScaleMin = 1f
    const val HaloScaleMax = 1.14f
    const val HaloAlphaUnselectedMax = 0.18f
    const val HaloAlphaSelectedMax = 0.13f
    const val HaloAlphaMin = 0.02f
    const val HaloAlphaStatic = 0.10f

    const val AmbientCircleAlphaMin = 0.10f
    const val AmbientCircleAlphaMax = 0.16f

    const val BottomBarFadeHeightDp = 40f
    const val BottomBarFadeFirstAlpha = 0.25f
    const val BottomBarFadeMidAlpha = 0.72f
    const val BottomBarFadeEndAlpha = 1.0f
    const val BottomBarControlAlpha = 1.0f

    const val ScanLineFrozenProgress = 0.42f
}

data class LivePlayHaloFrame(
    val scale: Float,
    val alpha: Float,
    val animated: Boolean,
)

fun livePlayHaloFrame(
    pulse: Float,
    selected: Boolean,
    animationsEnabled: Boolean,
): LivePlayHaloFrame {
    if (!animationsEnabled) {
        return LivePlayHaloFrame(
            scale = 1.08f,
            alpha = AppMotion.HaloAlphaStatic,
            animated = false,
        )
    }
    val t = pulse.coerceIn(0f, 1f)
    val alphaMax = if (selected) AppMotion.HaloAlphaSelectedMax else AppMotion.HaloAlphaUnselectedMax
    return LivePlayHaloFrame(
        scale = AppMotion.HaloScaleMin + (AppMotion.HaloScaleMax - AppMotion.HaloScaleMin) * t,
        alpha = alphaMax + (AppMotion.HaloAlphaMin - alphaMax) * t,
        animated = true,
    )
}

fun isAnimatorScaleEnabled(animatorDurationScale: Float): Boolean = animatorDurationScale > 0f

@Composable
fun rememberAppAnimationsEnabled(): Boolean {
    val context = LocalContext.current
    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    val scaleEnabled = remember(context) {
        isAnimatorScaleEnabled(
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f,
            ),
        )
    }
    return scaleEnabled && lifecycleState.isAtLeast(Lifecycle.State.STARTED)
}
