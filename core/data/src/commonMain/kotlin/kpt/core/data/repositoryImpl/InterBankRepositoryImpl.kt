/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.repositoryImpl

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kpt.core.common.ScreenStateStream
import kpt.core.common.asScreenStateFlow
import kpt.core.data.repository.InterBankRepository
import kpt.core.model.interbank.InterBankParticipantRequest
import kpt.core.model.interbank.InterBankParticipantResponse
import kpt.core.model.interbank.InterBankPartyInfoRequest
import kpt.core.model.interbank.InterBankPartyInfoResponse
import kpt.core.model.interbank.InterBankTransferRequest
import kpt.core.model.interbank.InterBankTransferResponse
import kpt.core.network.InterBankApiManager
/** Default [InterBankRepository] backed by the inter-bank API. */
class InterBankRepositoryImpl(
    private val apiManager: InterBankApiManager,
    private val ioDispatcher: CoroutineDispatcher,
) : InterBankRepository {

    override suspend fun fetchParticipant(
        partyId: String,
        currencyCode: String,
    ): InterBankParticipantResponse = withContext(ioDispatcher) {
        val request = InterBankParticipantRequest(
            partyId = partyId.trim(),
            currencyCode = currencyCode,
            partyIdType = "MSISDN",
        )
        apiManager.interBankApi.fetchParticipant(request)
    }

    override suspend fun fetchPartyInfo(
        partyId: String,
        currencyCode: String,
        ownerFspId: String,
    ): InterBankPartyInfoResponse = withContext(ioDispatcher) {
        val request = InterBankPartyInfoRequest(
            partyId = partyId.trim(),
            currencyCode = currencyCode,
            ownerFspId = ownerFspId,
            partyIdType = "MSISDN",
        )
        apiManager.interBankApi.fetchPartyInfo(request)
    }

    override fun findParticipant(
        partyId: String,
        currencyCode: String,
    ): ScreenStateStream<InterBankPartyInfoResponse> {
        return flow {
            // First, fetch participant to get the FSP ID, then fetch party info.
            val participant = fetchParticipant(partyId, currencyCode)
            val partyInfo = fetchPartyInfo(
                partyId = partyId,
                currencyCode = currencyCode,
                ownerFspId = participant.fspId,
            )
            emit(partyInfo)
        }.asScreenStateFlow().flowOn(ioDispatcher)
    }

    override suspend fun interBankMakeTransfer(
        request: InterBankTransferRequest,
    ): InterBankTransferResponse = withContext(ioDispatcher) {
        apiManager.interBankApi.interBankMakeTransfer(request)
    }
}
