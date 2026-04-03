package com.example.mamunbingoapp.ui.screens.manual

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.lerp
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import kotlin.math.roundToInt
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mamunbingoapp.BuildConfig
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.OnPrimary
import com.example.mamunbingoapp.theme.OnSurface
import com.example.mamunbingoapp.theme.Outline
import com.example.mamunbingoapp.theme.Primary
import com.example.mamunbingoapp.ui.components.AppConfirmDialog
import com.example.mamunbingoapp.ui.components.BingoCardGrid
import com.example.mamunbingoapp.ui.components.BingoGridMode
import com.example.mamunbingoapp.ui.components.RoomConflictDialog
import com.example.mamunbingoapp.ui.components.AppBottomBar
import com.example.mamunbingoapp.ui.components.AppHeaderBackground
import com.example.mamunbingoapp.ui.components.AppTab
import com.example.mamunbingoapp.ui.components.AppTopBar
import com.example.mamunbingoapp.data.LiveRoom
import com.example.mamunbingoapp.viewmodel.ManualEntryUiAction
import com.example.mamunbingoapp.viewmodel.ManualEntryUiEvent
import com.example.mamunbingoapp.ui.model.BingoCellUi
import com.example.mamunbingoapp.viewmodel.ManualEntryViewModel
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.PlatformTextStyle
import android.util.Log
import androidx.activity.compose.BackHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val MANUAL_ENTRY_TAG = "ManualEntry"

private fun manualEntryKeypadLayoutHeightsPx(
    innerMaxHeightPx: Int,
    fullDockPx: Int,
    motionProgress: Float
): Pair<Int, Int> {
    val dockPadPx = (fullDockPx.toFloat() * motionProgress).roundToInt()
    val maxHPxClosed = innerMaxHeightPx + dockPadPx
    val maxHPxWhenKeypadOpen = (maxHPxClosed - fullDockPx).coerceAtLeast(1)
    return maxHPxClosed to maxHPxWhenKeypadOpen
}

private fun List<String>.draftListPaddedTo25(): List<String> =
    if (size >= 25) take(25) else this + List(25 - size) { "" }

