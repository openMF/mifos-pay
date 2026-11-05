/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
plugins {
    alias(libs.plugins.kmp.core.base.library.convention)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.ktor.client.core)
            api(libs.ktor.client.logging)
            api(libs.ktor.client.content.negotiation)
            api(libs.ktor.serialization.kotlinx.json)
            api(libs.ktor.client.auth)
            api(libs.ktorfit.lib)
            api(libs.kermit.logging)
            // restApi<T> DSL registers Koin singles + resolves the AccessPointRegistry via get().
            api(libs.koin.core)
            implementation(projects.coreBase.security)
            // NetworkConfigRepository persists runtime network-config changes in the app's plain settings.
            implementation(libs.multiplatform.settings)
            api(libs.supabase.postgrest)
            // api-exported (not just postgrest) so any fork whose backend is Supabase-with-auth can
            // install(Auth) + reference client.auth (RLS-gated RPCs needing a real auth.uid(),
            // OAuth/email sign-in, anonymous guest sessions) WITHOUT adding the dep to a template-owned
            // build file that a sync would clobber. The supabase-auth catalog alias already ships.
            api(libs.supabase.auth)
        }

        androidMain.dependencies {
            api(libs.ktor.client.okhttp)
            api(libs.koin.android)
        }

        nativeMain.dependencies {
            api(libs.ktor.client.darwin)
        }

        desktopMain.dependencies {
            api(libs.ktor.client.okhttp)
        }

        jsMain.dependencies {
            api(libs.ktor.client.js)
        }

        wasmJsMain.dependencies {
            api(libs.ktor.client.js)
        }
    }
}
