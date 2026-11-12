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
        commonMain.dependencies {
            // kpt.core.model.* / kpt.core.datastore.UserPreferencesRepository
            // (needed by SpecificTransactionsViewModel/TransactionDetailViewModel) come
            // transitively via core/data's api(core.model)/api(core.datastore) re-export.
            implementation(projects.core.common)
            implementation(compose.ui)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.kotlinx.datetime)
            implementation(libs.cmp.pdf.generator)
            implementation(libs.kotlinx.html)
        }
    }
}

compose.resources {
    packageOfResClass = "kpt.feature.history.generated.resources"
}
