/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.domain.di

import kpt.core.common.MifosDispatchers
import kpt.core.domain.LoginUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module
/** Koin module providing the domain use cases. */
val DomainModule = module {
    single {
        LoginUseCase(
            repository = get(),
            clientRepository = get(),
            userPreferencesRepository = get(),
            ioDispatcher = get(named(MifosDispatchers.IO.name)),
        )
    }
}