private fun manualEntryKeypadTargetScale(cardHeightPx: Int, availHeightPx: Int): Float {
    val cardH = cardHeightPx.coerceAtLeast(1)
    val availH = availHeightPx.coerceAtLeast(1)
    val computedScale = (availH.toFloat() / cardH.toFloat()).coerceAtMost(1f)
    val minScale = 0.92f
    val floored = maxOf(computedScale, minScale)
    return if (cardH.toFloat() * floored <= availH.toFloat()) floored else computedScale
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualEntryScreen(
    onBack: () -> Unit,
    onNavigateToLivePlay: (roomId: String) -> Unit,
    onSaveOnlySuccess: (ticketId: String, roomId: String?) -> Unit = { _, _ -> },
    onTabSelected: (AppTab) -> Unit = {},
    scannedNumbers: List<Int> = emptyList(),
    prefillAsRowMajor: Boolean = false,
    ocrSourceLabel: String? = null,
    losNumber: String? = null,
    serialNumber: String? = null,
    viewModel: ManualEntryViewModel = viewModel()
) {
    Log.d(MANUAL_ENTRY_TAG, "ManualEntryScreen composed, scannedNumbers.size=${scannedNumbers.size}")
    if (scannedNumbers.isNotEmpty()) {
        Log.d(MANUAL_ENTRY_TAG, "Manual Entry open: source=$ocrSourceLabel, total count=${scannedNumbers.size}, first 10 values=${scannedNumbers.take(10)}")
    }
    val state = viewModel.state.collectAsState().value
    val rooms = viewModel.rooms.collectAsState().value
    var losNummerDraft by rememberSaveable(losNumber) { mutableStateOf(losNumber.orEmpty()) }
    var serialNummerDraft by rememberSaveable(serialNumber) { mutableStateOf(serialNumber.orEmpty()) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var pendingNavigateAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var draftPerCell by remember { mutableStateOf(List(25) { "" }) }
    var manualEntrySelectionBaseline by remember {
        mutableStateOf<Pair<Int, Int?>>(-1 to null)
    }
    val hasEnteredValues =
        state.cells.any { (it.number ?: "").isNotBlank() } ||
            draftPerCell.any { it.isNotBlank() } ||
            losNummerDraft.isNotBlank() ||
            serialNummerDraft.isNotBlank()
    BackHandler(enabled = hasEnteredValues) {
        Log.d(MANUAL_ENTRY_TAG, "discard dialog shown")
        showDiscardDialog = true
        pendingNavigateAction = { onBack() }
    }
    var hasPrefilledScannedNumbers by rememberSaveable { mutableStateOf(false) }
    var isApplyingScannedPrefill by rememberSaveable { mutableStateOf(false) }
    var infoDialog by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isEditingSheetName by remember { mutableStateOf(false) }
    val sheetTitleFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    var sheetTitleBlurJob by remember { mutableStateOf<Job?>(null) }
    var sheetTitleHadFocusWhileEditing by remember { mutableStateOf(false) }
    val isPartialScan = scannedNumbers.isNotEmpty() && (scannedNumbers.size < 25 || scannedNumbers.any { it == 0 })
    var hasShownPartialInfoDialog by rememberSaveable { mutableStateOf(false) }
    var allowNavigationByEvent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(300)
        allowNavigationByEvent = true
        Log.d(MANUAL_ENTRY_TAG, "allowNavigationByEvent set true")
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                ManualEntryUiEvent.NavigateBack -> {
                    if (!allowNavigationByEvent) return@collect
                    val hasValues = viewModel.state.value.cells.any { (it.number ?: "").isNotBlank() }
                    if (hasValues) {
                        Log.d(MANUAL_ENTRY_TAG, "discard dialog shown")
                        showDiscardDialog = true
                        pendingNavigateAction = { onBack() }
                    } else {
                        onBack()
                    }
                }
                is ManualEntryUiEvent.NavigateToLivePlay -> {
                    Log.d(MANUAL_ENTRY_TAG, "navigate to history/livePlay triggered, roomId=${event.roomId}")
                    if (allowNavigationByEvent) onNavigateToLivePlay(event.roomId)
                }
                is ManualEntryUiEvent.SaveOnlyCompleted -> {
                    Log.d(MANUAL_ENTRY_TAG, "saveOnlyCompleted triggered, ticketId=${event.ticketId}")
                    if (allowNavigationByEvent) onSaveOnlySuccess(event.ticketId, event.roomId)
                }
                is ManualEntryUiEvent.ShowSnackbar -> { }
                is ManualEntryUiEvent.ShowInfoDialog -> infoDialog = event.title to event.message
            }
        }
    }

    LaunchedEffect(isPartialScan, hasPrefilledScannedNumbers, hasShownPartialInfoDialog) {
        if (isPartialScan && hasPrefilledScannedNumbers && !hasShownPartialInfoDialog) {
            hasShownPartialInfoDialog = true
            infoDialog = "Some boxes not read" to "Some boxes were not added. Please confirm the blank boxes manually."
        }
    }
    infoDialog?.let { (title, message) ->
        AppConfirmDialog(
            visible = true,
            title = title,
            message = message,
            confirmText = "OK",
            showCancelButton = false,
            onConfirm = { infoDialog = null },
            onCancel = { infoDialog = null },
            onDismiss = { infoDialog = null }
        )
    }

    if (showDiscardDialog) {
        AppConfirmDialog(
            visible = true,
            title = "Discard manual entry?",
            message = "Leaving will discard the entered bingo numbers. They will be lost.",
            confirmText = "Discard",
            cancelText = "Keep editing",
            showCancelButton = true,
            onConfirm = {
                Log.d(MANUAL_ENTRY_TAG, "discard confirmed")
                pendingNavigateAction?.invoke()
                pendingNavigateAction = null
                showDiscardDialog = false
            },
            onCancel = {
                Log.d(MANUAL_ENTRY_TAG, "discard canceled")
                showDiscardDialog = false
                pendingNavigateAction = null
            },
            onDismiss = {
                showDiscardDialog = false
                pendingNavigateAction = null
            }
        )
    }

    RoomConflictDialog(
        visible = state.roomConflict.visible,
        existingRoomName = state.roomConflict.existingRoomName ?: "another room",
        hasTargetRoom = state.roomConflict.targetRoomId != null,
        onCancel = { viewModel.dismissConflict() },
        onOpenExistingRoom = { viewModel.openExistingRoom() },
        onMoveToTargetRoom = { viewModel.moveToTargetRoom() }
    )

    if (state.isRoomPickerOpen) {
        RoomPickerBottomSheet(
            rooms = rooms,
            onRoomSelected = { roomId -> viewModel.onAction(ManualEntryUiAction.RoomSelected(roomId)) },
            onDismiss = { viewModel.onAction(ManualEntryUiAction.DismissRoomPicker) }
        )
    }

    LaunchedEffect(viewModel) {
        viewModel.onAction(ManualEntryUiAction.CellSelected(-1))
    }

    LaunchedEffect(state.selectedIndex) {
        val i = state.selectedIndex
        if (i in 0..24) {
            focusManager.clearFocus()
            val committed = state.cells.getOrNull(i)?.number?.orEmpty() ?: ""
            manualEntrySelectionBaseline =
                i to committed.takeIf { it.isNotBlank() }?.toIntOrNull()
            draftPerCell =
                draftPerCell.draftListPaddedTo25().toMutableList().apply { set(i, committed) }
        } else {
            manualEntrySelectionBaseline = -1 to null
        }
    }

    LaunchedEffect(isEditingSheetName) {
        sheetTitleBlurJob?.cancel()
        sheetTitleBlurJob = null
        if (isEditingSheetName) {
            sheetTitleHadFocusWhileEditing = false
            val prev = state.selectedIndex
            if (prev in 0..24) {
                val d = draftPerCell.getOrNull(prev).orEmpty()
                manualEntryApplyLeaveCellDraft(
                    cellIndex = prev,
                    draft = d,
                    baselinePair = manualEntrySelectionBaseline,
                    viewModel = viewModel,
                    onDraftCleared = { clearedIdx ->
                        draftPerCell =
                            draftPerCell.draftListPaddedTo25().toMutableList()
                                .apply { set(clearedIdx, "") }
                    }
                )
            }
            viewModel.onAction(ManualEntryUiAction.CellSelected(-1))
            delay(80)
            runCatching { sheetTitleFocusRequester.requestFocus() }
        }
    }

    LaunchedEffect(scannedNumbers) {
        if (!hasPrefilledScannedNumbers && scannedNumbers.isNotEmpty()) {
            Log.d(MANUAL_ENTRY_TAG, "prefill input count=${scannedNumbers.size}, prefillAsRowMajor=$prefillAsRowMajor")
            Log.d(MANUAL_ENTRY_TAG, "scannedNumbers raw list (first 8)=${scannedNumbers.take(8)}")
            hasPrefilledScannedNumbers = true
            isApplyingScannedPrefill = true
            val padded = if (scannedNumbers.size < 25) scannedNumbers + List(25 - scannedNumbers.size) { 0 } else scannedNumbers
            val valuesToFill = if (prefillAsRowMajor) {
                Log.d(MANUAL_ENTRY_TAG, "prefill using OCR order as-is (row-major)")
                padded
            } else {
                val rowMajor = storedColumnOrderToRowMajor(padded)
                Log.d(MANUAL_ENTRY_TAG, "mapped row-major list (first 8)=${rowMajor.take(8)}")
                rowMajor
            }
            for (i in 0..24) {
                viewModel.onAction(ManualEntryUiAction.CellSelected(i))
                val v = valuesToFill.getOrNull(i) ?: 0
                if (v != 0) viewModel.onAction(ManualEntryUiAction.NumberPressed(v))
            }
            viewModel.onAction(ManualEntryUiAction.CellSelected(-1))
        }
    }

    LaunchedEffect(state.isComplete, isApplyingScannedPrefill) {
        if (isApplyingScannedPrefill && state.isComplete) {
            draftPerCell = List(25) { "" }
            isApplyingScannedPrefill = false
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent,
        topBar = { },
        bottomBar = {
            AppBottomBar(
                selectedTab = AppTab.Scan,
                onTabSelected = { tab ->
                    if (hasEnteredValues) {
                        Log.d(MANUAL_ENTRY_TAG, "discard dialog shown")
                        showDiscardDialog = true
                        pendingNavigateAction = { onBack(); onTabSelected(tab) }
                    } else {
                        onTabSelected(tab)
                    }
                }
            )
        }
    ) { paddingValues ->
        val bottomInsetDp = paddingValues.calculateBottomPadding()
        val keypadVisible = state.selectedIndex in 0..24
        var lastKeypadIndex by remember { mutableStateOf(0) }
        var hasEntered by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            hasEntered = true
        }
        LaunchedEffect(state.selectedIndex) {
            if (state.selectedIndex in 0..24) lastKeypadIndex = state.selectedIndex
        }
        val keypadKeyHeight = Dimens.spacing32 + Dimens.spacing12
        val keypadTopRowH =
            maxOf(Dimens.inputBarHeight, Dimens.buttonHeight)
        val keypadDockEstimateDp = Dimens.spacing12 + Dimens.spacing16 + keypadTopRowH +
            Dimens.spacing12 + keypadKeyHeight + Dimens.spacing8 + keypadKeyHeight

        val keypadMotionMs = 160
        val keypadSharedTweenFloat = tween<Float>(
            durationMillis = keypadMotionMs,
            easing = FastOutSlowInEasing
        )
        val keyboardTransition = updateTransition(
            targetState = keypadVisible,
            label = "manualEntryKeyboardTransition"
        )
        val keyboardOpenProgress by keyboardTransition.animateFloat(
            transitionSpec = { keypadSharedTweenFloat },
            label = "keyboardOpenProgress"
        ) { open -> if (open) 1f else 0f }
        val motionProgress = if (hasEntered) {
            keyboardOpenProgress
        } else {
            if (keypadVisible) 1f else 0f
        }
        val animatedKeypadDockDp = keypadDockEstimateDp * motionProgress
        val showKeypadSurface = keypadVisible || motionProgress > 0f
        val keyboardUiActive = showKeypadSurface
        val keypadTotalHeight = animatedKeypadDockDp + bottomInsetDp
        val dismissSheetTitleEdit: () -> Unit = {
            sheetTitleBlurJob?.cancel()
            sheetTitleBlurJob = null
            sheetTitleHadFocusWhileEditing = false
            focusManager.clearFocus()
            isEditingSheetName = false
        }
        val flushManualEntryLeaveDraft: () -> Unit = {
            val prev = state.selectedIndex
            if (prev in 0..24) {
                val d = draftPerCell.getOrNull(prev).orEmpty()
                manualEntryApplyLeaveCellDraft(
                    cellIndex = prev,
                    draft = d,
                    baselinePair = manualEntrySelectionBaseline,
                    viewModel = viewModel,
                    onDraftCleared = { clearedIdx ->
                        draftPerCell =
                            draftPerCell.draftListPaddedTo25().toMutableList()
                                .apply { set(clearedIdx, "") }
                    }
                )
            }
        }

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                AppHeaderBackground(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.4f)
                        .align(Alignment.TopCenter)
                )
                val keypadDockForLayout = animatedKeypadDockDp
                val bingoKeypadBreathingGap =
                    if (keypadVisible) Dimens.screenHorizontalPadding else 0.dp
                val sheetTitleStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                )
                val gridScrollState = rememberScrollState()
                val outsideDismissFlexInteraction = remember { MutableInteractionSource() }
                val sheetTitleFlexOutsideInteraction = remember { MutableInteractionSource() }
                val outsideDismissFlexModifier = when {
                    keyboardUiActive -> Modifier.clickable(
                        indication = null,
                        interactionSource = outsideDismissFlexInteraction
                    ) {
                        if (isEditingSheetName) dismissSheetTitleEdit()
                        flushManualEntryLeaveDraft()
                        viewModel.onAction(ManualEntryUiAction.CellSelected(-1))
                    }
                    isEditingSheetName -> Modifier.clickable(
                        indication = null,
                        interactionSource = sheetTitleFlexOutsideInteraction
                    ) {
                        dismissSheetTitleEdit()
                    }
                    else -> Modifier
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    val saveAndPlayPress = rememberTopBarSaveIconPress(
                        "meSaveAndPlayPressScale",
                        "meSaveAndPlayPressAlpha"
                    )
                    val saveOnlyPress = rememberTopBarSaveIconPress(
                        "meSaveOnlyPressScale",
                        "meSaveOnlyPressAlpha"
                    )
                    AppTopBar(
                        title = "Manual Entry",
                        modifier = Modifier.fillMaxWidth(),
                        showBack = true,
                        onBackClick = {
                            dismissSheetTitleEdit()
                            if (hasEnteredValues) {
                                showDiscardDialog = true
                                pendingNavigateAction = { onBack() }
                            } else {
                                onBack()
                            }
                        },
                        actions = {
                            Box(
                                modifier = Modifier
                                    .sizeIn(
                                        minWidth = 48.dp,
                                        minHeight = 48.dp
                                    )
                                    .clickable(
                                        enabled = state.isComplete,
                                        interactionSource = saveAndPlayPress.interaction,
                                        indication = null,
                                        role = Role.Button,
                                        onClick = {
                                            dismissSheetTitleEdit()
                                            viewModel.onAction(
                                                ManualEntryUiAction.SaveAndPlayClicked(
                                                    losNummerDraft,
                                                    serialNummerDraft
                                                )
                                            )
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier.graphicsLayer {
                                        scaleX = saveAndPlayPress.scale
                                        scaleY = saveAndPlayPress.scale
                                        alpha = saveAndPlayPress.alpha
                                        transformOrigin = TransformOrigin(0.5f, 0.5f)
                                    },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = "Save and add to live play",
                                        modifier = Modifier.size(Dimens.iconDefault),
                                        tint = if (state.isComplete) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface.copy(
                                                alpha = 0.38f
                                            )
                                        }
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .sizeIn(
                                        minWidth = 48.dp,
                                        minHeight = 48.dp
                                    )
                                    .clickable(
                                        enabled = state.isComplete,
                                        interactionSource = saveOnlyPress.interaction,
                                        indication = null,
                                        role = Role.Button,
                                        onClick = {
                                            dismissSheetTitleEdit()
                                            viewModel.onAction(
                                                ManualEntryUiAction.SaveOnlyClicked(
                                                    losNummerDraft,
                                                    serialNummerDraft
                                                )
                                            )
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier.graphicsLayer {
                                        scaleX = saveOnlyPress.scale
                                        scaleY = saveOnlyPress.scale
                                        alpha = saveOnlyPress.alpha
                                        transformOrigin = TransformOrigin(0.5f, 0.5f)
                                    },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Save,
                                        contentDescription = "Save ticket sheet only",
                                        modifier = Modifier.size(Dimens.iconDefault),
                                        tint = if (state.isComplete) {
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                alpha = 0.88f
                                            )
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                alpha = 0.42f
                                            )
                                        }
                                    )
                                }
                            }
                            ManualEntryDebugAutoFillTopBarAction(
                                onAutoFill = {
                                    dismissSheetTitleEdit()
                                    viewModel.onAction(
                                        ManualEntryUiAction.AutoFillClicked
                                    )
                                }
                            )
                        }
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(bottom = keypadDockForLayout + bingoKeypadBreathingGap)
                            .then(outsideDismissFlexModifier)
                    ) {
                        BoxWithConstraints(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = Dimens.screenHorizontalPadding)
                        ) {
                                var cardHeightPx by remember { mutableIntStateOf(0) }
                                val density = LocalDensity.current
                                val fullDockPx = with(density) { keypadDockEstimateDp.roundToPx() }
                                val maxHPxInner = with(density) { maxHeight.roundToPx() }
                                val (_, maxHPxWhenKeypadOpenForHeader) =
                                    manualEntryKeypadLayoutHeightsPx(
                                        maxHPxInner,
                                        fullDockPx,
                                        motionProgress
                                    )
                                val targetScaleOpenForHeader = when {
                                    cardHeightPx <= 0 -> 1f
                                    cardHeightPx <= maxHPxWhenKeypadOpenForHeader -> 1f
                                    else -> manualEntryKeypadTargetScale(
                                        cardHeightPx,
                                        maxHPxWhenKeypadOpenForHeader
                                    )
                                }
                                val visualScaleApprox =
                                    lerp(1f, targetScaleOpenForHeader, motionProgress)
                                val dateText = SimpleDateFormat(
                                    "MMM d, yyyy",
                                    Locale.getDefault()
                                ).format(Date(state.playedAtMillis))
                                val cardTopPadding = Dimens.spacing4 * motionProgress
                                val bingoCard: @Composable () -> Unit = @Composable {
                                    Box(
                                        modifier = Modifier.onSizeChanged { sz ->
                                            cardHeightPx = sz.height
                                        }
                                    ) {
                                    ManualEntryBingoCard(
                                        modifier = Modifier.padding(top = cardTopPadding),
                                        compactGreenHeader = visualScaleApprox < 1f,
                                        sheetName = state.sheetName,
                                        dateText = dateText,
                                        isEditingSheetName = isEditingSheetName,
                                        sheetTitleStyle = sheetTitleStyle,
                                        sheetTitleFocusRequester = sheetTitleFocusRequester,
                                        onSheetNameChange = { name ->
                                            viewModel.onAction(
                                                ManualEntryUiAction.SheetNameChanged(name)
                                            )
                                        },
                                        onSheetTitleFocusChanged = { fc ->
                                            if (fc.isFocused) {
                                                sheetTitleBlurJob?.cancel()
                                                sheetTitleHadFocusWhileEditing = true
                                            } else if (
                                                isEditingSheetName &&
                                                sheetTitleHadFocusWhileEditing
                                            ) {
                                                sheetTitleBlurJob?.cancel()
                                                sheetTitleBlurJob = coroutineScope.launch {
                                                    delay(180)
                                                    isEditingSheetName = false
                                                }
                                            }
                                        },
                                        onSheetNameDone = {
                                            sheetTitleBlurJob?.cancel()
                                            focusManager.clearFocus()
                                            isEditingSheetName = false
                                        },
                                        onRequestEditTitle = { isEditingSheetName = true },
                                        onToggleEditSheet = {
                                            sheetTitleBlurJob?.cancel()
                                            if (isEditingSheetName) {
                                                focusManager.clearFocus()
                                            }
                                            isEditingSheetName = !isEditingSheetName
                                        },
                                        onEditDate = { showDatePicker = true },
                                        onDismissSheetTitleEdit = dismissSheetTitleEdit,
                                        losNummerText = losNummerDraft,
                                        onLosNummerChange = { losNummerDraft = it },
                                        serienNummerText = serialNummerDraft,
                                        onSerienNummerChange = { serialNummerDraft = it },
                                        cells = state.cells,
                                        selectedIndex = state.selectedIndex,
                                        draftPerCell = draftPerCell,
                                        onCellSelected = {
                                            dismissSheetTitleEdit()
                                            val prev = state.selectedIndex
                                            if (prev in 0..24 && it != prev) {
                                                flushManualEntryLeaveDraft()
                                            }
                                            if (it in 0..24) {
                                                val committed =
                                                    state.cells.getOrNull(it)?.number?.orEmpty()
                                                        ?: ""
                                                draftPerCell =
                                                    draftPerCell.draftListPaddedTo25()
                                                        .toMutableList()
                                                        .apply { set(it, committed) }
                                            }
                                            viewModel.onAction(
                                                ManualEntryUiAction.CellSelected(it)
                                            )
                                        }
                                    )
                                    }
                                }
                                SubcomposeLayout(modifier = Modifier.fillMaxSize()) { c ->
                                    val loose = Constraints(
                                        minWidth = 0,
                                        maxWidth = c.maxWidth,
                                        minHeight = 0,
                                        maxHeight = Constraints.Infinity
                                    )
                                    val measured =
                                        subcompose("bingo-measure", bingoCard).first()
                                            .measure(loose)
                                    val (maxHPxClosed, maxHPxWhenKeypadOpen) =
                                        manualEntryKeypadLayoutHeightsPx(
                                            c.maxHeight,
                                            fullDockPx,
                                            motionProgress
                                        )
                                    val targetScaleOpen = when {
                                        cardHeightPx <= 0 -> 1f
                                        cardHeightPx <= maxHPxWhenKeypadOpen -> 1f
                                        else -> manualEntryKeypadTargetScale(
                                            cardHeightPx,
                                            maxHPxWhenKeypadOpen
                                        )
                                    }
                                    val currentCardScale =
                                        lerp(1f, targetScaleOpen, motionProgress)
                                    val closedY =
                                        (
                                            (maxHPxClosed - measured.height) / 2f
                                            ).roundToInt().coerceAtLeast(0)
                                    val currentCardY =
                                        lerp(closedY.toFloat(), 0f, motionProgress).roundToInt()
                                    val useScaledLayer =
                                        motionProgress > 0f ||
                                            currentCardScale < 0.999f ||
                                            measured.height > c.maxHeight
                                    if (!keyboardUiActive && measured.height > c.maxHeight) {
                                        val scrollPlaceable =
                                            subcompose("bingo-scroll") {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .verticalScroll(gridScrollState),
                                                    horizontalAlignment =
                                                        Alignment.CenterHorizontally
                                                ) {
                                                    bingoCard()
                                                }
                                            }.first().measure(c)
                                        layout(c.maxWidth, c.maxHeight) {
                                            scrollPlaceable.placeRelative(0, 0)
                                        }
                                    } else if (useScaledLayer) {
                                        layout(c.maxWidth, c.maxHeight) {
                                            measured.placeRelativeWithLayer(
                                                (c.maxWidth - measured.width) / 2,
                                                currentCardY
                                            ) {
                                                scaleX = currentCardScale
                                                scaleY = currentCardScale
                                                transformOrigin = TransformOrigin(0.5f, 0f)
                                            }
                                        }
                                    } else {
                                        val y = (c.maxHeight - measured.height) / 2
                                        layout(c.maxWidth, c.maxHeight) {
                                            measured.placeRelative(
                                                (c.maxWidth - measured.width) / 2,
                                                y
                                            )
                                        }
                                    }
                                }
                        }
                    }
                }
            }
            if (showKeypadSurface) {
                val idx = state.selectedIndex.takeIf { it in 0..24 } ?: lastKeypadIndex
                val layoutDirection = LocalLayoutDirection.current
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(
                            start = paddingValues.calculateStartPadding(layoutDirection),
                            end = paddingValues.calculateEndPadding(layoutDirection)
                        )
                        .height(keypadTotalHeight)
                        .background(MaterialTheme.colorScheme.surface)
                        .clip(RectangleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(bottom = bottomInsetDp)
                    ) {
                        ManualEntryNumericKeypad(
                            selectedIndex = idx,
                            draft = draftPerCell.getOrNull(idx).orEmpty(),
                            onDigit = { digit ->
                                dismissSheetTitleEdit()
                                val cur = draftPerCell.getOrNull(idx).orEmpty()
                                val hasCommitted =
                                    state.cells.getOrNull(idx)?.number?.isNotBlank() == true
                                val newDraft = when {
                                    cur.length >= 2 -> digit.toString()
                                    cur.isEmpty() && hasCommitted -> digit.toString()
                                    else -> cur + digit.toString()
                                }
                                val col = idx % 5
                                val columnRange = manualEntryBingoColumnRange(col)
                                val lzValue = manualEntryLeadingZeroDraftValue(newDraft)
                                if (lzValue != null && lzValue in columnRange) {
                                    manualEntryCommitNumberAndAdvanceToEmpty(
                                        viewModel,
                                        lzValue,
                                        committedCellIndex = idx
                                    )
                                    draftPerCell =
                                        draftPerCell.draftListPaddedTo25().toMutableList()
                                            .apply { set(idx, "") }
                                } else {
                                    val matches =
                                        manualEntryNumbersMatchingDraft(col, newDraft)
                                    if (matches.size == 1) {
                                        manualEntryCommitNumberAndAdvanceToEmpty(
                                            viewModel,
                                            matches.first(),
                                            committedCellIndex = idx
                                        )
                                        draftPerCell =
                                            draftPerCell.draftListPaddedTo25().toMutableList()
                                                .apply { set(idx, "") }
                                    } else {
                                        draftPerCell =
                                            draftPerCell.draftListPaddedTo25().toMutableList()
                                                .apply { set(idx, newDraft) }
                                    }
                                }
                            },
                            onClear = {
                                dismissSheetTitleEdit()
                                val d = draftPerCell.getOrNull(idx).orEmpty()
                                val committed =
                                    state.cells.getOrNull(idx)?.number?.isNotBlank() == true
                                when {
                                    d.isNotEmpty() || committed -> {
                                        viewModel.onAction(
                                            ManualEntryUiAction.DeletePressed
                                        )
                                        draftPerCell =
                                            draftPerCell.draftListPaddedTo25().toMutableList()
                                                .apply { set(idx, "") }
                                    }
                                    idx > 0 -> {
                                        val d = draftPerCell.getOrNull(idx).orEmpty()
                                        manualEntryApplyLeaveCellDraft(
                                            cellIndex = idx,
                                            draft = d,
                                            baselinePair = manualEntrySelectionBaseline,
                                            viewModel = viewModel,
                                            onDraftCleared = { clearedIdx ->
                                                draftPerCell =
                                                    draftPerCell.draftListPaddedTo25()
                                                        .toMutableList()
                                                        .apply { set(clearedIdx, "") }
                                            }
                                        )
                                        val prev = idx - 1
                                        draftPerCell =
                                            draftPerCell.draftListPaddedTo25().toMutableList()
                                                .apply { set(prev, "") }
                                        viewModel.onAction(
                                            ManualEntryUiAction.CellSelected(prev)
                                        )
                                        viewModel.onAction(
                                            ManualEntryUiAction.DeletePressed
                                        )
                                    }
                                }
                            },
                            onConfirm = {
                                dismissSheetTitleEdit()
                                val d = draftPerCell.getOrNull(idx).orEmpty()
                                manualEntryApplyLeaveCellDraft(
                                    cellIndex = idx,
                                    draft = d,
                                    baselinePair = manualEntrySelectionBaseline,
                                    viewModel = viewModel,
                                    onDraftCleared = { clearedIdx ->
                                        draftPerCell =
                                            draftPerCell.draftListPaddedTo25().toMutableList()
                                                .apply { set(clearedIdx, "") }
                                    }
                                )
                            }
                        )
                    }
                }
            }
        }
        if (showDatePicker) {
            val datePickerState = androidx.compose.material3.rememberDatePickerState(
                initialSelectedDateMillis = state.playedAtMillis,
                initialDisplayedMonthMillis = state.playedAtMillis
            )
            val datePickerColors = androidx.compose.material3.DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                headlineContentColor = MaterialTheme.colorScheme.onSurface
            )
            val bolderHeadlineTypography = MaterialTheme.typography.copy(
                headlineMedium = MaterialTheme.typography.headlineMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            )
            androidx.compose.material3.DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                viewModel.onAction(ManualEntryUiAction.PlayDateChanged(millis))
                            }
                            showDatePicker = false
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                MaterialTheme(
                    typography = bolderHeadlineTypography,
                    content = {
                        androidx.compose.material3.DatePicker(
                            state = datePickerState,
                            colors = datePickerColors
                        )
                    }
                )
            }
        }
    }
}

