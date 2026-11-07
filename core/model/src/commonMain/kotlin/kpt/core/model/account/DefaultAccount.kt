/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.account

import kotlinx.serialization.Serializable
import kpt.core.common.Parcelable
import kpt.core.common.Parcelize
/**
 * The account the user has chosen as their default for payments.
 *
 * @property accountId Id of the default account.
 * @property accountNo Account number of the default account.
 */
@Serializable
@Parcelize
data class DefaultAccount(
    val accountId: Long,
    val accountNo: String,
) : Parcelable {
    /**
     * Holds the empty default value.
     */
    companion object {
        /**
         * Placeholder instance with id 0 and a blank account number, meaning no default account is set.
         */
        val DEFAULT = DefaultAccount(
            accountId = 0,
            accountNo = "",
        )
    }
}
