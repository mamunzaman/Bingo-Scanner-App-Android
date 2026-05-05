package com.example.mamunbingoapp.ui.components.qr

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.Error
import com.example.mamunbingoapp.ui.model.BingoCellUi

fun cellsToQrGrid5x5(cells: List<BingoCellUi>): List<List<String>> {
    val list25 = if (cells.size >= 25) {
        cells.take(25)
    } else {
        cells + List(25 - cells.size) { BingoCellUi(null, false, false, false, false) }
    }
    return list25.chunked(5).map { row ->
        row.map { cell -> cell.number?.trim().orEmpty() }
    }
}

private const val QR_HELP_TEXT = "Scan this QR to add this Bingo sheet"

@Composable
fun TicketQrDialog(
    bitmap: android.graphics.Bitmap?,
    errorMessage: String?,
    onDismiss: () -> Unit,
    isLoading: Boolean = false,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacing24)
                .clip(RoundedCornerShape(Dimens.radiusCard))
                .background(MaterialTheme.colorScheme.surface)
                .padding(Dimens.spacing16),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Ticket QR",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(Dimens.spacing8))
            Text(
                text = QR_HELP_TEXT,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Dimens.spacing8)
            )
            Spacer(modifier = Modifier.height(Dimens.spacing16))
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp)
                    )
                }
                errorMessage != null -> {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Error
                    )
                }
                bitmap != null -> {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Ticket QR code",
                        modifier = Modifier
                            .size(280.dp)
                            .clip(RoundedCornerShape(Dimens.spacing8))
                    )
                }
            }
            if (bitmap != null && errorMessage == null && !isLoading) {
                Text(
                    text = "Scan with another phone camera, then tap the link to open in Mamun Bingo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = Dimens.spacing8,
                            end = Dimens.spacing8,
                            top = Dimens.spacing8,
                        )
                )
            }
            Spacer(modifier = Modifier.height(Dimens.spacing16))
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    }
}
