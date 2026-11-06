/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.history.di

import com.mobilebytelabs.kmptoolkit.pdfgenerator.ExperimentalPdfGeneratorApi
import kpt.feature.history.HistoryViewModel
import kpt.feature.history.detail.TransactionDetailViewModel
import kpt.feature.history.transactions.SpecificTransactionsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
@OptIn(ExperimentalPdfGeneratorApi::class)
val HistoryModule = module {
    includes(HistoryPdfModule)
    viewModelOf(::HistoryViewModel)
    viewModelOf(::SpecificTransactionsViewModel)
    viewModelOf(::TransactionDetailViewModel)
}
