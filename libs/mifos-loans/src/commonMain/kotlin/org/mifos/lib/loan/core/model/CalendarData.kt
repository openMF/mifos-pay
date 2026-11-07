/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.lib.loan.core.model

import kotlinx.serialization.Serializable

/**
 * Domain model of the calendar data used in the loan application flow.
 *
 * @property id Unique identifier.
 * @property calendarInstanceId Identifier of the calendar instance.
 * @property entityId Identifier of the entity the calendar is attached to.
 * @property entityType Type of the entity the calendar is attached to.
 * @property title Title.
 * @property startDate Start date.
 * @property endDate End date.
 * @property duration Duration.
 * @property type Type.
 * @property repeating Whether the event repeats.
 * @property recurrence Recurrence rule.
 * @property frequency Frequency.
 * @property interval Interval between occurrences.
 * @property repeatsOnNthDayOfMonth Repeats on nth day of month.
 * @property firstReminder Days before the event of the first reminder.
 * @property secondReminder Days before the event of the second reminder.
 * @property humanReadable Human-readable description of the recurrence.
 * @property createdDate Date the record was created.
 * @property lastUpdatedDate Date the record was last updated.
 * @property createdByUserId Identifier of the user who created the record.
 * @property createdByUsername Username of the user who created the record.
 * @property lastUpdatedByUserId Identifier of the user who last updated the record.
 * @property lastUpdatedByUsername Username of the user who last updated the record.
 */
@Serializable
data class CalendarData(
    val id: Int? = null,

    val calendarInstanceId: Int? = null,

    val entityId: Int? = null,

    val entityType: EntityType,

    val title: String? = null,

    val startDate: List<Int> = emptyList(),

    val endDate: List<Int> = emptyList(),

    val duration: Double? = null,

    val type: Type,

    val repeating: Boolean? = null,

    val recurrence: String? = null,

    val frequency: Frequency,

    val interval: Double? = null,

    val repeatsOnNthDayOfMonth: RepeatsOnNthDayOfMonth,

    val firstReminder: Int? = null,

    val secondReminder: Int? = null,

    val humanReadable: String? = null,

    val createdDate: String? = null,

    val lastUpdatedDate: String? = null,

    val createdByUserId: Int? = null,

    val createdByUsername: String? = null,

    val lastUpdatedByUserId: Int? = null,

    val lastUpdatedByUsername: String? = null,
)
