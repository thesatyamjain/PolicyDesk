package com.policydesk.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

// Apple Light Scheme
private val AppleLightColorScheme = lightColorScheme(
    primary = AppleSystemBlue,
    onPrimary = IosSecondaryGrouped,
    primaryContainer = AppleSystemBlueLight,
    onPrimaryContainer = AppleSystemBlueDark,
    secondary = Navy800,
    onSecondary = IosSecondaryGrouped,
    secondaryContainer = IosTertiaryGrouped,
    onSecondaryContainer = IosLabelPrimary,
    background = IosSystemBackground,
    onBackground = IosLabelPrimary,
    surface = IosSecondaryGrouped,
    onSurface = IosLabelPrimary,
    surfaceVariant = IosTertiaryGrouped,
    onSurfaceVariant = IosLabelSecondary,
    outline = Slate200,
    error = AppleSystemRed,
    onError = IosSecondaryGrouped,
    errorContainer = AppleSystemRedBg,
    onErrorContainer = AppleSystemRed
)

// Cupertino Dark Mode Scheme (True Black OLED & Moonlight Insets)
private val AppleDarkColorScheme = darkColorScheme(
    primary = AppleDarkSystemBlue,
    onPrimary = IosDarkLabelPrimary,
    primaryContainer = AppleDarkSystemBlueLight,
    onPrimaryContainer = AppleDarkSystemBlue,
    secondary = IosDarkLabelSecondary,
    onSecondary = IosDarkSystemBackground,
    secondaryContainer = IosDarkTertiaryGrouped,
    onSecondaryContainer = IosDarkLabelPrimary,
    background = IosDarkSystemBackground,
    onBackground = IosDarkLabelPrimary,
    surface = IosDarkSecondaryGrouped,
    onSurface = IosDarkLabelPrimary,
    surfaceVariant = IosDarkTertiaryGrouped,
    onSurfaceVariant = IosDarkLabelSecondary,
    outline = IosDarkSeparator,
    error = AppleDarkSystemRed,
    onError = IosDarkSystemBackground,
    errorContainer = AppleDarkSystemRedBg,
    onErrorContainer = AppleDarkSystemRed
)

// Apple Squircle & Capsule Geometry
val PolicyDeskShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),      // Liquid Glass Cards
    extraLarge = RoundedCornerShape(26.dp)  // Glass Action Pills & Sheets
)

@Composable
fun PolicyDeskTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) AppleDarkColorScheme else AppleLightColorScheme
    val bgSysColor = if (darkTheme) IosDarkSystemBackground else IosSystemBackground

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = PolicyDeskShapes,
        content = content
    )
}
