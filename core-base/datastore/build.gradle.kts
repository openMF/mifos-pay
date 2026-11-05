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
    // Explicit for local-visibility; also applied by KMPCoreBaseLibraryConventionPlugin.
    // Applying twice is idempotent (Gradle no-ops the second apply via hasPlugin gate).
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core-base:common"))
            implementation(project(":core-base:crypto"))
            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.serialization)
            implementation(libs.multiplatform.settings.coroutines)
            // Explicit for local-visibility; also added by the core-base convention plugin.
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            api(libs.koin.core)
        }

        androidMain.dependencies {
            implementation(libs.androidx.security.crypto)
            implementation(libs.koin.android)
        }

        // localStorage for the web secure store (WebSecureStore). Declared on the shared jsCommon
        // source set because that is where the code lives; kotlinx-browser is in stdlib for js but
        // a separate artifact for wasmJs, so the shared set needs it explicitly.
        jsCommonMain.dependencies {
            implementation(libs.kotlinx.browser)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.multiplatform.settings.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

/*
 * Run the wasm node tests.
 *
 * The build convention disables `wasmJsNodeTest` for every non-Compose module because their wasm
 * bundles used to reference `skiko.mjs` without shipping it (ERR_MODULE_NOT_FOUND). This module no
 * longer has any path to skiko: its crypto primitives now come from the Compose-free
 * `core-base/crypto` instead of `core-base/security`, so the blanket exclusion no longer applies
 * here and `SecureStoreCoreTest` can run on wasm as well as desktop, iOS and JS.
 *
 * If a future dependency reintroduces a Compose/skiko edge, this task fails loudly rather than
 * silently losing the target - which is the point of enabling it explicitly.
 */
afterEvaluate {
    tasks.matching { it.name == "wasmJsNodeTest" }.configureEach { enabled = true }
}
