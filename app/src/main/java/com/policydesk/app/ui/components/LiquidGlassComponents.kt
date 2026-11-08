package com.policydesk.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.input.KeyboardCapitalization
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import com.policydesk.app.domain.model.NumberPlateStyle
import com.policydesk.app.domain.model.VehiclePlateClassifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import com.policydesk.app.domain.model.RegistrationNormalizer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.IntrinsicSize
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.policydesk.app.ui.theme.AppleDarkSystemBlue
import com.policydesk.app.ui.theme.AppleDarkSystemGreen
import com.policydesk.app.ui.theme.AppleDarkSystemRed
import com.policydesk.app.ui.theme.AppleIcons
import com.policydesk.app.ui.theme.AppleSystemBlue
import com.policydesk.app.ui.theme.AppleSystemGreen
import com.policydesk.app.ui.theme.AppleSystemRed
import com.policydesk.app.ui.theme.GlassBorderBottom
import com.policydesk.app.ui.theme.GlassBorderTop
import com.policydesk.app.ui.theme.GlassDarkBorderBottom
import com.policydesk.app.ui.theme.GlassDarkBorderTop
import com.policydesk.app.ui.theme.GlassDarkShadow
import com.policydesk.app.ui.theme.GlassDarkSurface
import com.policydesk.app.ui.theme.GlassDarkSurfaceSubtle
import com.policydesk.app.ui.theme.GlassShadow
import com.policydesk.app.ui.theme.GlassSurface
import com.policydesk.app.ui.theme.GlassSurfaceSubtle
import com.policydesk.app.ui.theme.IosDarkLabelPrimary
import com.policydesk.app.ui.theme.IosDarkLabelSecondary
import com.policydesk.app.ui.theme.IosDarkLabelTertiary
import com.policydesk.app.ui.theme.IosDarkSeparator
import com.policydesk.app.ui.theme.IosLabelPrimary
import com.policydesk.app.ui.theme.IosLabelSecondary
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.pointerInput
import com.policydesk.app.ui.theme.IosLabelTertiary
import com.policydesk.app.ui.theme.IosSeparator

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

private val DarkMeniscusColors = listOf(
    Color(0xFF1E1E22).copy(alpha = 0.85f),
    Color(0xFF141416).copy(alpha = 0.65f),
    Color(0xFF0F0F12).copy(alpha = 0.50f),
    Color(0xFF1A1A1E).copy(alpha = 0.70f)
)

private val LightMeniscusColors = listOf(
    Color.White.copy(alpha = 0.82f),
    Color.White.copy(alpha = 0.46f),
    Color.White.copy(alpha = 0.35f),
    Color.White.copy(alpha = 0.55f)
)

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

/**
 * Modifier that applies True Apple Liquid Glass (matching My Warranties):
 * 1. Dual-layer ambient & contact edge shadow (Light mode) or pure surface elevation (Dark mode)
 * 2. 4-stop diagonal meniscus refraction gradient
 * 3. 1.1dp 92% white dual-incident specular border
 */
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(18.dp),
    elevation: Dp = 4.dp,
    isDark: Boolean = false,
    fillAlphaTop: Float = if (isDark) 0.68f else 0.82f,
    fillAlphaBottom: Float = if (isDark) 0.46f else 0.55f,
    borderAlphaTop: Float = if (isDark) 0.28f else 0.92f,
    borderAlphaBottom: Float = if (isDark) 0.08f else 0.50f
): Modifier {
    val borderColor = Color.White.copy(alpha = borderAlphaTop)
    val brush = if (isDark) DarkGlassBrush else LightGlassBrush

    // In dark mode, iOS design uses surface luminance hierarchy and specular edge highlights.
    // Offscreen GPU Gaussian shadows on black backgrounds are invisible and drop scroll frames.
    val shadowModifier = if (!isDark && elevation > 0.dp) {
        Modifier.shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = Color(0x140F172A),
            spotColor = Color(0x140F172A)
        )
    } else {
        Modifier
    }

    return this
        .then(shadowModifier)
        .clip(shape)
        .background(
            brush = brush,
            shape = shape
        )
        .border(
            width = 1.1.dp,
            color = borderColor,
            shape = shape
        )
}

