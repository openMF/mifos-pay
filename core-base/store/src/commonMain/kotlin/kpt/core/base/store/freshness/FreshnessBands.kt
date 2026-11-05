/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.store.freshness

import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Pure computation of [FreshnessBand] from time + last-error inputs only.
 *
 * Decision table (first match wins):
 *  1. `lastSyncedAt == null && lastError == null` → [FreshnessBand.Initial]
 *  2. `lastError != null`                         → [FreshnessBand.VeryStale]
 *  3. `age <= ttl`                                → [FreshnessBand.Fresh]
 *  4. `age <= ttl * 3`                            → [FreshnessBand.Stale]
 *  5. else                                        → [FreshnessBand.VeryStale]
 *
 * **No NetworkMonitor input.** Network state is rendered separately by
 * `ConnectivityBanner`; freshness is purely "how old is the data".
 */
@OptIn(ExperimentalTime::class)
object FreshnessBands {
    /**
     * Derives the freshness band from a last-synced instant and a TTL. Pure, so the banding rule is unit-testable
     * without standing up a Store.
     */
    fun bandFor(
        now: Instant,
        lastSyncedAt: Instant?,
        ttl: Duration,
        lastError: Throwable?,
    ): FreshnessBand = when {
        lastError != null -> FreshnessBand.VeryStale
        lastSyncedAt == null -> FreshnessBand.Initial
        else -> {
            val age = now - lastSyncedAt
            when {
                age <= ttl -> FreshnessBand.Fresh
                age <= ttl * 3 -> FreshnessBand.Stale
                else -> FreshnessBand.VeryStale
            }
        }
    }
}
