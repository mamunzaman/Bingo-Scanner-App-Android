package com.example.mamunbingoapp.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class BingoDrawDto(
    val id: String,
    @SerialName("draw_date") val drawDate: String,
    val jackpot: Long = 0,
    @SerialName("next_draw_at") val nextDrawAt: String? = null,
    @SerialName("winning_numbers") val winningNumbers: List<Int> = emptyList(),
    @SerialName("game_amount") val gameAmount: Long = 0,
    @SerialName("final_game_amount") val finalGameAmount: Long = 0,
    val superchance: List<SuperchanceCandidateDto> = emptyList(),
    @SerialName("raw_data") val rawData: JsonElement? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class SuperchanceCandidateDto(
    val index: Int = 0,
    @SerialName("serialNumber") val serialNumber: String? = null,
    val ticket: String? = null,
)

@Serializable
data class BingoPrizeDto(
    val id: String? = null,
    @SerialName("draw_id") val drawId: String,
    @SerialName("winning_class") val winningClass: Int,
    val category: String? = null,
    @SerialName("winner_count") val winnerCount: Int = 0,
    val amount: Double = 0.0,
)