/**
 * Apple Liquid Glass Inset Card
 * Supports fluid spring physics and Cupertino tactile haptic feedback.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    elevation: Dp = 3.dp,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && (onClick != null || onLongClick != null)) 0.962f else 1f,
        animationSpec = spring(
            dampingRatio = 0.78f,
            stiffness = 380f
        ),
        label = "glass_card_spring"
    )

    val clickModifier = if (onClick != null || onLongClick != null) {
        Modifier.combinedClickable(
            interactionSource = interactionSource,
            indication = null,
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
                    scaleX = scale
                    scaleY = scale
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
                        letterSpacing = (-0.4).sp
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
 * Sliding illuminated pill picker for switching tabs or filters with haptic feedback.
 */
@Composable
fun <T> LiquidGlassSegmentedControl(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    labelProvider: (T) -> String
) {
    val isDark = isSystemInDarkTheme()
    val haptic = LocalHapticFeedback.current
    val trackShape = RoundedCornerShape(12.dp)
    val trackBg = if (isDark) Color(0x33545458) else Color(0x14767680)
    val trackBorder = if (isDark) Color(0x33FFFFFF) else Color(0x1F000000)

    Box(
        modifier = modifier
            .background(trackBg, trackShape)
            .border(0.5.dp, trackBorder, trackShape)
            .padding(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items.forEach { item ->
                val isSelected = item == selectedItem
                val itemShape = RoundedCornerShape(10.dp)

                val pillModifier = if (isSelected) {
                    val pillBg = if (isDark) {
                        Brush.verticalGradient(listOf(Color(0xFF3A3A3C), Color(0xFF2C2C2E)))
                    } else {
                        Brush.verticalGradient(listOf(Color.White, Color(0xFFFBFBFD)))
                    }
                    val pillBorder = if (isDark) Color(0x40FFFFFF) else Color(0x1A000000)

                    Modifier
                        .shadow(2.dp, itemShape, spotColor = Color(0x1A000000))
                        .background(brush = pillBg, shape = itemShape)
                        .border(0.5.dp, pillBorder, itemShape)
                } else {
                    Modifier
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .then(pillModifier)
                        .clip(itemShape)
                        .clickable {
                            if (!isSelected) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onItemSelected(item)
                            }
                        }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val selectedColor = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
                    val unselectedColor = if (isDark) IosDarkLabelSecondary else IosLabelSecondary

                    Text(
                        text = labelProvider(item),
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isSelected) selectedColor else unselectedColor,
                        letterSpacing = (-0.1).sp
                    )
                }
            }
        }
    }
}

/**
 * Apple Liquid Glass Search Bar
 * Frosted pill search field with instant clear button and haptic confirmation.
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
    val haptic = LocalHapticFeedback.current
    val barShape = RoundedCornerShape(14.dp)
    val primaryText = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val placeholderColor = if (isDark) IosDarkLabelTertiary else IosLabelTertiary
    val accentBlue = if (isDark) AppleDarkSystemBlue else AppleSystemBlue

    Box(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(
                shape = barShape,
                elevation = 1.dp,
                isDark = isDark,
                fillAlphaTop = if (isDark) 0.90f else 0.90f,
                fillAlphaBottom = if (isDark) 0.78f else 0.78f
            )
            .padding(horizontal = 12.dp, vertical = 9.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = AppleIcons.Search,
                contentDescription = null,
                tint = placeholderColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = placeholder,
                        fontSize = 14.sp,
                        color = placeholderColor
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
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (query.isNotEmpty()) {
                Icon(
                    imageVector = AppleIcons.Clear,
                    contentDescription = "Clear search",
                    tint = placeholderColor,
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onQueryChange("")
                        }
                )
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
 * Features spring press physics and Cupertino haptic actuation.
 */
