/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.notification

import kotlinx.serialization.Serializable
import kpt.core.common.Parcelable
import kpt.core.common.Parcelize
/**
 * One page of notifications returned by the server.
 *
 * @property totalFilteredRecords Total number of records matching the query across all pages.
 * @property pageItems Notifications in this page.
 */
@Serializable
@Parcelize
data class NotificationPayload(
    val totalFilteredRecords: Long,
    val pageItems: List<Notification>,
) : Parcelable
