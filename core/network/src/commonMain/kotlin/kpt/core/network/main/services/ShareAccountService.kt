/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.main.services

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import kotlinx.coroutines.flow.Flow
import kpt.core.model.network.entity.shareAccount.ShareWithAssociationsResponseDto
import kpt.core.network.main.utils.ApiEndPoints
/** Ktorfit service for share accounts. */
interface ShareAccountService {

    /** Fetches share account details. */
    @GET(ApiEndPoints.SHARE_ACCOUNTS + "/{accountId}")
    fun getShareAccountDetails(
        @Path("accountId") accountId: Long,
        @Query("associations") associations: String = "all",
    ): Flow<ShareWithAssociationsResponseDto>
}
