/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.wallet.account

/**
 * Store5 key for the SINGLE-ROW-PER-KEY `accountDetail` store — one
 * savings-account detail record per key.
 *
 * The [accountId] doubles as the primary key at the DAO layer
 * (`SavingAccountDetailDao.upsert(entity(id = accountId))`), so a fresh fetch for
 * account A never disturbs cached detail rows for account B.
 *
 * @property accountId Savings account id the cached data belongs to.
 */
data class AccountDetailKey(val accountId: Long)
