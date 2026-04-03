package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mamunbingoapp.theme.Dimens

private val undoWidth = 56.dp
private val callWidth = 108.dp

@Composable
fun LiveCallInputBar(
    progressText: String,
    enterLabel: String,
    enterRange: String,
    modifier: Modifier = Modifier,
    inputText: String = "",
    onInputChange: (String) -> Unit = {},
    canAddNumber: Boolean = true,
    actionInProgress: Boolean = false,
    showScrimBehind: Boolean = false,
    onDiceClick: () -> Unit = {},
    onCallClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    focusRequester: FocusRequester? = null
) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(
        topStart = Dimens.radiusSmall,
        topEnd = Dimens.radiusSmall,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )
    val keyboardController = LocalSoftwareKeyboardController.current
    var fieldValue by remember(inputText) { mutableStateOf(TextFieldValue(inputText, TextRange(inputText.length))) }
    LaunchedEffect(inputText) {
        if (fieldValue.text != inputText) fieldValue = TextFieldValue(inputText, TextRange(inputText.length))
    }
    val parsedNumber = fieldValue.text.trim().toIntOrNull()
    val isValidNumber = parsedNumber != null && parsedNumber in 1..75
    val actionsEnabled = !actionInProgress

    Row(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .height(Dimens.inputBarHeight)
            .clip(shape)
            .background(cs.surface)
            .border(1.dp, cs.outlineVariant.copy(alpha = 0.6f), shape),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(horizontal = Dimens.spacing12, vertical = Dimens.spacing4),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8)
        ) {
            Column(
                modifier = Modifier.wrapContentHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = progressText.replace(" ", ""),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = cs.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "called",
                    style = MaterialTheme.typography.labelSmall,
                    color = cs.onSurfaceVariant.copy(alpha = 0.9f),
                    maxLines = 1
                )
            }
            VerticalDivider(
                modifier = Modifier.height(Dimens.iconAlertBox),
                thickness = 1.dp,
                color = cs.outlineVariant.copy(alpha = 0.5f)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .then(
                        if (focusRequester != null) Modifier.clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { focusRequester.requestFocus() }
                        else Modifier
                    ),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(min = 72.dp, max = 96.dp)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        BasicTextField(
                            value = fieldValue,
                            onValueChange = { newValue ->
                                val filtered = newValue.text.filter { c -> c.isDigit() }.take(2)
                                fieldValue = TextFieldValue(filtered, TextRange(filtered.length))
                                if (filtered != inputText) onInputChange(filtered)
                            },
                            enabled = canAddNumber && actionsEnabled,
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
                                .onFocusChanged {
                                    if (it.isFocused) {
                                        keyboardController?.show()
                                        if (fieldValue.text.isNotEmpty()) {
                                            fieldValue = fieldValue.copy(selection = TextRange(0, fieldValue.text.length))
                                        }
                                    }
                                },
                            textStyle = TextStyle(
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = cs.primary,
                                textAlign = TextAlign.Center
                            ),
                            maxLines = 1,
                            singleLine = true,
                            cursorBrush = SolidColor(cs.primary),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            decorationBox = { inner ->
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (fieldValue.text.isEmpty()) {
                                        Text(
                                            "1–75",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = cs.onSurfaceVariant
                                            ),
                                            maxLines = 1
                                        )
                                    }
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        inner()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        VerticalDivider(
            modifier = Modifier.height(Dimens.inputBarHeight),
            thickness = 1.dp,
            color = cs.outlineVariant.copy(alpha = 0.5f)
        )

        Box(
            modifier = Modifier
                .width(undoWidth)
                .fillMaxHeight()
                .semantics { role = Role.Button }
                .background(cs.surface)
                .clickable(
                    enabled = actionsEnabled,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    if (!actionsEnabled) return@clickable
                    onBackClick()
                    focusRequester?.requestFocus()
                    keyboardController?.show()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Undo,
                contentDescription = "Undo last call",
                modifier = Modifier.size(Dimens.iconDefault),
                tint = cs.primary
            )
        }

        VerticalDivider(
            modifier = Modifier.height(Dimens.inputBarHeight),
            thickness = 1.dp,
            color = cs.outlineVariant.copy(alpha = 0.5f)
        )

        Box(
            modifier = Modifier
                .width(callWidth)
                .fillMaxHeight()
                .semantics { role = Role.Button }
                .background(cs.primary)
                .clickable(
                    enabled = canAddNumber && isValidNumber && actionsEnabled,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    if (!actionsEnabled) return@clickable
                    if (isValidNumber) onCallClick()
                    focusRequester?.requestFocus()
                    keyboardController?.show()
                }
                .padding(horizontal = Dimens.spacing8),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardReturn,
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.iconCompact),
                    tint = cs.onPrimary
                )
                Text(
                    text = "Call",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = cs.onPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
