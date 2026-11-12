package com.policydesk.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.policydesk.app.domain.model.NumberPlateStyle
import com.policydesk.app.domain.model.RegistrationNormalizer
import com.policydesk.app.domain.model.VehiclePlateClassifier
import com.policydesk.app.ui.theme.AppleDarkSystemBlue
import com.policydesk.app.ui.theme.AppleDarkSystemBlueDark
import com.policydesk.app.ui.theme.AppleDarkSystemBlueFill
import com.policydesk.app.ui.theme.AppleDarkSystemGreen
import com.policydesk.app.ui.theme.AppleDarkSystemOrange
import com.policydesk.app.ui.theme.AppleDarkSystemPurple
import com.policydesk.app.ui.theme.AppleDarkSystemRed
import com.policydesk.app.ui.theme.AppleIcons
import com.policydesk.app.ui.theme.AppleSystemBlue
import com.policydesk.app.ui.theme.AppleSystemBlueDark
import com.policydesk.app.ui.theme.AppleSystemBlueFill
import com.policydesk.app.ui.theme.AppleSystemGreen
import com.policydesk.app.ui.theme.AppleSystemGreenFill
import com.policydesk.app.ui.theme.AppleSystemOrange
import com.policydesk.app.ui.theme.AppleSystemPurple
import com.policydesk.app.ui.theme.AppleSystemRed
import com.policydesk.app.ui.theme.GlassDarkShadow
import com.policydesk.app.ui.theme.GlassShadow
import com.policydesk.app.ui.theme.IosDarkLabelPrimary
import com.policydesk.app.ui.theme.IosDarkLabelSecondary
import com.policydesk.app.ui.theme.IosDarkLabelTertiary
import com.policydesk.app.ui.theme.IosDarkSeparator
import com.policydesk.app.ui.theme.IosLabelPrimary
import com.policydesk.app.ui.theme.IosLabelSecondary
import com.policydesk.app.ui.theme.IosLabelTertiary
import com.policydesk.app.ui.theme.IosSeparator
import com.policydesk.app.ui.theme.LocalReducedMotion
import com.policydesk.app.ui.theme.isSystemMotionReduced
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import kotlin.math.roundToInt

val LocalHazeState = compositionLocalOf<HazeState?> { null }
val LocalScrollToTopTrigger = compositionLocalOf { 0L }
val LocalScrollToTopDispatcher = compositionLocalOf<(() -> Unit)> { {} }

@Composable
fun BindBackToTop(listState: LazyListState) {
    val trigger = LocalScrollToTopTrigger.current
    LaunchedEffect(trigger) {
        if (trigger > 0L) {
            listState.animateScrollToItem(0)
        }
    }
}

@Composable
fun BindBackToTop(scrollState: ScrollState) {
    val trigger = LocalScrollToTopTrigger.current
    LaunchedEffect(trigger) {
        if (trigger > 0L) {
            scrollState.animateScrollTo(0)
        }
    }
}

/**
 * Root Ambient Aurora Mesh Canvas for Apple Liquid Glass (iOS HIG).
 * Recreates the exact My Warranties continuous full-screen diagonal pastel wash (glassBackdrop).
 * Zero circular orbs or spotted blobs; completely seamless and luminous.
 */
@Composable
fun LiquidGlassCanvas(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    val backdropBrush = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF000000), // Pure OLED True Black
                Color(0xFF050608), // Ultra-deep subtle obsidian
                Color(0xFF090A0D), // True midnight depth
                Color(0xFF000000)  // Pure OLED True Black
            ),
            start = Offset.Zero,
            end = Offset.Infinite
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFD6E6FE), // Apple Soft Ice Blue
                Color(0xFFEBE7FB), // Soft Lavender/Iris glow
                Color(0xFFF5F3FA), // Neutral Refraction center
                Color(0xFFFDE7EE)  // Apple Subtle Quartz Pink
            ),
            start = Offset.Zero,
            end = Offset.Infinite
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = backdropBrush)
    ) {
        content()
    }
}

/**
 * Canonical Apple Spring Motion Archetypes (iOS 17+ / visionOS)
 * Damped Harmonic Oscillators calibrated for touch responsiveness and settlement.
 */
object AppleSprings {
    /** Smooth: Critically damped (zeta = 1.0), zero overshoot. For sheets, modals, screen transitions. */
    val Smooth = spring<Float>(dampingRatio = 1.0f, stiffness = 300f)
    val SmoothDp = spring<Dp>(dampingRatio = 1.0f, stiffness = 300f)

    /** Snappy: High tension with slight sub-audible rebound (zeta = 0.82). For docks, tabs, selection bars. */
    val Snappy = spring<Float>(dampingRatio = 0.82f, stiffness = 400f)
    val SnappyDp = spring<Dp>(dampingRatio = 0.82f, stiffness = 400f)
    val SnappyColor = spring<Color>(dampingRatio = 0.82f, stiffness = 400f)

    /** Bouncy: Expressive elastic overshoot (zeta = 0.55). For badges, confirmations, celebrations. */
    val Bouncy = spring<Float>(dampingRatio = 0.55f, stiffness = 280f)

    /** Interactive: Rapid responsiveness tracking continuous user touch (zeta = 0.86). */
    val Interactive = spring<Float>(dampingRatio = 0.86f, stiffness = 600f)

    /** Pop: Micro-scale icon expansion on activation (zeta = 0.52). */
    val Pop = spring<Float>(dampingRatio = 0.52f, stiffness = Spring.StiffnessMediumLow)

    /** CardPress: Tactile physical compression on tap (zeta = 0.78). */
    val CardPress = spring<Float>(dampingRatio = 0.78f, stiffness = 380f)

    /** Switch: Fluid thumb slide for toggle switches (zeta = 0.80). */
    val Switch = spring<Dp>(dampingRatio = 0.80f, stiffness = 340f)
}

private val DarkMeniscusColors = listOf(
    Color(0xFF26262B).copy(alpha = 0.94f),
    Color(0xFF1C1C20).copy(alpha = 0.92f),
    Color(0xFF141417).copy(alpha = 0.90f),
    Color(0xFF202024).copy(alpha = 0.94f)
)

private val LightMeniscusColors = listOf(
    Color.White.copy(alpha = 0.96f),
    Color(0xFFF9F9FB).copy(alpha = 0.94f),
    Color(0xFFF2F2F7).copy(alpha = 0.91f),
    Color.White.copy(alpha = 0.96f)
)

@Composable
private fun effectiveReducedMotion(): Boolean =
    LocalReducedMotion.current || isSystemMotionReduced(LocalInspectionMode.current)

private val DarkGlassBrush = Brush.linearGradient(
    colors = DarkMeniscusColors,
    start = Offset.Zero,
    end = Offset.Infinite
)

private val LightGlassBrush = Brush.linearGradient(
    colors = LightMeniscusColors,
    start = Offset.Zero,
    end = Offset.Infinite
)

private val DarkBorderBrush = Brush.verticalGradient(
    colors = listOf(
        Color.White.copy(alpha = 0.35f),
        Color.White.copy(alpha = (0.35f + 0.06f) * 0.45f),
        Color.White.copy(alpha = 0.06f)
    )
)

private val LightBorderBrush = Brush.verticalGradient(
    colors = listOf(
        Color.White.copy(alpha = 0.90f),
        Color.White.copy(alpha = 0.35f),
        Color.White.copy(alpha = 0.20f)
    )
)

