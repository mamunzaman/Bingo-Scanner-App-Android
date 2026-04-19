package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.theme.Dimens

@Composable
fun LiveRoomTopBar(
    title: String,
    onBack: () -> Unit,
    onAddTicket: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenInfo: () -> Unit,
    onResetGame: () -> Unit = {},
    onLeaveRoom: () -> Unit,
    showArchivedBadge: Boolean = false,
    showSheetViewModeMenu: Boolean = false,
    listViewSelected: Boolean = false,
    onSelectCardsView: () -> Unit = {},
    onSelectListView: () -> Unit = {},
) {
    var menuExpanded by rememberSaveable { mutableStateOf(false) }
    val iconColor = MaterialTheme.colorScheme.onSurface
    AppTopBar(
        title = title,
        showBack = true,
        onBackClick = onBack,
        titleContent = {
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.semantics { heading() }
                )
                if (showArchivedBadge) {
                    Row(
                        modifier = Modifier.padding(start = Dimens.spacing8),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = Dimens.spacing8, vertical = Dimens.spacing4),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Outlined.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(Dimens.iconCompact),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                androidx.compose.foundation.layout.Spacer(Modifier.width(Dimens.spacing4))
                                Text(
                                    "Archived",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        actions = {
            IconButton(
                onClick = onAddTicket,
                modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add ticket",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                ) {
                    Icon(
                        Icons.Outlined.MoreVert,
                        contentDescription = "More options",
                        modifier = Modifier.size(Dimens.iconDefault),
                        tint = iconColor
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    if (showSheetViewModeMenu) {
                        DropdownMenuItem(
                            text = { Text("Cards view") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.GridView,
                                    contentDescription = null,
                                    tint = if (!listViewSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onSelectCardsView()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("List view") },
                            leadingIcon = {
                                Icon(
                                    Icons.AutoMirrored.Filled.List,
                                    contentDescription = null,
                                    tint = if (listViewSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onSelectListView()
                            }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                    DropdownMenuItem(
                        text = { Text("Info") },
                        leadingIcon = {
                            Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                        },
                        onClick = { menuExpanded = false; onOpenInfo() }
                    )
                    DropdownMenuItem(
                        text = { Text("Settings") },
                        leadingIcon = {
                            Icon(Icons.Outlined.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                        },
                        onClick = { menuExpanded = false; onOpenSettings() }
                    )
                    DropdownMenuItem(
                        text = { Text("Reset game") },
                        leadingIcon = {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                        },
                        onClick = { menuExpanded = false; onResetGame() }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    DropdownMenuItem(
                        text = { Text("Leave Room", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = {
                            Icon(Icons.AutoMirrored.Outlined.ExitToApp, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        },
                        onClick = { menuExpanded = false; onLeaveRoom() }
                    )
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun LiveRoomTopBarPreview() {
    com.example.mamunbingoapp.theme.MamunBingoTheme {
        LiveRoomTopBar(
            title = "Friday Night Bingo",
            onBack = {},
            onAddTicket = {},
            onOpenSettings = {},
            onOpenInfo = {},
            onResetGame = {},
            onLeaveRoom = {}
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LiveRoomTopBarDarkPreview() {
    com.example.mamunbingoapp.theme.MamunBingoTheme(darkTheme = true) {
        LiveRoomTopBar(
            title = "Friday\nNight Bingo",
            onBack = {},
            onAddTicket = {},
            onOpenSettings = {},
            onOpenInfo = {},
            onResetGame = {},
            onLeaveRoom = {}
        )
    }
}
