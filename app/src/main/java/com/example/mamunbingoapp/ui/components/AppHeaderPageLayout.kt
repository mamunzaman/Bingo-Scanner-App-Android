@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.example.mamunbingoapp.theme.Dimens

/**
 * Shared shell for screens that show [AppHeaderBackground].
 * Provides a [surface]-filled root, the decorative gradient band (top 40 %),
 * and a [Column] for [topBar] + caller [content] with IME-aware insets.
 *
 * @param scrollableContent When true, wraps [content] in a vertically scrollable IME-aware
 * column (auth / short forms). When false, only passes [content] to a plain Column so nested
 * scroll views (e.g. Profile, Settings) can scroll without double-scroll.
 * Form screens with decorative bottom art should append [AppImeFormScrollBottomSpacer] manually
 * at the end of their scroll content, after buttons/footers.
 * @param contentTopPadding Body inset below [topBar]; use [Dimens.pageContentTopPadding] on tab
 * screens. Pass [0.dp] when there is no top bar (e.g. login hero layout).
 */
@Composable
fun AppHeaderPageLayout(
    modifier: Modifier = Modifier,
    headerHeightFraction: Float = 0.4f,
    scrollableContent: Boolean = false,
    contentTopPadding: Dp = Dimens.pageContentTopPadding,
    topBar: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scrollState = rememberScrollState()
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        AppHeaderBackground(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(headerHeightFraction)
                .align(Alignment.TopCenter),
        )
        Column(Modifier.fillMaxSize()) {
            topBar()
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(top = contentTopPadding)
                    .then(
                        if (scrollableContent) {
                            Modifier.appImeScrollable(scrollState)
                        } else {
                            Modifier
                        },
                    ),
                content = content,
            )
        }
    }
}
