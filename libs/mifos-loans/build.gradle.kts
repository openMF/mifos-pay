/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
plugins {
    alias(libs.plugins.cmp.feature.convention)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktorfit)
    alias(libs.plugins.ksp)
}

// namespace auto-derives from baseNamespace + module path via kmp.library.convention.
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(compose.ui)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.filekit.core)
            implementation(libs.filekit.dialogs)
            implementation(libs.filekit.dialogs.compose)
            implementation(libs.compose.signature)
            implementation(projects.coreBase.ui)
            implementation(projects.coreBase.common)
            implementation(projects.coreBase.network)
            // org.mifos.corebase.network (KtorHttpClient, ResultSuspendConverterFactory) moved from
            // core-base/network (template-owned) to core/network at the 90ff6da6 template sync.
            implementation(projects.core.network)
        }

        androidMain.dependencies {
            implementation(libs.compose.webview.multiplatform)
            // Android-only deps moved from the top-level `dependencies { }` block —
            // AGP-9 KMP-library rejects untargeted `implementation()` at the top level.
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.core.ktx)
        }

        nativeMain.dependencies {
            implementation(libs.compose.webview.multiplatform)
        }

        commonTest.dependencies {
            // JUnit moved from top-level `testImplementation` — the KMP source-set-scoped
            // dependency block is the AGP-9-correct location.
            implementation(libs.junit)
        }

        androidInstrumentedTest.dependencies {
            // AndroidX test / espresso re-wired from top-level `androidTestImplementation`.
            implementation(libs.androidx.test.ext.junit)
            implementation(libs.espresso.core)
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", libs.ktorfit.ksp)
    add("kspAndroid", libs.ktorfit.ksp)
    add("kspJs", libs.ktorfit.ksp)
    add("kspWasmJs", libs.ktorfit.ksp)
    add("kspDesktop", libs.ktorfit.ksp)
    // kspIosX64 removed — iosX64 target is not registered by the template's KMP
    // convention on AGP-9 (iosArm64 + iosSimulatorArm64 only).
    add("kspIosArm64", libs.ktorfit.ksp)
    add("kspIosSimulatorArm64", libs.ktorfit.ksp)
}
