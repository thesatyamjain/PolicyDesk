package com.policydesk.app.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// Apple Human Interface Guidelines (HIG) Palette
// ==========================================

// iOS Light System Backgrounds & Grouped Surfaces
val IosSystemBackground = Color(0xFFF2F2F7) // iOS System Grouped Background
val IosSecondaryGrouped = Color(0xFFFFFFFF) // iOS Inset Card Background
val IosTertiaryGrouped = Color(0xFFE5E5EA)  // iOS Grouped Inset Fill

// iOS Dark System Backgrounds & Surfaces (OLED True Black & Acrylic)
val IosDarkSystemBackground = Color(0xFF000000)
val IosDarkSecondaryGrouped = Color(0xFF1C1C1E)
val IosDarkTertiaryGrouped = Color(0xFF2C2C2E)

// iOS Light Label Hierarchy (WCAG AAA Contrast)
val IosLabelPrimary = Color(0xFF1C1C1E)     // Primary text
val IosLabelSecondary = Color(0xFF6C6C70)   // Subhead & secondary text
val IosLabelTertiary = Color(0xFF8E8E93)    // Footnotes & placeholder
val IosSeparator = Color(0x33000000)        // 20% Black Hairline separator

// iOS Dark Label Hierarchy
val IosDarkLabelPrimary = Color(0xFFFFFFFF)
val IosDarkLabelSecondary = Color(0xFF8E8E93)
val IosDarkLabelTertiary = Color(0xFF636366)
val IosDarkSeparator = Color(0x54545865)

// Apple Light Accent Colors (<80% saturation, high contrast)
val AppleSystemBlue = Color(0xFF007AFF)     // Signature Cupertino Blue
val AppleSystemBlueDark = Color(0xFF0056B3)
val AppleSystemBlueLight = Color(0xFFE5F1FF)

// Apple Dark Accent Colors (Higher luminance for dark canvas)
val AppleDarkSystemBlue = Color(0xFF0A84FF)
val AppleDarkSystemBlueDark = Color(0xFF0063D1)
val AppleDarkSystemBlueLight = Color(0xFF1C3456)

// Apple Light Status Colors
val AppleSystemGreen = Color(0xFF34C759)
val AppleSystemGreenBg = Color(0x1F34C759) // 12% translucent tint
val AppleSystemOrange = Color(0xFFFF9500)
val AppleSystemOrangeBg = Color(0x1FFF9500)
val AppleSystemDeepOrange = Color(0xFFFF6B00)
val AppleSystemDeepOrangeBg = Color(0x1FFF6B00)
val AppleSystemRed = Color(0xFFFF3B30)
val AppleSystemRedBg = Color(0x1FFF3B30)

// Apple Dark Status Colors
val AppleDarkSystemGreen = Color(0xFF30D158)
val AppleDarkSystemGreenBg = Color(0x2E30D158)
val AppleDarkSystemOrange = Color(0xFFFF9F0A)
val AppleDarkSystemOrangeBg = Color(0x2EFF9F0A)
val AppleDarkSystemDeepOrange = Color(0xFFFF6961)
val AppleDarkSystemDeepOrangeBg = Color(0x2EFF6961)
val AppleDarkSystemRed = Color(0xFFFF453A)
val AppleDarkSystemRedBg = Color(0x2EFF453A)

// ==========================================
// True Apple Liquid Glass Tokens (Light & Dark)
// ==========================================
// Light Glass
val GlassSurface = Color(0xD9FFFFFF)          // 85% Frosted glass substrate
val GlassSurfaceSubtle = Color(0xB8FFFFFF)    // 72% Translucent glass
val GlassSurfaceDeep = Color(0xF2F9F9FC)      // 95% High opacity glass
val GlassBorderTop = Color(0xBFFFFFFF)        // Specular highlight rim (75% white)
val GlassBorderBottom = Color(0x26FFFFFF)     // Subtle ambient edge (15% white)
val GlassHairline = Color(0x1A000000)         // 10% dark refraction border
val GlassShadow = Color(0x1F000000)           // Ambient diffuse shadow (12% black)

// Dark Glass Acrylic
val GlassDarkSurface = Color(0xD91C1C1E)      // 85% Midnight acrylic
val GlassDarkSurfaceSubtle = Color(0xB32C2C2E)// 70% Translucent dark glass
val GlassDarkBorderTop = Color(0x38FFFFFF)    // Moonlight specular highlight rim (22% white)
val GlassDarkBorderBottom = Color(0x0FFFFFFF) // Ambient bottom rim (6% white)
val GlassDarkShadow = Color(0x59000000)       // Deep ambient shadow

// Backward-compatible mappings
val Navy900 = IosLabelPrimary
val Navy800 = Color(0xFF2C2C2E)
val Navy700 = Color(0xFF3A3A3C)
val Slate600 = IosLabelSecondary
val Slate400 = IosLabelTertiary
val Slate200 = Color(0xFFD1D1D6)
val Slate100 = IosTertiaryGrouped
val Slate50 = IosSystemBackground

val SapphireBlue = AppleSystemBlue
val SapphireBlueDark = AppleSystemBlueDark
val SapphireBlueLight = AppleSystemBlueLight

val StatusActiveGreen = AppleSystemGreen
val StatusActiveGreenBg = AppleSystemGreenBg
val StatusExpiringAmber = AppleSystemOrange
val StatusExpiringAmberBg = AppleSystemOrangeBg
val StatusUrgentOrange = AppleSystemDeepOrange
val StatusUrgentOrangeBg = AppleSystemDeepOrangeBg
val StatusOverdueRed = AppleSystemRed
val StatusOverdueRedBg = AppleSystemRedBg

val CardBackground = IosSecondaryGrouped
val SurfaceMuted = IosSystemBackground