private fun storedColumnOrderToRowMajor(numbers: List<Int>): List<Int> {
    if (numbers.size != 25) return emptyList()
    return (0..4).flatMap { row ->
        (0..4).map { col -> numbers[col * 5 + row] }
    }
}

private fun manualEntryBingoColumnRange(col: Int): IntRange = when (col) {
    0 -> 1..15
    1 -> 16..30
    2 -> 31..45
    3 -> 46..60
    else -> 61..75
}

private fun manualEntryNumbersMatchingDraft(col: Int, draftDigits: String): List<Int> {
    if (draftDigits.isEmpty() || !draftDigits.all { it.isDigit() }) return emptyList()
    return manualEntryBingoColumnRange(col).filter { n ->
        n.toString().startsWith(draftDigits)
    }
}

private fun manualEntryLeadingZeroDraftValue(draftDigits: String): Int? {
    if (draftDigits.length != 2 || draftDigits[0] != '0') return null
    val d = draftDigits[1]
    if (d !in '1'..'9') return null
    return d.digitToInt()
}

private fun manualEntryParsedDraftValue(col: Int, draft: String): Int? {
    if (draft.isEmpty() || !draft.all { it.isDigit() }) return null
    val range = manualEntryBingoColumnRange(col)
    manualEntryLeadingZeroDraftValue(draft)?.takeIf { it in range }?.let { return it }
    if (draft.length == 1) {
        val c = draft[0]
        if (c in '1'..'9' && c.digitToInt() in range) return c.digitToInt()
        return null
    }
    val matches = manualEntryNumbersMatchingDraft(col, draft)
    if (matches.size == 1) return matches.first()
    val v = draft.toIntOrNull() ?: return null
    return v.takeIf { it in range }
}

