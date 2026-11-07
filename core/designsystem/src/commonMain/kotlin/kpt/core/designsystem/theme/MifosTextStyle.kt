/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

/** Medium-weight 16sp sans-serif text style. */
val styleMedium16sp =
    TextStyle(
        fontSize = 16.sp,
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
    )

/** Normal-weight 18sp sans-serif text style. */
val styleNormal18sp =
    TextStyle(
        fontSize = 18.sp,
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
    )

/** Medium-weight 30sp sans-serif text style. */
val styleMedium30sp =
    TextStyle(
        fontSize = 30.sp,
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
    )

/** Semi-bold dark text style used for top bar titles. */
val styleMifosTopBar =
    TextStyle(
        color = Color(0xFF212121),
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
    )

/** Center-aligned text style for settings buttons. */
val styleSettingsButton = TextStyle(textAlign = TextAlign.Center)

/** 16sp text style for transaction history items. */
val historyItemTextStyle = TextStyle(fontSize = 16.sp)
