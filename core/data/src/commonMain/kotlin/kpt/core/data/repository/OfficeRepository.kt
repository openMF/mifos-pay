/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.repository

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.common.ScreenStateStream
import kpt.core.model.office.Office
interface OfficeRepository {
    // Phase-3 cutover — reads on ScreenState.
    fun getOffices(): ScreenStateStream<List<Office>>

    /**
     * Phase-5 Batch-3 LEDGER read — GOAL D13 (`createStore` + CACHE_FIRST_SWR).
     *
     * Store-backed alternative to [getOffices] — reads through the
     * `AppStoreRegistry.Offices` Store5 (offline-first via `wallet_offices`
     * Room SoT, SWR revalidation once the TTL elapses).
     *
     * The office list is global reference data (not client-scoped), so the
     * store uses a value-less singleton key (`OfficesKey`) and there is exactly
     * one cache slot per app install. See `OfficesKey` KDoc for the rationale.
     *
     * @param scope the caller's [CoroutineScope] (typically `viewModelScope`)
     *   — Store5 subscribes its internal refresh trigger to this scope.
     */
    fun getOfficesStream(scope: CoroutineScope): ScreenDataStream<List<Office>>
}
