/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(io.github.mobilebytelabs.worker.ExperimentalWorkerApi::class)

package kpt.sync.infra

import io.github.mobilebytelabs.worker.CoroutineWorker
import io.github.mobilebytelabs.worker.WorkResult
import io.github.mobilebytelabs.worker.WorkerContext
import kpt.core.base.data.infra.Synchronizer
import kpt.core.base.datastore.infra.ChangeListVersions
import kpt.core.base.datastore.infra.SyncStatePersister

/**
 * Single data-sync worker. Implements [Synchronizer] so its two [Syncable]
 * collaborators ([CurrencyRepository], [MacroIndicatorsRepository]) can read +
 * write [ChangeListVersions] through `this` without an extra abstraction.
 *
 * **No `getAll<Syncable>()`** — the two repos are constructor-injected as named interfaces. Adding a third
 * Syncable (e.g. FRED interest rates) requires editing this class signature +
 * the [SyncModule] binding — not a runtime discovery.
 *
 * The `awaitAll` shape means partial failure is full failure — if one repo
 * throws, the entire sync is `Result.retry()`. This matches NiA's surface
 * (single `Flow<Boolean>` to observers) and is what the template's home
 * dashboard expects.
 */
public class DataSyncWorker(
    context: WorkerContext,
    private val persister: SyncStatePersister,
) : CoroutineWorker(context), Synchronizer {

    // Synchronizer's in-memory state for THIS worker invocation. Read at start
    // from the persister; written back at end.
    private var workingVersions: ChangeListVersions = ChangeListVersions()

    override suspend fun getChangeListVersions(): ChangeListVersions = workingVersions

    override suspend fun updateChangeListVersions(update: ChangeListVersions.() -> ChangeListVersions) {
        workingVersions = workingVersions.update()
    }

    override suspend fun doWork(): WorkResult {
        workingVersions = persister.read()
        persister.write(workingVersions)
        return WorkResult.success()
    }
}
