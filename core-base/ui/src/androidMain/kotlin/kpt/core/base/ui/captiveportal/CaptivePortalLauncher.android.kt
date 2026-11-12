/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.ui.captiveportal

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/** `rememberOpenCaptivePortalSignIn` on Android. */
@Composable
actual fun rememberOpenCaptivePortalSignIn(): () -> Unit {
    val context = LocalContext.current
    return remember(context) {
        {
            try {
                val intent = Intent("android.settings.WIFI_SETTINGS").also {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (_: Exception) {
                // silently ignore if the intent cannot be resolved
            }
        }
    }
}
