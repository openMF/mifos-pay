/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
// ── Workspace Library Linker (managed by /lib-integrate) ──────────────────
// Edit lib-integrate.properties to add/remove libraries. Never edit this block.
// Path-existence guard: if library not cloned locally → silently uses Maven Central.
// Groups libraries by path so multiple modules from same build use one includeBuild.
val libProps = java.util.Properties().apply {
    file("lib-integrate.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}
data class LibEntry(val artifact: String, val module: String)
val pathToEntries = mutableMapOf<String, MutableList<LibEntry>>()
libProps.stringPropertyNames()
    .filter { it.endsWith(".local") && libProps[it] == "true" }
    .forEach { key ->
        val lib = key.removeSuffix(".local")
        val path = libProps["$lib.path"] as? String ?: return@forEach
        val module = libProps["$lib.module"] as? String ?: return@forEach
        val artifact = libProps["$lib.artifact"] as? String ?: return@forEach
        // Normalize ".." segments before existence check -- Java's File.exists()
        // doesn't collapse ".." past filesystem root on Windows, so the bare
        // file() check can spuriously pass on CI runners.
        val resolved = settingsDir.toPath().resolve(path).normalize().toFile()
        if (!resolved.isDirectory) {
            println("\uD83D\uDCE6 [lib-integrate] $lib \u2192 Maven Central (source not found at $path)")
            return@forEach
        }
        println("\u26A1 [lib-integrate] $lib \u2192 local source ($path)")
        pathToEntries.getOrPut(path) { mutableListOf() }.add(LibEntry(artifact, module))
    }
pathToEntries.forEach { (path, entries) ->
    includeBuild(path) {
        dependencySubstitution {
            entries.forEach { (artifact, module) ->
                substitute(module(artifact)).using(project(module))
            }
        }
    }
}
// ── End lib-integrate managed block ───────────────────────────────────────

pluginManagement {
    includeBuild("build-logic")
    repositories {
        mavenLocal()
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.PREFER_PROJECT
    repositories {
        mavenLocal()
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("1.0.0")
    id("org.ajoberstar.reckon.settings") version("0.19.2")
}

buildCache {
    local {
        isEnabled = true
        directory = File(rootDir, "build-cache")
    }
}

extensions.configure<org.ajoberstar.reckon.gradle.ReckonExtension> {
    setDefaultInferredScope("patch")
    stages("beta", "rc", "final")
    setScopeCalc { java.util.Optional.of(org.ajoberstar.reckon.core.Scope.PATCH) }
    setScopeCalc(calcScopeFromProp().or(calcScopeFromCommitMessages()))
    setStageCalc(calcStageFromProp())
    setTagWriter { it.toString() }
}

// Project name is driven by fork.project.name in gradle.properties (written by syncForkConfig).
// Fallback keeps the template name so a clean checkout builds without running syncForkConfig first.
rootProject.name = providers.gradleProperty("fork.project.name").getOrElse("kmp-project-template")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":cmp-shared")
include(":cmp-android")
include(":cmp-desktop")
include(":cmp-web")
include(":cmp-navigation")

include(":core:firebase")
include(":core:common")
include(":core:data")
include(":core:database")
include(":core:datastore")
include(":core:designsystem")
include(":core:domain")
include(":core:model")
include(":core:network")
include(":core:platform")
include(":core:store")
include(":tools:store-ksp")
include(":tools:database-ksp")
include(":tools:network-ksp")
include(":tools:data-ksp")
include(":core:ui")

include(":feature:home")
include(":feature:profile")
include(":feature:settings")
include(":feature:onboarding")

include(":core-base:firebase")
include(":core-base:common")
include(":core-base:crypto")
include(":core-base:data")
include(":core-base:database")
include(":core-base:datastore")
include(":core-base:designsystem")
include(":core-base:network")
include(":core-base:observability")
include(":core-base:platform")
include(":core-base:security")
include(":core-base:store")
include(":core-base:ui")

check(JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17)) {
    """
    This project requires JDK 17+ but it is currently using JDK ${JavaVersion.current()}.
    Java Home: [${System.getProperty("java.home")}]
    https://developer.android.com/build/jdks#jdk-config-in-studio
    """.trimIndent()
}
include(":sync")

// Fork-owned module-include seam (B1/T11 white-label). A fork adds its own `include(":feature:x")`
// lines in `settings.local.gradle.kts` — never in this template-synced file. Guarded so a `--clean`
// fork that removed the file still configures.
if (file("settings.local.gradle.kts").exists()) {
    apply(from = "settings.local.gradle.kts")
}