private sealed interface ManualEntryLeaveVmEffect {
    data object NoVmAction : ManualEntryLeaveVmEffect
    data object ClearDraftOnly : ManualEntryLeaveVmEffect
    data object Delete : ManualEntryLeaveVmEffect
    data class Number(val value: Int) : ManualEntryLeaveVmEffect
}

private fun manualEntryBaselineIntForCell(
    cellIndex: Int,
    baselinePair: Pair<Int, Int?>
): Int? {
    val (idx, v) = baselinePair
    return if (idx == cellIndex) v else null
}

private fun manualEntryCellHasCommittedNumber(cell: BingoCellUi?): Boolean =
    !(cell?.number ?: "").isBlank()

private fun manualEntryIndexOfNextEmptyCell(
    cells: List<BingoCellUi>,
    filledIndex: Int
): Int? {
    if (filledIndex !in 0..24) return null
    for (i in (filledIndex + 1)..24) {
        val c = cells.getOrNull(i) ?: continue
        if (!manualEntryCellHasCommittedNumber(c)) return i
    }
    for (i in 0 until filledIndex) {
        val c = cells.getOrNull(i) ?: continue
        if (!manualEntryCellHasCommittedNumber(c)) return i
    }
    return null
}

private fun manualEntryCommitNumberAndAdvanceToEmpty(
    viewModel: ManualEntryViewModel,
    value: Int,
    committedCellIndex: Int
) {
    if (committedCellIndex !in 0..24) {
        viewModel.onAction(ManualEntryUiAction.NumberPressed(value))
        return
    }
    viewModel.onAction(ManualEntryUiAction.NumberPressed(value))
    val after = viewModel.state.value
    val expected = value.toString().padStart(2, '0')
    if (after.cells.getOrNull(committedCellIndex)?.number != expected) return
    val target = manualEntryIndexOfNextEmptyCell(after.cells, committedCellIndex)
    if (target != null) {
        viewModel.onAction(ManualEntryUiAction.CellSelected(target))
    }
}

