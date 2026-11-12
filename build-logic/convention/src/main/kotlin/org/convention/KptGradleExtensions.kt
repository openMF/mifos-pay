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

import com.diffplug.gradle.spotless.SpotlessExtension
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType

/**
 * Base namespace prefix for every module's Android `namespace` (R-class package), derived as
 * `BASE_MODULE_NAMESPACE + <module-path>` (e.g. `kpt.feature.loans`). It intentionally matches the
 * hardcoded Kotlin package root (`kpt.*`) used across the source — there is NO reason for the Android
 * namespace to differ per fork, so this is a fixed TEMPLATE constant (owner: template, full-copied on
 * sync), NOT a catalog value a fork rewrites. Keeping it out of `libs.versions.toml` removes a
 * pointless per-fork catalog-merge conflict during `/kmp-project-template-sync`.
 */
const val BASE_MODULE_NAMESPACE = "kpt"

/**
 * Get the `libs` version catalog.
 */
val Project.libs
    get(): VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

/**
 * Get the dynamic version of the project.
 *
 * `version.txt` at the repo root is the version SOURCE OF TRUTH — written by the release pipeline and
 * round-tripped by the gradle `versionFile` task. Prefer it over reckon's `project.version`, which
 * infers from git tags and can fall back to a default when the release tag format isn't recognized.
 * Falls back to reckon only when `version.txt` is absent/blank (e.g. a fresh clone before any release).
 */
val Project.dynamicVersion
    get(): String {
        /**
         * The version catalog, resolved from the file rather than the `libs` accessor — a convention plugin runs
         * before the accessor exists.
         */
        val fromFile = runCatching {
            rootProject.file("version.txt").takeIf { it.exists() }?.readText()?.trim()
        }.getOrNull()?.takeIf { it.isNotBlank() && it != "unspecified" && it != "0.0.0" }
        return (fromFile ?: project.version.toString()).split('+')[0]
    }

/**
 * Configures the `detekt` plugin with the [configure] lambda.
 */
inline fun Project.detektGradle(crossinline configure: DetektExtension.() -> Unit) =
    extensions.configure<DetektExtension> {
        configure()
    }

/**
 * Configures the `spotless` plugin with the [configure] lambda.
 */
inline fun Project.spotlessGradle(crossinline configure: SpotlessExtension.() -> Unit) =
    extensions.configure<SpotlessExtension> {
        configure()
    }
