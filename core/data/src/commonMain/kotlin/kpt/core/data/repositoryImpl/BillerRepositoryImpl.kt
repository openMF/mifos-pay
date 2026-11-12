/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.repositoryImpl

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kpt.core.data.repository.BillerRepository
import kpt.core.model.autopay.Biller
import kpt.core.model.autopay.BillerCategory
import kpt.core.network.main.services.BillerService
/**
 * Network-based implementation of BillerRepository.
 *
 * TODO: This implementation uses placeholder API endpoints. When the backend APIs
 * for biller management are finalized, update the endpoints and request/response
 * models according to the actual API contract.
 *
 * TODO(phase-4): Migrate to ScreenState via `createOfflineStore` (Biller
 * autopay-bills tier). Deferred from Phase-3 Batch B cutover because billers
 * need a real Store5 offline store (SourceOfTruth = Room, fetcher = Fineract
 * billers/categories endpoints) before their reads can adopt the fork-wide
 * `ScreenStateStream<T>` envelope.
 */
class BillerRepositoryImpl(
    private val billerService: BillerService,
    private val ioDispatcher: CoroutineDispatcher,
) : BillerRepository {

    override fun getAllBillers(): Flow<List<Biller>> {
        return billerService
            .getAllBillers()
            .catch {
                // Return empty list on error for now
                emit(emptyList())
            }
            .flowOn(ioDispatcher)
    }

    override suspend fun getBillerById(id: String): Biller? {
        return try {
            withContext(ioDispatcher) {
                billerService.getBillerById(id).first()
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun saveBiller(biller: Biller): Biller {
        return withContext(ioDispatcher) {
            billerService.createBiller(biller).first()
        }
    }

    override suspend fun updateBiller(biller: Biller): Biller {
        val billerId = requireNotNull(biller.id) { "Biller ID is required for update" }
        return withContext(ioDispatcher) {
            billerService.updateBiller(billerId, biller).first()
        }
    }

    override suspend fun deleteBiller(id: String) {
        withContext(ioDispatcher) {
            billerService.deleteBiller(id).first()
        }
    }

    override suspend fun getBillersByCategory(category: BillerCategory): List<Biller> {
        return try {
            withContext(ioDispatcher) {
                billerService.getBillersByCategory(category).first()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun searchBillersByName(query: String): List<Biller> {
        return try {
            withContext(ioDispatcher) {
                billerService.searchBillersByName(query).first()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun isBillerExists(name: String, accountNumber: String): Boolean {
        return try {
            withContext(ioDispatcher) {
                billerService.isBillerExists(name, accountNumber).first()
            }
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun clearAllBillers() {
        withContext(ioDispatcher) {
            billerService.clearAllBillers().first()
        }
    }
}
