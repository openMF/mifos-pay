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

import kpt.core.model.savingsaccount.Transaction
/**
 * Pairs a list of accounts with the transactions that belong to them.
 *
 * @property accounts The accounts.
 * @property transactions The transactions across those accounts.
 */
data class AccountsWithTransactions(
    val accounts: List<Account>,
    val transactions: List<Transaction>,
)
