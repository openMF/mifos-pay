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
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.ListSerializer
import kpt.core.model.autopay.Bill
private const val BILLS_KEY = "bills"

/** Settings-backed local store of [Bill]s (superseded by Room, see PreferencesModule). */
class BillDataSource(
    private val settings: Settings,
    private val dispatcher: CoroutineDispatcher,
) {
    private val _bills = MutableStateFlow(
        settings.decodeValue(
            key = BILLS_KEY,
            serializer = ListSerializer(Bill.serializer()),
            defaultValue = emptyList(),
        ),
    )

    /** Stored bills. */
    val bills: Flow<List<Bill>> = _bills

    /** Replaces all stored [bills]. */
    suspend fun updateBills(bills: List<Bill>) {
        withContext(dispatcher) {
            settings.putBills(bills)
            _bills.value = bills
        }
    }

    /** Adds [bill] to the stored list. */
    suspend fun addBill(bill: Bill) {
        withContext(dispatcher) {
            val currentBills = _bills.value.toMutableList()
            if (!currentBills.any { it.id == bill.id }) {
                currentBills.add(bill)
                settings.putBills(currentBills)
                _bills.value = currentBills
            }
        }
    }

    /** Removes the bill with [billId]. */
    suspend fun removeBill(billId: String) {
        withContext(dispatcher) {
            val currentBills = _bills.value.toMutableList()
            currentBills.removeAll { it.id == billId }
            settings.putBills(currentBills)
            _bills.value = currentBills
        }
    }

    /** Removes all stored bills. */
    suspend fun clearBills() {
        withContext(dispatcher) {
            settings.remove(BILLS_KEY)
            _bills.value = emptyList()
        }
    }
}

private fun Settings.putBills(bills: List<Bill>) {
    encodeValue(
        key = BILLS_KEY,
        serializer = ListSerializer(Bill.serializer()),
        value = bills,
    )
}
