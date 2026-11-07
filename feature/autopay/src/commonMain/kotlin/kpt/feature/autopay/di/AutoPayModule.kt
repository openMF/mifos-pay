/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.autopay.di

import kpt.feature.autopay.AddBillViewModel
import kpt.feature.autopay.AddBillerViewModel
import kpt.feature.autopay.AutoPayHistoryViewModel
import kpt.feature.autopay.AutoPayPreferencesViewModel
import kpt.feature.autopay.AutoPayScheduleDetailsViewModel
import kpt.feature.autopay.AutoPayScheduleManagementViewModel
import kpt.feature.autopay.AutoPayViewModel
import kpt.feature.autopay.BillListViewModel
import kpt.feature.autopay.BillerListViewModel
import kpt.feature.autopay.EditBillViewModel
import kpt.feature.autopay.EditBillerViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
/** Koin module that registers the AutoPay feature view models. */
val AutoPayModule = module {
    viewModelOf(::AutoPayViewModel)
    viewModelOf(::AutoPayScheduleDetailsViewModel)
    viewModelOf(::AutoPayHistoryViewModel)
    viewModelOf(::AddBillerViewModel)
    viewModelOf(::BillerListViewModel)
    viewModelOf(::EditBillerViewModel)
    viewModelOf(::AddBillViewModel)
    viewModelOf(::EditBillViewModel)
    viewModelOf(::BillListViewModel)
    viewModelOf(::AutoPayPreferencesViewModel)
    viewModelOf(::AutoPayScheduleManagementViewModel)
    viewModelOf(::AutoPayHistoryViewModel)
}
