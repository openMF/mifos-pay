/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.lib.loan.core.model

import kotlinx.serialization.Serializable

/**
 * Domain model of the account linking options used in the loan application flow.
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
data class AccountLinkingOptions(

    val accountNo: String? = null,

    val clientId: Int? = null,

    val clientName: String? = null,

    val currency: Currency? = null,

    val fieldOfficerId: Int? = null,

    val id: Int? = null,

    val productId: Int? = null,

    val productName: String? = null,
)