private fun manualEntryLeaveCellDraftEffect(
    cellIndex: Int,
    draft: String,
    baselineInt: Int?
): ManualEntryLeaveVmEffect {
    val col = cellIndex % 5
    val parsed = manualEntryParsedDraftValue(col, draft)
    return when {
        parsed != null && baselineInt != null && parsed == baselineInt ->
            ManualEntryLeaveVmEffect.ClearDraftOnly
        parsed != null ->
            ManualEntryLeaveVmEffect.Number(parsed)
        draft.isBlank() && baselineInt != null ->
            ManualEntryLeaveVmEffect.Delete
        else ->
            ManualEntryLeaveVmEffect.NoVmAction
    }
}

private fun manualEntryApplyLeaveCellDraft(
    cellIndex: Int,
    draft: String,
    baselinePair: Pair<Int, Int?>,
    viewModel: ManualEntryViewModel,
    onDraftCleared: (Int) -> Unit
) {
    val baseline = manualEntryBaselineIntForCell(cellIndex, baselinePair)
    when (
        val eff = manualEntryLeaveCellDraftEffect(cellIndex, draft, baseline)
    ) {
        ManualEntryLeaveVmEffect.ClearDraftOnly -> onDraftCleared(cellIndex)
        is ManualEntryLeaveVmEffect.Number -> {
            manualEntryCommitNumberAndAdvanceToEmpty(
                viewModel,
                eff.value,
                committedCellIndex = cellIndex
            )
            onDraftCleared(cellIndex)
        }
        ManualEntryLeaveVmEffect.Delete -> {
            viewModel.onAction(ManualEntryUiAction.DeletePressed)
            onDraftCleared(cellIndex)
        }
        ManualEntryLeaveVmEffect.NoVmAction -> onDraftCleared(cellIndex)
    }
}

