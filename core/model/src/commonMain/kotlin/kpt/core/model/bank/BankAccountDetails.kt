/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.bank

import kpt.core.common.Parcelable
import kpt.core.common.Parcelize
/**
 * Details of an external bank account linked to the wallet, including UPI settings.
 *
 * @property accountNo Bank account number.
 * @property bankName Name of the bank.
 * @property accountHolderName Name of the account holder.
 * @property branch Branch name.
 * @property ifsc IFSC code of the branch.
 * @property type Type of the account.
 * @property isUpiEnabled Whether UPI is enabled for this account.
 * @property upiPin UPI PIN associated with the account.
 */
@Parcelize
data class BankAccountDetails(
    val accountNo: String,
    val bankName: String?,
    val accountHolderName: String?,
    val branch: String?,
    val ifsc: String?,
    val type: String?,
    val isUpiEnabled: Boolean = false,
    val upiPin: String?,
) : Parcelable
