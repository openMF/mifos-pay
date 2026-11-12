/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */

// module-deps.gradle.kts — the FORK-OWNED dependency seam for `core/data` (white-label).
//
// `core/data/build.gradle.kts` is template-owned and FULL-COPIES on `/kmp-project-template-sync`.
// This fork's own dependencies live HERE so they survive every sync.
//
// String configuration notation is required — type-safe `libs.`/`projects.`/`compose.` accessors
// are not generated for applied script plugins.

val libs = extensions.getByType<org.gradle.api.artifacts.VersionCatalogsExtension>().named("libs")

dependencies {
    // Moved out of commonMain.dependencies in build.gradle.kts (template-owned, full-copied).
    // `api` is load-bearing here — these types surface in core/data's public API.
    "commonMainApi"(project(":core:datastore"))
    "commonMainApi"(project(":core:model"))
    "commonMainApi"(project(":core:network"))

    "commonMainImplementation"(libs.findLibrary("mifos-authenticator-biometrics").get())
    "commonMainImplementation"(libs.findLibrary("mifos-authenticator-passcode").get())
}
