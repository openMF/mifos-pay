/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.wallet.history

/**
 * Store5 key for the LEDGER `history` store — one page of transactions per
 * savings account.
 *
 * The [accountId] doubles as the page key at the DAO layer
 * (`TransactionDao.replacePage(accountId, entities)`), so a fresh fetch for
 * account A never disturbs cached rows for account B.
 *
 * @property accountId Savings account id the cached data belongs to.
 */
data class TransactionKey(val accountId: Long)
