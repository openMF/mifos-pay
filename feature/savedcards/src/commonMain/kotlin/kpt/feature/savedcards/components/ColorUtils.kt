/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.savedcards.components

import androidx.compose.ui.graphics.Color

/**
 * Converts this color to HSL, returned as a float array of hue, saturation and lightness.
 */
fun Color.toHsl(): FloatArray {
    val redComponent = red
    val greenComponent = green
    val blueComponent = blue

    val maxComponent = maxOf(redComponent, greenComponent, blueComponent)
    val minComponent = minOf(redComponent, greenComponent, blueComponent)
    val delta = maxComponent - minComponent
    val lightness = (maxComponent + minComponent) / 2

    val hue: Float
    val saturation: Float

    if (maxComponent == minComponent) {
        // Grayscale color, no saturation and hue is undefined
        hue = 0f
        saturation = 0f
    } else {
        // Calculating saturation
        saturation = if (lightness > 0.5) delta / (2 - maxComponent - minComponent) else delta / (maxComponent + minComponent)
        // Calculating hue
        hue = when (maxComponent) {
            redComponent -> 60 * ((greenComponent - blueComponent) / delta % 6)
            greenComponent -> 60 * ((blueComponent - redComponent) / delta + 2)
            else -> 60 * ((redComponent - greenComponent) / delta + 4)
        }
    }

    // Returning HSL values, ensuring hue is within 0-360 range
    return floatArrayOf(hue.coerceIn(0f, 360f), saturation, lightness)
}

/**
 * Builds a [Color] from [hue] (degrees), [saturation] and [lightness] (0..1).
 */
fun hslToColor(hue: Float, saturation: Float, lightness: Float): Color {
    val chroma = (1 - kotlin.math.abs(2 * lightness - 1)) * saturation
    val secondaryColorComponent = chroma * (1 - kotlin.math.abs((hue / 60) % 2 - 1))
    val matchValue = lightness - chroma / 2

    var red = matchValue
    var green = matchValue
    var blue = matchValue

    when ((hue.toInt() / 60) % 6) {
        0 -> {
            red += chroma
            green += secondaryColorComponent
        }
        1 -> {
            red += secondaryColorComponent
            green += chroma
        }
        2 -> {
            green += chroma
            blue += secondaryColorComponent
        }
        3 -> {
            green += secondaryColorComponent
            blue += chroma
        }
        4 -> {
            red += secondaryColorComponent
            blue += chroma
        }
        5 -> {
            red += chroma
            blue += secondaryColorComponent
        }
    }

    // Creating a color from RGB components
    return Color(red = red, green = green, blue = blue)
}

/**
 * Returns this color with its saturation replaced by [newSaturation], keeping hue and lightness.
 */
fun Color.setSaturation(newSaturation: Float): Color {
    val hslValues = this.toHsl()
    // Adjusting the saturation while keeping hue and lightness the same
    return hslToColor(hslValues[0], newSaturation.coerceIn(0f, 1f), hslValues[2])
}
