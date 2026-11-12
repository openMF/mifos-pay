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
import kotlinx.coroutines.flow.first
import kpt.core.model.autopay.Bill
/**
 * TODO: This implementation currently uses local storage (Multiplatform Settings) for bill data.
 * When the backend APIs for bill management are clarified and implemented, this should be
 * refactored to use network-based repository pattern similar to other repositories in the codebase
 * (e.g., UserRepositoryImpl, BeneficiaryRepositoryImpl) with proper API integration.
 */
class BillRepositoryImpl(
    private val billDataSource: BillDataSource,
) : BillRepository {

    override fun getAllBills(): Flow<List<Bill>> {
        return billDataSource.bills
    }

    override suspend fun getBillById(id: String): Bill? {
        return try {
            val bills = billDataSource.bills.first()
            bills.find { it.id == id }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun saveBill(bill: Bill): Bill {
        val existingBills = billDataSource.bills.first()

        // Check if bill already exists
        val existingBill = existingBills.find {
            it.name == bill.name && it.billerId == bill.billerId
        }
        require(existingBill == null) { "Bill with this name and biller already exists" }
        billDataSource.addBill(bill)
        return bill
    }

    override suspend fun updateBill(bill: Bill): Bill {
        val existingBills = billDataSource.bills.first().toMutableList()
        val index = existingBills.indexOfFirst { it.id == bill.id }
        require(index != -1) { "Bill not found" }
        existingBills[index] = bill
        billDataSource.updateBills(existingBills)
        return bill
    }

    override suspend fun deleteBill(id: String) {
        billDataSource.removeBill(id)
    }

    override suspend fun searchBillsByName(query: String): List<Bill> {
        return try {
            val bills = billDataSource.bills.first()
            bills.filter { it.name.contains(query, ignoreCase = true) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun clearAllBills() {
        billDataSource.clearBills()
    }
}
