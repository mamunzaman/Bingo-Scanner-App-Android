package com.example.mamunbingoapp.ui.core.interaction

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color

fun Modifier.appClickable(
    enabled: Boolean = true,
    ripple: Boolean = true,
    rippleColor: Color = Color.Unspecified,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val indication = if (ripple) appRipple(color = rippleColor) else null
    this.clickable(
        interactionSource = interactionSource,
        indication = indication,
        enabled = enabled,
        onClick = onClick
    )
}
