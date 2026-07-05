package com.example.mamunbingoapp.scanner

import com.example.mamunbingoapp.domain.qr.CalledNumbersQrCodec
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

object CalledNumbersQrParser {
    private val json = Json { ignoreUnknownKeys = true }

    fun parse(raw: String): List<Int>? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return null
        parseJson(trimmed)?.let { return normalize(it) }
        val delimited = trimmed.split(',', ';', '|', ' ', '\n', '\r', '\t')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { token ->
                token.filter(Char::isDigit).toIntOrNull() ?: token.toIntOrNull()
            }
        if (delimited.isNotEmpty()) return normalize(delimited)
        val regexHits = Regex("""\b(\d{1,2})\b""")
            .findAll(trimmed)
            .mapNotNull { it.groupValues[1].toIntOrNull() }
            .toList()
        return normalize(regexHits)
    }

    private fun parseJson(raw: String): List<Int>? {
        if (!raw.startsWith("{") && !raw.startsWith("[")) return null
        return runCatching {
            when (val element = json.parseToJsonElement(raw)) {
                is JsonArray -> numbersFromJsonArray(element)
                is JsonObject -> {
                    val type = element["type"]?.jsonPrimitive?.content
                    if (type != null && type != CalledNumbersQrCodec.PAYLOAD_TYPE) {
                        return@runCatching null
                    }
                    val keys = listOf("numbers", "calledNumbers", "calls", "called")
                    keys.firstNotNullOfOrNull { key ->
                        element[key]?.let { child ->
                            if (child is JsonArray) numbersFromJsonArray(child) else null
                        }
                    }
                }
                else -> null
            }
        }.getOrNull()
    }

    private fun numbersFromJsonArray(array: JsonArray): List<Int>? {
        val nums = array.mapNotNull { element -> intFromJsonElement(element) }
        return nums.takeIf { it.isNotEmpty() }
    }

    private fun intFromJsonElement(element: JsonElement): Int? {
        return runCatching { element.jsonPrimitive.intOrNull }.getOrNull()
            ?: runCatching { element.jsonPrimitive.content.toIntOrNull() }.getOrNull()
    }

    private fun normalize(numbers: List<Int>): List<Int>? {
        val result = mutableListOf<Int>()
        val seen = mutableSetOf<Int>()
        for (number in numbers) {
            if (number !in 1..75 || number in seen) continue
            seen.add(number)
            result.add(number)
        }
        return result.takeIf { it.isNotEmpty() }
    }
}
