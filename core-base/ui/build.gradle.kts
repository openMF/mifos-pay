/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import org.convention.forkProp

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
    // Module-local BuildKonfig so AppInfo.appDisplayName (the SINGLE common-code accessor for the
    // app's user-facing display name) reads the fork's app.display.name WITHOUT a hardcoded string
    // resource. Same mechanism feature/settings + core/network use (white-label seam).
    alias(libs.plugins.buildkonfig)
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            api(libs.androidx.metrics)
            implementation(libs.androidx.compose.runtime)
        }

        commonMain.dependencies {
            implementation(projects.coreBase.store)
            implementation(projects.coreBase.designsystem)
            implementation(libs.cmp.network.monitor.compose)
            implementation(libs.cmp.intent.launcher)

            // Compose surfaces for the cores wired in core-base/platform. `api`, not
            // `implementation`: these exist to be CALLED from feature screens, and the feature
            // convention plugin already gives every feature core-base:ui — so exposing them here is
            // what makes ShareButton / ShareSheet / AppIntentsRegistry / rememberSupportsIntent
            // reachable without each feature declaring anything.
            //
            // Their composables call the libraries' global entry points (Share.share, AppIntents…)
            // rather than the CompositionLocals these modules also export, so nothing here needs the
            // toolkit locals provided. The template's own locals in kpt.core.base.platform stay
            // canonical — see the note in LocalManagerProviders.kt about the name collision.
            api(libs.cmp.share.compose)
            api(libs.cmp.intent.launcher.compose)
            api(libs.cmp.app.intents.compose)

            implementation(compose.ui)
            implementation(compose.material3)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.components.resources)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.uiToolingPreview)

            // Compottie — first-class Lottie support for ScreenStateVisual.Lottie.
            // `api` so apps that pass ScreenStateVisual.Lottie(spec = { ... }) can build
            // a LottieCompositionSpec without re-declaring the dep.
            api(libs.compottie)
            api(libs.compottie.resources)

            implementation(libs.jb.composeViewmodel)
            implementation(libs.jb.lifecycle.compose)
            implementation(libs.jb.lifecycleViewmodel)
            implementation(libs.jb.composeNavigation)
            implementation(libs.jb.lifecycleViewmodelSavedState)

            implementation(libs.coil.kt)
            implementation(libs.coil.kt.compose)

            implementation(libs.filekit.core)
            implementation(libs.filekit.compose)
            implementation(libs.filekit.coil)
        }
        androidInstrumentedTest.dependencies {
            implementation(libs.bundles.androidx.compose.ui.test)
        }

        desktopMain.dependencies {
            implementation(compose.desktop.common)
            implementation(compose.desktop.currentOs)
        }

        jvmJsCommonMain.dependencies {
            implementation(libs.filekit.core)
            implementation(libs.filekit.compose)
            implementation(libs.filekit.coil)
        }
    }
}

compose.resources {
    publicResClass = true
    generateResClass = always
    packageOfResClass = "kpt.core.base.ui.generated.resources"
}

// Fork app display name → `kpt.core.base.ui.BuildKonfig.APP_DISPLAY_NAME`, read from
// `gradle/fork.properties#app.display.name` — the build-bridge that syncForkConfig generates from the
// SoT `app-profile/app.yaml#identity.app_name`. AppInfo.appDisplayName exposes it as the single
// common-code read point, so a fork rebrands in app-profile, not in per-feature strings.xml.
// Read through the ONE Gradle-side reader (org.convention.KptAppProfileProperties).
val coreBaseUiAppDisplayName = forkProp("app.display.name", "App Toolkit")

buildkonfig {
    packageName = "kpt.core.base.ui"
    defaultConfigs {
        buildConfigField(
            STRING,
            "APP_DISPLAY_NAME",
            coreBaseUiAppDisplayName,
        )
    }
}

// commonTest gets the multiplatform `runComposeUiTest` API, the desktop (JVM) target gets the JUnit4
// runner that executes it — the same three lines `core-base/designsystem` declares, by COORDINATE
// rather than through the `compose.*` accessors, which avoids the experimental-accessor opt-in.
//
// This module owns `ScreenContent` and its two wrappers and had NO render test of any kind, which is
// how `RefreshableScreenContent` shipped forwarding none of `ScreenContent`'s state slots: nothing
// could compose it and observe that a caller's `empty` never appeared. A state-rendering library
// with no way to render it in a test cannot catch that class of defect.
dependencies {
    val composeVersion = libs.versions.compose.plugin.get()
    add("commonTestImplementation", "org.jetbrains.compose.ui:ui-test:$composeVersion")
    add("desktopTestImplementation", "org.jetbrains.compose.ui:ui-test-junit4:$composeVersion")
    add("desktopTestImplementation", compose.desktop.currentOs)
}
