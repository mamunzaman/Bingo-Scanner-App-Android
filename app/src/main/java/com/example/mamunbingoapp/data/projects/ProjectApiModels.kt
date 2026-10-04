package com.example.mamunbingoapp.data.projects

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProjectsApiResponse(
    val data: List<ProjectApiDto> = emptyList(),
)

@Serializable
data class ProjectApiDto(
    val id: Int = 0,
    val title: String = "",
    val summary: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("source_url") val sourceUrl: String? = null,
    val region: String? = null,
    @SerialName("project_year") val projectYear: Int? = null,
    @SerialName("funding_amount_eur") val fundingAmountEur: Double? = null,
    @SerialName("is_featured") val isFeatured: Boolean = false,
)
