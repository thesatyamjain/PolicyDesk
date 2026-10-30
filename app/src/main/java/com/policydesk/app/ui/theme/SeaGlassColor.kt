package com.policydesk.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Google Sea Glass Design System Palette
 * Inspired by @SLKVinyl's concept & Figma Sea Glass specifications.
 * Weathered beach-tumbled glass, ocean pastels, luminous edge highlights, and sandy neutrals.
 */

// ── Primary Coastal & Ocean Tints ───────────────────────────────────────────
val SeaGlassAquaPrimary = Color(0xFF2A9D8F)      // Balanced sea teal (WCAG AA compliant)
val SeaGlassAquaDark = Color(0xFF1F7A6F)         // Deeper sea teal for dark mode text
val SeaGlassAquaLight = Color(0xFF52B788)        // Radiant seafoam
val SeaGlassAquaGleam = Color(0xFFC7F9CC)        // Refractive edge shine
val SeaGlassAquaBg = Color(0x242A9D8F)           // 14% translucent ocean tint

// ── Deep Marine & Coastal Slate ─────────────────────────────────────────────
val SeaGlassOceanDeep = Color(0xFF1D3557)        // Coastal midnight navy
val SeaGlassOceanMedium = Color(0xFF2B2D42)      // Deep slate pebble
val SeaGlassOceanMuted = Color(0xFF457B9D)       // Weathered sea blue
val SeaGlassOceanGleam = Color(0xFF90E0EF)       // Luminous azure gleam

// ── Pebble Sands & Backgrounds (Tinted with Ocean Hue) ───────────────────────
val SeaGlassSandLight = Color(0xFFF6F8F7)        // Light beach-sand canvas
val SeaGlassSandSecondary = Color(0xFFEDF2F0)    // Secondary inset sand
val SeaGlassWetPebbleDark = Color(0xFF0C1318)    // Deep ocean midnight (OLED dark canvas)
val SeaGlassWetPebbleSecondary = Color(0xFF15222B) // Secondary wet-slate card

// ── Frosted Glass Card Fills ────────────────────────────────────────────────
val SeaGlassCardFillLight = Color(0x99FFFFFF)    // 60% frosted milk glass
val SeaGlassCardFillDark = Color(0x40162533)     // 25% deep marine translucent glass
val SeaGlassBorderLight = Color(0x66FFFFFF)      // 40% white edge reflection
val SeaGlassBorderDark = Color(0x3380ED99)       // 20% seafoam edge reflection

// ── Text Hierarchy (Tuned for frosted glass legibility) ─────────────────────
val SeaGlassTextPrimaryLight = Color(0xFF142129) // Rich ocean charcoal
val SeaGlassTextSecondaryLight = Color(0xFF4F636F) // Slate secondary
val SeaGlassTextTertiaryLight = Color(0xFF7E929E)  // Muted note

val SeaGlassTextPrimaryDark = Color(0xFFF0F5F5)  // Pale frosted white
val SeaGlassTextSecondaryDark = Color(0xFFA5B8C2) // Pale slate
val SeaGlassTextTertiaryDark = Color(0xFF6F8694)  // Subdued note

// ── Sea Glass Status Colors ─────────────────────────────────────────────────
val SeaGlassActiveGreen = Color(0xFF10B981)      // Emerald sea glass
val SeaGlassActiveGreenBg = Color(0x2210B981)
val SeaGlassExpiringAmber = Color(0xFFF59E0B)    // Amber glass bead
val SeaGlassExpiringAmberBg = Color(0x26F59E0B)
val SeaGlassOverdueCoral = Color(0xFFEF4444)     // Sea coral red
val SeaGlassOverdueCoralBg = Color(0x26EF4444)

// ── Light Refraction Edge Gradients ─────────────────────────────────────────
val SeaGlassGleamBrushLight = Brush.linearGradient(
    colors = listOf(
        Color(0x99FFFFFF), // Bright top-left specular highlight
        Color(0x33FFFFFF), // Mid transmission
        Color(0x1A2A9D8F)  // Subtle bottom-right ocean refraction
    )
)

val SeaGlassGleamBrushDark = Brush.linearGradient(
    colors = listOf(
        Color(0x4D80ED99), // Seafoam luminous edge
        Color(0x1A457B9D), // Deep azure mid
        Color(0x0D000000)  // Diffuse shadow
    )
)
