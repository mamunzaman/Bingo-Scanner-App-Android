package com.example.mamunbingoapp.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BingoStatusDto(
    @SerialName("current_jackpot_eur") val currentJackpotEur: Double? = null,
    @SerialName("next_draw_at") val nextDrawAt: String? = null,
    @SerialName("latest_draw_date") val latestDrawDate: String? = null,
    @SerialName("checked_at") val checkedAt: String? = null,
)

@Serializable
data class BingoDrawResultDto(
    @SerialName("draw_date") val drawDate: String,
    @SerialName("winning_numbers") val winningNumbers: List<Int> = emptyList(),
    @SerialName("source_updated_at") val sourceUpdatedAt: String? = null,
    @SerialName("fetched_at") val fetchedAt: String? = null,
)
