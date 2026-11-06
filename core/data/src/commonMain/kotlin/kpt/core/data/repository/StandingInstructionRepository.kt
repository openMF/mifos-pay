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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.common.ScreenState
import kpt.core.model.standinginstruction.SITemplate
import kpt.core.model.standinginstruction.SIUpdatePayload
import kpt.core.model.standinginstruction.StandingInstruction
import kpt.core.model.standinginstruction.StandingInstructionPayload
interface StandingInstructionRepository {
    // Phase-3 cutover — reads on ScreenState.
    fun getStandingInstructionTemplate(
        fromOfficeId: Long,
        fromClientId: Long,
        fromAccountType: Long,
    ): Flow<ScreenState<SITemplate>>

    fun getAllStandingInstructions(
        clientId: Long,
    ): Flow<ScreenState<List<StandingInstruction>>>

    /**
     * Phase-5 Batch-3 LEDGER read — GOAL D13 (`createStore` + CACHE_FIRST_SWR).
     *
     * Store-backed alternative to [getAllStandingInstructions] — reads through
     * the `AppStoreRegistry.StandingInstruction` Store5 (offline-first via
     * `wallet_standing_instructions` Room SoT, SWR revalidation once the TTL
     * elapses).
     *
     * @param clientId the store's page key AND the API query parameter.
     * @param scope the caller's [CoroutineScope] (typically `viewModelScope`)
     *   — Store5 subscribes its internal refresh trigger to this scope.
     */
    fun getAllStandingInstructionsStream(
        clientId: Long,
        scope: CoroutineScope,
    ): ScreenDataStream<List<StandingInstruction>>

    fun getStandingInstruction(instructionId: Long): Flow<ScreenState<StandingInstruction>>

    // Writes complete normally on success and throw on failure; the caller's
    // SubmitHandler maps success/exception to SubmitState. The user-facing success
    // message is a feature StringResource surfaced by the ViewModel, not repo copy.
    suspend fun createStandingInstruction(
        payload: StandingInstructionPayload,
    )

    suspend fun updateStandingInstruction(
        instructionId: Long,
        payload: SIUpdatePayload,
    )

    suspend fun deleteStandingInstruction(instructionId: Long)
}
