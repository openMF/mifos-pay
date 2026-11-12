/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.convention

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Configure the Kotlin Multiplatform plugin with the default hierarchy template and additional targets.
 * This includes JVM, iOS, JS and WASM targets.
 *
 * NOTE: androidTarget() is intentionally absent. com.android.kotlin.multiplatform.library (AGP 9+)
 * registers the Android target automatically via its withPlugin("kotlin.multiplatform") callback.
 * Calling androidTarget() here would create a duplicate 'android' target conflict.
 *
 * @see KotlinMultiplatformExtension
 * @see configure
 */
@OptIn(ExperimentalWasmDsl::class, ExperimentalKotlinGradlePluginApi::class)
internal fun Project.configureKotlinMultiplatform() {
    val jvmToolchainVersion = libs.findVersion("jvmToolchain").get().requiredVersion.toInt()

    extensions.configure<KotlinMultiplatformExtension> {
        applyProjectHierarchyTemplate()

        // Gradle JVM toolchain — selects the JDK used to compile + run tests.
        // Source/target compatibility stays at javaVersion (KotlinAndroid.kt) so
        // emitted bytecode remains compatible for downstream consumers; only the
        // BUILD JVM is set here.
        jvmToolchain(jvmToolchainVersion)

        jvm("desktop")
        iosSimulatorArm64()
        iosArm64()
        // Web (js + wasmJs): every module is fully configured for BOTH a nodejs and a headless-Chrome
        // browser test environment, with `useEsModules()` so the whole graph shares ONE module format.
        // Without ESM only `core-base/database` (forced by Room3-web's `import.meta.url` worker URL)
        // and `cmp-web` emit ES modules while everything else emits UMD; that split makes Node load the
        // UMD `.js` files as ESM (the wrapper's CJS/AMD branches don't fire → "kotlin-kotlin-stdlib was
        // not found"). One format across the graph fixes the node-tested logic modules.
        //
        // WHICH web test task actually runs is decided below by module kind (detected via the Compose
        // plugin, order-independent) — configuring both envs here keeps every target fully set up (a
        // bare `wasmJs {}` leaves a module "not configured for JS usage" and breaks wasm npm install).
        js(IR) {
            useEsModules()
            nodejs()
            browser {
                testTask {
                    useKarma {
                        useChromeHeadless()
                    }
                }
            }
            binaries.executable()
        }
        wasmJs {
            useEsModules()
            nodejs()
            browser {
                testTask {
                    useKarma {
                        useChromeHeadless()
                    }
                }
            }
            binaries.executable()
        }

        compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
            freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
            freeCompilerArgs.add("-opt-in=kotlin.time.ExperimentalTime")
        }
    }

    // Route each module's web tests to the environment where they can actually run, keeping the
    // OTHER (physically-unrunnable) web test tasks off. Detection is by the Compose plugin, not the
    // module path, so it's correct regardless of apply order or location (e.g. `:cmp-shared`, which
    // applies the Compose feature convention but is not under `:feature:*`).
    //
    //  • Compose-UI module (has `org.jetbrains.compose`) → runs `jsBrowserTest` + `wasmJsBrowserTest`
    //    in headless Chrome, where `runComposeUiTest` (RULE-KMP-COMPOSE-UITEST-001) renders through
    //    skiko. The node web-test tasks CANNOT render Compose (no `window`/DOM → the test binary
    //    aborts with `ReferenceError: window is not defined`), so they're turned off.
    //
    //  • Logic module (no Compose) → runs `jsNodeTest` (fast, and the ESM fix above makes it load).
    //    `jsBrowserTest` is just a slower duplicate of `jsNodeTest`, so it stays off.
    //    `wasmJsNodeTest` is off ONLY when the module's wasm test bundle actually pulls skiko
    //    (via core:store → core-base/ui → compottie): skiko ships no `skiko.mjs`, so the bundle
    //    cannot resolve it under node. Modules that never reach skiko RUN it — measured
    //    2026-09-17 on :core:database, which executes 10 tests there. A blanket disable cost
    //    those modules their entire wasm verdict while still reporting BUILD SUCCESSFUL, which
    //    is indistinguishable from having run and passed.
    //
    // There is no single config where all four web test tasks pass: Compose-on-node is impossible
    // by construction, so the environment MUST be selected.
    afterEvaluate {
        val isCompose = pluginManager.hasPlugin("org.jetbrains.compose")
        val disabledWebTestTasks = if (isCompose) {
            setOf("jsNodeTest", "wasmJsNodeTest")
        } else {
            setOf("jsBrowserTest", "wasmJsBrowserTest")
        }
        // Resolved lazily through a Provider so the configuration is not resolved at configuration
        // time and the value stays configuration-cache safe.
        val wasmPullsSkiko = configurations.named("wasmJsTestRuntimeClasspath").flatMap { cfg ->
            cfg.incoming.artifacts.resolvedArtifacts.map { artifacts ->
                artifacts.any { it.id.componentIdentifier.displayName.contains("skiko") }
            }
        }
        tasks.configureEach {
            if (name in disabledWebTestTasks) {
                enabled = false
            }
            if (!isCompose && name == "wasmJsNodeTest") {
                // Skip only when skiko is genuinely on the bundle's classpath, and SAY SO — a
                // silently-disabled test task reports green and reads as passing coverage.
                onlyIf {
                    val pulls = wasmPullsSkiko.get()
                    if (pulls) {
                        logger.lifecycle(
                            "[web-tests] $path SKIPPED - wasm test bundle pulls skiko, which ships no skiko.mjs",
                        )
                    }
                    !pulls
                }
            }
        }
    }
}
