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

@Serializable
@Parcelize
data class Biller
@OptIn(ExperimentalTime::class)
constructor(
    val id: String? = null,
    val name: String,
    val accountNumber: String,
    val contactNumber: String,
    val email: String? = null,
    val category: BillerCategory,
    val address: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val updatedAt: Long = Clock.System.now().toEpochMilliseconds(),
) : Parcelable

@Serializable
enum class BillerCategory(val displayName: String) {
    UTILITIES("Utilities"),
    INSURANCE("Insurance"),
    TELECOM("Telecommunications"),
    INTERNET("Internet & Cable"),
    LOAN("Loan Payments"),
    CREDIT_CARD("Credit Card"),
    RENT("Rent"),
    SUBSCRIPTION("Subscriptions"),
    OTHER("Other"),
    ;

    companion object {
        fun fromDisplayName(displayName: String): BillerCategory? {
            return entries.find { it.displayName == displayName }
        }
    }
}

@Serializable
data class BillerFormData(
    val name: String = "",
    val accountNumber: String = "",
    val contactNumber: String = "",
    val email: String = "",
    val category: BillerCategory? = null,
    val address: String = "",
)

@Serializable
data class BillerValidationResult(
    val isValid: Boolean,
    val nameError: String? = null,
    val accountNumberError: String? = null,
    val contactNumberError: String? = null,
    val emailError: String? = null,
    val categoryError: String? = null,
)
