package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.theme.Dimens
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CalledHistoryPanel(
    calledNumbers: List<Int>,
    isCallLimitReached: Boolean = false,
    modifier: Modifier = Modifier,
    maxPerColumn: Int = 4,
    showTitle: Boolean = true,
    showLimitMessage: Boolean = true
) {
    val colorScheme = MaterialTheme.colorScheme
    val lastCalled = calledNumbers.lastOrNull()
    val displayList = calledNumbers
    val isEmpty = displayList.isEmpty()
    val shape = RoundedCornerShape(Dimens.radiusCard)
    val listState = rememberLazyListState()
    var newlyAdded by remember { mutableStateOf(false) }
    var previousHighlightedLast by remember { mutableStateOf<Int?>(null) }
    var hasDoneInitialScroll by remember { mutableStateOf(false) }
    var previousLastCalled by remember { mutableStateOf<Int?>(null) }
    var newCountWhileAway by remember { mutableStateOf(0) }
    var previousSizeWhenAway by remember { mutableStateOf(0) }
    LaunchedEffect(lastCalled) {
        if (lastCalled != null && lastCalled != previousHighlightedLast) {
            newlyAdded = true
            previousHighlightedLast = lastCalled
        }
    }
    LaunchedEffect(newlyAdded) {
        if (newlyAdded) {
            delay(400)
            newlyAdded = false
        }
    }
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val lastIdx = (displayList.size - 1).coerceAtLeast(0)
    val isNearEnd = isEmpty || listState.layoutInfo.visibleItemsInfo.any { it.index >= lastIdx - 1 }
    val showJumpToLatest = !isEmpty && !isNearEnd
    LaunchedEffect(displayList.size, isNearEnd) {
        if (isEmpty) return@LaunchedEffect
        if (isNearEnd) {
            newCountWhileAway = 0
            previousSizeWhenAway = displayList.size
        } else {
            if (displayList.size > previousSizeWhenAway) {
                newCountWhileAway += displayList.size - previousSizeWhenAway
            }
            previousSizeWhenAway = displayList.size
        }
    }
    val trailingPx = with(density) { Dimens.spacing8.toPx() }
    val scrollJobRef = remember { object { var job: Job? = null } }
    LaunchedEffect(lastCalled, displayList.size) {
        scrollJobRef.job?.cancel()
        if (isEmpty) return@LaunchedEffect
        val lastIndex = displayList.size - 1
        val trailingPx = with(density) { Dimens.spacing8.toPx() }
        val doInitial = !hasDoneInitialScroll
        val latestLastCalled = lastCalled
        scrollJobRef.job = scope.launch {
            if (doInitial) {
                listState.scrollToItem(lastIndex)
                listState.scroll { scrollBy(-trailingPx) }
                hasDoneInitialScroll = true
            } else if (latestLastCalled != null && latestLastCalled != previousLastCalled) {
                val visibleIndices = listState.layoutInfo.visibleItemsInfo.map { it.index }
                val nearEnd = visibleIndices.any { it >= lastIndex - 1 }
                if (nearEnd) {
                    listState.animateScrollToItem(lastIndex)
                    listState.scroll { scrollBy(-trailingPx) }
                }
            }
            if (latestLastCalled != null) previousLastCalled = latestLastCalled
        }
    }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, colorScheme.outlineVariant.copy(alpha = 0.5f), shape),
        shape = shape,
        color = colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(
                start = Dimens.spacing16,
                end = Dimens.spacing16,
                top = Dimens.spacing12,
                bottom = Dimens.spacing12
            )
        ) {
            if (showTitle) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Called History",
                        style = MaterialTheme.typography.titleMedium,
                        color = colorScheme.onSurface
                    )
                    TextButton(
                        onClick = {
                            if (!showJumpToLatest) return@TextButton
                            newCountWhileAway = 0
                            previousSizeWhenAway = displayList.size
                            scope.launch {
                                listState.animateScrollToItem(displayList.size - 1)
                                listState.scroll { scrollBy(-trailingPx) }
                            }
                        },
                        contentPadding = PaddingValues(horizontal = Dimens.spacing8, vertical = Dimens.spacing4),
                        modifier = Modifier
                            .widthIn(min = Dimens.spacing8)
                            .then(if (showJumpToLatest) Modifier else Modifier
                                .alpha(0f)
                                .semantics(mergeDescendants = true) { contentDescription = "" }
                                .focusProperties { canFocus = false }
                            )
                    ) {
                        Text(
                            text = if (newCountWhileAway > 0) "Latest ($newCountWhileAway new)" else "Latest",
                            style = MaterialTheme.typography.labelMedium,
                            color = colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Dimens.spacing8))
            }
            if (isEmpty) {
                EmptyStateBlock(
                    icon = Icons.Default.Numbers,
                    title = "No numbers called yet",
                    subtitle = "Called numbers will appear here during the game."
                )
            } else {
                LazyRow(
                    state = listState,
                    contentPadding = PaddingValues(start = Dimens.spacing8, end = Dimens.spacing8),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(displayList, key = { _, num -> num }) { index, num ->
                        CalledNumberChip(
                            number = num,
                            isActive = num == lastCalled,
                            isNewlyAdded = num == lastCalled && newlyAdded
                        )
                    }
                }
                if (showLimitMessage && isCallLimitReached) {
                    Spacer(modifier = Modifier.height(Dimens.spacing8))
                    Text(
                        text = "Call numbers ended",
                        style = MaterialTheme.typography.labelLarge,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
