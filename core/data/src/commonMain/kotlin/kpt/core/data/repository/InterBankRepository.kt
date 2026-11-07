/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.repository

import kpt.core.common.ScreenStateStream
import kpt.core.model.interbank.InterBankParticipantResponse
import kpt.core.model.interbank.InterBankPartyInfoResponse
import kpt.core.model.interbank.InterBankTransferRequest
import kpt.core.model.interbank.InterBankTransferResponse
/** Repository for inter-bank (cross-institution) transfers. */
interface InterBankRepository {
    /** Fetches the participant that owns the given identifier. */
    suspend fun fetchParticipant(
        partyId: String,
        currencyCode: String,
    ): InterBankParticipantResponse

    /** Fetches party information for an inter-bank transfer. */
    suspend fun fetchPartyInfo(
        partyId: String,
        currencyCode: String,
        ownerFspId: String,
    ): InterBankPartyInfoResponse

    /** Finds a participant for the given identifier. */
    fun findParticipant(
        partyId: String,
        currencyCode: String,
    ): ScreenStateStream<InterBankPartyInfoResponse>

    /** Executes an inter-bank transfer. */
    suspend fun interBankMakeTransfer(
        request: InterBankTransferRequest,
    ): InterBankTransferResponse
}
