/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.autopay

import kotlinx.serialization.Serializable
import kpt.core.common.Parcelable
import kpt.core.common.Parcelize
// TODO: Align data models with final API response schema once confirmed by backend
@Serializable
@Parcelize
data class AutoPayPayload(
    val name: String = "",
    val description: String = "",
    val amount: String = "",
    val currency: String = "",
    val frequency: String = "",
    val frequencyInterval: String = "",
    val recipientName: String = "",
    val recipientAccountNumber: String = "",
    val recipientBankCode: String = "",
    val sourceAccountId: Long = 0,
    val sourceAccountNumber: String = "",
    val sourceAccountType: String = "",
    val clientId: Long = 0,
    val validFrom: String = "",
    val validTill: String = "",
    val maxAmount: String = "",
    val minAmount: String = "",
    val paymentMethod: String = "",
    val locale: String = "en",
    val dateFormat: String = "dd MMMM yyyy",
) : Parcelable

@Serializable
@Parcelize
data class AutoPayUpdatePayload(
    val name: String? = null,
    val description: String? = null,
    val amount: String? = null,
    val currency: String? = null,
    val frequency: String? = null,
    val frequencyInterval: String? = null,
    val recipientName: String? = null,
    val recipientAccountNumber: String? = null,
    val recipientBankCode: String? = null,
    val validFrom: String? = null,
    val validTill: String? = null,
    val maxAmount: String? = null,
    val minAmount: String? = null,
    val paymentMethod: String? = null,
    val status: String? = null,
    val locale: String = "en",
    val dateFormat: String = "dd MMMM yyyy",
) : Parcelable

@Serializable
@Parcelize
data class AutoPayHistory(
    val id: Long? = null,
    val autoPayId: Long? = null,
    val amount: Double? = null,
    val currency: String? = null,
    val status: PaymentStatus? = null,
    val transactionDate: String? = null,
    val recipientName: String? = null,
    val recipientAccountNumber: String? = null,
    val sourceAccountNumber: String? = null,
    val referenceNumber: String? = null,
    val failureReason: String? = null,
    val createdDate: String? = null,
) : Parcelable

@Serializable
@Parcelize
data class UpcomingPayment(
    val id: String? = null,
    val autoPayId: Long? = null,
    val scheduleName: String? = null,
    val amount: Double? = null,
    val currency: String? = null,
    val dueDate: String? = null,
    val status: PaymentStatus? = null,
    val recipientName: String? = null,
    val recipientAccountNumber: String? = null,
    val sourceAccountNumber: String? = null,
) : Parcelable
