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
import de.jensklingenberg.ktorfit.http.PUT
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import kotlinx.coroutines.flow.Flow
import kpt.core.model.network.entity.Page
import kpt.core.model.standinginstruction.SITemplate
import kpt.core.model.standinginstruction.SIUpdatePayload
import kpt.core.model.standinginstruction.StandingInstruction
import kpt.core.model.standinginstruction.StandingInstructionPayload
import kpt.core.network.main.utils.ApiEndPoints
/** Ktorfit service for standing instructions. */
interface StandingInstructionService {

    /** Fetches the template for creating a standing instruction. */
    @GET("${ApiEndPoints.STANDING_INSTRUCTION}/template")
    fun getStandingInstructionTemplate(
        @Query("fromOfficeId") fromOfficeId: Long,
        @Query("fromClientId") fromClientId: Long,
        @Query("fromAccountType") fromAccountType: Long,
    ): Flow<SITemplate>

    /**
     * @param clientId - passed as Query to limit the response to client specific response
     */
    @GET(ApiEndPoints.STANDING_INSTRUCTION)
    fun getAllStandingInstructions(
        @Query("clientId") clientId: Long,
    ): Flow<Page<StandingInstruction>>

    /** Fetches a single standing instruction. */
    @GET("${ApiEndPoints.STANDING_INSTRUCTION}/{standingInstructionId}")
    fun getStandingInstruction(
        @Path("standingInstructionId")
        standingInstructionId: Long,
    ): Flow<StandingInstruction>

    /** Creates a standing instruction. */
    @POST(ApiEndPoints.STANDING_INSTRUCTION)
    suspend fun createStandingInstruction(
        @Body payload: StandingInstructionPayload,
    )

    /**
     * @param command - if command is passed as "update" then the corresponding standing instruction
     * is updated. If passed as "delete" then the standing instruction is deleted.
     *
     * @param instructionId - unique id of the standing instruction on which the operation
     * will be performed.
     */
    @PUT("${ApiEndPoints.STANDING_INSTRUCTION}/{standingInstructionId}")
    suspend fun deleteStandingInstruction(
        @Path("standingInstructionId") instructionId: Long,
        @Query("command") command: String,
    )

    /** Updates a standing instruction. */
    @PUT("${ApiEndPoints.STANDING_INSTRUCTION}/{standingInstructionId}")
    suspend fun updateStandingInstruction(
        @Path("standingInstructionId") instructionId: Long,
        @Body payload: SIUpdatePayload,
        @Query("command") command: String,
    )
}
