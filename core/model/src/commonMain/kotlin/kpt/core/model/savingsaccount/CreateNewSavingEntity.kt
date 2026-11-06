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
