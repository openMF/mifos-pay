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

import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Configures the `kover` plugin with the [configure] lambda.
 *
 * Mirrors the [detektGradle] / [spotlessGradle] helpers in KptGradleExtensions.kt
 * so KoverConventionPlugin reads the same way as DetektConventionPlugin and
 * SpotlessConventionPlugin.
 */
internal inline fun Project.koverGradle(crossinline configure: KoverProjectExtension.() -> Unit) =
    extensions.configure<KoverProjectExtension> {
        configure()
    }

/**
 * Root-level kover report configuration — single source of truth for the
 * filter / verify rules that gate the aggregated coverage report.
 *
 * Applied by KoverConventionPlugin when it runs on the root project. Lives
 * here (not inline in the plugin class) for parity with the detekt / spotless
 * convention plugins, which delegate their configuration to helpers in this
 * package.
 */
internal fun Project.configureKoverRootReports() = koverGradle {
    reports {
        filters {
            excludes {
                classes(
                    // Koin / kotlin-inject DI modules
                    "*.di.*",
                    "*.BuildConfig",
                    // Compose generated lambda holders
                    "*ComposableSingletons*",
                    // Generated factories
                    "*_*Factory*",
                    "*\$ComposableLambda\$*",
                    // @Preview functions
                    "*Preview*",
                    // test helpers themselves
                    "*Test*",
                )
                packages(
                    "*.generated.*",
                    "*.ksp.*",
                )
                annotatedBy(
                    // @Composable funcs are better tested via screenshot/UI tests,
                    // not Kover line coverage.
                    "androidx.compose.runtime.Composable",
                )
            }
        }
        verify {
            // Phase 1 floor — single global threshold while coverage grows.
            // Per-module thresholds added as test-coverage PRs raise individual modules.
            rule { minBound(40) }
        }
    }
}
