package com.example.mamunbingoapp.ui.projects

data class ProjectUiModel(
    val id: String,
    val title: String,
    val summary: String,
    val imageUrl: String?,
    val sourceUrl: String,
    val location: String?,
    val projectYear: Int?,
    val fundingAmount: String?,
    val isFeatured: Boolean = false,
)