/**
 * Modifier that applies True Apple Liquid Glass (matching My Warranties / visionOS):
 * 1. Dual-layer ambient & contact edge shadow with Apple diffuse elevation
 * 2. 4-stop diagonal meniscus refraction gradient with 90%-95% fail-safe opacity
 * 3. 1.1dp dual-incident vertical specular border (Zenith highlight -> Nadir catch)
 */
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(18.dp),
    elevation: Dp = 4.dp,
    isDark: Boolean = false,
    fillAlphaTop: Float = if (isDark) 0.94f else 0.96f,
    fillAlphaBottom: Float = if (isDark) 0.90f else 0.91f,
    borderAlphaTop: Float = if (isDark) 0.35f else 0.90f,
    borderAlphaBottom: Float = if (isDark) 0.06f else 0.20f
): Modifier {
    val useDefaultBorder = (isDark && borderAlphaTop == 0.35f && borderAlphaBottom == 0.06f) ||
        (!isDark && borderAlphaTop == 0.90f && borderAlphaBottom == 0.20f)
    val borderBrush = if (useDefaultBorder) {
        if (isDark) DarkBorderBrush else LightBorderBrush
    } else {
        Brush.verticalGradient(
            colors = if (isDark) listOf(
                Color.White.copy(alpha = borderAlphaTop),
                Color.White.copy(alpha = (borderAlphaTop + borderAlphaBottom) * 0.45f),
                Color.White.copy(alpha = borderAlphaBottom)
            ) else listOf(
                Color.White.copy(alpha = borderAlphaTop),
                Color.White.copy(alpha = 0.35f),
                Color.White.copy(alpha = borderAlphaBottom)
            )
        )
    }

    val useDefaultFill = (isDark && fillAlphaTop == 0.94f && fillAlphaBottom == 0.90f) ||
        (!isDark && fillAlphaTop == 0.96f && fillAlphaBottom == 0.91f)
    val meniscusBrush = if (useDefaultFill) {
        if (isDark) DarkGlassBrush else LightGlassBrush
    } else {
        Brush.linearGradient(
            colors = if (isDark) listOf(
                Color(0xFF26262B).copy(alpha = fillAlphaTop),
                Color(0xFF1C1C20).copy(alpha = fillAlphaTop * 0.98f),
                Color(0xFF141417).copy(alpha = fillAlphaBottom),
                Color(0xFF202024).copy(alpha = fillAlphaTop)
            ) else listOf(
                Color.White.copy(alpha = fillAlphaTop),
                Color(0xFFF9F9FB).copy(alpha = fillAlphaTop * 0.98f),
                Color(0xFFF2F2F7).copy(alpha = fillAlphaBottom),
                Color.White.copy(alpha = fillAlphaTop)
            ),
            start = Offset.Zero,
            end = Offset.Infinite
        )
    }

    // 3. Apple Ambient + Contact Elevation
    val shadowModifier = if (elevation > 0.dp) {
        Modifier.shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = if (isDark) Color(0x66000000) else Color(0x140F172A),
            spotColor = if (isDark) Color(0x66000000) else Color(0x14000000)
        )
    } else {
        Modifier
    }

    return this
        .then(shadowModifier)
        .clip(shape)
        .background(
            brush = meniscusBrush,
            shape = shape
        )
        .border(
            width = 1.1.dp,
            brush = borderBrush,
            shape = shape
        )
}

/**
 * Apple Liquid Glass Inset Card
 * Supports fluid spring physics, GPU-only press scaling, reduced motion, and Role.Button semantics.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    elevation: Dp = 3.dp,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    onClickLabel: String? = null,
    onLongClickLabel: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val reducedMotion = effectiveReducedMotion()
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isInteractive = onClick != null || onLongClick != null

    val scaleState = animateFloatAsState(
        targetValue = if (!reducedMotion && isPressed && isInteractive) 0.962f else 1f,
        animationSpec = if (reducedMotion) snap() else AppleSprings.CardPress,
        label = "glass_card_spring"
    )

    val clickModifier = if (isInteractive) {
        Modifier.combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            role = Role.Button,
            onClickLabel = onClickLabel,
            onLongClickLabel = onLongClickLabel,
            onClick = {
                if (onClick != null) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            },
            onLongClick = if (onLongClick != null) {
                {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            } else null
        )
    } else Modifier

    val contentColor = if (isDark) IosDarkLabelPrimary else IosLabelPrimary

    androidx.compose.runtime.CompositionLocalProvider(
        androidx.compose.material3.LocalContentColor provides contentColor
    ) {
        Column(
            modifier = modifier
                .graphicsLayer {
                    val s = scaleState.value
                    scaleX = s
                    scaleY = s
                }
                .liquidGlass(shape = shape, elevation = elevation, isDark = isDark)
                .clip(shape)
                .then(clickModifier)
                .padding(16.dp),
            content = content
        )
    }
}

/**
 * Apple Liquid Glass Navigation Bar
 * Frosted translucent header with title, back chevron, and action slots.
 */
@Composable
fun LiquidGlassTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onNavigateBack: (() -> Unit)? = null,
    onDoubleTap: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()
    val haptic = LocalHapticFeedback.current
    val scrollDispatcher = LocalScrollToTopDispatcher.current
    val effectiveDoubleTap = onDoubleTap ?: scrollDispatcher
    var lastTopTapTime by remember { mutableStateOf(0L) }

    val primaryText = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val secondaryText = if (isDark) IosDarkLabelSecondary else IosLabelSecondary
    val accentBlue = if (isDark) AppleDarkSystemBlue else AppleSystemBlue
    val separatorColor = if (isDark) IosDarkSeparator else IosSeparator

    Box(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        effectiveDoubleTap.invoke()
                    }
                )
            }
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isDark) listOf(
                        Color(0xF0000000),
                        Color(0xD0000000),
                        Color(0x99000000)
                    ) else listOf(
                        Color(0xF5FFFFFF),
                        Color(0xE0FFFFFF),
                        Color(0xB3FFFFFF)
                    )
                )
            )
            .drawBehind {
                // Subtle Apple hairline bottom divider only (no top/side card borders)
                drawLine(
                    color = separatorColor.copy(alpha = 0.45f),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 0.5.dp.toPx()
                )
            }
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                if (onNavigateBack != null) {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onNavigateBack()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = AppleIcons.ChevronLeft,
                            contentDescription = "Back",
                            tint = accentBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Column(
                    modifier = Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        val now = System.currentTimeMillis()
                        if (now - lastTopTapTime < 450L) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            effectiveDoubleTap.invoke()
                            lastTopTapTime = 0L
                        } else {
                            lastTopTapTime = now
                        }
                    }
                ) {
                    Text(
                        text = title,
                        fontSize = if (subtitle == null) 22.sp else 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryText,
                        letterSpacing = (-0.4).sp,
                        modifier = Modifier.semantics { heading() }
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = secondaryText,
                            letterSpacing = 0.1.sp
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                actions()
            }
        }
    }
}

/**
 * Apple Liquid Glass Segmented Control
 * Sliding spring-illuminated pill picker for switching tabs or filters with haptic & keyboard support.
 */
