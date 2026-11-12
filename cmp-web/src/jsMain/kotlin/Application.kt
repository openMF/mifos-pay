/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import cmp.shared.SharedApp
import cmp.shared.utils.initKoin
import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kpt.core.base.datastore.SecureSettingsFactory
import org.jetbrains.skiko.wasm.onWasmReady

/*
 * The entry point of the WebAssembly Compose application.
 *
 * 1. Initializes the Koin dependency injection framework to set up dependencies.
 * 2. Waits for the WebAssembly environment to be ready using `onWasmReady`.
 * 3. Creates a Compose viewport linked to the document body, where the UI is rendered.
 * 4. Invokes the `SharedApp` composable, which serves as the root of the app's UI.
 */
/*
 * Web secure storage (passcode, auth state) is AES-GCM encrypted under a NON-EXTRACTABLE WebCrypto
 * key, and decrypting it is asynchronous. Koin builds the `secure` Settings from that store, so the
 * warm-up must COMPLETE before initKoin() — otherwise the first read races the key load and the
 * factory throws. See kpt.core.base.datastore.WebSecureStore.
 */
/** Browser entry point: warms the WebCrypto secure store, then starts Koin and mounts the Compose app. */
fun main() {
    MainScope().launch {
        SecureSettingsFactory.warmUp()
        startApp()
    }
}

@OptIn(ExperimentalComposeUiApi::class)
private fun startApp() {
    installWebNetworkMonitor()
    initKoin() // Set up Koin for dependency injection.

    // Apply stored language preference on startup
    val storedLanguage = localStorage.getItem("app_language")
    if (storedLanguage != null) {
        document.documentElement?.setAttribute("lang", storedLanguage)
    }

    onWasmReady {
        ComposeViewport(document.body!!) {
            // State to trigger recomposition when locale changes
            var localeVersion by remember { mutableStateOf(0) }

            // Use key() to force complete recomposition when locale changes
            key(localeVersion) {
                SharedApp(
                    updateScreenCapture = {},
                    handleRecreate = {
                        // Reload the page to apply locale changes
                        window.location.reload()
                    },
                    handleThemeMode = {},
                    handleAppLocale = { languageTag ->
                        if (languageTag != null) {
                            // Store language preference in localStorage
                            localStorage.setItem("app_language", languageTag)
                            // Set HTML lang attribute for accessibility
                            document.documentElement?.setAttribute("lang", languageTag)
                        } else {
                            // System Default: remove stored language preference
                            localStorage.removeItem("app_language")
                            // Reset to browser's default language
                            val browserLang = window.navigator.language
                            document.documentElement?.setAttribute("lang", browserLang)
                        }
                        // Reload page to apply language changes (required for web)
                        // Note: This will reload the page, and locale selection depends on browser settings
                        // window.location.reload()
                    },
                    onSplashScreenRemoved = {},
                )
            }
        }
    }
}
