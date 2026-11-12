/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.transfer.intrabank.di

import kpt.feature.transfer.intrabank.confirm.TransferConfirmViewModel
import kpt.feature.transfer.intrabank.hub.IntraBankHubViewModel
import kpt.feature.transfer.intrabank.selectScreen.SelectScreenViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
/** Koin module providing the intra-bank transfer view models. */
val IntraBankModule = module {
    viewModelOf(::IntraBankHubViewModel)
    viewModelOf(::SelectScreenViewModel)
    viewModelOf(::TransferConfirmViewModel)
}
