package com.example.mamunbingoapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mamunbingoapp.BuildConfig
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.data.HistoryRepository
import com.example.mamunbingoapp.data.HistorySession
import com.example.mamunbingoapp.data.RoomRepository
import com.example.mamunbingoapp.data.TicketCalledNumbersResolver
import com.example.mamunbingoapp.data.TicketPlayLogRepository
import com.example.mamunbingoapp.data.TicketRepository
import com.example.mamunbingoapp.data.db.TicketCellEntity
import com.example.mamunbingoapp.data.remote.BingoDrawDto
import com.example.mamunbingoapp.data.remote.BingoPrizeDto
import com.example.mamunbingoapp.data.remote.BingoRemoteRepository
import com.example.mamunbingoapp.ui.components.home.ActiveTicketCellState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

data class HomeActiveTicketCardState(
    val sessionId: String,
    val isInLiveRoom: Boolean,
    val resultSource: TicketCalledNumbersResolver.Source,
    val calledCount: Int,
    val calledProgress: Float,
    val showCalledProgress: Boolean,
    val neutralGrid: Boolean,
    val cellStates: List<ActiveTicketCellState>,
    val drawDateMillis: Long,
)

data class HomeActiveTicketsUiState(
    val activeCount: Int = 0,
    val calledCount: Int = 0,
    val hasActiveLiveRoom: Boolean = false,
    val previews: List<HomeActiveTicketCardState> = emptyList(),
)

