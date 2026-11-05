/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
import org.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Plugin that applies the CMP feature plugin and configures it.
 * This plugin applies the following plugins:
 * - kpt.kmp.library - Kotlin Multiplatform Library
 * - kpt.kmp.koin - Koin for Kotlin Multiplatform
 * - org.jetbrains.kotlin.plugin.compose - Kotlin Compose
 * - org.jetbrains.compose - Compose Multiplatform
 * - kpt.detekt.plugin - Detekt Plugin
 * - kpt.spotless.plugin - Spotless Plugin
 *
 */
class CMPFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply("org.convention.kmp.library")
                apply("org.convention.kmp.koin")
                apply("org.jetbrains.kotlin.plugin.compose")
                apply("org.jetbrains.compose")
                apply("org.convention.detekt.plugin")
                apply("org.convention.spotless.plugin")
                // Device-free CMP render tier (SCREENSHOT_TEST.md CMP-PRIMARY): renders commonMain
                // @Preview from desktopTest (JVM, no emulator/Robolectric) → verifyRoborazziDesktop.
                apply("io.github.takahirom.roborazzi")
            }

            // Compose Multiplatform UI-test infra (RULE-KMP-COMPOSE-UITEST-001):
            // commonTest gets the multiplatform `runComposeUiTest` API; the desktop (JVM)
            // target gets the JUnit4-backed runner that actually executes it (CI-runnable).
            // Referenced by coordinate — build-logic does not expose the
            // org.jetbrains.compose plugin types, only its plugin id.
            val composeVersion = libs.findVersion("compose-plugin").get().requiredVersion

            dependencies {
                // The @Preview annotation itself, in commonMain. This is what makes the device-free
                // render tier automatic per the block above: a feature declares `@Preview` siblings
                // and CommonComposablePreviewScanner picks them up with zero per-module wiring.
                // MUST be the org.jetbrains artifact — the androidx `ui-tooling-preview` annotation
                // is Android-only and is rejected by G-COMPOSE-PREVIEW (CP-3).
                add(
                    "commonMainImplementation",
                    "org.jetbrains.compose.components:components-ui-tooling-preview:$composeVersion",
                )

                add("commonTestImplementation", "org.jetbrains.compose.ui:ui-test:$composeVersion")
                add(
                    "desktopTestImplementation",
                    "org.jetbrains.compose.ui:ui-test-junit4:$composeVersion",
                )

                // Device-free render tier deps — desktopTest ONLY (JVM-capable). roborazzi-compose-desktop
                // has no JS/Wasm variant, so it MUST NOT go in the js/wasm-shared commonTest. The plain
                // `roborazzi` AAR is never added here (no JVM variant → breaks desktopTest classpath).
                add("desktopTestImplementation", libs.findLibrary("roborazzi-composeDesktop").get())
                add("desktopTestImplementation", libs.findLibrary("composablePreviewScanner-common").get())
                add("desktopTestImplementation", libs.findLibrary("composablePreviewScanner-jvm").get())

                add("commonMainImplementation", project(":core:ui"))
                add("commonMainImplementation", project(":core-base:ui"))
                add("commonMainImplementation", project(":core:designsystem"))
                add("commonMainImplementation", project(":core-base:designsystem"))
                add("commonMainImplementation", project(":core:data"))
                add("commonMainImplementation", project(":core:firebase"))

                add("commonMainImplementation", libs.findLibrary("koin.compose").get())
                add("commonMainImplementation", libs.findLibrary("koin.compose.viewmodel").get())

                add("commonMainImplementation", libs.findLibrary("jb.composeRuntime").get())
                add("commonMainImplementation", libs.findLibrary("jb.composeViewmodel").get())
                add("commonMainImplementation", libs.findLibrary("jb.lifecycleViewmodel").get())
                add("commonMainImplementation", libs.findLibrary("jb.lifecycle.compose").get())

                add(
                    "commonMainImplementation",
                    libs.findLibrary("jb.lifecycleViewmodelSavedState").get(),
                )
                add("commonMainImplementation", libs.findLibrary("jb.savedstate").get())
                add("commonMainImplementation", libs.findLibrary("jb.bundle").get())
                add("commonMainImplementation", libs.findLibrary("jb.composeNavigation").get())
                add(
                    "commonMainImplementation",
                    libs.findLibrary("kotlinx.collections.immutable").get(),
                )

                add("androidMainImplementation", platform(libs.findLibrary("koin-bom").get()))
                add("androidMainImplementation", libs.findLibrary("koin-android").get())
                add("androidMainImplementation", libs.findLibrary("koin.androidx.compose").get())

                add("androidMainImplementation", libs.findLibrary("koin.android").get())
                add("androidMainImplementation", libs.findLibrary("koin.androidx.navigation").get())
                add("androidMainImplementation", libs.findLibrary("koin.androidx.compose").get())
                add("androidMainImplementation", libs.findLibrary("koin.core.viewmodel").get())
            }
        }
    }
}
