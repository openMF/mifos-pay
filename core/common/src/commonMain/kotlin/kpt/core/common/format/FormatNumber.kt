/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.common.format

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToLong

// KMP-safe number formatting. String.format(...) is JVM-only and fails on
// Kotlin/JS and Kotlin/Native; these helpers cover the use cases we hit
// in the UI: fixed decimal places + thousand separators.

/**
 * Formats to exactly [places] decimals, zero-padded so a column of figures stays aligned.
 *
 * Hand-rolled because `String.format` is JVM-only — it fails to link on Kotlin/JS and Native, so a
 * shared UI cannot use it. Rounds half-up via [roundToLong] rather than truncating.
 */
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

/**
 * Formats with `,` thousands separators and exactly [places] decimals — the money/quantity form.
 *
 * The separator is fixed, not locale-derived: KMP has no common locale-aware number formatter, and a
 * per-platform one would render the same figure differently on Android and web.
 */
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

/**
 * Formats a whole number with `,` thousands separators — counts and sizes, no fractional part.
 */
fun Long.formatGrouped(): String {
    val negative = this < 0
    val s = abs(this).toString()
    val grouped = s.reversed().chunked(3).joinToString(",").reversed()
    return if (negative) "-$grouped" else grouped
}
