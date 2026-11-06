/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.common.utils

/**
 * Formats a list of strings into a bullet-pointed multiline string.
 *
 * Each non-blank line is trimmed and prefixed with a Unicode bullet (•) followed by a space.
 * Empty or whitespace-only strings are ignored.
 *
 * @param lines A list of strings to be formatted.
 * @return A single string where each line is prefixed with a bullet point and separated by a newline.
 *
 * ### Example:
 * ```
 * val feedback = listOf(
 *     "The password must be at least 12 characters long",
 *     "Include at least one number"
 * )
 *
 * val result = formatAsBulletPoints(feedback)
 * println(result)
 * ```
 *
 * ### Output:
 * ```
 * • The password must be at least 12 characters long
 * • Include at least one number
 * ```
 */
fun formatAsBulletPoints(lines: List<String>): String {
    return lines
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .joinToString(separator = "\n") { "• $it" }
}
