/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.send.money

import co.touchlab.kermit.Logger
import com.google.android.gms.common.moduleinstall.ModuleInstallClient
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import org.koin.core.module.Module

/**
 * [QrScanner] backed by the Google Code Scanner; installs the Play scanner module if missing.
 * @property scanner Google Play services barcode scanner client.
 * @property playModule Client used to check for and install the scanner module.
 */
class QrScannerImp(
    private val scanner: GmsBarcodeScanner,
    private val playModule: ModuleInstallClient,
) : QrScanner {
    init {
        playModule
            .areModulesAvailable(scanner)
            .addOnSuccessListener {
                if (!it.areModulesAvailable()) {
                    val newRequest = ModuleInstallRequest.newBuilder().addApi(scanner).build()
                    playModule.installModules(newRequest)
                }
            }.addOnFailureListener {
                Logger.d("Failed to install QRCodeScanner Module")
            }
    }

    override fun startScanning(): Flow<String?> {
        return callbackFlow {
            scanner.startScan()
                .addOnSuccessListener { barcode ->
                    launch {
                        val rawValue = barcode.rawValue
                        send(rawValue)
                    }
                }.addOnFailureListener { exception ->
                    launch {
                        send(null)
                    }
                }
            awaitClose { }
        }
    }
}

/** Android actual: provides [PlayScannerModule] as the QR scanner Koin module. */
actual val ScannerModule: Module
    get() = PlayScannerModule
