package com.policydesk.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.policydesk.app.ui.theme.AppleSystemBlue
import com.policydesk.app.ui.theme.GlassBorderBottom
import com.policydesk.app.ui.theme.GlassBorderTop
import com.policydesk.app.ui.theme.GlassShadow
import com.policydesk.app.ui.theme.IosLabelPrimary
import com.policydesk.app.ui.theme.IosLabelSecondary
import com.policydesk.app.ui.theme.IosLabelTertiary
import com.policydesk.app.ui.theme.IosSeparator

/**
 * Modifier that applies True Apple Liquid Glass properties:
 * 1. Soft diffused ambient & spot shadow
 * 2. Multi-stop translucent frosted gradient fill
 * 3. Directional specular refraction rim border
 */
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(18.dp),
    elevation: Dp = 4.dp,
    fillAlphaTop: Float = 0.88f,
    fillAlphaBottom: Float = 0.72f,
    borderAlphaTop: Float = 0.80f,
    borderAlphaBottom: Float = 0.15f
): Modifier = this
    .shadow(
        elevation = elevation,
        shape = shape,
        ambientColor = GlassShadow,
        spotColor = GlassShadow
    )
    .background(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = fillAlphaTop),
                Color.White.copy(alpha = fillAlphaBottom)
            )
        ),
        shape = shape
    )
    .border(
        width = 1.dp,
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = borderAlphaTop),
                Color.White.copy(alpha = borderAlphaTop * 0.4f),
                Color.White.copy(alpha = borderAlphaBottom)
            )
        ),
        shape = shape
    )

/**
 * Apple Liquid Glass Inset Card
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    elevation: Dp = 3.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed && onClick != null) 0.985f else 1f, label = "glass_press")

    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    } else Modifier

    Column(
        modifier = modifier
            .scale(scale)
            .liquidGlass(shape = shape, elevation = elevation)
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
    Box(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(
                shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                elevation = 2.dp,
                fillAlphaTop = 0.92f,
                fillAlphaBottom = 0.82f
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
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
                        onClick = onNavigateBack,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                            contentDescription = "Back",
                            tint = AppleSystemBlue,
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
                        color = IosLabelPrimary,
                        letterSpacing = (-0.4).sp
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = IosLabelSecondary,
                            letterSpacing = 0.1.sp
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                actions()
            }
        }
    }
}

/**
 * Apple Liquid Glass Segmented Control
 * Sliding illuminated pill picker for switching tabs or filters.
 */
@Composable
fun <T> LiquidGlassSegmentedControl(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    labelProvider: (T) -> String
) {
    val trackShape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .background(Color(0x14767680), trackShape)
            .border(0.5.dp, Color(0x1F000000), trackShape)
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
                    Modifier
                        .shadow(2.dp, itemShape, spotColor = Color(0x1A000000))
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(Color.White, Color(0xFFFBFBFD))
                            ),
                            shape = itemShape
                        )
                        .border(0.5.dp, Color(0x1A000000), itemShape)
                } else {
                    Modifier
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .then(pillModifier)
                        .clip(itemShape)
                        .clickable { onItemSelected(item) }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = labelProvider(item),
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isSelected) IosLabelPrimary else IosLabelSecondary,
                        letterSpacing = (-0.1).sp
                    )
                }
            }
        }
    }
}

/**
 * Apple Liquid Glass Search Bar
 * Frosted pill search field with instant clear button.
 */
@Composable
fun LiquidGlassSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    onSearchAction: () -> Unit = {}
) {
    val barShape = RoundedCornerShape(14.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(
                shape = barShape,
                elevation = 1.dp,
                fillAlphaTop = 0.90f,
                fillAlphaBottom = 0.78f
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
                tint = IosLabelTertiary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = placeholder,
                        fontSize = 14.sp,
                        color = IosLabelTertiary
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        color = IosLabelPrimary,
                        fontWeight = FontWeight.Normal
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(AppleSystemBlue),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearchAction() }),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (query.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear search",
                    tint = IosLabelTertiary,
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .clickable { onQueryChange("") }
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
    Column(modifier = modifier.fillMaxWidth()) {
        if (title != null) {
            Text(
                text = title.uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = IosLabelSecondary,
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
    val clickModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
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
                        color = IosLabelPrimary
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            fontSize = 12.sp,
                            color = IosLabelSecondary
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (value != null) {
                    Text(
                        text = value,
                        fontSize = 14.sp,
                        color = IosLabelSecondary,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }
                trailingContent?.invoke()
            }
        }

        if (showDivider) {
            HorizontalDivider(
                color = IosSeparator,
                thickness = 0.5.dp,
                modifier = Modifier.padding(start = if (icon != null) 42.dp else 0.dp)
            )
        }
    }
}

/**
 * Apple Liquid Glass Floating Pill Action Button
 */
@Composable
fun LiquidGlassFloatingButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.96f else 1f, label = "fab_press")
    val pillShape = CircleShape

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(6.dp, pillShape, spotColor = AppleSystemBlue.copy(alpha = 0.35f))
            .background(
                brush = Brush.verticalGradient(
                    listOf(AppleSystemBlue, Color(0xFF0063D1))
                ),
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
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
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
