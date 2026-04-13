package com.example.mamunbingoapp.ui.core.interaction

import androidx.compose.foundation.Indication
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun appRipple(
    bounded: Boolean = true,
    color: Color = Color.Unspecified
): Indication = rememberRipple(bounded = bounded, color = color)
