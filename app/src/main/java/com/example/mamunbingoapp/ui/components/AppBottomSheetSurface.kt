package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.unit.Dp

/**
 * Default [ModalBottomSheet] chrome for app sheets: [rememberModalBottomSheetState], drag handle,
 * [surfaceContainer] tone. Pass [windowInsets] / [shape] to match legacy sheets during migration.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberAppBottomSheetState(skipPartiallyExpanded: Boolean = true): SheetState =
    rememberModalBottomSheetState(skipPartiallyExpanded = skipPartiallyExpanded)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBottomSheetSurface(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberAppBottomSheetState(),
    shape: Shape = BottomSheetDefaults.ExpandedShape,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
    windowInsets: WindowInsets = BottomSheetDefaults.windowInsets,
    sheetMaxWidth: Dp = BottomSheetDefaults.SheetMaxWidth,
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier,
        shape = shape,
        containerColor = containerColor,
        dragHandle = dragHandle,
        windowInsets = windowInsets,
        sheetMaxWidth = sheetMaxWidth,
        content = content,
    )
}
