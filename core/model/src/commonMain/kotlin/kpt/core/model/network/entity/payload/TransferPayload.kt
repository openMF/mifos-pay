/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.network.entity.payload

import kotlinx.serialization.Serializable

/**
 * Request body for an account-to-account transfer; the no-arg constructor leaves all fields unset.
 *
 * @property fromOfficeId Id of the source office.
 * @property fromClientId Id of the source client.
 * @property fromAccountType Type code of the source account.
 * @property fromAccountId Id of the source account.
 * @property toOfficeId Id of the destination office.
 * @property toClientId Id of the destination client.
 * @property toAccountType Type code of the destination account.
 * @property toAccountId Id of the destination account.
 * @property transferDate Transfer date.
 * @property transferAmount Amount to transfer.
 * @property transferDescription Description of the transfer.
 * @property dateFormat Date format pattern used for the date fields.
 * @property locale Locale used to parse numbers and dates.
 */
@Serializable
data class TransferPayload(
    val fromOfficeId: Int? = null,
    val fromClientId: Long? = null,
    val fromAccountType: Int? = null,
    val fromAccountId: Int? = null,
    val toOfficeId: Int? = null,
    val toClientId: Long? = null,
    val toAccountType: Int? = null,
    val toAccountId: Int? = null,
    val transferDate: String? = null,
    val transferAmount: Double? = null,
    val transferDescription: String? = null,
    val dateFormat: String? = "dd MMMM yyyy",
    val locale: String? = "en",
) {
    constructor() : this(
        fromOfficeId = null,
        fromClientId = null,
        fromAccountType = null,
        fromAccountId = null,
        toOfficeId = null,
        toClientId = null,
        toAccountType = null,
        toAccountId = null,
        transferDate = null,
        transferAmount = null,
        transferDescription = null,
    )
}
