package com.example.mamunbingoapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.example.mamunbingoapp.ui.components.iosElevatedShadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.viewmodel.JackpotViewModel
import com.example.mamunbingoapp.theme.AppTextStyles
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.ui.components.AppBottomBar
import com.example.mamunbingoapp.ui.components.AppHeaderPageLayout
import com.example.mamunbingoapp.ui.components.AppIconContainer
import com.example.mamunbingoapp.ui.components.AppTopBar
import com.example.mamunbingoapp.ui.components.AppTab
import com.example.mamunbingoapp.domain.model.BingoScanType
import com.example.mamunbingoapp.ui.screens.scan.ScanTypeSelectionSheet

@Composable
fun JackpotScreen(
    onTabSelected: (AppTab) -> Unit,
    onStartResumeLive: () -> Unit,
    onLaunchCamera: (BingoScanType) -> Unit = {},
    onManualEntry: () -> Unit,
    onHistory: () -> Unit,
    onGoLivePlay: () -> Unit,
    viewModel: JackpotViewModel = viewModel(),
    showBottomBar: Boolean = true
) {
    val sheetCount by viewModel.sheetCount.collectAsState()
    val calledCount by viewModel.calledCount.collectAsState()
    var showScanTypeSheet by remember { mutableStateOf(false) }

    AppHeaderPageLayout(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.live_nav_title)
            )
        },
        content = {
            if (sheetCount == 0) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(Dimens.screenHorizontalPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.live_play_empty_sheets_title),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = Dimens.screenHorizontalPadding)
                        .padding(top = 24.dp, bottom = Dimens.spacing16)
                ) {
                    StartResumeCard(
                        sheetCount = sheetCount,
                        calledCount = calledCount,
                        onClick = onStartResumeLive
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    LiveNavActionGrid(
                        onScanSheet = { showScanTypeSheet = true },
                        onManualEntry = onManualEntry,
                        onHistory = onHistory,
                        onGoLivePlay = onGoLivePlay
                    )
                }
            }
            if (showBottomBar) {
                AppBottomBar(selectedTab = AppTab.Jackpot, onTabSelected = onTabSelected)
            }
        }
    )
    if (showScanTypeSheet) {
        ScanTypeSelectionSheet(
            onDismiss = { showScanTypeSheet = false },
            onScanTypeSelected = { type ->
                showScanTypeSheet = false
                onLaunchCamera(type)
            },
        )
    }
}

@Composable
private fun StartResumeCard(
    sheetCount: Int,
    calledCount: Int,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(Dimens.radiusCard)
    val todayLabel = stringResource(R.string.common_today)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .iosElevatedShadow(shape = shape)
            .background(MaterialTheme.colorScheme.primaryContainer, shape)
            .clickable(onClick = onClick)
            .padding(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.live_nav_start_resume),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.live_nav_start_resume_subtitle, todayLabel, sheetCount, calledCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun LiveNavActionGrid(
    onScanSheet: () -> Unit,
    onManualEntry: () -> Unit,
    onHistory: () -> Unit,
    onGoLivePlay: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacing16)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing16)
        ) {
            LiveNavActionCard(
                title = stringResource(R.string.live_nav_scan_sheet),
                subtitle = stringResource(R.string.live_nav_scan_sheet_subtitle),
                icon = Icons.Default.QrCodeScanner,
                modifier = Modifier.weight(1f),
                onClick = onScanSheet
            )
            LiveNavActionCard(
                title = stringResource(R.string.live_nav_manual_entry),
                subtitle = stringResource(R.string.live_nav_manual_entry_subtitle),
                icon = Icons.Default.Edit,
                modifier = Modifier.weight(1f),
                onClick = onManualEntry
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing16)
        ) {
            LiveNavActionCard(
                title = stringResource(R.string.live_nav_history),
                subtitle = stringResource(R.string.live_nav_history_subtitle),
                icon = Icons.Default.History,
                modifier = Modifier.weight(1f),
                onClick = onHistory
            )
            LiveNavActionCard(
                title = stringResource(R.string.live_nav_go_live),
                subtitle = stringResource(R.string.live_nav_go_live_subtitle),
                icon = Icons.Default.PlayCircle,
                modifier = Modifier.weight(1f),
                onClick = onGoLivePlay
            )
        }
    }
}

@Composable
private fun LiveNavActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(Dimens.radiusCard)
    Column(
        modifier = modifier
            .height(155.dp)
            .iosElevatedShadow(shape = shape)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AppIconContainer(icon = icon, size = 40.dp, iconSize = 24.dp)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = AppTextStyles.actionCardTitle,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = AppTextStyles.actionCardSubtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
