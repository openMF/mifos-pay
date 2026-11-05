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
import org.gradle.api.Project

/**
 * ktlint version Spotless runs. Pinned here rather than in the catalog: Spotless resolves it at configuration time,
 * before the catalog is available to build-logic.
 */
const val KTLINT_VERSION = "1.0.1"

/**
 * Configures the Spotless plugin with the [extension] configuration.
 * This includes setting up the `ktlint` formatter and the license header.
 *
 * On the ROOT project it formats what no module can see instead: `build-logic/` (an included build,
 * so no module's Spotless reaches it) and the root `*.kts` files. Every other project formats its own
 * tree. Both run ktlint on `.kts` too, so build scripts follow the same style as the sources.
 * @see SpotlessExtension
 */
internal fun Project.configureSpotless(extension: SpotlessExtension) = extension.apply {
    val root = this@configureSpotless == rootProject
    kotlin {
        if (root) target("build-logic/**/*.kt") else target("**/*.kt")
        targetExclude("**/build/**/*.kt")
        ktlint(KTLINT_VERSION).editorConfigOverride(
            // build-logic only: ktlint cannot see that `import org.gradle.kotlin.dsl.assign` is used —
            // it backs every `property = value` on a Gradle Property — so this rule deletes it and
            // build-logic stops compiling.
            if (root) {
                mapOf("android" to "true", "ktlint_standard_no-unused-imports" to "disabled")
            } else {
                mapOf("android" to "true")
            },
        )
        // ONE header source for every format — spotless/license-header.txt (fork-owned). See LicenseHeader.
        // An EMPTY header is not configured at all: Spotless applies `licenseHeader("")` by DELETING every
        // existing header, so a missing header file (a fork that never received it) stripped the header from
        // ~2,500 files on 2026-11-02. No header source means "leave headers alone", never "remove them".
        // Template code keeps the template's header (LicenseHeader.NOT_TEMPLATE_CODE).
        LicenseHeader.kotlin(rootProject.rootDir).takeIf(String::isNotEmpty)
            ?.let { licenseHeader(it).onlyIfContentMatches(LicenseHeader.NOT_TEMPLATE_CODE) }
    }

    kotlinGradle {
        if (root) target("*.kts", "build-logic/**/*.kts") else target("**/*.kts")
        targetExclude("**/build/**/*.kts")
        ktlint(KTLINT_VERSION).editorConfigOverride(
            mapOf("android" to "true"),
        )
        // Look for the first line that doesn't have a block comment (assumed to be the license)
        LicenseHeader.kotlin(rootProject.rootDir).takeIf(String::isNotEmpty)
            ?.let { licenseHeader(it, "(^(?![\\/ ]\\*).*$)").onlyIfContentMatches(LicenseHeader.NOT_TEMPLATE_CODE) }
    }

    format("xml") {
        if (root) {
            // app-profile/ is not a Gradle project, so no module's Spotless reaches it. Its icon XML is
            // the SOURCE syncForkConfig copies into cmp-android/src/main/res — formatting only the copy
            // would be undone by the next syncForkConfig, so the source is formatted here.
            target("app-profile/icons/**/*.xml")
        } else {
            target("**/*.xml")
        }
        targetExclude("**/build/**/*.xml")
        // Look for the first XML tag that isn't a comment (<!--) or the xml declaration (<?xml)
        LicenseHeader.xml(rootProject.rootDir).takeIf(String::isNotEmpty)
            ?.let { licenseHeader(it, "(<[^!?])").onlyIfContentMatches(LicenseHeader.NOT_TEMPLATE_CODE) }
    }
}
