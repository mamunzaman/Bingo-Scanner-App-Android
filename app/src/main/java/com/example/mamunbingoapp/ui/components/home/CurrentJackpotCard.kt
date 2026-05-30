package com.example.mamunbingoapp.ui.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.stringResource
import com.example.mamunbingoapp.R
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mamunbingoapp.data.remote.BingoDrawDto
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.DarkPrimary
import com.example.mamunbingoapp.theme.Primary
import com.example.mamunbingoapp.theme.PrimaryDark
import com.example.mamunbingoapp.ui.components.iosElevatedShadow
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlinx.coroutines.delay

private val berlinZone: ZoneId = ZoneId.of("Europe/Berlin")
private val cardOnGreen = Color.White
private val cardLabelMuted = Color.White.copy(alpha = 0.72f)
private val cardBodySubtle = Color.White.copy(alpha = 0.58f)
private val cardDivider = Color.White.copy(alpha = 0.24f)
private val chipBorderBright = Color.White.copy(alpha = 0.55f)
private val chipInnerGlowCenter = Color.White.copy(alpha = 0.22f)
private val chipInnerGlowEdge = Color.White.copy(alpha = 0.05f)

private const val PREVIEW_CHIP_COUNT = 6
private val compactScanButtonHeight = 40.dp
private val previewChipSize = 26.dp

private val softTextShadow = Shadow(
    color = Color.Black.copy(alpha = 0.22f),
    offset = Offset(0f, 1.5f),
    blurRadius = 6f,
)

@Composable
fun CurrentJackpotCard(
    latestDraw: BingoDrawDto?,
    isRemoteLoading: Boolean,
    remoteError: String?,
    onScanClick: () -> Unit,
    onLatestNumbersClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val countdown = rememberSundayDrawCountdown()
    val jackpotText = latestDraw?.jackpot?.let(::formatEuroJackpot)
    val numbers = latestDraw?.winningNumbers.orEmpty()
    val shape = RoundedCornerShape(Dimens.radiusLarge)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .iosElevatedShadow(elevation = 6.dp, shape = shape)
            .clip(shape)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(PrimaryDark, Primary, DarkPrimary),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .size(120.dp)
                .offset(x = (-36).dp, y = (-32).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f)),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(96.dp)
                .offset(x = 40.dp, y = (-28).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f)),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(Dimens.spacing14),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = Dimens.spacing8),
            ) {
                GreenCardText(
                    text = stringResource(R.string.jackpot_card_current_label),
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 0.8.sp,
                        shadow = softTextShadow,
                    ),
                    fontWeight = FontWeight.SemiBold,
                    color = cardLabelMuted,
                )
                Spacer(modifier = Modifier.height(Dimens.spacing8))
                if (isRemoteLoading && jackpotText == null) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = cardOnGreen,
                        strokeWidth = 2.dp,
                    )
                } else {
                    GreenCardText(
                        text = jackpotText ?: stringResource(R.string.common_em_dash),
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontSize = 24.sp,
                            lineHeight = 28.sp,
                            shadow = softTextShadow,
                        ),
                        fontWeight = FontWeight.Bold,
                        color = cardOnGreen,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(modifier = Modifier.height(Dimens.spacing10))
                Button(
                    onClick = onScanClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(compactScanButtonHeight)
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(Dimens.radiusButtonPill),
                            ambientColor = Color.Black.copy(alpha = 0.1f),
                            spotColor = Color.Black.copy(alpha = 0.14f),
                        ),
                    shape = RoundedCornerShape(Dimens.radiusButtonPill),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Primary,
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 4.dp,
                    ),
                    contentPadding = PaddingValues(
                        horizontal = Dimens.spacing12,
                        vertical = 0.dp,
                    ),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.QrCodeScanner,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(Dimens.spacing8))
                        Text(
                            text = stringResource(R.string.home_scan_ticket),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(cardDivider),
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        start = Dimens.spacing12,
                        end = Dimens.spacing4,
                        bottom = Dimens.spacing4,
                    )
                    .then(
                        if (numbers.isNotEmpty()) {
                            Modifier.clickable(onClick = onLatestNumbersClick)
                        } else {
                            Modifier
                        },
                    ),
            ) {
                GreenCardText(
                    text = stringResource(R.string.jackpot_card_next_draw_label),
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 0.8.sp,
                        shadow = softTextShadow,
                    ),
                    fontWeight = FontWeight.SemiBold,
                    color = cardLabelMuted,
                )
                Spacer(modifier = Modifier.height(Dimens.spacing4))
                GreenCardText(
                    text = countdown,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 20.sp,
                        lineHeight = 24.sp,
                        shadow = softTextShadow,
                    ),
                    fontWeight = FontWeight.Bold,
                    color = cardOnGreen,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                GreenCardText(
                    text = stringResource(R.string.jackpot_card_draw_schedule),
                    style = MaterialTheme.typography.labelSmall.copy(shadow = softTextShadow),
                    fontWeight = FontWeight.Normal,
                    color = cardBodySubtle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(Dimens.spacing8))
                GreenCardText(
                    text = stringResource(R.string.jackpot_card_latest_numbers_label),
                    style = MaterialTheme.typography.labelSmall.copy(shadow = softTextShadow),
                    fontWeight = FontWeight.SemiBold,
                    color = cardLabelMuted,
                )
                Spacer(modifier = Modifier.height(Dimens.spacing8))
                when {
                    isRemoteLoading && numbers.isEmpty() -> {
                        GreenCardText(
                            text = stringResource(R.string.common_loading),
                            style = MaterialTheme.typography.labelSmall.copy(shadow = softTextShadow),
                            fontWeight = FontWeight.Normal,
                            color = cardBodySubtle,
                        )
                    }
                    numbers.isNotEmpty() -> {
                        CurrentJackpotNumberChipsPreview(numbers = numbers)
                        Spacer(modifier = Modifier.height(Dimens.spacing4))
                    }
                    !remoteError.isNullOrBlank() -> {
                        GreenCardText(
                            text = remoteError,
                            style = MaterialTheme.typography.labelSmall.copy(shadow = softTextShadow),
                            fontWeight = FontWeight.Normal,
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GreenCardText(
    text: String,
    style: TextStyle,
    fontWeight: FontWeight,
    color: Color,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        text = text,
        modifier = modifier,
        style = style.copy(fontWeight = fontWeight),
        color = color,
        maxLines = maxLines,
        overflow = overflow,
    )
}

@Composable
private fun CurrentJackpotNumberChipsPreview(numbers: List<Int>) {
    val preview = numbers.take(PREVIEW_CHIP_COUNT)
    val overflow = numbers.size - preview.size
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = Dimens.spacing4, bottom = Dimens.spacing4)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing10),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        preview.forEach { number ->
            JackpotPreviewChip(text = number.toString())
        }
        if (overflow > 0) {
            JackpotPreviewChip(text = "+$overflow")
        }
    }
}

