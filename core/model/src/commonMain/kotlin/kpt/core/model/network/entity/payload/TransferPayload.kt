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
