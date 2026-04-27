package com.example.mamunbingoapp.ui.screens.history

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.ui.components.AppConfirmDialog
import com.example.mamunbingoapp.ui.components.AppHeaderBackground
import com.example.mamunbingoapp.ui.components.AppTopBar
import com.example.mamunbingoapp.ui.screens.ImportTicketMainContent
import com.example.mamunbingoapp.ui.screens.rememberImportTicketGalleryImagePickLauncher
import com.example.mamunbingoapp.viewmodel.ImportTicketViewModel
import com.example.mamunbingoapp.viewmodel.ScanResultUiState
import com.example.mamunbingoapp.viewmodel.finalUiGridRowMajor

/**
 * **Route:** `historyPhotoImport` — **Take photo** = `bingoLiveCameraImport` (live QR + optional GMS) from parent; **Gallery** = picker → uCrop (3:4) → in-app preview → Apply, except a **valid app Bingo QR** on the image auto-commits (no Apply). **Analysis** runs app **QR** before **OCR**; valid ticket QR skips OCR. Save → `manualEntry` when numbers exist (e.g. live-scan prefill).
 */
@Composable
fun HistoryPhotoImportScreen(
    onBackClick: () -> Unit,
    onTakePhotoClick: () -> Unit,
    onClearImageClick: () -> Unit = {},
    onScanAgainClick: () -> Unit = {},
    onRetryAnalysisClick: () -> Unit = {},
    onRegisterLeaveHandler: (((() -> Unit) -> Unit)?) -> Unit = {},
    @Suppress("UNUSED_PARAMETER") handoffLosNumber: String? = null,
    @Suppress("UNUSED_PARAMETER") handoffSerialNumber: String? = null,
    @Suppress("UNUSED_PARAMETER") selectedImageUri: Uri? = null,
    @Suppress("UNUSED_PARAMETER") isAnalyzing: Boolean = false,
    @Suppress("UNUSED_PARAMETER") analysisSummary: String? = null,
    @Suppress("UNUSED_PARAMETER") detectedCount: Int = 0,
    @Suppress("UNUSED_PARAMETER") canContinue: Boolean = false,
    @Suppress("UNUSED_PARAMETER") showIncompleteWarning: Boolean = false,
    @Suppress("UNUSED_PARAMETER") showLowConfidenceWarning: Boolean = false,
    onSaveClick: () -> Unit = {},
    onSaveAndRoomClick: () -> Unit = {},
    suppressHeroImage: Boolean = false,
    modifier: Modifier = Modifier
) {
    val importVm: ImportTicketViewModel = viewModel()
    val context = LocalContext.current
    val selectedUriVm by importVm.selectedImageUri.collectAsState()
    val galleryPendingUri by importVm.galleryPendingEditUri.collectAsState()
    val displayUri = galleryPendingUri ?: selectedUriVm
    val scanResult by importVm.scanResult.collectAsState()
    val isAnalyzingUi = scanResult is ScanResultUiState.Loading
    val successState = scanResult as? ScanResultUiState.Success
    val finalUiGrid = successState?.let { finalUiGridRowMajor(it.numbers) }
    val detectedCountUi = finalUiGrid?.count { it != 0 } ?: 0
    var showDiscardDialog by remember { mutableStateOf(false) }
    var pendingLeaveAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    val isLocked = isAnalyzingUi || (detectedCountUi > 0) || (displayUri != null)
    var showScanTipsDialog by remember { mutableStateOf(false) }

    val pickImageFromGallery = rememberImportTicketGalleryImagePickLauncher { uri ->
        importVm.setGalleryPendingEdit(uri)
    }

    fun handleExitRequest(onExit: () -> Unit) {
        if (galleryPendingUri != null) {
            importVm.cancelGalleryPendingEdit()
            return
        }
        if (isLocked) {
            showDiscardDialog = true
            pendingLeaveAction = onExit
        } else {
            onExit()
        }
    }
    DisposableEffect(Unit) {
        onRegisterLeaveHandler { action -> handleExitRequest(action) }
        onDispose { onRegisterLeaveHandler(null) }
    }
    BackHandler {
        when {
            showDiscardDialog -> showDiscardDialog = false
            galleryPendingUri != null -> importVm.cancelGalleryPendingEdit()
            else -> handleExitRequest(onBackClick)
        }
    }
    AppConfirmDialog(
        visible = showDiscardDialog,
        title = "Discard photo and scan?",
        message = "Leaving will discard the selected image and any scan or import progress and results.",
        cancelText = "Continue Editing",
        confirmText = "Discard",
        onCancel = { showDiscardDialog = false; pendingLeaveAction = null },
        onConfirm = {
            importVm.clear()
            onClearImageClick()
            pendingLeaveAction?.invoke()
            pendingLeaveAction = null
            showDiscardDialog = false
        },
        onDismiss = { showDiscardDialog = false; pendingLeaveAction = null }
    )
    AppConfirmDialog(
        visible = showScanTipsDialog,
        title = stringResource(R.string.import_ticket_scan_tips_title),
        message = "",
        showCancelButton = false,
        confirmText = stringResource(R.string.import_ticket_scan_tips_dialog_ok),
        onConfirm = { showScanTipsDialog = false },
        onCancel = { showScanTipsDialog = false },
        onDismiss = { showScanTipsDialog = false },
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacing8)) {
                val tipIds = listOf(
                    R.string.import_ticket_scan_tip_1,
                    R.string.import_ticket_scan_tip_2,
                    R.string.import_ticket_scan_tip_3,
                )
                tipIds.forEach { resId ->
                    Text(
                        text = "• ${stringResource(resId)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(Dimens.spacing4))
                Text(
                    text = stringResource(R.string.import_ticket_scan_tips_footer),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    )

    val idleHint = stringResource(R.string.import_ticket_idle_hint)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        AppHeaderBackground(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f)
                .align(Alignment.TopCenter)
        )
        Column(Modifier.fillMaxSize()) {
            AppTopBar(
                title = "Import Ticket",
                showBack = true,
                onBackClick = { handleExitRequest(onBackClick) },
                actions = {
                    IconButton(onClick = { showScanTipsDialog = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = stringResource(R.string.import_ticket_scan_tips_title),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.88f),
                        )
                    }
                }
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = Dimens.spacing8)
                        .padding(horizontal = Dimens.screenHorizontalPadding)
                        .padding(bottom = Dimens.spacing8)
                ) {
                    ImportTicketMainContent(
                        selectedUri = displayUri,
                        galleryPendingEditUri = galleryPendingUri,
                        scanResult = scanResult,
                        isAnalyzing = isAnalyzingUi,
                        imageSource = null,
                        idleHint = idleHint,
                        onTakePhoto = onTakePhotoClick,
                        onPickFromGallery = pickImageFromGallery,
                        onSecondaryOutlinedClick = onScanAgainClick,
                        onGalleryApply = { l, t, r, b -> importVm.applyGalleryPendingEdit(context, l, t, r, b) },
                        onGalleryCancel = { importVm.cancelGalleryPendingEdit() },
                        suppressHeroImage = suppressHeroImage,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