@Composable
private fun JackpotPreviewChip(text: String) {
    Box(
        modifier = Modifier
            .size(previewChipSize)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(chipInnerGlowCenter, chipInnerGlowEdge),
                ),
            )
            .border(width = 1.dp, color = chipBorderBright, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                shadow = softTextShadow,
            ),
            color = cardOnGreen,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

private fun nextSundayDrawBerlin(from: ZonedDateTime = ZonedDateTime.now(berlinZone)): ZonedDateTime {
    var target = from.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        .withHour(17)
        .withMinute(0)
        .withSecond(0)
        .withNano(0)
    if (!target.isAfter(from)) {
        target = target.plusWeeks(1)
    }
    return target
}

private fun formatDrawCountdown(remaining: Duration): String {
    val totalSeconds = remaining.seconds.coerceAtLeast(0)
    val days = totalSeconds / 86_400
    val hours = (totalSeconds % 86_400) / 3_600
    val minutes = (totalSeconds % 3_600) / 60
    val seconds = totalSeconds % 60
    return when {
        days > 0 -> "${days}d ${hours}h ${minutes}m"
        hours > 0 -> "${hours}h ${minutes}m ${seconds}s"
        else -> "${minutes}m ${seconds}s"
    }
}

@Composable
private fun rememberSundayDrawCountdown(): String {
    var countdown by remember {
        mutableStateOf(formatDrawCountdown(Duration.between(ZonedDateTime.now(berlinZone), nextSundayDrawBerlin())))
    }
    LaunchedEffect(Unit) {
        while (true) {
            val now = ZonedDateTime.now(berlinZone)
            countdown = formatDrawCountdown(Duration.between(now, nextSundayDrawBerlin(now)))
            delay(1_000L)
        }
    }
    return countdown
}

private fun formatEuroJackpot(amount: Long): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.GERMANY)
    formatter.maximumFractionDigits = 0
    return formatter.format(amount)
}
