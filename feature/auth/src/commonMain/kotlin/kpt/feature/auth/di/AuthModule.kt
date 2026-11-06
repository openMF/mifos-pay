/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.auth.di

import kpt.feature.auth.login.LoginViewModel
import kpt.feature.auth.mobileVerify.MobileVerificationViewModel
import kpt.feature.auth.signup.SignupViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
val AuthModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::SignupViewModel)
    viewModelOf(::MobileVerificationViewModel)
}
