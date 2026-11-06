/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.ui.utils

import androidx.compose.ui.Modifier
import co.touchlab.kermit.Logger

/**
 * Desktop stub implementation of ScreenshotUtils.
 *
 * Screenshot functionality is not implemented for Desktop yet.
 */
actual object ScreenshotUtils {

    actual suspend fun takeScreenshotAndShare(
        excludeModifiers: List<Modifier>,
        contextDetails: ScreenshotContextDetails,
        onError: (String) -> Unit,
    ) {
        Logger.w("Screenshot capture not implemented for Desktop yet")
        onError("Screenshot capture not implemented for Desktop yet")
    }
}
