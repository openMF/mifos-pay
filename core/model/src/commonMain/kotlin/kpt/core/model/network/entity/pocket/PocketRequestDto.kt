/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.network.entity.pocket

import kotlinx.serialization.Serializable

@Serializable
data class PocketLinkRequest(
    val accountsDetail: List<AccountDetail>,
) {
    @Serializable
    data class AccountDetail(
        val accountId: String,
        val accountType: String,
    )
}

@Serializable
data class PocketCommandResponse(
    val resourceId: Long,
)

@Serializable
data class PocketDelinkRequest(
    val pocketAccountMappingIds: List<Long>,
)
