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

import org.koin.core.scope.Scope
import kotlin.experimental.ExperimentalNativeApi

/** Whether this Kotlin/Native binary was compiled as debug (the Xcode Debug configuration). */
@OptIn(ExperimentalNativeApi::class)
actual fun Scope.isDebugBuild(): Boolean = Platform.isDebugBinary
