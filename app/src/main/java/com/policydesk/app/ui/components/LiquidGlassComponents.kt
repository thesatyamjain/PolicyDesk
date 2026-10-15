package com.policydesk.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.policydesk.app.ui.theme.AppleDarkSystemBlue
import com.policydesk.app.ui.theme.AppleSystemBlue
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
import com.policydesk.app.ui.theme.IosLabelTertiary
import com.policydesk.app.ui.theme.IosSeparator

/**
 * Modifier that applies True Apple Liquid Glass properties:
 * 1. Soft diffused ambient & spot shadow
 * 2. Multi-stop translucent frosted gradient fill (Dark or Light)
 * 3. Directional specular refraction rim border
 */
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(18.dp),
    elevation: Dp = 3.dp,
    isDark: Boolean = false,
    fillAlphaTop: Float = if (isDark) 0.88f else 0.96f,
    fillAlphaBottom: Float = if (isDark) 0.72f else 0.88f,
    borderAlphaTop: Float = if (isDark) 0.28f else 0.95f,
    borderAlphaBottom: Float = if (isDark) 0.06f else 0.65f
): Modifier {
    val topFill = if (isDark) Color(0xFF242426).copy(alpha = fillAlphaTop) else Color.White.copy(alpha = fillAlphaTop)
    val bottomFill = if (isDark) Color(0xFF161618).copy(alpha = fillAlphaBottom) else Color(0xFFF9F9FB).copy(alpha = fillAlphaBottom)
    val shadowColor = if (isDark) GlassDarkShadow else GlassShadow

    val borderColors = if (isDark) {
        listOf(
            Color.White.copy(alpha = borderAlphaTop),
            Color.White.copy(alpha = borderAlphaTop * 0.4f),
            Color.White.copy(alpha = borderAlphaBottom)
        )
    } else {
        listOf(
            Color.White.copy(alpha = borderAlphaTop),
            Color(0xFFE5E5EA).copy(alpha = 0.85f),
            Color(0xFFD1D1D6).copy(alpha = borderAlphaBottom)
        )
    }

    return this
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = shadowColor,
            spotColor = shadowColor
        )
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(topFill, bottomFill)
            ),
            shape = shape
        )
        .border(
            width = 1.dp,
            brush = Brush.verticalGradient(
                colors = borderColors
            ),
            shape = shape
        )
}

/**
 * Apple Liquid Glass Inset Card
 * Supports fluid spring physics and Cupertino tactile haptic feedback.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    elevation: Dp = 3.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.975f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "glass_card_spring"
    )

    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
        )
    } else Modifier

    Column(
        modifier = modifier
            .scale(scale)
            .liquidGlass(shape = shape, elevation = elevation, isDark = isDark)
            .clip(shape)
            .then(clickModifier)
            .padding(16.dp),
        content = content
    )
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
    actions: @Composable () -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()
    val haptic = LocalHapticFeedback.current
    val primaryText = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val secondaryText = if (isDark) IosDarkLabelSecondary else IosLabelSecondary
    val accentBlue = if (isDark) AppleDarkSystemBlue else AppleSystemBlue

    Box(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(
                shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                elevation = 3.dp,
                isDark = isDark,
                fillAlphaTop = if (isDark) 0.94f else 0.96f,
                fillAlphaBottom = if (isDark) 0.84f else 0.90f
            )
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
                            imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                            contentDescription = "Back",
                            tint = accentBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Column {
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
                horizontalArrangement = Arrangement.spacedBy(4.dp)
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
                imageVector = Icons.Default.Search,
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
                    imageVector = Icons.Default.Clear,
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
                text = title.uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = captionColor,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(start = 16.dp, bottom = 6.dp, top = 10.dp)
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
            .scale(scale)
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
