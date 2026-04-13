package com.example.mamunbingoapp.ui.core.interaction

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween

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
