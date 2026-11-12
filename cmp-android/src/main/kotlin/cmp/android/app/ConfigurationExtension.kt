/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.android.app

import android.content.res.Configuration

/**
 * Whether this configuration is in night mode, read from `uiMode`'s night bits.
 *
 * Android-only: it answers what the SYSTEM is doing, which is only half the decision — `DarkThemeConfig.isDarkMode`
 * combines it with the user's own preference. Reading this directly from a screen would ignore a user who forced
 * light.
 */
val Configuration.isSystemInDarkMode
    get() = (uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
