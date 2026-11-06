/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */

plugins {
    alias(libs.plugins.kmp.library.convention)
    alias(libs.plugins.cmp.feature.convention)
    alias(libs.plugins.kmp.koin.convention)
    id("org.convention.feature.aggregate")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Core Modules
            implementation(projects.core.data)
            implementation(projects.core.database)
            implementation(projects.core.network) // E1: ForkFeatureRegistry wires the relocated DemoNetworkModule (core/network/demo/di)
            implementation(projects.core.model)
            implementation(projects.core.common)
            implementation(projects.core.datastore)
            // Firebase analytics (firebaseModule + AnalyticsHelper + Compose helpers) via core/firebase.
            implementation(projects.core.firebase)
            // core/platform re-exports core-base/platform (platformModule, GarbageCollectionManager) —
            // the app-shell reaches those through core/ per G-CORE-BASE-ENCAP.
            implementation(projects.core.platform)
            // core-base/security is the ONE sanctioned app-shell exception: cmp-navigation is the DI
            // aggregator (KoinModules wires SecurityModule) and reads isReleaseBuild; no core/ wrapper
            // is warranted for a security module the shell itself assembles. Feature modules NEVER
            // depend on core-base — enforced by the encapsulation gate (Phase A).
            implementation(projects.coreBase.security)
            // KptRootScaffold's snackbarHost slot defaults to KptToastHost, so the app-wide
            // transient-message surface exists without every screen wiring one.
            // Routed through core/designsystem, which re-exports core-base/designsystem with
            // api(...). An app shell must not reach core-base directly (G-CORE-BASE-ENCAP).
            implementation(projects.core.designsystem)

            // Backbone shell features (template-owned) — always present in every fork.
            implementation(projects.feature.home)
            implementation(projects.feature.profile)
            implementation(projects.feature.settings)
            // First-run onboarding, installed by RootNavScreen (shown only when first_run_onboarding is on).
            implementation(projects.feature.onboarding)
            // Fork feature-module deps come from the fork-owned `feature-deps.gradle.kts` seam
            // (applied at the bottom of this file, S7/F4). A fork adds a feature there, never here.
            implementation(projects.sync)

            // put your multiplatform dependencies here
            // Deep links (home-screen widgets + external app:// URIs) — cmp-deep-link (KmpToolkit,
            // commonMain). Android capture is auto-init (ContentProvider); RootNavScreen collects
            // DeepLinkHandler.lastReceived and dispatches through the fork-owned ForkDeepLinkRegistry seam.
            implementation(libs.cmp.deep.link)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.components.resources)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            // Phase 3 (store5-screen-state-persistence 03-vm-scoping) — enables
            // koinNavViewModel() so nav destinations acquire ViewModels scoped to
            // NavBackStackEntry (cleared on pop) instead of Activity (cleared on
            // Activity death). Resolves io.insert-koin:koin-compose-viewmodel-navigation
            // via gradle/libs.versions.toml:259; version is the shared Koin ref.
            implementation(libs.koin.compose.navigation)
            // Provides `com.russhwolf.settings.Settings` referenced by
            // `saveable/PersistentSaveableStateRegistry.kt` at the app root.
            // The `named("plain")` binding itself is contributed by
            // `core-base/datastore/DatastoreBaseModule` (transitively wired in
            // via `core/datastore/DatastoreModule` in `KoinModules.allModules`).
            implementation(libs.multiplatform.settings)
        }

        commonTest.dependencies {
            implementation(libs.kotlinx.serialization.core)
        }
    }
}

compose.resources {
    publicResClass = true
    generateResClass = always
    packageOfResClass = "cmp.navigation.generated.resources"
}

// Fork-owned feature-module dependencies (S7/F4 white-label seam). Applied AFTER the `kotlin { }` block
// above so the `commonMainImplementation` configuration it contributes to already exists. A fork edits
// `feature-deps.gradle.kts`, never this template-owned build file — a template sync full-copies this file.
//
// Apply ONLY when the seam file is present. `feature-deps.gradle.kts` is `owner: fork` (never synced), so a
// fork that adopted the template BEFORE this seam existed — or is mid-adoption — may not have it yet; an
// unconditional `apply(from = …)` then fails the whole configuration ("Could not read script …feature-deps
// .gradle.kts as it does not exist"), which blocks even `syncForkConfig`. Guarding the apply keeps
// cmp-navigation configurable in that window; the fork wires its features by creating the seam file.
rootProject.file("feature-deps.gradle.kts").takeIf { it.exists() }?.let { apply(from = it) }
