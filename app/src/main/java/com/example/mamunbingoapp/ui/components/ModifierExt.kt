package com.example.mamunbingoapp.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.iosElevatedShadow(
    elevation: Dp = 4.dp,
    shape: Shape,
    clip: Boolean = false
): Modifier = shadow(
    elevation = elevation,
    shape = shape,
    clip = clip,
    ambientColor = Color.Black.copy(alpha = 0.05f),
    spotColor = Color.Black.copy(alpha = 0.05f)
)
