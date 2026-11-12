/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.lib.loan.network.model

import kotlinx.serialization.Serializable

/**
 * Network response DTO for the account linking options, as returned by the Fineract loans API.
 *
 * @property accountNo Account number.
 * @property clientId Identifier of the client.
 * @property clientName Name of the client.
 * @property currency Currency of the amount.
 * @property fieldOfficerId Identifier of the field officer.
 * @property id Unique identifier.
 * @property productId Identifier of the loan product.
 * @property productName Name of the loan product.
 */
@Serializable
data class AccountLinkingOptionsResponseDto(

    val accountNo: String? = null,

    val clientId: Int? = null,

    val clientName: String? = null,

    val currency: CurrencyResponseDto? = null,

    val fieldOfficerId: Int? = null,

    val id: Int? = null,

    val productId: Int? = null,

    val productName: String? = null,
)
