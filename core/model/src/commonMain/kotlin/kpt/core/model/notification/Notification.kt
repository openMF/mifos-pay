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
import kpt.core.common.DateHelper.toFormattedDateTime
import kpt.core.common.IgnoredOnParcel
import kpt.core.common.Parcelable
import kpt.core.common.Parcelize
/**
 * A single in-app notification raised for the wallet user.
 *
 * @property id Identifier of the notification.
 * @property objectType Type of the entity the notification refers to.
 * @property objectId Identifier of the entity the notification refers to.
 * @property action Action that triggered the notification.
 * @property actorId Identifier of the user who triggered the notification.
 * @property content Notification text.
 * @property isRead Whether the user has read the notification.
 * @property isSystemGenerated Whether the system, rather than a user, generated the notification.
 * @property createdAt Creation timestamp as reported by the server.
 */
@Serializable
@Parcelize
data class Notification(
    val id: Long,
    val objectType: String,
    val objectId: Long,
    val action: String,
    val actorId: Long,
    val content: String,
    val isRead: Boolean,
    val isSystemGenerated: Boolean,
    val createdAt: String,
) : Parcelable {
    /** [createdAt] formatted as a date and time for display. */
    @IgnoredOnParcel
    val formattedDate = createdAt.toFormattedDateTime()
}
