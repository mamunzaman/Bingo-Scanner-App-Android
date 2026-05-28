package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.Error
import com.example.mamunbingoapp.theme.IconContainerBg
import com.example.mamunbingoapp.theme.Info
import com.example.mamunbingoapp.theme.PrimaryContainer
import com.example.mamunbingoapp.theme.SecondaryContainer
import com.example.mamunbingoapp.theme.Success

enum class AppAuthMessageType {
    Error,
    Success,
    Info,
}

/** Maps register-screen combined feedback to Error vs Success (e.g. email confirmation). */
fun registerFeedbackMessageType(message: String?): AppAuthMessageType = when {
    message?.startsWith(ACCOUNT_CREATED_PREFIX, ignoreCase = true) == true ->
        AppAuthMessageType.Success
    else -> AppAuthMessageType.Error
}

private const val ACCOUNT_CREATED_PREFIX = "Account created"

/**
 * Inline auth feedback (login, register, forgot password). Soft container + icon + body text.
 * No-op when [message] is null or blank.
 */
@Composable
fun AppAuthMessage(
    message: String?,
    type: AppAuthMessageType,
    modifier: Modifier = Modifier,
) {
    if (message.isNullOrBlank()) return
    val shape = RoundedCornerShape(Dimens.radiusSmall)
    val style = authMessageStyle(type)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(style.containerColor, shape)
            .border(Dimens.cardBorderDefault, style.borderColor, shape)
            .padding(horizontal = Dimens.spacing12, vertical = Dimens.spacing10),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = style.icon,
            contentDescription = null,
            tint = style.contentColor,
            modifier = Modifier.size(Dimens.iconCompact),
        )
        Spacer(modifier = Modifier.width(Dimens.spacing8))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = style.contentColor,
            modifier = Modifier.weight(1f),
        )
    }
}

private data class AuthMessageStyle(
    val containerColor: Color,
    val borderColor: Color,
    val contentColor: Color,
    val icon: ImageVector,
)

@Composable
private fun authMessageStyle(type: AppAuthMessageType): AuthMessageStyle = when (type) {
    AppAuthMessageType.Error -> AuthMessageStyle(
        containerColor = SecondaryContainer,
        borderColor = Error.copy(alpha = 0.28f),
        contentColor = Error,
        icon = Icons.Filled.Error,
    )
    AppAuthMessageType.Success -> AuthMessageStyle(
        containerColor = IconContainerBg,
        borderColor = Success.copy(alpha = 0.35f),
        contentColor = Success,
        icon = Icons.Filled.CheckCircle,
    )
    AppAuthMessageType.Info -> AuthMessageStyle(
        containerColor = PrimaryContainer.copy(alpha = 0.65f),
        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
        contentColor = Info,
        icon = Icons.Filled.Info,
    )
}
