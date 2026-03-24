package com.example.farmdirectoryupgraded.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val FarmDarkColorScheme = darkColorScheme(
    primary = FarmGreen80,
    secondary = HarvestAmber80,
    tertiary = SkyBlue80,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onPrimary = FarmGreen20,
    onSecondary = HarvestAmber20,
    onTertiary = SkyBlue20,
    onBackground = MorningMist,
    onSurface = MorningMist,
)

private val FarmLightColorScheme = lightColorScheme(
    primary = FarmGreen40,
    secondary = HarvestAmber40,
    tertiary = SkyBlue40,
    background = FarmBackground,
    surface = FarmSurface,
    surfaceVariant = FarmSurfaceVariant,
    onPrimary = FarmSurface,
    onSecondary = FarmSurface,
    onTertiary = FarmSurface,
    onBackground = FarmGreen20,
    onSurface = FarmGreen20,
)

@Composable
fun FarmDirectoryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> FarmDarkColorScheme
        else -> FarmLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
