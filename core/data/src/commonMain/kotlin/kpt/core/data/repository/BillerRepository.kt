/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.repository

import kotlinx.coroutines.flow.Flow
import kpt.core.model.autopay.Biller
import kpt.core.model.autopay.BillerCategory
/** Repository for the billers a wallet user can pay bills to. */
interface BillerRepository {
    /**
     * Get all saved billers
     */
    fun getAllBillers(): Flow<List<Biller>>

    /**
     * Get biller by ID
     */
    suspend fun getBillerById(id: String): Biller?

    /**
     * Save a new biller. Returns the saved biller or throws on failure.
     */
    suspend fun saveBiller(biller: Biller): Biller

    /**
     * Update an existing biller. Returns the updated biller or throws on failure.
     */
    suspend fun updateBiller(biller: Biller): Biller

    /**
     * Delete a biller. Throws on failure.
     */
    suspend fun deleteBiller(id: String)

    /**
     * Get billers by category
     */
    suspend fun getBillersByCategory(category: BillerCategory): List<Biller>

    /**
     * Search billers by name
     */
    suspend fun searchBillersByName(query: String): List<Biller>

    /**
     * Check if biller exists by name and account number
     */
    suspend fun isBillerExists(name: String, accountNumber: String): Boolean

    /**
     * Clear all billers. Throws on failure.
     */
    suspend fun clearAllBillers()
}
