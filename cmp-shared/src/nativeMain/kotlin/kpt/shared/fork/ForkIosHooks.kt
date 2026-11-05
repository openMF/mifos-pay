/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.shared.fork

/**
 * The fork's iOS startup hook — FORK-OWNED (customization-surface.yaml). A template sync seeds this file
 * once and never touches it again.
 *
 * `viewController(platformBridges:)` calls this right after Koin starts, with whatever objects the Swift
 * app passed (`ContentView.swift`). Swift-implemented Kotlin interfaces — a StoreKit 2 shim, a native
 * ads bridge — can only be created in Swift, so this is where a fork picks them out and loads the Koin
 * modules that use them, e.g.
 *
 *     filterIsInstance<StoreKit2Bridge>().firstOrNull()
 *         ?.let { loadKoinModules(paycraftStoreKit2BillingModule(it)) }
 *
 * The template passes no bridges and does nothing.
 */
fun List<Any>.onForkIosConfigure() = Unit
