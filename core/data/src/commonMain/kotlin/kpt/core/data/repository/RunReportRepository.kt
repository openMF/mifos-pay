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
import kpt.core.model.savingsaccount.Transaction
interface RunReportRepository {
    // Phase-3 cutover — reads on ScreenState.
    suspend fun getTransactionReceipt(
        outputType: String,
        transactionId: String,
    ): ScreenStateStream<Transaction>
}
