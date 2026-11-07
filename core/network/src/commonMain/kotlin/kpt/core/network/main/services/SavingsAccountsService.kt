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
import kpt.core.model.network.entity.Page
import kpt.core.model.savingsaccount.BlockUnblockResponseEntity
import kpt.core.model.savingsaccount.CreateNewSavingEntity
import kpt.core.model.savingsaccount.SavingAccountTemplate
import kpt.core.model.savingsaccount.SavingsWithAssociationsEntity
import kpt.core.model.savingsaccount.TransactionsEntity
import kpt.core.model.savingsaccount.UpdateSavingAccountEntity
import kpt.core.network.main.utils.ApiEndPoints
/** Ktorfit service for savings accounts. */
interface SavingsAccountsService {
    /** Fetches a savings account with its associations. */
    @GET(ApiEndPoints.SAVINGS_ACCOUNTS + "/{accountId}")
    fun getSavingsWithAssociations(
        @Path("accountId") accountId: Long,
        @Query("associations") associationType: String,
    ): Flow<SavingsWithAssociationsEntity>

    /** Fetches savings accounts. */
    @GET(ApiEndPoints.SAVINGS_ACCOUNTS)
    suspend fun getSavingsAccounts(
        @Query("limit") limit: Int,
    ): Flow<Page<SavingsWithAssociationsEntity>>

    /** Creates a savings account. */
    @POST(ApiEndPoints.SAVINGS_ACCOUNTS)
    suspend fun createSavingsAccount(@Body savingAccount: CreateNewSavingEntity)

    /** Updates a savings account. */
    @POST(ApiEndPoints.SAVINGS_ACCOUNTS + "/{accountId}")
    suspend fun updateSavingsAccount(
        @Path("accountId") accountId: Long,
        @Body savingAccount: UpdateSavingAccountEntity,
    )

    /** Blocks or unblocks a savings account. */
    @POST(ApiEndPoints.SAVINGS_ACCOUNTS + "/{accountId}")
    suspend fun blockUnblockAccount(
        @Path("accountId") accountId: Long,
        @Query("command") command: String?,
    ): BlockUnblockResponseEntity

    /** Fetches a transaction of a savings account. */
    @GET(
        ApiEndPoints.SAVINGS_ACCOUNTS + "/{accountId}/" + ApiEndPoints.TRANSACTIONS +
            "/{transactionId}",
    )
    suspend fun getSavingAccountTransaction(
        @Path("accountId") accountId: Long,
        @Path("transactionId") transactionId: Long,
    ): Flow<TransactionsEntity>

    /** Initiates a pay-via-mobile transaction on the account. */
    @POST(
        ApiEndPoints.SAVINGS_ACCOUNTS +
            "/{accountId}/" + ApiEndPoints.TRANSACTIONS + "?command=deposit",
    )
    suspend fun payViaMobile(@Path("accountId") accountId: Long): Flow<TransactionsEntity>

    /** Fetches the template for creating a savings account. */
    @GET(ApiEndPoints.SAVINGS_ACCOUNTS + "/template")
    fun getSavingAccountTemplate(@Query("clientId") clientId: Long): Flow<SavingAccountTemplate>
}
