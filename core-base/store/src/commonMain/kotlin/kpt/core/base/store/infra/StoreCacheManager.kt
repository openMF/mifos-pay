/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.store.infra

/**
 * Manages Store cache lifecycle.
 *
 * - Call [clearAll] on logout to prevent stale data leaking across user sessions.
 * - Call [pruneExpiredDrafts] on app start to clean up old SUBMITTED/FAILED draft rows.
 */
interface StoreCacheManager {
    /** Clears all store caches (in-memory + database). Call on logout. */
    suspend fun clearAll()

    /**
     * Removes SUBMITTED and FAILED draft rows older than [maxAgeMs] milliseconds.
     * PENDING drafts are never removed — users can still resume them offline.
     *
     * Default retention is 30 days. Call once on app start.
     */
    suspend fun pruneExpiredDrafts(maxAgeMs: Long = DEFAULT_DRAFT_TTL_MS)

    /** Manager entry points. */
    companion object {
        /** 30 days in milliseconds. */
        const val DEFAULT_DRAFT_TTL_MS: Long = 30L * 24 * 60 * 60 * 1000
    }
}
