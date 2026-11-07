/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.passcode

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Koin module exposing the `:feature:passcode` ViewModels. Included by the
 * top-level [cmp.navigation.registry.app.di.KoinModules] aggregator.
 *
 * Note: the `PasscodeManager` singleton itself is registered in
 * [cmp.navigation.registry.app.di.KoinModules.MifosPasscodeModule] (cmp-shared)
 * because it depends on `PasscodeStorageAdapter`, which lives in
 * `:core:data`'s [kpt.core.data.di.fork.RepositoryModule]. Keeping the
 * VM bindings here and the manager binding in cmp-shared avoids a circular
 * module dependency.
 */
val MifosAuthenticatorModule = module {
    viewModelOf(::BiometricSetupScreenViewmodel)
    viewModelOf(::MifosPasscodeViewModel)
}
