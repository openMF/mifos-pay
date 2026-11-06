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

val styleMedium16sp =
    TextStyle(
        fontSize = 16.sp,
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
    )

val styleNormal18sp =
    TextStyle(
        fontSize = 18.sp,
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
    )

val styleMedium30sp =
    TextStyle(
        fontSize = 30.sp,
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
    )

val styleMifosTopBar =
    TextStyle(
        color = Color(0xFF212121),
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
    )

val styleSettingsButton = TextStyle(textAlign = TextAlign.Center)
val historyItemTextStyle = TextStyle(fontSize = 16.sp)
