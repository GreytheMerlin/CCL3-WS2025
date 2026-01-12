package com.example.snorly.core.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SnorlyColorScheme = darkColorScheme(
    // 1. Brand Colors
    primary = MoonYellow,
    onPrimary = Color.Black, // Black text on Yellow looks best
    secondary = Blue,
    onSecondary = Color.White,

    // 2. Backgrounds
    background = Background,
    onBackground = TextWhite, // Fixed: Was dark grey, now white

    surface = Surface,
    onSurface = TextWhite,    // Fixed: Was dark grey, now white

    // 3. Extras (Mapping your border/mute colors)
    outline = Border,
    surfaceVariant = Surface, // Cards will use this
    onSurfaceVariant = TextMute,
    error = Danger,
    onError = Color.Black
)



@Composable
fun SnorlyTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = SnorlyColorScheme

    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Status bar matches the background (Black)
            window.statusBarColor = colorScheme.background.toArgb()
            // Ensure icons in status bar are light (since background is dark)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}