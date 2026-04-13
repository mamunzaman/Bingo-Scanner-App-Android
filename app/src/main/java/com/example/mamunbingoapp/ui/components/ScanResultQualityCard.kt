package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.draw.shadow
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.Error
import com.example.mamunbingoapp.theme.OnSurface
import com.example.mamunbingoapp.theme.Primary
import com.example.mamunbingoapp.theme.PrimaryDark
import com.example.mamunbingoapp.theme.Slate400
import com.example.mamunbingoapp.theme.Slate600
import com.example.mamunbingoapp.theme.SurfaceContainer
import com.example.mamunbingoapp.theme.Warning
import com.example.mamunbingoapp.theme.AppTextStyles
import com.example.mamunbingoapp.theme.MamunBingoTheme

data class ScanResultData(
    val numbers: String = "—",
    val grid: String = "—",
    val card: String = "—",
    val helperLine: String? = null,
)

enum class QualityLevel { NONE, LOW, MEDIUM, HIGH }

data class ScanQualityData(
    val level: QualityLevel = QualityLevel.NONE,
    val lightingOk: Boolean = false,
    val focusOk: Boolean = false,
    val angleOk: Boolean = false,
)

val QualityLevel.labelColor: Color
    get() = when (this) {
        QualityLevel.NONE -> Slate400
        QualityLevel.LOW -> Error
        QualityLevel.MEDIUM -> Warning
        QualityLevel.HIGH -> Primary
    }

val QualityLevel.displayLabel: String
    get() = when (this) {
        QualityLevel.NONE -> "—"
        QualityLevel.LOW -> "LOW"
        QualityLevel.MEDIUM -> "MEDIUM"
        QualityLevel.HIGH -> "HIGH"
    }

@Composable
fun ScanResultQualityCard(
    scanResult: ScanResultData,
    qualityData: ScanQualityData,
    showQualitySection: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val cardShape = RoundedCornerShape(Dimens.radiusCard)
    val cs = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(Dimens.cardElevationDefault, cardShape, ambientColor = cs.primary.copy(alpha = 0.06f), spotColor = cs.primary.copy(alpha = 0.08f))
            .clip(cardShape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        cs.surface,
                        cs.surfaceVariant.copy(alpha = 0.4f),
                    ),
                ),
                shape = cardShape,
            )
            .border(BorderStroke(Dimens.cardBorderDefault, cs.outlineVariant), cardShape),
    ) {
        Column(modifier = Modifier.padding(Dimens.spacing12)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "SCAN RESULT",
                    style = AppTextStyles.sectionLabel,
                    color = PrimaryDark,
                )
                if (showQualitySection && qualityData.level != QualityLevel.NONE) {
                    Text(
                        text = qualityData.level.displayLabel,
                        style = AppTextStyles.sectionLabel,
                        color = qualityData.level.labelColor,
                    )
                }
            }
            Spacer(Modifier.height(Dimens.spacing8))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacing4),
                ) {
                    StatRow(label = "Numbers", value = scanResult.numbers)
                    StatRow(label = "Grid", value = scanResult.grid)
                }
                Box(
                    modifier = Modifier
                        .padding(horizontal = Dimens.spacing8)
                        .width(Dimens.cardBorderDefault)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.outlineVariant),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacing4),
                ) {
                    if (showQualitySection) {
                        QualitySegmentBar(level = qualityData.level)
                        Spacer(Modifier.height(Dimens.spacing4))
                        val neutralIndicators = qualityData.level == QualityLevel.NONE
                        QualityIndicatorRow(label = "Lighting", ok = if (neutralIndicators) null else qualityData.lightingOk)
                        QualityIndicatorRow(label = "Focus", ok = if (neutralIndicators) null else qualityData.focusOk)
                        QualityIndicatorRow(label = "Angle", ok = if (neutralIndicators) null else qualityData.angleOk)
                    } else {
                        QualitySegmentBar(level = QualityLevel.NONE)
                        Spacer(Modifier.height(Dimens.spacing4))
                        QualityIndicatorRow(label = "Lighting", ok = null)
                        QualityIndicatorRow(label = "Focus", ok = null)
                        QualityIndicatorRow(label = "Angle", ok = null)
                    }
                }
            }
            scanResult.helperLine?.let { line ->
                Spacer(Modifier.height(Dimens.spacing8))
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}


@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.radiusBingoCell))
            .background(SurfaceContainer)
            .padding(
                horizontal = Dimens.spacing8,
                vertical = Dimens.spacing4,
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Slate600,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = if (value == "—") Slate400 else OnSurface,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun QualitySegmentBar(level: QualityLevel) {
    val filledCount = when (level) {
        QualityLevel.NONE -> 0
        QualityLevel.LOW -> 1
        QualityLevel.MEDIUM -> 3
        QualityLevel.HIGH -> 5
    }
    val emptyColor = if (level == QualityLevel.NONE) MaterialTheme.colorScheme.outlineVariant else SurfaceContainer
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing4),
    ) {
        repeat(5) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(Dimens.spacing5)
                    .clip(RoundedCornerShape(Dimens.radiusXSmall))
                    .background(if (index < filledCount) Primary else emptyColor),
            )
        }
    }
}

@Composable
private fun QualityIndicatorRow(label: String, ok: Boolean?) {
    val neutral = ok == null
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing4),
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.spacing5)
                .clip(CircleShape)
                .background(
                    when {
                        neutral -> Slate400
                        ok == true -> Primary
                        else -> Error
                    }
                ),
        )
        Text(
            text = when {
                neutral -> "$label…"
                ok == true -> label
                else -> "$label !"
            },
            style = MaterialTheme.typography.labelSmall,
            color = when {
                neutral -> Slate400
                ok == true -> Primary
                else -> Error
            },
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Preview(name = "Empty")
@Composable
private fun ScanResultQualityCardPreviewEmpty() {
    MamunBingoTheme {
        ScanResultQualityCard(
            scanResult = ScanResultData(),
            qualityData = ScanQualityData(),
            modifier = Modifier.padding(horizontal = Dimens.screenHorizontalPadding),
        )
    }
}

@Preview(name = "Filled")
@Composable
private fun ScanResultQualityCardPreviewFilled() {
    MamunBingoTheme {
        ScanResultQualityCard(
            scanResult = ScanResultData(numbers = "25", grid = "5×5", card = "1"),
            qualityData = ScanQualityData(
                level = QualityLevel.HIGH,
                lightingOk = true,
                focusOk = false,
                angleOk = true,
            ),
            modifier = Modifier.padding(horizontal = Dimens.screenHorizontalPadding),
        )
    }
}
