package com.example.mamunbingoapp.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import androidx.lifecycle.viewModelScope
import com.example.mamunbingoapp.data.HistoryRepository
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

    fun setRoomId(roomId: String) { _roomId.value = roomId }
    fun setQuery(s: String) { _query.value = s }
    fun setFilter(option: TicketFilter) { _selectedFilter.value = option }
    fun setSort(option: TicketSort) { _selectedSort.value = option }

    private val ticketsFlow = _roomId.flatMapLatest { id ->
        HistoryRepository.ticketsForPickerFlow(id.ifBlank { null })
    }

    val filterCounts: StateFlow<Map<String, Int>> = ticketsFlow.map { list ->
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
        ticketsFlow,
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
