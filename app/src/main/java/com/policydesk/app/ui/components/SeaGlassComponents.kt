package com.policydesk.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.policydesk.app.ui.theme.SeaGlassActiveGreen
import com.policydesk.app.ui.theme.SeaGlassActiveGreenBg
import com.policydesk.app.ui.theme.SeaGlassAquaBg
import com.policydesk.app.ui.theme.SeaGlassAquaDark
import com.policydesk.app.ui.theme.SeaGlassAquaGleam
import com.policydesk.app.ui.theme.SeaGlassAquaLight
import com.policydesk.app.ui.theme.SeaGlassAquaPrimary
import com.policydesk.app.ui.theme.SeaGlassExpiringAmber
import com.policydesk.app.ui.theme.SeaGlassExpiringAmberBg
import com.policydesk.app.ui.theme.SeaGlassGleamBrushDark
import com.policydesk.app.ui.theme.SeaGlassGleamBrushLight
import com.policydesk.app.ui.theme.SeaGlassOverdueCoral
import com.policydesk.app.ui.theme.SeaGlassOverdueCoralBg
import com.policydesk.app.ui.theme.SeaGlassSandLight
import com.policydesk.app.ui.theme.SeaGlassSandSecondary
import com.policydesk.app.ui.theme.SeaGlassTextPrimaryDark
import com.policydesk.app.ui.theme.SeaGlassTextPrimaryLight
import com.policydesk.app.ui.theme.SeaGlassTextSecondaryDark
import com.policydesk.app.ui.theme.SeaGlassTextSecondaryLight
import com.policydesk.app.ui.theme.SeaGlassWetPebbleDark
import com.policydesk.app.ui.theme.SeaGlassWetPebbleSecondary

// Default organic pebble squircle shape
val SeaGlassPebbleShape = RoundedCornerShape(26.dp)

// Asymmetrical natural pebble shape for statistics & highlights
val SeaGlassAsymmetricPebbleShape = RoundedCornerShape(
    topStart = 28.dp,
    topEnd = 20.dp,
    bottomEnd = 28.dp,
    bottomStart = 16.dp
)

private val LightSeaGlassMeniscus = listOf(
    Color.White.copy(alpha = 0.88f),
    Color(0xFFE8F5E9).copy(alpha = 0.50f),
    Color.White.copy(alpha = 0.40f),
    Color(0xFFE0F2FE).copy(alpha = 0.65f)
)

private val DarkSeaGlassMeniscus = listOf(
    Color(0xFF1B2A38).copy(alpha = 0.88f),
    Color(0xFF14222D).copy(alpha = 0.65f),
    Color(0xFF0F1A24).copy(alpha = 0.50f),
    Color(0xFF172533).copy(alpha = 0.75f)
)

private val LightSeaGlassBrush = Brush.linearGradient(
    colors = LightSeaGlassMeniscus,
    start = Offset.Zero,
    end = Offset.Infinite
)

private val DarkSeaGlassBrush = Brush.linearGradient(
    colors = DarkSeaGlassMeniscus,
    start = Offset.Zero,
    end = Offset.Infinite
)

/**
 * Root ambient canvas for Google Sea Glass.
 * Diagonal soft ocean mist wash over sandy foundation.
 */
@Composable
fun SeaGlassCanvas(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    val backgroundBrush = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                SeaGlassWetPebbleDark,
                Color(0xFF0F1820),
                Color(0xFF121E27),
                SeaGlassWetPebbleDark
            ),
            start = Offset.Zero,
            end = Offset.Infinite
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                SeaGlassSandLight,
                Color(0xFFE8F5EE), // Soft seagrass tint
                Color(0xFFEAF2F8), // Soft ocean azure
                SeaGlassSandSecondary
            ),
            start = Offset.Zero,
            end = Offset.Infinite
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = backgroundBrush)
    ) {
        content()
    }
}

/**
 * Modifier applying the Google Sea Glass material:
 * - 26dp pebble contour
 * - 4-stop diagonal tumbled glass refraction
 * - 1.2dp luminous specular gleam border
 * - Soft diffuse ambient shadow
 */
