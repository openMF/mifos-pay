/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.network.entity.common

import kotlinx.serialization.Serializable

/**
 * Currency details embedded in Fineract account responses.
 *
 * @property code Machine-readable code.
 * @property name Display name.
 * @property decimalPlaces Number of decimal places.
 * @property inMultiplesOf Smallest unit the amount must be a multiple of.
 * @property displaySymbol Symbol shown for the currency.
 * @property nameCode Localization key of the name.
 * @property displayLabel Label shown to the user.
 */
@Serializable
data class CurrencyResponseDto(
    val code: String? = null,
    val name: String? = null,
    val decimalPlaces: Int? = null,
    val inMultiplesOf: Double? = null,
    val displaySymbol: String? = null,
    val nameCode: String? = null,
    val displayLabel: String? = null,
)
