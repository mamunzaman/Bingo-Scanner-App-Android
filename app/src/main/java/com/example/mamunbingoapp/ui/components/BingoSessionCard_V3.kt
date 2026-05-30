package com.example.mamunbingoapp.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.example.mamunbingoapp.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.core.MAX_CALLED_NUMBERS
import com.example.mamunbingoapp.theme.MamunBingoTheme

@Composable
fun BingoSessionCard_V3(
    title: String,
    ticketsInRoom: Int,
    calledCount: Int,
    totalCalledCount: Int = MAX_CALLED_NUMBERS,
    onJoin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(Dimens.radiusCard)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onJoin)
            .border(1.dp, cs.outlineVariant, shape),
        shape = shape,
        color = cs.surface
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(cs.primary)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 20.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = cs.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = cs.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .background(cs.primary, CircleShape)
                                    )
                                    Text(
                                        text = stringResource(R.string.bingo_session_active),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = cs.primary
                                    )
                                }
                            }
                            Text(
                                text = if (ticketsInRoom == 1) {
                                    stringResource(R.string.bingo_session_tickets_one)
                                } else {
                                    stringResource(R.string.bingo_session_tickets_other, ticketsInRoom)
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = cs.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Button(
                        onClick = onJoin,
                        modifier = Modifier.height(40.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = cs.primary,
                            contentColor = cs.onPrimary
                        ),
                        contentPadding = PaddingValues(start = 16.dp, end = 24.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Login,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.bingo_session_join),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val clampedCalled = calledCount.coerceAtMost(totalCalledCount)
                    Text(
                        text = stringResource(
                            R.string.bingo_session_called_count,
                            clampedCalled,
                            totalCalledCount,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = cs.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    val progress = if (totalCalledCount > 0) (clampedCalled.toFloat() / totalCalledCount).coerceIn(0f, 1f) else 0f
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = cs.primary,
                        trackColor = cs.outlineVariant
                    )
                }
            }
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun BingoSessionCard_V3LightPreview() {
    MamunBingoTheme {
        BingoSessionCard_V3(
            title = "Friday Night Bingo",
            ticketsInRoom = 3,
            calledCount = 16,
            totalCalledCount = 25,
            onJoin = {}
        )
    }
}

@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun BingoSessionCard_V3DarkPreview() {
    MamunBingoTheme(darkTheme = true) {
        BingoSessionCard_V3(
            title = "Weekend Session",
            ticketsInRoom = 1,
            calledCount = 25,
            totalCalledCount = 25,
            onJoin = {}
        )
    }
}
