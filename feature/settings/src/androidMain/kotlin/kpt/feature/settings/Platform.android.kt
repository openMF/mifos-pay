/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.settings

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

/** Android actual of [getPlatform]. */
actual fun getPlatform(): Platform {
    return Platform.Android
}

/** Android actual; dynamic (Material You) color is supported only on Android 12 and above. */
@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
actual fun supportsDynamicTheming(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
}
