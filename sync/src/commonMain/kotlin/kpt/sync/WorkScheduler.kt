/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.sync

import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

/**
 * Koin-injectable façade over worker-kmp's `WorkManager` — the **single API
 * surface** every consumer module (feature/loans, feature/bills, …) uses to
 * schedule data sync + notifications.
 *
 * Consumers depend on the `sync` module to get this interface but NEVER
 * import `io.github.mobilebytelabs.worker.*` directly. That decoupling is the
 * point: when worker-kmp's `WorkManager` API evolves, only `DefaultWorkScheduler`
 * (the impl) changes — call sites stay stable.
 *
 * Example (cross-module use from `feature/loans`):
 * ```kotlin
 * class LoanReminderUseCase(private val scheduler: WorkScheduler) {
 *     fun scheduleDueDateReminder(loan: Loan) {
 *         scheduler.scheduleNotification(
 *             NotificationContent("Loan payment due", "Loan #${loan.id}"),
 *             delay = (loan.dueAt - now()) - 1.days,
 *             mode = WorkMode.Foreground,
 *         )
 *     }
 *     fun refreshLoans() = scheduler.enqueueDataSync(mode = WorkMode.Background)
 * }
 * ```
 */
public interface WorkScheduler {
    /**
     * Enqueue a one-shot data sync (fans out the in-process
     * `List<Syncable>` via the `DataSyncWorker`'s explicit awaitAll).
     * Unique-named [DATA_SYNC_WORK_NAME] under KEEP policy so re-invocations
     * don't pile up.
     *
     * @param mode [WorkMode.Foreground] requests Android expedited execution;
     *   silent on background. [WorkMode.Background] runs under normal
     *   constraints (connected network).
     */
    public suspend fun enqueueDataSync(mode: WorkMode = WorkMode.Background): WorkHandle

    /**
     * Schedule a one-shot notification. The [NotificationWorker] posts the notification via the
     * multiplatform KMPNotifier `localNotifier` (see `SyncNotifier.kt`).
     *
     * @param delay non-negative; pass [Duration.ZERO] for immediate. Backed by
     *   `OneTimeWorkRequest.initialDelay`.
     * @param uniqueName when non-null the request is enqueued as unique work with REPLACE — a later
     *   schedule under the same name replaces the pending one (e.g. re-scheduling a bill after an edit).
     *   Null = fire-and-forget (no de-duplication).
     * @param tags extra tags added to the request (beyond the framework notification tag) so callers
     *   can cancel a logical group via [cancelWorkByTag] (e.g. one tag per bill + one shared tag).
     */
    public suspend fun scheduleNotification(
        content: NotificationContent,
        delay: Duration = Duration.ZERO,
        mode: WorkMode = WorkMode.Background,
        uniqueName: String? = null,
        tags: List<String> = emptyList(),
    ): WorkHandle

    /**
     * A notification that repeats every [interval] (a daily reminder, a weekly digest) until cancelled.
     * One-shot notifications forced callers to pre-schedule N of them and re-schedule on every launch,
     * so reminders stopped when the app was not opened for N days.
     *
     * @param uniqueName required: re-scheduling under the same name UPDATES the existing periodic work
     *   (keeps its timing, takes the new content) instead of stacking a second one on every app start.
     * @param initialDelay before the first delivery, e.g. to align a daily reminder to a time of day.
     *
     * The default falls back to a single one-shot delivery, so an existing implementation (a test fake)
     * keeps compiling; [kpt.sync.infra.DefaultWorkScheduler] schedules real periodic work.
     */
    public suspend fun schedulePeriodicNotification(
        content: NotificationContent,
        interval: Duration,
        uniqueName: String,
        initialDelay: Duration = Duration.ZERO,
        tags: List<String> = emptyList(),
    ): WorkHandle = scheduleNotification(content, initialDelay, WorkMode.Background, uniqueName, tags)

    /**
     * Observe a previously-enqueued work unit. Emits a [WorkStatus] every time
     * the underlying `WorkInfo` transitions.
     */
    public fun observeWork(handle: WorkHandle): Flow<WorkStatus>

    /** Cancel a previously-enqueued work unit by handle. */
    public suspend fun cancelWork(handle: WorkHandle)

    /** Cancel every enqueued work unit carrying [tag] (e.g. all bill-reminder notifications). */
    public suspend fun cancelWorkByTag(tag: String)
}
