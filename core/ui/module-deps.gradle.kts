/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */

// module-deps.gradle.kts — the FORK-OWNED dependency seam for `core/ui` (white-label).
//
// `core/ui/build.gradle.kts` is template-owned and FULL-COPIES on `/kmp-project-template-sync`.
// This fork's own dependencies live HERE so they survive every sync.
//
// String configuration notation is required — type-safe `libs.`/`projects.`/`compose.` accessors
// are not generated for applied script plugins.

val libs = extensions.getByType<org.gradle.api.artifacts.VersionCatalogsExtension>().named("libs")

dependencies {
    // Fork addition: PasswordStrengthIndicator.kt uses Icons.Filled.CheckCircle/Close.
    // Was `implementation(compose.materialIconsExtended)` in commonMain.dependencies.
    //
    // Resolved through the catalog alias `jb-composeMaterialIconsExtended`, whose version.ref is
    // `composeMaterialIconsExtended` (1.7.3) and NOT `composeJB` (1.11.1) — that artifact is frozen
    // upstream at 1.7.3, which is what the `compose.materialIconsExtended` accessor resolves.
    // Verified by dependency-graph diff: using composeJB silently moved it 1.7.3 → 1.11.1.
    "commonMainImplementation"(libs.findLibrary("jb-composeMaterialIconsExtended").get())
    // BiometricError, mapped to copy by fork/biometrics for the passcode and settings features.
    "commonMainImplementation"(libs.findLibrary("mifos-authenticator-biometrics").get())
}
