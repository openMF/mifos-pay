/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
plugins {
    alias(libs.plugins.cmp.feature.convention)
}

// namespace auto-derives from baseNamespace + module path via kmp.library.convention.
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(compose.ui)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.coil.kt.compose)
            implementation(libs.qrose)

            // Fork addition: MpayQrScreen/MpayQrViewModel read kpt.core.common
            // (MifosDispatchers/getSerialized/ScreenState). kpt.core.model.* /
            // kpt.core.datastore.UserPreferencesRepository come transitively via
            // core/data's api(core.model)/api(core.datastore) re-export.
            implementation(projects.core.common)
            implementation(libs.kermit.logging)
        }

        androidMain.dependencies {
            // zxing is a plain JVM/Android library — must live in androidMain, not
            // top-level `dependencies { }` (AGP-9 KMP-library rejects untargeted
            // top-level implementation() calls).
            implementation(libs.zxing)
        }

        commonTest.dependencies {
            implementation(libs.multiplatform.settings.test)
            implementation(libs.turbine)
        }
    }
}

compose.resources {
    packageOfResClass = "kpt.feature.mpay.qr.generated.resources"
}