@Composable
fun <T> LiquidGlassSegmentedControl(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    labelProvider: (T) -> String
) {
    if (items.isEmpty()) return
    val isDark = isSystemInDarkTheme()
    val reducedMotion = effectiveReducedMotion()
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val trackShape = RoundedCornerShape(12.dp)
    val itemShape = RoundedCornerShape(10.dp)
    val trackBg = if (isDark) Color(0x33545458) else Color(0x1F767680)
    val trackBorder = if (isDark) Color(0x33FFFFFF) else Color(0x1F000000)

    val selectedIndex = items.indexOf(selectedItem).coerceAtLeast(0)
    val pillIndexAnim = animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = if (reducedMotion) snap() else AppleSprings.Snappy,
        label = "seg_pill_index"
    )
    val pillBg = remember(isDark) {
        if (isDark) {
            Brush.verticalGradient(listOf(Color(0xFF3A3A3C), Color(0xFF2C2C2E)))
        } else {
            Brush.verticalGradient(listOf(Color.White, Color(0xFFFBFBFD)))
        }
    }
    val pillBorder = if (isDark) Color(0x40FFFFFF) else Color(0x1A000000)
    val selectedColor = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val unselectedColor = if (isDark) IosDarkLabelSecondary else IosLabelSecondary
    val focusRequesters = remember(items.size) { List(items.size) { FocusRequester() } }

    BoxWithConstraints(
        modifier = modifier
            .background(trackBg, trackShape)
            .border(0.5.dp, trackBorder, trackShape)
            .padding(3.dp)
            .selectableGroup()
    ) {
        val segmentSpacing = 2.dp
        val count = items.size
        val totalSpacing = segmentSpacing * (count - 1).coerceAtLeast(0)
        val segmentWidth = (maxWidth - totalSpacing) / count
        val stepPx = with(density) { (segmentWidth + segmentSpacing).toPx() }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Gliding indicator pill rendered purely via GPU translationX
            Box(
                modifier = Modifier
                    .width(segmentWidth)
                    .fillMaxHeight()
                    .graphicsLayer {
                        translationX = pillIndexAnim.value * stepPx
                    }
                    .shadow(2.dp, itemShape, spotColor = Color(0x1A000000))
                    .background(brush = pillBg, shape = itemShape)
                    .border(0.5.dp, pillBorder, itemShape)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(segmentSpacing)
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = index == selectedIndex
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val pressScale = animateFloatAsState(
                        targetValue = if (!reducedMotion && isPressed) 0.96f else 1f,
                        animationSpec = if (reducedMotion) snap() else AppleSprings.Pop,
                        label = "seg_item_press"
                    )
                    val labelColor by animateColorAsState(
                        targetValue = if (isSelected) selectedColor else unselectedColor,
                        animationSpec = if (reducedMotion) snap() else AppleSprings.SnappyColor,
                        label = "seg_item_color"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .graphicsLayer {
                                val s = pressScale.value
                                scaleX = s
                                scaleY = s
                            }
                            .clip(itemShape)
                            .focusRequester(focusRequesters[index])
                            .onPreviewKeyEvent { event ->
                                if (event.type == KeyEventType.KeyDown && items.size > 1) {
                                    when (event.key) {
                                        Key.DirectionRight -> {
                                            val next = (index + 1) % items.size
                                            focusRequesters[next].requestFocus()
                                            onItemSelected(items[next])
                                            true
                                        }
                                        Key.DirectionLeft -> {
                                            val prev = (index - 1 + items.size) % items.size
                                            focusRequesters[prev].requestFocus()
                                            onItemSelected(items[prev])
                                            true
                                        }
                                        else -> false
                                    }
                                } else {
                                    false
                                }
                            }
                            .selectable(
                                selected = isSelected,
                                interactionSource = interactionSource,
                                indication = null,
                                role = Role.Tab,
                                onClick = {
                                    if (!isSelected) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onItemSelected(item)
                                    }
                                }
                            )
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = labelProvider(item),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            color = labelColor,
                            letterSpacing = (-0.1).sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/**
 * Apple Liquid Glass Search Bar
 * Frosted pill search field with subtle Apple focus glow ring, accessible clear button, and haptic confirmation.
 */
@Composable
fun LiquidGlassSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    onSearchAction: () -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()
    val reducedMotion = effectiveReducedMotion()
    val haptic = LocalHapticFeedback.current
    val barShape = RoundedCornerShape(14.dp)
    val primaryText = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val placeholderColor = if (isDark) IosDarkLabelTertiary else IosLabelTertiary
    val accentBlue = if (isDark) AppleDarkSystemBlue else AppleSystemBlue
    val focusBorderColor = if (isDark) AppleDarkSystemBlueFill else AppleSystemBlueFill
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }

    val focusProgress = animateFloatAsState(
        targetValue = if (isFocused) 1f else 0f,
        animationSpec = if (reducedMotion) snap() else AppleSprings.Smooth,
        label = "search_focus_glow"
    )
    val iconScale = animateFloatAsState(
        targetValue = if (!reducedMotion && isFocused) 1.06f else 1f,
        animationSpec = if (reducedMotion) snap() else AppleSprings.Pop,
        label = "search_icon_scale"
    )
    val iconTint by animateColorAsState(
        targetValue = if (isFocused) accentBlue else placeholderColor,
        animationSpec = if (reducedMotion) snap() else AppleSprings.SnappyColor,
        label = "search_icon_tint"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                val p = focusProgress.value
                if (p > 0.005f) {
                    val cornerPx = 14.dp.toPx()
                    val haloExpand = 3.5.dp.toPx()
                    val haloAlpha = (if (isDark) 0.24f else 0.14f) * p
                    drawRoundRect(
                        color = accentBlue.copy(alpha = haloAlpha),
                        topLeft = Offset(-haloExpand, -haloExpand),
                        size = Size(size.width + haloExpand * 2f, size.height + haloExpand * 2f),
                        cornerRadius = CornerRadius(cornerPx + haloExpand, cornerPx + haloExpand)
                    )
                }
            }
            .liquidGlass(
                shape = barShape,
                elevation = 1.dp,
                isDark = isDark,
                fillAlphaTop = 0.90f,
                fillAlphaBottom = 0.78f
            )
            .drawWithContent {
                drawContent()
                val p = focusProgress.value
                if (p > 0.005f) {
                    val strokePx = 1.4.dp.toPx()
                    val cornerPx = 14.dp.toPx()
                    drawRoundRect(
                        color = focusBorderColor.copy(alpha = p),
                        style = Stroke(width = strokePx),
                        cornerRadius = CornerRadius(cornerPx, cornerPx)
                    )
                }
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .pointerInput(focusRequester) {
                        detectTapGestures { focusRequester.requestFocus() }
                    }
                    .padding(start = 12.dp, top = 6.dp, bottom = 6.dp)
                    .height(30.dp)
            ) {
                Icon(
                    imageVector = AppleIcons.Search,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier
                        .size(18.dp)
                        .graphicsLayer {
                            val s = iconScale.value
                            scaleX = s
                            scaleY = s
                        }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (query.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = 14.sp,
                            color = placeholderColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            color = primaryText,
                            fontWeight = FontWeight.Normal
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(accentBlue),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { onSearchAction() }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .onFocusChanged { isFocused = it.isFocused }
                    )
                }
            }
            AnimatedVisibility(
                visible = query.isNotEmpty(),
                enter = if (reducedMotion) EnterTransition.None else fadeIn(tween(140)) + scaleIn(initialScale = 0.72f, animationSpec = AppleSprings.Pop),
                exit = if (reducedMotion) ExitTransition.None else fadeOut(tween(120)) + scaleOut(targetScale = 0.72f, animationSpec = tween(120))
            ) {
                Box(
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .size(30.dp)
                        .clip(CircleShape)
                        .clickable(
                            role = Role.Button,
                            onClickLabel = "Clear search",
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onQueryChange("")
                                focusRequester.requestFocus()
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = AppleIcons.Clear,
                        contentDescription = "Clear search",
                        tint = placeholderColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Apple Inset Grouped Section
 * Grouped container with uppercase header caption and internal cell dividers.
 */
@Composable
fun LiquidGlassGroupedSection(
    title: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val captionColor = if (isDark) IosDarkLabelSecondary else IosLabelSecondary

    Column(modifier = modifier.fillMaxWidth()) {
        if (title != null) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = captionColor,
                letterSpacing = (-0.1).sp,
                modifier = Modifier.padding(start = 16.dp, bottom = 6.dp, top = 12.dp)
            )
        }
        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            elevation = 2.dp,
            content = content
        )
    }
}

/**
 * Apple Inset Grouped List Row
 */
@Composable
fun LiquidGlassListRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    value: String? = null,
    icon: ImageVector? = null,
    iconBgColor: Color = AppleSystemBlue,
    iconTint: Color = Color.White,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    showDivider: Boolean = true
) {
    val isDark = isSystemInDarkTheme()
    val haptic = LocalHapticFeedback.current
    val primaryText = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val secondaryText = if (isDark) IosDarkLabelSecondary else IosLabelSecondary
    val separatorColor = if (isDark) IosDarkSeparator else IosSeparator

    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
        )
    } else Modifier

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(clickModifier)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                if (icon != null) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(iconBgColor, RoundedCornerShape(8.dp))
                            .border(0.5.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Column {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = primaryText
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            fontSize = 12.sp,
                            color = secondaryText
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (value != null) {
                    Text(
                        text = value,
                        fontSize = 14.sp,
                        color = secondaryText,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }
                trailingContent?.invoke()
            }
        }

        if (showDivider) {
            HorizontalDivider(
                color = separatorColor,
                thickness = 0.5.dp,
                modifier = Modifier.padding(start = if (icon != null) 42.dp else 0.dp)
            )
        }
    }
}

/**
 * Apple Liquid Glass Floating Pill Action Button
 * Features spring press physics, reduced-motion awareness, and Cupertino haptic actuation.
 */
@Composable
fun LiquidGlassFloatingButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val reducedMotion = effectiveReducedMotion()
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scaleState = animateFloatAsState(
        targetValue = if (!reducedMotion && isPressed) 0.95f else 1f,
        animationSpec = if (reducedMotion) snap() else AppleSprings.CardPress,
        label = "fab_spring_press"
    )
    val pillShape = CircleShape
    val blueGradient = if (isDark) {
        listOf(AppleDarkSystemBlueFill, AppleDarkSystemBlueDark)
    } else {
        listOf(AppleSystemBlueFill, AppleSystemBlueDark)
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                val s = scaleState.value
                scaleX = s
                scaleY = s
            }
            .shadow(6.dp, pillShape, spotColor = AppleSystemBlueFill.copy(alpha = 0.35f))
            .background(
                brush = Brush.verticalGradient(blueGradient),
                shape = pillShape
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.5f), Color.White.copy(alpha = 0.15f))
                ),
                shape = pillShape
            )
            .clip(pillShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            )
            .padding(horizontal = 20.dp, vertical = 13.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.2).sp
            )
        }
    }
}

