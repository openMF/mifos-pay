/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */

// feature-deps.gradle.kts — the FORK-OWNED feature-dependency seam (white-label, S7/F4).
//
// `cmp-navigation/build.gradle.kts` (template-owned) applies this script via `apply(from = ...)`. A fork
// adds/removes a feature module dependency by editing ONLY this file — it never touches the template build
// file, so a template sync full-copies `cmp-navigation/build.gradle.kts` while these deps survive. This is
// the build-graph twin of the `cmp-navigation/registry` runtime seams (ForkFeatureRegistry etc.); adding a
// feature is: include it in `settings.gradle.kts` (or `settings.local.gradle.kts`), add its dep here, and
// register its module + routes in `ForkFeatureRegistry`.
//
// String `project(":feature:x")` notation (not the type-safe `projects.feature.x` accessors) is used
// deliberately: type-safe project accessors are NOT generated for `apply(from = ...)` script plugins. The
// `commonMainImplementation` configuration is the KMP-created implementation configuration for the
// commonMain source set; it exists by the time this script is applied (after the `kotlin { }` block).
//
// Mifos Pay's existing ~28 feature-module deps stay inline in `cmp-navigation/build.gradle.kts`'s own
// `commonMain.dependencies { }` block (pre-dates this seam's adoption; not moved to avoid an unnecessary
// bulk-move of working config). Any NEW feature dependency should go here instead, so it survives the
// next template sync.

dependencies {
    // Every fork feature reaches the app shell here, so cmp-navigation's generated feature aggregates
    // (GeneratedFeatureKoinBindings / Destinations / Tabs scan every feature/*) resolve. Added at the 90ff6da6 sync.
    "commonMainImplementation"(project(":feature:accounts"))
    "commonMainImplementation"(project(":feature:auth"))
    "commonMainImplementation"(project(":feature:autopay"))
    "commonMainImplementation"(project(":feature:beneficiary"))
    "commonMainImplementation"(project(":feature:editpassword"))
    "commonMainImplementation"(project(":feature:faq"))
    "commonMainImplementation"(project(":feature:fast-mpay"))
    "commonMainImplementation"(project(":feature:finance"))
    "commonMainImplementation"(project(":feature:history"))
    "commonMainImplementation"(project(":feature:invoices"))
    "commonMainImplementation"(project(":feature:kyc"))
    "commonMainImplementation"(project(":feature:merchants"))
    "commonMainImplementation"(project(":feature:mpay-qr-scan"))
    "commonMainImplementation"(project(":feature:mpay-qr"))
    "commonMainImplementation"(project(":feature:notification"))
    "commonMainImplementation"(project(":feature:passcode"))
    "commonMainImplementation"(project(":feature:payments"))
    "commonMainImplementation"(project(":feature:pocket"))
    "commonMainImplementation"(project(":feature:receipt"))
    "commonMainImplementation"(project(":feature:savedcards"))
    "commonMainImplementation"(project(":feature:send-money"))
    "commonMainImplementation"(project(":feature:standing-instruction"))
    "commonMainImplementation"(project(":feature:transfer-interbank"))
    "commonMainImplementation"(project(":feature:transfer-intrabank"))
    "commonMainImplementation"(project(":feature:upi-setup"))
    // Fork feature dependencies go below this line, e.g.:
    // "commonMainImplementation"(project(":feature:my-feature"))
}
