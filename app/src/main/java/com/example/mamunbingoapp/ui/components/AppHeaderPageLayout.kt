package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Shared shell for screens that show [AppHeaderBackground].
 * Provides a [surface]-filled root, the decorative gradient band (top 40 %),
 * and a [Column] for [topBar] + caller [content].
 */
@Composable
fun AppHeaderPageLayout(
    modifier: Modifier = Modifier,
    headerHeightFraction: Float = 0.4f,
    topBar: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        AppHeaderBackground(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(headerHeightFraction)
                .align(Alignment.TopCenter)
        )
        Column(Modifier.fillMaxSize()) {
            topBar()
            content()
        }
    }
}