/**
 * Apple Primary Action Button (iOS 26 HIG .glassProminent)
 * Luminous vertical gradient, specular top highlight, soft radiant shadow,
 * spring press physics and Cupertino haptic actuation.
 */
@Composable
fun AppleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = AppleSystemBlue,
    contentColor: Color = Color.White,
    icon: ImageVector? = null,
    isLoading: Boolean = false,
    height: Dp = 48.dp,
    shape: Shape = RoundedCornerShape(12.dp),
    horizontalPadding: Dp = 20.dp,
    fontSize: TextUnit = 15.sp
) {
    val isDark = isSystemInDarkTheme()
    val reducedMotion = effectiveReducedMotion()
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scaleState = animateFloatAsState(
        targetValue = if (!reducedMotion && isPressed && enabled && !isLoading) 0.965f else 1f,
        animationSpec = if (reducedMotion) snap() else AppleSprings.CardPress,
        label = "apple_button_spring"
    )

    val actualContainerColor = if (!enabled) {
        if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)
    } else {
        containerColor
    }

    val actualContentColor = if (!enabled) {
        if (isDark) IosDarkLabelTertiary else IosLabelTertiary
    } else {
        contentColor
    }

    val buttonBrush = if (!enabled) {
        SolidColor(actualContainerColor)
    } else if (containerColor == AppleSystemBlue) {
        val topBlue = if (isDark) AppleDarkSystemBlueFill else AppleSystemBlueFill
        val bottomBlue = if (isDark) AppleDarkSystemBlueDark else AppleSystemBlueDark
        Brush.verticalGradient(listOf(topBlue, bottomBlue))
    } else {
        SolidColor(containerColor)
    }

    val shadowColor = if (enabled) {
        if (isDark) AppleDarkSystemBlueFill.copy(alpha = 0.35f) else AppleSystemBlueFill.copy(alpha = 0.30f)
    } else Color.Transparent

    Box(
        modifier = modifier
            .graphicsLayer {
                val s = scaleState.value
                scaleX = s
                scaleY = s
            }
            .height(height)
            .shadow(
                elevation = if (enabled) 5.dp else 0.dp,
                shape = shape,
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .clip(shape)
            .background(buttonBrush, shape)
            .drawWithContent {
                drawContent()
                if (enabled) {
                    // Specular rim light on top of button
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.White.copy(alpha = 0.30f), Color.Transparent),
                            startY = 0f,
                            endY = 12.dp.toPx()
                        ),
                        size = Size(size.width, 12.dp.toPx())
                    )
                }
            }
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = if (enabled) {
                        listOf(Color.White.copy(alpha = 0.45f), Color.White.copy(alpha = 0.12f))
                    } else {
                        listOf(Color.Transparent, Color.Transparent)
                    }
                ),
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled && !isLoading,
                role = Role.Button,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            )
            .padding(horizontal = horizontalPadding),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = actualContentColor,
                strokeWidth = 2.dp
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
                        tint = actualContentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    color = actualContentColor,
                    fontSize = fontSize,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.2).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Apple Secondary / Frosted Glass Action Button (iOS 26 HIG .glass)
 * Translucent frosted glass capsule with directional specular border and haptic actuation.
 */
@Composable
fun AppleSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tintColor: Color = AppleSystemBlue,
    icon: ImageVector? = null,
    height: Dp = 48.dp,
    shape: Shape = RoundedCornerShape(12.dp),
    horizontalPadding: Dp = 16.dp,
    fontSize: TextUnit = 13.sp
) {
    val isDark = isSystemInDarkTheme()
    val reducedMotion = effectiveReducedMotion()
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scaleState = animateFloatAsState(
        targetValue = if (!reducedMotion && isPressed && enabled) 0.97f else 1f,
        animationSpec = if (reducedMotion) snap() else AppleSprings.CardPress,
        label = "apple_sec_button_spring"
    )

    val bgBrush = if (!enabled) {
        SolidColor(if (isDark) Color(0x1AFFFFFF) else Color(0x0A000000))
    } else {
        if (isDark) {
            Brush.verticalGradient(
                listOf(Color(0xFF2C2C2E).copy(alpha = 0.65f), Color(0xFF1C1C1E).copy(alpha = 0.45f))
            )
        } else {
            Brush.verticalGradient(
                listOf(Color.White.copy(alpha = 0.85f), Color(0xFFF2F2F7).copy(alpha = 0.65f))
            )
        }
    }

    val effectiveTint = if (isDark && tintColor == AppleSystemBlue) AppleDarkSystemBlue else tintColor
    val contentColor = if (!enabled) {
        if (isDark) IosDarkLabelTertiary else IosLabelTertiary
    } else {
        effectiveTint
    }

    val borderBrush = if (enabled) {
        if (isDark) {
            Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.28f), Color.White.copy(alpha = 0.08f)))
        } else {
            Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.95f), Color(0xFFD1D1D6).copy(alpha = 0.40f)))
        }
    } else {
        SolidColor(Color.Transparent)
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                val s = scaleState.value
                scaleX = s
                scaleY = s
            }
            .height(height)
            .shadow(
                elevation = if (enabled) 2.dp else 0.dp,
                shape = shape,
                ambientColor = if (isDark) GlassDarkShadow else GlassShadow,
                spotColor = if (isDark) GlassDarkShadow else GlassShadow
            )
            .clip(shape)
            .background(bgBrush, shape)
            .border(1.dp, borderBrush, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            )
            .padding(horizontal = horizontalPadding),
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
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                color = contentColor,
                fontSize = fontSize,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.1).sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Authentic Cupertino Switch (iOS 18 HIG)
 * 51dp x 31dp track with GPU-translated spring thumb, reduced-motion awareness, and Role.Switch semantics.
 */
@Composable
fun AppleSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val isDark = isSystemInDarkTheme()
    val reducedMotion = effectiveReducedMotion()
    val haptic = LocalHapticFeedback.current
    val activeColor = if (isDark) Color(0xFF30D158) else AppleSystemGreenFill
    val inactiveColor = if (isDark) Color(0xFF39393D) else Color(0xFFE9E9EA)

    val trackColor by animateColorAsState(
        targetValue = if (checked) activeColor else inactiveColor,
        animationSpec = if (reducedMotion) snap() else AppleSprings.SnappyColor,
        label = "switch_track_color"
    )

    val thumbProgress = animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = if (reducedMotion) snap() else spring(dampingRatio = 0.80f, stiffness = 340f),
        label = "switch_thumb_progress"
    )

    Box(
        modifier = modifier
            .size(width = 51.dp, height = 31.dp)
            .background(trackColor, CircleShape)
            .clip(CircleShape)
            .toggleable(
                value = checked,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = { next ->
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onCheckedChange(next)
                }
            )
            .padding(2.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    translationX = thumbProgress.value * 20.dp.toPx()
                }
                .size(27.dp)
                .shadow(elevation = 3.dp, shape = CircleShape, spotColor = Color(0x33000000))
                .background(Color.White, CircleShape)
        )
    }
}

/**
 * Apple UIAlertController Style Alert Dialog
 * Rounded 14dp container, centered typography, hairline split actions.
 */
