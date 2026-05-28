package com.example.mamunbingoapp.ui.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mamunbingoapp.R
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
private val cardDivider = Color.White.copy(alpha = 0.38f)
private val chipBorderBright = Color.White.copy(alpha = 0.62f)
private val chipInnerGlowCenter = Color.White.copy(alpha = 0.26f)
private val chipInnerGlowEdge = Color.White.copy(alpha = 0.06f)

private val softTextShadow = Shadow(
    color = Color.Black.copy(alpha = 0.22f),
    offset = Offset(0f, 1.5f),
    blurRadius = 6f,
)
private val strongTextShadow = Shadow(
    color = Color.Black.copy(alpha = 0.28f),
    offset = Offset(0f, 2f),
    blurRadius = 8f,
)

@Composable
fun CurrentJackpotCard(
    latestDraw: BingoDrawDto?,
    isRemoteLoading: Boolean,
    remoteError: String?,
    onScanClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val countdown = rememberSundayDrawCountdown()
    val jackpotText = latestDraw?.jackpot?.let(::formatEuroJackpot)
    val shape = RoundedCornerShape(Dimens.radiusXL)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .iosElevatedShadow(elevation = 8.dp, shape = shape)
            .clip(shape)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            PrimaryDark,
                            Primary,
                            DarkPrimary,
                        ),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = (-48).dp, y = (-56).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f)),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(160.dp)
                .offset(x = 56.dp, y = (-40).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.14f)),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(120.dp)
                .offset(x = (-32).dp, y = 48.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f)),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacing24),
        ) {
            GreenCardText(
                text = "CURRENT JACKPOT",
                style = MaterialTheme.typography.labelLarge.copy(
                    letterSpacing = 1.2.sp,
                    shadow = softTextShadow,
                ),
                fontWeight = FontWeight.Medium,
                color = cardLabelMuted,
            )
            Spacer(modifier = Modifier.height(Dimens.spacing10))
            if (isRemoteLoading && jackpotText == null) {
                CircularProgressIndicator(
                    modifier = Modifier.size(44.dp),
                    color = cardOnGreen,
                    strokeWidth = 3.dp,
                )
            } else {
                GreenCardText(
                    text = jackpotText ?: "—",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = 46.sp,
                        lineHeight = 50.sp,
                        shadow = strongTextShadow,
                    ),
                    fontWeight = FontWeight.Black,
                    color = cardOnGreen,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.height(Dimens.spacing20))
            HorizontalDivider(color = cardDivider, thickness = 1.25.dp)
            Spacer(modifier = Modifier.height(Dimens.spacing20))

            NextDrawSection(countdown = countdown)

            when {
                isRemoteLoading -> {
                    Spacer(modifier = Modifier.height(Dimens.spacing12))
                    GreenCardText(
                        text = "Loading latest draw…",
                        style = MaterialTheme.typography.bodySmall.copy(shadow = softTextShadow),
                        fontWeight = FontWeight.Normal,
                        color = cardBodySubtle,
                    )
                }
                !remoteError.isNullOrBlank() -> {
                    Spacer(modifier = Modifier.height(Dimens.spacing12))
                    GreenCardText(
                        text = remoteError,
                        style = MaterialTheme.typography.bodySmall.copy(shadow = softTextShadow),
                        fontWeight = FontWeight.Normal,
                        color = Color.White.copy(alpha = 0.92f),
                    )
                }
            }

            val numbers = latestDraw?.winningNumbers.orEmpty()
            if (numbers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Dimens.spacing20))
                GreenCardText(
                    text = "Latest numbers",
                    style = MaterialTheme.typography.labelMedium.copy(shadow = softTextShadow),
                    fontWeight = FontWeight.Medium,
                    color = cardLabelMuted,
                )
                Spacer(modifier = Modifier.height(Dimens.spacing12))
                CurrentJackpotNumberChips(numbers = numbers)
            }

            Spacer(modifier = Modifier.height(Dimens.spacing24))
            Button(
                onClick = onScanClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.buttonHeight)
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(Dimens.radiusButtonPill),
                        ambientColor = Color.Black.copy(alpha = 0.12f),
                        spotColor = Color.Black.copy(alpha = 0.18f),
                    ),
                shape = RoundedCornerShape(Dimens.radiusButtonPill),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Primary,
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 6.dp,
                ),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.QrCodeScanner,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(modifier = Modifier.width(Dimens.spacing10))
                    Text(
                        text = stringResource(R.string.home_scan_ticket),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun NextDrawSection(countdown: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.24f),
                            Color.White.copy(alpha = 0.08f),
                        ),
                    ),
                )
                .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Event,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = cardOnGreen,
            )
        }
        Spacer(modifier = Modifier.width(Dimens.spacing12))
        Column(modifier = Modifier.weight(1f)) {
            GreenCardText(
                text = "Next Draw",
                style = MaterialTheme.typography.titleSmall.copy(shadow = softTextShadow),
                fontWeight = FontWeight.SemiBold,
                color = cardOnGreen,
            )
            Spacer(modifier = Modifier.height(Dimens.spacing8))
            GreenCardText(
                text = "Sunday draw • 17:00",
                style = MaterialTheme.typography.bodySmall.copy(shadow = softTextShadow),
                fontWeight = FontWeight.Normal,
                color = cardBodySubtle,
            )
            Spacer(modifier = Modifier.height(Dimens.spacing16))
            GreenCardText(
                text = countdown,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 32.sp,
                    lineHeight = 36.sp,
                    shadow = strongTextShadow,
                ),
                fontWeight = FontWeight.Bold,
                color = cardOnGreen,
            )
            Spacer(modifier = Modifier.height(Dimens.spacing4))
            GreenCardText(
                text = "until draw",
                style = MaterialTheme.typography.labelMedium.copy(shadow = softTextShadow),
                fontWeight = FontWeight.Medium,
                color = cardLabelMuted,
            )
        }
    }
}

@Composable
private fun GreenCardText(
    text: String,
    style: TextStyle,
    fontWeight: FontWeight,
    color: Color,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        text = text,
        style = style.copy(fontWeight = fontWeight),
        color = color,
        maxLines = maxLines,
        overflow = overflow,
    )
}

@Composable
private fun CurrentJackpotNumberChips(numbers: List<Int>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing12),
    ) {
        numbers.forEach { number ->
            Box(
                modifier = Modifier
                    .size(40.dp)
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
                    text = number.toString(),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        shadow = softTextShadow,
                    ),
                    color = cardOnGreen,
                    textAlign = TextAlign.Center,
                )
            }
        }
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
