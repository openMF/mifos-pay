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
 * Request body for opening a new savings account for a client.
 *
 * @property clientId Identifier of the client the account is opened for.
 * @property productId Identifier of the savings product.
 * @property nominalAnnualInterestRate Nominal annual interest rate.
 * @property minRequiredOpeningBalance Minimum balance required to open the account.
 * @property withdrawalFeeForTransfers Whether a withdrawal fee applies to transfers.
 * @property allowOverdraft Whether overdraft is allowed.
 * @property overdraftLimit Maximum overdraft amount.
 * @property enforceMinRequiredBalance Whether the minimum required balance is enforced.
 * @property withHoldTax Whether withholding tax applies.
 * @property externalId External identifier.
 * @property submittedOnDate Date the application was submitted.
 * @property locale Locale used to parse numbers and dates.
 * @property dateFormat Format used for date values.
 */
@Serializable
@Parcelize
data class CreateNewSavingEntity(
    val clientId: String,
    val productId: Long,
    val nominalAnnualInterestRate: Double,
    val minRequiredOpeningBalance: Long,
    val withdrawalFeeForTransfers: Boolean,
    val allowOverdraft: Boolean,
    val overdraftLimit: String,
    val enforceMinRequiredBalance: Boolean,
    val withHoldTax: Boolean,
    val externalId: String,
    val submittedOnDate: String,
    val locale: String,
    val dateFormat: String,
) : Parcelable
