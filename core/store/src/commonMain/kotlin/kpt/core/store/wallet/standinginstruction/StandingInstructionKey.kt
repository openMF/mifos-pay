/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.wallet.standinginstruction

/**
 * Store5 key for the LEDGER `standingInstruction` store — one page of standing
 * instructions per client.
 *
 * The [clientId] doubles as the page key at the DAO layer
 * (`StandingInstructionDao.replacePage(clientId, entities)`) AND as the API
 * query parameter (`GET /standinginstructions?clientId=<clientId>`), so
 * per-client scoping is enforced end-to-end.
 *
 * @property clientId Client id the cached data belongs to.
 */
data class StandingInstructionKey(val clientId: Long)
