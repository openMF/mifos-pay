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

/**
 * Platform detection for QR scanner UI behavior.
 */
expect object WebPlatform {
    /**
     * Returns true if running on web platform (JS or WasmJS).
     * Used to determine whether to show camera scanner UI or file picker UI.
     */
    val isWeb: Boolean

    /**
     * Returns true if running on web platform on a desktop browser.
     * Desktop browsers should show file picker only, not camera scanner.
     */
    val isWebDesktop: Boolean
}
