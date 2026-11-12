/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.ui

import androidx.compose.runtime.compositionLocalOf
import kotlinx.datetime.TimeZone

/**
 * TimeZone that can be provided with the TimeZoneMonitor.
 * This way, it's not needed to pass every single composable the time zone to show in UI.
 */
val LocalTimeZone = compositionLocalOf { TimeZone.currentSystemDefault() }
