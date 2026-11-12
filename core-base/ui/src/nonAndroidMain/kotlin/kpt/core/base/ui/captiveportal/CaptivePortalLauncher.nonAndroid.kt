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

import androidx.compose.runtime.Composable

/** `rememberOpenCaptivePortalSignIn` on Non-Android targets. */
@Composable
actual fun rememberOpenCaptivePortalSignIn(): () -> Unit = { }
