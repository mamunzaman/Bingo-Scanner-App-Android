package com.example.mamunbingoapp.data

import com.example.mamunbingoapp.data.db.TicketCellEntity
import com.example.mamunbingoapp.data.remote.BingoRemoteRepository
import com.example.mamunbingoapp.data.remote.NoDrawForWeekException
import com.example.mamunbingoapp.ui.components.home.ActiveTicketCellState

object TicketCalledNumbersResolver {

    enum class Source {
        LIVE_ROOM,
        TEST_DATE,
        ARCHIVED,
        NONE,
    }

    data class Result(
        val calledNumbers: List<Int>,
        val source: Source,
        val drawDateLabel: String? = null,
        val testDateError: TestDateError? = null,
    )

    enum class TestDateError {
        NO_DRAW,
        LOAD_ERROR,
    }

    fun forLiveRoom(
        roomId: String?,
        calledNumbersByRoom: Map<String, List<Int>>,
    ): Result {
        if (roomId.isNullOrBlank()) return Result(emptyList(), Source.NONE)
        return Result(
            calledNumbers = calledNumbersByRoom[roomId].orEmpty(),
            source = Source.LIVE_ROOM,
        )
    }

    suspend fun forOfflineTicket(
        testDateMillis: Long?,
        archivedNumbers: List<Int>,
        drawCache: MutableMap<Long, List<Int>?> = mutableMapOf(),
    ): Result {
        if (testDateMillis != null) {
            val cachedNumbers = drawCache[testDateMillis]
            if (cachedNumbers == null) {
                val drawResult = BingoRemoteRepository.getDrawForWeekContaining(testDateMillis)
                val draw = drawResult.getOrNull()
                drawCache[testDateMillis] = draw?.winningNumbers
                if (draw != null && draw.winningNumbers.isNotEmpty()) {
                    return Result(
                        calledNumbers = draw.winningNumbers,
                        source = Source.TEST_DATE,
                        drawDateLabel = draw.drawDate,
                    )
                }
                val testDateError = when {
                    drawResult.isFailure && drawResult.exceptionOrNull() is NoDrawForWeekException ->
                        TestDateError.NO_DRAW
                    drawResult.isFailure -> TestDateError.LOAD_ERROR
                    else -> TestDateError.NO_DRAW
                }
                if (archivedNumbers.isNotEmpty()) {
                    return Result(
                        calledNumbers = archivedNumbers,
                        source = Source.ARCHIVED,
                        testDateError = testDateError,
                    )
                }
                return Result(
                    calledNumbers = emptyList(),
                    source = Source.NONE,
                    testDateError = testDateError,
                )
            }
            if (cachedNumbers.isNotEmpty()) {
                return Result(cachedNumbers, Source.TEST_DATE)
            }
            if (archivedNumbers.isNotEmpty()) {
                return Result(archivedNumbers, Source.ARCHIVED)
            }
            return Result(emptyList(), Source.NONE)
        }
        if (archivedNumbers.isNotEmpty()) {
            return Result(archivedNumbers, Source.ARCHIVED)
        }
        return Result(emptyList(), Source.NONE)
    }

    fun buildActiveTicketCellStates(
        cells: List<TicketCellEntity>,
        calledNumbers: List<Int>,
    ): List<ActiveTicketCellState> {
        val calledSet = calledNumbers.toSet()
        val sorted = cells.sortedBy { it.cellIndex }
        return (0 until 25).map { index ->
            val cell = sorted.getOrNull(index)
            val number = cell?.value?.trim()?.takeIf { it.uppercase() != "FREE" }?.toIntOrNull()
            val isCalled = (number != null && number in calledSet) || cell?.isMarked == true
            ActiveTicketCellState(
                display = cell?.value?.trim().orEmpty(),
                isCalled = isCalled,
            )
        }
    }
}
