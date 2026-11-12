/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkMonitorConfig
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkMonitorProvider
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.ValidationStrategy

/**
 * Installs the app's network monitor with the browser's own online signal (`navigator.onLine`).
 *
 * The default strategy also confirms connectivity with an HTTP probe of google's `generate_204`.
 * From a web page that probe is a cross-origin request without CORS headers, so the browser blocks
 * it, the probe fails, and the monitor reports offline on every network — the app shows
 * "No internet connection" and never loads remote data.
 *
 * Must run before `initKoin()`: [NetworkMonitorProvider.install] is first-call-wins, and the Koin
 * graph installs the monitor with the default config on first use.
 */
internal fun installWebNetworkMonitor() {
    NetworkMonitorProvider.install(NetworkMonitorConfig { validationStrategy = ValidationStrategy.NativeOnly })
}
