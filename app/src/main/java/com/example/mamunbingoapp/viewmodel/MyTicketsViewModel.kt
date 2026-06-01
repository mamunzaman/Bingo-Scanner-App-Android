package com.example.mamunbingoapp.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import androidx.lifecycle.viewModelScope
import com.example.mamunbingoapp.data.HistoryRepository
import com.example.mamunbingoapp.data.RoomRepository
import com.example.mamunbingoapp.ui.model.TicketUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

enum class TicketSort(val displayName: String) {
    Newest("Newest"),
    Oldest("Oldest"),
    NameAZ("Name (A–Z)"),
    Date("Date")
}

private fun Long.isToday(): Boolean {
    val cal = Calendar.getInstance().apply { timeInMillis = this@isToday }
    val today = Calendar.getInstance()
    return cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
        cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
}

private fun Long.isThisWeek(): Boolean {
    val cal = Calendar.getInstance().apply { timeInMillis = this@isThisWeek }
    val now = Calendar.getInstance()
    val weekStart = now.clone() as Calendar
    weekStart.set(Calendar.DAY_OF_WEEK, weekStart.firstDayOfWeek)
    weekStart.set(Calendar.HOUR_OF_DAY, 0)
    weekStart.set(Calendar.MINUTE, 0)
    weekStart.set(Calendar.SECOND, 0)
    val weekEnd = weekStart.clone() as Calendar
    weekEnd.add(Calendar.DAY_OF_YEAR, 7)
    return !cal.before(weekStart) && cal.before(weekEnd)
}

@OptIn(ExperimentalCoroutinesApi::class)
class MyTicketsViewModel : ViewModel() {
    private val _roomId = MutableStateFlow("")
    private val _query = MutableStateFlow("")
    private val _selectedFilter = MutableStateFlow(TicketFilter.All)
    private val _selectedSort = MutableStateFlow(TicketSort.Newest)

    val query: StateFlow<String> = _query.asStateFlow()
    val selectedFilter: StateFlow<TicketFilter> = _selectedFilter.asStateFlow()
    val selectedSort: StateFlow<TicketSort> = _selectedSort.asStateFlow()

    /** Ticket/session ids in the current room — extra fallback if [ticketToRoomFlow] lags (legacy row ids). */
    private val _currentRoomOccupantIds = MutableStateFlow<Set<String>>(emptySet())

    fun setRoomId(roomId: String) { _roomId.value = roomId }
    fun setRoomOccupantIds(ids: Set<String>) { _currentRoomOccupantIds.value = ids }
    fun setQuery(s: String) { _query.value = s }
    fun setFilter(option: TicketFilter) { _selectedFilter.value = option }
    fun setSort(option: TicketSort) { _selectedSort.value = option }

    private val ticketsFlow = _roomId.flatMapLatest { id ->
        HistoryRepository.ticketsForPickerFlow(id.ifBlank { null })
    }

    private val expandedCurrentRoomOccupantIds = combine(ticketsFlow, _currentRoomOccupantIds) { tickets, raw ->
        expandRoomOccupantIds(raw, tickets)
    }

    /** All ticket ids assigned to any live room ([room_tickets]), expanded for legacy session/ticket id pairs. */
    private val expandedAllRoomOccupantIds = combine(
        ticketsFlow,
        RoomRepository.liveTicketIdsFlow(),
    ) { tickets, allRoomTicketIds ->
        expandRoomOccupantIds(allRoomTicketIds, tickets)
    }

    /** One bingo sheet → one room: only tickets with no room assignment are addable. */
    private val availableTicketsFlow = combine(
        ticketsFlow,
        expandedCurrentRoomOccupantIds,
        expandedAllRoomOccupantIds,
    ) { tickets, currentRoomOccupantIds, allRoomOccupantIds ->
        tickets.filter { ticket ->
            !ticket.shouldHideFromPicker(currentRoomOccupantIds, allRoomOccupantIds)
        }
    }

    val allPickerTickets: StateFlow<List<TicketUiModel>> = ticketsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    val availablePickerTickets: StateFlow<List<TicketUiModel>> = availableTicketsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    val filterCounts: StateFlow<Map<String, Int>> = availableTicketsFlow.map { list ->
        mapOf(
            "All" to list.size,
            "Today" to list.count { it.createdAt.isToday() },
            "Week" to list.count { it.createdAt.isThisWeek() }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    val filteredTickets: StateFlow<List<TicketUiModel>> = combine(
        availableTicketsFlow,
        _query,
        _selectedFilter,
        _selectedSort
    ) { list, q, filter, sort ->
        var result = list
            .filter { it.title.contains(q, ignoreCase = true) }
            .filter {
                when (filter) {
                    TicketFilter.All -> true
                    TicketFilter.Today -> it.createdAt.isToday()
                    TicketFilter.ThisWeek -> it.createdAt.isThisWeek()
                }
            }
        result = when (sort) {
            TicketSort.Newest -> result.sortedByDescending { it.createdAt }
            TicketSort.Oldest -> result.sortedBy { it.createdAt }
            TicketSort.NameAZ -> result.sortedBy { it.title.lowercase() }
            TicketSort.Date -> result.sortedByDescending { it.createdAt }
        }
        result
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}

/** Room rows may store [TicketUiModel.id] or legacy [TicketUiModel.sessionId] — match both. */
internal fun expandRoomOccupantIds(
    occupantIds: Set<String>,
    pickerTickets: List<TicketUiModel>,
): Set<String> {
    if (occupantIds.isEmpty()) return emptySet()
    val expanded = occupantIds.toMutableSet()
    pickerTickets.forEach { ticket ->
        if (ticket.id in occupantIds || ticket.sessionId in occupantIds) {
            if (ticket.id.isNotBlank()) expanded.add(ticket.id)
            if (ticket.sessionId.isNotBlank()) expanded.add(ticket.sessionId)
        }
    }
    return expanded
}

/**
 * My Tickets add picker: a sheet may only be assigned to one live room at a time.
 * Hide if [assignedRoomId] is set, if listed under any room in [room_tickets], or if listed
 * under the current room occupant fallback (legacy id shapes).
 */
private fun TicketUiModel.shouldHideFromPicker(
    currentRoomOccupantIds: Set<String>,
    allRoomOccupantIds: Set<String>,
): Boolean {
    if (!assignedRoomId.isNullOrBlank()) return true
    if (id in allRoomOccupantIds || sessionId in allRoomOccupantIds) return true
    if (currentRoomOccupantIds.isNotEmpty() &&
        (id in currentRoomOccupantIds || sessionId in currentRoomOccupantIds)
    ) {
        return true
    }
    return false
}
