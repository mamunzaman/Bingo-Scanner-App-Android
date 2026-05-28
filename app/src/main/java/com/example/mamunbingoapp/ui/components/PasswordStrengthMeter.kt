package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.Success
import com.example.mamunbingoapp.theme.Warning

private const val MIN_PASSWORD_LENGTH = 8

enum class PasswordStrength {
    Weak,
    Medium,
    Strong,
}

@Composable
fun PasswordStrengthMeter(
    password: String,
    modifier: Modifier = Modifier,
) {
    if (password.isBlank()) return
    val strength = passwordStrength(password)
    val (label, progress, color) = when (strength) {
        PasswordStrength.Weak -> Triple("Weak", 0.34f, MaterialTheme.colorScheme.error)
        PasswordStrength.Medium -> Triple("Medium", 0.67f, Warning)
        PasswordStrength.Strong -> Triple("Strong", 1f, Success)
    }
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Password strength: $label",
            style = MaterialTheme.typography.bodySmall,
            color = color,
        )
        Spacer(modifier = Modifier.height(Dimens.spacing4))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(3.dp),
                ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(6.dp)
                    .background(color = color, shape = RoundedCornerShape(3.dp)),
            )
        }
    }
}

fun passwordStrength(password: String): PasswordStrength {
    if (password.length < MIN_PASSWORD_LENGTH) return PasswordStrength.Weak
    val hasLetter = password.any { it.isLetter() }
    val hasDigit = password.any { it.isDigit() }
    val hasUpper = password.any { it.isUpperCase() }
    val hasLower = password.any { it.isLowerCase() }
    val hasSymbol = password.any { !it.isLetterOrDigit() }
    return when {
        hasUpper && hasLower && hasDigit && hasSymbol -> PasswordStrength.Strong
        hasLetter && hasDigit -> PasswordStrength.Medium
        else -> PasswordStrength.Weak
    }
}