private data class HomeActiveTicketsInput(
    val sessions: List<HistorySession>,
    val cellsByTicket: Map<String, List<TicketCellEntity>>,
    val ticketToRoom: Map<String, String>,
    val calledNumbersByRoom: Map<String, List<Int>>,
    val archivedByRoom: Map<String, Boolean>,
    val archivedCalledByTicket: Map<String, List<Int>>,
    val testDatesBySession: Map<String, Long>,
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private companion object {
        const val TAG = "HomeViewModel"
    }

    private val _latestDraw = MutableStateFlow<BingoDrawDto?>(null)
    val latestDraw: StateFlow<BingoDrawDto?> = _latestDraw.asStateFlow()

    private val _latestPrizes = MutableStateFlow<List<BingoPrizeDto>>(emptyList())
    val latestPrizes: StateFlow<List<BingoPrizeDto>> = _latestPrizes.asStateFlow()

    private val _isRemoteLoading = MutableStateFlow(false)
    val isRemoteLoading: StateFlow<Boolean> = _isRemoteLoading.asStateFlow()

    private val _remoteError = MutableStateFlow<String?>(null)
    val remoteError: StateFlow<String?> = _remoteError.asStateFlow()

    private val activeTicketsInputFlow = combine(
        combine(
            HistoryRepository.sessionsFlow,
            TicketRepository.cellsByTicketFlow(),
            RoomRepository.ticketToRoomFlow(),
        ) { sessions, cellsByTicket, ticketToRoom ->
            Triple(sessions, cellsByTicket, ticketToRoom)
        },
        combine(
            RoomRepository.allRoomsCalledNumbersFlow(),
            RoomRepository.roomsArchivedMapFlow(),
            TicketPlayLogRepository.observeLatestCalledNumbersByTicket(),
        ) { calledByRoom, archivedByRoom, archivedByTicket ->
            Triple(calledByRoom, archivedByRoom, archivedByTicket)
        },
        HistoryRepository.observeAllHistoryTestDates(),
    ) { core, room, testDates ->
        HomeActiveTicketsInput(
            sessions = core.first,
            cellsByTicket = core.second,
            ticketToRoom = core.third,
            calledNumbersByRoom = room.first,
            archivedByRoom = room.second,
            archivedCalledByTicket = room.third,
            testDatesBySession = testDates,
        )
    }

    val activeTicketsUi: StateFlow<HomeActiveTicketsUiState> = activeTicketsInputFlow.flatMapLatest { input ->
        flow { emit(buildActiveTicketsUi(input)) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeActiveTicketsUiState(),
    )

    private var latestDrawLoadJob: Job? = null
    private val latestDrawRequestId = AtomicInteger(0)

    init {
        refreshLatestBingoDraw()
    }

    /** Reload jackpot / draw from Supabase (tab return, pull-to-refresh, cold start). */
    fun refreshLatestBingoDraw() {
        val requestId = latestDrawRequestId.incrementAndGet()
        latestDrawLoadJob?.cancel()
        latestDrawLoadJob = viewModelScope.launch {
            loadLatestBingoDraw(requestId)
        }
    }

    private suspend fun buildActiveTicketsUi(input: HomeActiveTicketsInput): HomeActiveTicketsUiState {
        val activeLiveRoomId = resolveActiveLiveRoomId(
            calledNumbersByRoom = input.calledNumbersByRoom,
            archivedByRoom = input.archivedByRoom,
            ticketToRoom = input.ticketToRoom,
        )
        val activeCalledCount = activeLiveRoomId
            ?.let { input.calledNumbersByRoom[it]?.size }
            ?: 0
        val drawCache = mutableMapOf<Long, List<Int>?>()
        val previews = input.sessions.map { session ->
            val ticketId = session.ticketId
            val roomId = input.ticketToRoom[ticketId]
            val archivedNumbers = input.archivedCalledByTicket[ticketId].orEmpty()
            val testDateMillis = input.testDatesBySession[session.id]
            val resolved = if (!roomId.isNullOrBlank()) {
                TicketCalledNumbersResolver.forLiveRoom(roomId, input.calledNumbersByRoom)
            } else {
                TicketCalledNumbersResolver.forOfflineTicket(
                    testDateMillis = testDateMillis,
                    archivedNumbers = archivedNumbers,
                    drawCache = drawCache,
                )
            }
            val isInLiveRoom = !roomId.isNullOrBlank()
            val calledCount = resolved.calledNumbers.size
            val hasResolvedNumbers = calledCount > 0
            HomeActiveTicketCardState(
                sessionId = session.id,
                isInLiveRoom = isInLiveRoom,
                resultSource = resolved.source,
                calledCount = calledCount,
                calledProgress = calledCount / 75f,
                showCalledProgress = isInLiveRoom || (hasResolvedNumbers && resolved.source != TicketCalledNumbersResolver.Source.NONE),
                neutralGrid = !isInLiveRoom && !hasResolvedNumbers,
                cellStates = TicketCalledNumbersResolver.buildActiveTicketCellStates(
                    cells = input.cellsByTicket[ticketId].orEmpty(),
                    calledNumbers = resolved.calledNumbers,
                ),
                drawDateMillis = testDateMillis ?: session.effectivePlayedAtMillis(),
            )
        }
        return HomeActiveTicketsUiState(
            activeCount = input.sessions.size,
            calledCount = activeCalledCount,
            hasActiveLiveRoom = activeLiveRoomId != null,
            previews = previews,
        )
    }

    private fun resolveActiveLiveRoomId(
        calledNumbersByRoom: Map<String, List<Int>>,
        archivedByRoom: Map<String, Boolean>,
        ticketToRoom: Map<String, String>,
    ): String? {
        val liveRoomIds = ticketToRoom.values.toSet()
        return calledNumbersByRoom
            .filter { (roomId, numbers) ->
                numbers.isNotEmpty() &&
                    !archivedByRoom.getOrDefault(roomId, false) &&
                    (liveRoomIds.isEmpty() || roomId in liveRoomIds)
            }
            .maxByOrNull { it.value.size }
            ?.key
    }

    private suspend fun loadLatestBingoDraw(requestId: Int) {
        _isRemoteLoading.value = true
        _remoteError.value = null
        try {
            BingoRemoteRepository.getLatestDraw()
                .onSuccess { draw ->
                    if (!isLatestDrawRequest(requestId)) {
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "Ignoring stale draw response requestId=$requestId")
                        }
                        return@onSuccess
                    }
                    if (BuildConfig.DEBUG) {
                        Log.d(
                            TAG,
                            "Applying latest draw requestId=$requestId " +
                                "drawDate=${draw.drawDate} jackpot=${draw.jackpot} " +
                                "updatedAt=${draw.updatedAt}",
                        )
                    }
                    _latestDraw.value = draw
                    BingoRemoteRepository.getPrizesForDraw(draw.id)
                        .onSuccess { prizes ->
                            if (isLatestDrawRequest(requestId)) {
                                _latestPrizes.value = prizes
                            }
                        }
                        .onFailure { error ->
                            if (!isLatestDrawRequest(requestId)) return@onFailure
                            Log.w(TAG, "Failed to load prizes", error)
                            _remoteError.value = getApplication<Application>().getString(
                                R.string.home_error_load_prizes,
                            )
                        }
                }
                .onFailure { error ->
                    if (!isLatestDrawRequest(requestId)) return@onFailure
                    Log.w(TAG, "Failed to load latest draw", error)
                    _latestDraw.value = null
                    _latestPrizes.value = emptyList()
                    _remoteError.value = getApplication<Application>().getString(
                        R.string.home_error_load_latest_draw,
                    )
                }
        } finally {
            if (isLatestDrawRequest(requestId)) {
                _isRemoteLoading.value = false
            }
        }
    }

    private fun isLatestDrawRequest(requestId: Int): Boolean =
        requestId == latestDrawRequestId.get()
}

