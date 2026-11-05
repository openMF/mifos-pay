/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Modifier that detects a multi-tap gesture within a configurable timeout window.
 *
 * This is useful for revealing hidden developer/debug features, such as:
 * - Server instance selector
 * - Debug menu
 * - Developer options
 *
 * ## Usage
 *
 * ```kotlin
 * Text(
 *     text = "Tap me 5 times!",
 *     modifier = Modifier
 *         .detectMultiTapGesture {
 *             // Show hidden feature
 *             showDebugMenu = true
 *         }
 * )
 * ```
 *
 * ## Custom Configuration
 *
 * ```kotlin
 * Text(
 *     text = "Tap me 3 times quickly!",
 *     modifier = Modifier
 *         .detectMultiTapGesture(
 *             tapCount = 3,
 *             tapTimeoutMs = 500L,
 *         ) {
 *             // Show hidden feature
 *         }
 * )
 * ```
 *
 * @param tapCount Number of taps required to trigger the gesture. Defaults to 5.
 * @param tapTimeoutMs Time window in milliseconds within which taps must occur. Defaults to 1000ms.
 * @param onGestureDetected Callback invoked when the required number of taps is detected within the timeout.
 */
@OptIn(ExperimentalTime::class)
fun Modifier.detectMultiTapGesture(
    tapCount: Int = 5,
    tapTimeoutMs: Long = 1000L,
    onGestureDetected: () -> Unit,
): Modifier = composed {
    var currentTapCount by remember { mutableIntStateOf(0) }
    var lastTapTime by remember { mutableLongStateOf(0L) }

    this.pointerInput(Unit) {
        detectTapGestures(
            onTap = {
                val currentTime = Clock.System.now().toEpochMilliseconds()
                if (currentTime - lastTapTime > tapTimeoutMs) {
                    currentTapCount = 0
                }
                currentTapCount++
                lastTapTime = currentTime

                if (currentTapCount >= tapCount) {
                    onGestureDetected()
                    currentTapCount = 0
                }
            },
        )
    }
}

/**
 * Modifier that detects a long press gesture.
 *
 * ## Usage
 *
 * ```kotlin
 * Box(
 *     modifier = Modifier
 *         .detectLongPressGesture {
 *             // Handle long press
 *         }
 * )
 * ```
 *
 * @param onLongPress Callback invoked when a long press is detected.
 */
fun Modifier.detectLongPressGesture(
    onLongPress: () -> Unit,
): Modifier = composed {
    this.pointerInput(Unit) {
        detectTapGestures(
            onLongPress = { onLongPress() },
        )
    }
}

/**
 * Modifier that detects a double tap gesture.
 *
 * ## Usage
 *
 * ```kotlin
 * Image(
 *     modifier = Modifier
 *         .detectDoubleTapGesture {
 *             // Handle double tap (e.g., like action)
 *         }
 * )
 * ```
 *
 * @param onDoubleTap Callback invoked when a double tap is detected.
 */
fun Modifier.detectDoubleTapGesture(
    onDoubleTap: () -> Unit,
): Modifier = composed {
    this.pointerInput(Unit) {
        detectTapGestures(
            onDoubleTap = { onDoubleTap() },
        )
    }
}
