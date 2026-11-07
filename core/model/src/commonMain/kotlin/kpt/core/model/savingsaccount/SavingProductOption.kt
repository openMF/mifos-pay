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
 * A savings product that can be selected when opening an account.
 *
 * @property id Unique identifier.
 * @property name Display name.
 * @property withdrawalFeeForTransfers Whether a withdrawal fee applies to transfers.
 * @property allowOverdraft Whether overdraft is allowed.
 * @property enforceMinRequiredBalance Whether the minimum required balance is enforced.
 * @property lienAllowed Whether a lien can be placed on the account.
 * @property withHoldTax Whether withholding tax applies.
 */
@Parcelize
@Serializable
data class SavingProductOption(
    val id: Long,
    val name: String,
    val withdrawalFeeForTransfers: Boolean,
    val allowOverdraft: Boolean,
    val enforceMinRequiredBalance: Boolean,
    val lienAllowed: Boolean,
    val withHoldTax: Boolean,
) : Parcelable