private fun manualEntryCommittedNumberDisplay(raw: String?): String? {
    val s = raw?.takeIf { it.isNotBlank() } ?: return null
    val n = s.toIntOrNull() ?: return s
    return n.toString()
}

@Composable
private fun ManualEntryBingoCard(
    sheetName: String,
    dateText: String,
    isEditingSheetName: Boolean,
    sheetTitleStyle: TextStyle,
    sheetTitleFocusRequester: FocusRequester,
    onSheetNameChange: (String) -> Unit,
    onSheetTitleFocusChanged: (FocusState) -> Unit,
    onSheetNameDone: () -> Unit,
    onRequestEditTitle: () -> Unit,
    onToggleEditSheet: () -> Unit,
    onEditDate: () -> Unit,
    onDismissSheetTitleEdit: () -> Unit,
    losNummerText: String,
    onLosNummerChange: (String) -> Unit,
    serienNummerText: String,
    onSerienNummerChange: (String) -> Unit,
    cells: List<BingoCellUi>,
    selectedIndex: Int,
    draftPerCell: List<String>,
    onCellSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    compactGreenHeader: Boolean = false
) {
    val sheetTitleOutsideTapInteraction = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(Dimens.radiusCard)
    val dividerLine = OnPrimary.copy(alpha = 0.12f)
    val metaHeadlineStyle = MaterialTheme.typography.headlineSmall.copy(
        fontWeight = FontWeight.Bold,
        color = OnPrimary
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(align = Alignment.Top)
            .clip(shape)
            .border(1.dp, Outline.copy(alpha = 0.42f), shape)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Primary)
                .padding(
                    horizontal = Dimens.spacing12,
                    vertical = if (compactGreenHeader) Dimens.spacing5 else Dimens.spacing8
                )
        ) {
            val greenHeaderLabelColor = OnPrimary.copy(alpha = 0.78f)
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "BINGO!",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = OnPrimary,
                        modifier = Modifier.then(
                            if (isEditingSheetName) {
                                Modifier.clickable(
                                    indication = null,
                                    interactionSource = sheetTitleOutsideTapInteraction
                                ) { onDismissSheetTitleEdit() }
                            } else {
                                Modifier
                            }
                        )
                    )
                    Text(
                        text = dateText,
                        style = MaterialTheme.typography.bodySmall,
                        color = greenHeaderLabelColor,
                        modifier = Modifier.clickable {
                            if (isEditingSheetName) onDismissSheetTitleEdit()
                            onEditDate()
                        }
                    )
                }
                Spacer(modifier = Modifier.height(Dimens.spacing4))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (isEditingSheetName) {
                            BasicTextField(
                                value = sheetName,
                                onValueChange = onSheetNameChange,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(sheetTitleFocusRequester)
                                    .onFocusChanged(onSheetTitleFocusChanged),
                                textStyle = sheetTitleStyle.copy(
                                    color = OnPrimary,
                                    fontWeight = FontWeight.Normal
                                ),
                                singleLine = true,
                                cursorBrush = SolidColor(OnPrimary),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = { onSheetNameDone() }
                                ),
                                decorationBox = { inner ->
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (sheetName.isEmpty()) {
                                            Text(
                                                text = "Untitled sheet",
                                                style = sheetTitleStyle.copy(
                                                    fontWeight = FontWeight.Normal
                                                ),
                                                color = greenHeaderLabelColor
                                            )
                                        }
                                        inner()
                                    }
                                }
                            )
                        } else {
                            Text(
                                text = sheetName.ifBlank { "Untitled sheet" },
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Normal
                                ),
                                color = greenHeaderLabelColor,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onRequestEditTitle() }
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Rename sheet",
                        modifier = Modifier
                            .size(Dimens.iconAlert)
                            .clickable { onToggleEditSheet() },
                        tint = greenHeaderLabelColor
                    )
                }
            }
            HorizontalDivider(
                modifier = Modifier
                    .padding(vertical = Dimens.spacing5)
                    .then(
                        if (isEditingSheetName) {
                            Modifier.clickable(
                                indication = null,
                                interactionSource = sheetTitleOutsideTapInteraction
                            ) { onDismissSheetTitleEdit() }
                        } else {
                            Modifier
                        }
                    ),
                thickness = 1.dp,
                color = dividerLine
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isEditingSheetName) {
                            Modifier.clickable(
                                indication = null,
                                interactionSource = sheetTitleOutsideTapInteraction
                            ) { onDismissSheetTitleEdit() }
                        } else {
                            Modifier
                        }
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "LOSNUMMER",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 0.6.sp
                        ),
                        color = greenHeaderLabelColor
                    )
                    BasicTextField(
                        value = losNummerText,
                        onValueChange = onLosNummerChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Dimens.spacing8),
                        textStyle = metaHeadlineStyle,
                        singleLine = true,
                        cursorBrush = SolidColor(OnPrimary),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        decorationBox = { inner ->
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (losNummerText.isEmpty()) {
                                    Text(
                                        text = "—",
                                        style = metaHeadlineStyle,
                                        color = greenHeaderLabelColor
                                    )
                                }
                                inner()
                            }
                        }
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(horizontal = Dimens.spacing8)
                        .width(1.dp)
                        .height(Dimens.spacing24)
                        .background(dividerLine)
                )
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "SERIENNUMMER",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 0.6.sp
                        ),
                        color = greenHeaderLabelColor,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                    BasicTextField(
                        value = serienNummerText,
                        onValueChange = onSerienNummerChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Dimens.spacing8),
                        textStyle = metaHeadlineStyle.copy(textAlign = TextAlign.End),
                        singleLine = true,
                        cursorBrush = SolidColor(OnPrimary),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        decorationBox = { inner ->
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                if (serienNummerText.isEmpty()) {
                                    Text(
                                        text = "—",
                                        style = metaHeadlineStyle,
                                        color = greenHeaderLabelColor,
                                        textAlign = TextAlign.End
                                    )
                                }
                                inner()
                            }
                        }
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(
                    start = Dimens.spacing12,
                    top = Dimens.spacing12,
                    end = Dimens.spacing12,
                    bottom = Dimens.spacing12
                )
                .clickable(
                    enabled = isEditingSheetName,
                    indication = null,
                    interactionSource = sheetTitleOutsideTapInteraction
                ) { onDismissSheetTitleEdit() }
        ) {
            val displayCells = remember(cells, selectedIndex, draftPerCell) {
                val normalized =
                    if (cells.size >= 25) cells.take(25) else cells + List(25 - cells.size) {
                        BingoCellUi(null, false, false, true, false)
                    }
                normalized.mapIndexed { i, cell ->
                    val draft = draftPerCell.getOrNull(i).orEmpty()
                    val committed =
                        manualEntryCommittedNumberDisplay(cell.number).orEmpty()
                    val displayNumber = if (i == selectedIndex) {
                        if (draft.isNotEmpty()) draft else committed
                    } else {
                        committed
                    }
                    cell.copy(
                        number = displayNumber.takeIf { it.isNotEmpty() },
                        isSelected = i == selectedIndex,
                        isEditable = true
                    )
                }
            }
            BingoCardGrid(
                cells = displayCells,
                modifier = Modifier.fillMaxWidth(),
                mode = BingoGridMode.PREVIEW,
                winningCells = emptySet(),
                onCellClick = onCellSelected
            )
        }
    }
}

