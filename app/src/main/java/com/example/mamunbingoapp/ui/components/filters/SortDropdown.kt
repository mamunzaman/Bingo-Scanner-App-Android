package com.example.mamunbingoapp.ui.components.filters

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.mamunbingoapp.theme.Dimens

@Composable
fun SortDropdown(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean? = null,
    onDismissRequest: (() -> Unit)? = null,
    onExpandRequested: (() -> Unit)? = null
) {
    var internalExpanded by remember { mutableStateOf(false) }
    val isControlled = expanded != null && onDismissRequest != null
    val expandedValue = if (isControlled) expanded!! else internalExpanded
    val onDismiss: () -> Unit = if (isControlled) onDismissRequest!! else { { internalExpanded = false } }
    val colorScheme = MaterialTheme.colorScheme

    Box(modifier = modifier) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = colorScheme.onSurfaceVariant) },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = Dimens.buttonHeight)
                .clickable {
                    if (isControlled) onExpandRequested?.invoke() else internalExpanded = true
                },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colorScheme.surfaceContainer,
                unfocusedContainerColor = colorScheme.surfaceContainer,
                focusedBorderColor = colorScheme.primary,
                unfocusedBorderColor = colorScheme.outlineVariant,
                focusedTextColor = colorScheme.onSurface,
                unfocusedTextColor = colorScheme.onSurface
            )
        )
        DropdownMenu(
            expanded = expandedValue,
            onDismissRequest = onDismiss
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        onDismiss()
                    }
                )
            }
        }
    }
}
