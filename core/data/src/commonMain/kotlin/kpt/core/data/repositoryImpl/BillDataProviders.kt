/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.repositoryImpl

import kotlinx.coroutines.CoroutineDispatcher
import kpt.core.base.data.annotation.DataProvider
import kpt.core.base.data.annotation.FromQualifier
import kpt.core.data.repository.BillRepository
import kpt.core.data.repository.BillerRepository
import kpt.core.network.FineractApiManager

/**
 * The network-backed bill and biller repositories, bound under their fully qualified names.
 *
 * Both share a simple name with the offline `kpt.core.datastore` interfaces the screens use
 * (`BillOfflineRepositoryImpl`, `BillerOfflineRepositoryImpl`). On JS and Wasm, Koin keys a binding by
 * simple name, so an unqualified binding here would replace the offline one there. They have no
 * consumers yet; they wait for Fineract's bill-management endpoints.
 */
@DataProvider(qualifier = "kpt.core.data.repository.BillerRepository")
fun provideNetworkBillerRepository(
    apiManager: FineractApiManager,
    @FromQualifier("IO") ioDispatcher: CoroutineDispatcher,
): BillerRepository = BillerRepositoryImpl(apiManager, ioDispatcher)

/** The network-backed [BillRepository]; see [provideNetworkBillerRepository] for why it is qualified. */
@DataProvider(qualifier = "kpt.core.data.repository.BillRepository")
fun provideNetworkBillRepository(
    apiManager: FineractApiManager,
    @FromQualifier("IO") ioDispatcher: CoroutineDispatcher,
): BillRepository = BillRepositoryImpl(apiManager, ioDispatcher)
