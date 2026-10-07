package com.example.mamunbingoapp.data.bingo

enum class BingoHttpOutcome {
    DECODE_JSON,
    DRAW_NOT_FOUND,
    HTTP_ERROR,
    REJECT_NON_JSON,
}

object BingoHttpResponse {
    fun outcome(
        statusCode: Int,
        contentType: String?,
        bodyStart: String,
        drawNotFoundOn404: Boolean,
    ): BingoHttpOutcome {
        if (statusCode == 404 && drawNotFoundOn404) return BingoHttpOutcome.DRAW_NOT_FOUND
        if (statusCode !in 200..299) return BingoHttpOutcome.HTTP_ERROR
        val type = contentType.orEmpty()
        val looksJson = type.contains("json", ignoreCase = true) ||
            bodyStart.trimStart().startsWith("{")
        if (!looksJson) return BingoHttpOutcome.REJECT_NON_JSON
        return BingoHttpOutcome.DECODE_JSON
    }
}
