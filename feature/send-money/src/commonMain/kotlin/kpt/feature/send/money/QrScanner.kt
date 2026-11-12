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
import org.koin.core.module.Module

/** Platform-agnostic QR code scanner. */
interface QrScanner {
    /** Starts a scan and emits the raw scanned value, or null if scanning failed. */
    fun startScanning(): Flow<String?>
}

/** Platform-specific Koin module that provides the [QrScanner] implementation. */
expect val ScannerModule: Module
