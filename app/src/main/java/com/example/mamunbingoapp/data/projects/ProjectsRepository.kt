package com.example.mamunbingoapp.data.projects

import android.util.Log
import com.example.mamunbingoapp.BuildConfig
import com.example.mamunbingoapp.ui.projects.ProjectUiModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import java.text.NumberFormat
import java.util.Locale
import kotlinx.serialization.json.Json

object ProjectsRepository {

    private const val TAG = "ProjectsRepository"
    private const val PROJECTS_URL = "https://blogs.bingo-hub.de/api/projects"
    private val USER_AGENT = "Mozilla/5.0 (Linux; Android) BingoApp/${BuildConfig.VERSION_NAME}"

    data class FetchResult(
        val projects: List<ProjectUiModel>,
        val updatedAtMillis: Long,
    )

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val http: HttpClient by lazy {
        HttpClient(OkHttp) {
            expectSuccess = false
            install(ContentNegotiation) {
                json(json)
            }
        }
    }

    fun init(context: android.content.Context) {
        ProjectsCache.init(context)
    }

    suspend fun loadCachedProjects(): FetchResult? {
        val payload = ProjectsCache.read() ?: return null
        return FetchResult(
            projects = payload.projects.map { it.toUiModel() },
            updatedAtMillis = payload.updatedAtMillis,
        )
    }

    suspend fun fetchProjects(): Result<FetchResult> {
        return try {
            val httpResponse: HttpResponse = http.get(PROJECTS_URL) {
                header(HttpHeaders.Accept, "application/json")
                header(HttpHeaders.UserAgent, USER_AGENT)
            }
            if (!httpResponse.status.isSuccess()) {
                return Result.failure(
                    IllegalStateException(serverStatusError(httpResponse.status.value)),
                )
            }
            val response: ProjectsApiResponse = httpResponse.body()
            val projects = response.data
                .asSequence()
                .mapNotNull { it.toUiModelOrNull() }
                .toList()
            val updatedAtMillis = System.currentTimeMillis()
            ProjectsCache.write(projects, updatedAtMillis)
            Result.success(FetchResult(projects = projects, updatedAtMillis = updatedAtMillis))
        } catch (error: Throwable) {
            Log.w(TAG, "fetchProjects failed", error)
            Result.failure(IllegalStateException(DEFAULT_ERROR))
        }
    }

    private fun ProjectApiDto.toUiModelOrNull(): ProjectUiModel? {
        val titleText = title.trim()
        val source = sourceUrl?.trim().orEmpty()
        if (titleText.isBlank() || source.isBlank()) return null
        return ProjectUiModel(
            id = id.toString(),
            title = titleText,
            summary = summary?.trim().orEmpty(),
            imageUrl = imageUrl?.trim()?.takeIf { it.isNotBlank() },
            sourceUrl = source,
            location = region?.trim()?.takeIf { it.isNotBlank() },
            projectYear = projectYear,
            fundingAmount = formatFundingAmount(fundingAmountEur),
            isFeatured = isFeatured,
        )
    }

    private fun formatFundingAmount(amount: Double?): String? {
        if (amount == null) return null
        return NumberFormat.getNumberInstance(Locale.US).format(amount)
    }

    private fun serverStatusError(statusCode: Int): String =
        "Projects could not be loaded. Server response: $statusCode."

    private const val DEFAULT_ERROR = "Could not load projects."
}