@Composable
fun LiquidGlassFloatingButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "fab_spring_press"
    )
    val pillShape = CircleShape
    val blueGradient = if (isDark) {
        listOf(AppleDarkSystemBlue, Color(0xFF005AC1))
    } else {
        listOf(AppleSystemBlue, Color(0xFF0063D1))
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(6.dp, pillShape, spotColor = AppleSystemBlue.copy(alpha = 0.35f))
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
    shape: Shape = RoundedCornerShape(12.dp)
) {
    val isDark = isSystemInDarkTheme()
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled && !isLoading) 0.965f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "apple_button_spring"
    )

    val actualContainerColor = if (!enabled) {
        if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)
    } else {
        containerColor
    }

    val actualContentColor = if (!enabled) {
        if (isDark) Color(0xFF636366) else Color(0xFF8E8E93)
    } else {
        contentColor
    }

    val buttonBrush = if (!enabled) {
        SolidColor(actualContainerColor)
    } else {
        val topBlue = if (isDark) AppleDarkSystemBlue else AppleSystemBlue
        val bottomBlue = if (isDark) Color(0xFF0056B3) else Color(0xFF005AC1)
        Brush.verticalGradient(listOf(topBlue, bottomBlue))
    }

    val shadowColor = if (enabled) {
        if (isDark) AppleDarkSystemBlue.copy(alpha = 0.35f) else AppleSystemBlue.copy(alpha = 0.30f)
    } else Color.Transparent

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
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
                            colors = listOf(Color.White.copy(alpha = 0.35f), Color.Transparent),
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
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            )
            .padding(horizontal = 20.dp),
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
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.2).sp
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
    shape: Shape = RoundedCornerShape(12.dp)
) {
    val isDark = isSystemInDarkTheme()
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
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

    val contentColor = if (!enabled) {
        if (isDark) Color(0xFF636366) else Color(0xFF8E8E93)
    } else {
        tintColor
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
                scaleX = scale
                scaleY = scale
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
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            )
            .padding(horizontal = 16.dp),
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
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.1).sp
            )
        }
    }
}

/**
 * Authentic Cupertino Switch (iOS 18 HIG)
 * 51dp x 31dp track with spring animated thumb, AppleSystemGreen active state, and haptics.
 */
