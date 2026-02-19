package com.fiscalia.quindio.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = FiscaliaBlue,
    onPrimary = Color.White,
    primaryContainer = FiscaliaBlueLight,
    onPrimaryContainer = Color.White,
    secondary = FiscaliaYellow,
    onSecondary = FiscaliaBlue,
    secondaryContainer = FiscaliaYellowLight,
    onSecondaryContainer = FiscaliaBlue,
    tertiary = FiscaliaRed,
    background = Color.White,
    surface = Color.White,
    error = FiscaliaRed,
    onBackground = FiscaliaText,
    onSurface = FiscaliaText
)

@Composable
fun FiscaliaQuindioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = FiscaliaBlue.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}