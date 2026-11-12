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
import java.util.Properties

plugins {
    alias(libs.plugins.kmp.library.convention)
    alias(libs.plugins.ktrofit)
    alias(libs.plugins.buildkonfig)
    id("kotlinx-serialization")
    id("com.google.devtools.ksp")
}

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) load(f.inputStream())
}

buildkonfig {
    // packageName mirrors the module's Kotlin source package (module scope) so the generated
    // `kpt.core.network.BuildKonfig` is imported without ceremony.
    packageName = "kpt.core.network"
    defaultConfigs {
        // demo:begin — FRED is a DEMO access point; `scripts/remove-demo.sh` deletes FredApi/FredApiConfig
        // and DemoNetworkModule, so this field must go with them. Unfenced, a cleaned fork kept a
        // BuildKonfig field for an API it no longer has.
        // FRED_API_KEY is a VAULT-managed client secret, NOT a hardcoded key. It is declared as the
        // `mifos-x-fred-api-key` alias (category env_var_client) in secrets-manifest.yaml, and the "fred"
        // access point in app-profile/app.yaml references it. `/secrets pull` materializes it to
        // local.properties (the sanctioned KMP env_var_client target, RULE-SECRETS-LAYOUT-001); CI reads
        // the FRED_API_KEY env var (from `/secrets sync-to-ci`). A compiled-in client key must be read at
        // build time — this IS that sanctioned read, so no secret value ever lives in a tracked file.
        buildConfigField(
            STRING, "FRED_API_KEY",
            System.getenv("FRED_API_KEY") ?: localProps.getProperty("FRED_API_KEY", ""),
        )
        // demo:end
        // syncForkConfig:buildkonfig:begin — GENERATED from `app-profile/app.yaml`: one field per
        // access point declaring `publishable_key_env:`/`api_key_env:`, plus every `network.build_config_fields`
        // entry. DO NOT HAND-EDIT — declare the key in app-profile and re-run `./gradlew syncForkConfig`.
        // Values are read at BUILD time from the env var or local.properties, so no secret is committed.
        // syncForkConfig:buildkonfig:end
    }
}

androidComponents {
    finalizeDsl { ext ->
        ext.withHostTest {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
        }
    }
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.common)
            api(projects.core.model)
            api(projects.coreBase.network)

            implementation(projects.core.datastore)

            implementation(libs.kotlinx.serialization.json)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.serialization)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.serialization.kotlinx.json)

            implementation(libs.ktorfit.lib)

            implementation(libs.squareup.okio)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.koin.android)
        }

        nativeMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktorfit.lib)
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", libs.ktorfit.ksp)
    add("kspAndroid", libs.ktorfit.ksp)
    add("kspJs", libs.ktorfit.ksp)
    add("kspWasmJs", libs.ktorfit.ksp)
    add("kspDesktop", libs.ktorfit.ksp)
    add("kspIosArm64", libs.ktorfit.ksp)
    add("kspIosSimulatorArm64", libs.ktorfit.ksp)
}

// ── Fork-owned dependency seam (white-label, mirrors `feature-deps.gradle.kts`) ────────────────
// A fork adds its OWN dependencies for this module in `core/network/module-deps.gradle.kts` — never in
// this file. That is what lets THIS build file be `owner: template` and FULL-COPY on a template
// sync: the fork's deps live in a file the sync never touches, so a template plugin/version bump
// can no longer drop them and no 3-way merge is needed.
//
// String `"commonMainImplementation"(...)` notation is used in the seam, not the type-safe
// `libs.`/`projects.` accessors: those are NOT generated for `apply(from = ...)` script plugins.
//
// Guarded like feature-deps: a fork that adopted the template BEFORE this seam existed may not have
// the file yet, and an unconditional apply would fail the whole configuration.
project.file("module-deps.gradle.kts").takeIf { it.exists() }?.let { apply(from = it) }
