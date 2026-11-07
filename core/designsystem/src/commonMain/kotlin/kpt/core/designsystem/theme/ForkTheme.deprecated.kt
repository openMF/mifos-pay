/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:Suppress("unused")

package kpt.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

/**
 * Back-compat shim — forwards to [KptTheme]. Composables cannot be `typealias`'d,
 * so this file holds a thin `@Composable fun` wrapper that re-exports the old
 * `ForkTheme` name for one release while consumers migrate.
 *
 * @see KptTheme
 */
@Deprecated(
    "Renamed to KptTheme (Kpt* is the template-neutral prefix).",
    ReplaceWith("KptTheme(darkTheme, androidTheme, useDynamicColor, content)"),
    level = DeprecationLevel.WARNING,
)
@Composable
fun ForkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    androidTheme: Boolean = false,
    useDynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) = KptTheme(
    darkTheme = darkTheme,
    androidTheme = androidTheme,
    useDynamicColor = useDynamicColor,
    content = content,
)
