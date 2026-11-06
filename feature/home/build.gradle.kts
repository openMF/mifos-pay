/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
plugins {
    alias(libs.plugins.cmp.feature.convention)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.common)
            implementation(projects.core.data)
            implementation(projects.core.model)
            implementation(projects.core.store)
            implementation(projects.core.ui)
            // Fork addition: kpt.feature.home.{HomeScreen,HomeViewModel} (the fork's
            // pre-existing Home screen, mounted wholesale via cmp-shared's MifosNavHost) read
            // the kpt.core.designsystem.{component,icon}.* design system surface.
            // kpt.core.datastore.UserPreferencesRepository comes transitively via
            // core/data's api(core.datastore) re-export.
            implementation(projects.core.designsystem)

            implementation(compose.ui)
            implementation(compose.material3)
            implementation(compose.foundation)
            implementation(compose.materialIconsExtended)
            // compose-resources — for stringResource()-based UI copy (i18n) per
            // RULE-IMPL-NO-HARDCODED-STRING-001 (W2 of store5-superbrain-v2).
            // Mirrors `feature/loans/build.gradle.kts` wiring.
            implementation(compose.components.resources)
            // Fork addition: HomeScreen's @Preview composables need the tooling-preview API.
            implementation(compose.components.uiToolingPreview)
            // Fork addition: HomeScreen uses androidx.compose.material3.windowsizeclass.* for
            // calculateWindowSizeClass()/WindowWidthSizeClass adaptive-layout branching.
            implementation(libs.window.size)

            // Phase 3 (store5-screen-state-persistence 03-vm-scoping) — koinNavViewModel()
            // for NavBackStackEntry-scoped VM acquisition on the HomeScreen bottom-nav-tab
            // VM. Screens import it aliased as `retainedKoinViewModel` per the sub-plan's
            // naming contract; enumeration Kdoc lives in
            // `cmp-navigation/.../RetainedKoinViewModel.kt`. See feature/loans deviation note.
            implementation(libs.koin.compose.navigation)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
    }
}

// Compose-resources class generator config — mirrors `feature/loans/build.gradle.kts`
// so `Res.string.*` is exposed under `kpt.feature.home.generated.resources`,
// matching the source `import kpt.feature.home.generated.resources.Res`
// inserted by RULE-IMPL-NO-HARDCODED-STRING-001 backfill.
compose.resources {
    publicResClass = true
    generateResClass = always
    packageOfResClass = "kpt.feature.home.generated.resources"
}
