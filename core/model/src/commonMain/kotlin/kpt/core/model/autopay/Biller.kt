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
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * A payee (utility, insurer, lender and so on) that bills are paid to.
 *
 * @property id Unique id of the biller.
 * @property name Name of the biller.
 * @property accountNumber The user's account number with the biller.
 * @property contactNumber Biller contact phone number.
 * @property email Optional biller email.
 * @property category Category of the biller.
 * @property address Optional biller address.
 * @property isActive Whether the biller is active.
 * @property createdAt Creation time as epoch milliseconds.
 * @property updatedAt Last update time as epoch milliseconds.
 */
@Serializable
@Parcelize
data class Biller
@OptIn(ExperimentalTime::class)
constructor(
    /**
     * Unique id of the biller.
     */
    val id: String? = null,
    /**
     * Name of the biller.
     */
    val name: String,
    /**
     * The user's account number with the biller.
     */
    val accountNumber: String,
    /**
     * Biller contact phone number.
     */
    val contactNumber: String,
    /**
     * Optional biller email.
     */
    val email: String? = null,
    /**
     * Category of the biller.
     */
    val category: BillerCategory,
    /**
     * Optional biller address.
     */
    val address: String? = null,
    /**
     * Whether the biller is active.
     */
    val isActive: Boolean = true,
    /**
     * Creation time as epoch milliseconds.
     */
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    /**
     * Last update time as epoch milliseconds.
     */
    val updatedAt: Long = Clock.System.now().toEpochMilliseconds(),
) : Parcelable

/**
 * Category of a biller, with its display label.
 *
 * @property displayName Human-readable label.
 */
@Serializable
enum class BillerCategory(val displayName: String) {
    /**
     * Utilities such as water and electricity.
     */
    UTILITIES("Utilities"),

    /**
     * Insurance premiums.
     */
    INSURANCE("Insurance"),

    /**
     * Telephone and mobile services.
     */
    TELECOM("Telecommunications"),

    /**
     * Internet and cable services.
     */
    INTERNET("Internet & Cable"),

    /**
     * Loan repayments.
     */
    LOAN("Loan Payments"),

    /**
     * Credit card payments.
     */
    CREDIT_CARD("Credit Card"),

    /**
     * Rent payments.
     */
    RENT("Rent"),

    /**
     * Recurring subscriptions.
     */
    SUBSCRIPTION("Subscriptions"),

    /**
     * Any other category.
     */
    OTHER("Other"),
    ;

    /**
     * Lookup helpers for BillerCategory.
     */
    companion object {
        /**
         * Returns the category whose display name matches, or null if none does.
         */
        fun fromDisplayName(displayName: String): BillerCategory? {
            return entries.find { it.displayName == displayName }
        }
    }
}

/**
 * Editable form state for creating or editing a biller.
 *
 * @property name Entered biller name.
 * @property accountNumber Entered account number.
 * @property contactNumber Entered contact number.
 * @property email Entered email.
 * @property category Selected category, if any.
 * @property address Entered address.
 */
@Serializable
data class BillerFormData(
    val name: String = "",
    val accountNumber: String = "",
    val contactNumber: String = "",
    val email: String = "",
    val category: BillerCategory? = null,
    val address: String = "",
)

/**
 * Outcome of validating a BillerFormData, with an error message per invalid field.
 *
 * @property isValid True when every field passed validation.
 * @property nameError Error for the name field.
 * @property accountNumberError Error for the account number field.
 * @property contactNumberError Error for the contact number field.
 * @property emailError Error for the email field.
 * @property categoryError Error for the category field.
 */
@Serializable
data class BillerValidationResult(
    val isValid: Boolean,
    val nameError: String? = null,
    val accountNumberError: String? = null,
    val contactNumberError: String? = null,
    val emailError: String? = null,
    val categoryError: String? = null,
)
