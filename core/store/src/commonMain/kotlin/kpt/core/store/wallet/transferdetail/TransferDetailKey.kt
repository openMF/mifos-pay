/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.wallet.transferdetail

/**
 * Store5 key for the SINGLE-ROW-PER-KEY `transferDetail` store — one
 * account-transfer detail record per key. Mirrors `AccountDetailKey`.
 *
 * The [transferId] doubles as the primary key at the DAO layer
 * (`TransferDetailDao.upsert(entity(transferId = transferId))`), so a fresh fetch
 * for transfer A never disturbs cached detail rows for transfer B.
 *
 * @property transferId Account transfer id the cached detail belongs to.
 */
data class TransferDetailKey(val transferId: Long)
