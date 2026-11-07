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

/**
 * Request body for linking accounts to a pocket.
 *
 * @property accountsDetail Accounts to link.
 */
@Serializable
data class PocketLinkRequest(
    val accountsDetail: List<AccountDetail>,
) {
    /**
     * Identifies one account (id and type) to link to a pocket.
     *
     * @property accountId Id of the account.
     * @property accountType Type of the account.
     */
    @Serializable
    data class AccountDetail(
        val accountId: String,
        val accountType: String,
    )
}

/**
 * Response to a pocket link command, carrying the created resource id.
 *
 * @property resourceId Id of the created or affected resource.
 */
@Serializable
data class PocketCommandResponse(
    val resourceId: Long,
)

/**
 * Request body for removing accounts from a pocket by mapping id.
 *
 * @property pocketAccountMappingIds Ids of the pocket-account mappings to remove.
 */
@Serializable
data class PocketDelinkRequest(
    val pocketAccountMappingIds: List<Long>,
)
