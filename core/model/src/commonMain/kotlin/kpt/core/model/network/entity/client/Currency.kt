/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.network.entity.client

import kotlinx.serialization.Serializable

/**
 * A currency definition as reported by Fineract, with display metadata.
 *
 * @property code Currency code.
 * @property name Currency name.
 * @property decimalPlaces Number of decimal places.
 * @property displaySymbol Symbol shown for the currency.
 * @property nameCode Localization key of the name.
 * @property displayLabel Label shown to the user.
 */
@Serializable
data class Currency(
    val code: String? = null,
    val name: String? = null,
    val decimalPlaces: Int? = null,
    val displaySymbol: String? = null,
    val nameCode: String? = null,
    val displayLabel: String? = null,
) {
    constructor() : this(
        code = null,
        name = null,
        decimalPlaces = null,
        displaySymbol = null,
        nameCode = null,
        displayLabel = null,
    )
}
