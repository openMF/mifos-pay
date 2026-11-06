/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.main.services

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import kotlinx.coroutines.flow.Flow
import kpt.core.model.account.AccountTransferPayload
import kpt.core.model.network.entity.templates.account.AccountOptionsTemplate
import kpt.core.model.savingsaccount.TransactionsEntity
import kpt.core.model.savingsaccount.TransferDetail
import kpt.core.model.search.AccountResult
import kpt.core.network.main.utils.ApiEndPoints
interface AccountTransfersService {

    @GET(ApiEndPoints.SAVINGS_ACCOUNTS + "/{accountId}/transactions/{transactionId}")
    fun getTransaction(
        @Path("accountId") clientId: Long,
        @Path("transactionId") transactionId: Long,
    ): Flow<TransactionsEntity>

    @GET(ApiEndPoints.ACCOUNT_TRANSFER + "/{transferId}")
    fun getAccountTransfer(@Path("transferId") transferId: Int): Flow<TransferDetail>

    @GET(ApiEndPoints.SEARCH)
    fun searchAccounts(
        @Query("query") query: String,
        @Query("resource") resource: String,
    ): Flow<List<AccountResult>>

    @POST(ApiEndPoints.ACCOUNT_TRANSFER)
    suspend fun makeTransfer(
        @Body payload: AccountTransferPayload,
    )

    @GET(ApiEndPoints.ACCOUNT_TRANSFER + "/template")
    fun getAccountTransferTemplate(): Flow<AccountOptionsTemplate>
}
