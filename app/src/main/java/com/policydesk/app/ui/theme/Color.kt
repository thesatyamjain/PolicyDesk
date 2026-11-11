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

// iOS Light Label Hierarchy (WCAG AA >= 4.5:1 Contrast)
val IosLabelPrimary = Color(0xFF1C1C1E)     // Primary text
val IosLabelSecondary = Color(0xFF5A5A60)   // Subhead & secondary text (>= 6.1:1 on light surfaces)
val IosLabelTertiary = Color(0xFF6C6C72)    // Footnotes & placeholder (>= 4.6:1 on light surfaces)
val IosSeparator = Color(0x33000000)        // 20% Black Hairline separator

// iOS Dark Label Hierarchy (WCAG AA >= 4.5:1 Contrast)
val IosDarkLabelPrimary = Color(0xFFFFFFFF)
val IosDarkLabelSecondary = Color(0xFFA1A1AA) // >= 5.4:1 on #2C2C2E, >= 6.6:1 on #1C1C1E
val IosDarkLabelTertiary = Color(0xFF949499)  // >= 4.6:1 on #2C2C2E, >= 5.6:1 on #1C1C1E
val IosDarkSeparator = Color(0x54545865)

// Apple Light Accent Colors (<80% saturation, WCAG AA >= 4.5:1)
val AppleSystemBlue = Color(0xFF0056B8)     // AA Text/Icon Blue (>= 6.2:1 on light surfaces)
val AppleSystemBlueFill = Color(0xFF0062CC) // AA Button/Pill Fill (>= 5.8:1 with white text)
val AppleSystemBlueDark = Color(0xFF0056B3)
val AppleSystemBlueLight = Color(0xFFE5F1FF)

// Apple Dark Accent Colors (Higher luminance for dark canvas, separate AA fill tokens)
val AppleDarkSystemBlue = Color(0xFF4CA6FF)     // AA Text/Icon Blue (>= 4.5:1 on dark glass/pills)
val AppleDarkSystemBlueFill = Color(0xFF0066D6) // AA Button/Pill Fill (>= 5.4:1 with white text)
val AppleDarkSystemBlueDark = Color(0xFF0052AD)
val AppleDarkSystemBlueLight = Color(0xFF1C3456)

// Apple Light Status Colors (WCAG AA >= 4.5:1 on light cards & translucent tints)
val AppleSystemGreen = Color(0xFF146C34)
val AppleSystemGreenFill = Color(0xFF15803D)
val AppleSystemGreenBg = Color(0x1F15803D) // 12% translucent tint
val AppleSystemOrange = Color(0xFF9A4104)
val AppleSystemOrangeBg = Color(0x21D97706) // 13% translucent tint
val AppleSystemDeepOrange = Color(0xFFB33909)
val AppleSystemDeepOrangeBg = Color(0x21EA580C)
val AppleSystemRed = Color(0xFFBE1B1B)
val AppleSystemRedFill = Color(0xFFDC2626)
val AppleSystemRedBg = Color(0x1FDC2626)

// Apple Dark Status Colors (WCAG AA >= 4.5:1 on dark glass & translucent tints)
val AppleDarkSystemGreen = Color(0xFF34D375)
val AppleDarkSystemGreenFill = Color(0xFF15803D)
val AppleDarkSystemGreenBg = Color(0x3330D158) // 20% translucent tint
val AppleDarkSystemOrange = Color(0xFFFBBF24)
val AppleDarkSystemOrangeBg = Color(0x33FF9F0A)
val AppleDarkSystemDeepOrange = Color(0xFFFB923C)
val AppleDarkSystemDeepOrangeBg = Color(0x33FF6961)
val AppleDarkSystemRed = Color(0xFFFF857D)
val AppleDarkSystemRedFill = Color(0xFFDC2626)
val AppleDarkSystemRedBg = Color(0x33FF453A)

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

val AppleSystemPurple = Color(0xFF4F46E5)
val AppleDarkSystemPurple = Color(0xFF9A98F8)

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
