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
