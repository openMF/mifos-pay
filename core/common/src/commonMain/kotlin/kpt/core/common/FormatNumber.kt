/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.common

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToLong

// KMP-safe number formatting. String.format(...) is JVM-only and fails on
// Kotlin/JS and Kotlin/Native; these helpers cover the use cases we hit
// in the UI: fixed decimal places + thousand separators.

/** Formats this value rounded to [places] decimal places without grouping (KMP-safe replacement for String.format). */
fun Double.formatDecimal(places: Int): String {
    val factor = 10.0.pow(places)
    val rounded = (this * factor).roundToLong() / factor
    val negative = rounded < 0
    val absStr = abs(rounded).toString()
    val dot = absStr.indexOf('.')
    val intPart = if (dot >= 0) absStr.substring(0, dot) else absStr
    val fracPart = if (dot >= 0) absStr.substring(dot + 1) else ""
    val padded = fracPart.padEnd(places, '0').take(places)
    val sign = if (negative) "-" else ""
    return if (places == 0) "$sign$intPart" else "$sign$intPart.$padded"
}

/** Formats this value with [places] decimal places and comma thousands separators. */
fun Double.formatGrouped(places: Int): String {
    val raw = formatDecimal(places)
    val negative = raw.startsWith('-')
    val body = if (negative) raw.drop(1) else raw
    val dot = body.indexOf('.')
    val intPart = if (dot >= 0) body.substring(0, dot) else body
    val fracPart = if (dot >= 0) body.substring(dot) else ""
    val grouped = intPart.reversed().chunked(3).joinToString(",").reversed()
    return (if (negative) "-" else "") + grouped + fracPart
}

/** Formats this value with comma thousands separators. */
fun Long.formatGrouped(): String {
    val negative = this < 0
    val s = abs(this).toString()
    val grouped = s.reversed().chunked(3).joinToString(",").reversed()
    return if (negative) "-$grouped" else grouped
}
