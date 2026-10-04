package com.example.mamunbingoapp.scanner

import com.example.mamunbingoapp.domain.qr.CalledNumbersQrCodec
import com.example.mamunbingoapp.domain.qr.QrTicketCodec
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

data class CalledNumbersQrParseResult(
    val numbers: List<Int>,
    val rejectedCount: Int,
    val duplicateCount: Int,
) {
    val sortedNumbers: List<Int> get() = numbers.sorted()
}

object CalledNumbersQrParser {
    private val json = Json { ignoreUnknownKeys = true }

    fun parse(raw: String): List<Int>? =
        parseDetailed(raw)?.numbers?.takeIf { it.isNotEmpty() }

    fun parseDetailed(raw: String): CalledNumbersQrParseResult? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return null
        if (isRejectedStructuredPayload(trimmed)) return null
        return if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            parseJson(trimmed)
        } else {
            parsePlainList(trimmed)
        }
    }

    fun isRejectedStructuredPayload(raw: String): Boolean {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return false
        if (trimmed.startsWith(QrTicketCodec.PREFIX, ignoreCase = true)) return true
        val lower = trimmed.lowercase()
        if (lower.contains("import-ticket")) return true
        return looksLikeUrl(trimmed)
    }

    private fun looksLikeUrl(raw: String): Boolean {
        val lower = raw.lowercase()
        return lower.contains("://") ||
            lower.startsWith("http:") ||
            lower.startsWith("https:") ||
            lower.startsWith("intent:") ||
            lower.startsWith("mamunbingo:")
    }

    private fun parseJson(raw: String): CalledNumbersQrParseResult? {
        val element = runCatching { json.parseToJsonElement(raw) }.getOrNull() ?: return null
        val candidates = when (element) {
            is JsonArray -> numbersFromJsonArray(element) ?: return null
            is JsonObject -> {
                val type = runCatching { element["type"]?.jsonPrimitive?.content }.getOrNull()
                if (type != null && type != CalledNumbersQrCodec.PAYLOAD_TYPE) return null
                val keys = listOf("numbers", "calledNumbers", "calls", "called")
                keys.firstNotNullOfOrNull { key ->
                    element[key]?.let { child ->
                        if (child is JsonArray) numbersFromJsonArray(child) else null
                    }
                } ?: return null
            }
            else -> return null
        }
        return summarize(candidates)
    }

    private fun parsePlainList(raw: String): CalledNumbersQrParseResult? {
        val delimited = raw.split(',', ';', '|', ' ', '\n', '\r', '\t')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { token -> token.toIntOrNull() }
        if (delimited.isEmpty()) return null
        return summarize(delimited)
    }

    private fun numbersFromJsonArray(array: JsonArray): List<Int>? {
        val nums = array.mapNotNull { element -> intFromJsonElement(element) }
        return nums.takeIf { it.isNotEmpty() }
    }

    private fun intFromJsonElement(element: JsonElement): Int? {
        return runCatching { element.jsonPrimitive.intOrNull }.getOrNull()
            ?: runCatching { element.jsonPrimitive.content.toIntOrNull() }.getOrNull()
    }

    private fun summarize(numbers: List<Int>): CalledNumbersQrParseResult? {
        val result = mutableListOf<Int>()
        val seen = mutableSetOf<Int>()
        var rejected = 0
        var duplicates = 0
        for (number in numbers) {
            if (number !in 1..75) {
                rejected++
                continue
            }
            if (number in seen) {
                duplicates++
                continue
            }
            seen.add(number)
            result.add(number)
        }
        if (result.isEmpty()) return null
        return CalledNumbersQrParseResult(
            numbers = result,
            rejectedCount = rejected,
            duplicateCount = duplicates,
        )
    }
}
