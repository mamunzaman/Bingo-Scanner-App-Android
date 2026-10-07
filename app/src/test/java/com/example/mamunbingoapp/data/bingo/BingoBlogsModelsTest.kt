package com.example.mamunbingoapp.data.bingo

import com.example.mamunbingoapp.data.TicketCalledNumbersResolver
import com.example.mamunbingoapp.data.remote.BingoDrawResultDto
import com.example.mamunbingoapp.data.remote.BingoStatusDto
import com.example.mamunbingoapp.viewmodel.HomeBingoRefreshReducer
import com.example.mamunbingoapp.viewmodel.HomeBingoUiSnapshot
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BingoBlogsModelsTest {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val sunday = LocalDate.of(2026, 10, 4)
    private val validNumbers = listOf(
        6, 50, 20, 60, 13, 46, 40, 2, 56, 54, 62, 63, 5, 37, 73, 7, 65, 41, 12, 55, 43, 72,
    )

    @Test
    fun statusJsonDecodes() {
        val dto = json.decodeFromString<BingoStatusDto>(
            """{"current_jackpot_eur":1000000,"next_draw_at":"2026-10-11T15:00:00Z","latest_draw_date":"2026-10-04","checked_at":"2026-10-06T22:35:53Z"}""",
        )
        val status = BingoDrawValidator.validateStatus(dto)
        assertEquals(1_000_000.0, status?.jackpotEur)
        assertEquals(Instant.parse("2026-10-11T15:00:00Z"), status?.nextDrawAt)
        assertEquals(sunday, status?.latestDrawDate)
    }

    @Test
    fun nullableStatusJsonDecodes() {
        val dto = json.decodeFromString<BingoStatusDto>("""{"checked_at":"2026-10-06T22:35:53Z"}""")
        val status = BingoDrawValidator.validateStatus(dto)
        assertNotNull(status)
        assertNull(status?.jackpotEur)
        assertNull(status?.latestDrawDate)
    }

    @Test
    fun drawJsonDecodes() {
        val dto = json.decodeFromString<BingoDrawResultDto>(drawJson(validNumbers))
        val draw = BingoDrawValidator.validateDraw(sunday, dto)
        assertEquals(sunday, draw?.drawDate)
        assertEquals(validNumbers, draw?.winningNumbers)
    }

    @Test
    fun valid22NumbersAccepted() {
        assertNotNull(BingoDrawValidator.validateDraw(sunday, dto(validNumbers)))
    }

    @Test
    fun duplicateNumberRejected() {
        val numbers = validNumbers.toMutableList()
        numbers[21] = numbers[0]
        assertNull(BingoDrawValidator.validateDraw(sunday, dto(numbers)))
    }

    @Test
    fun numberBelow1Rejected() {
        val numbers = validNumbers.toMutableList()
        numbers[0] = 0
        assertNull(BingoDrawValidator.validateDraw(sunday, dto(numbers)))
    }

    @Test
    fun numberAbove75Rejected() {
        val numbers = validNumbers.toMutableList()
        numbers[0] = 76
        assertNull(BingoDrawValidator.validateDraw(sunday, dto(numbers)))
    }

    @Test
    fun fewerThan22Rejected() {
        assertNull(BingoDrawValidator.validateDraw(sunday, dto(validNumbers.take(21))))
    }

    @Test
    fun moreThan22Rejected() {
        assertNull(BingoDrawValidator.validateDraw(sunday, dto(validNumbers + 1)))
    }

    @Test
    fun responseDateMismatchRejected() {
        assertNull(BingoDrawValidator.validateDraw(LocalDate.of(2026, 9, 27), dto(validNumbers)))
    }

    @Test
    fun nonSundayRejected() {
        val monday = LocalDate.of(2026, 10, 5)
        val dto = BingoDrawResultDto(drawDate = "2026-10-05", winningNumbers = validNumbers)
        assertNull(BingoDrawValidator.validateDraw(monday, dto))
    }

    @Test
    fun winningNumberOrderPreserved() {
        val reversed = validNumbers.reversed()
        assertEquals(reversed, BingoDrawValidator.validateDraw(sunday, dto(reversed))?.winningNumbers)
    }

    @Test
    fun invalidJackpotRejected() {
        assertNull(BingoDrawValidator.validateJackpot(0.0))
        assertNull(BingoDrawValidator.validateJackpot(-1.0))
        assertNull(BingoDrawValidator.validateJackpot(Double.NaN))
        assertNull(BingoDrawValidator.validateJackpot(Double.POSITIVE_INFINITY))
    }

    @Test
    fun draw404MappedUnavailable() {
        assertEquals(
            BingoHttpOutcome.DRAW_NOT_FOUND,
            BingoHttpResponse.outcome(404, "application/json", """{"error":"not_found"}""", true),
        )
    }

    @Test
    fun non2xxHtmlRejectedBeforeJson() {
        assertEquals(
            BingoHttpOutcome.HTTP_ERROR,
            BingoHttpResponse.outcome(403, "text/html", "<html>denied</html>", false),
        )
        assertEquals(
            BingoHttpOutcome.REJECT_NON_JSON,
            BingoHttpResponse.outcome(200, "text/html", "<html>ok</html>", false),
        )
    }

    @Test
    fun statusAndDrawCachedSeparately() {
        val status = BingoStatus(1_000_000.0, Instant.parse("2026-10-11T15:00:00Z"), sunday, null)
        val draw = BingoDrawResult(sunday, validNumbers, null, null)
        val mergedStatus = BingoCachePolicy.mergeStatus(status, status.copy(jackpotEur = null))
        assertEquals(1_000_000.0, mergedStatus.jackpotEur)
        val kept = BingoCachePolicy.upsertDraw(listOf(draw), draw)
        assertEquals(1, kept.size)
        assertEquals(validNumbers, kept.first().winningNumbers)
    }

    @Test
    fun validRefreshUpdatesCachePolicy() {
        val previous = BingoDrawResult(sunday, validNumbers, null, null)
        val nextNumbers = validNumbers.drop(1) + 9
        val incoming = BingoDrawResult(sunday, nextNumbers, null, null)
        assertEquals(nextNumbers, BingoCachePolicy.upsertDraw(listOf(previous), incoming).first().winningNumbers)
    }

    @Test
    fun failedRefreshPreservesCachedStatus() {
        val cached = BingoStatus(1_000_000.0, null, sunday, null)
        val snapshot = HomeBingoRefreshReducer.applyStatusFailure(
            HomeBingoUiSnapshot(status = cached),
            warning = "warn",
            blocking = "block",
        )
        assertEquals(1_000_000.0, snapshot.status?.jackpotEur)
        assertEquals("warn", snapshot.refreshWarning)
        assertNull(snapshot.blockingError)
    }

    @Test
    fun failedRefreshPreservesCachedDraw() {
        val draw = BingoDrawResult(sunday, validNumbers, null, null)
        val snapshot = HomeBingoRefreshReducer.applyDrawFailure(
            HomeBingoUiSnapshot(draw = draw),
            warning = "warn",
            blocking = "block",
        )
        assertEquals(validNumbers, snapshot.draw?.winningNumbers)
        assertEquals("warn", snapshot.refreshWarning)
    }

    @Test
    fun invalidServerDrawDoesNotReplaceCachedDraw() {
        val cached = BingoDrawResult(sunday, validNumbers, null, null)
        val invalid = BingoDrawValidator.validateDraw(sunday, dto(validNumbers.take(10)))
        assertNull(invalid)
        assertEquals(validNumbers, cached.winningNumbers)
    }

    @Test
    fun invalidJackpotDoesNotReplaceCachedJackpot() {
        val cached = BingoStatus(1_000_000.0, null, sunday, null)
        val incoming = BingoStatus(null, Instant.parse("2026-10-11T15:00:00Z"), sunday, null)
        assertEquals(1_000_000.0, BingoCachePolicy.mergeStatus(cached, incoming).jackpotEur)
    }

    @Test
    fun cacheRetainsAtMost60DrawDates() {
        val draws = (0 until 80).map { offset ->
            BingoDrawResult(sunday.minusWeeks(offset.toLong()), validNumbers, null, null)
        }
        assertEquals(60, BingoCachePolicy.retainNewest(draws).size)
        assertEquals(sunday, BingoCachePolicy.retainNewest(draws).first().drawDate)
    }

    @Test
    fun historicalResolverRequestsCorrectSunday() {
        val wednesday = Instant.parse("2026-10-07T12:00:00Z").toEpochMilli()
        assertEquals(sunday, BingoBerlinDates.sundayContainingMillis(wednesday))
    }

    @Test
    fun historicalResolverNeverSubstitutesLatest() {
        val requested = LocalDate.of(2026, 9, 27)
        val latest = dto(validNumbers)
        assertNull(BingoDrawValidator.validateDraw(requested, latest))
    }

    @Test
    fun berlinWeekUsesEuropeBerlinNotUtcMidnight() {
        val lateSundayBerlin = Instant.parse("2026-10-04T21:30:00Z")
        assertEquals(sunday, BingoBerlinDates.sundayContaining(lateSundayBerlin))
        val mondayMorningUtc = Instant.parse("2026-10-04T23:30:00Z")
        assertEquals(LocalDate.of(2026, 10, 4), BingoBerlinDates.sundayContaining(mondayMorningUtc))
    }

    @Test
    fun cetNextDrawParsing() {
        val instant = Instant.parse("2026-01-11T16:00:00Z")
        val berlin = instant.atZone(BingoBerlinDates.berlinZone)
        assertEquals(17, berlin.hour)
        assertEquals(ZoneOffset.ofHours(1), berlin.offset)
    }

    @Test
    fun cestNextDrawParsing() {
        val instant = Instant.parse("2026-10-11T15:00:00Z")
        val berlin = instant.atZone(BingoBerlinDates.berlinZone)
        assertEquals(17, berlin.hour)
        assertEquals(ZoneOffset.ofHours(2), berlin.offset)
    }

    @Test
    fun homeShowsJackpotWhenDrawFetchFails() {
        val status = BingoStatus(1_000_000.0, Instant.parse("2026-10-11T15:00:00Z"), sunday, null)
        val snapshot = HomeBingoRefreshReducer.applyDrawFailure(
            HomeBingoRefreshReducer.applyStatusSuccess(HomeBingoUiSnapshot(), status),
            warning = "warn",
            blocking = "block",
        )
        assertEquals(1_000_000.0, snapshot.status?.jackpotEur)
        assertNull(snapshot.blockingError)
    }

    @Test
    fun homeShowsCachedNumbersWhenStatusFetchFails() {
        val draw = BingoDrawResult(sunday, validNumbers, null, null)
        val snapshot = HomeBingoRefreshReducer.applyStatusFailure(
            HomeBingoUiSnapshot(draw = draw),
            warning = "warn",
            blocking = "block",
        )
        assertEquals(validNumbers, snapshot.draw?.winningNumbers)
        assertTrue(snapshot.hasDisplayableCache)
    }

    @Test
    fun ticketResolverUsesInjectedSundayAndDoesNotUseLatest() {
        val requested = LocalDate.of(2026, 9, 27)
        val historical = BingoDrawResult(requested, validNumbers.take(21) + 11, null, null)
        val latest = BingoDrawResult(sunday, validNumbers, null, null)
        var fetched: LocalDate? = null
        val result = runBlocking {
            TicketCalledNumbersResolver.forOfflineTicket(
                testDateMillis = Instant.parse("2026-09-30T10:00:00Z").toEpochMilli(),
                archivedNumbers = emptyList(),
                fetchDraw = { date ->
                    fetched = date
                    if (date == requested) kotlin.Result.success(historical) else kotlin.Result.success(latest)
                },
            )
        }
        assertEquals(requested, fetched)
        assertEquals(historical.winningNumbers, result.calledNumbers)
    }

    private fun dto(numbers: List<Int>) = BingoDrawResultDto(
        drawDate = "2026-10-04",
        winningNumbers = numbers,
    )

    private fun drawJson(numbers: List<Int>): String =
        """{"draw_date":"2026-10-04","winning_numbers":[${numbers.joinToString(",")}],"source_updated_at":"2026-10-05T15:29:34Z","fetched_at":"2026-10-06T22:30:09Z"}"""
}
