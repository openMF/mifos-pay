/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.lib.loan.mapper

import org.mifos.lib.loan.core.model.CalendarData
import org.mifos.lib.loan.core.model.InterestRecalculationData
import org.mifos.lib.loan.network.model.CalendarDataResponseDto
import org.mifos.lib.loan.network.model.InterestRecalculationDataResponseDto

/** Maps this interest recalculation DTO to the [InterestRecalculationData] domain model. */
fun InterestRecalculationDataResponseDto.toModel(): InterestRecalculationData =
    InterestRecalculationData(
        id = id,
        loanId = loanId,
        interestRecalculationCompoundingType =
        interestRecalculationCompoundingType?.toInterestRecalculationCompoundingType(),
        rescheduleStrategyType = rescheduleStrategyType?.toRescheduleStrategyType(),
        calendarData = calendarData.toModel(),
        recalculationRestFrequencyType =
        recalculationRestFrequencyType?.toRecalculationRestFrequencyType(),
        recalculationRestFrequencyInterval = recalculationRestFrequencyInterval,
        recalculationCompoundingFrequencyType =
        recalculationCompoundingFrequencyType?.toRecalculationCompoundingFrequencyType(),
        compoundingToBePostedAsTransaction = compoundingToBePostedAsTransaction,
        allowCompoundingOnEod = allowCompoundingOnEod,
    )

/** Maps this calendar DTO to the [CalendarData] domain model. */
fun CalendarDataResponseDto.toModel(): CalendarData =
    CalendarData(
        id = id,
        calendarInstanceId = calendarInstanceId,
        entityId = entityId,
        entityType = entityType.toEntityType(),
        title = title,
        startDate = startDate,
        endDate = endDate,
        duration = duration,
        type = type.toType(),
        repeating = repeating,
        recurrence = recurrence,
        frequency = frequency.toFrequency(),
        interval = interval,
        repeatsOnNthDayOfMonth = repeatsOnNthDayOfMonth.toRepeatsOnNthDayOfMonth(),
        firstReminder = firstReminder,
        secondReminder = secondReminder,
        humanReadable = humanReadable,
        createdDate = createdDate,
        lastUpdatedDate = lastUpdatedDate,
        createdByUserId = createdByUserId,
        createdByUsername = createdByUsername,
        lastUpdatedByUserId = lastUpdatedByUserId,
        lastUpdatedByUsername = lastUpdatedByUsername,
    )
