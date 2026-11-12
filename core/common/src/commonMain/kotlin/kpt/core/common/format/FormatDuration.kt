/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.common.format

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Returns a human-readable "X ago" label for a past [instant], e.g. "just now", "5m ago",
 * "2h ago", "3d ago". Returns `null` when [instant] is null.
 */
@OptIn(ExperimentalTime::class)
fun formatTimeAgo(instant: Instant?): String? {
    instant ?: return null
    val seconds = (Clock.System.now() - instant).inWholeSeconds
    return when {
        seconds < 60 -> "just now"
        seconds < 3600 -> "${seconds / 60}m ago"
        seconds < 86400 -> "${seconds / 3600}h ago"
        else -> "${seconds / 86400}d ago"
    }
}
