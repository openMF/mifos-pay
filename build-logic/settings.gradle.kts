/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
pluginManagement {
    // OPTIONAL composite-include of the upstream kmp-product-flavors build-logic.
    // Controlled by lib-integrate.properties (kmp-product-flavors.local / .path).
    // Set .local=false to force Maven Central resolution even when source is present.
    // When the path doesn't exist (CI, contributor's flat checkout) the plugin
    // resolves from Maven Central / Gradle Plugin Portal regardless of the flag.
    val libProps = java.util.Properties().apply {
        val propsFile = file("../lib-integrate.properties")
        if (propsFile.exists()) propsFile.inputStream().use { load(it) }
    }
    val flavorLocal = libProps.getProperty("kmp-product-flavors.local", "true") == "true"
    val flavorPath = libProps.getProperty(
        "kmp-product-flavors.path",
        "../../../../mbs/kmp-product-flavors/source/kmp-product-flavors/build-logic",
    )
    val upstreamFlavorPluginBuildLogic = settingsDir.toPath().resolve(flavorPath).normalize().toFile()
    if (flavorLocal && upstreamFlavorPluginBuildLogic.isDirectory) {
        println("⚡ [lib-integrate] kmp-product-flavors → local source ($flavorPath)")
        includeBuild(upstreamFlavorPluginBuildLogic) {
            name = "kmp-product-flavors-build-logic"
        }
    } else if (!upstreamFlavorPluginBuildLogic.isDirectory) {
        println("📦 [lib-integrate] kmp-product-flavors → Maven Central (source not found at $flavorPath)")
    } else {
        println("📦 [lib-integrate] kmp-product-flavors → Maven Central (local=false)")
    }
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"
include(":convention")
