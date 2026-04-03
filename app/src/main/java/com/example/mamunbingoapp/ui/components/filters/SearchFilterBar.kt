package com.example.mamunbingoapp.ui.components.filters

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mamunbingoapp.theme.Dimens

@Composable
fun SearchFilterBar(
    query: String,
    onQueryChange: (String) -> Unit,
    filter: FilterState,
    onFilterSelect: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    sortDropdown: (@Composable () -> Unit)? = null
) {
    val colorScheme = MaterialTheme.colorScheme
    val searchShape = RoundedCornerShape(Dimens.radiusSearchField)
    val chipShape = RoundedCornerShape(Dimens.radiusPill)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacing12)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.textFieldHeight),
            placeholder = { Text(placeholder, color = colorScheme.onSurfaceVariant) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = colorScheme.onSurfaceVariant
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colorScheme.surfaceContainer,
                unfocusedContainerColor = colorScheme.surfaceContainer,
                focusedBorderColor = colorScheme.primary,
                unfocusedBorderColor = colorScheme.outlineVariant,
                focusedTextColor = colorScheme.onSurface,
                unfocusedTextColor = colorScheme.onSurface,
                cursorColor = colorScheme.primary
            ),
            shape = searchShape
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing12)
        ) {
            LazyRow(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = Dimens.buttonHeight),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8),
                contentPadding = PaddingValues(0.dp)
            ) {
                items(filter.options, key = { it }) { option ->
                    val selected = option == filter.selected
                    Row(
                        modifier = Modifier
                            .clip(chipShape)
                            .background(
                                if (selected) colorScheme.primaryContainer
                                else colorScheme.surface
                            )
                            .border(
                                1.5.dp,
                                if (selected) colorScheme.primary else colorScheme.outlineVariant,
                                chipShape
                            )
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = { onFilterSelect(option) }
                            )
                            .padding(horizontal = Dimens.spacing16, vertical = Dimens.spacing8),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = if (selected) colorScheme.primary else colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            if (sortDropdown != null) sortDropdown()
        }
    }
}
