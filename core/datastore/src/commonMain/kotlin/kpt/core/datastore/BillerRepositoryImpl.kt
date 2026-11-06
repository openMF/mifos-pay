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
import kpt.core.model.autopay.Biller
import kpt.core.model.autopay.BillerCategory
/**
 * TODO: This implementation currently uses local storage (Multiplatform Settings) for biller data.
 * When the backend APIs for biller management are clarified and implemented, this should be
 * refactored to use network-based repository pattern similar to other repositories in the codebase
 * (e.g., UserRepositoryImpl, BeneficiaryRepositoryImpl) with proper API integration.
 */
class BillerRepositoryImpl(
    private val billerDataSource: BillerDataSource,
) : BillerRepository {

    override fun getAllBillers(): Flow<List<Biller>> {
        return billerDataSource.billers
    }

    override suspend fun getBillerById(id: String): Biller? {
        return try {
            val billers = billerDataSource.billers.first()
            billers.find { it.id == id }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun saveBiller(biller: Biller): Biller {
        val existingBillers = billerDataSource.billers.first()

        // Check if biller already exists
        val existingBiller = existingBillers.find {
            it.name == biller.name && it.accountNumber == biller.accountNumber
        }
        require(existingBiller == null) { "Biller with this name and account number already exists" }
        billerDataSource.addBiller(biller)
        return biller
    }

    override suspend fun updateBiller(biller: Biller): Biller {
        val existingBillers = billerDataSource.billers.first().toMutableList()
        val index = existingBillers.indexOfFirst { it.id == biller.id }
        require(index != -1) { "Biller not found" }
        existingBillers[index] = biller
        billerDataSource.updateBillers(existingBillers)
        return biller
    }

    override suspend fun deleteBiller(id: String) {
        billerDataSource.removeBiller(id)
    }

    override suspend fun getBillersByCategory(category: BillerCategory): List<Biller> {
        return try {
            val billers = billerDataSource.billers.first()
            billers.filter { it.category == category }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun searchBillersByName(query: String): List<Biller> {
        return try {
            val billers = billerDataSource.billers.first()
            billers.filter { it.name.contains(query, ignoreCase = true) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun isBillerExists(name: String, accountNumber: String): Boolean {
        return try {
            val billers = billerDataSource.billers.first()
            billers.any { it.name == name && it.accountNumber == accountNumber }
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun clearAllBillers() {
        billerDataSource.clearBillers()
    }
}