@Composable
fun AppleAlertDialog(
    onDismissRequest: () -> Unit,
    title: String,
    message: String? = null,
    confirmText: String = "OK",
    onConfirm: () -> Unit,
    dismissText: String? = "Cancel",
    onDismiss: (() -> Unit)? = null,
    isDestructive: Boolean = false,
    content: (@Composable () -> Unit)? = null
) {
    val isDark = isSystemInDarkTheme()
    val haptic = LocalHapticFeedback.current
    val bgBrush = if (isDark) {
        Brush.verticalGradient(listOf(Color(0xFF2C2C2E), Color(0xFF1C1C1E)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF7F7FA)))
    }
    val titleColor = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val messageColor = if (isDark) IosDarkLabelSecondary else IosLabelSecondary
    val separatorColor = if (isDark) IosDarkSeparator else IosSeparator
    val confirmColor = if (isDestructive) {
        if (isDark) AppleDarkSystemRed else AppleSystemRed
    } else {
        if (isDark) AppleDarkSystemBlue else AppleSystemBlue
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .width(280.dp)
                .shadow(16.dp, RoundedCornerShape(14.dp), spotColor = Color(0x40000000))
                .background(bgBrush, RoundedCornerShape(14.dp))
                .border(0.5.dp, if (isDark) Color(0x33FFFFFF) else Color(0x20000000), RoundedCornerShape(14.dp))
                .clip(RoundedCornerShape(14.dp))
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Column(
                    modifier = Modifier.padding(top = 18.dp, start = 16.dp, end = 16.dp, bottom = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = titleColor,
                        letterSpacing = (-0.4).sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    if (message != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = message,
                            fontSize = 13.sp,
                            color = messageColor,
                            lineHeight = 17.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                    if (content != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        content()
                    }
                }

                HorizontalDivider(color = separatorColor, thickness = 0.5.dp)

                if (dismissText != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable(role = Role.Button) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onDismiss?.invoke() ?: onDismissRequest()
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dismissText,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Normal,
                                color = if (isDark) AppleDarkSystemBlue else AppleSystemBlue
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(0.5.dp)
                                .fillMaxHeight()
                                .background(separatorColor)
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable(role = Role.Button) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onConfirm()
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = confirmText,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = confirmColor
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(role = Role.Button) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onConfirm()
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = confirmText,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = confirmColor
                        )
                    }
                }
            }
        }
    }
}

internal data class HsrpPlateColors(
    val bg: Color,
    val text: Color,
    val border: Color,
    val placeholder: Color
)

internal fun hsrpPlateColors(style: NumberPlateStyle): HsrpPlateColors = when (style) {
    NumberPlateStyle.PRIVATE_WHITE -> HsrpPlateColors(
        bg = Color(0xFFFAFAFA),
        text = Color(0xFF111317),
        border = Color(0xFF1C1C1E),
        placeholder = Color(0xFF5A5A60)
    )
    NumberPlateStyle.COMMERCIAL_YELLOW -> HsrpPlateColors(
        bg = Color(0xFFFFD60A),
        text = Color(0xFF111317),
        border = Color(0xFF1C1C1E),
        placeholder = Color(0xFF4A3E00)
    )
    NumberPlateStyle.EV_GREEN -> HsrpPlateColors(
        bg = Color(0xFF0F6830),
        text = Color(0xFFFFFFFF),
        border = Color(0xFF083D1B),
        placeholder = Color(0xFFD1FAE5)
    )
    NumberPlateStyle.COMMERCIAL_EV -> HsrpPlateColors(
        bg = Color(0xFF0F5E2C),
        text = Color(0xFFFFD60A),
        border = Color(0xFF083D1B),
        placeholder = Color(0xFFFEF08A)
    )
}

/**
 * Authentic High-Security Registration Plate (HSRP) for Indian Motor Vehicles
 * Renders the blue IND band with emblem, symmetric snap-lock rivets, and embossed registration.
 * Dynamically supports:
 * - Private (White plate, dark text)
 * - Commercial (Yellow plate, dark text)
 * - Electric Vehicle / EV (Green #0F6830 plate, White text)
 * - Commercial EV / E-Rickshaw / EV Taxi (Green #0F5E2C plate, #FFD60A text)
 */
@Composable
fun HsrpNumberPlate(
    regNo: String,
    isCommercial: Boolean = false,
    makeModel: String? = null,
    style: NumberPlateStyle? = null,
    modifier: Modifier = Modifier
) {
    val resolvedStyle = style ?: if (isCommercial) {
        NumberPlateStyle.COMMERCIAL_YELLOW
    } else {
        VehiclePlateClassifier.detectStyle(makeModel)
    }

    val plateShape = RoundedCornerShape(6.dp)
    val palette = hsrpPlateColors(resolvedStyle)

    val clean = regNo.replace(" ", "").uppercase()
    val formatted = remember(clean) {
        RegistrationNormalizer.formatForDisplay(clean)
    }

    Row(
        modifier = modifier
            .clip(plateShape)
            .background(palette.bg, plateShape)
            .border(
                width = 1.25.dp,
                color = palette.border,
                shape = plateShape
            )
            .height(29.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Blue IND Stripe with Ashoka Chakra & Hologram (decorative for TalkBack)
        Box(
            modifier = Modifier
                .width(19.dp)
                .fillMaxHeight()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0B4DA2),
                            Color(0xFF07397B)
                        )
                    )
                )
                .clearAndSetSemantics {},
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Chromium Ashoka Chakra Hologram Badge (Hot-stamped)
                Canvas(modifier = Modifier.size(7.5.dp)) {
                    val radius = size.minDimension / 2f
                    val center = Offset(size.width / 2f, size.height / 2f)

                    drawCircle(
                        color = Color(0xFFFDE047),
                        radius = radius,
                        center = center
                    )
                    drawCircle(
                        color = Color(0xFF07397B),
                        radius = radius * 0.68f,
                        center = center
                    )
                    drawCircle(
                        color = Color(0xFFFDE047),
                        radius = radius * 0.30f,
                        center = center
                    )
                }

                Spacer(modifier = Modifier.height(1.5.dp))

                // Embossed "IND" Text
                Text(
                    text = "IND",
                    fontSize = 6.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                    color = Color.White,
                    letterSpacing = 0.3.sp,
                    lineHeight = 6.5.sp,
                    maxLines = 1,
                    softWrap = false,
                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                )
            }
        }

        // Left Snap-Lock Rivet Screw (symmetric with right rivet)
        Box(
            modifier = Modifier
                .padding(start = 5.dp)
                .size(3.5.dp)
                .clip(CircleShape)
                .background(palette.text.copy(alpha = 0.35f))
                .clearAndSetSemantics {}
        )

        // Registration Plate Text (Centered vertically, wraps tightly so status badges sit beside it)
        Box(
            modifier = Modifier
                .weight(1f, fill = false)
                .padding(horizontal = 5.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = formatted,
                fontSize = 13.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                fontWeight = FontWeight.Black,
                color = palette.text,
                letterSpacing = 1.0.sp,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                    fontFeatureSettings = "tnum",
                    shadow = Shadow(
                        color = palette.border.copy(alpha = 0.25f),
                        offset = Offset(0f, 0.8f),
                        blurRadius = 0.5f
                    )
                )
            )
        }

        // Right Snap-Lock Rivet Screw (symmetric with left rivet)
        Box(
            modifier = Modifier
                .padding(end = 5.dp)
                .size(3.5.dp)
                .clip(CircleShape)
                .background(palette.text.copy(alpha = 0.35f))
                .clearAndSetSemantics {}
        )
    }
}

/**
 * Unified Authentic HSRP Number Plate Text Field
 * Merges the Indian High-Security Registration Plate (HSRP) and the vehicle registration input into one interactive component.
 */
