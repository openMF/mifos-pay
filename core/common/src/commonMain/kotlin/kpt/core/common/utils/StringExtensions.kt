/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.common.utils

/**
 * Whether or not string is a valid email address.
 *
 * This just checks if the string contains the "@" symbol.
 */
fun String.isValidEmail(): Boolean = contains("@")

/** Masks all but the last three characters of [input] with [maskChar]; short inputs are returned as is. */
fun maskString(input: String, maskChar: Char = '*'): String {
    if (input.length <= 3) return input

    val visibleCount = 3
    val maskLength = input.length - visibleCount

    return buildString {
        append(maskChar.toString().repeat(maskLength))
        append(input.takeLast(visibleCount))
    }
}

/** Upper-cases the first letter of each space-separated word. */
fun String.capitalizeWords(): String = split(" ").joinToString(" ") { it ->
    it.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

/** True if this string contains a space. */
fun String.hasSpaces(): Boolean {
    return this.contains(" ")
}

/** True if this string contains two identical characters in a row. */
fun String.hasConsecutiveRepetitions(): Boolean {
    return Regex("(.)\\1").containsMatchIn(this)
}
