/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.account

import kotlinx.serialization.Serializable
import kpt.core.model.beneficiary.Beneficiary
/**
 * Combined payload of the user's accounts and saved beneficiaries.
 *
 * @property accounts The user's accounts.
 * @property beneficiaries The user's saved beneficiaries.
 */
@Serializable
data class AccountContent(
    val accounts: List<Account>,
    val beneficiaries: List<Beneficiary>,
)
