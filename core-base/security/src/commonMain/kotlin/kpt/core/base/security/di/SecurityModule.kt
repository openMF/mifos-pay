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

import kpt.core.base.security.BiometricAuthenticator
import kpt.core.base.security.DeepLinkValidator
import kpt.core.base.security.FailedAttemptTracker
import kpt.core.base.security.SecureAuthManager
import kpt.core.base.security.SecureNavHandler
import kpt.core.base.security.SecureWiper
import kpt.core.base.security.SecurityConfig
import kpt.core.base.security.SecurityPolicy
import kpt.core.base.security.SessionManager
import kpt.core.base.security.TamperDetector
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Zero-config security Koin module. Auto-detects build type via
 * platform-specific [kpt.core.base.security.isReleaseBuild] and
 * registers all security components with sensible defaults.
 *
 * Consumer apps do NOT need to call this — it is auto-included in
 * [cmp.navigation.di.KoinModules.allModules].
 */
val SecurityModule = module {
    includes(platformSecurityModule)

    single { SecurityConfig() }
    single { SecurityPolicy.default() }
    single { TamperDetector() }
    single { SecureWiper() }
    single { BiometricAuthenticator() }
    single { FailedAttemptTracker(get(), get()) }
    single { SessionManager(get()) }
    single { DeepLinkValidator() }
    single { SecureNavHandler(get()) }
    single { SecureAuthManager(get(), get(), get()) }
}

/**
 * Per-target security bindings supplied by each `actual` — the keystore/keychain backing and the
 * platform biometric prompt.
 *
 * Separate from the common module because these are the members that genuinely cannot be expressed
 * in common code; everything else in `core-base/security` is shared.
 */
expect val platformSecurityModule: Module
