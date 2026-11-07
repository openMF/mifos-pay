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
 * Request body for transferring funds between two accounts in the same Fineract instance.
 *
 * @property fromOfficeId Office id of the source account.
 * @property fromClientId Client id of the source account owner.
 * @property fromAccountType Account type code of the source account.
 * @property fromAccountId Id of the source account.
 * @property toOfficeId Office id of the destination account.
 * @property toClientId Client id of the destination account owner.
 * @property toAccountType Account type code of the destination account.
 * @property toAccountId Id of the destination account.
 * @property transferAmount Amount to transfer, as a string.
 * @property transferDescription Free-text description of the transfer.
 * @property locale Locale used to parse the amount.
 * @property dateFormat Format pattern used to parse transferDate.
 * @property transferDate Date of the transfer, formatted per dateFormat.
 */
@Serializable
@Parcelize
data class AccountTransferPayload(
    val fromOfficeId: Long,
    val fromClientId: Long,
    val fromAccountType: Long,
    val fromAccountId: Long,

    val toOfficeId: Long,
    val toClientId: Long,
    val toAccountType: Long,
    val toAccountId: Long,

    val transferAmount: String,
    val transferDescription: String,

    val locale: String,
    val dateFormat: String,
    val transferDate: String,
) : Parcelable
