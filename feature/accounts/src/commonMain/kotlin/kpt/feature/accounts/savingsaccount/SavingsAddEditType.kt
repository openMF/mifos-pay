/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.accounts.savingsaccount

import kotlinx.serialization.Serializable

@Serializable
sealed class SavingsAddEditType {

    abstract val savingsAccountId: Long?

    @Serializable
    data object AddItem : SavingsAddEditType() {
        override val savingsAccountId: Long?
            get() = null
    }

    @Serializable
    data class EditItem(
        override val savingsAccountId: Long,
    ) : SavingsAddEditType()
}
