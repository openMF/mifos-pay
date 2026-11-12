/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.kyc.di

import kpt.feature.kyc.KYCDescriptionViewModel
import kpt.feature.kyc.KYCLevel1ViewModel
import kpt.feature.kyc.KYCLevel2ViewModel
import kpt.feature.kyc.KYCLevel3ViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
/** Koin module providing the KYC description and level 1 to 3 view models. */
val KYCModule = module {
    viewModelOf(::KYCDescriptionViewModel)
    viewModelOf(::KYCLevel1ViewModel)
    viewModelOf(::KYCLevel2ViewModel)
    viewModelOf(::KYCLevel3ViewModel)
}
