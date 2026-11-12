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
    alias(libs.plugins.kmp.library.convention)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    sourceSets {
        androidInstrumentedTest.dependencies {
            implementation(libs.androidx.compose.ui.test)
        }
        androidUnitTest.dependencies {
            implementation(libs.androidx.compose.ui.test)
        }
        commonMain.dependencies {
            api(projects.coreBase.designsystem)
            // `api`, and deliberately from here: core:platform api-exposes core-base:platform, and
            // every feature module already receives core:designsystem from CMPFeatureConventionPlugin.
            // Routing it through this module means a feature can read the platform CompositionLocals
            // — LocalShareManager, LocalUrlLauncher, LocalClipboardManager, LocalPdfManager and the
            // rest — without declaring anything, and without adding another line to the convention
            // plugin for every capability the toolkit grows.
            //
            // Without this the capabilities are bound in platformModule but unreachable from where
            // forks actually write screens: 0 of 17 feature modules could see them.
            api(projects.core.platform)
            // Theme wires LocalScreenStateDefaults from core/store so every screen
            // wrapped by KptTheme picks up the app's branded ScreenState defaults.
            implementation(projects.core.store)

            implementation(compose.ui)
            implementation(compose.uiUtil)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(libs.coil.kt.compose)
        }
    }
}

compose.resources {
    publicResClass = true
    generateResClass = always
    packageOfResClass = "kpt.core.designsystem.generated.resources"
}
// ── Fork-owned dependency seam (white-label, mirrors `feature-deps.gradle.kts`) ────────────────
// A fork adds its OWN dependencies for this module in `core/designsystem/module-deps.gradle.kts` — never in
// this file. That is what lets THIS build file be `owner: template` and FULL-COPY on a template
// sync: the fork's deps live in a file the sync never touches, so a template plugin/version bump
// can no longer drop them and no 3-way merge is needed.
//
// String `"commonMainImplementation"(...)` notation is used in the seam, not the type-safe
// `libs.`/`projects.` accessors: those are NOT generated for `apply(from = ...)` script plugins.
//
// Guarded like feature-deps: a fork that adopted the template BEFORE this seam existed may not have
// the file yet, and an unconditional apply would fail the whole configuration.
project.file("module-deps.gradle.kts").takeIf { it.exists() }?.let { apply(from = it) }
