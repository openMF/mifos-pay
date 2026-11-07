/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.send.money.di

import kpt.feature.send.money.BankTransferViewModel
import kpt.feature.send.money.PayAnyoneViewModel
import kpt.feature.send.money.PayeeDetailsViewModel
import kpt.feature.send.money.PaymentChatHistoryViewModel
import kpt.feature.send.money.PaymentDetailsViewModel
import kpt.feature.send.money.PaymentProcessingViewModel
import kpt.feature.send.money.PaymentSuccessViewModel
import kpt.feature.send.money.ScannerModule
import kpt.feature.send.money.SearchIfscViewModel
import kpt.feature.send.money.SendMoneyOptionsViewModel
import kpt.feature.send.money.SendMoneyViewModel
import kpt.feature.send.money.UpiPinViewModel
import kpt.feature.send.money.UpiTransactionHistoryViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
/** Koin module that registers the send-money view models and the platform [ScannerModule]. */
val SendMoneyModule = module {
    includes(ScannerModule)
    viewModelOf(::SendMoneyViewModel)
    viewModelOf(::SendMoneyOptionsViewModel)
    viewModelOf(::PayeeDetailsViewModel)
    viewModelOf(::BankTransferViewModel)
    viewModelOf(::SearchIfscViewModel)

    viewModelOf(::PayAnyoneViewModel)
    viewModelOf(::PaymentProcessingViewModel)
    viewModelOf(::PaymentSuccessViewModel)
    viewModelOf(::UpiTransactionHistoryViewModel)
    viewModelOf(::UpiPinViewModel)
    viewModelOf(::PaymentChatHistoryViewModel)
    viewModelOf(::PaymentDetailsViewModel)
}