@Composable
private fun ManualEntryDebugAutoFillTopBarAction(onAutoFill: () -> Unit) {
    if (!BuildConfig.DEBUG) return
    Spacer(modifier = Modifier.width(Dimens.spacing8))
    TextButton(
        onClick = onAutoFill,
        modifier = Modifier.sizeIn(
            minWidth = 48.dp,
            minHeight = 48.dp
        ),
        contentPadding = PaddingValues(
            horizontal = Dimens.spacing8,
            vertical = Dimens.spacing8
        )
    ) {
        Text(
            text = "Auto Fill",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
        )
    }
}

@Composable
private fun RowScope.ManualEntryKeypadDigitKey(
    digit: Int,
    keyHeight: Dp,
    keyShape: RoundedCornerShape,
    keyBgIdle: Color,
    onDigit: (Int) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = tween(75, easing = FastOutSlowInEasing),
        label = "keypadDigitScale"
    )
    val keyBg = if (pressed) {
        scheme.surfaceContainerHighest.copy(alpha = 0.62f)
    } else {
        keyBgIdle
    }
    Box(
        modifier = Modifier
            .weight(1f)
            .height(keyHeight)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin(0.5f, 0.5f)
            }
            .clip(keyShape)
            .background(keyBg)
            .clickable(
                interactionSource = interaction,
                indication = null
            ) { onDigit(digit) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$digit",
            style = MaterialTheme.typography.titleMedium,
            color = scheme.onSurface
        )
    }
}

private data class KeypadSideIconPress(
    val interaction: MutableInteractionSource,
    val scale: Float,
    val alpha: Float
)

@Composable
private fun rememberIconPressMotion(
    pressedScale: Float,
    pressedContentAlpha: Float,
    durationMillis: Int,
    scaleLabel: String,
    alphaLabel: String
): KeypadSideIconPress {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = tween(durationMillis, easing = FastOutSlowInEasing),
        label = scaleLabel
    )
    val alpha by animateFloatAsState(
        targetValue = if (pressed) pressedContentAlpha else 1f,
        animationSpec = tween(durationMillis, easing = FastOutSlowInEasing),
        label = alphaLabel
    )
    return KeypadSideIconPress(interaction, scale, alpha)
}

@Composable
private fun rememberKeypadSideIconPress(
    scaleLabel: String,
    alphaLabel: String
): KeypadSideIconPress = rememberIconPressMotion(
    pressedScale = 0.98f,
    pressedContentAlpha = 0.9f,
    durationMillis = 55,
    scaleLabel = scaleLabel,
    alphaLabel = alphaLabel
)

@Composable
private fun rememberTopBarSaveIconPress(
    scaleLabel: String,
    alphaLabel: String
): KeypadSideIconPress = rememberIconPressMotion(
    pressedScale = 0.97f,
    pressedContentAlpha = 0.9f,
    durationMillis = 60,
    scaleLabel = scaleLabel,
    alphaLabel = alphaLabel
)

