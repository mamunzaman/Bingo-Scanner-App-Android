package com.example.mamunbingoapp.domain.qr

import android.util.Base64
import com.example.mamunbingoapp.domain.model.QrTicketPayload
import java.nio.charset.StandardCharsets
import kotlinx.serialization.json.Json

object QrTicketCodec {
    const val PREFIX = "MAMUN_BINGO_TICKET:"

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    private const val SUPPORTED_VERSION = 1
    private const val EXPECTED_TYPE = "bingo_ticket"

    private val base64Flags: Int
        get() = Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING

    fun encode(payload: QrTicketPayload): String {
        requireValidPayloadOrThrow(payload)
        val body = json.encodeToString(QrTicketPayload.serializer(), payload)
        val b64 = Base64.encodeToString(
            body.toByteArray(StandardCharsets.UTF_8),
            base64Flags
        )
        return "$PREFIX$b64"
    }

    fun decode(raw: String): Result<QrTicketPayload> = runCatching {
        if (!raw.startsWith(PREFIX)) {
            error("expected prefix $PREFIX")
        }
        val b64 = raw.removePrefix(PREFIX)
        if (b64.isBlank()) {
            error("empty payload")
        }
        val decoded = Base64.decode(b64, base64Flags)
        val text = String(decoded, StandardCharsets.UTF_8)
        val payload = json.decodeFromString(QrTicketPayload.serializer(), text)
        if (payload.v != SUPPORTED_VERSION) {
            error("unsupported version: ${payload.v}")
        }
        if (payload.type != EXPECTED_TYPE) {
            error("invalid type: ${payload.type}")
        }
        if (!isValidGrid5x5(payload.grid)) {
            error("grid must be 5x5")
        }
        payload
    }

    private fun requireValidPayloadOrThrow(payload: QrTicketPayload) {
        if (payload.v != SUPPORTED_VERSION) {
            throw IllegalArgumentException("unsupported version: ${payload.v}")
        }
        if (payload.type != EXPECTED_TYPE) {
            throw IllegalArgumentException("invalid type: ${payload.type}")
        }
        if (!isValidGrid5x5(payload.grid)) {
            throw IllegalArgumentException("grid must be 5x5")
        }
    }

    private fun isValidGrid5x5(grid: List<List<String>>): Boolean {
        if (grid.size != 5) return false
        return grid.all { row -> row.size == 5 }
    }

    /** Row-major 25 values for manual entry; blank/non-numeric/out-of-range → 0. */
    fun rowMajorFromQrGrid5x5(grid: List<List<String>>): List<Int> {
        require(isValidGrid5x5(grid)) { "grid must be 5x5" }
        return grid.flatMap { row -> row.toList() }.map { cell ->
            val t = cell.trim()
            if (t.isEmpty()) 0
            else t.toIntOrNull()?.takeIf { n -> n in 1..75 } ?: 0
        }
    }
}
