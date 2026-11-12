/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.security.di

import kpt.core.base.crypto.FieldEncryptor
import kpt.core.base.crypto.SecureKeyProvider
import kpt.core.base.crypto.SecureRandom
import kpt.core.base.security.CertificatePinConfig
import org.koin.core.module.Module
import org.koin.dsl.module

/** `platformSecurityModule` on Web (JS/WasmJS). */
actual val platformSecurityModule: Module = module {
    single { SecureKeyProvider() }
    single { FieldEncryptor() }
    single { SecureRandom() }
    single { CertificatePinConfig.default() }
}
