/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.wallet.beneficiary

/**
 * Store5 key for the LEDGER `beneficiary` store — one page of beneficiaries per
 * authenticated client.
 *
 * The [clientId] doubles as the page key at the DAO layer
 * (`BeneficiaryDao.replacePage(clientId, entities)`), so a logout-then-login as
 * a different client never disturbs the previous client's cached list.
 *
 * The Fineract `beneficiaryList()` endpoint does NOT take a `clientId` path or
 * query parameter (the list is scoped to the authenticated session on the
 * server); the key is used purely for cache scoping, and the mapper stamps
 * the [clientId] into each persisted row at write time.
 *
 * @property clientId Client id the cached data belongs to.
 */
data class BeneficiaryKey(val clientId: Long)
