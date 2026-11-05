/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
import org.convention.configureKoverRootReports
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Self-registering kover convention plugin.
 *
 * - Applied to root (via `alias(libs.plugins.kover.convention)` in the root
 *   plugins block): applies kover and delegates the report filter + verify
 *   rule configuration to `Project.configureKoverRootReports()` in
 *   `org.convention.Kover.kt` — same shape as DetektConventionPlugin /
 *   SpotlessConventionPlugin delegate to `detektGradle` / `spotlessGradle`.
 *
 * - Applied to any leaf module (chained from AndroidApplication / KMPLibrary
 *   / KMPCoreBaseLibraryConventionPlugin, or directly from cmp-desktop):
 *   applies kover AND self-registers into root's aggregation via
 *   `rootProject.dependencies.add("kover", project)`. Each module opts itself
 *   in — there is no central subprojects filter to maintain.
 *
 * Adding a new feature / core module to coverage requires zero changes here:
 * just ensure the new module applies one of the base convention plugins (or
 * applies `org.convention.kover.plugin` directly).
 */
class KoverConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlinx.kover")

            if (project == rootProject) {
                configureKoverRootReports()
            } else {
                // Self-register into root's kover aggregation. Root's `kover`
                // configuration is created when KoverConventionPlugin applies
                // to root (during root build.gradle.kts evaluation, before
                // any subproject is configured), so this add() is safe.
                rootProject.dependencies.add("kover", project)
            }
        }
    }
}
