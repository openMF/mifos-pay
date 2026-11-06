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

import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val PlayScannerModule = module {

    single {
        GmsBarcodeScanning.getClient(androidContext())
    }

    single {
        ModuleInstall.getClient(androidContext())
    }

    single<QrScanner> {
        QrScannerImp(
            scanner = get(),
            playModule = get(),
        )
    }
}
