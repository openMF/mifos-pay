/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.fastmpay.di

import kpt.feature.fastmpay.FastMpayProcessor
import kpt.feature.fastmpay.FastMpayViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
/** Koin module providing the Fast Mpay QR processor and ViewModel. */
val FastMpayModule = module {
    factory {
        // Phase-5 Batch-3: BeneficiaryRepository → SelfServiceRepository so the
        // processor's beneficiary read walks the batch-1 store-backed
        // getBeneficiaryListScreen(...) reader (offline-first via
        // wallet_beneficiaries Room SoT). No new BeneficiaryStore is created
        // for the fast-mpay surface — the batch-1 store is REUSED.
        FastMpayProcessor(
            selfServiceRepository = get(),
            userPreferencesRepository = get(),
            officeRepository = get(),
        )
    }
    viewModelOf(::FastMpayViewModel)
}
