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

import kotlinx.datetime.LocalDate

data class NewAccount(
    val clientId: Int,
    val productId: String? = null,
    val submittedOnDate: LocalDate? = null,
    val accountNo: String,
    val locale: String? = null,
    val dateFormat: String? = null,
)
