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

kotlin {
    sourceSets {

        androidMain.dependencies {

            // ML Kit QR Scanner
            implementation(libs.mlkit.barcode.scanning)

            // Play Services Code Scanner API
            implementation(libs.google.play.services.code.scanner)

            // Dynamic module installer
            implementation(libs.androidx.profileinstaller)

            // Kermit logger
            implementation(libs.kermit.logging)

            implementation(libs.accompanist.permissions)
        }

        commonMain.dependencies {
            implementation(compose.ui)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(projects.core.common)
            implementation(projects.core.ui)
            // kpt.core.model.* (needed by PayAnyoneViewModel/PayeeDetailsViewModel etc.)
            // comes transitively via core/data's api(core.model) re-export.
        }
    }
}

compose.resources {
    packageOfResClass = "kpt.feature.send.money.generated.resources"
}
