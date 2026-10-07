package com.example.mamunbingoapp.data.remote

import android.util.Log
import com.example.mamunbingoapp.BuildConfig
import com.example.mamunbingoapp.data.bingo.BingoBerlinDates
import com.example.mamunbingoapp.data.bingo.BingoBlogsCache
import com.example.mamunbingoapp.data.bingo.BingoCachePolicy
import com.example.mamunbingoapp.data.bingo.BingoDrawNotFoundException
import com.example.mamunbingoapp.data.bingo.BingoDrawResult
import com.example.mamunbingoapp.data.bingo.BingoDrawValidator
import com.example.mamunbingoapp.data.bingo.BingoHttpOutcome
import com.example.mamunbingoapp.data.bingo.BingoHttpResponse
import com.example.mamunbingoapp.data.bingo.BingoRemoteException
import com.example.mamunbingoapp.data.bingo.BingoStatus
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import java.io.IOException
import java.net.SocketTimeoutException
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient

object BingoRemoteRepository {
    private const val TAG = "BingoRemoteRepository"
    private const val STATUS_URL = "https://blogs.bingo-hub.de/api/bingo/status"
    private const val DRAWS_URL_PREFIX = "https://blogs.bingo-hub.de/api/bingo/draws/"
    private val USER_AGENT = "Mozilla/5.0 (Linux; Android) BingoApp/${BuildConfig.VERSION_NAME}"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val refreshMutex = Mutex()

    private val http: HttpClient by lazy {
        val okHttp = OkHttpClient.Builder()
            .cache(null)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .build()
        HttpClient(OkHttp) {
            expectSuccess = false
            engine { preconfigured = okHttp }
        }
    }

    fun init(context: android.content.Context) {
        BingoBlogsCache.init(context)
    }

    suspend fun loadCachedStatus(): BingoStatus? = BingoBlogsCache.readStatus()

    suspend fun loadCachedDraw(date: LocalDate): BingoDrawResult? = BingoBlogsCache.readDraw(date)

    suspend fun fetchStatus(): Result<BingoStatus> = successOrMappedFailure(
        runCatching {
            val dto = getJson<BingoStatusDto>(STATUS_URL, "fetchStatus", drawNotFoundOn404 = false)
            val validated = BingoDrawValidator.validateStatus(dto)
                ?: throw BingoRemoteException.InvalidStatus()
            val merged = BingoCachePolicy.mergeStatus(BingoBlogsCache.readStatus(), validated)
            BingoBlogsCache.writeStatus(merged)
            merged
        },
    )

    suspend fun fetchDraw(date: LocalDate): Result<BingoDrawResult> = successOrMappedFailure(
        runCatching {
            val url = "$DRAWS_URL_PREFIX${BingoBerlinDates.formatIsoDate(date)}"
            val dto = getJson<BingoDrawResultDto>(url, "fetchDraw", drawNotFoundOn404 = true)
            val validated = BingoDrawValidator.validateDraw(date, dto)
                ?: throw BingoRemoteException.InvalidDraw()
            BingoBlogsCache.writeDraw(validated)
            validated
        },
    )

    suspend fun refreshLatest(): Result<Pair<BingoStatus?, BingoDrawResult?>> =
        refreshMutex.withLock {
            val statusResult = fetchStatus()
            val status = statusResult.getOrNull()
            val drawDate = status?.latestDrawDate
            val drawResult = if (drawDate != null) fetchDraw(drawDate) else null
            when {
                statusResult.isFailure && drawResult?.isFailure != false &&
                    BingoBlogsCache.readStatus() == null &&
                    (drawDate == null || BingoBlogsCache.readDraw(drawDate) == null) ->
                    Result.failure(statusResult.exceptionOrNull() ?: BingoRemoteException.NetworkUnavailable())
                else -> Result.success(status to drawResult?.getOrNull())
            }
        }

    suspend fun getDrawForWeekContaining(dateMillis: Long): Result<BingoDrawResult> {
        val sunday = BingoBerlinDates.sundayContainingMillis(dateMillis)
        BingoBlogsCache.readDraw(sunday)?.let { return Result.success(it) }
        return fetchDraw(sunday)
    }

    private inline fun <T> successOrMappedFailure(result: Result<T>): Result<T> =
        result.fold(
            onSuccess = { Result.success(it) },
            onFailure = { error ->
                Log.w(TAG, "bingo request failed: ${error::class.java.simpleName}")
                Result.failure(mapError(error))
            },
        )

    private suspend inline fun <reified T> getJson(
        url: String,
        requestTag: String,
        drawNotFoundOn404: Boolean,
    ): T {
        val response: HttpResponse = try {
            http.get(url) {
                header(HttpHeaders.Accept, "application/json")
                header(HttpHeaders.UserAgent, USER_AGENT)
            }
        } catch (error: SocketTimeoutException) {
            throw BingoRemoteException.Timeout()
        } catch (error: IOException) {
            throw BingoRemoteException.NetworkUnavailable()
        }
        val statusCode = response.status.value
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "$requestTag status=$statusCode contentType=${response.contentType()}")
        }
        val peek = response.bodyAsText()
        when (
            BingoHttpResponse.outcome(
                statusCode = statusCode,
                contentType = response.contentType()?.withoutParameters()?.toString(),
                bodyStart = peek.take(32),
                drawNotFoundOn404 = drawNotFoundOn404,
            )
        ) {
            BingoHttpOutcome.DRAW_NOT_FOUND -> throw BingoDrawNotFoundException()
            BingoHttpOutcome.HTTP_ERROR -> throw BingoRemoteException.HttpStatus(statusCode)
            BingoHttpOutcome.REJECT_NON_JSON -> {
                if (drawNotFoundOn404) throw BingoRemoteException.InvalidDraw()
                throw BingoRemoteException.InvalidStatus()
            }
            BingoHttpOutcome.DECODE_JSON -> Unit
        }
        val text = peek
        return runCatching { json.decodeFromString<T>(text) }.getOrElse {
            if (T::class == BingoDrawResultDto::class) throw BingoRemoteException.InvalidDraw()
            throw BingoRemoteException.InvalidStatus()
        }
    }

    private fun mapError(error: Throwable): Throwable = when (error) {
        is BingoRemoteException,
        is BingoDrawNotFoundException,
        -> error
        is SocketTimeoutException -> BingoRemoteException.Timeout()
        is IOException -> BingoRemoteException.NetworkUnavailable()
        else -> error
    }
}
