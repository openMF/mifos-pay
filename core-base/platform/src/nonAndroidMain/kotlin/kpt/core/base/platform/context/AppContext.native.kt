/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.platform.context

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Apple/native `AppContext`. No platform context object exists on these targets, so this carries no state — it exists
 * only to satisfy the `expect` so commonMain can name one type.
 */
actual abstract class AppContext private constructor() {
    /** Accessors. */
    companion object {
        /** `INSTANCE` on Non-Android targets. */
        val INSTANCE = object : AppContext() {}
    }
}

/** `LocalContext` on Non-Android targets. */
actual val LocalContext: ProvidableCompositionLocal<AppContext>
    get() = staticCompositionLocalOf { AppContext.INSTANCE }

/** `AppContext` on Non-Android targets. */
actual val AppContext.activity: Any
    @Composable
    get() = AppContext.INSTANCE
