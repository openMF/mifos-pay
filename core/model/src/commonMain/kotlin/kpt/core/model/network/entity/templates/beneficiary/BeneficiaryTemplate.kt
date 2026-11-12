/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.network.entity.templates.beneficiary

import kotlinx.serialization.Serializable

/**
 * Fineract beneficiary template listing the allowed account types.
 *
 * @property accountTypeOptions Allowed account types.
 */
@Serializable
data class BeneficiaryTemplate(
    val accountTypeOptions: List<AccountTypeOption?>? = null,
)
