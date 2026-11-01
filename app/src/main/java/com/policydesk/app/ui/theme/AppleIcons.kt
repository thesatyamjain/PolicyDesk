package com.policydesk.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Apple Design Language (SF Symbols) Iconography for PolicyDesk
 * Clean, fluid, human-scaled vector icons matching iOS 18 HIG.
 */
object AppleIcons {

    private fun buildIcon(
        name: String,
        pathFillType: PathFillType = PathFillType.NonZero,
        block: PathBuilder.() -> Unit
    ): ImageVector {
        return ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            fill = SolidColor(Color.White),
            pathFillType = pathFillType,
            pathBuilder = block
        ).build()
    }

    /** SF Symbol: house.fill (Cupertino home / terminal tab) */
    val House: ImageVector by lazy {
        buildIcon("AppleHouse") {
            moveTo(12f, 3f)
            lineTo(2.5f, 10.5f)
            curveTo(2.1f, 10.8f, 2f, 11.3f, 2.3f, 11.7f)
            curveTo(2.6f, 12f, 3.1f, 12.1f, 3.5f, 11.8f)
            lineTo(4f, 11.4f)
            lineTo(4f, 20f)
            curveTo(4f, 21.1f, 4.9f, 22f, 6f, 22f)
            lineTo(9.5f, 22f)
            curveTo(10f, 22f, 10.5f, 21.5f, 10.5f, 21f)
            lineTo(10.5f, 15.5f)
            curveTo(10.5f, 14.7f, 11.2f, 14f, 12f, 14f)
            curveTo(12.8f, 14f, 13.5f, 14.7f, 13.5f, 15.5f)
            lineTo(13.5f, 21f)
            curveTo(13.5f, 21.5f, 14f, 22f, 14.5f, 22f)
            lineTo(18f, 22f)
            curveTo(19.1f, 22f, 20f, 21.1f, 20f, 20f)
            lineTo(20f, 11.4f)
            lineTo(20.5f, 11.8f)
            curveTo(20.9f, 12.1f, 21.4f, 12f, 21.7f, 11.7f)
            curveTo(22f, 11.3f, 21.9f, 10.8f, 21.5f, 10.5f)
            lineTo(12f, 3f)
            close()
        }
    }

    /** SF Symbol: person.fill (Cupertino single user avatar) */
    val Person: ImageVector by lazy {
        buildIcon("ApplePerson") {
            // Head
            moveTo(12f, 4.5f)
            curveTo(9.8f, 4.5f, 8f, 6.3f, 8f, 8.5f)
            curveTo(8f, 10.7f, 9.8f, 12.5f, 12f, 12.5f)
            curveTo(14.2f, 12.5f, 16f, 10.7f, 16f, 8.5f)
            curveTo(16f, 6.3f, 14.2f, 4.5f, 12f, 4.5f)
            close()

            // Body / shoulders
            moveTo(12f, 14f)
            curveTo(7.6f, 14f, 4f, 16.5f, 4f, 20f)
            curveTo(4f, 20.6f, 4.4f, 21f, 5f, 21f)
            lineTo(19f, 21f)
            curveTo(19.6f, 21f, 20f, 20.6f, 20f, 20f)
            curveTo(20f, 16.5f, 16.4f, 14f, 12f, 14f)
            close()
        }
    }

    /** SF Symbol: person.2.fill (Cupertino client contacts) */
    val Person2: ImageVector by lazy {
        buildIcon("ApplePerson2") {
            // Front person head
            moveTo(9f, 4f)
            curveTo(6.8f, 4f, 5f, 5.8f, 5f, 8f)
            curveTo(5f, 10.2f, 6.8f, 12f, 9f, 12f)
            curveTo(11.2f, 12f, 13f, 10.2f, 13f, 8f)
            curveTo(13f, 5.8f, 11.2f, 4f, 9f, 4f)
            close()

            // Front person body
            moveTo(9f, 13.5f)
            curveTo(5.5f, 13.5f, 2f, 15.5f, 2f, 19f)
            curveTo(2f, 20f, 2.8f, 20.5f, 3.8f, 20.5f)
            lineTo(14.2f, 20.5f)
            curveTo(15.2f, 20.5f, 16f, 20f, 16f, 19f)
            curveTo(16f, 15.5f, 12.5f, 13.5f, 9f, 13.5f)
            close()

            // Back person head
            moveTo(16.5f, 5.5f)
            curveTo(14.8f, 5.5f, 13.8f, 6.8f, 13.8f, 8.2f)
            curveTo(13.8f, 8.8f, 14f, 9.4f, 14.3f, 9.9f)
            curveTo(14.9f, 9.6f, 15.6f, 9.5f, 16.5f, 9.5f)
            curveTo(17.8f, 9.5f, 18.9f, 10.2f, 19.5f, 11.2f)
            curveTo(19.8f, 10.4f, 20f, 9.4f, 20f, 8.2f)
            curveTo(20f, 6.5f, 18.5f, 5.5f, 16.5f, 5.5f)
            close()

            // Back person shoulder
            moveTo(17f, 13.5f)
            curveTo(16.5f, 13.5f, 16f, 13.6f, 15.5f, 13.8f)
            curveTo(16.8f, 15.2f, 17.5f, 17f, 17.5f, 19.2f)
            lineTo(21.2f, 19.2f)
            curveTo(21.8f, 19.2f, 22f, 18.8f, 22f, 18.2f)
            curveTo(22f, 15.2f, 19.5f, 13.5f, 17f, 13.5f)
            close()
        }
    }

    /** SF Symbol: calendar (Apple Calendar with binder tabs) */
    val Calendar: ImageVector by lazy {
        buildIcon("AppleCalendar") {
            // Main calendar rounded card
            moveTo(6.5f, 4f)
            lineTo(7.5f, 4f)
            lineTo(7.5f, 2.5f)
            curveTo(7.5f, 2f, 8f, 1.5f, 8.5f, 1.5f)
            curveTo(9f, 1.5f, 9.5f, 2f, 9.5f, 2.5f)
            lineTo(9.5f, 4f)
            lineTo(14.5f, 4f)
            lineTo(14.5f, 2.5f)
            curveTo(14.5f, 2f, 15f, 1.5f, 15.5f, 1.5f)
            curveTo(16f, 1.5f, 16.5f, 2f, 16.5f, 2.5f)
            lineTo(16.5f, 4f)
            lineTo(17.5f, 4f)
            curveTo(20f, 4f, 21.5f, 5.5f, 21.5f, 8f)
            lineTo(21.5f, 18f)
            curveTo(21.5f, 20.5f, 20f, 22f, 17.5f, 22f)
            lineTo(6.5f, 22f)
            curveTo(4f, 22f, 2.5f, 20.5f, 2.5f, 18f)
            lineTo(2.5f, 8f)
            curveTo(2.5f, 5.5f, 4f, 4f, 6.5f, 4f)
            close()

            // Inner cut out to form Apple calendar frame
            moveTo(4.5f, 9f)
            lineTo(19.5f, 9f)
            lineTo(19.5f, 8f)
            curveTo(19.5f, 6.5f, 18.5f, 5.8f, 17.2f, 5.8f)
            lineTo(6.8f, 5.8f)
            curveTo(5.5f, 5.8f, 4.5f, 6.5f, 4.5f, 8f)
            close()

            // Inset calendar date pills
            moveTo(6.5f, 12f)
            lineTo(8.5f, 12f)
            lineTo(8.5f, 14f)
            lineTo(6.5f, 14f)
            close()

            moveTo(11f, 12f)
            lineTo(13f, 12f)
            lineTo(13f, 14f)
            lineTo(11f, 14f)
            close()

            moveTo(15.5f, 12f)
            lineTo(17.5f, 12f)
            lineTo(17.5f, 14f)
            lineTo(15.5f, 14f)
            close()

            moveTo(6.5f, 16.5f)
            lineTo(8.5f, 16.5f)
            lineTo(8.5f, 18.5f)
            lineTo(6.5f, 18.5f)
            close()

            moveTo(11f, 16.5f)
            lineTo(13f, 16.5f)
            lineTo(13f, 18.5f)
            lineTo(11f, 18.5f)
            close()

            moveTo(15.5f, 16.5f)
            lineTo(17.5f, 16.5f)
            lineTo(17.5f, 18.5f)
            lineTo(15.5f, 18.5f)
            close()
        }
    }

    /** SF Symbol: chart.bar.fill (Apple 3-capsule histogram) */
    val ChartBar: ImageVector by lazy {
        buildIcon("AppleChartBar") {
            // Left bar
            moveTo(4.5f, 14f)
            curveTo(4.5f, 12.6f, 5.6f, 11.5f, 7f, 11.5f)
            curveTo(8.4f, 11.5f, 9.5f, 12.6f, 9.5f, 14f)
            lineTo(9.5f, 20.5f)
            lineTo(4.5f, 20.5f)
            close()

            // Middle bar (tallest)
            moveTo(10.5f, 6.5f)
            curveTo(10.5f, 5.1f, 11.6f, 4f, 13f, 4f)
            curveTo(14.4f, 4f, 15.5f, 5.1f, 15.5f, 6.5f)
            lineTo(15.5f, 20.5f)
            lineTo(10.5f, 20.5f)
            close()

            // Right bar
            moveTo(16.5f, 10f)
            curveTo(16.5f, 8.6f, 17.6f, 7.5f, 19f, 7.5f)
            curveTo(20.4f, 7.5f, 21.5f, 8.6f, 21.5f, 10f)
            lineTo(21.5f, 20.5f)
            lineTo(16.5f, 20.5f)
            close()
        }
    }

    /** SF Symbol: gearshape.fill / System settings gear with precision center hole */
    val GearShape: ImageVector by lazy {
        buildIcon("AppleGearShape", pathFillType = PathFillType.EvenOdd) {
            // Central hole cutout (center 12, 12, radius 3.5)
            moveTo(12f, 8.5f)
            curveTo(13.93f, 8.5f, 15.5f, 10.07f, 15.5f, 12f)
            curveTo(15.5f, 13.93f, 13.93f, 15.5f, 12f, 15.5f)
            curveTo(10.07f, 15.5f, 8.5f, 13.93f, 8.5f, 12f)
            curveTo(8.5f, 10.07f, 10.07f, 8.5f, 12f, 8.5f)
            close()

            // Symmetrical gear teeth centered at (12, 12)
            moveTo(19.14f, 12.94f)
            curveTo(19.18f, 12.64f, 19.2f, 12.33f, 19.2f, 12f)
            curveTo(19.2f, 11.68f, 19.18f, 11.36f, 19.13f, 11.06f)
            lineTo(21.16f, 9.48f)
            curveTo(21.34f, 9.34f, 21.39f, 9.07f, 21.28f, 8.87f)
            lineTo(19.36f, 5.55f)
            curveTo(19.24f, 5.33f, 18.99f, 5.26f, 18.77f, 5.33f)
            lineTo(16.38f, 6.29f)
            curveTo(15.88f, 5.91f, 15.35f, 5.59f, 14.76f, 5.35f)
            lineTo(14.4f, 2.81f)
            curveTo(14.36f, 2.57f, 14.16f, 2.4f, 13.92f, 2.4f)
            lineTo(10.08f, 2.4f)
            curveTo(9.84f, 2.4f, 9.65f, 2.57f, 9.61f, 2.81f)
            lineTo(9.25f, 5.35f)
            curveTo(8.66f, 5.59f, 8.12f, 5.92f, 7.63f, 6.29f)
            lineTo(5.24f, 5.33f)
            curveTo(5.02f, 5.25f, 4.77f, 5.33f, 4.65f, 5.55f)
            lineTo(2.73f, 8.87f)
            curveTo(2.61f, 9.08f, 2.65f, 9.34f, 2.85f, 9.48f)
            lineTo(4.88f, 11.06f)
            curveTo(4.83f, 11.36f, 4.79f, 11.69f, 4.79f, 12f)
            curveTo(4.79f, 12.32f, 4.81f, 12.64f, 4.86f, 12.94f)
            lineTo(2.83f, 14.52f)
            curveTo(2.65f, 14.66f, 2.6f, 14.93f, 2.71f, 15.13f)
            lineTo(4.63f, 18.45f)
            curveTo(4.75f, 18.67f, 5f, 18.74f, 5.22f, 18.67f)
            lineTo(7.61f, 17.71f)
            curveTo(8.11f, 18.09f, 8.64f, 18.41f, 9.23f, 18.65f)
            lineTo(9.59f, 21.19f)
            curveTo(9.64f, 21.43f, 9.83f, 21.6f, 10.08f, 21.6f)
            lineTo(13.92f, 21.6f)
            curveTo(14.16f, 21.6f, 14.36f, 21.43f, 14.39f, 21.19f)
            lineTo(14.75f, 18.65f)
            curveTo(15.34f, 18.41f, 15.88f, 18.08f, 16.37f, 17.71f)
            lineTo(18.76f, 18.67f)
            curveTo(18.98f, 18.75f, 19.23f, 18.67f, 19.35f, 18.45f)
            lineTo(21.27f, 15.13f)
            curveTo(21.39f, 14.91f, 21.34f, 14.66f, 21.15f, 14.52f)
            lineTo(19.14f, 12.94f)
            close()
        }
    }

    /** SF Symbol: car.fill (Apple Maps aerodynamic vehicle) */
    val Car: ImageVector by lazy {
        buildIcon("AppleCar") {
            moveTo(18.9f, 7.5f)
            curveTo(18.5f, 6.2f, 17.3f, 5.3f, 15.9f, 5.2f)
            lineTo(8.1f, 5.2f)
            curveTo(6.7f, 5.3f, 5.5f, 6.2f, 5.1f, 7.5f)
            lineTo(3.2f, 12.5f)
            curveTo(2.5f, 12.8f, 2f, 13.5f, 2f, 14.3f)
            lineTo(2f, 18f)
            curveTo(2f, 18.8f, 2.7f, 19.5f, 3.5f, 19.5f)
            lineTo(4.5f, 19.5f)
            curveTo(5.3f, 19.5f, 6f, 18.8f, 6f, 18f)
            lineTo(6f, 17.2f)
            lineTo(18f, 17.2f)
            lineTo(18f, 18f)
            curveTo(18f, 18.8f, 18.7f, 19.5f, 19.5f, 19.5f)
            lineTo(20.5f, 19.5f)
            curveTo(21.3f, 19.5f, 22f, 18.8f, 22f, 18f)
            lineTo(22f, 14.3f)
            curveTo(22f, 13.5f, 21.5f, 12.8f, 20.8f, 12.5f)
            close()

            // Headlights & Windshield cuts
            moveTo(5.5f, 14.5f)
            curveTo(4.7f, 14.5f, 4f, 13.8f, 4f, 13f)
            curveTo(4f, 12.2f, 4.7f, 11.5f, 5.5f, 11.5f)
            curveTo(6.3f, 11.5f, 7f, 12.2f, 7f, 13f)
            curveTo(7f, 13.8f, 6.3f, 14.5f, 5.5f, 14.5f)
            close()

            moveTo(18.5f, 14.5f)
            curveTo(17.7f, 14.5f, 17f, 13.8f, 17f, 13f)
            curveTo(17f, 12.2f, 17.7f, 11.5f, 18.5f, 11.5f)
            curveTo(19.3f, 11.5f, 20f, 12.2f, 20f, 13f)
            curveTo(20f, 13.8f, 19.3f, 14.5f, 18.5f, 14.5f)
            close()

            moveTo(6.2f, 10.2f)
            lineTo(7.5f, 7.2f)
            curveTo(7.7f, 6.8f, 8.2f, 6.5f, 8.6f, 6.5f)
            lineTo(15.4f, 6.5f)
            curveTo(15.8f, 6.5f, 16.3f, 6.8f, 16.5f, 7.2f)
            lineTo(17.8f, 10.2f)
            close()
        }
    }

    /** SF Symbol: plus (Cupertino rounded capsule cross) */
    val Plus: ImageVector by lazy {
        buildIcon("ApplePlus") {
            moveTo(12f, 4f)
            curveTo(12.7f, 4f, 13.2f, 4.5f, 13.2f, 5.2f)
            lineTo(13.2f, 10.8f)
            lineTo(18.8f, 10.8f)
            curveTo(19.5f, 10.8f, 20f, 11.3f, 20f, 12f)
            curveTo(20f, 12.7f, 19.5f, 13.2f, 18.8f, 13.2f)
            lineTo(13.2f, 13.2f)
            lineTo(13.2f, 18.8f)
            curveTo(13.2f, 19.5f, 12.7f, 20f, 12f, 20f)
            curveTo(11.3f, 20f, 10.8f, 19.5f, 10.8f, 18.8f)
            lineTo(10.8f, 13.2f)
            lineTo(5.2f, 13.2f)
            curveTo(4.5f, 13.2f, 4f, 12.7f, 4f, 12f)
            curveTo(4f, 11.3f, 4.5f, 10.8f, 5.2f, 10.8f)
            lineTo(10.8f, 10.8f)
            lineTo(10.8f, 5.2f)
            curveTo(10.8f, 4.5f, 11.3f, 4f, 12f, 4f)
            close()
        }
    }

    /** SF Symbol: magnifyingglass (Apple 45° search glass) */
    val Search: ImageVector by lazy {
        buildIcon("AppleSearch") {
            moveTo(10.5f, 3.5f)
            curveTo(6.6f, 3.5f, 3.5f, 6.6f, 3.5f, 10.5f)
            curveTo(3.5f, 14.4f, 6.6f, 17.5f, 10.5f, 17.5f)
            curveTo(12.2f, 17.5f, 13.7f, 16.9f, 14.9f, 15.9f)
            lineTo(19.3f, 20.3f)
            curveTo(19.7f, 20.7f, 20.3f, 20.7f, 20.7f, 20.3f)
            curveTo(21.1f, 19.9f, 21.1f, 19.3f, 20.7f, 18.9f)
            lineTo(16.3f, 14.5f)
            curveTo(17.1f, 13.4f, 17.5f, 12f, 17.5f, 10.5f)
            curveTo(17.5f, 6.6f, 14.4f, 3.5f, 10.5f, 3.5f)
            close()

            moveTo(10.5f, 5.5f)
            curveTo(13.3f, 5.5f, 15.5f, 7.7f, 15.5f, 10.5f)
            curveTo(15.5f, 13.3f, 13.3f, 15.5f, 10.5f, 15.5f)
            curveTo(7.7f, 15.5f, 5.5f, 13.3f, 5.5f, 10.5f)
            curveTo(5.5f, 7.7f, 7.7f, 5.5f, 10.5f, 5.5f)
            close()
        }
    }

    /** SF Symbol: square.and.pencil (Apple Notes compose action) */
    val SquareAndPencil: ImageVector by lazy {
        buildIcon("AppleSquareAndPencil") {
            // Document box with rounded corners
            moveTo(4f, 5f)
            curveTo(4f, 3.9f, 4.9f, 3f, 6f, 3f)
            lineTo(13f, 3f)
            lineTo(13f, 5f)
            lineTo(6f, 5f)
            lineTo(6f, 19f)
            lineTo(19f, 19f)
            lineTo(19f, 12f)
            lineTo(21f, 12f)
            lineTo(21f, 19f)
            curveTo(21f, 20.1f, 20.1f, 21f, 19f, 21f)
            lineTo(6f, 21f)
            curveTo(4.9f, 21f, 4f, 20.1f, 4f, 19f)
            close()

            // Stylus pencil angled at 45°
            moveTo(20.7f, 4.3f)
            curveTo(21.1f, 4.7f, 21.1f, 5.3f, 20.7f, 5.7f)
            lineTo(19.3f, 7.1f)
            lineTo(17.4f, 5.2f)
            lineTo(18.8f, 3.8f)
            curveTo(19.2f, 3.4f, 19.8f, 3.4f, 20.2f, 3.8f)
            close()

            moveTo(16.5f, 6.1f)
            lineTo(18.4f, 8f)
            lineTo(11.5f, 14.9f)
            lineTo(9.5f, 15.5f)
            lineTo(10.1f, 13.5f)
            close()
        }
    }

    /** SF Symbol: tablecells.fill (Apple Numbers rounded spreadsheet grid) */
    val TableCells: ImageVector by lazy {
        buildIcon("AppleTableCells", pathFillType = PathFillType.EvenOdd) {
            moveTo(6f, 3.5f)
            lineTo(18f, 3.5f)
            curveTo(20.2f, 3.5f, 21.5f, 4.8f, 21.5f, 7f)
            lineTo(21.5f, 17f)
            curveTo(21.5f, 19.2f, 20.2f, 20.5f, 18f, 20.5f)
            lineTo(6f, 20.5f)
            curveTo(3.8f, 20.5f, 2.5f, 19.2f, 2.5f, 17f)
            lineTo(2.5f, 7f)
            curveTo(2.5f, 4.8f, 3.8f, 3.5f, 6f, 3.5f)
            close()

            // Grid cutout grooves
            moveTo(4.5f, 11f)
            lineTo(10.5f, 11f)
            lineTo(10.5f, 5.5f)
            lineTo(6.5f, 5.5f)
            curveTo(5.2f, 5.5f, 4.5f, 6.2f, 4.5f, 7.5f)
            close()

            moveTo(12.5f, 11f)
            lineTo(19.5f, 11f)
            lineTo(19.5f, 7.5f)
            curveTo(19.5f, 6.2f, 18.8f, 5.5f, 17.5f, 5.5f)
            lineTo(12.5f, 5.5f)
            close()

            moveTo(4.5f, 13f)
            lineTo(4.5f, 16.5f)
            curveTo(4.5f, 17.8f, 5.2f, 18.5f, 6.5f, 18.5f)
            lineTo(10.5f, 18.5f)
            lineTo(10.5f, 13f)
            close()

            moveTo(12.5f, 13f)
            lineTo(12.5f, 18.5f)
            lineTo(17.5f, 18.5f)
            curveTo(18.8f, 18.5f, 19.5f, 17.8f, 19.5f, 16.5f)
            lineTo(19.5f, 13f)
            close()
        }
    }

    /** SF Symbol: doc.text.fill (Apple PDF / policy document) */
    val DocText: ImageVector by lazy {
        buildIcon("AppleDocText", pathFillType = PathFillType.EvenOdd) {
            moveTo(6f, 2.5f)
            lineTo(14f, 2.5f)
            lineTo(20f, 8.5f)
            lineTo(20f, 19.5f)
            curveTo(20f, 21f, 18.8f, 22f, 17.5f, 22f)
            lineTo(6f, 22f)
            curveTo(4.5f, 22f, 3.5f, 21f, 3.5f, 19.5f)
            lineTo(3.5f, 5f)
            curveTo(3.5f, 3.5f, 4.5f, 2.5f, 6f, 2.5f)
            close()

            // Folded corner
            moveTo(13.5f, 3f)
            lineTo(13.5f, 8f)
            lineTo(18.5f, 8f)
            close()

            // Inset document lines
            moveTo(6.5f, 12f)
            lineTo(16.5f, 12f)
            lineTo(16.5f, 13.5f)
            lineTo(6.5f, 13.5f)
            close()

            moveTo(6.5f, 15.5f)
            lineTo(14.5f, 15.5f)
            lineTo(14.5f, 17f)
            lineTo(6.5f, 17f)
            close()
        }
    }

    /** SF Symbol: checkmark.shield.fill (Apple Security & Verification) */
    val ShieldCheck: ImageVector by lazy {
        buildIcon("AppleShieldCheck", pathFillType = PathFillType.EvenOdd) {
            moveTo(12f, 2f)
            curveTo(14.5f, 4f, 17.5f, 4.5f, 20f, 4.5f)
            curveTo(20.5f, 4.5f, 21f, 4.9f, 21f, 5.5f)
            lineTo(21f, 11.2f)
            curveTo(21f, 16.5f, 17.2f, 20.8f, 12.3f, 22f)
            curveTo(12.1f, 22f, 11.9f, 22f, 11.7f, 22f)
            curveTo(6.8f, 20.8f, 3f, 16.5f, 3f, 11.2f)
            lineTo(3f, 5.5f)
            curveTo(3f, 4.9f, 3.5f, 4.5f, 4f, 4.5f)
            curveTo(6.5f, 4.5f, 9.5f, 4f, 12f, 2f)
            close()

            // Checkmark knockout
            moveTo(10.5f, 15.5f)
            lineTo(7f, 12f)
            lineTo(8.4f, 10.6f)
            lineTo(10.5f, 12.7f)
            lineTo(15.6f, 7.6f)
            lineTo(17f, 9f)
            close()
        }
    }

    /** SF Symbol: phone.fill (Apple smooth telephone handset) */
    val Phone: ImageVector by lazy {
        buildIcon("ApplePhone") {
            moveTo(19.2f, 15.8f)
            lineTo(16.5f, 14.6f)
            curveTo(15.9f, 14.3f, 15.2f, 14.5f, 14.8f, 15f)
            lineTo(13.6f, 16.2f)
            curveTo(11.4f, 15.1f, 9.4f, 13.1f, 8.3f, 10.9f)
            lineTo(9.5f, 9.7f)
            curveTo(10f, 9.2f, 10.2f, 8.5f, 9.9f, 7.9f)
            lineTo(8.7f, 5.2f)
            curveTo(8.4f, 4.5f, 7.6f, 4.1f, 6.9f, 4.3f)
            lineTo(4.4f, 5.1f)
            curveTo(3.6f, 5.3f, 3f, 6.1f, 3f, 7f)
            curveTo(3f, 15.3f, 9.7f, 22f, 18f, 22f)
            curveTo(18.9f, 22f, 19.7f, 21.4f, 19.9f, 20.6f)
            lineTo(20.7f, 18.1f)
            curveTo(20.9f, 17.4f, 20.5f, 16.6f, 19.2f, 15.8f)
            close()
        }
    }

    /** SF Symbol: bubble.left.fill (Apple iMessage bubble) */
    val Message: ImageVector by lazy {
        buildIcon("AppleMessage") {
            moveTo(12f, 3.5f)
            curveTo(6.8f, 3.5f, 2.5f, 7.3f, 2.5f, 12f)
            curveTo(2.5f, 14.6f, 3.7f, 16.9f, 5.7f, 18.4f)
            lineTo(4.2f, 21.5f)
            curveTo(4f, 21.9f, 4.3f, 22.3f, 4.7f, 22.2f)
            lineTo(8.6f, 20.6f)
            curveTo(9.7f, 20.9f, 10.8f, 21.1f, 12f, 21.1f)
            curveTo(17.2f, 21.1f, 21.5f, 17.3f, 21.5f, 12f)
            curveTo(21.5f, 7.3f, 17.2f, 3.5f, 12f, 3.5f)
            close()
        }
    }

    /** WhatsApp brand icon (Speech bubble with phone receiver cutout) */
    val WhatsApp: ImageVector by lazy {
        buildIcon("AppleWhatsApp", pathFillType = PathFillType.EvenOdd) {
            // Outer speech bubble
            moveTo(12.04f, 2f)
            curveTo(6.58f, 2f, 2.13f, 6.45f, 2.13f, 11.91f)
            curveTo(2.13f, 13.66f, 2.59f, 15.36f, 3.45f, 16.86f)
            lineTo(2.05f, 22f)
            lineTo(7.3f, 20.62f)
            curveTo(8.75f, 21.41f, 10.38f, 21.83f, 12.04f, 21.83f)
            curveTo(17.5f, 21.83f, 21.95f, 17.38f, 21.95f, 11.92f)
            curveTo(21.95f, 9.27f, 20.92f, 6.78f, 19.05f, 4.91f)
            curveTo(17.18f, 3.03f, 14.69f, 2f, 12.04f, 2f)
            close()

            // Inner phone handset receiver cutout
            moveTo(17.53f, 14.37f)
            curveTo(17.23f, 14.22f, 15.75f, 13.49f, 15.48f, 13.39f)
            curveTo(15.2f, 13.29f, 15.01f, 13.24f, 14.81f, 13.54f)
            curveTo(14.62f, 13.84f, 14.07f, 14.48f, 13.9f, 14.68f)
            curveTo(13.73f, 14.88f, 13.56f, 14.9f, 13.26f, 14.75f)
            curveTo(12.96f, 14.6f, 12.01f, 14.29f, 10.88f, 13.28f)
            curveTo(10f, 12.5f, 9.4f, 11.53f, 9.23f, 11.23f)
            curveTo(9.06f, 10.93f, 9.21f, 10.77f, 9.36f, 10.62f)
            curveTo(9.5f, 10.49f, 9.66f, 10.27f, 9.81f, 10.1f)
            curveTo(9.96f, 9.93f, 10.01f, 9.81f, 10.11f, 9.61f)
            curveTo(10.21f, 9.41f, 10.16f, 9.24f, 10.08f, 9.09f)
            curveTo(10.01f, 8.94f, 9.41f, 7.47f, 9.17f, 6.87f)
            curveTo(8.92f, 6.29f, 8.68f, 6.37f, 8.5f, 6.36f)
            curveTo(8.33f, 6.35f, 8.13f, 6.35f, 7.93f, 6.35f)
            curveTo(7.74f, 6.35f, 7.42f, 6.42f, 7.15f, 6.72f)
            curveTo(6.88f, 7.01f, 6.12f, 7.72f, 6.12f, 9.17f)
            curveTo(6.12f, 10.62f, 7.18f, 12.02f, 7.32f, 12.22f)
            curveTo(7.47f, 12.41f, 9.41f, 15.41f, 12.38f, 16.69f)
            curveTo(13.08f, 17f, 13.64f, 17.18f, 14.06f, 17.32f)
            curveTo(14.77f, 17.54f, 15.42f, 17.51f, 15.93f, 17.43f)
            curveTo(16.5f, 17.35f, 17.68f, 16.72f, 17.93f, 16.02f)
            curveTo(18.17f, 15.32f, 18.17f, 14.72f, 18.1f, 14.6f)
            curveTo(18.03f, 14.48f, 17.83f, 14.41f, 17.53f, 14.37f)
            close()
        }
    }

    /** SF Symbol: clock.arrow.circlepath (Apple renewal & history) */
    val History: ImageVector by lazy {
        buildIcon("AppleHistory") {
            moveTo(13f, 3f)
            curveTo(17.9f, 3.5f, 21.5f, 7.5f, 21.5f, 12.5f)
            curveTo(21.5f, 17.7f, 17.2f, 22f, 12f, 22f)
            curveTo(6.8f, 22f, 2.5f, 17.7f, 2.5f, 12.5f)
            curveTo(2.5f, 9.8f, 3.7f, 7.4f, 5.6f, 5.7f)
            lineTo(3.8f, 4f)
            lineTo(8.5f, 3.5f)
            lineTo(8f, 8.2f)
            lineTo(6.7f, 6.9f)
            curveTo(5.2f, 8.3f, 4.3f, 10.3f, 4.3f, 12.5f)
            curveTo(4.3f, 16.7f, 7.8f, 20.2f, 12f, 20.2f)
            curveTo(16.2f, 20.2f, 19.7f, 16.7f, 19.7f, 12.5f)
            curveTo(19.7f, 8.6f, 16.8f, 5.3f, 13f, 4.8f)
            close()

            // Clock hands
            moveTo(11.2f, 7f)
            lineTo(12.8f, 7f)
            lineTo(12.8f, 12f)
            lineTo(16f, 14f)
            lineTo(15.2f, 15.3f)
            lineTo(11.2f, 12.8f)
            close()
        }
    }

    /** SF Symbol: checkmark (Apple rounded tick) */
    val Checkmark: ImageVector by lazy {
        buildIcon("AppleCheckmark") {
            moveTo(9.5f, 17.5f)
            curveTo(9.1f, 17.5f, 8.7f, 17.3f, 8.4f, 17f)
            lineTo(4.4f, 13f)
            curveTo(3.8f, 12.4f, 3.8f, 11.5f, 4.4f, 10.9f)
            curveTo(5f, 10.3f, 5.9f, 10.3f, 6.5f, 10.9f)
            lineTo(9.5f, 13.9f)
            lineTo(17.5f, 5.9f)
            curveTo(18.1f, 5.3f, 19f, 5.3f, 19.6f, 5.9f)
            curveTo(20.2f, 6.5f, 20.2f, 7.4f, 19.6f, 8f)
            lineTo(10.6f, 17f)
            curveTo(10.3f, 17.3f, 9.9f, 17.5f, 9.5f, 17.5f)
            close()
        }
    }

    /** SF Symbol: exclamationmark.triangle.fill (Apple caution triangle) */
    val Warning: ImageVector by lazy {
        buildIcon("AppleWarning", pathFillType = PathFillType.EvenOdd) {
            moveTo(10.3f, 3.4f)
            curveTo(11f, 2.1f, 13f, 2.1f, 13.7f, 3.4f)
            lineTo(22.3f, 18.2f)
            curveTo(23f, 19.5f, 22.1f, 21f, 20.6f, 21f)
            lineTo(3.4f, 21f)
            curveTo(1.9f, 21f, 1f, 19.5f, 1.7f, 18.2f)
            close()

            // Exclamation pill with rounded caps (center x = 12)
            moveTo(12f, 8f)
            curveTo(12.65f, 8f, 13.15f, 8.45f, 13.1f, 9.1f)
            lineTo(12.7f, 13.2f)
            curveTo(12.65f, 13.7f, 12.2f, 14.1f, 12f, 14.1f)
            curveTo(11.8f, 14.1f, 11.35f, 13.7f, 11.3f, 13.2f)
            lineTo(10.9f, 9.1f)
            curveTo(10.85f, 8.45f, 11.35f, 8f, 12f, 8f)
            close()

            // Exclamation dot
            moveTo(12f, 15.7f)
            curveTo(12.65f, 15.7f, 13.15f, 16.2f, 13.15f, 16.85f)
            curveTo(13.15f, 17.5f, 12.65f, 18f, 12f, 18f)
            curveTo(11.35f, 18f, 10.85f, 17.5f, 10.85f, 16.85f)
            curveTo(10.85f, 16.2f, 11.35f, 15.7f, 12f, 15.7f)
            close()
        }
    }

    /** SF Symbol: xmark.circle.fill (Apple search clear pill) */
    val Clear: ImageVector by lazy {
        buildIcon("AppleClear", pathFillType = PathFillType.EvenOdd) {
            moveTo(12f, 2f)
            curveTo(6.5f, 2f, 2f, 6.5f, 2f, 12f)
            curveTo(2f, 17.5f, 6.5f, 22f, 12f, 22f)
            curveTo(17.5f, 22f, 22f, 17.5f, 22f, 12f)
            curveTo(22f, 6.5f, 17.5f, 2f, 12f, 2f)
            close()

            // Inset X
            moveTo(15.5f, 9.9f)
            lineTo(14.1f, 8.5f)
            lineTo(12f, 10.6f)
            lineTo(9.9f, 8.5f)
            lineTo(8.5f, 9.9f)
            lineTo(10.6f, 12f)
            lineTo(8.5f, 14.1f)
            lineTo(9.9f, 15.5f)
            lineTo(12f, 13.4f)
            lineTo(14.1f, 15.5f)
            lineTo(15.5f, 14.1f)
            lineTo(13.4f, 12f)
            close()
        }
    }

    /** SF Symbol: checkmark.circle.fill (Apple verified status) */
    val CheckCircle: ImageVector by lazy {
        buildIcon("AppleCheckCircle", pathFillType = PathFillType.EvenOdd) {
            moveTo(12f, 2f)
            curveTo(6.5f, 2f, 2f, 6.5f, 2f, 12f)
            curveTo(2f, 17.5f, 6.5f, 22f, 12f, 22f)
            curveTo(17.5f, 22f, 22f, 17.5f, 22f, 12f)
            curveTo(22f, 6.5f, 17.5f, 2f, 12f, 2f)
            close()

            // Inset Checkmark
            moveTo(10.2f, 16.2f)
            lineTo(6.8f, 12.8f)
            lineTo(8.2f, 11.4f)
            lineTo(10.2f, 13.4f)
            lineTo(15.8f, 7.8f)
            lineTo(17.2f, 9.2f)
            close()
        }
    }

    /** SF Symbol: arrow.up.doc.fill (Apple upload document) */
    val Upload: ImageVector by lazy {
        buildIcon("AppleUpload") {
            moveTo(6f, 2.5f)
            lineTo(14f, 2.5f)
            lineTo(20f, 8.5f)
            lineTo(20f, 19.5f)
            curveTo(20f, 21f, 18.8f, 22f, 17.5f, 22f)
            lineTo(6f, 22f)
            curveTo(4.5f, 22f, 3.5f, 21f, 3.5f, 19.5f)
            lineTo(3.5f, 5f)
            curveTo(3.5f, 3.5f, 4.5f, 2.5f, 6f, 2.5f)
            close()

            // Inset upward arrow
            moveTo(12f, 11f)
            lineTo(15.5f, 14.5f)
            lineTo(14.1f, 15.9f)
            lineTo(13f, 14.8f)
            lineTo(13f, 19f)
            lineTo(11f, 19f)
            lineTo(11f, 14.8f)
            lineTo(9.9f, 15.9f)
            lineTo(8.5f, 14.5f)
            close()
        }
    }

    /** SF Symbol: arrow.down.doc.fill (Apple download document) */
    val Download: ImageVector by lazy {
        buildIcon("AppleDownload") {
            moveTo(6f, 2.5f)
            lineTo(14f, 2.5f)
            lineTo(20f, 8.5f)
            lineTo(20f, 19.5f)
            curveTo(20f, 21f, 18.8f, 22f, 17.5f, 22f)
            lineTo(6f, 22f)
            curveTo(4.5f, 22f, 3.5f, 21f, 3.5f, 19.5f)
            lineTo(3.5f, 5f)
            curveTo(3.5f, 3.5f, 4.5f, 2.5f, 6f, 2.5f)
            close()

            // Inset downward arrow
            moveTo(12f, 19f)
            lineTo(8.5f, 15.5f)
            lineTo(9.9f, 14.1f)
            lineTo(11f, 15.2f)
            lineTo(11f, 11f)
            lineTo(13f, 11f)
            lineTo(13f, 15.2f)
            lineTo(14.1f, 14.1f)
            lineTo(15.5f, 15.5f)
            close()
        }
    }

    /** SF Symbol: lock.fill (Apple iOS security padlock) */
    val Lock: ImageVector by lazy {
        buildIcon("AppleLock", pathFillType = PathFillType.EvenOdd) {
            // Shackle (arch)
            moveTo(12f, 2f)
            curveTo(9.2f, 2f, 7f, 4.2f, 7f, 7f)
            lineTo(7f, 10f)
            lineTo(17f, 10f)
            lineTo(17f, 7f)
            curveTo(17f, 4.2f, 14.8f, 2f, 12f, 2f)
            close()

            // Shackle inner cut
            moveTo(12f, 4.5f)
            curveTo(13.4f, 4.5f, 14.5f, 5.6f, 14.5f, 7f)
            lineTo(14.5f, 10f)
            lineTo(9.5f, 10f)
            lineTo(9.5f, 7f)
            curveTo(9.5f, 5.6f, 10.6f, 4.5f, 12f, 4.5f)
            close()

            // Padlock body
            moveTo(5.5f, 10f)
            curveTo(4.4f, 10f, 3.5f, 10.9f, 3.5f, 12f)
            lineTo(3.5f, 20f)
            curveTo(3.5f, 21.1f, 4.4f, 22f, 5.5f, 22f)
            lineTo(18.5f, 22f)
            curveTo(19.6f, 22f, 20.5f, 21.1f, 20.5f, 20f)
            lineTo(20.5f, 12f)
            curveTo(20.5f, 10.9f, 19.6f, 10f, 18.5f, 10f)
            close()

            // Keyhole
            moveTo(12f, 14f)
            curveTo(11.2f, 14f, 10.5f, 14.7f, 10.5f, 15.5f)
            curveTo(10.5f, 16.1f, 10.9f, 16.6f, 11.4f, 16.9f)
            lineTo(11f, 19f)
            lineTo(13f, 19f)
            lineTo(12.6f, 16.9f)
            curveTo(13.1f, 16.6f, 13.5f, 16.1f, 13.5f, 15.5f)
            curveTo(13.5f, 14.7f, 12.8f, 14f, 12f, 14f)
            close()
        }
    }

    /** SF Symbol: trash.fill (Apple iOS Cupertino delete/trash) */
    val Trash: ImageVector by lazy {
        buildIcon("AppleTrash") {
            moveTo(6f, 19f)
            curveTo(6f, 20.1f, 6.9f, 21f, 8f, 21f)
            lineTo(16f, 21f)
            curveTo(17.1f, 21f, 18f, 20.1f, 18f, 19f)
            lineTo(19f, 7f)
            lineTo(5f, 7f)
            lineTo(6f, 19f)
            close()

            moveTo(15.5f, 4f)
            lineTo(14.5f, 3f)
            lineTo(9.5f, 3f)
            lineTo(8.5f, 4f)
            lineTo(5f, 4f)
            curveTo(4.4f, 4f, 4f, 4.4f, 4f, 5f)
            curveTo(4f, 5.6f, 4.4f, 6f, 5f, 6f)
            lineTo(19f, 6f)
            curveTo(19.6f, 6f, 20f, 5.6f, 20f, 5f)
            curveTo(20f, 4.4f, 19.6f, 4f, 19f, 4f)
            lineTo(15.5f, 4f)
            close()
        }
    }

    /** SF Symbol: square.and.arrow.up (Apple iOS Cupertino share/export) */
    val Share: ImageVector by lazy {
        buildIcon("AppleShare") {
            moveTo(19f, 11f)
            lineTo(19f, 19f)
            curveTo(19f, 20.1f, 18.1f, 21f, 17f, 21f)
            lineTo(7f, 21f)
            curveTo(5.9f, 21f, 5f, 20.1f, 5f, 19f)
            lineTo(5f, 11f)
            curveTo(5f, 10.4f, 5.4f, 10f, 6f, 10f)
            curveTo(6.6f, 10f, 7f, 10.4f, 7f, 11f)
            lineTo(7f, 19f)
            lineTo(17f, 19f)
            lineTo(17f, 11f)
            curveTo(17f, 10.4f, 17.4f, 10f, 18f, 10f)
            curveTo(18.6f, 10f, 19f, 10.4f, 19f, 11f)
            close()

            moveTo(12f, 3f)
            curveTo(12.3f, 3f, 12.5f, 3.1f, 12.7f, 3.3f)
            lineTo(16.7f, 7.3f)
            curveTo(17.1f, 7.7f, 17.1f, 8.3f, 16.7f, 8.7f)
            curveTo(16.3f, 9.1f, 15.7f, 9.1f, 15.3f, 8.7f)
            lineTo(13f, 6.4f)
            lineTo(13f, 14f)
            curveTo(13f, 14.6f, 12.6f, 15f, 12f, 15f)
            curveTo(11.4f, 15f, 11f, 14.6f, 11f, 14f)
            lineTo(11f, 6.4f)
            lineTo(8.7f, 8.7f)
            curveTo(8.3f, 9.1f, 7.7f, 9.1f, 7.3f, 8.7f)
            curveTo(6.9f, 8.3f, 6.9f, 7.7f, 7.3f, 7.3f)
            lineTo(11.3f, 3.3f)
            curveTo(11.5f, 3.1f, 11.7f, 3f, 12f, 3f)
            close()
        }
    }
}

