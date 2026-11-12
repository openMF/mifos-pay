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

/**
 * Reports dropped frames during scrolling of [scrollableState], tagged [stateName] so one screen's
 * jank is attributable in a trace.
 *
 * Android-only in practice; a no-op `actual` elsewhere, which lets a shared screen call it
 * unconditionally rather than guarding every use site.
 */
@Composable
expect fun TrackScrollJank(scrollableState: ScrollableState, stateName: String)
