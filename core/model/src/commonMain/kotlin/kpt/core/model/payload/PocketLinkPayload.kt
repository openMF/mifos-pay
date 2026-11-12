/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.payload

import kpt.core.model.enums.AccountType
/**
 * Request body that links one or more accounts to a pocket.
 *
 * @property accountsDetail Accounts to link to the pocket.
 */
data class PocketLinkPayload(
    val accountsDetail: List<AccountDetail>,
) {
    /**
     * Reference to one account (id and type) to be linked to a pocket.
     *
     * @property accountId Identifier of the account.
     * @property accountType Type of the account.
     */
    data class AccountDetail(
        val accountId: String,
        val accountType: AccountType,
    )
}
