/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.datastore

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kpt.core.model.autopay.AutoPay
import kpt.core.model.autopay.AutoPayHistory
import kpt.core.model.autopay.UpcomingPayment
interface AutoPayPreferencesRepository {
    /**
     * AutoPay enabled state
     */
    val isAutoPayEnabled: StateFlow<Boolean>

    /**
     * Cached AutoPay schedules
     */
    val cachedAutoPaySchedules: Flow<List<AutoPay>>

    /**
     * Cached upcoming payments
     */
    val cachedUpcomingPayments: Flow<List<UpcomingPayment>>

    /**
     * Cached AutoPay history
     */
    val cachedAutoPayHistory: Flow<List<AutoPayHistory>>

    /**
     * Last sync timestamp
     */
    val lastSyncTimestamp: StateFlow<Long>

    /**
     * Update AutoPay enabled state
     */
    suspend fun updateAutoPayEnabled(enabled: Boolean)

    /**
     * Cache AutoPay schedules
     */
    suspend fun cacheAutoPaySchedules(schedules: List<AutoPay>)

    /**
     * Cache upcoming payments
     */
    suspend fun cacheUpcomingPayments(payments: List<UpcomingPayment>)

    /**
     * Cache AutoPay history
     */
    suspend fun cacheAutoPayHistory(history: List<AutoPayHistory>)

    /**
     * Update last sync timestamp
     */
    suspend fun updateLastSyncTimestamp(timestamp: Long)

    /**
     * Clear all cached data
     */
    suspend fun clearCache()

    /**
     * Get cached AutoPay schedule by ID
     */
    suspend fun getCachedAutoPaySchedule(autoPayId: Long): AutoPay?

    /**
     * Check if cache is stale (older than specified time)
     */
    suspend fun isCacheStale(maxAgeMinutes: Long = 30): Boolean
}
