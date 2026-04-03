package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp

@Composable
fun AuthBottomWave(modifier: Modifier = Modifier) {
    val waveColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(128.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val path = Path().apply {
                moveTo(0f, size.height)
                cubicTo(
                    size.width * 0.2f, 0f,
                    size.width * 0.5f, 0f,
                    size.width, size.height
                )
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(path = path, color = waveColor)
        }
    }
}

@Composable
fun AuthBottomPlant(modifier: Modifier = Modifier) {
    Icon(
        imageVector = Icons.Filled.Eco,
        contentDescription = null,
        modifier = modifier.size(240.dp),
        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    )
}
