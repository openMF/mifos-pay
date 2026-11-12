/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(ExperimentalUuidApi::class)

package kpt.sync

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** Unique-work names for the two `sync/` workers. */
public const val DATA_SYNC_WORK_NAME: String = "kpt.sync.DataSyncWorker"

/**
 * Unique-work name PREFIX for scheduled notifications. A prefix, not a fixed name, because several notifications can
 * be in flight and each needs its own unique-work slot.
 */
public const val NOTIFICATION_WORK_NAME_PREFIX: String = "kpt.sync.NotificationWorker"

/**
 * Whether work runs as foreground (user-visible — notification + expedited
 * quota request) or background (silent). On platforms other than Android the
 * distinction collapses to a single execution mode — see [WorkScheduler] KDoc.
 */
public enum class WorkMode {
    /** User-visible: posts a notification and requests expedited quota. */
    Foreground,

    /** Silent. The only mode that exists off Android. */
    Background,
}

/**
 * Reified result handle of a [WorkScheduler] call. Returned by all enqueue +
 * schedule methods. The `uniqueName` is populated for unique-named enqueues
 * (e.g. data sync); null for one-off notification scheduling.
 */
public data class WorkHandle(
    /** The platform work id. */
    val id: Uuid,
    /** The unique-work name, or null for a one-off enqueue that claimed no slot. */
    val uniqueName: String?,
)

/**
 * Observable lifecycle state of an enqueued [WorkRequest]. Mirrors
 * `WorkInfo.State` in worker-kmp; collapsed to the user-meaningful slice
 * the [WorkScheduler.observeWork] caller cares about.
 */
public enum class WorkStatus {
    /** Enqueued, not yet started. */
    Pending,

    /** Executing now. */
    Running,

    /** Finished successfully — terminal. */
    Succeeded,

    /** Finished unsuccessfully after its retries — terminal. */
    Failed,

    /** Cancelled before finishing — terminal. */
    Cancelled,
}

/**
 * Content for a one-off notification scheduled via [WorkScheduler.scheduleNotification].
 *
 * @property title required headline.
 * @property body required body text.
 * @property channelId optional Android notification channel; defaults to a
 *   sync-related channel created by the host app's `App.onCreate`.
 */
public data class NotificationContent(
    val title: String,
    val body: String,
    val channelId: String? = null,
)
