/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/** Non-Android actual: returns the default Material 3 light or dark color scheme (no dynamic color). */
@Composable
actual fun platformColorScheme(useDarkTheme: Boolean, dynamicColor: Boolean): ColorScheme {
    return when (useDarkTheme) {
        true -> darkColorScheme()
        false -> lightColorScheme()
    }
}
