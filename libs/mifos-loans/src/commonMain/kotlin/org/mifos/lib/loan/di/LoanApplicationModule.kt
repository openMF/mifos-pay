/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.lib.loan.di

import kpt.core.common.MifosDispatchers
import kpt.core.data.util.MifosNetworkMonitorQualifier
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.mifos.lib.loan.network.di.LoansNetworkModule
import org.mifos.lib.loan.providers.DummyLoanProvider
import org.mifos.lib.loan.providers.FineractLoanProvider
import org.mifos.lib.loan.providers.LoanProviderRegistry
import org.mifos.lib.loan.repository.LoansRepository
import org.mifos.lib.loan.repository.LoansRepositoryImpl
import org.mifos.lib.loan.ui.confirmDetails.ConfirmDetailsViewModel
import org.mifos.lib.loan.ui.loanApply.LoanApplyViewModel
import org.mifos.lib.loan.ui.loanProductDetails.LoanProductDetailsViewModel
import org.mifos.lib.loan.ui.providerWebView.LoanProviderWebViewViewModel
import org.mifos.lib.loan.ui.selectLoanProvider.SelectLoanProviderViewModel
import org.mifos.lib.loan.ui.selectLoanType.SelectLoanTypeViewModel
import org.mifos.lib.loan.ui.uploadDocs.UploadDocsViewModel
/**
 * Koin module for the `libs/mifos-loans` loan-application wizard.
 */
val LoanApplicationModule = module {
    includes(LoansNetworkModule)

    // The loan-application flow has no real per-provider backend yet — the product owner asked
    // for a dummy provider-selection step ahead of the (also-dummied-for-now) product flow. All
    // four ids offered by the Select Loan Provider screen ("hdfc", "sbi", "icici", "axis") resolve
    // to the SAME shared [DummyLoanProvider] instance below, keeping today's "all providers behave
    // identically" behaviour while making the resolution mechanism (LoanProviderRegistry) real.
    // The real Fineract-backed [FineractLoanProvider] is registered under "fineract" for future
    // activation — swap any of the four entries below for a distinct adapter once one exists.
    single<DummyLoanProvider> { DummyLoanProvider() }
    single<FineractLoanProvider> {
        FineractLoanProvider(get(), get(named(MifosDispatchers.IO.name)))
    }
    single<LoanProviderRegistry> {
        val dummy = get<DummyLoanProvider>()
        LoanProviderRegistry(
            providersById = mapOf(
                "hdfc" to dummy,
                "sbi" to dummy,
                "icici" to dummy,
                "axis" to dummy,
                "fineract" to get<FineractLoanProvider>(),
            ),
            defaultProvider = dummy,
        )
    }
    single<LoansRepository> { LoansRepositoryImpl(get()) }

    viewModelOf(::SelectLoanProviderViewModel)
    viewModelOf(::LoanProviderWebViewViewModel)
    viewModelOf(::SelectLoanTypeViewModel)
    viewModelOf(::LoanProductDetailsViewModel)
    viewModel {
        LoanApplyViewModel(
            savedStateHandle = get(),
            loansRepository = get(),
            selfServiceRepository = get(),
            userPreferencesRepository = get(),
            networkMonitor = get(MifosNetworkMonitorQualifier),
        )
    }
    viewModelOf(::UploadDocsViewModel)
    viewModelOf(::ConfirmDetailsViewModel)
}
