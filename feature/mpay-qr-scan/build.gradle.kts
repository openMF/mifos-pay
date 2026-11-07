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

// namespace auto-derives from baseNamespace + module path via kmp.library.convention;
// consumer-rules.pro is auto-registered by the convention plugin when the file exists.
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.coreBase.platform)
            implementation(compose.ui)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.coil.kt.compose)
            implementation(libs.filekit.core)
            implementation(libs.filekit.compose)
        }

        androidMain.dependencies {
            implementation(libs.androidx.camera.view)
            implementation(libs.androidx.camera.camera2)
            implementation(libs.androidx.camera.lifecycle)
            implementation(libs.accompanist.permissions)
            implementation(libs.mlkit.barcode.scanning)
            implementation(libs.guava)
            implementation(libs.kotlinx.coroutines.play.services)
        }

        nativeMain.dependencies {
            implementation(libs.moko.permission.compose)
        }

        desktopMain.dependencies {
            implementation(libs.zxing)
            implementation(libs.zxing.javase)
        }

        jsMain.dependencies {
            implementation(npm("html5-qrcode", "2.3.8"))
        }

        commonTest.dependencies {
            implementation(libs.multiplatform.settings.test)
            implementation(libs.turbine)
        }
    }
}

compose.resources {
    packageOfResClass = "kpt.feature.mpay.qr.scan.generated.resources"
}
