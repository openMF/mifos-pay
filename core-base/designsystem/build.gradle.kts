/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
plugins {
    alias(libs.plugins.kmp.core.base.library.convention)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.compose.ui.tooling)
        }
        commonMain.dependencies {
            implementation(compose.ui)
            implementation(compose.material3)
            implementation(compose.foundation)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.materialIconsExtended)

            // Backs KptToastHost. The DI binding lives in core-base/platform (platformModule),
            // which already owns the manager singles and has Koin; this module only renders.
            // `api` because KptToastHost's own signature takes a ToastHostState and a
            // `@Composable (ToastData) -> Unit` slot — a caller must be able to name both.
            api(libs.cmp.toast)

            api(compose.material3AdaptiveNavigationSuite)
            api(libs.jetbrains.compose.material3.adaptive)
            api(libs.jetbrains.compose.material3.adaptive.layout)
            api(libs.jetbrains.compose.material3.adaptive.navigation)

            implementation(libs.jb.lifecycleViewmodel)
            implementation(libs.ui.backhandler)
        }
    }
}

// Compose Multiplatform UI-test infra — the same pair CMPFeatureConventionPlugin gives a feature module:
// commonTest gets the multiplatform `runComposeUiTest` API, the desktop (JVM) target gets the JUnit4
// runner that executes it. Declared by COORDINATE rather than through the `compose.*` accessors, which is
// what the feature plugin does, and which avoids the experimental-accessor opt-in.
//
// This module holds 40 composables and had NO render test of any kind — which is exactly how `KptFlowRow`
// shipped unable to render any content at all: its `layout {}` block indexed a list it never filled. A
// composable library with no way to compose it in a test cannot catch that class of defect.
dependencies {
    val composeVersion = libs.versions.compose.plugin.get()
    add("commonTestImplementation", "org.jetbrains.compose.ui:ui-test:$composeVersion")
    add("desktopTestImplementation", "org.jetbrains.compose.ui:ui-test-junit4:$composeVersion")
    add("desktopTestImplementation", compose.desktop.currentOs)
}

compose.resources {
    publicResClass = true
    generateResClass = always
    packageOfResClass = "kpt.core.base.designsystem.generated.resources"
}
