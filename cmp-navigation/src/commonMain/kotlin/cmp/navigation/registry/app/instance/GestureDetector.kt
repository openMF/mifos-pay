/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.registry.app.instance

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kpt.core.base.ui.detectMultiTapGesture

/**
 * Convenience wrapper for instance selector gesture detection.
 * Uses the generic [detectMultiTapGesture] from core-base/ui.
 */
@Composable
fun Modifier.detectInstanceSelectorGesture(
    onGestureDetected: () -> Unit,
): Modifier = detectMultiTapGesture(
    tapCount = 5,
    tapTimeoutMs = 1000L,
    onGestureDetected = onGestureDetected,
)
