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

import kpt.core.common.ScreenStateStream
import kpt.core.model.network.entity.TPTResponse
import kpt.core.model.network.entity.payload.TransferPayload
import kpt.core.model.network.entity.templates.account.AccountOptionsTemplate
interface ThirdPartyTransferRepository {
    suspend fun getTransferTemplate(): AccountOptionsTemplate

    // Phase-3 cutover — Flow-shaped submission surface exposes ScreenState
    // so screens can consume Loading / Content / Error uniformly.
    // Underlying network call remains a POST (a semantic write) but the
    // read-facing envelope is aligned with the rest of the fork.
    fun makeTransfer(payload: TransferPayload): ScreenStateStream<TPTResponse>
}
