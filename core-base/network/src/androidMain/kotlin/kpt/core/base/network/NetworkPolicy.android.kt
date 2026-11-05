/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.network

import android.content.Context
import android.content.pm.ApplicationInfo
import org.koin.core.scope.Scope

/** The app's own `android:debuggable` flag — set by the debug build type, absent from release. */
actual fun Scope.isDebugBuild(): Boolean =
    getOrNull<Context>()?.let { (it.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0 } ?: false
