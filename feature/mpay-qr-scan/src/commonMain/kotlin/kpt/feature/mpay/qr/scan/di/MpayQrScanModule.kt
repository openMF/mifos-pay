/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.mpay.qr.scan.di

import kpt.feature.mpay.qr.scan.ScanQrViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
// from send money pr
// import kpt.feature.send.money.PayeeDetailsViewModel
// import kpt.feature.send.money.PaymentChatHistoryViewModel
// import kpt.feature.send.money.PaymentDetailsViewModel
// import kpt.feature.send.money.PaymentProcessingViewModel
// import kpt.feature.send.money.PaymentSuccessViewModel
// import kpt.feature.send.money.ScannerModule
// import kpt.feature.send.money.SendMoneyOptionsViewModel
// import kpt.feature.send.money.SendMoneyViewModel
// import kpt.feature.send.money.UpiPinViewModel
// import kpt.feature.send.money.UpiTransactionHistoryViewModel

val MpayQrScanModule = module {
    viewModelOf(::ScanQrViewModel)

    // from send money pr
//    viewModelOf(::SendMoneyViewModel)
//    viewModelOf(::SendMoneyOptionsViewModel)
//    viewModelOf(::PayeeDetailsViewModel)
//    viewModelOf(::PaymentProcessingViewModel)
//    viewModelOf(::PaymentSuccessViewModel)
//    viewModelOf(::UpiPinViewModel)
//    viewModelOf(::PaymentChatHistoryViewModel)
//    viewModelOf(::PaymentDetailsViewModel)
//    viewModelOf(::UpiTransactionHistoryViewModel)
}
