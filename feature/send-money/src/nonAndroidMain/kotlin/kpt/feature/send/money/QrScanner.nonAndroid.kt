/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.send.money

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.koin.core.module.Module
import org.koin.dsl.module

private object NoOpQrScanner : QrScanner {
    override fun startScanning(): Flow<String?> = emptyFlow()
}

/** Non-Android actual: provides a no-op [QrScanner] that never emits a scan result. */
actual val ScannerModule: Module
    get() = module {
        single<QrScanner> { NoOpQrScanner }
    }
