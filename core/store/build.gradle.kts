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
    alias(libs.plugins.kmp.koin.convention)
    alias(libs.plugins.ksp)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Foundation: state model + UI defaults the seam customizes.
            // `api` so consumers get LocalScreenStateDefaults, ScreenState*, etc.
            // by depending on `core/store` alone.
            api(projects.coreBase.store)
            api(projects.coreBase.ui)

            // Compose runtime — needed for the @Composable appScreenStateDefaults() factory.
            implementation(compose.runtime)
            // compose-resources — for stringResource()-based ScreenState copy (i18n).
            implementation(compose.components.resources)

            implementation(projects.core.database)
            implementation(projects.coreBase.database)
            implementation(projects.core.model)
            implementation(projects.core.network)
            implementation(libs.cmp.network.monitor)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kermit.logging)
        }
    }
}

compose.resources {
    publicResClass = true
    generateResClass = always
    packageOfResClass = "kpt.core.store.generated.resources"
}

// ── Fork-owned dependency seam (white-label, mirrors `feature-deps.gradle.kts`) ────────────────
// A fork adds its OWN dependencies for this module in `core/store/module-deps.gradle.kts` — never in
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

/*
 * SPIKE — KSP on the COMMON metadata compilation.
 *
 * The repo's only KSP precedent is Room, wired per-target (`kspDesktop`, `kspIosArm64`, …) because
 * Room must generate a platform driver for each. Store bindings are the opposite: ONE commonMain
 * file every target shares. That needs `kspCommonMainMetadata` plus adding its output to commonMain's
 * source set — and whether that combination works on Kotlin 2.4.0 / KSP 2.3.11 in this build is
 * exactly what this spike is testing, before 21 stores are migrated onto it.
 */
dependencies {
    add("kspCommonMainMetadata", project(":tools:store-ksp"))
}

kotlin.sourceSets.named("commonMain") {
    kotlin.srcDir(layout.buildDirectory.dir("generated/ksp/metadata/commonMain/kotlin"))
}

// Everything that READS commonMain must wait for the metadata pass. That is not just the compile
// tasks: putting the generated dir on commonMain's source set makes the PER-TARGET ksp tasks
// (kspKotlinDesktop, kspKotlinWasmJs, …) consume it as an input too, and Gradle fails the build on
// the undeclared dependency. Covering only `compileKotlin*` left those racing a half-written file.
val kspCommonMetadata = "kspCommonMainKotlinMetadata"
tasks.matching {
    (it.name.startsWith("compileKotlin") || it.name.startsWith("ksp")) && it.name != kspCommonMetadata
}.configureEach { dependsOn(kspCommonMetadata) }
