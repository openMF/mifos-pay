/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.beneficiary.di

import kpt.feature.beneficiary.addupdatebeneficiary.AddEditBeneficiaryViewModel
import kpt.feature.beneficiary.deletebeneficiary.DeleteBeneficiaryViewModel
import kpt.feature.beneficiary.list.BeneficiaryListViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
val BeneficiaryModule = module {
    viewModelOf(::AddEditBeneficiaryViewModel)
    viewModelOf(::BeneficiaryListViewModel)
    viewModelOf(::DeleteBeneficiaryViewModel)
}