fun Modifier.seaGlass(
    shape: Shape = SeaGlassPebbleShape,
    elevation: Dp = 4.dp,
    isDark: Boolean = false
): Modifier = this
    .shadow(
        elevation = if (isDark) 0.dp else elevation,
        shape = shape,
        ambientColor = Color(0x1A2A9D8F),
        spotColor = Color(0x1F1D3557)
    )
    .background(
        brush = if (isDark) DarkSeaGlassBrush else LightSeaGlassBrush,
        shape = shape
    )
    .border(
        width = 1.2.dp,
        brush = if (isDark) SeaGlassGleamBrushDark else SeaGlassGleamBrushLight,
        shape = shape
    )

/**
 * Tactile frosted pebble card.
 * Integrates haptic feedback and spring physics on press.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SeaGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = SeaGlassPebbleShape,
    elevation: Dp = 4.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.965f else 1f,
        animationSpec = spring(
            dampingRatio = 0.78f,
            stiffness = 380f
        ),
        label = "sea_glass_card_bounce"
    )

    val clickModifier = if (onClick != null) {
        Modifier.combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
        )
    } else Modifier

    val textColor = if (isDark) SeaGlassTextPrimaryDark else SeaGlassTextPrimaryLight

    androidx.compose.runtime.CompositionLocalProvider(
        androidx.compose.material3.LocalContentColor provides textColor
    ) {
        Column(
            modifier = modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .seaGlass(shape = shape, elevation = elevation, isDark = isDark)
                .then(clickModifier)
        ) {
            content()
        }
    }
}

/**
 * Asymmetrical Stat Pebble for Dashboard Metrics & KPIs.
 */
@Composable
fun SeaGlassStatPebble(
    title: String,
    value: String,
    subtitle: String? = null,
    accentColor: Color = SeaGlassAquaPrimary,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val isDark = isSystemInDarkTheme()
    val labelPrimary = if (isDark) SeaGlassTextPrimaryDark else SeaGlassTextPrimaryLight
    val labelSecondary = if (isDark) SeaGlassTextSecondaryDark else SeaGlassTextSecondaryLight

    SeaGlassCard(
        modifier = modifier,
        shape = SeaGlassAsymmetricPebbleShape,
        elevation = 3.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = labelSecondary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor,
                lineHeight = 28.sp
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = labelSecondary
                )
            }
        }
    }
}

/**
 * Full-Pill Tactile Sea Glass Primary Button.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SeaGlassPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled && !isLoading) 0.955f else 1f,
        animationSpec = spring(
            dampingRatio = 0.75f,
            stiffness = 400f
        ),
        label = "sea_pill_btn_bounce"
    )

    val buttonBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF2EC4B6), // Luminous teal
            Color(0xFF208B81)  // Ocean deep teal
        )
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = if (enabled) 1f else 0.45f
            }
            .shadow(
                elevation = if (enabled) 6.dp else 0.dp,
                shape = CircleShape,
                ambientColor = Color(0x332EC4B6),
                spotColor = Color(0x401F7A6F)
            )
            .background(brush = buttonBrush, shape = CircleShape)
            .border(
                width = 1.2.dp,
                brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.45f), Color.Transparent)
                ),
                shape = CircleShape
            )
            .clip(CircleShape)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled && !isLoading,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            )
            .padding(horizontal = 22.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(18.dp)
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Translucent Frosted Sea Glass Secondary Button.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SeaGlassSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    val isDark = isSystemInDarkTheme()
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.955f else 1f,
        animationSpec = spring(
            dampingRatio = 0.75f,
            stiffness = 400f
        ),
        label = "sea_sec_btn_bounce"
    )

    val textColor = if (isDark) SeaGlassAquaGleam else SeaGlassAquaDark

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = if (enabled) 1f else 0.45f
            }
            .seaGlass(shape = CircleShape, elevation = 2.dp, isDark = isDark)
            .clip(CircleShape)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            )
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(7.dp))
            }
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}

/**
 * Frosted Status Badge in Sea Glass Material.
 */
@Composable
fun SeaGlassStatusBadge(
    text: String,
    accentColor: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Box(
        modifier = modifier
            .background(backgroundColor, CircleShape)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(accentColor.copy(alpha = 0.35f), Color.Transparent)
                ),
                shape = CircleShape
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
    }
}
