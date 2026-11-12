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

/** Whether the savings account form creates a new account or edits an existing one. */
@Serializable
sealed class SavingsAddEditType {

    /** Id of the account being edited, or null when adding. */
    abstract val savingsAccountId: Long?

    /** Create a new savings account. */
    @Serializable
    data object AddItem : SavingsAddEditType() {
        override val savingsAccountId: Long?
            get() = null
    }

    /** Edit an existing savings account. */
    @Serializable
    data class EditItem(
        override val savingsAccountId: Long,
    ) : SavingsAddEditType()
}
