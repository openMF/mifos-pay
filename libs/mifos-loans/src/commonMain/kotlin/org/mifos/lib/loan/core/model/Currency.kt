/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.lib.loan.core.model

import kotlinx.serialization.Serializable

/**
 * Domain model of the currency used in the loan application flow.
 *
 * @property code Short code identifying the value.
 * @property name Name.
 * @property decimalPlaces Number of decimal places used for the currency.
 * @property inMultiplesOf Amounts must be a multiple of this value.
 * @property displaySymbol Currency symbol shown to the user.
 * @property nameCode Localization code for the currency name.
 * @property displayLabel Label shown to the user.
 */
@Serializable
data class Currency(

    val code: String? = null,

    val name: String? = null,

    val decimalPlaces: Double? = null,

    val inMultiplesOf: Int? = null,

    val displaySymbol: String? = null,

    val nameCode: String? = null,

    val displayLabel: String? = null,
)