@Composable
fun HsrpRegistrationTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    plateStyle: NumberPlateStyle = NumberPlateStyle.PRIVATE_WHITE,
    label: String = "Vehicle Registration No *",
    placeholder: String = "DL 01 AB 1234",
    isError: Boolean = false,
    errorMessage: String? = null,
    onFocusLost: (() -> Unit)? = null,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    imeAction: ImeAction = ImeAction.Next
) {
    val isDark = isSystemInDarkTheme()
    var isFocused by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val palette = hsrpPlateColors(plateStyle)
    val errorColor = if (isDark) AppleDarkSystemRed else AppleSystemRed

    val activeBorder = when {
        isError -> errorColor
        isFocused -> if (isDark) AppleDarkSystemBlue else AppleSystemBlue
        else -> palette.border
    }

    val plateShape = RoundedCornerShape(10.dp)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        // Top Label Row with Category Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(
                            when (plateStyle) {
                                NumberPlateStyle.COMMERCIAL_YELLOW -> Color(0xFFEAB308)
                                NumberPlateStyle.EV_GREEN,
                                NumberPlateStyle.COMMERCIAL_EV -> if (isDark) AppleDarkSystemGreen else AppleSystemGreen
                                else -> if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                            }
                        )
                )
                Text(
                    text = plateStyle.displayName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = when (plateStyle) {
                        NumberPlateStyle.COMMERCIAL_YELLOW -> if (isDark) AppleDarkSystemOrange else AppleSystemOrange
                        NumberPlateStyle.EV_GREEN,
                        NumberPlateStyle.COMMERCIAL_EV -> if (isDark) AppleDarkSystemGreen else AppleSystemGreen
                        else -> if (isDark) IosDarkLabelSecondary else IosLabelSecondary
                    }
                )
            }
        }

        // Unified HSRP Plate Input Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(plateShape)
                .background(palette.bg, plateShape)
                .border(
                    width = if (isFocused || isError) 2.dp else 1.25.dp,
                    color = activeBorder,
                    shape = plateShape
                )
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Authentic Blue IND Stripe
                Box(
                    modifier = Modifier
                        .width(30.dp)
                        .fillMaxHeight()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF0B4DA2),
                                    Color(0xFF07397B)
                                )
                            )
                        )
                        .clearAndSetSemantics {},
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Hot-stamped Chromium Ashoka Chakra Hologram Badge
                        Canvas(modifier = Modifier.size(9.dp)) {
                            val radius = size.minDimension / 2f
                            val center = Offset(size.width / 2f, size.height / 2f)

                            drawCircle(
                                color = Color(0xFFFDE047),
                                radius = radius,
                                center = center
                            )
                            drawCircle(
                                color = Color(0xFF07397B),
                                radius = radius * 0.72f,
                                center = center
                            )
                            drawCircle(
                                color = Color(0xFFFDE047),
                                radius = radius * 0.32f,
                                center = center
                            )

                            val spokeCount = 12
                            for (i in 0 until spokeCount) {
                                val angle = (i * 360f / spokeCount) * (Math.PI / 180.0)
                                val start = Offset(
                                    x = center.x + (radius * 0.35f * kotlin.math.cos(angle)).toFloat(),
                                    y = center.y + (radius * 0.35f * kotlin.math.sin(angle)).toFloat()
                                )
                                val end = Offset(
                                    x = center.x + (radius * 0.72f * kotlin.math.cos(angle)).toFloat(),
                                    y = center.y + (radius * 0.72f * kotlin.math.sin(angle)).toFloat()
                                )
                                drawLine(
                                    color = Color(0xFFFDE047),
                                    start = start,
                                    end = end,
                                    strokeWidth = 1f
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "IND",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                            color = Color.White,
                            letterSpacing = 0.5.sp,
                            lineHeight = 9.sp
                        )
                    }
                }

                // Center Input Field
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = 16.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = palette.placeholder,
                            letterSpacing = 1.sp
                        )
                    }

                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                isFocused = focusState.isFocused
                                if (!focusState.isFocused) {
                                    onFocusLost?.invoke()
                                }
                            },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = palette.text,
                            fontSize = 16.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp
                        ),
                        cursorBrush = SolidColor(palette.text),
                        visualTransformation = RegistrationPlateVisualTransformation,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction = imeAction
                        ),
                        keyboardActions = keyboardActions
                    )
                }

                // Right Clear Action Button
                if (value.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable(
                                role = Role.Button,
                                onClickLabel = "Clear registration number",
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onValueChange("")
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = AppleIcons.Clear,
                            contentDescription = "Clear registration number",
                            tint = palette.placeholder,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Error message if present
        if (isError && !errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                fontSize = 11.sp,
                color = errorColor,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 2.dp)
            )
        }
    }
}

/**
 * Interactive Vehicle Category & HSRP Plate Style Selector
 * Allows advisor to choose between Auto-Detect, Private, Commercial, EV, and Commercial EV.
 */
@Composable
fun VehicleCategoryPicker(
    selectedStyle: NumberPlateStyle?,
    detectedStyle: NumberPlateStyle,
    onStyleSelected: (NumberPlateStyle?) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val haptic = LocalHapticFeedback.current

    val options = listOf<Pair<NumberPlateStyle?, String>>(
        null to "Auto",
        NumberPlateStyle.PRIVATE_WHITE to "Private",
        NumberPlateStyle.COMMERCIAL_YELLOW to "Commercial",
        NumberPlateStyle.EV_GREEN to "Electric EV",
        NumberPlateStyle.COMMERCIAL_EV to "Comm EV"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Vehicle Category & Plate Type",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) IosDarkLabelSecondary else IosLabelSecondary,
                letterSpacing = (-0.1).sp
            )
            Text(
                text = if (selectedStyle == null) "Auto: ${detectedStyle.displayName}" else selectedStyle.displayName,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDark) AppleDarkSystemBlue else AppleSystemBlue
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEach { (style, label) ->
                val isSelected = (selectedStyle == style)
                val activeBg = if (isDark) AppleDarkSystemBlue.copy(alpha = 0.20f) else AppleSystemBlue.copy(alpha = 0.12f)
                val inactiveBg = if (isDark) Color(0xFF2C2C2E) else Color.White
                val activeBorder = if (isDark) AppleDarkSystemBlue else AppleSystemBlue
                val inactiveBorder = if (isDark) Color(0x38FFFFFF) else Color(0x24000000)
                val textColor = if (isSelected) {
                    if (isDark) AppleDarkSystemBlue else AppleSystemBlue
                } else {
                    if (isDark) IosDarkLabelPrimary else IosLabelPrimary
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) activeBg else inactiveBg)
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) activeBorder else inactiveBorder,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .selectable(
                            selected = isSelected,
                            role = Role.Button,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onStyleSelected(style)
                            }
                        )
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        when (style) {
                            null -> {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isDark) AppleDarkSystemBlue else AppleSystemBlue)
                                )
                            }
                            NumberPlateStyle.PRIVATE_WHITE -> {
                                Box(
                                    modifier = Modifier
                                        .size(9.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFAFAFA))
                                        .border(1.dp, Color(0xFF1C1C1E), CircleShape)
                                )
                            }
                            NumberPlateStyle.COMMERCIAL_YELLOW -> {
                                Box(
                                    modifier = Modifier
                                        .size(9.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFD60A))
                                        .border(1.dp, Color(0xFF1C1C1E), CircleShape)
                                )
                            }
                            NumberPlateStyle.EV_GREEN -> {
                                Box(
                                    modifier = Modifier
                                        .size(9.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF0F6830))
                                        .border(1.dp, Color(0xFF083D1B), CircleShape)
                                )
                            }
                            NumberPlateStyle.COMMERCIAL_EV -> {
                                Box(
                                    modifier = Modifier
                                        .size(9.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF0F5E2C))
                                        .border(1.dp, Color(0xFFFFD60A), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(3.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFFD60A))
                                    )
                                }
                            }
                        }

                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}

enum class DockTab {
    DASHBOARD,
    CUSTOMERS,
    TIMELINE,
    REPORTS,
    SETTINGS
}

/**
 * Floating Cupertino Glass Dock (Bottom Navigation - iOS HIG)
 * Floating frosted glass dock with GPU-composited gliding spring pill, tab-colored ambient glow,
 * horizontal scrub gesture, keyboard ArrowLeft/ArrowRight & Role.Tab accessibility, and reduced-motion support.
 */
