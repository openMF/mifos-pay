/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.util

import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Creates a [MutableSharedFlow] with a buffer of [Int.MAX_VALUE] and the given [replay] count.
 */
fun <T> bufferedMutableSharedFlow(replay: Int = 0): MutableSharedFlow<T> = MutableSharedFlow(
    replay = replay,
    extraBufferCapacity = Int.MAX_VALUE,
)
