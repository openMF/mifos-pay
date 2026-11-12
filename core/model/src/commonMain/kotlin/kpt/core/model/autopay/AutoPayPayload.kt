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
/**
 * Request body for creating an auto-pay schedule.
 *
 * @property name Display name of the schedule.
 * @property description Free-text description.
 * @property amount Amount per payment, as a string.
 * @property currency Currency code.
 * @property frequency Recurrence frequency code.
 * @property frequencyInterval Number of frequency units between payments.
 * @property recipientName Name of the payee.
 * @property recipientAccountNumber Account number of the payee.
 * @property recipientBankCode Bank code of the payee's bank.
 * @property sourceAccountId Id of the account payments are drawn from.
 * @property sourceAccountNumber Account number of the source account.
 * @property sourceAccountType Type of the source account.
 * @property clientId Id of the owning client.
 * @property validFrom Start date of the schedule.
 * @property validTill End date of the schedule.
 * @property maxAmount Upper limit for a single payment.
 * @property minAmount Lower limit for a single payment.
 * @property paymentMethod Payment method used to execute payments.
 * @property locale Locale used to parse numbers in the request.
 * @property dateFormat Format pattern used for date fields.
 */
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

/**
 * Request body for updating an auto-pay schedule; only non-null fields are changed.
 *
 * @property name New display name.
 * @property description New description.
 * @property amount New amount per payment.
 * @property currency New currency code.
 * @property frequency New frequency code.
 * @property frequencyInterval New number of frequency units between payments.
 * @property recipientName New payee name.
 * @property recipientAccountNumber New payee account number.
 * @property recipientBankCode New payee bank code.
 * @property validFrom New start date.
 * @property validTill New end date.
 * @property maxAmount New upper limit per payment.
 * @property minAmount New lower limit per payment.
 * @property paymentMethod New payment method.
 * @property status New status.
 * @property locale Locale used to parse numbers in the request.
 * @property dateFormat Format pattern used for date fields.
 */
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

/**
 * A historical record of one auto-pay payment attempt.
 *
 * @property id Id of the history record.
 * @property autoPayId Id of the auto-pay schedule that produced it.
 * @property amount Amount paid.
 * @property currency Currency code.
 * @property status Outcome of the payment.
 * @property transactionDate Date the payment was made.
 * @property recipientName Name of the payee.
 * @property recipientAccountNumber Account number of the payee.
 * @property sourceAccountNumber Account number the payment was drawn from.
 * @property referenceNumber Reference number of the payment.
 * @property failureReason Reason for failure, when the payment failed.
 * @property createdDate Date the record was created.
 */
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

/**
 * A payment due soon under an auto-pay schedule.
 *
 * @property id Id of the upcoming payment.
 * @property autoPayId Id of the auto-pay schedule it belongs to.
 * @property scheduleName Name of the schedule.
 * @property amount Amount due.
 * @property currency Currency code.
 * @property dueDate Date the payment is due.
 * @property status Current status of the payment.
 * @property recipientName Name of the payee.
 * @property recipientAccountNumber Account number of the payee.
 * @property sourceAccountNumber Account number the payment will be drawn from.
 */
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