@Composable
fun LiquidGlassDock(
    currentTab: DockTab,
    onTabSelected: (DockTab) -> Unit,
    modifier: Modifier = Modifier,
    onScrollToTop: (() -> Unit)? = null,
    onAddClicked: (() -> Unit)? = null,
    hazeState: HazeState? = null,
    pagerPosition: Float? = null,
    onScrubProgress: ((Float) -> Unit)? = null,
    onScrubFinished: ((Int) -> Unit)? = null
) {
    val isDark = isSystemInDarkTheme()
    val reducedMotion = effectiveReducedMotion()
    val dockShape = RoundedCornerShape(28.dp)
    val pillShape = RoundedCornerShape(24.dp)
    val haptic = LocalHapticFeedback.current
    val activeHaze = hazeState ?: LocalHazeState.current
    val scrollDispatcher = LocalScrollToTopDispatcher.current
    val effectiveScrollToTop = onScrollToTop ?: scrollDispatcher

    var lastTapTime by remember { mutableStateOf(0L) }
    var lastTappedTab by remember { mutableStateOf<DockTab?>(null) }
    var activeDragPosition by remember { mutableStateOf<Float?>(null) }
    var lastScrubbedIndex by remember { mutableStateOf(-1) }

    val tabs = remember {
        listOf(
            DockTab.DASHBOARD to ("Policies" to AppleIcons.House),
            DockTab.CUSTOMERS to ("Clients" to AppleIcons.Person2),
            DockTab.TIMELINE to ("Timeline" to AppleIcons.Calendar),
            DockTab.REPORTS to ("Analytics" to AppleIcons.ChartBar)
        )
    }
    val tabAccents = remember(isDark) {
        if (isDark) {
            listOf(
                AppleDarkSystemBlue,
                AppleDarkSystemPurple,
                AppleDarkSystemOrange,
                AppleDarkSystemGreen
            )
        } else {
            listOf(
                AppleSystemBlue,
                AppleSystemPurple,
                AppleSystemOrange,
                AppleSystemGreen
            )
        }
    }
    val focusRequesters = remember(tabs.size) { List(tabs.size) { FocusRequester() } }
    val latestPagerPos = rememberUpdatedState(pagerPosition)

    val activeDrag = activeDragPosition
    val effectivePosition = activeDrag ?: pagerPosition
    val selectedIndex = if (effectivePosition != null) {
        effectivePosition.roundToInt().coerceIn(0, tabs.size - 1)
    } else {
        tabs.indexOfFirst { it.first == currentTab }.coerceAtLeast(0)
    }

    val animatedAccentState = animateColorAsState(
        targetValue = tabAccents[selectedIndex],
        animationSpec = if (reducedMotion) snap() else AppleSprings.SnappyColor,
        label = "dock_tab_accent"
    )
    val pillProgressState = animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = if (reducedMotion) snap() else AppleSprings.Snappy,
        label = "dock_gliding_pill"
    )
    val pillScaleState = animateFloatAsState(
        targetValue = if (!reducedMotion && activeDrag != null) 1.05f else 1.0f,
        animationSpec = if (reducedMotion) snap() else AppleSprings.Snappy,
        label = "dock_scrub_pill_scale"
    )

    val pillBrush = remember(isDark) {
        Brush.linearGradient(
            colors = if (isDark) {
                listOf(Color(0xFF38383C).copy(alpha = 0.96f), Color(0xFF28282C).copy(alpha = 0.76f))
            } else {
                listOf(Color.White.copy(alpha = 0.96f), Color.White.copy(alpha = 0.76f))
            },
            start = Offset.Zero,
            end = Offset.Infinite
        )
    }
    val inactiveColor = if (isDark) IosDarkLabelSecondary else IosLabelSecondary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .height(56.dp)
                .floatingDockGlassChrome(
                    shape = dockShape,
                    isDark = isDark,
                    hazeState = activeHaze
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            effectiveScrollToTop.invoke()
                        }
                    )
                }
                .padding(4.dp)
        ) {
            val density = LocalDensity.current
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val itemWidth = maxWidth / tabs.size
                val itemWidthPx = with(density) { itemWidth.toPx() }
                val maxIndexF = (tabs.size - 1).toFloat()

                val dragScrubModifier = if (onScrubProgress != null || onScrubFinished != null) {
                    Modifier.pointerInput(tabs.size, itemWidthPx) {
                        var currentDragPos = 0f
                        detectHorizontalDragGestures(
                            onDragStart = { startOffset ->
                                if (itemWidthPx > 0f) {
                                    val pos = ((startOffset.x - itemWidthPx / 2f) / itemWidthPx)
                                        .coerceIn(0f, maxIndexF)
                                    currentDragPos = pos
                                    activeDragPosition = pos
                                    lastScrubbedIndex = pos.roundToInt().coerceIn(0, tabs.size - 1)
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onScrubProgress?.invoke(pos)
                                }
                            },
                            onHorizontalDrag = { change, _ ->
                                if (itemWidthPx > 0f) {
                                    change.consume()
                                    val pos = ((change.position.x - itemWidthPx / 2f) / itemWidthPx)
                                        .coerceIn(0f, maxIndexF)
                                    currentDragPos = pos
                                    activeDragPosition = pos
                                    val crossedIndex = pos.roundToInt().coerceIn(0, tabs.size - 1)
                                    if (crossedIndex != lastScrubbedIndex) {
                                        lastScrubbedIndex = crossedIndex
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                    onScrubProgress?.invoke(pos)
                                }
                            },
                            onDragEnd = {
                                val targetIndex = currentDragPos.roundToInt().coerceIn(0, tabs.size - 1)
                                activeDragPosition = null
                                lastScrubbedIndex = -1
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                if (onScrubFinished != null) {
                                    onScrubFinished(targetIndex)
                                } else {
                                    onTabSelected(tabs[targetIndex].first)
                                }
                            },
                            onDragCancel = {
                                val targetIndex = currentDragPos.roundToInt().coerceIn(0, tabs.size - 1)
                                activeDragPosition = null
                                lastScrubbedIndex = -1
                                if (onScrubFinished != null) {
                                    onScrubFinished(targetIndex)
                                } else {
                                    onTabSelected(tabs[targetIndex].first)
                                }
                            }
                        )
                    }
                } else Modifier

                // 0. Smooth tab-colored ambient glow translated on GPU (no RenderEffect blur)
                Box(
                    modifier = Modifier
                        .width(itemWidth * 1.8f)
                        .fillMaxHeight()
                        .graphicsLayer {
                            val pos = (activeDragPosition ?: latestPagerPos.value ?: pillProgressState.value)
                                .coerceIn(0f, maxIndexF)
                            val centerX = (pos + 0.5f) * itemWidthPx
                            translationX = centerX - (size.width / 2f)
                        }
                        .drawBehind {
                            val glowColor = animatedAccentState.value
                            val glowAlpha = if (isDark) 0.24f else 0.14f
                            drawRect(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        glowColor.copy(alpha = glowAlpha),
                                        glowColor.copy(alpha = glowAlpha * 0.35f),
                                        Color.Transparent
                                    ),
                                    center = Offset(size.width / 2f, size.height / 2f),
                                    radius = size.width * 0.52f
                                )
                            )
                        }
                )

                // 1. Gliding Animated Indicator Pill rendered via GPU translationX & scale
                Box(
                    modifier = Modifier
                        .width(itemWidth)
                        .fillMaxHeight()
                        .graphicsLayer {
                            val pos = (activeDragPosition ?: latestPagerPos.value ?: pillProgressState.value)
                                .coerceIn(0f, maxIndexF)
                            translationX = pos * itemWidthPx
                            val s = pillScaleState.value
                            scaleX = s
                            scaleY = s
                        }
                        .shadow(
                            elevation = 4.dp,
                            shape = pillShape,
                            ambientColor = if (isDark) Color(0x40000000) else animatedAccentState.value.copy(alpha = 0.12f),
                            spotColor = if (isDark) Color(0x40000000) else Color(0x0D000000)
                        )
                        .clip(pillShape)
                        .background(
                            brush = pillBrush,
                            shape = pillShape
                        )
                        .border(
                            width = 1.dp,
                            color = if (isDark) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.90f),
                            shape = pillShape
                        )
                )

                // 2. Interactive Tab Icons & Labels Row (tap, double-tap, drag scrub, ArrowLeft/ArrowRight, Role.Tab)
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(dragScrubModifier)
                        .selectableGroup(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEachIndexed { index, (tab, pair) ->
                        val (label, icon) = pair
                        val isSelected = index == selectedIndex
                        val tabInteractionSource = remember { MutableInteractionSource() }
                        val isPressed by tabInteractionSource.collectIsPressedAsState()

                        val iconScaleState = animateFloatAsState(
                            targetValue = when {
                                reducedMotion -> 1.0f
                                isPressed -> 0.92f
                                isSelected -> 1.14f
                                else -> 1.0f
                            },
                            animationSpec = if (reducedMotion) snap() else AppleSprings.Pop,
                            label = "dock_icon_pop"
                        )
                        val tabPressScaleState = animateFloatAsState(
                            targetValue = if (!reducedMotion && isPressed) 0.94f else 1.0f,
                            animationSpec = if (reducedMotion) snap() else AppleSprings.Pop,
                            label = "dock_tab_press"
                        )
                        val tabColor by animateColorAsState(
                            targetValue = if (isSelected) tabAccents[index] else inactiveColor,
                            animationSpec = if (reducedMotion) snap() else AppleSprings.SnappyColor,
                            label = "dock_tab_tint"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .graphicsLayer {
                                    val s = tabPressScaleState.value
                                    scaleX = s
                                    scaleY = s
                                }
                                .clip(pillShape)
                                .focusRequester(focusRequesters[index])
                                .onPreviewKeyEvent { event ->
                                    if (event.type == KeyEventType.KeyDown) {
                                        when (event.key) {
                                            Key.DirectionRight -> {
                                                val next = (index + 1) % tabs.size
                                                focusRequesters[next].requestFocus()
                                                onTabSelected(tabs[next].first)
                                                true
                                            }
                                            Key.DirectionLeft -> {
                                                val prev = (index - 1 + tabs.size) % tabs.size
                                                focusRequesters[prev].requestFocus()
                                                onTabSelected(tabs[prev].first)
                                                true
                                            }
                                            else -> false
                                        }
                                    } else {
                                        false
                                    }
                                }
                                .selectable(
                                    selected = isSelected,
                                    interactionSource = tabInteractionSource,
                                    indication = null,
                                    role = Role.Tab,
                                    onClick = {
                                        val now = System.currentTimeMillis()
                                        val isDoubleTap = (lastTappedTab == tab && (now - lastTapTime) < 450L)
                                        lastTapTime = now
                                        lastTappedTab = tab

                                        if (isDoubleTap) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            effectiveScrollToTop.invoke()
                                        } else {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onTabSelected(tab)
                                        }
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = tabColor,
                                    modifier = Modifier
                                        .size(19.dp)
                                        .graphicsLayer {
                                            val s = iconScaleState.value
                                            scaleX = s
                                            scaleY = s
                                        }
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = tabColor,
                                    letterSpacing = (-0.1).sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun Modifier.floatingDockGlassChrome(
    shape: Shape,
    isDark: Boolean,
    hazeState: HazeState? = null
): Modifier {
    val hazeMod = if (hazeState != null) {
        try {
            val bg = (if (isDark) Color(0xFF141418) else Color.White).copy(alpha = 0.88f)
            Modifier.hazeEffect(
                state = hazeState,
                style = HazeStyle(
                    backgroundColor = bg,
                    blurRadius = 24.dp,
                    tints = listOf(HazeTint(bg)),
                    noiseFactor = 0.05f
                )
            )
        } catch (_: Throwable) {
            Modifier
        }
    } else Modifier

    return this
        .shadow(
            elevation = 16.dp,
            shape = shape,
            ambientColor = if (isDark) Color(0x66000000) else Color(0x1F0F172A),
            spotColor = if (isDark) Color(0x66000000) else Color(0x14000000)
        )
        .clip(shape)
        .then(hazeMod)
        .background(
            brush = if (isDark) DarkGlassBrush else LightGlassBrush,
            shape = shape
        )
        .border(
            width = 1.1.dp,
            brush = if (isDark) DarkBorderBrush else LightBorderBrush,
            shape = shape
        )
}

/**
 * Suspended floating Liquid Glass capsule dock for sub-screen actions
 * (e.g. Vehicle Details power dock, Add/Edit policy form actions).
 */
@Composable
fun LiquidGlassActionDock(
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    imeAware: Boolean = false,
    dockHeight: Dp = 58.dp,
    topBanner: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val reducedMotion = effectiveReducedMotion()
    val density = LocalDensity.current
    val isImeVisible = imeAware && WindowInsets.ime.getBottom(density) > 0
    val bottomPadding by animateDpAsState(
        targetValue = if (isImeVisible) 8.dp else 14.dp,
        animationSpec = if (reducedMotion) snap() else AppleSprings.SnappyDp,
        label = "action_dock_bottom_inset"
    )
    val dockShape = RoundedCornerShape(29.dp)
    val scrimColor = if (isDark) Color(0xFF000000) else Color(0xFFF2F2F7)
    val insetsModifier = if (imeAware) {
        Modifier.windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
    } else {
        Modifier.navigationBarsPadding()
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Ambient Bottom Scrim so scrolling cards fade gracefully behind the floating dock
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .then(if (imeAware) Modifier.imePadding() else Modifier)
                .height(96.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            scrimColor.copy(alpha = 0.45f),
                            scrimColor.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(insetsModifier)
                .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = bottomPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (topBanner != null) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 440.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    content = topBanner
                )
            }

            Box(
                modifier = Modifier
                    .widthIn(max = 440.dp)
                    .fillMaxWidth()
                    .height(dockHeight)
                    .floatingDockGlassChrome(
                        shape = dockShape,
                        isDark = isDark,
                        hazeState = hazeState
                    )
                    .padding(5.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    content = content
                )
            }
        }
    }
}

/**
 * Apple Liquid Glass Bulk Selection Toolbar
 * Suspended floating bar with Cupertino pill geometry and action buttons.
 */
@Composable
fun LiquidGlassSelectionBar(
    selectedCount: Int,
    totalCount: Int,
    allSelected: Boolean,
    onToggleSelectAll: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val haptic = LocalHapticFeedback.current
    val barShape = RoundedCornerShape(26.dp)
    val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 440.dp)
                .fillMaxWidth()
                .height(58.dp)
                .floatingDockGlassChrome(
                    shape = barShape,
                    isDark = isDark
                )
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Select All toggle pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (allSelected) (if (isDark) AppleDarkSystemBlue else AppleSystemBlue).copy(alpha = 0.2f)
                            else (if (isDark) Color(0x33545458) else Color(0x14767680))
                        )
                        .toggleable(
                            value = allSelected,
                            role = Role.Checkbox,
                            onValueChange = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onToggleSelectAll()
                            }
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(
                                if (allSelected) (if (isDark) AppleDarkSystemBlueFill else AppleSystemBlueFill)
                                else Color.Transparent
                            )
                            .border(
                                width = 1.5.dp,
                                color = if (allSelected) Color.Transparent else (if (isDark) Color(0x88FFFFFF) else Color(0x44000000)),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (allSelected) {
                            Icon(
                                imageVector = AppleIcons.Checkmark,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }
                    Text(
                        text = if (allSelected) "All ($totalCount)" else "$selectedCount / $totalCount",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (allSelected) (if (isDark) AppleDarkSystemBlue else AppleSystemBlue) else labelPrimary
                    )
                }

                // Actions: Share, Delete, Done
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Share
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onShare()
                        },
                        enabled = selectedCount > 0,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = AppleIcons.Share,
                            contentDescription = "Share selected",
                            tint = if (selectedCount > 0) (if (isDark) AppleDarkSystemBlue else AppleSystemBlue) else labelSecondary.copy(alpha = 0.4f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Delete
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDelete()
                        },
                        enabled = selectedCount > 0,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = AppleIcons.Trash,
                            contentDescription = "Delete selected",
                            tint = if (selectedCount > 0) (if (isDark) AppleDarkSystemRed else AppleSystemRed) else labelSecondary.copy(alpha = 0.4f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Done / Close
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) Color(0x33545458) else Color(0x14767680))
                            .clickable(role = Role.Button) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onClose()
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Done",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) AppleDarkSystemBlue else AppleSystemBlue
                        )
                    }
                }
            }
        }
    }
}

