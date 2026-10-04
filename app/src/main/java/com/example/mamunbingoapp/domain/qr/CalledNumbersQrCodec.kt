package com.example.mamunbingoapp.domain.qr

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class CalledNumbersQrPayload(
    val type: String,
    val numbers: List<Int>,
)

object CalledNumbersQrCodec {
    const val PAYLOAD_TYPE = "called_numbers"

    private val json = Json {
        encodeDefaults = true
        prettyPrint = false
    }

    fun normalize(calledNumbers: List<Int>): List<Int> {
        val seen = LinkedHashSet<Int>()
        for (number in calledNumbers) {
            if (number in 1..75) seen.add(number)
        }
        return seen.toList()
    }

    fun encode(calledNumbers: List<Int>): String {
        val payload = CalledNumbersQrPayload(
            type = PAYLOAD_TYPE,
            numbers = normalize(calledNumbers),
        )
        return json.encodeToString(CalledNumbersQrPayload.serializer(), payload)
    }
}
