package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Shared pull-to-refresh for remote sync (e.g. Supabase profile).
 *
 * TODO: Local-only Bingo screens (History, Live play, ticket lists) can reuse this wrapper
 * later with a Room/cache reload callback — do not sync Bingo ticket/session data to cloud.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AppPullRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = onRefresh,
    )
    Box(
        modifier = modifier.pullRefresh(
            state = pullRefreshState,
            enabled = enabled,
        ),
    ) {
        content()
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
        )
    }
}
