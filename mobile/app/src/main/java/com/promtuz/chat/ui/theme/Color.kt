package com.promtuz.chat.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.tooling.preview.*
import androidx.compose.ui.unit.*
import androidx.core.graphics.ColorUtils


/**
 *
 * 1f changeInLight is 100%
 *
 * Example:
 * 1. `+0.3f` will increase light by `30%` regardless of base value, but will max out at 100% (pure white)
 * 2. `-0.3f` will decrease light by `30%` regardless of base value, but will max out at 0% (pitch black)
 */
fun adjustLight(col: Color, changeInLight: Float): Color {
    val hsl = floatArrayOf(0f, 0f, 0f)
    ColorUtils.RGBToHSL(
        (col.red * 255f).toInt(),
        (col.green * 255f).toInt(),
        (col.blue * 255f).toInt(),
        hsl
    )
    hsl[2] += changeInLight
    return Color(ColorUtils.HSLToColor(hsl))
}


data class OutlinedFormElementColorGroup(
    val border: Color,
    val label: Color,
    val placeholder: Color
)

data class OutlinedFormElementColors(
    val unfocused: OutlinedFormElementColorGroup,
    val focused: OutlinedFormElementColorGroup,
    val error: OutlinedFormElementColorGroup,
    val disabled: OutlinedFormElementColorGroup,
)


@Composable
fun outlinedFormElementsColors(): OutlinedFormElementColors {
    return if (isSystemInDarkTheme()) {
        val baseColor = MaterialTheme.colorScheme.background
        val errorColor = MaterialTheme.colorScheme.errorContainer
        OutlinedFormElementColors(
            OutlinedFormElementColorGroup(
                adjustLight(baseColor, 0.3f),
                adjustLight(baseColor, 0.5f),
                adjustLight(baseColor, 0.4f),
            ),
            OutlinedFormElementColorGroup(
                adjustLight(baseColor, 0.5f),
                adjustLight(baseColor, 0.6f),
                adjustLight(baseColor, 0.5f),
            ),
            OutlinedFormElementColorGroup(
                adjustLight(errorColor, 0.3f),
                adjustLight(errorColor, 0.4f),
                adjustLight(errorColor, 0.35f),
            ),
            OutlinedFormElementColorGroup(
                adjustLight(baseColor, 0.3f).copy(0.75f),
                adjustLight(baseColor, 0.4f).copy(0.75f),
                adjustLight(baseColor, 0.35f).copy(0.75f)
            )
        )
    } else {
        val baseColor = MaterialTheme.colorScheme.primary
        val errorColor = MaterialTheme.colorScheme.errorContainer
        OutlinedFormElementColors(
            OutlinedFormElementColorGroup(
                adjustLight(baseColor, 0.1f),
                adjustLight(baseColor, 0.2f),
                adjustLight(baseColor, 0.15f)
            ),
            OutlinedFormElementColorGroup(
                adjustLight(baseColor, 0f),
                adjustLight(baseColor, 0f),
                adjustLight(baseColor, 0f)
            ),
            OutlinedFormElementColorGroup(
                adjustLight(errorColor, 0.1f),
                adjustLight(errorColor, 0.2f),
                adjustLight(errorColor, 0.1f)
            ),
            OutlinedFormElementColorGroup(
                adjustLight(baseColor, 0.3f).copy(0.75f),
                adjustLight(baseColor, 0.4f).copy(0.75f),
                adjustLight(baseColor, 0.3f).copy(0.75f),
            )
        )
    }
}


@Preview(wallpaper = Wallpapers.BLUE_DOMINATED_EXAMPLE)
@Composable
private fun SurfaceColorsPreview(modifier: Modifier = Modifier) {
    PromtuzTheme(true) {
        val colors = MaterialTheme.colorScheme
//        Box(
//            modifier
//                .fillMaxSize()
//                .background(colors.secondary)
//        ) {
//
//        }
        Row(Modifier.fillMaxWidth()) {
            ColoredBox(colors.primary, "PRIMARY")
            ColoredBox(colors.secondary, "SECONDARY")
            ColoredBox(colors.tertiary, "TERTIARY")
        }
    }
}


@Composable
private fun RowScope.ColoredBox(col: Color, label: String) {
    Box(
        Modifier
            .weight(1f)
            .background(col)
            .padding(vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, style = MaterialTheme.typography.labelMediumEmphasized)
    }
}