/**
 * Apple iOS modal sheet grabber / drag handle (36dp x 5dp pill).
 */
@Composable
fun AppleSheetGrabber(
    modifier: Modifier = Modifier,
    color: Color? = null
) {
    val isDark = isSystemInDarkTheme()
    val grabberColor = color ?: if (isDark) Color(0xFF545458).copy(alpha = 0.65f) else Color(0xFF3C3C43).copy(alpha = 0.3f)
    Box(
        modifier = modifier
            .padding(top = 8.dp, bottom = 4.dp)
            .size(width = 36.dp, height = 5.dp)
            .clip(RoundedCornerShape(2.5.dp))
            .background(grabberColor)
    )
}

/**
 * Apple iOS standard Navigation Bar Text Action (e.g., "Select", "Select All", "Done", "Cancel").
 */
@Composable
fun CupertinoTopBarAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isProminent: Boolean = false,
    enabled: Boolean = true,
    tint: Color? = null
) {
    val isDark = isSystemInDarkTheme()
    val reducedMotion = effectiveReducedMotion()
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val actionTint = tint ?: if (isDark) AppleDarkSystemBlue else AppleSystemBlue

    val alphaState = animateFloatAsState(
        targetValue = when {
            !enabled -> 0.38f
            isPressed -> 0.45f
            else -> 1f
        },
        animationSpec = if (reducedMotion) snap() else AppleSprings.Snappy,
        label = "cupertino_action_alpha"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            )
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .graphicsLayer { this.alpha = alphaState.value },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = actionTint,
            fontSize = 17.sp,
            fontWeight = if (isProminent) FontWeight.SemiBold else FontWeight.Normal,
            letterSpacing = (-0.4).sp
        )
    }
}



