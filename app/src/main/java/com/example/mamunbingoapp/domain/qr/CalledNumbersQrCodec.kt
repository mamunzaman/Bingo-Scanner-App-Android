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

    fun encode(calledNumbers: List<Int>): String {
        val payload = CalledNumbersQrPayload(
            type = PAYLOAD_TYPE,
            numbers = calledNumbers,
        )
        return json.encodeToString(CalledNumbersQrPayload.serializer(), payload)
    }
}
