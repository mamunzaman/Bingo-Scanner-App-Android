package com.example.mamunbingoapp.domain.qr

import com.example.mamunbingoapp.scanner.CalledNumbersQrParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalledNumbersQrCodecTest {
    @Test
    fun normalize_keepsUniqueValuesFrom1To75() {
        assertEquals(
            listOf(1, 15, 75),
            CalledNumbersQrCodec.normalize(listOf(1, 15, 15, 0, 76, 75, 1)),
        )
    }

    @Test
    fun encode_usesNormalizedCalledNumbersJson() {
        val encoded = CalledNumbersQrCodec.encode(listOf(12, 12, 80, 4))
        assertTrue(encoded.contains("\"type\":\"called_numbers\""))
        assertEquals(listOf(12, 4), CalledNumbersQrParser.parse(encoded))
    }

    @Test
    fun validCalledNumbersJson_roundTrips() {
        val source = listOf(3, 18, 42)
        val encoded = CalledNumbersQrCodec.encode(source)
        assertEquals(source, CalledNumbersQrParser.parse(encoded))
    }
}
