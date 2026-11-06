/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.mpay.qr.di

import kpt.core.common.MifosDispatchers
import kpt.feature.mpay.qr.MpayQrViewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
val MpayQrModule = module {
    factory {
        MpayQrViewModel(
            localRepository = get(),
            repository = get(),
            accountRepository = get(),
            pocketRepository = get(),
            savedStateHandle = get(),
            ioDispatcher = get(named(MifosDispatchers.IO.name)),
        )
    }
}
