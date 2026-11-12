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

/**
 * Data needed to open a new savings account for a client.
 *
 * @property clientId Id of the client the account is opened for.
 * @property productId Id of the savings product to open.
 * @property submittedOnDate Date the application is submitted.
 * @property accountNo Account number to assign.
 * @property locale Locale used to parse values in the request.
 * @property dateFormat Format pattern used for date fields in the request.
 */
data class NewAccount(
    val clientId: Int,
    val productId: String? = null,
    val submittedOnDate: LocalDate? = null,
    val accountNo: String,
    val locale: String? = null,
    val dateFormat: String? = null,
)
