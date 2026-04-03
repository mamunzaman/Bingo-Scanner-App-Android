package com.example.mamunbingoapp.data

data class HistorySession(
    val id: String,
    val title: String,
    val isCompleted: Boolean,
    val sheetsCount: Int,
    val calledCount: Int,
    val calledNumbersPreview: List<String>,
    val calledNumbersFull: List<Int>,
    val sheetsPlayed: List<SheetPlayed>,
    val sheetName: String = "",
    val playedAtMillis: Long? = null,
    val ocrSource: String? = null,
    val ocrConfidence: Float? = null,
    val originalOcrNumbers: String? = null,
    val losNumber: String? = null,
    val serialNumber: String? = null,
) {
    /** Resolved ticket id for room assignment. Live rooms store ticketId only; never use session id for RoomTicketEntity. */
    val ticketId: String get() = sheetsPlayed.firstOrNull()?.ticketId ?: id
    fun effectivePlayedAtMillis(): Long = playedAtMillis ?: System.currentTimeMillis()
    fun effectiveSheetName(): String = sheetName.ifEmpty { title }
}

data class SheetPlayed(
    val ticketId: String,
    val title: String,
    val subtitle: String,
    val markedCount: Int,
    val totalCount: Int
)
