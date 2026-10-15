package com.policydesk.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val AppleColorScheme = lightColorScheme(
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
    val colorScheme = AppleColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = IosSystemBackground.toArgb()
            window.navigationBarColor = IosSystemBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = PolicyDeskShapes,
        content = content
    )
}
