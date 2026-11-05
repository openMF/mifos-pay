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

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.named

/**
 * Configures the Detekt plugin with the [extension] configuration.
 * This includes setting the JVM target to 17 and enabling all reports.
 * Additionally, it adds the `detekt-formatting` and `twitter-detekt-compose` plugins.
 * @see DetektExtension
 * @see Detekt
 */
internal fun Project.configureDetekt(extension: DetektExtension) = extension.apply {
    tasks.named<Detekt>("detekt") {
        mustRunAfter(":cmp-android:dependencyGuard")
        jvmTarget = "17"
        // Fork: the detekt baseline that grandfathers the pre-template mifos-pay findings (held; queued upstream as a baseline seam).
        baseline.set(rootDir.resolve("config/detekt/baseline.xml"))
        // ONE whole-repo pass, on :cmp-android (CI's `./gradlew detekt` reaches it); every other module
        // lints only its own tree. When every module scanned rootDir, `./gradlew detekt` repeated the
        // whole-repo analysis once per feature module — 25 min on a fork with ~40 of them, each failing
        // on the same fork-wide findings. `-Pkpt.detekt.files=a.kt,b.kt` (paths relative to the root)
        // narrows the whole-repo pass to exactly those files: what the git hooks check, so a commit is
        // judged on the code it touches.
        val onlyFiles = providers.gradleProperty("kpt.detekt.files").orNull
        when {
            this@configureDetekt.path != ":cmp-android" -> setSource(files(projectDir))
            onlyFiles != null -> setSource(files(onlyFiles.split(',').filter(String::isNotBlank).map(rootDir::resolve)))
            else -> setSource(files(rootDir))
        }
        include("**/*.kt")
        exclude("**/*.kts")
        exclude("**/resources/**")
        exclude("**/build/**")
        exclude("**/generated/**")
        exclude("**/build-logic/**")
        exclude("**/spotless/**")
        // product-health canary fixtures. `source(files(rootDir))` means EVERY module's detekt task
        // scans the whole repo, so these get linted by all of them. They are deliberately-malformed
        // sample code — a RED fixture exists precisely to be wrong, and a GREEN one is a fragment with
        // no package/consumer — so linting them reports defects that are the fixture's whole purpose.
        exclude("scripts/product-health/tests/**")
        // Same reason for the doc-scanner's fixtures: scripts/docs/tests/** holds deliberately
        // UNDOCUMENTED declarations so scripts/docs/scanners/kotlin.awk can be proven to find them.
        // Documenting a red fixture deletes the evidence that the scanner works.
        exclude("scripts/docs/tests/**")
        reports {
            xml.required.set(true)
            html.required.set(true)
            txt.required.set(true)
            sarif.required.set(true)
            md.required.set(true)
        }
    }
    dependencies {
        "detektPlugins"(libs.findLibrary("detekt-formatting").get())
        "detektPlugins"(libs.findLibrary("twitter-detekt-compose").get())
    }
}
