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

/**
 * Template returned by the server with defaults and options for creating a savings account.
 *
 * @property clientId Identifier of the client.
 * @property clientName Name of the client.
 * @property nominalAnnualInterestRate Nominal annual interest rate.
 * @property withdrawalFeeForTransfers Whether a withdrawal fee applies to transfers.
 * @property allowOverdraft Whether overdraft is allowed.
 * @property enforceMinRequiredBalance Whether the minimum required balance is enforced.
 * @property lienAllowed Whether a lien can be placed on the account.
 * @property withHoldTax Whether withholding tax applies.
 * @property isDormancyTrackingActive Whether dormancy tracking is active.
 * @property productOptions Savings products available for selection.
 * @property chargeOptions Charges available for selection.
 */
@Serializable
data class SavingAccountTemplate(
    val clientId: String,
    val clientName: String,
    val nominalAnnualInterestRate: Double,
    val withdrawalFeeForTransfers: Boolean,
    val allowOverdraft: Boolean,
    val enforceMinRequiredBalance: Boolean,
    val lienAllowed: Boolean,
    val withHoldTax: Boolean,
    val isDormancyTrackingActive: Boolean,
    val productOptions: List<SavingProductOption>,
    val chargeOptions: List<SavingCharge> = emptyList(),
)
