/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.main.config

import kpt.core.network.config.SupabaseCredentials as GeneratedSupabaseCredentials
import org.mifos.corebase.network.SupabaseCredentials as GenericSupabaseCredentials

/**
 * Implementation of SupabaseCredentials that bridges the generated
 * SupabaseCredentials object with the generic interface.
 *
 * The concrete credentials object is emitted by SupabaseConfigConventionPlugin into the
 * module-scoped package configured in build.gradle.kts (`supabaseConfig.packageName =
 * "kpt.core.network.config"`), so we read the generated `url`/`anonKey` from there rather
 * than assuming a same-package object. `isConfigured` is derived locally from those values.
 */
internal object SupabaseCredentialsImpl : GenericSupabaseCredentials {
    override val url: String get() = GeneratedSupabaseCredentials.url
    override val anonKey: String get() = GeneratedSupabaseCredentials.anonKey
    override val isConfigured: Boolean get() = url.isNotBlank() && anonKey.isNotBlank()
}
