/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.beneficiary

import kotlinx.serialization.Serializable

/**
 * Request body for adding a beneficiary.
 *
 * @property locale Locale used to parse numbers in the request.
 * @property name Display name of the beneficiary.
 * @property accountNumber Beneficiary account number.
 * @property accountType Account type code.
 * @property transferLimit Maximum amount allowed per transfer.
 * @property officeName Name of the beneficiary's office.
 */
@Serializable
data class BeneficiaryPayload(
    val locale: String? = "en_GB",
    val name: String? = null,
    val accountNumber: String? = null,
    val accountType: Int = 0,
    val transferLimit: Int = 0,
    val officeName: String? = null,
) {
    constructor() : this(
        locale = null,
        name = null,
        accountNumber = null,
        accountType = 0,
        transferLimit = 0,
        officeName = null,
    )
}
