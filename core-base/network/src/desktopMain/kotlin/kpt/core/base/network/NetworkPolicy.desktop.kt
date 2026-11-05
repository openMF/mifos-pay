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

/**
 * A packaged desktop app (jpackage: DMG/MSI/DEB) sets `jpackage.app-path`; `./gradlew run` does not.
 * So "not packaged" is the development run.
 */
actual fun Scope.isDebugBuild(): Boolean = System.getProperty("jpackage.app-path") == null
