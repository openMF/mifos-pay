/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.bank

import kpt.core.common.Parcelable
import kpt.core.common.Parcelize
/**
 * A bank listed for selection when linking an external bank account.
 *
 * @property name Bank name.
 * @property image Drawable resource id of the bank logo.
 * @property bankType Whether the bank is shown as popular or other.
 */
@Parcelize
data class Bank(
    val name: String,
    val image: Int,
    val bankType: BankType = BankType.OTHER,
) : Parcelable

/**
 * Grouping used when listing banks.
 */
enum class BankType {
    /**
     * Banks shown in the popular section.
     */
    POPULAR,

    /**
     * All remaining banks.
     */
    OTHER,
}
