package com.example.mamunbingoapp.domain.qr

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

object QrTicketImageGenerator {

    private const val MIN_SIZE_PX = 32

    fun generateBitmap(
        content: String,
        sizePx: Int = 1024,
    ): Result<Bitmap> = runCatching {
        require(content.isNotBlank()) { "content must not be blank" }
        require(sizePx >= MIN_SIZE_PX) { "sizePx must be at least $MIN_SIZE_PX, was $sizePx" }

        val hints = mapOf(
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.MARGIN to 1,
        )
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(width * height)
        var i = 0
        for (y in 0 until height) {
            for (x in 0 until width) {
                pixels[i++] = if (bitMatrix[x, y]) {
                    0xFF000000.toInt()
                } else {
                    0xFFFFFFFF.toInt()
                }
            }
        }
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        bitmap
    }
}
