package com.example.mamunbingoapp.data

import com.example.mamunbingoapp.core.BingoPlayableNumbers
import com.example.mamunbingoapp.ui.model.BingoCellUi

object DemoDataFactory {

    const val ROOM_1 = "demo-room-1"
    const val ROOM_2 = "demo-room-2"
    const val ROOM_3 = "demo-room-3"
    const val SESSION_1 = "demo-s1"
    const val SESSION_2 = "demo-s2"
    const val SESSION_COMPLETED = "demo-completed"
    const val TICKET_1 = "demo-t1"
    const val TICKET_2 = "demo-t2"
    const val TICKET_COMPLETED = "demo-t-completed"

    /** Sample LOS/SERIE for debug `DEMO_MODE` carousel QA only — not used in release. */
    const val DEMO_LOS_1 = "12"
    const val DEMO_SERIAL_1 = "345678"
    const val DEMO_LOS_2 = "08"
    const val DEMO_SERIAL_2 = "901234"
    const val DEMO_LOS_COMPLETED = "99"
    const val DEMO_SERIAL_COMPLETED = "111222"

    private val baseTime = System.currentTimeMillis() - 86_400_000

    /** Six calls for [ROOM_1]; every value exists on [TICKET_1] (assigned in demo room seed). */
    private val DEMO_ROOM_1_CALLED = listOf(2, 11, 14, 22, 38, 52)

    private val DEMO_COMPLETED_CALLED =
        listOf(3, 7, 14, 22, 29, 38, 42, 51, 55, 61, 68, 74)

    data class DemoTicketSeed(
        val ticketId: String,
        val sheetName: String,
        val losNumber: String,
        val serialNumber: String,
        val playedAtMillis: Long,
    )

    fun demoTicketSeeds(): List<DemoTicketSeed> = listOf(
        DemoTicketSeed(TICKET_1, "Demo Sheet 1", DEMO_LOS_1, DEMO_SERIAL_1, baseTime),
        DemoTicketSeed(TICKET_2, "Demo Sheet 2", DEMO_LOS_2, DEMO_SERIAL_2, baseTime + 1_800_000),
        DemoTicketSeed(
            TICKET_COMPLETED,
            "Completed Demo",
            DEMO_LOS_COMPLETED,
            DEMO_SERIAL_COMPLETED,
            baseTime - 86_400_000,
        ),
    )

    fun createDemoRooms(): List<LiveRoom> = listOf(
        LiveRoom(ROOM_1, "Friday Night Bingo", baseTime, true),
        LiveRoom(ROOM_2, "Weekend Session", baseTime + 3600_000, true),
        LiveRoom(ROOM_3, "Practice Room", baseTime + 7200_000, true)
    )

    fun createDemoSessions(): List<HistorySession> {
        val cellsByTicket = createDemoTicketCells()
        val completedCells = cellsByTicket[TICKET_COMPLETED].orEmpty()
        val completedMarked = demoMarkedPlayableCount(completedCells, DEMO_COMPLETED_CALLED)
        return listOf(
        HistorySession(
            id = SESSION_1,
            title = "Demo Sheet 1",
            isCompleted = false,
            sheetsCount = 1,
            calledCount = 0,
            calledNumbersPreview = emptyList(),
            calledNumbersFull = emptyList(),
            sheetsPlayed = listOf(
                SheetPlayed(TICKET_1, "Demo Sheet 1", "Marked: 0/25", 0, 25)
            ),
            sheetName = "Demo Sheet 1",
            playedAtMillis = baseTime,
            losNumber = DEMO_LOS_1,
            serialNumber = DEMO_SERIAL_1,
        ),
        HistorySession(
            id = SESSION_2,
            title = "Demo Sheet 2",
            isCompleted = false,
            sheetsCount = 1,
            calledCount = 0,
            calledNumbersPreview = emptyList(),
            calledNumbersFull = emptyList(),
            sheetsPlayed = listOf(
                SheetPlayed(TICKET_2, "Demo Sheet 2", "Marked: 0/25", 0, 25)
            ),
            sheetName = "Demo Sheet 2",
            playedAtMillis = baseTime + 1800_000,
            losNumber = DEMO_LOS_2,
            serialNumber = DEMO_SERIAL_2,
        ),
        HistorySession(
            id = SESSION_COMPLETED,
            title = "Completed Demo",
            isCompleted = true,
            sheetsCount = 1,
            calledCount = DEMO_COMPLETED_CALLED.size,
            calledNumbersPreview = listOf("B3", "I22", "N38"),
            calledNumbersFull = DEMO_COMPLETED_CALLED,
            sheetsPlayed = listOf(
                SheetPlayed(
                    TICKET_COMPLETED,
                    "Completed Demo",
                    demoMarkedSubtitle(completedMarked),
                    completedMarked,
                    25,
                ),
            ),
            sheetName = "Completed Demo",
            playedAtMillis = baseTime - 86_400_000,
            losNumber = DEMO_LOS_COMPLETED,
            serialNumber = DEMO_SERIAL_COMPLETED,
        ),
        )
    }

    fun createDemoTicketCells(): Map<String, List<BingoCellUi>> {
        val cells1 = cellsForTicket(listOf(
            listOf(2, 11, 23, 38, 52),
            listOf(9, 18, 31, 47, 61),
            listOf(5, 22, 0, 44, 70),
            listOf(12, 27, 36, 50, 65),
            listOf(14, 19, 33, 58, 72)
        ))
        val cells2 = cellsForTicket(listOf(
            listOf(4, 15, 21, 39, 55),
            listOf(7, 17, 28, 45, 63),
            listOf(10, 24, 0, 42, 67),
            listOf(13, 26, 34, 49, 60),
            listOf(1, 20, 37, 53, 74)
        ))
        val cellsCompleted = cellsForTicket(listOf(
            listOf(3, 12, 25, 40, 54),
            listOf(7, 22, 29, 46, 61),
            listOf(14, 38, 0, 42, 68),
            listOf(11, 27, 51, 55, 70),
            listOf(9, 19, 33, 74, 72)
        ))
        return mapOf(
            SESSION_1 to cells1,
            TICKET_1 to cells1,
            SESSION_2 to cells2,
            TICKET_2 to cells2,
            SESSION_COMPLETED to cellsCompleted,
            TICKET_COMPLETED to cellsCompleted
        )
    }

    private fun cellsForTicket(grid: List<List<Int>>): List<BingoCellUi> {
        val list = mutableListOf<BingoCellUi>()
        for (row in grid) {
            for (n in row) {
                val num = if (n == 0) null else n.toString().padStart(2, '0')
                list.add(BingoCellUi(num, false, false, false, false))
            }
        }
        return list
    }

    fun createDemoCalledNumbers(): List<Int> = DEMO_ROOM_1_CALLED

    private fun demoMarkedPlayableCount(cells: List<BingoCellUi>, called: List<Int>): Int {
        val calledSet = called.toSet()
        return cells.take(BingoPlayableNumbers.GRID_CELL_COUNT)
            .mapIndexed { index, cell ->
                BingoPlayableNumbers.isPlayableCellIndex(index) &&
                    cell.number?.trim()?.takeIf { !it.equals("FREE", ignoreCase = true) }
                        ?.toIntOrNull()?.let { it in calledSet } == true
            }
            .count { it }
    }

    private fun demoMarkedSubtitle(marked: Int): String =
        "Marked: ${BingoPlayableNumbers.coercePlayableMarkedCount(marked)}/${BingoPlayableNumbers.PLAYABLE_COUNT}"
}
