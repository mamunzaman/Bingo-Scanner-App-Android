package com.example.mamunbingoapp.data.projects

import android.util.Log
import com.example.mamunbingoapp.ui.projects.ProjectUiModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import java.text.NumberFormat
import java.util.Locale
import kotlinx.serialization.json.Json

object ProjectsRepository {

    private const val TAG = "ProjectsRepository"
    private const val PROJECTS_URL = "https://bingo-hub.de/project-news/api/projects.php"

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

    suspend fun fetchProjects(): Result<FetchResult> = runCatching {
        val response: ProjectsApiResponse = http.get(PROJECTS_URL).body()
        val projects = response.data
            .asSequence()
            .filter { it.isActive }
            .mapNotNull { it.toUiModelOrNull() }
            .toList()
        val updatedAtMillis = System.currentTimeMillis()
        ProjectsCache.write(projects, updatedAtMillis)
        FetchResult(projects = projects, updatedAtMillis = updatedAtMillis)
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = { error ->
            Log.w(TAG, "fetchProjects failed", error)
            Result.failure(
                IllegalStateException(
                    error.message?.takeIf { it.isNotBlank() } ?: DEFAULT_ERROR,
                ),
            )
        },
    )

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
            location = location?.trim()?.takeIf { it.isNotBlank() },
            projectYear = projectYear,
            fundingAmount = formatFundingAmount(fundingAmount),
            isFeatured = isFeatured,
        )
    }

    private fun formatFundingAmount(amount: Double?): String? {
        if (amount == null) return null
        return NumberFormat.getNumberInstance(Locale.US).format(amount)
    }

    private const val DEFAULT_ERROR = "Could not load projects."
}
