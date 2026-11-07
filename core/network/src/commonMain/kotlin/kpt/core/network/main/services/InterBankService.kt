/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.main.services

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.POST
import kpt.core.model.interbank.InterBankParticipantRequest
import kpt.core.model.interbank.InterBankParticipantResponse
import kpt.core.model.interbank.InterBankPartyInfoRequest
import kpt.core.model.interbank.InterBankPartyInfoResponse
import kpt.core.model.interbank.InterBankTransferRequest
import kpt.core.model.interbank.InterBankTransferResponse
/** Ktorfit service for inter-bank transfers. */
interface InterBankService {
    /** Fetches a participant. */
    @POST("participant")
    suspend fun fetchParticipant(
        @Body request: InterBankParticipantRequest,
    ): InterBankParticipantResponse

    /** Fetches party information for an inter-bank transfer. */
    @POST("partyinfo")
    suspend fun fetchPartyInfo(
        @Body request: InterBankPartyInfoRequest,
    ): InterBankPartyInfoResponse

    /** Executes an inter-bank transfer. */
    @POST("executetransfer")
    suspend fun interBankMakeTransfer(
        @Body request: InterBankTransferRequest,
    ): InterBankTransferResponse
}
