/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.data.infra

import kotlinx.coroutines.flow.Flow

/**
 * Observer surface for the in-flight sync state. NiA-port.
 *
 * Single binary signal — `isSyncing` — collected by composables that want to
 * surface "Refreshing…" indicators globally (typically the top app bar, the
 * home dashboard hero, etc.). `requestSync()` lets any caller kick off a
 * one-shot data sync without depending on `WorkScheduler` directly.
 *
 * Per-platform implementations live in the `sync/` module:
 *  - Android: WorkManager.getWorkInfosForUniqueWorkFlow(DATA_SYNC_WORK_NAME)
 *    mapped to Boolean
 *  - iOS/Desktop/Web: MutableStateFlow-backed stubs (v1 — real cross-platform
 *    scheduling is a follow-up).
 */
interface SyncManager {
    /** Whether a sync is in flight — drives the refreshing affordance without each screen tracking it. */
    val isSyncing: Flow<Boolean>

    /** Requests a sync. Advisory: the manager may coalesce it with one already running. */
    fun requestSync()
}
