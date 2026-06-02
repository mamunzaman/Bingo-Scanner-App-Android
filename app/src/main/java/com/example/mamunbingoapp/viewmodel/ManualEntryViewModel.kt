package com.example.mamunbingoapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.data.AssignTicketResult
import com.example.mamunbingoapp.data.RoomRepository
import com.example.mamunbingoapp.data.TicketRepository
import com.example.mamunbingoapp.ui.components.PendingSheetSave
import com.example.mamunbingoapp.ui.components.RoomConflictUi
import com.example.mamunbingoapp.ui.components.SheetDuplicateUi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.lifecycle.viewModelScope
import com.example.mamunbingoapp.ui.model.BingoCellUi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class ManualEntryUiAction {
    data class CellSelected(val index: Int) : ManualEntryUiAction()
    data class NumberPressed(val value: Int) : ManualEntryUiAction()
    data class SheetNameDraftChanged(val text: String) : ManualEntryUiAction()
    object SheetNameEditStarted : ManualEntryUiAction()
    object SheetNameEditCommitted : ManualEntryUiAction()
    data class PlayDateChanged(val millis: Long) : ManualEntryUiAction()
    object DeletePressed : ManualEntryUiAction()
    object NextPressed : ManualEntryUiAction()
    data class SaveAndPlayClicked(val losNumber: String = "", val serialNumber: String = "") : ManualEntryUiAction()
    data class SaveOnlyClicked(val losNumber: String = "", val serialNumber: String = "") : ManualEntryUiAction()
    object BackClicked : ManualEntryUiAction()
    object AutoFillClicked : ManualEntryUiAction()
    data class RoomSelected(val roomId: String) : ManualEntryUiAction()
    object DismissRoomPicker : ManualEntryUiAction()
}

sealed class ManualEntryUiEvent {
    object NavigateBack : ManualEntryUiEvent()
    data class NavigateToLivePlay(val roomId: String) : ManualEntryUiEvent()
    data class SaveOnlyCompleted(val ticketId: String, val roomId: String?) : ManualEntryUiEvent()
    data class ShowSnackbar(val message: String) : ManualEntryUiEvent()
    data class ShowInfoDialog(val title: String, val message: String) : ManualEntryUiEvent()
    data class NavigateToHistoryDetail(val ticketId: String) : ManualEntryUiEvent()
    /** Clears keypad draft for [cellIndex]; keeps that cell selected (invalid/duplicate entry). */
    data class ClearKeypadDraft(val cellIndex: Int) : ManualEntryUiEvent()
}

private const val MANUAL_ENTRY_VM_TAG = "ManualEntryViewModel"

/** [SavedStateHandle] keys for shell bottom-bar leave interception while Manual Entry is dirty. */
const val MANUAL_ENTRY_UNSAVED_DIRTY_KEY = "manualEntryUnsavedDirty"
const val MANUAL_ENTRY_PENDING_TAB_KEY = "manualEntryPendingTab"

private val BINGO_COLUMN_RANGES = listOf(1..15, 16..30, 31..45, 46..60, 61..75)
private val BINGO_COLUMN_LABELS = listOf("B", "I", "N", "G", "O")

/** Result of evaluating a keypad digit draft before OK is pressed. */
sealed interface KeypadDraftEvaluation {
    data object Incomplete : KeypadDraftEvaluation
    data class Apply(val value: Int) : KeypadDraftEvaluation
    data object WrongColumn : KeypadDraftEvaluation
    data object Duplicate : KeypadDraftEvaluation
}

private fun keypadNumbersMatchingDraft(col: Int, draftDigits: String): List<Int> {
    if (draftDigits.isEmpty() || !draftDigits.all { it.isDigit() }) return emptyList()
    return BINGO_COLUMN_RANGES[col].filter { n -> n.toString().startsWith(draftDigits) }
}

private fun keypadLeadingZeroDraftValue(draftDigits: String): Int? {
    if (draftDigits.length != 2 || draftDigits[0] != '0') return null
    val d = draftDigits[1]
    if (d !in '1'..'9') return null
    return d.digitToInt()
}

