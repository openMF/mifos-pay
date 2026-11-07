/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(ExperimentalSerializationApi::class, ExperimentalSettingsApi::class)

package kpt.core.datastore

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import com.russhwolf.settings.serialization.decodeValue
import com.russhwolf.settings.serialization.encodeValue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.ListSerializer
import kpt.core.model.autopay.AutoPay
import kpt.core.model.autopay.AutoPayHistory
import kpt.core.model.autopay.UpcomingPayment
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private const val IS_AUTO_PAY_ENABLED_KEY = "is_autopay_enabled"
private const val CACHED_AUTO_PAY_SCHEDULES_KEY = "cached_autopay_schedules"
private const val CACHED_UPCOMING_PAYMENTS_KEY = "cached_upcoming_payments"
private const val CACHED_AUTO_PAY_HISTORY_KEY = "cached_autopay_history"
private const val LAST_SYNC_TIMESTAMP_KEY = "last_sync_timestamp"

/** Settings-backed local cache for AutoPay: enabled flag, schedules, upcoming payments, history and last-sync time. */
@OptIn(ExperimentalSerializationApi::class)
class AutoPayPreferencesDataSource(
    private val settings: Settings,
    private val dispatcher: CoroutineDispatcher,
) {

    private val _isAutoPayEnabled = MutableStateFlow(
        settings.getBoolean(IS_AUTO_PAY_ENABLED_KEY, false),
    )

    private val _cachedAutoPaySchedules = MutableStateFlow(
        settings.decodeValue(
            key = CACHED_AUTO_PAY_SCHEDULES_KEY,
            serializer = ListSerializer(AutoPay.serializer()),
            defaultValue = emptyList(),
        ),
    )

    private val _cachedUpcomingPayments = MutableStateFlow(
        settings.decodeValue(
            key = CACHED_UPCOMING_PAYMENTS_KEY,
            serializer = ListSerializer(UpcomingPayment.serializer()),
            defaultValue = emptyList(),
        ),
    )

    private val _cachedAutoPayHistory = MutableStateFlow(
        settings.decodeValue(
            key = CACHED_AUTO_PAY_HISTORY_KEY,
            serializer = ListSerializer(AutoPayHistory.serializer()),
            defaultValue = emptyList(),
        ),
    )

    private val _lastSyncTimestamp = MutableStateFlow(
        settings.getLong(LAST_SYNC_TIMESTAMP_KEY, 0L),
    )

    /** Whether AutoPay is enabled. */
    val isAutoPayEnabled: StateFlow<Boolean> = _isAutoPayEnabled

    /** Cached AutoPay schedules. */
    val cachedAutoPaySchedules: Flow<List<AutoPay>> = _cachedAutoPaySchedules

    /** Cached upcoming scheduled payments. */
    val cachedUpcomingPayments: Flow<List<UpcomingPayment>> = _cachedUpcomingPayments

    /** Cached history of executed AutoPay payments. */
    val cachedAutoPayHistory: Flow<List<AutoPayHistory>> = _cachedAutoPayHistory

    /** Epoch-millis time of the last successful sync, 0 if never synced. */
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp

    /** Persists the AutoPay [enabled] flag. */
    suspend fun updateAutoPayEnabled(enabled: Boolean) {
        withContext(dispatcher) {
            settings.putBoolean(IS_AUTO_PAY_ENABLED_KEY, enabled)
            _isAutoPayEnabled.value = enabled
        }
    }

    /** Replaces the cached AutoPay [schedules]. */
    suspend fun cacheAutoPaySchedules(schedules: List<AutoPay>) {
        withContext(dispatcher) {
            settings.putAutoPaySchedules(schedules)
            _cachedAutoPaySchedules.value = schedules
        }
    }

    /** Replaces the cached upcoming [payments]. */
    suspend fun cacheUpcomingPayments(payments: List<UpcomingPayment>) {
        withContext(dispatcher) {
            settings.putUpcomingPayments(payments)
            _cachedUpcomingPayments.value = payments
        }
    }

    /** Replaces the cached AutoPay [history]. */
    suspend fun cacheAutoPayHistory(history: List<AutoPayHistory>) {
        withContext(dispatcher) {
            settings.putAutoPayHistory(history)
            _cachedAutoPayHistory.value = history
        }
    }

    /** Persists [timestamp] as the last sync time. */
    suspend fun updateLastSyncTimestamp(timestamp: Long) {
        withContext(dispatcher) {
            settings.putLong(LAST_SYNC_TIMESTAMP_KEY, timestamp)
            _lastSyncTimestamp.value = timestamp
        }
    }

    /** Persists the current time as the last sync time. */
    @OptIn(ExperimentalTime::class)
    suspend fun updateLastSyncTimestamp() {
        withContext(dispatcher) {
            val timestamp = Clock.System.now().toEpochMilliseconds()
            settings.putLong(LAST_SYNC_TIMESTAMP_KEY, timestamp)
            _lastSyncTimestamp.value = timestamp
        }
    }

    /** Clears all cached AutoPay data and resets the last sync time. */
    suspend fun clearCache() {
        withContext(dispatcher) {
            settings.remove(CACHED_AUTO_PAY_SCHEDULES_KEY)
            settings.remove(CACHED_UPCOMING_PAYMENTS_KEY)
            settings.remove(CACHED_AUTO_PAY_HISTORY_KEY)
            settings.remove(LAST_SYNC_TIMESTAMP_KEY)

            _cachedAutoPaySchedules.value = emptyList()
            _cachedUpcomingPayments.value = emptyList()
            _cachedAutoPayHistory.value = emptyList()
            _lastSyncTimestamp.value = 0L
        }
    }

    /** Returns the cached schedule with [autoPayId], or null. */
    suspend fun getCachedAutoPaySchedule(autoPayId: Long): AutoPay? {
        return _cachedAutoPaySchedules.value.find { autoPay -> autoPay.id == autoPayId }
    }

    /** True when the last sync is older than [maxAgeMinutes]. */
    @OptIn(ExperimentalTime::class)
    suspend fun isCacheStale(maxAgeMinutes: Long): Boolean {
        val lastSync = _lastSyncTimestamp.value
        val currentTime = Clock.System.now().toEpochMilliseconds()
        val maxAgeMillis = maxAgeMinutes * 60 * 1000
        return (currentTime - lastSync) > maxAgeMillis
    }
}

private fun Settings.putAutoPaySchedules(schedules: List<AutoPay>) {
    encodeValue(
        key = CACHED_AUTO_PAY_SCHEDULES_KEY,
        serializer = ListSerializer(AutoPay.serializer()),
        value = schedules,
    )
}

private fun Settings.putUpcomingPayments(payments: List<UpcomingPayment>) {
    encodeValue(
        key = CACHED_UPCOMING_PAYMENTS_KEY,
        serializer = ListSerializer(UpcomingPayment.serializer()),
        value = payments,
    )
}

private fun Settings.putAutoPayHistory(history: List<AutoPayHistory>) {
    encodeValue(
        key = CACHED_AUTO_PAY_HISTORY_KEY,
        serializer = ListSerializer(AutoPayHistory.serializer()),
        value = history,
    )
}
