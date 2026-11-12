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
import kpt.sync.NotificationContent

/**
 * One-shot notification worker. Reads [NotificationContent] from `inputData`
 * + posts a local notification via [renderNotification] — backed by the
 * multiplatform KMPNotifier library. See [initSyncNotifier] for the one-time setup.
 */
public class NotificationWorker(
    context: WorkerContext,
) : CoroutineWorker(context) {

    /** Input-data keys. */
    public companion object {
        /** Input-data key for the headline. */
        public const val KEY_TITLE: String = "kpt.sync.notification.title"

        /** Input-data key for the body text. */
        public const val KEY_BODY: String = "kpt.sync.notification.body"

        /** Input-data key for the Android channel id; ignored elsewhere. */
        public const val KEY_CHANNEL_ID: String = "kpt.sync.notification.channelId"
    }

    override suspend fun doWork(): WorkResult {
        val title = inputData.getString(KEY_TITLE)
        val body = inputData.getString(KEY_BODY)
        if (title == null || body == null) return WorkResult.failure("missing title or body")
        val channelId = inputData.getString(KEY_CHANNEL_ID)
        return runCatching {
            renderNotification(
                NotificationContent(title = title, body = body, channelId = channelId),
            )
            WorkResult.success()
        }.getOrElse { WorkResult.failure("renderNotification threw: ${it.message}") }
    }
}