/** Single-digit prefix that may still form a valid two-digit number in this column. */
private fun keypadShouldWaitOnSingleDigit(col: Int, digit: Char): Boolean {
    if (!digit.isDigit()) return true
    val d = digit.digitToInt()
    return when (col) {
        0 -> d == 0 || d == 1
        1 -> d in 1..3
        2 -> d in 3..4
        3 -> d in 4..6
        else -> d in 6..7
    }
}

private fun formatManualEntryStoredNumber(value: Int): String = value.toString()

private fun cellCommittedInt(number: String?): Int? =
    number?.trim()?.takeIf { it.isNotBlank() }?.toIntOrNull()

data class ManualEntryUiState(
    val cells: List<BingoCellUi> = List(25) { i ->
        BingoCellUi(null, false, false, true, false, isSelected = i == 0)
    },
    val selectedIndex: Int = 0,
    val isComplete: Boolean = false,
    val errorMessage: String? = null,
    val sheetName: String = "",
    val sheetNameDraft: String? = null,
    val lastValidSheetName: String = "",
    val playedAtMillis: Long = System.currentTimeMillis(),
    val isRoomPickerOpen: Boolean = false,
    val pendingTicketId: String? = null,
    val roomConflict: RoomConflictUi = RoomConflictUi(),
    val sheetDuplicate: SheetDuplicateUi = SheetDuplicateUi(),
)

class ManualEntryViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(ManualEntryUiState())
    val state: StateFlow<ManualEntryUiState> = _state.asStateFlow()
    /** After user starts renaming, do not overwrite sheet name from import/prefill. */
    private var sheetNameTouchedByUser = false
    private var openingSnapshotBound = false
    private var openingLos = ""
    private var openingSerial = ""
    private var openingSheetName = ""
    private var saveCompleted = false

    val pendingBottomTab: kotlinx.coroutines.flow.StateFlow<String?> =
        savedStateHandle.getStateFlow(MANUAL_ENTRY_PENDING_TAB_KEY, null)
    private val roomId: String? = savedStateHandle.get<String>("roomId")?.takeIf { it.isNotBlank() }
    private val ocrSource: String? = savedStateHandle.get<String>("ocrSource")?.takeIf { it in listOf("GEMINI", "ML_KIT") }
    private val ocrConfidence: Float? = savedStateHandle.get<String>("ocrConfidence")?.toFloatOrNull()?.coerceIn(0f, 1f)
    private val originalOcrNumbers: String? = if (ocrSource != null) savedStateHandle.get<String>("scannedNumbers")?.takeIf { it.isNotBlank() } else null

    val rooms: StateFlow<List<com.example.mamunbingoapp.data.LiveRoom>> =
        RoomRepository.getRooms().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _events = MutableSharedFlow<ManualEntryUiEvent>()
    val events: SharedFlow<ManualEntryUiEvent> = _events.asSharedFlow()

    init {
        try {
            _state.update { current ->
                if (current.sheetName.isNotBlank() && current.lastValidSheetName.isNotBlank()) {
                    current
                } else {
                    val defaultName = defaultSheetName(current.playedAtMillis)
                    current.copy(
                        sheetName = defaultName,
                        lastValidSheetName = defaultName,
                    )
                }
            }
        } catch (e: Exception) {
            logRenameState("init_default_name", e)
        }
    }

    /**
     * Applies OCR row-major grid in one update (Master Sheet import). Skips invalid/duplicate numbers
     * instead of showing per-cell dialogs.
     */
    fun applyScannedGridPrefill(rowMajorValues: List<Int>) {
        try {
            val values = rowMajorValues.take(25).let { list ->
                if (list.size < 25) list + List(25 - list.size) { 0 } else list
            }
            val used = mutableSetOf<String>()
            val current = _state.value
            val cells = values.mapIndexed { i, v ->
                val formatted = when {
                    v !in 1..75 -> null
                    else -> {
                        val f = v.toString().padStart(2, '0')
                        if (f in used) null else {
                            used.add(f)
                            f
                        }
                    }
                }
                current.cells.getOrNull(i)?.copy(
                    number = formatted,
                    isMarked = false,
                    isSelected = false,
                ) ?: BingoCellUi(formatted, false, false, true, false, isSelected = false)
            }
            val filled = cells.count { !it.number.isNullOrBlank() }
            _state.update {
                it.copy(
                    cells = cells,
                    selectedIndex = -1,
                    isComplete = filled >= 25,
                )
            }
            Log.d(MANUAL_ENTRY_VM_TAG, "applyScannedGridPrefill filled=$filled/25")
        } catch (e: Exception) {
            Log.e(MANUAL_ENTRY_VM_TAG, "applyScannedGridPrefill failed", e)
        }
    }

    fun bindOpeningSnapshot(los: String, serial: String) {
        if (openingSnapshotBound) return
        openingSnapshotBound = true
        openingLos = los.trim()
        openingSerial = serial.trim()
        openingSheetName = _state.value.sheetName.trim()
    }

    fun hasUnsavedChanges(currentLos: String, currentSerial: String): Boolean {
        if (saveCompleted) return false
        val st = _state.value
        if (st.cells.any { !(it.number.isNullOrBlank()) }) return true
        if (currentLos.trim() != openingLos) return true
        if (currentSerial.trim() != openingSerial) return true
        val nameNow = (st.sheetNameDraft ?: st.sheetName).trim()
        if (nameNow != openingSheetName) return true
        return false
    }

    fun publishUnsavedDirtyFlag(dirty: Boolean) {
        savedStateHandle[MANUAL_ENTRY_UNSAVED_DIRTY_KEY] = dirty
    }

    fun clearPendingBottomTab() {
        savedStateHandle[MANUAL_ENTRY_PENDING_TAB_KEY] = null
    }

    private fun markSaveCompleted() {
        saveCompleted = true
        publishUnsavedDirtyFlag(false)
        savedStateHandle[MANUAL_ENTRY_PENDING_TAB_KEY] = null
    }

    fun applyPrefilledSheetName(name: String?) {
        val trimmed = name?.trim().orEmpty()
        if (trimmed.isBlank()) return
        if (sheetNameTouchedByUser) {
            Log.d(MANUAL_ENTRY_VM_TAG, "applyPrefilledSheetName skipped: user edited sheet name")
            return
        }
        try {
            _state.update {
                it.copy(
                    sheetName = trimmed,
                    lastValidSheetName = trimmed,
                    sheetNameDraft = null,
                )
            }
        } catch (e: Exception) {
            logRenameState("applyPrefilledSheetName", e)
        }
    }

    fun onAction(action: ManualEntryUiAction) {
        when (action) {
            is ManualEntryUiAction.CellSelected -> selectCell(action.index)
            is ManualEntryUiAction.NumberPressed -> enterNumber(action.value)
            is ManualEntryUiAction.SheetNameDraftChanged -> onSheetNameDraftChanged(action.text)
            ManualEntryUiAction.SheetNameEditStarted -> onSheetNameEditStarted()
            ManualEntryUiAction.SheetNameEditCommitted -> commitSheetNameEdit()
            is ManualEntryUiAction.PlayDateChanged -> _state.update { it.copy(playedAtMillis = action.millis) }
            ManualEntryUiAction.DeletePressed -> deleteCurrent()
            ManualEntryUiAction.NextPressed -> moveToNext()
            is ManualEntryUiAction.SaveAndPlayClicked -> saveAndPlay(action.losNumber, action.serialNumber)
            is ManualEntryUiAction.SaveOnlyClicked -> saveOnly(action.losNumber, action.serialNumber)
            ManualEntryUiAction.BackClicked -> navigateBack()
            ManualEntryUiAction.AutoFillClicked -> autoFill()
            is ManualEntryUiAction.RoomSelected -> onRoomSelected(action.roomId)
            ManualEntryUiAction.DismissRoomPicker -> _state.update { it.copy(isRoomPickerOpen = false, pendingTicketId = null) }
        }
    }

    private fun autoFill() {
        val B = (1..15).shuffled().take(5)
        val I = (16..30).shuffled().take(5)
        val N = (31..45).shuffled().take(5)
        val G = (46..60).shuffled().take(5)
        val O = (61..75).shuffled().take(5)
        val columns = listOf(B, I, N, G, O)
        val generated = mutableListOf<String>()
        for (row in 0..4) {
            for (col in 0..4) {
                generated.add(columns[col][row].toString().padStart(2, '0'))
            }
        }
        val current = _state.value
        val cells = generated.mapIndexed { i, num ->
            current.cells[i].copy(
                number = num,
                isMarked = false,
                isSelected = i == 0
            )
        }
        _state.update {
            it.copy(
                cells = cells,
                selectedIndex = 0,
                isComplete = true
            )
        }
    }

    private fun selectCell(index: Int) {
        if (index in -1..24) {
            _state.update { it ->
                it.copy(
                    cells = it.cells.mapIndexed { i, c ->
                        c.copy(isSelected = index >= 0 && i == index)
                    },
                    selectedIndex = index
                )
            }
        }
    }

    private fun rejectKeypadEntry(cellIndex: Int, message: String) {
        viewModelScope.launch {
            _events.emit(ManualEntryUiEvent.ShowSnackbar(message))
            _events.emit(ManualEntryUiEvent.ClearKeypadDraft(cellIndex))
        }
        selectCell(cellIndex)
    }

    fun evaluateKeypadDraft(cellIndex: Int, draft: String): KeypadDraftEvaluation {
        if (cellIndex !in 0..24 || draft.isEmpty() || !draft.all { it.isDigit() }) {
            return KeypadDraftEvaluation.Incomplete
        }
        val col = cellIndex % 5
        val range = BINGO_COLUMN_RANGES[col]

        keypadLeadingZeroDraftValue(draft)?.let { lz ->
            return resolveKeypadDraftValue(cellIndex, lz, range)
        }

        if (draft.length == 1) {
            val digit = draft[0]
            if (keypadShouldWaitOnSingleDigit(col, digit)) {
                return KeypadDraftEvaluation.Incomplete
            }
            return resolveKeypadDraftValue(cellIndex, digit.digitToInt(), range)
        }

        if (draft.length == 2) {
            val matches = keypadNumbersMatchingDraft(col, draft)
            if (matches.size == 1) {
                return resolveKeypadDraftValue(cellIndex, matches.first(), range)
            }
            val parsed = draft.toIntOrNull()
                ?: return KeypadDraftEvaluation.WrongColumn
            return resolveKeypadDraftValue(cellIndex, parsed, range)
        }

        return KeypadDraftEvaluation.Incomplete
    }

    fun rejectInvalidKeypadDraft(cellIndex: Int, evaluation: KeypadDraftEvaluation) {
        if (cellIndex !in 0..24) return
        val app = getApplication<Application>()
        val message = when (evaluation) {
            KeypadDraftEvaluation.WrongColumn ->
                app.getString(R.string.manual_entry_keypad_column_not_allowed)
            KeypadDraftEvaluation.Duplicate ->
                app.getString(R.string.manual_entry_keypad_number_already_added)
            else -> return
        }
        rejectKeypadEntry(cellIndex, message)
    }

    private fun resolveKeypadDraftValue(
        cellIndex: Int,
        value: Int,
        allowedRange: IntRange,
    ): KeypadDraftEvaluation {
        if (value !in allowedRange) return KeypadDraftEvaluation.WrongColumn
        val usedElsewhere = _state.value.cells.mapIndexedNotNull { i, c ->
            cellCommittedInt(c.number)?.takeIf { i != cellIndex }
        }.toSet()
        if (value in usedElsewhere) return KeypadDraftEvaluation.Duplicate
        return KeypadDraftEvaluation.Apply(value)
    }

    private fun enterNumber(value: Int) {
        if (value !in 1..75) return
        val current = _state.value
        val cellIndex = current.selectedIndex
        if (cellIndex !in 0..24) return
        val app = getApplication<Application>()
        val colIndex = cellIndex % 5
        val allowedRange = BINGO_COLUMN_RANGES[colIndex]
        if (value !in allowedRange) {
            rejectKeypadEntry(
                cellIndex,
                app.getString(R.string.manual_entry_keypad_column_not_allowed),
            )
            return
        }
        val usedElsewhere = current.cells.mapIndexedNotNull { i, c ->
            cellCommittedInt(c.number)?.takeIf { i != cellIndex }
        }.toSet()
        if (value in usedElsewhere) {
            rejectKeypadEntry(
                cellIndex,
                app.getString(R.string.manual_entry_keypad_number_already_added),
            )
            return
        }
        val stored = formatManualEntryStoredNumber(value)
        val newCells = current.cells.toMutableList()
        newCells[cellIndex] = current.cells[cellIndex].copy(
            number = stored,
            isSelected = false,
        )
        val nextIndex = (cellIndex + 1).coerceAtMost(24)
        newCells[nextIndex] = newCells[nextIndex].copy(isSelected = true)
        val updated = newCells.mapIndexed { i, c ->
            if (i != nextIndex) c.copy(isSelected = false) else c
        }
        _state.update {
            it.copy(
                cells = updated,
                selectedIndex = nextIndex,
                isComplete = updated.all { c -> c.number != null },
            )
        }
        viewModelScope.launch {
            _events.emit(ManualEntryUiEvent.ClearKeypadDraft(cellIndex))
        }
    }

    private fun deleteCurrent() {
        val current = _state.value
        if (current.selectedIndex !in 0..24) return
        val newCells = current.cells.toMutableList()
        newCells[current.selectedIndex] = current.cells[current.selectedIndex].copy(
            number = null,
            isSelected = true
        )
        _state.update {
            it.copy(
                cells = newCells,
                isComplete = false
            )
        }
    }

    private fun moveToNext() {
        val idx = _state.value.selectedIndex
        if (idx !in 0..24) return
        val nextIndex = (idx + 1).coerceAtMost(24)
        selectCell(nextIndex)
    }

    private fun defaultSheetName(playedAtMillis: Long): String {
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return getApplication<Application>().getString(
            R.string.manual_entry_default_sheet_name,
            df.format(Date(playedAtMillis))
        )
    }

    private fun anotherRoomFallback(): String =
        getApplication<Application>().getString(R.string.history_detail_another_room_fallback)

    private fun localizeAssignError(message: String?): String {
        val app = getApplication<Application>()
        return when (message) {
            "Unknown error" -> app.getString(R.string.manual_entry_error_unknown)
            null, "" -> app.getString(R.string.manual_entry_error_assign_failed)
            else -> message
        }
    }

    private fun onSheetNameEditStarted() {
        try {
            sheetNameTouchedByUser = true
            _state.update { current ->
                current.copy(sheetNameDraft = current.sheetNameDraft ?: current.sheetName)
            }
            logRenameState("edit_started")
        } catch (e: Exception) {
            logRenameState("edit_started", e)
        }
    }

    private fun onSheetNameDraftChanged(text: String) {
        try {
            sheetNameTouchedByUser = true
            val current = _state.value
            val normalized = text.ifEmpty { "" }
            if (normalized == current.sheetNameDraft) return
            _state.update { it.copy(sheetNameDraft = normalized) }
            logRenameState("draft_changed")
        } catch (e: Exception) {
            logRenameState("draft_changed", e)
        }
    }

    private fun commitSheetNameEdit() {
        try {
            _state.update { current ->
                val raw = current.sheetNameDraft ?: current.sheetName
                val trimmed = raw.trim()
                val resolved = trimmed.ifEmpty {
                    current.lastValidSheetName.trim().ifEmpty {
                        defaultSheetName(current.playedAtMillis)
                    }
                }
                current.copy(
                    sheetName = resolved,
                    sheetNameDraft = null,
                    lastValidSheetName = resolved,
                )
            }
            logRenameState("commit_ok")
        } catch (e: Exception) {
            logRenameState("commit_failed", e)
        }
    }

    private fun logRenameState(action: String, error: Throwable? = null) {
        val s = _state.value
        val message = buildString {
            append("rename action=")
            append(action)
            append(" editing=")
            append(s.sheetNameDraft != null)
            append(" sheetName=\"")
            append(s.sheetName)
            append("\" draft=\"")
            append(s.sheetNameDraft.orEmpty())
            append("\" lastValid=\"")
            append(s.lastValidSheetName)
            append("\" pendingTicketId=")
            append(s.pendingTicketId ?: "null")
            append(" roomId=")
            append(roomId ?: "null")
        }
        if (error != null) {
            Log.e(MANUAL_ENTRY_VM_TAG, message, error)
        } else {
            Log.d(MANUAL_ENTRY_VM_TAG, message)
        }
    }

    private fun effectiveSheetName(): String {
        val current = _state.value
        val draft = current.sheetNameDraft
        if (draft != null) {
            val trimmedDraft = draft.trim()
            if (trimmedDraft.isNotEmpty()) return trimmedDraft
        }
        val fromCommitted = current.sheetName.trim()
        if (fromCommitted.isNotEmpty()) return fromCommitted
        val fallback = current.lastValidSheetName.trim()
        if (fallback.isNotEmpty()) return fallback
        return defaultSheetName(current.playedAtMillis)
    }

    private fun normalizedTicketMeta(los: String, serial: String): Pair<String?, String?> {
        val l = los.trim().takeIf { it.isNotEmpty() }
        val s = serial.trim().takeIf { it.isNotEmpty() }
        return l to s
    }

    private fun saveAndPlay(losNumber: String, serialNumber: String, forceDuplicate: Boolean = false) {
        if (!_state.value.isComplete) return
        Log.d("ManualEntry", "saveAndPlay triggered")
        val st = _state.value
        val (los, ser) = normalizedTicketMeta(losNumber, serialNumber)
        viewModelScope.launch {
            if (!forceDuplicate && los != null && ser != null) {
                val existing = TicketRepository.findDuplicateTicketForWeeklyPlay(
                    los, ser, st.playedAtMillis,
                )
                if (existing != null) {
                    _state.update {
                        it.copy(
                            sheetDuplicate = SheetDuplicateUi(
                                visible = true,
                                existingTicketId = existing,
                                losNumber = los,
                                serialNumber = ser,
                                pendingSave = PendingSheetSave.SAVE_AND_PLAY,
                            ),
                        )
                    }
                    return@launch
                }
            }
            val ticketId = TicketRepository.saveManualTicket(
                sheetName = effectiveSheetName(),
                playedAtMillis = st.playedAtMillis,
                cells = st.cells.map { it.copy(isSelected = false) },
                ocrSource = ocrSource,
                ocrConfidence = ocrConfidence,
                originalOcrNumbers = originalOcrNumbers,
                losNumber = los,
                serialNumber = ser,
            )
            markSaveCompleted()
            _state.update { it.copy(isRoomPickerOpen = true, pendingTicketId = ticketId) }
        }
    }

    private fun onRoomSelected(roomId: String) {
        val ticketId = _state.value.pendingTicketId ?: return
        viewModelScope.launch {
            when (val r = RoomRepository.assignTicketToRoom(roomId, ticketId)) {
                is AssignTicketResult.Success -> {
                    markSaveCompleted()
                    _state.update { it.copy(isRoomPickerOpen = false, pendingTicketId = null) }
                    _events.emit(ManualEntryUiEvent.NavigateToLivePlay(roomId))
                }
                is AssignTicketResult.AlreadyInRoom -> {
                    val roomName = RoomRepository.getRoom(r.existingRoomId)?.name ?: anotherRoomFallback()
                    _state.update {
                        it.copy(
                            isRoomPickerOpen = false,
                            roomConflict = RoomConflictUi(
                                visible = true,
                                existingRoomId = r.existingRoomId,
                                existingRoomName = roomName,
                                targetRoomId = roomId
                            )
                        )
                    }
                }
                is AssignTicketResult.Error -> _events.emit(
                    ManualEntryUiEvent.ShowSnackbar(localizeAssignError(r.message))
                )
            }
        }
    }

    private fun saveOnly(losNumber: String, serialNumber: String, forceDuplicate: Boolean = false) {
        if (!_state.value.isComplete) return
        Log.d("ManualEntry", "saveOnly triggered")
        val st = _state.value
        val (los, ser) = normalizedTicketMeta(losNumber, serialNumber)
        viewModelScope.launch {
            if (!forceDuplicate && los != null && ser != null) {
                val existing = TicketRepository.findDuplicateTicketForWeeklyPlay(
                    los, ser, st.playedAtMillis,
                )
                if (existing != null) {
                    _state.update {
                        it.copy(
                            sheetDuplicate = SheetDuplicateUi(
                                visible = true,
                                existingTicketId = existing,
                                losNumber = los,
                                serialNumber = ser,
                                pendingSave = PendingSheetSave.SAVE_ONLY,
                            ),
                        )
                    }
                    return@launch
                }
            }
            val ticketId = TicketRepository.saveManualTicket(
                sheetName = effectiveSheetName(),
                playedAtMillis = st.playedAtMillis,
                cells = st.cells.map { it.copy(isSelected = false) },
                ocrSource = ocrSource,
                ocrConfidence = ocrConfidence,
                originalOcrNumbers = originalOcrNumbers,
                losNumber = los,
                serialNumber = ser,
            )
            markSaveCompleted()
            val targetRoomId = roomId
            if (targetRoomId != null) {
                when (val result = RoomRepository.assignTicketToRoom(targetRoomId, ticketId)) {
                    is AssignTicketResult.Success,
                    is AssignTicketResult.AlreadyInRoom -> {
                        Log.d("ManualEntry", "saveOnlyCompleted emitted, ticketId=$ticketId")
                        _events.emit(ManualEntryUiEvent.SaveOnlyCompleted(ticketId, targetRoomId))
                    }
                    is AssignTicketResult.Error -> {
                        _events.emit(ManualEntryUiEvent.ShowSnackbar(localizeAssignError(result.message)))
                    }
                }
            } else {
                Log.d("ManualEntry", "saveOnlyCompleted emitted (no room), ticketId=$ticketId")
                _events.emit(ManualEntryUiEvent.SaveOnlyCompleted(ticketId, null))
            }
        }
    }

    private fun navigateBack() {
        Log.d("ManualEntry", "navigate back triggered (emitting NavigateBack)")
        viewModelScope.launch { _events.emit(ManualEntryUiEvent.NavigateBack) }
    }

    fun dismissSheetDuplicate() {
        _state.update { it.copy(sheetDuplicate = SheetDuplicateUi()) }
    }

    fun openExistingSheetFromDuplicate() {
        val id = _state.value.sheetDuplicate.existingTicketId ?: return
        dismissSheetDuplicate()
        viewModelScope.launch { _events.emit(ManualEntryUiEvent.NavigateToHistoryDetail(id)) }
    }

    fun saveDespiteSheetDuplicate() {
        val dup = _state.value.sheetDuplicate
        val pending = dup.pendingSave ?: return
        val los = dup.losNumber
        val serial = dup.serialNumber
        dismissSheetDuplicate()
        when (pending) {
            PendingSheetSave.SAVE_ONLY -> saveOnly(los, serial, forceDuplicate = true)
            PendingSheetSave.SAVE_AND_PLAY -> saveAndPlay(los, serial, forceDuplicate = true)
        }
    }

    fun dismissConflict() {
        _state.update { it.copy(roomConflict = RoomConflictUi()) }
    }

    fun openExistingRoom() {
        val id = _state.value.roomConflict.existingRoomId ?: return
        markSaveCompleted()
        _state.update { it.copy(roomConflict = RoomConflictUi()) }
        viewModelScope.launch { _events.emit(ManualEntryUiEvent.NavigateToLivePlay(id)) }
    }

    fun moveToTargetRoom() {
        val c = _state.value.roomConflict
        val from = c.existingRoomId ?: return
        val to = c.targetRoomId ?: return
        val ticketId = _state.value.pendingTicketId ?: return
        viewModelScope.launch {
            when (val moveResult = RoomRepository.moveTicketToRoom(ticketId, from, to)) {
                is AssignTicketResult.Success -> {
                    markSaveCompleted()
                    _state.update { it.copy(roomConflict = RoomConflictUi()) }
                    _events.emit(ManualEntryUiEvent.NavigateToLivePlay(to))
                }
                is AssignTicketResult.AlreadyInRoom -> { }
                is AssignTicketResult.Error -> _events.emit(
                    ManualEntryUiEvent.ShowSnackbar(localizeAssignError(moveResult.message))
                )
            }
        }
    }
}
