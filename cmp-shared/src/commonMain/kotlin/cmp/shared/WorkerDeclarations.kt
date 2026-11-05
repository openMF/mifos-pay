/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.shared

import io.github.mobilebytelabs.worker.app.WorkerKmpWorkers
import kpt.sync.infra.DataSyncWorker
import kpt.sync.infra.NotificationWorker

/**
 * Worker registry declaration site for the template's BASE workers (owner: template — do not fork-edit).
 * A fork declares ITS OWN workers in ForkWorkerDeclarations.kt (owner: fork); the KSP processor scans
 * cmp-shared/commonMain for every @WorkerKmpWorkers site and aggregates both. This keeps the module
 * white-label: a template sync full-copies this file while the fork's workers survive in the seam file.
 *
 * KSP processor (`cmp-worker-app-ksp`) reads this annotation and generates per-platform
 * `installWorkerKmp{Platform}()` files under `cmp-shared/build/generated/worker-kmp-app/...`.
 * The commonMain `initKoin()` (cmp-shared/utils/KoinExt.kt) calls `WorkerKmpAuto.install()`
 * (the codegen-emitted commonMain shim) ONCE — every platform (Android / iOS / Desktop / Web)
 * funnels through `initKoin()`, so one call wires them all. It is NOT called from any single
 * platform's app class — see docs/guides/framework/KMP_MULTIPLATFORM_ARCHITECTURE.md.
 *
 * To add a new worker:
 *   1. Add the class to the `workers = [...]` array below.
 *   2. The codegen scans its primary constructor — make sure all params are `public` types,
 *      `WorkerContext` is the first param, and any default-valued params are skipped from
 *      Koin autowiring automatically.
 *   3. Re-build; the registry update is picked up via the Gradle plugin's task `inputs.files`.
 *
 * For platform-specific workers (e.g. `WebPushSubscriptionWorker` only on `wasmJsMain`),
 * annotate the worker class with `@WorkerForPlatforms([Platform.Web])` — the codegen will
 * skip it on other platforms' init files.
 *
 * See https://github.com/MobileByteLabs/worker-kmp/wiki/single-api-guide for full reference.
 */
@WorkerKmpWorkers(
    workers = [
        DataSyncWorker::class,
        NotificationWorker::class,
    ],
)
public fun workerDeclarations(): Unit = Unit
