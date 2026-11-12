/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.shared

/**
 * ForkWorkerDeclarations — the FORK-OWNED white-label seam for a fork's own background workers.
 *
 * The worker-kmp KSP processor scans cmp-shared/commonMain for EVERY `@WorkerKmpWorkers` site and
 * aggregates them with the template's base workers (see [workerDeclarations] in WorkerDeclarations.kt).
 * So declare YOUR fork's workers HERE — never edit the template WorkerDeclarations.kt. This file is
 * `owner: fork` in customization-surface.yaml, so a `sync-dirs` / `white-label-doctor` template sync
 * NEVER overwrites it while it full-copies the template infra.
 *
 * To register fork workers, annotate this function and list the classes (each a `CoroutineWorker`
 * per worker-kmp):
 * ```
 * @io.github.mobilebytelabs.worker.app.WorkerKmpWorkers(
 *     workers = [MyFeatureSyncWorker::class],
 * )
 * public fun forkWorkerDeclarations(): Unit = Unit
 * ```
 * Leave it un-annotated (as shipped) when the fork has no extra workers — the KSP processor finds
 * nothing to add here and uses the template's base worker set. The declaration itself is a no-op
 * anchor: it keeps this seam a valid, non-empty source file the fork edits in place.
 */
public fun forkWorkerDeclarations(): Unit = Unit
