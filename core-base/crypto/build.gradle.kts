/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */

/*
 * Cryptographic primitives, deliberately free of Compose.
 *
 * These used to live in `core-base/security`, which applies the Compose plugin for its two UI
 * pieces (SecurityGate, SecurityState). That made every consumer of a crypto primitive — including
 * `core-base/datastore` and `core/database`, which have no UI — drag in the whole Compose graph:
 *
 *     core-base:datastore -> core-base:security -> compose.foundation
 *                         -> animation -> ui -> ui-graphics -> skiko
 *
 * Besides bloating web bundles, that is why the build convention had to disable three of the four
 * web test tasks for every non-Compose module: their wasm bundles referenced `skiko.mjs` and never
 * shipped it, so `wasmJsNodeTest` failed with ERR_MODULE_NOT_FOUND. Splitting the primitives out
 * removes the edge rather than working around its symptom.
 *
 * Keep this module Compose-free. Anything needing @Composable belongs in `core-base/security`.
 */
plugins {
    alias(libs.plugins.kmp.core.base.library.convention)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core-base:common"))
            implementation(libs.kotlinx.coroutines.core)
        }

        androidMain.dependencies {
            implementation(libs.androidx.security.crypto)
        }

        desktopMain.dependencies {
            implementation(libs.bouncycastle)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

/*
 * Route this module's web tests to a real browser.
 *
 * The build convention picks the web test environment by asking "does this module apply Compose?",
 * using that as a proxy for "does it need a DOM". This module needs a browser for a DIFFERENT
 * reason: `WebSecureCrypto` calls WebCrypto (`crypto.subtle`) and persists its key in IndexedDB,
 * and Node provides neither — under the default routing `WebSecureCryptoTest` fails 9 of 10.
 *
 * Testing the real thing is the entire point of that suite (a fake cipher would prove nothing about
 * whether the key is genuinely non-extractable), so the browser tasks are turned on and the node
 * ones off. The platform-agnostic store logic that CAN run anywhere lives in `core-base/datastore`
 * and is tested on desktop, iOS, JS and wasm.
 */
afterEvaluate {
    val nodeWebTests = setOf("jsNodeTest", "wasmJsNodeTest")
    val browserWebTests = setOf("jsBrowserTest", "wasmJsBrowserTest")
    tasks.configureEach {
        when (name) {
            in nodeWebTests -> enabled = false
            in browserWebTests -> enabled = true
        }
    }
}
