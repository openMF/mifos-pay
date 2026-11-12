/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.ui.util

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.runtime.Composable

/** `TrackScrollJank` on Non-Android targets. */
@Composable
actual fun TrackScrollJank(
    scrollableState: ScrollableState,
    stateName: String,
) {
}
