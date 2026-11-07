/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.accounts.di

import kotlinx.serialization.json.Json
import kpt.feature.accounts.AccountViewModel
import kpt.feature.accounts.savingsaccount.AddEditSavingViewModel
import kpt.feature.accounts.savingsaccount.details.SavingAccountDetailViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
/** Koin module providing the accounts feature view models and its Json instance. */
val AccountsModule = module {
    single<Json> { Json { ignoreUnknownKeys = true } }
    viewModelOf(::AccountViewModel)
    viewModelOf(::SavingAccountDetailViewModel)
    viewModelOf(::AddEditSavingViewModel)
}
