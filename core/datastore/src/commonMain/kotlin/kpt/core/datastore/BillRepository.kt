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
import kpt.core.model.autopay.Bill
/** Repository contract for saved bills. */
interface BillRepository {
    /**
     * Get all saved bills
     */
    fun getAllBills(): Flow<List<Bill>>

    /**
     * Get bill by ID
     */
    suspend fun getBillById(id: String): Bill?

    /**
     * Save a new bill. Returns the saved bill or throws on failure.
     */
    suspend fun saveBill(bill: Bill): Bill

    /**
     * Update an existing bill. Returns the updated bill or throws on failure.
     */
    suspend fun updateBill(bill: Bill): Bill

    /**
     * Delete a bill. Throws on failure.
     */
    suspend fun deleteBill(id: String)

    /**
     * Search bills by name
     */
    suspend fun searchBillsByName(query: String): List<Bill>

    /**
     * Clear all bills. Throws on failure.
     */
    suspend fun clearAllBills()
}
