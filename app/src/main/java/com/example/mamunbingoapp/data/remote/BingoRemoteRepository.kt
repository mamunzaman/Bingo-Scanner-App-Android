package com.example.mamunbingoapp.data.remote

import android.util.Log
import com.example.mamunbingoapp.BuildConfig
import com.example.mamunbingoapp.data.auth.SupabaseClientProvider
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

object BingoRemoteRepository {

    private const val TAG = "BingoRemoteRepository"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val http: HttpClient by lazy {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(json)
            }
        }
    }

    suspend fun getDrawForWeekContaining(dateMillis: Long): Result<BingoDrawDto> = runCatching {
        SupabaseClientProvider.requireConfigured()
        val zone = ZoneId.systemDefault()
        val selectedDate = Instant.ofEpochMilli(dateMillis).atZone(zone).toLocalDate()
        val exactDate = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val weekStart = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekEnd = weekStart.plusDays(6)
        val weekStartStr = weekStart.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val weekEndStr = weekEnd.format(DateTimeFormatter.ISO_LOCAL_DATE)

        val exactMatch: List<BingoDrawDto> = authorizedGet("bingo_draws") {
            parameter("select", "*")
            parameter("draw_date", "eq.$exactDate")
            parameter("limit", "1")
        }
        if (exactMatch.isNotEmpty()) return@runCatching exactMatch.first()

        val weekMatch: List<BingoDrawDto> = authorizedGet("bingo_draws") {
            parameter("select", "*")
            parameter("and", "(draw_date.gte.$weekStartStr,draw_date.lte.$weekEndStr)")
            parameter("order", "draw_date.desc")
            parameter("limit", "1")
        }
        weekMatch.firstOrNull() ?: throw NoDrawForWeekException()
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = { error ->
            Log.w(TAG, "getDrawForWeekContaining failed dateMillis=$dateMillis", error)
            Result.failure(
                if (error is NoDrawForWeekException) error
                else IllegalStateException(mapRemoteError(error)),
            )
        },
    )

    suspend fun getLatestDraw(): Result<BingoDrawDto> = runCatching {
        SupabaseClientProvider.requireConfigured()
        val rows: List<BingoDrawDto> = authorizedGet("bingo_draws") {
            parameter("select", "*")
            parameter("order", "draw_date.desc")
            parameter("limit", "1")
        }
        val draw = rows.firstOrNull() ?: error("No bingo draw found.")
        if (BuildConfig.DEBUG) {
            Log.d(
                TAG,
                "getLatestDraw ok drawDate=${draw.drawDate} jackpot=${draw.jackpot} " +
                    "numbers=${draw.winningNumbers.size} updatedAt=${draw.updatedAt}",
            )
        }
        draw
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = { error ->
            Log.w(TAG, "getLatestDraw failed", error)
            Result.failure(IllegalStateException(mapRemoteError(error)))
        },
    )

    suspend fun getPrizesForDraw(drawId: String): Result<List<BingoPrizeDto>> = runCatching {
        val id = drawId.trim()
        if (id.isBlank()) error("Draw id is required.")
        SupabaseClientProvider.requireConfigured()
        val prizes: List<BingoPrizeDto> = authorizedGet("bingo_prizes") {
            parameter("select", "*")
            parameter("draw_id", "eq.$id")
            parameter("order", "winning_class.asc")
        }
        prizes
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = { error ->
            Log.w(TAG, "getPrizesForDraw failed drawId=$drawId", error)
            Result.failure(IllegalStateException(mapRemoteError(error)))
        },
    )

    private suspend inline fun <reified T> authorizedGet(
        table: String,
        crossinline block: io.ktor.client.request.HttpRequestBuilder.() -> Unit = {},
    ): T {
        val anonKey = BuildConfig.SUPABASE_ANON_KEY.trim()
        val baseUrl = BuildConfig.SUPABASE_URL.trim().trimEnd('/')
        return http.get("$baseUrl/rest/v1/$table") {
            header(HttpHeaders.Accept, "application/json")
            header("apikey", anonKey)
            header(HttpHeaders.Authorization, "Bearer $anonKey")
            block()
        }.body()
    }

    private fun mapRemoteError(error: Throwable): String = when (error) {
        is IllegalStateException -> error.message.orEmpty().ifBlank { DEFAULT_ERROR }
        else -> error.message?.takeIf { it.isNotBlank() } ?: DEFAULT_ERROR
    }

    private const val DEFAULT_ERROR = "Could not load bingo draw data."
}
