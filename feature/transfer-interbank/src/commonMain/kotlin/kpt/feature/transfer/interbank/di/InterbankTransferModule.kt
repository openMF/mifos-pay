/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.transfer.interbank.di

import kpt.feature.transfer.interbank.InterbankTransferViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
// from #1906 pr
// import kpt.feature.send.money.PayAnyoneViewModel
// import kpt.feature.send.money.PayeeDetailsViewModel
// import kpt.feature.send.money.ScannerModule
// import kpt.feature.send.money.SendMoneyOptionsViewModel
// import kpt.feature.send.money.SendMoneyViewModel

val interbankTransferModule = module {
    viewModelOf(::InterbankTransferViewModel)

    // from #1906 pr
//    includes(ScannerModule)
//    viewModelOf(::SendMoneyViewModel)
//    viewModelOf(::SendMoneyOptionsViewModel)
//    viewModelOf(::PayeeDetailsViewModel)
//    viewModelOf(::PayAnyoneViewModel)
}
