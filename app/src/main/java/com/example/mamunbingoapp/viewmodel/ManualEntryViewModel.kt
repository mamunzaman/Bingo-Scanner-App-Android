package com.example.mamunbingoapp.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import com.example.mamunbingoapp.data.AssignTicketResult
import com.example.mamunbingoapp.data.RoomRepository
import com.example.mamunbingoapp.data.TicketRepository
import com.example.mamunbingoapp.ui.components.RoomConflictUi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.lifecycle.ViewModel
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
    data class SheetNameChanged(val text: String) : ManualEntryUiAction()
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
}

private val BINGO_COLUMN_RANGES = listOf(1..15, 16..30, 31..45, 46..60, 61..75)
private val BINGO_COLUMN_LABELS = listOf("B", "I", "N", "G", "O")

data class ManualEntryUiState(
    val cells: List<BingoCellUi> = List(25) { i ->
        BingoCellUi(null, false, false, true, false, isSelected = i == 0)
    },
    val selectedIndex: Int = 0,
    val isComplete: Boolean = false,
    val errorMessage: String? = null,
    val sheetName: String = "",
    val playedAtMillis: Long = System.currentTimeMillis(),
    val isRoomPickerOpen: Boolean = false,
    val pendingTicketId: String? = null,
    val roomConflict: RoomConflictUi = RoomConflictUi()
)

class ManualEntryViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(ManualEntryUiState())
    val state: StateFlow<ManualEntryUiState> = _state.asStateFlow()
    private val roomId: String? = savedStateHandle.get<String>("roomId")?.takeIf { it.isNotBlank() }
    private val ocrSource: String? = savedStateHandle.get<String>("ocrSource")?.takeIf { it in listOf("GEMINI", "ML_KIT") }
    private val ocrConfidence: Float? = savedStateHandle.get<String>("ocrConfidence")?.toFloatOrNull()?.coerceIn(0f, 1f)
    private val originalOcrNumbers: String? = if (ocrSource != null) savedStateHandle.get<String>("scannedNumbers")?.takeIf { it.isNotBlank() } else null

    val rooms: StateFlow<List<com.example.mamunbingoapp.data.LiveRoom>> =
        RoomRepository.getRooms().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _events = MutableSharedFlow<ManualEntryUiEvent>()
    val events: SharedFlow<ManualEntryUiEvent> = _events.asSharedFlow()

    fun onAction(action: ManualEntryUiAction) {
        when (action) {
            is ManualEntryUiAction.CellSelected -> selectCell(action.index)
            is ManualEntryUiAction.NumberPressed -> enterNumber(action.value)
            is ManualEntryUiAction.SheetNameChanged -> _state.update { it.copy(sheetName = action.text) }
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

    private fun enterNumber(value: Int) {
        if (value !in 1..75) return
        val current = _state.value
        if (current.selectedIndex !in 0..24) return
        val colIndex = current.selectedIndex % 5
        val allowedRange = BINGO_COLUMN_RANGES[colIndex]
        if (value !in allowedRange) {
            val label = BINGO_COLUMN_LABELS[colIndex]
            viewModelScope.launch {
                _events.emit(
                    ManualEntryUiEvent.ShowInfoDialog(
                        title = "Invalid Number for Column $label",
                        message = "$value is not allowed. Column $label only accepts numbers ${allowedRange.first}–${allowedRange.last}."
                    )
                )
            }
            return
        }
        val formatted = value.toString().padStart(2, '0')
        val existing = current.cells.mapNotNull { it.number }.toSet()
        if (formatted in existing) {
            viewModelScope.launch {
                _events.emit(
                    ManualEntryUiEvent.ShowInfoDialog(
                        title = "Number Already Used",
                        message = "$formatted has already been added to the grid."
                    )
                )
            }
            return
        }
        val newCells = current.cells.toMutableList()
        newCells[current.selectedIndex] = current.cells[current.selectedIndex].copy(
            number = formatted,
            isSelected = false
        )
        val nextIndex = (current.selectedIndex + 1).coerceAtMost(24)
        newCells[nextIndex] = newCells[nextIndex].copy(isSelected = true)
        val updated = newCells.mapIndexed { i, c ->
            if (i != nextIndex) c.copy(isSelected = false) else c
        }
        _state.update {
            it.copy(
                cells = updated,
                selectedIndex = nextIndex,
                isComplete = updated.all { c -> c.number != null }
            )
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

    private fun effectiveSheetName(): String {
        val s = _state.value.sheetName.trim()
        if (s.isNotEmpty()) return s
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return "Sheet ${df.format(Date(_state.value.playedAtMillis))}"
    }

    private fun normalizedTicketMeta(los: String, serial: String): Pair<String?, String?> {
        val l = los.trim().takeIf { it.isNotEmpty() }
        val s = serial.trim().takeIf { it.isNotEmpty() }
        return l to s
    }

    private fun saveAndPlay(losNumber: String, serialNumber: String) {
        if (!_state.value.isComplete) return
        Log.d("ManualEntry", "saveAndPlay triggered")
        val st = _state.value
        val (los, ser) = normalizedTicketMeta(losNumber, serialNumber)
        viewModelScope.launch {
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
            _state.update { it.copy(isRoomPickerOpen = true, pendingTicketId = ticketId) }
        }
    }

    private fun onRoomSelected(roomId: String) {
        val ticketId = _state.value.pendingTicketId ?: return
        viewModelScope.launch {
            when (val r = RoomRepository.assignTicketToRoom(roomId, ticketId)) {
                is AssignTicketResult.Success -> {
                    _state.update { it.copy(isRoomPickerOpen = false, pendingTicketId = null) }
                    _events.emit(ManualEntryUiEvent.NavigateToLivePlay(roomId))
                }
                is AssignTicketResult.AlreadyInRoom -> {
                    val roomName = RoomRepository.getRoom(r.existingRoomId)?.name ?: "another room"
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
                    ManualEntryUiEvent.ShowSnackbar(r.message)
                )
            }
        }
    }

    private fun saveOnly(losNumber: String, serialNumber: String) {
        if (!_state.value.isComplete) return
        Log.d("ManualEntry", "saveOnly triggered")
        val st = _state.value
        val (los, ser) = normalizedTicketMeta(losNumber, serialNumber)
        viewModelScope.launch {
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
            val targetRoomId = roomId
            if (targetRoomId != null) {
                when (val result = RoomRepository.assignTicketToRoom(targetRoomId, ticketId)) {
                    is AssignTicketResult.Success,
                    is AssignTicketResult.AlreadyInRoom -> {
                        Log.d("ManualEntry", "saveOnlyCompleted emitted, ticketId=$ticketId")
                        _events.emit(ManualEntryUiEvent.SaveOnlyCompleted(ticketId, targetRoomId))
                    }
                    is AssignTicketResult.Error -> {
                        _events.emit(ManualEntryUiEvent.ShowSnackbar(result.message))
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

    fun dismissConflict() {
        _state.update { it.copy(roomConflict = RoomConflictUi()) }
    }

    fun openExistingRoom() {
        val id = _state.value.roomConflict.existingRoomId ?: return
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
                    _state.update { it.copy(roomConflict = RoomConflictUi()) }
                    _events.emit(ManualEntryUiEvent.NavigateToLivePlay(to))
                }
                is AssignTicketResult.AlreadyInRoom -> { }
                is AssignTicketResult.Error -> _events.emit(
                    ManualEntryUiEvent.ShowSnackbar(moveResult.message)
                )
            }
        }
    }
}
