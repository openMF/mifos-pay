/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.network.entity.pocket

import kotlinx.serialization.Serializable

/**
 * Accounts linked to a pocket, grouped by loan, savings and share accounts.
 *
 * @property loanAccounts Loan accounts.
 * @property savingsAccounts Savings accounts of the client.
 * @property shareAccounts Share accounts.
 */
@Serializable
data class PocketResponseDto(
    val loanAccounts: List<PocketAccountDto> = emptyList(),
    val savingsAccounts: List<PocketAccountDto> = emptyList(),
    val shareAccounts: List<PocketAccountDto> = emptyList(),
)

/**
 * An account linked to a pocket.
 *
 * @property pocketId Id of the pocket.
 * @property accountId Id of the account.
 * @property accountType Type of the account.
 * @property accountNumber Account number.
 * @property id Unique id.
 */
@Serializable
data class PocketAccountDto(
    val pocketId: Long? = null,
    val accountId: Long? = null,
    val accountType: Int? = null,
    val accountNumber: String? = null,
    val id: Long? = null,
)
