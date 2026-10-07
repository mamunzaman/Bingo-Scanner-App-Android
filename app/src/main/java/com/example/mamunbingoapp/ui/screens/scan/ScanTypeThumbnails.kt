package com.example.mamunbingoapp.ui.screens.scan

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.domain.model.BingoScanType
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.Primary
import com.example.mamunbingoapp.theme.TicketGold
import com.example.mamunbingoapp.theme.TicketPaperTop

private val PlayPaper = Color(0xFFFFF8EC)
private val PlayStrip = Color(0xFFD32F2F)
private val PlayHeaderBlue = Color(0xFF1E5AA8)
private val PlayGridLine = Color(0xFF2B2B2B)
private val PlayAccentGreen = Color(0xFF3E8B2A)
private val PlayCell = Color(0xFFFFFDF8)
private val DigitalDesk = Color(0xFF3A3A3A)
private val DigitalBezel = Color(0xFF2A2A2A)
private val MasterPaper = Color(0xFFF7F4EE)
private val MasterInk = Color(0xFF2C2C2C)

@Composable
fun ScanTypeThumbnail(
    type: BingoScanType,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.radiusSmall))
            .background(TicketPaperTop),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            when (type) {
                BingoScanType.PLAY_PAPER -> drawPlaySheetPreview()
                BingoScanType.ONLINE -> drawDigitalSheetPreview()
                BingoScanType.MAIN_SHEET -> drawMasterSheetPreview()
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPlaySheetPreview() {
    drawRect(Color(0xFFEFE8DC))
    val inset = size.minDimension * 0.07f
    val ticketW = size.width - inset * 2f
    val ticketH = ticketW * 0.58f
    val left = inset
    val top = (size.height - ticketH) / 2f
    val corner = CornerRadius(5.dp.toPx(), 5.dp.toPx())
    drawRoundRect(
        color = PlayPaper,
        topLeft = Offset(left, top),
        size = Size(ticketW, ticketH),
        cornerRadius = corner,
    )
    val stripW = ticketW * 0.10f
    drawRect(PlayStrip, Offset(left, top), Size(stripW, ticketH))
    drawRect(PlayAccentGreen, Offset(left, top + ticketH * 0.72f), Size(stripW, ticketH * 0.28f))
    val gridLeft = left + stripW
    val gridW = ticketW - stripW
    val headerH = ticketH * 0.26f
    drawRect(PlayHeaderBlue, Offset(gridLeft, top), Size(gridW, headerH))
    val colW = gridW / 5f
    for (i in 0..4) {
        val cellPad = colW * 0.12f
        drawRoundRect(
            color = PlayCell,
            topLeft = Offset(gridLeft + colW * i + cellPad, top + headerH * 0.18f),
            size = Size(colW - cellPad * 2f, headerH * 0.64f),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
        )
        val cx = gridLeft + colW * (i + 0.5f)
        val cy = top + headerH * 0.50f
        drawLine(
            PlayHeaderBlue,
            Offset(cx - colW * 0.12f, cy),
            Offset(cx + colW * 0.12f, cy),
            1.6.dp.toPx(),
        )
    }
    val gridTop = top + headerH
    val gridH = ticketH - headerH
    drawRect(PlayCell, Offset(gridLeft, gridTop), Size(gridW, gridH))
    val rows = 5
    val rowH = gridH / rows
    val line = PlayGridLine.copy(alpha = 0.78f)
    for (c in 0..5) {
        drawLine(line, Offset(gridLeft + colW * c, gridTop), Offset(gridLeft + colW * c, top + ticketH), 1.1.dp.toPx())
    }
    for (r in 0..rows) {
        drawLine(line, Offset(gridLeft, gridTop + rowH * r), Offset(left + ticketW, gridTop + rowH * r), 1.1.dp.toPx())
    }
    for (c in 0..4) {
        for (r in 0..4) {
            val cx = gridLeft + colW * (c + 0.5f)
            val cy = gridTop + rowH * (r + 0.5f)
            drawCircle(PlayGridLine.copy(alpha = 0.18f), radius = 1.2.dp.toPx(), center = Offset(cx, cy))
        }
    }
    drawRect(PlayStrip.copy(alpha = 0.18f), Offset(gridLeft, top + ticketH - 3.dp.toPx()), Size(gridW, 3.dp.toPx()))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDigitalSheetPreview() {
    drawRect(DigitalDesk)
    drawRoundRect(
        color = DigitalBezel,
        topLeft = Offset(size.width * 0.12f, size.height * 0.10f),
        size = Size(size.width * 0.76f, size.height * 0.80f),
        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
    )
    val screenL = size.width * 0.16f
    val screenT = size.height * 0.16f
    val screenW = size.width * 0.68f
    val screenH = size.height * 0.68f
    drawRoundRect(
        color = Color(0xFFF4F6F2),
        topLeft = Offset(screenL, screenT),
        size = Size(screenW, screenH),
        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
    )
    val colW = screenW / 5f
    val headerY = screenT + screenH * 0.16f
    for (i in 0..4) {
        drawCircle(
            color = Primary,
            radius = colW * 0.28f,
            center = Offset(screenL + colW * (i + 0.5f), headerY),
        )
    }
    val gridTop = screenT + screenH * 0.32f
    val gridH = screenH * 0.58f
    val line = Primary.copy(alpha = 0.28f)
    for (c in 0..5) {
        drawLine(line, Offset(screenL + colW * c, gridTop), Offset(screenL + colW * c, gridTop + gridH), 1.dp.toPx())
    }
    val rowH = gridH / 5f
    for (r in 0..5) {
        drawLine(line, Offset(screenL, gridTop + rowH * r), Offset(screenL + screenW, gridTop + rowH * r), 1.dp.toPx())
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMasterSheetPreview() {
    drawRect(Color(0xFFE8E4DC))
    val left = size.width * 0.22f
    val top = size.height * 0.06f
    val w = size.width * 0.56f
    val h = size.height * 0.88f
    drawRoundRect(
        color = MasterPaper,
        topLeft = Offset(left, top),
        size = Size(w, h),
        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
    )
    drawRoundRect(
        color = MasterInk,
        topLeft = Offset(left, top),
        size = Size(w, h),
        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
        style = Stroke(width = 1.dp.toPx()),
    )
    val headerH = h * 0.12f
    drawRect(TicketGold.copy(alpha = 0.35f), Offset(left, top), Size(w, headerH))
    val gridTop = top + h * 0.18f
    val gridH = h * 0.48f
    val cols = 5
    val rows = 6
    val colW = w / cols
    val rowH = gridH / rows
    val line = MasterInk.copy(alpha = 0.35f)
    for (c in 0..cols) {
        drawLine(line, Offset(left + colW * c, gridTop), Offset(left + colW * c, gridTop + gridH), 1.dp.toPx())
    }
    for (r in 0..rows) {
        drawLine(line, Offset(left, gridTop + rowH * r), Offset(left + w, gridTop + rowH * r), 1.dp.toPx())
    }
    val qr = w * 0.34f
    val qrL = left + (w - qr) / 2f
    val qrT = top + h * 0.72f
    drawRect(MasterInk.copy(alpha = 0.82f), Offset(qrL, qrT), Size(qr, qr))
    drawRect(MasterPaper, Offset(qrL + qr * 0.18f, qrT + qr * 0.18f), Size(qr * 0.28f, qr * 0.28f))
    drawRect(MasterPaper, Offset(qrL + qr * 0.54f, qrT + qr * 0.54f), Size(qr * 0.22f, qr * 0.22f))
}
