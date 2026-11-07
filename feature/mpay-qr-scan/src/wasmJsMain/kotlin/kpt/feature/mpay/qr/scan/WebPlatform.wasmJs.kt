/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.mpay.qr.scan

import kotlinx.browser.window

/** Wasm/JS actual of [WebPlatform]. */
actual object WebPlatform {
    /** Always true on Wasm/JS. */
    actual val isWeb: Boolean = true

    /** True when the browser is not a mobile browser, judged from the user agent. */
    actual val isWebDesktop: Boolean by lazy {
        !isMobileBrowser()
    }

    private fun isMobileBrowser(): Boolean {
        val userAgent = window.navigator.userAgent.lowercase()

        // Check for mobile keywords in user agent
        val mobileKeywords = listOf(
            "android",
            "webos",
            "iphone",
            "ipad",
            "ipod",
            "blackberry",
            "windows phone",
            "mobile",
        )

        return mobileKeywords.any { userAgent.contains(it) }
    }
}
