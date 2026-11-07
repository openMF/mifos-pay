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

/** Desktop actual of [WebPlatform]; never a web environment. */
actual object WebPlatform {
    /** Always false on desktop. */
    actual val isWeb: Boolean = false

    /** Always false on desktop. */
    actual val isWebDesktop: Boolean = false
}
