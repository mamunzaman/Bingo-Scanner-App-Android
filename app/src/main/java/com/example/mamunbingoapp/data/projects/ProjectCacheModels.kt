package com.example.mamunbingoapp.data.projects

import com.example.mamunbingoapp.ui.projects.ProjectUiModel
import kotlinx.serialization.Serializable

@Serializable
data class ProjectsCachePayload(
    val updatedAtMillis: Long,
    val projects: List<CachedProjectDto> = emptyList(),
)

@Serializable
data class CachedProjectDto(
    val id: String,
    val title: String,
    val summary: String,
    val imageUrl: String? = null,
    val sourceUrl: String,
    val location: String? = null,
    val projectYear: Int? = null,
    val fundingAmount: String? = null,
    val isFeatured: Boolean = false,
)

internal fun ProjectUiModel.toCachedDto(): CachedProjectDto = CachedProjectDto(
    id = id,
    title = title,
    summary = summary,
    imageUrl = imageUrl,
    sourceUrl = sourceUrl,
    location = location,
    projectYear = projectYear,
    fundingAmount = fundingAmount,
    isFeatured = isFeatured,
)

internal fun CachedProjectDto.toUiModel(): ProjectUiModel = ProjectUiModel(
    id = id,
    title = title,
    summary = summary,
    imageUrl = imageUrl,
    sourceUrl = sourceUrl,
    location = location,
    projectYear = projectYear,
    fundingAmount = fundingAmount,
    isFeatured = isFeatured,
)