@Composable
private fun ManualEntryNumericKeypad(
    selectedIndex: Int,
    draft: String,
    onDigit: (Int) -> Unit,
    onClear: () -> Unit,
    onConfirm: () -> Unit
) {
    val letters = listOf("B", "I", "N", "G", "O")
    val col = selectedIndex % 5
    val row = selectedIndex / 5
    val scheme = MaterialTheme.colorScheme
    val keyHeight = Dimens.spacing32 + Dimens.spacing12
    val keyShape = RoundedCornerShape(Dimens.radiusSmall)
    val keyBg = scheme.surfaceContainerHighest.copy(alpha = 0.55f)
    val pillShape = RoundedCornerShape(Dimens.radiusPill)
    val confirmDiameter = Dimens.buttonHeight
    val haptic = LocalHapticFeedback.current
    val numberPopScale = remember { Animatable(1f) }
    var draftPopLaunched by remember { mutableStateOf(false) }
    LaunchedEffect(draft) {
        if (!draftPopLaunched) {
            draftPopLaunched = true
        } else if (draft.isNotEmpty()) {
            numberPopScale.snapTo(1f)
            numberPopScale.animateTo(1.06f, tween(85, easing = FastOutSlowInEasing))
            numberPopScale.animateTo(1f, tween(115, easing = FastOutSlowInEasing))
        } else {
            numberPopScale.snapTo(1f)
        }
    }
    val dividerHeight by animateDpAsState(
        targetValue = if (draft.isNotEmpty()) Dimens.spacing24 else Dimens.spacing16,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "meDividerH"
    )
    val dividerTone by animateFloatAsState(
        targetValue = if (draft.isNotEmpty()) 1f else 0.72f,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "meDividerT"
    )
    val clearPress = rememberKeypadSideIconPress("meClearPressScale", "meClearPressAlpha")
    val confirmPress = rememberKeypadSideIconPress("meConfirmPressScale", "meConfirmPressAlpha")
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(0.dp),
        shape = RoundedCornerShape(
            topStart = Dimens.radiusMedium,
            topEnd = Dimens.radiusMedium
        ),
        color = scheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = Dimens.screenHorizontalPadding,
                        end = Dimens.screenHorizontalPadding,
                        top = Dimens.spacing12,
                        bottom = Dimens.spacing16
                    )
            ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8)
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(Dimens.inputBarHeight)
                        .clip(pillShape)
                        .border(
                            width = Dimens.cardBorderDefault,
                            color = scheme.primary,
                            shape = pillShape
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .background(scheme.primary)
                            .padding(horizontal = Dimens.spacing16),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing4)
                    ) {
                        Text(
                            text = letters[col],
                            style = MaterialTheme.typography.labelLarge.copy(
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            ),
                            color = scheme.primaryContainer,
                            maxLines = 1,
                            textAlign = TextAlign.Start
                        )
                        Text(
                            text = "${row + 1}",
                            style = MaterialTheme.typography.labelLarge.copy(
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            ),
                            color = scheme.onPrimary,
                            maxLines = 1,
                            textAlign = TextAlign.Start
                        )
                    }
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(scheme.primaryContainer)
                            .padding(
                                start = Dimens.spacing12,
                                end = Dimens.spacing8
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .graphicsLayer {
                                    scaleX = numberPopScale.value
                                    scaleY = numberPopScale.value
                                    transformOrigin = TransformOrigin(0.08f, 0.5f)
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Crossfade(
                                    targetState = draft.isEmpty(),
                                    modifier = Modifier.fillMaxWidth(),
                                    animationSpec = tween(
                                        durationMillis = 85,
                                        easing = FastOutSlowInEasing
                                    ),
                                    label = "meValueEmptyFade"
                                ) { isEmpty ->
                                    Text(
                                        text = if (isEmpty) "—" else draft,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .offset(y = (-1).dp),
                                        style = MaterialTheme.typography.headlineLarge.copy(
                                            fontWeight = if (isEmpty) {
                                                FontWeight.Medium
                                            } else {
                                                FontWeight.Bold
                                            },
                                            lineHeight = 40.sp,
                                            letterSpacing = if (isEmpty) {
                                                0.sp
                                            } else {
                                                0.5.sp
                                            },
                                            platformStyle = PlatformTextStyle(
                                                includeFontPadding = false
                                            )
                                        ),
                                        color = if (isEmpty) {
                                            scheme.onPrimaryContainer.copy(alpha = 0.5f)
                                        } else {
                                            scheme.primary
                                        },
                                        maxLines = 1,
                                        textAlign = TextAlign.Start
                                    )
                                }
                            }
                        }
                        Box(
                            modifier = Modifier
                                .width(Dimens.cardBorderDefault)
                                .height(dividerHeight)
                                .background(
                                    scheme.primary.copy(alpha = 0.35f * dividerTone)
                                )
                        )
                        Box(
                            modifier = Modifier
                                .size(Dimens.spacing32)
                                .graphicsLayer {
                                    scaleX = clearPress.scale
                                    scaleY = clearPress.scale
                                    alpha = clearPress.alpha
                                    transformOrigin = TransformOrigin(0.5f, 0.5f)
                                }
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = clearPress.interaction,
                                    indication = null
                                ) { onClear() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "×",
                                modifier = Modifier.offset(y = (-1).dp),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 26.sp
                                ),
                                color = scheme.primary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .size(confirmDiameter)
                        .graphicsLayer {
                            scaleX = confirmPress.scale
                            scaleY = confirmPress.scale
                            alpha = confirmPress.alpha
                            transformOrigin = TransformOrigin(0.5f, 0.5f)
                        }
                        .clip(CircleShape)
                        .border(
                            width = Dimens.cardBorderDefault,
                            color = scheme.primary,
                            shape = CircleShape
                        )
                        .background(
                            if (draft.isNotEmpty()) {
                                scheme.primaryContainer
                            } else {
                                scheme.primaryContainer.copy(alpha = 0.55f)
                            }
                        )
                        .clickable(
                            enabled = draft.isNotEmpty(),
                            interactionSource = confirmPress.interaction,
                            indication = null
                        ) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onConfirm()
                        }
                        .semantics { contentDescription = "Confirm entry" },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier
                            .size(Dimens.iconDefault + Dimens.spacing4)
                            .offset(y = (-1).dp),
                        tint = if (draft.isNotEmpty()) {
                            scheme.primary
                        } else {
                            scheme.primary.copy(alpha = 0.38f)
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(Dimens.spacing12))
            val rowA = listOf(1, 2, 3, 4, 5)
            val rowB = listOf(6, 7, 8, 9, 0)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8)
            ) {
                for (d in rowA) {
                    ManualEntryKeypadDigitKey(
                        digit = d,
                        keyHeight = keyHeight,
                        keyShape = keyShape,
                        keyBgIdle = keyBg,
                        onDigit = onDigit
                    )
                }
            }
            Spacer(modifier = Modifier.height(Dimens.spacing8))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8)
            ) {
                for (d in rowB) {
                    ManualEntryKeypadDigitKey(
                        digit = d,
                        keyHeight = keyHeight,
                        keyShape = keyShape,
                        keyBgIdle = keyBg,
                        onDigit = onDigit
                    )
                }
            }
            }
        }
    }
}

@Composable
private fun RoomPickerBottomSheet(
    rooms: List<LiveRoom>,
    onRoomSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Select Live Room", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            if (rooms.isEmpty()) {
                Text("No rooms yet. Create one from Live Rooms.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                for (room in rooms) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onRoomSelected(room.roomId) }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(room.name, style = MaterialTheme.typography.bodyLarge)
                        Text("Add", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    }
}

