/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifospay.feature.settings.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.mifospay.feature.settings.SettingsViewModel

/**
 * Koin qualifier for this feature's [SettingsViewModel]. On JS / Wasm Koin keys a binding by the
 * type's simple name, and the template's `kpt.feature.settings.SettingsViewModel` is bound too.
 */
val MifosSettingsViewModelQualifier = named("org.mifospay.feature.settings.SettingsViewModel")

val SettingsModule = module {
    viewModelOf(::SettingsViewModel) { qualifier = MifosSettingsViewModelQualifier }
}
