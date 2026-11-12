/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.savingsaccount

import kotlinx.serialization.Serializable
import kpt.core.common.Parcelable
import kpt.core.common.Parcelize
/**
 * Payment details attached to a savings transaction.
 *
 * @property id Unique identifier.
 * @property paymentType Payment type used.
 * @property accountNumber Account number.
 * @property checkNumber Cheque number.
 * @property routingCode Bank routing code.
 * @property receiptNumber Receipt number.
 * @property bankNumber Bank number.
 */
@Serializable
@Parcelize
data class PaymentDetailData(
    val id: Long,
    val paymentType: PaymentType? = null,
    val accountNumber: String? = null,
    val checkNumber: String? = null,
    val routingCode: String? = null,
    val receiptNumber: String? = null,
    val bankNumber: String? = null,
) : Parcelable
