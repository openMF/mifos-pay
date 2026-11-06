/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */

import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kmp.library.convention)
    alias(libs.plugins.cmp.feature.convention)
    // SKIE — Swift-friendly export of the ComposeApp framework (sealed classes,
    // suspend → async/await, Flow → AsyncSequence, default arguments). Applies to
    // the KMP `binaries.framework { }` export below; the XCFramework the iOS app
    // consumes therefore ships the SKIE-enhanced Swift API. See 07-ios-swiftpm.md.
    alias(libs.plugins.skie)
    // worker-kmp v4.0.0 — applies @WorkerKmpApp/@WorkerKmpWorkers codegen pipeline.
    // KSP processor scans this module's commonMain for @WorkerKmpWorkers (see
    // cmp/shared/WorkerDeclarations.kt) + emits per-platform installWorkerKmp{Platform}
    // files + the commonMain WorkerKmpAuto.kt shim into build/generated/worker-kmp-app/.
    // No `version` since the plugin is already on the buildscript classpath via the
    // composite-build substitution from build-logic/convention — adding `version` would
    // cause Gradle's "plugin is already on the classpath with an unknown version" error.
    id("io.github.mobilebytelabs.worker-app")
}

// SKIE (Swift-ergonomic API bridging) is wired but DISABLED by default: the current SKIE release
// does not yet support Kotlin 2.4.0 (SKIE gates the compiler version and fails the whole build on a
// mismatch). Flip to `true` once a Kotlin-2.4.0-compatible SKIE version is pinned in libs.versions.toml.
skie {
    isEnabled = false
}

kotlin {
    // E6 — SwiftPM/XCFramework export.
    // Assemble a single `ComposeApp.xcframework` from the iOS device + simulator
    // slices; the iOS app consumes it via `cmp-ios/Package.swift` (SwiftPM binary
    // target) + the flavor-aware `cmp-ios/scripts/embed-xcframework.sh` Xcode
    // Run-Script build phase. The `assembleComposeApp{Debug,Release}XCFramework`
    // (and umbrella `assembleComposeAppXCFramework`) Gradle tasks are registered
    // automatically by this `XCFramework(...)` DSL, and the deploy lanes call them.
    val xcf = XCFramework("ComposeApp")
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            // KGP rejects debuggable=true + optimized=true on the same binary
            // (kotlin:kgp:misconfiguration:incompatible-binary-configuration).
            optimized = buildType == NativeBuildType.RELEASE
            xcf.add(this)
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Navigation Modules
            implementation(projects.cmpNavigation)
            implementation(compose.components.resources)
            implementation(projects.core.ui)
            // core/platform re-exports the core-base/platform surface the app-shell uses.
            implementation(projects.core.platform)

            implementation(libs.coil.kt.compose)

            // worker-kmp v4.0.0 — @WorkerKmpWorkers annotation site lives in this module
            // (cmp.shared.WorkerDeclarations.kt). Codegen runs in this module's build/
            // directory (consumer-side, not in published worker-kmp jars).
            implementation(libs.worker.app.annotations)
            // worker-kmp core — required DIRECTLY here (not just transitively via :sync) so the
            // @WorkerKmpWorkers KSP codegen can resolve the referenced workers' `CoroutineWorker`
            // supertype during commonMain metadata processing. KSP metadata resolution does not
            // traverse transitive klibs, so a transitive-only dep fails supertype resolution
            // ("must extend CoroutineWorker; found supertypes: ...").
            implementation(libs.worker.kmp)
            // The @WorkerKmpWorkers codegen emits `Generated_WorkerKmpInit.kt` per platform,
            // which references worker-koin (WorkerKmpHost / loadKoinModules / getKoin) + each
            // platform's WorkManager factory (desktopWorkManagerFactory, androidWorkManagerFactory,
            // …). The `worker-compose-all` umbrella supplies the koin integration + all platform
            // factories the generated code compiles against (same dep the worker-compose
            // convention adds to :sync).
            implementation(libs.worker.compose.all)
            // Worker classes referenced from the @WorkerKmpWorkers annotation live here.
            implementation(projects.sync)
            // DataSyncWorker's constructor params come from core:data (CurrencyRepository,
            // MacroIndicatorsRepository) + core:datastore (SyncStatePersister). The
            // @WorkerKmpWorkers KSP codegen reads every worker ctor param type for Koin
            // autowiring, and KSP metadata resolution needs them on the DIRECT classpath
            // (transitive-via-:sync klibs are not resolved) — else "could not resolve type
            // of constructor parameter ...".
            implementation(projects.core.data)
            implementation(projects.core.datastore)
        }

        desktopMain.dependencies {
            // Desktop specific dependencies
            implementation(compose.desktop.currentOs)
            implementation(compose.desktop.common)
        }
    }

    // NOTE — the flavor-aware `{flavor}{BuildType}` → Kotlin/Native build-type mapping
    // lives OUTSIDE Gradle, in the Xcode Run-Script build phase
    // `cmp-ios/scripts/embed-xcframework.sh`: it reads `$CONFIGURATION`
    // (`demoDebug` / `prodStaging` / `prodRelease` / …), maps a debuggable variant
    // → Debug and everything else → Release (identical semantics), then invokes the
    // matching `:cmp-shared:embedAndSignAppleFrameworkForXcode` with a canonical
    // Debug/Release `CONFIGURATION` so the KMP embed task selects the right slice.
}

compose.resources {
    publicResClass = true
    generateResClass = always
    packageOfResClass = "cmp.shared.generated.resources"
}

// ── XCFramework output hygiene ────────────────────────────────────────────────────────────────
// `xcodebuild -create-xcframework` REFUSES to write into a slice directory that already exists:
//     "ComposeApp.framework" couldn't be copied to "ios-arm64" because an item with the
//      same name already exists.        (xcodebuild exit 70 → task FAILED)
//
// Observed once on 2026-09-08 while bumping KmpToolkit, when the task re-ran (inputs changed)
// against an XCFramework left by a previous build. It did NOT reproduce afterwards — neither a
// plain re-run nor `--rerun` against a populated output dir failed — so the exact trigger is
// still unknown (most likely an output left behind by an interrupted assemble, which is a state
// the up-to-date check cannot see).
//
// Rather than leave that to chance, clear the slice directory before the task builds it. This is
// a `doFirst`, so it runs ONLY when the task actually executes — an UP-TO-DATE task keeps its
// output untouched and the incremental build stays fast (RULE-BUILD-WARMTH-001). The delete
// target is resolved as a Provider at configuration time, so it is configuration-cache safe.
run {
    val xcfRoot = layout.buildDirectory.dir("XCFrameworks")
    tasks.matching { it.name.matches(Regex("assembleComposeApp(Debug|Release)XCFramework")) }
        .configureEach {
            val slice = name.removePrefix("assembleComposeApp").removeSuffix("XCFramework").lowercase()
            val stale = xcfRoot.map { it.dir(slice).file("ComposeApp.xcframework") }
            doFirst {
                val f = stale.get().asFile
                if (f.exists()) {
                    logger.lifecycle("[xcframework] clearing stale output before -create-xcframework: $f")
                    f.deleteRecursively()
                }
            }
        }
}
