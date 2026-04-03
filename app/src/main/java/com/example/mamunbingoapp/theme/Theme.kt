package com.example.mamunbingoapp.theme

import android.app.Activity
import com.example.mamunbingoapp.ui.debug.A11yDebug
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnSurface,
    secondary = Secondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSurface,
    background = Background,
    onBackground = OnSurface,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurface,
    surfaceContainer = SurfaceContainer,
    outline = Outline,
    outlineVariant = Slate200,
    scrim = Scrim,
    error = Error,
    onError = OnPrimary
)

val LocalPrimaryBorder = staticCompositionLocalOf { PrimaryBorder }

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = OnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = OnDarkPrimaryContainer,
    secondary = Secondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSurface,
    background = DarkBackground,
    onBackground = OnDarkSurface,
    surface = DarkSurface,
    onSurface = OnDarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = OnDarkSurface,
    surfaceContainer = DarkSurfaceContainer,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    scrim = DarkScrim,
    error = Error,
    onError = OnPrimary
)

@Composable
fun MamunBingoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            @Suppress("DEPRECATION")
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    CompositionLocalProvider(LocalPrimaryBorder provides if (darkTheme) DarkPrimaryBorder else PrimaryBorder) {
        val base = Typography
        val scaledTypography = if (A11yDebug.ENABLED) {
            base.copy(
                bodyLarge = base.bodyLarge.copy(fontSize = base.bodyLarge.fontSize * 1.15f),
                bodyMedium = base.bodyMedium.copy(fontSize = base.bodyMedium.fontSize * 1.15f),
                labelLarge = base.labelLarge.copy(fontSize = base.labelLarge.fontSize * 1.15f),
                labelMedium = base.labelMedium.copy(fontSize = base.labelMedium.fontSize * 1.15f),
            )
        } else base
        MaterialTheme(
            colorScheme = colorScheme,
            typography = scaledTypography,
            shapes = androidx.compose.material3.Shapes(
                medium = RoundedCornerShape(Dimens.radiusSmall),
                large = RoundedCornerShape(Dimens.radiusCard)
            ),
            content = {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    content()
                }
            }
        )
    }
}
