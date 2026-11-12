/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.sync.di

import io.github.mobilebytelabs.worker.WorkManager
import io.github.mobilebytelabs.worker.sync.di.SyncObserverKoinModule
import kpt.sync.WorkScheduler
import kpt.sync.infra.DefaultWorkScheduler
import org.koin.dsl.module

/**
 * Aggregator Koin module for the `sync/` module.
 *
 * Worker-kmp single-API migration (v4.0.0):
 *  - REMOVED: the hand-rolled `single<SyncManager> { provideSyncManager(get<WorkManager>()) }`
 *    binding + its 4 per-platform `expect/actual provideSyncManager(...)` files. Replaced by
 *    `cmp-worker-sync` library's [SyncObserverKoinModule] which binds `UniqueWorkObserver`
 *    via the same KMP-single-API path (commonMain-only, no per-platform actuals).
 *  - KEPT: the consumer-owned `single<WorkScheduler> { DefaultWorkScheduler(...) }` binding
 *    (consumer-domain — choice of how to expose scheduling API to feature modules).
 *
 * Requires the host app to have already wired `single<WorkManager> { ... }` via
 * `WorkerKmpAuto.install()` (codegen-emitted by `cmp-worker-app-plugin` from the
 * `@WorkerKmpWorkers` annotation in `cmp-shared`).
 */
public val SyncModule = module {
    includes(SyncObserverKoinModule)
    single<WorkScheduler> { DefaultWorkScheduler(workManager = get<WorkManager>()) }
}
