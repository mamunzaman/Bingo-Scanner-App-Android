@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Shared IME inset handling for edge-to-edge form screens.
 * Use on scrollable form content or on the main content slot of [AppHeaderPageLayout].
 */
fun Modifier.appImeFormInsets(): Modifier = this
    .imePadding()

fun Modifier.appImeScrollable(scrollState: ScrollState): Modifier = this
    .appImeFormInsets()
    .verticalScroll(scrollState)

/**
 * Extra scroll range at the bottom of a form while the IME is visible.
 * Collapses to zero when the keyboard is closed — no permanent blank gap.
 * Place as the last child inside scrollable form content — after buttons, footers,
 * and decorative bottom sections (e.g. Login wave + footer).
 */
@Composable
fun AppImeFormScrollBottomSpacer(modifier: Modifier = Modifier) {
    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(0.dp)
            .imePadding(),
    )
}

@Composable
fun AppImeScrollColumn(
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    contentPadding: PaddingValues = PaddingValues(),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .appImeScrollable(scrollState)
            .padding(contentPadding),
        content = content,
    )
}
