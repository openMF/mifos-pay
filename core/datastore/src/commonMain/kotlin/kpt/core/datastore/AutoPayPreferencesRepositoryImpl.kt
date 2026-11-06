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

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kpt.core.model.autopay.AutoPay
import kpt.core.model.autopay.AutoPayHistory
import kpt.core.model.autopay.UpcomingPayment
class AutoPayPreferencesRepositoryImpl(
    private val autoPayPreferencesDataSource: AutoPayPreferencesDataSource,
    private val ioDispatcher: CoroutineDispatcher,
    unconfinedDispatcher: CoroutineDispatcher,
) : AutoPayPreferencesRepository {
    private val unconfinedScope = CoroutineScope(unconfinedDispatcher)

    override val isAutoPayEnabled: StateFlow<Boolean> = autoPayPreferencesDataSource.isAutoPayEnabled

    override val cachedAutoPaySchedules: Flow<List<AutoPay>> = autoPayPreferencesDataSource.cachedAutoPaySchedules.flowOn(ioDispatcher)

    override val cachedUpcomingPayments: Flow<List<UpcomingPayment>> = autoPayPreferencesDataSource.cachedUpcomingPayments.flowOn(ioDispatcher)

    override val cachedAutoPayHistory: Flow<List<AutoPayHistory>> = autoPayPreferencesDataSource.cachedAutoPayHistory.flowOn(ioDispatcher)

    override val lastSyncTimestamp: StateFlow<Long> = autoPayPreferencesDataSource.lastSyncTimestamp

    override suspend fun updateAutoPayEnabled(enabled: Boolean) {
        autoPayPreferencesDataSource.updateAutoPayEnabled(enabled)
    }

    override suspend fun cacheAutoPaySchedules(schedules: List<AutoPay>) {
        autoPayPreferencesDataSource.cacheAutoPaySchedules(schedules)
    }

    override suspend fun cacheUpcomingPayments(payments: List<UpcomingPayment>) {
        autoPayPreferencesDataSource.cacheUpcomingPayments(payments)
    }

    override suspend fun cacheAutoPayHistory(history: List<AutoPayHistory>) {
        autoPayPreferencesDataSource.cacheAutoPayHistory(history)
    }

    override suspend fun updateLastSyncTimestamp(timestamp: Long) {
        autoPayPreferencesDataSource.updateLastSyncTimestamp(timestamp)
    }

    suspend fun updateLastSyncTimestamp() {
        autoPayPreferencesDataSource.updateLastSyncTimestamp()
    }

    override suspend fun clearCache() {
        autoPayPreferencesDataSource.clearCache()
    }

    override suspend fun getCachedAutoPaySchedule(autoPayId: Long): AutoPay? {
        return autoPayPreferencesDataSource.getCachedAutoPaySchedule(autoPayId)
    }

    override suspend fun isCacheStale(maxAgeMinutes: Long): Boolean {
        return autoPayPreferencesDataSource.isCacheStale(maxAgeMinutes)
    }
}
