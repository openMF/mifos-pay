/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
import com.mobilebytelabs.kmpflavors.KmpFlavorExtension
import org.apache.tools.ant.filters.ReplaceTokens
import org.convention.forkPropFirst
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kmp.flavors.convention)
}

kotlin {
    js(IR) {
        useEsModules()
        outputModuleName = "cmp-web"
        browser {
            commonWebpackConfig {
                outputFileName = "cmp-web.js"
            }
        }
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        useEsModules()
        outputModuleName = "cmp-wasm"
        browser {
            commonWebpackConfig {
                outputFileName = "cmp-wasm.js"
            }
        }
        binaries.executable()
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        val jsWasmMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(projects.cmpShared)
                implementation(projects.core.common)
                implementation(projects.core.data)
                implementation(projects.core.database)
                implementation(projects.core.model)
                implementation(projects.core.datastore)

                implementation(compose.runtime)
                implementation(compose.ui)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.components.resources)

                implementation(libs.multiplatform.settings)
                implementation(libs.multiplatform.settings.serialization)
                implementation(libs.multiplatform.settings.coroutines)

                implementation(libs.koin.core)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.ktor.client.js)
                // installWebNetworkMonitor() configures the monitor before Koin first touches it.
                implementation(libs.cmp.network.monitor)
            }
        }

        jsMain.get().dependsOn(jsWasmMain)
        wasmJsMain.get().dependsOn(jsWasmMain)
    }
}

compose.resources {
    publicResClass = true
    generateResClass = always
}

val appDisplayName = libs.versions.appDisplayName.get()

// Resolve the active flavor (Gradle property -PkmpFlavor=demo|prod; falls back to DSL default).
val kmpFlavorExt = extensions.getByType<KmpFlavorExtension>()
val activeFlavor: String = (findProperty("kmpFlavor") as? String)
    ?: kmpFlavorExt.flavors.find { it.isDefault.getOrElse(false) }?.name
    ?: "prod"
val activeFlavorConfig = kmpFlavorExt.flavors.findByName(activeFlavor)
val webTitleSuffix = activeFlavorConfig?.webTitleSuffix?.orNull ?: ""

// index.html link-preview / SEO meta is white-labelled from the app-profile store SoT, materialized to
// gradle/fork.properties by syncForkConfig (task `syncForkConfig`). Without this the template's
// mobile-wallet og:image URLs + "Kotlin Multiplatform Template" copy ship on every fork's web build.
// Read through the ONE Gradle-side reader (org.convention.KptAppProfileProperties). `forkPropFirst` keeps
// this file's first-non-blank-wins semantics: each value has an ordered list of source keys.
val webDescription = forkPropFirst("store.android.short.description", "store.subtitle").ifBlank { appDisplayName }
val webKeywords = forkPropFirst("store.ios.keywords", "store.macos.keywords")
val webCopyright = forkPropFirst("store.copyright", "org.copyright").ifBlank { appDisplayName }
val webAuthor = forkPropFirst("org.name", "store.copyright").ifBlank { appDisplayName }
val webUrl = forkPropFirst("web.custom.domain", "org.marketing.url", "org.support.url")
val webOgImage = "./og-image.png" // materialized by syncForkConfig from app-profile/platforms/web/media

// Only index.html carries @TOKEN@s. ReplaceTokens is a text filter: applied to the whole task it
// re-encodes every binary in it as UTF-8 — including the skiko.wasm Compose copies in, which then
// fails to compile and the app never renders (and favicon.ico / og-image.png are corrupted too).
tasks.matching { it.name == "jsProcessResources" || it.name == "wasmJsProcessResources" }
    .configureEach {
        (this as Copy).filesMatching("index.html") {
            filter(
                mapOf(
                    "tokens" to mapOf(
                        "APP_DISPLAY_NAME" to "$appDisplayName$webTitleSuffix",
                        "APP_DESCRIPTION" to webDescription,
                        "APP_KEYWORDS" to webKeywords,
                        "APP_COPYRIGHT" to webCopyright,
                        "APP_AUTHOR" to webAuthor,
                        "APP_URL" to webUrl,
                        "OG_IMAGE" to webOgImage,
                    ),
                ),
                ReplaceTokens::class.java,
            )
        }
    }
