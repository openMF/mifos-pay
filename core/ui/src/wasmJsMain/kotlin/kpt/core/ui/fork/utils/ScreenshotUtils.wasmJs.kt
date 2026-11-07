/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.ui.fork.utils

import androidx.compose.ui.Modifier
import co.touchlab.kermit.Logger

/**
 * WASM/JS stub implementation of ScreenshotUtils.
 *
 * Screenshot functionality is not available in WASM/JS environments.
 */
actual object ScreenshotUtils {

    /** Not supported on this platform; reports failure through [onError]. */
    actual suspend fun takeScreenshotAndShare(
        excludeModifiers: List<Modifier>,
        contextDetails: ScreenshotContextDetails,
        onError: (String) -> Unit,
    ) {
        Logger.w("Screenshot capture not supported in WASM/JS environment")
        onError("Screenshot capture not supported in WASM/JS environment")
    }
}
