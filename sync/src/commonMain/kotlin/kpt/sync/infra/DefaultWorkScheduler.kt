/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(
    io.github.mobilebytelabs.worker.ExperimentalWorkerApi::class,
    kotlin.uuid.ExperimentalUuidApi::class,
)

package kpt.sync.infra

import io.github.mobilebytelabs.worker.ExistingPeriodicWorkPolicy
import io.github.mobilebytelabs.worker.ExistingWorkPolicy
import io.github.mobilebytelabs.worker.OutOfQuotaPolicy
import io.github.mobilebytelabs.worker.WorkInfo
import io.github.mobilebytelabs.worker.WorkManager
import io.github.mobilebytelabs.worker.oneTimeWorkRequest
import io.github.mobilebytelabs.worker.periodicWorkRequest
import io.github.mobilebytelabs.worker.workDataOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kpt.sync.DATA_SYNC_WORK_NAME
import kpt.sync.NOTIFICATION_WORK_NAME_PREFIX
import kpt.sync.NotificationContent
import kpt.sync.WorkHandle
import kpt.sync.WorkMode
import kpt.sync.WorkScheduler
import kpt.sync.WorkStatus
import kotlin.time.Duration

/**
 * Default [WorkScheduler] — thin façade over worker-kmp's [WorkManager].
 *
 * Encodes the project's policies:
 *  - Data sync is unique-named under [DATA_SYNC_WORK_NAME] with KEEP — silent
 *    no-op if a sync is already in flight.
 *  - Notifications are one-shot with `initialDelay`; tagged with the worker's
 *    Uuid so [observeWork] can re-find them via [WorkManager.getWorkInfosByTag].
 *  - [WorkMode.Foreground] requests Android expedited execution via
 *    [OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST] (silent fallback if
 *    quota exhausted). No-op on non-Android platforms.
 */
public class DefaultWorkScheduler(
    private val workManager: WorkManager,
) : WorkScheduler {

    override suspend fun enqueueDataSync(mode: WorkMode): WorkHandle {
        val request = oneTimeWorkRequest<DataSyncWorker> {
            addTag(DATA_SYNC_WORK_NAME)
            if (mode == WorkMode.Foreground) {
                setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            }
        }
        val id = workManager.enqueueUniqueWork(
            uniqueWorkName = DATA_SYNC_WORK_NAME,
            existingWorkPolicy = ExistingWorkPolicy.KEEP,
            request = request,
        )
        return WorkHandle(id = id, uniqueName = DATA_SYNC_WORK_NAME)
    }

    override suspend fun scheduleNotification(
        content: NotificationContent,
        delay: Duration,
        mode: WorkMode,
        uniqueName: String?,
        tags: List<String>,
    ): WorkHandle {
        val request = oneTimeWorkRequest<NotificationWorker> {
            setInputData(
                workDataOf(
                    NotificationWorker.KEY_TITLE to content.title,
                    NotificationWorker.KEY_BODY to content.body,
                    NotificationWorker.KEY_CHANNEL_ID to content.channelId,
                ),
            )
            setInitialDelay(delay)
            addTag(NOTIFICATION_WORK_NAME_PREFIX)
            tags.forEach { addTag(it) }
            if (mode == WorkMode.Foreground) {
                setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            }
        }
        // Unique-named → REPLACE the pending request under that name; else fire-and-forget.
        val id = if (uniqueName != null) {
            workManager.enqueueUniqueWork(
                uniqueWorkName = uniqueName,
                existingWorkPolicy = ExistingWorkPolicy.REPLACE,
                request = request,
            )
        } else {
            workManager.enqueue(request)
        }
        return WorkHandle(id = id, uniqueName = uniqueName)
    }

    override suspend fun schedulePeriodicNotification(
        content: NotificationContent,
        interval: Duration,
        uniqueName: String,
        initialDelay: Duration,
        tags: List<String>,
    ): WorkHandle {
        val request = periodicWorkRequest<NotificationWorker>(interval) {
            setInputData(
                workDataOf(
                    NotificationWorker.KEY_TITLE to content.title,
                    NotificationWorker.KEY_BODY to content.body,
                    NotificationWorker.KEY_CHANNEL_ID to content.channelId,
                ),
            )
            setInitialDelay(initialDelay)
            addTag(NOTIFICATION_WORK_NAME_PREFIX)
            tags.forEach { addTag(it) }
        }
        // UPDATE keeps the running schedule and takes the new content, so calling this on every app
        // start does not reset the period or stack duplicates.
        val id = workManager.enqueueUniquePeriodicWork(
            uniqueWorkName = uniqueName,
            existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE,
            request = request,
        )
        return WorkHandle(id = id, uniqueName = uniqueName)
    }

    override suspend fun cancelWorkByTag(tag: String) {
        workManager.cancelAllWorkByTag(tag)
    }

    override fun observeWork(handle: WorkHandle): Flow<WorkStatus> = workManager.getWorkInfosByTag(
        tag = handle.uniqueName ?: NOTIFICATION_WORK_NAME_PREFIX,
    ).map { infos ->
        val match = infos.firstOrNull { it.id == handle.id }
        match?.state?.toWorkStatus() ?: WorkStatus.Pending
    }

    override suspend fun cancelWork(handle: WorkHandle) {
        workManager.cancelWorkById(handle.id)
    }
}

private fun WorkInfo.State.toWorkStatus(): WorkStatus = when (this) {
    WorkInfo.State.ENQUEUED -> WorkStatus.Pending
    WorkInfo.State.RUNNING -> WorkStatus.Running
    WorkInfo.State.SUCCEEDED -> WorkStatus.Succeeded
    WorkInfo.State.FAILED -> WorkStatus.Failed
    WorkInfo.State.CANCELLED -> WorkStatus.Cancelled
    WorkInfo.State.BLOCKED -> WorkStatus.Pending
}
