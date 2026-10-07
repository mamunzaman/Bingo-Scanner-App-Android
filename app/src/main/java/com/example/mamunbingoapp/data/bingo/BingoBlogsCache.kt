package com.example.mamunbingoapp.data.bingo

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.bingoBlogsCacheDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "bingo_blogs_cache",
)

@Serializable
internal data class CachedBingoStatusDto(
    val jackpotEur: Double? = null,
    val nextDrawAt: String? = null,
    val latestDrawDate: String? = null,
    val checkedAt: String? = null,
)

@Serializable
internal data class CachedBingoDrawDto(
    val drawDate: String,
    val winningNumbers: List<Int> = emptyList(),
    val sourceUpdatedAt: String? = null,
    val fetchedAt: String? = null,
)

@Serializable
internal data class CachedBingoDrawsPayload(
    val draws: List<CachedBingoDrawDto> = emptyList(),
)

object BingoBlogsCache {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    private val statusKey = stringPreferencesKey("bingo_status_payload")
    private val drawsKey = stringPreferencesKey("bingo_draws_payload")
    private val writeMutex = Mutex()
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    suspend fun readStatus(): BingoStatus? {
        val raw = store().data.first()[statusKey]?.trim().orEmpty()
        if (raw.isBlank()) return null
        val dto = runCatching { json.decodeFromString<CachedBingoStatusDto>(raw) }.getOrNull()
            ?: return null
        return BingoDrawValidator.validateStatus(
            com.example.mamunbingoapp.data.remote.BingoStatusDto(
                currentJackpotEur = dto.jackpotEur,
                nextDrawAt = dto.nextDrawAt,
                latestDrawDate = dto.latestDrawDate,
                checkedAt = dto.checkedAt,
            ),
        )
    }

    suspend fun writeStatus(status: BingoStatus) {
        val payload = CachedBingoStatusDto(
            jackpotEur = status.jackpotEur,
            nextDrawAt = status.nextDrawAt?.toString(),
            latestDrawDate = status.latestDrawDate?.let(BingoBerlinDates::formatIsoDate),
            checkedAt = status.checkedAt?.toString(),
        )
        writeMutex.withLock {
            store().edit { prefs ->
                prefs[statusKey] = json.encodeToString(payload)
            }
        }
    }

    suspend fun readDraw(date: LocalDate): BingoDrawResult? =
        readDraws().firstOrNull { it.drawDate == date }

    suspend fun readDraws(): List<BingoDrawResult> {
        val raw = store().data.first()[drawsKey]?.trim().orEmpty()
        if (raw.isBlank()) return emptyList()
        val payload = runCatching { json.decodeFromString<CachedBingoDrawsPayload>(raw) }.getOrNull()
            ?: return emptyList()
        return payload.draws.mapNotNull { dto ->
            val date = BingoBerlinDates.parseIsoDate(dto.drawDate) ?: return@mapNotNull null
            BingoDrawValidator.validateDraw(
                requestedDate = date,
                dto = com.example.mamunbingoapp.data.remote.BingoDrawResultDto(
                    drawDate = dto.drawDate,
                    winningNumbers = dto.winningNumbers,
                    sourceUpdatedAt = dto.sourceUpdatedAt,
                    fetchedAt = dto.fetchedAt,
                ),
            )
        }.let(BingoCachePolicy::retainNewest)
    }

    suspend fun writeDraw(draw: BingoDrawResult) {
        writeMutex.withLock {
            val merged = BingoCachePolicy.upsertDraw(readDrawsUnlocked(), draw)
            store().edit { prefs ->
                prefs[drawsKey] = json.encodeToString(
                    CachedBingoDrawsPayload(draws = merged.map { it.toCachedDto() }),
                )
            }
        }
    }

    private suspend fun readDrawsUnlocked(): List<BingoDrawResult> = readDraws()

    private fun BingoDrawResult.toCachedDto(): CachedBingoDrawDto = CachedBingoDrawDto(
        drawDate = BingoBerlinDates.formatIsoDate(drawDate),
        winningNumbers = winningNumbers,
        sourceUpdatedAt = sourceUpdatedAt?.toString(),
        fetchedAt = fetchedAt?.toString(),
    )

    private fun store(): DataStore<Preferences> = checkNotNull(appContext) {
        "BingoBlogsCache not initialized. Call BingoRemoteRepository.init(context) from MainActivity."
    }.bingoBlogsCacheDataStore
}
