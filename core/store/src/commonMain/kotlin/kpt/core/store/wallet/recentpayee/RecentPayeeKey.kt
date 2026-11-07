/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.wallet.recentpayee

/**
 * Store5 key for the LOCAL-DERIVED `recentPayee` store — one page of
 * derived recent-payees per source savings-account.
 *
 * The [sourceAccountId] doubles as the page key at the DAO layer
 * (`RecentPayeeDao.replacePage(sourceAccountId, entities)`), so recomputing
 * the recent-payee walk for the same source account is idempotent (delete-all
 * + upsert-all under one transaction). A user with multiple own accounts sees
 * a separate cache slot per source account — matching the pre-store
 * `getRecentPayees(accountId, limit)` invocation shape.
 *
 * ### Not part of the key: `limit`
 *
 * The `limit` argument on `RecentPayeeRepository.getRecentPayees` /
 * `.getRecentPayeesScreen` is a CLIENT-SIDE display cap, not a partition
 * criterion. Applying it inside the cache key would fragment the cache into
 * N-per-limit-value entries (10, 20, 50, …); instead the store always caches
 * the full derived list and the read path applies `.take(limit)` after the
 * DAO emission. This keeps the derive walk O(1) per source-account regardless
 * of how many screens ask for different limits.
 *
 * @property sourceAccountId Source (own) account id the cached payees belong to.
 */
data class RecentPayeeKey(val sourceAccountId: Long)
