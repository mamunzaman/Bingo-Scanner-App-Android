package com.example.mamunbingoapp.scanner

import com.example.mamunbingoapp.domain.qr.CalledNumbersQrCodec
import com.example.mamunbingoapp.domain.qr.QrTicketCodec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalledNumbersQrParserTest {
    @Test
    fun validCalledNumbersJson_roundTrip() {
        val encoded = CalledNumbersQrCodec.encode(listOf(7, 22, 61))
        val parsed = CalledNumbersQrParser.parseDetailed(encoded)
        assertEquals(listOf(7, 22, 61), parsed?.numbers)
        assertEquals(0, parsed?.rejectedCount)
        assertEquals(0, parsed?.duplicateCount)
    }

    @Test
    fun duplicatesAndOutOfRange_areCountedAndDropped() {
        val parsed = CalledNumbersQrParser.parseDetailed(
            """{"type":"called_numbers","numbers":[4,4,80,0,19]}""",
        )
        assertEquals(listOf(4, 19), parsed?.numbers)
        assertEquals(2, parsed?.rejectedCount)
        assertEquals(1, parsed?.duplicateCount)
        assertEquals(listOf(4, 19), parsed?.sortedNumbers)
    }

    @Test
    fun jsonWithWrongType_isRejected() {
        assertNull(
            CalledNumbersQrParser.parse("""{"type":"bingo_ticket","numbers":[1,2,3]}"""),
        )
    }

    @Test
    fun ticketDeepLinkAndPrefix_areRejectedWithoutDigitFallback() {
        val deepLink = "https://bingoapp.itconsultingfirma.com/import-ticket?data=abc12"
        val prefix = "${QrTicketCodec.PREFIX}12,15,22"
        assertTrue(CalledNumbersQrParser.isRejectedStructuredPayload(deepLink))
        assertTrue(CalledNumbersQrParser.isRejectedStructuredPayload(prefix))
        assertNull(CalledNumbersQrParser.parse(deepLink))
        assertNull(CalledNumbersQrParser.parse(prefix))
        assertNull(CalledNumbersQrParser.parse("mamunbingo://import-ticket?data=12"))
    }

    @Test
    fun validCsvAndJsonArray_areAccepted() {
        assertEquals(listOf(5, 16, 33), CalledNumbersQrParser.parse("5, 16, 33"))
        assertEquals(listOf(8, 21), CalledNumbersQrParser.parse("[8,21]"))
    }
}
