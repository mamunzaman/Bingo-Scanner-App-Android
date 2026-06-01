package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.theme.Dimens

/**
 * Shared top bar row for main screens: status bar inset, [Dimens.topBarHeight],
 * [Dimens.screenHorizontalPadding], vertically centered title and actions.
 */
@Composable
fun AppPageTopBar(
    modifier: Modifier = Modifier,
    leading: @Composable RowScope.() -> Unit = {},
    title: @Composable RowScope.() -> Unit,
    actions: @Composable () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = Dimens.screenHorizontalPadding)
            .height(Dimens.topBarHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading()
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.CenterStart,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                title()
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            actions()
        }
    }
}

@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    showBack: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    titleContent: (@Composable () -> Unit)? = null,
    actions: @Composable () -> Unit = {},
) {
    AppPageTopBar(
        modifier = modifier,
        leading = {
            if (showBack && onBackClick != null) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        },
        title = {
            if (titleContent != null) {
                titleContent()
            } else {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.semantics { heading() },
                )
            }
        },
        actions = actions,
    )
}

@Preview(showBackground = true)
@Composable
private fun AppTopBarPreview() {
    com.example.mamunbingoapp.theme.MamunBingoTheme {
        AppTopBar(title = "Ticket Detail", showBack = true, onBackClick = {})
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AppTopBarDarkPreview() {
    com.example.mamunbingoapp.theme.MamunBingoTheme(darkTheme = true) {
        AppTopBar(title = "Sunday Room", showBack = true, onBackClick = {})
    }
}
