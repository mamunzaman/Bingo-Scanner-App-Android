package com.example.mamunbingoapp.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.ui.components.AppInsetDivider

sealed interface SearchHeaderVariant {
    data object SearchOnly : SearchHeaderVariant
    data object SearchSort : SearchHeaderVariant
    data object SearchFilterSort : SearchHeaderVariant
}

private val SearchHeaderContentPadding = PaddingValues(
    start = Dimens.screenHorizontalPadding,
    top = Dimens.spacing12,
    end = Dimens.screenHorizontalPadding,
    bottom = Dimens.spacing8
)

@Composable
fun SearchFilterSortHeader(
    variant: SearchHeaderVariant = SearchHeaderVariant.SearchFilterSort,
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    filterOptions: List<String> = emptyList(),
    selectedFilter: String? = null,
    onFilterSelect: (String) -> Unit = {},
    filterCounts: Map<String, Int> = emptyMap(),
    showFilterCounts: Boolean = false,
    sortOptions: List<String> = emptyList(),
    selectedSort: String? = null,
    onSortSelect: (String) -> Unit = {},
    contentPadding: PaddingValues = SearchHeaderContentPadding,
    modifier: Modifier = Modifier,
) {
    var sortExpanded by remember { mutableStateOf(false) }
    val sortDisplay = selectedSort?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.history_sort_newest)
    val sortLabel = stringResource(R.string.search_sort_by, sortDisplay)

    val showFilterRow = variant == SearchHeaderVariant.SearchFilterSort &&
        filterOptions.isNotEmpty() &&
        selectedFilter != null
    val showSort = (variant == SearchHeaderVariant.SearchSort || variant == SearchHeaderVariant.SearchFilterSort) &&
        sortOptions.isNotEmpty()
    val showSecondRow = showFilterRow || showSort

    Surface(
        shape = RoundedCornerShape(Dimens.radiusLarge),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 3.dp,
        modifier = modifier.padding(contentPadding).fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            val innerHPad = Dimens.spacing16
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.textFieldHeight)
                    .padding(horizontal = innerHPad)
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(Dimens.iconDefault)
                )
                Spacer(modifier = Modifier.width(Dimens.spacing12))
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        if (query.isEmpty()) {
                            Text(
                                placeholder,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        inner()
                    }
                )
            }

            if (showSecondRow) {
                AppInsetDivider(
                    startInset = innerHPad,
                    endInset = innerHPad,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 1.dp,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = innerHPad, vertical = Dimens.spacing8)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .height(36.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (showFilterRow) {
                            val listState = rememberLazyListState()
                            val selectedIndex = remember(filterOptions, selectedFilter) {
                                filterOptions.indexOfFirst { it == selectedFilter }
                            }
                            LaunchedEffect(selectedIndex) {
                                if (selectedIndex >= 0) listState.animateScrollToItem(selectedIndex)
                            }
                            LazyRow(
                                state = listState,
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(0.dp),
                                horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8)
                            ) {
                                itemsIndexed(filterOptions) { _, optionKey ->
                                    val count = filterCounts[optionKey]
                                    val displayLabel = if (showFilterCounts && count != null) "$optionKey ($count)" else optionKey
                                    FilterChipBox(
                                        label = displayLabel,
                                        selected = optionKey == selectedFilter,
                                        onClick = { onFilterSelect(optionKey) }
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(Dimens.spacing12))
                    if (showSort) {
                        Box(modifier = Modifier.wrapContentWidth()) {
                            Box(
                                modifier = Modifier
                                    .height(Dimens.spacing32)
                                    .clip(RoundedCornerShape(Dimens.radiusPill))
                                    .background(Color.Transparent)
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(Dimens.radiusPill))
                                    .clickable { sortExpanded = true }
                                    .padding(horizontal = Dimens.spacing12),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        sortLabel,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Icon(
                                        Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(Dimens.iconDefault)
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = sortExpanded,
                                onDismissRequest = { sortExpanded = false },
                                modifier = Modifier
                                    .wrapContentWidth(Alignment.End)
                                    .widthIn(min = 140.dp)
                            ) {
                                sortOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            sortExpanded = false
                                            onSortSelect(option)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipBox(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(Dimens.spacing32)
            .clip(RoundedCornerShape(Dimens.radiusPill))
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer
                else Color.Transparent
            )
            .then(
                if (selected) Modifier
                else Modifier.border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(Dimens.radiusPill)
                )
            )
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.spacing12)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