@Composable
fun AppleSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val isDark = isSystemInDarkTheme()
    val haptic = LocalHapticFeedback.current
    val activeColor = if (isDark) AppleDarkSystemGreen else AppleSystemGreen
    val inactiveColor = if (isDark) Color(0xFF39393D) else Color(0xFFE9E9EA)

    val trackColor by animateColorAsState(
        targetValue = if (checked) activeColor else inactiveColor,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "switch_track_color"
    )

    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 22.dp else 2.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "switch_thumb_offset"
    )

    Box(
        modifier = modifier
            .size(width = 51.dp, height = 31.dp)
            .background(trackColor, CircleShape)
            .clip(CircleShape)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                enabled = enabled,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onCheckedChange(!checked)
                }
            )
            .padding(2.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
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
                                .clickable {
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
                                .clickable {
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
                            .clickable {
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

/**
 * Authentic High-Security Registration Plate (HSRP) for Indian Motor Vehicles
 * Renders the blue IND band with emblem and sharp, embossed monospaced vehicle registration.
 * Dynamically supports:
 * - Private (White plate, Black text)
 * - Commercial (Yellow plate, Black text)
 * - Electric Vehicle / EV (Green plate, White text)
 * - Commercial EV / E-Rickshaw / EV Taxi (Green plate, Yellow text)
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

    val plateShape = RoundedCornerShape(5.dp)

    // Accurate RTO colors according to MoRTH specifications
    val (plateBg, plateTextColor, plateBorderColor) = when (resolvedStyle) {
        NumberPlateStyle.PRIVATE_WHITE -> Triple(
            Color(0xFFFAFAFA),
            Color(0xFF111827),
            Color(0xFF94A3B8)
        )
        NumberPlateStyle.COMMERCIAL_YELLOW -> Triple(
            Color(0xFFFFD600), // Pure Indian Commercial Yellow
            Color(0xFF111827),
            Color(0xFFCA8A04)
        )
        NumberPlateStyle.EV_GREEN -> Triple(
            Color(0xFF15803D),
            Color(0xFFFFFFFF),
            Color(0xFF166534)
        )
        NumberPlateStyle.COMMERCIAL_EV -> Triple(
            Color(0xFF15803D),
            Color(0xFFFFD600),
            Color(0xFF166534)
        )
    }

    val clean = regNo.replace(" ", "").uppercase()
    val formatted = remember(clean) {
        RegistrationNormalizer.formatForDisplay(clean)
    }

    // Deterministic 10-digit laser PIN derived from regNo (e.g. AA 10294812)
    val laserPin = remember(clean) {
        val hash = (clean.hashCode().toLong() and 0xFFFFFFFFL).toString().padStart(8, '0').takeLast(8)
        "AA$hash"
    }

    Row(
        modifier = modifier
            .clip(plateShape)
            .background(plateBg, plateShape)
            .border(
                width = 1.2.dp,
                color = plateBorderColor,
                shape = plateShape
            )
            .height(29.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Blue IND Stripe with Ashoka Chakra & Hologram
        Box(
            modifier = Modifier
                .width(20.dp)
                .fillMaxHeight()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0038A8), // Deep International Optical Blue
                            Color(0xFF002277)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Chromium Ashoka Chakra Hologram Badge (Hot-stamped)
                Canvas(modifier = Modifier.size(7.dp)) {
                    val radius = size.minDimension / 2f
                    val center = Offset(size.width / 2f, size.height / 2f)

                    // Outer chromium reflective ring
                    drawCircle(
                        color = Color(0xFFFDE047),
                        radius = radius,
                        center = center
                    )
                    drawCircle(
                        color = Color(0xFF002277),
                        radius = radius * 0.72f,
                        center = center
                    )
                    drawCircle(
                        color = Color(0xFFFDE047),
                        radius = radius * 0.32f,
                        center = center
                    )

                    // 12 spokes for high-density miniature representation
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
                            strokeWidth = 0.8f
                        )
                    }
                }

                Spacer(modifier = Modifier.height(1.5.dp))

                // Embossed "IND" Text
                Text(
                    text = "IND",
                    fontSize = 6.5.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                    color = Color.White,
                    letterSpacing = 0.4.sp,
                    lineHeight = 7.sp
                )
            }
        }

        // Center Registration Plate Text & Laser Watermark
        Box(
            modifier = Modifier
                .padding(start = 7.dp, end = 5.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.CenterStart
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                // Main Registration Display (DIN 1451 geometric monoline styling)
                Text(
                    text = formatted,
                    fontSize = 13.5.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                    fontWeight = FontWeight.Black,
                    color = plateTextColor,
                    letterSpacing = 1.4.sp,
                    style = TextStyle(
                        shadow = Shadow(
                            color = plateBorderColor.copy(alpha = 0.35f),
                            offset = Offset(0.5f, 0.8f),
                            blurRadius = 0.5f
                        )
                    )
                )

                // Laser-etched Serial / PIN watermark (Bottom border)
                Text(
                    text = laserPin,
                    fontSize = 4.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    color = plateTextColor.copy(alpha = 0.45f),
                    letterSpacing = 0.3.sp,
                    lineHeight = 5.sp
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Right Snap-Lock Rivet Screw Simulation
        Box(
            modifier = Modifier
                .padding(end = 5.dp)
                .size(4.dp)
                .clip(CircleShape)
                .background(plateBorderColor.copy(alpha = 0.65f))
        )
    }
}

/**
 * Unified Authentic HSRP Number Plate Text Field
 * Merges the Indian High-Security Registration Plate (HSRP) and the vehicle registration input into one interactive component.
 * Features:
 * - Left authentic deep blue IND stripe with golden chakra dot and "IND" text.
 * - Dynamic plate background and font color matching Private, Commercial, EV, and Commercial EV.
 * - Monospaced bold embossed typography with real-time bidirectional spacing transformation.
 * - Top label with live category badge and colored status indicator.
 * - 1-tap clear action icon with haptic response.
 * - Full validation error styling and focus states.
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

    val (plateBg, plateTextColor, plateBorderColor) = when (plateStyle) {
        NumberPlateStyle.PRIVATE_WHITE -> Triple(
            Color(0xFFF8FAFC),
            Color(0xFF0F172A),
            Color(0xFF94A3B8)
        )
        NumberPlateStyle.COMMERCIAL_YELLOW -> Triple(
            Color(0xFFFACC15),
            Color(0xFF0F172A),
            Color(0xFFCA8A04)
        )
        NumberPlateStyle.EV_GREEN -> Triple(
            Color(0xFF15803D),
            Color(0xFFFFFFFF),
            Color(0xFF166534)
        )
        NumberPlateStyle.COMMERCIAL_EV -> Triple(
            Color(0xFF15803D),
            Color(0xFFFDE047),
            Color(0xFF166534)
        )
    }

    val activeBorder = when {
        isError -> Color(0xFFEF4444)
        isFocused -> if (isDark) AppleDarkSystemBlue else AppleSystemBlue
        else -> plateBorderColor
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
                                NumberPlateStyle.COMMERCIAL_EV -> Color(0xFF16A34A)
                                else -> if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                            }
                        )
                )
                Text(
                    text = plateStyle.displayName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = when (plateStyle) {
                        NumberPlateStyle.COMMERCIAL_YELLOW -> Color(0xFFCA8A04)
                        NumberPlateStyle.EV_GREEN,
                        NumberPlateStyle.COMMERCIAL_EV -> Color(0xFF16A34A)
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
                .background(plateBg, plateShape)
                .border(
                    width = if (isFocused || isError) 2.dp else 1.2.dp,
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
                                    Color(0xFF0038A8),
                                    Color(0xFF002277)
                                )
                            )
                        ),
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
                                color = Color(0xFF002277),
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
                            color = plateTextColor.copy(alpha = 0.35f),
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
                            color = plateTextColor,
                            fontSize = 16.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp
                        ),
                        cursorBrush = SolidColor(
                            when (plateStyle) {
                                NumberPlateStyle.EV_GREEN -> Color.White
                                NumberPlateStyle.COMMERCIAL_EV -> Color(0xFFFDE047)
                                else -> Color(0xFF0F172A)
                            }
                        ),
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
                            .padding(end = 8.dp)
                            .size(28.dp)
                            .clip(CircleShape)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onValueChange("")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = AppleIcons.Clear,
                            contentDescription = "Clear",
                            tint = plateTextColor.copy(alpha = 0.45f),
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
                color = Color(0xFFEF4444),
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
                .horizontalScroll(rememberScrollState()),
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
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onStyleSelected(style)
                        }
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
                                        .background(Color(0xFFF8FAFC))
                                        .border(1.dp, Color(0xFF94A3B8), CircleShape)
                                )
                            }
                            NumberPlateStyle.COMMERCIAL_YELLOW -> {
                                Box(
                                    modifier = Modifier
                                        .size(9.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFACC15))
                                        .border(1.dp, Color(0xFFCA8A04), CircleShape)
                                )
                            }
                            NumberPlateStyle.EV_GREEN -> {
                                Box(
                                    modifier = Modifier
                                        .size(9.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF15803D))
                                        .border(1.dp, Color(0xFF166534), CircleShape)
                                )
                            }
                            NumberPlateStyle.COMMERCIAL_EV -> {
                                Box(
                                    modifier = Modifier
                                        .size(9.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF15803D))
                                        .border(1.dp, Color(0xFFFDE047), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(3.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFDE047))
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
 * Exact recreation of My Warranties floating glass dock with gliding animated selection pill.
 */
@Composable
fun LiquidGlassDock(
    currentTab: DockTab,
    onTabSelected: (DockTab) -> Unit,
    modifier: Modifier = Modifier,
    onScrollToTop: (() -> Unit)? = null,
    onAddClicked: (() -> Unit)? = null,
    hazeState: HazeState? = null,
    pagerPosition: Float? = null
) {
    val isDark = isSystemInDarkTheme()
    val dockShape = RoundedCornerShape(28.dp)
    val haptic = LocalHapticFeedback.current
    val activeHaze = hazeState ?: LocalHazeState.current
    val scrollDispatcher = LocalScrollToTopDispatcher.current
    val effectiveScrollToTop = onScrollToTop ?: scrollDispatcher

    var lastTapTime by remember { mutableStateOf(0L) }
    var lastTappedTab by remember { mutableStateOf<DockTab?>(null) }

    val tabs = remember {
        listOf(
            DockTab.DASHBOARD to ("Policies" to AppleIcons.House),
            DockTab.CUSTOMERS to ("Clients" to AppleIcons.Person2),
            DockTab.TIMELINE to ("Timeline" to AppleIcons.Calendar),
            DockTab.REPORTS to ("Analytics" to AppleIcons.ChartBar)
        )
    }

    val selectedIndex = if (pagerPosition != null) {
        pagerPosition.roundToInt().coerceIn(0, tabs.size - 1)
    } else {
        tabs.indexOfFirst { it.first == currentTab }.coerceAtLeast(0)
    }

    val hazeMod = if (activeHaze != null) {
        try {
            val bg = (if (isDark) Color(0xFF121215) else Color.White).copy(alpha = 0.60f)
            Modifier.hazeEffect(
                state = activeHaze,
                style = HazeStyle(
                    backgroundColor = bg,
                    blurRadius = 26.dp,
                    tints = listOf(HazeTint(bg)),
                    noiseFactor = 0.05f
                )
            )
        } catch (_: Throwable) {
            Modifier
        }
    } else Modifier

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .height(56.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = dockShape,
                    ambientColor = if (isDark) Color(0x66000000) else Color(0x1F0F172A),
                    spotColor = if (isDark) Color(0x66000000) else Color(0x14000000)
                )
                .clip(dockShape)
                .then(hazeMod)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            effectiveScrollToTop.invoke()
                        }
                    )
                }
                .background(
                    brush = Brush.linearGradient(
                        colors = if (isDark) {
                            listOf(
                                Color(0xFF242428).copy(alpha = if (activeHaze != null) 0.38f else 0.80f),
                                Color(0xFF161618).copy(alpha = if (activeHaze != null) 0.28f else 0.50f),
                                Color(0xFF101012).copy(alpha = if (activeHaze != null) 0.18f else 0.35f),
                                Color(0xFF1E1E22).copy(alpha = if (activeHaze != null) 0.28f else 0.55f)
                            )
                        } else {
                            listOf(
                                Color.White.copy(alpha = if (activeHaze != null) 0.45f else 0.82f),
                                Color.White.copy(alpha = if (activeHaze != null) 0.30f else 0.46f),
                                Color.White.copy(alpha = if (activeHaze != null) 0.20f else 0.35f),
                                Color.White.copy(alpha = if (activeHaze != null) 0.30f else 0.55f)
                            )
                        },
                        start = Offset.Zero,
                        end = Offset.Infinite
                    ),
                    shape = dockShape
                )
                .border(
                    width = 1.1.dp,
                    brush = Brush.verticalGradient(
                        colors = if (isDark) listOf(
                            Color.White.copy(alpha = 0.35f),
                            Color.White.copy(alpha = 0.12f),
                            Color.White.copy(alpha = 0.04f)
                        ) else listOf(
                            Color.White.copy(alpha = 0.90f),
                            Color.White.copy(alpha = 0.35f),
                            Color.White.copy(alpha = 0.15f)
                        )
                    ),
                    shape = dockShape
                )
                .padding(4.dp)
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val itemWidth = maxWidth / tabs.size
                val animatedPillOffset by animateDpAsState(
                    targetValue = itemWidth * selectedIndex,
                    animationSpec = spring(
                        dampingRatio = 0.76f,
                        stiffness = 380f
                    ),
                    label = "dock_gliding_pill"
                )
                val pillOffset = if (pagerPosition != null) {
                    itemWidth * pagerPosition.coerceIn(0f, (tabs.size - 1).toFloat())
                } else {
                    animatedPillOffset
                }

                // 1. Gliding Animated Indicator Pill (My Warranties)
                Box(
                    modifier = Modifier
                        .offset(x = pillOffset)
                        .width(itemWidth)
                        .fillMaxHeight()
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(24.dp),
                            ambientColor = if (isDark) Color(0x40000000) else AppleSystemBlue.copy(alpha = 0.08f),
                            spotColor = if (isDark) Color(0x40000000) else Color(0x0D000000)
                        )
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = if (isDark) {
                                    listOf(Color(0xFF38383C).copy(alpha = 0.96f), Color(0xFF28282C).copy(alpha = 0.76f))
                                } else {
                                    listOf(Color.White.copy(alpha = 0.96f), Color.White.copy(alpha = 0.76f))
                                },
                                start = Offset.Zero,
                                end = Offset.Infinite
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isDark) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.90f),
                            shape = RoundedCornerShape(24.dp)
                        )
                )

                // 2. Interactive Tab Icons & Labels Row
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEachIndexed { index, (tab, pair) ->
                        val (label, icon) = pair
                        val isSelected = index == selectedIndex
                        val activeColor = if (isDark) AppleDarkSystemBlue else AppleSystemBlue
                        val inactiveColor = if (isDark) IosDarkLabelSecondary else IosLabelSecondary

                        val iconScale by animateFloatAsState(
                            targetValue = if (isSelected) 1.14f else 1.0f,
                            animationSpec = spring(
                                dampingRatio = 0.52f,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            label = "dock_icon_pop"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(24.dp))
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
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
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (isSelected) activeColor else inactiveColor,
                                    modifier = Modifier
                                        .size(19.dp)
                                        .graphicsLayer {
                                            scaleX = iconScale
                                            scaleY = iconScale
                                        }
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) activeColor else inactiveColor,
                                    letterSpacing = (-0.1).sp
                                )
                            }
                        }
                    }
                }
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
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 440.dp)
                .fillMaxWidth()
                .height(60.dp)
                .liquidGlass(shape = barShape, elevation = 8.dp, isDark = isDark)
                .clip(barShape)
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
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onToggleSelectAll()
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(
                                if (allSelected) (if (isDark) AppleDarkSystemBlue else AppleSystemBlue)
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
                            tint = if (selectedCount > 0) AppleSystemRed else labelSecondary.copy(alpha = 0.4f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Done / Close
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) Color(0x33545458) else Color(0x14767680))
                            .clickable {
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
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val actionTint = tint ?: if (isDark) AppleDarkSystemBlue else AppleSystemBlue

    val alpha by animateFloatAsState(
        targetValue = when {
            !enabled -> 0.38f
            isPressed -> 0.45f
            else -> 1f
        },
        label = "cupertino_action_alpha"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            )
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .graphicsLayer { this.alpha = alpha },
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



