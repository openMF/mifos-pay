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
import org.convention.readSecretValue
import java.util.Properties

plugins {
    alias(libs.plugins.kmp.library.convention)
    alias(libs.plugins.ktrofit)
    alias(libs.plugins.buildkonfig)
    id("kotlinx-serialization")
    id("com.google.devtools.ksp")
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
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
        // syncForkConfig:buildkonfig:begin — GENERATED from `app-profile/app.yaml`: one field per
        // access point declaring `publishable_key_env:`/`api_key_env:`, plus every `network.build_config_fields`
        // entry. DO NOT HAND-EDIT — declare the key in app-profile and re-run `./gradlew syncForkConfig`.
        // Values are read at BUILD time — the env var (CI), then the secrets/LAYOUT.yaml `kind: value` row whose
        // source_env matches (secrets/live/ else the secrets/sample/ fake), then local.properties (legacy).
        // No secret is committed.
        buildConfigField(
            STRING,
            "SUPABASE_PUBLISHABLE_KEY",
            System.getenv("SUPABASE_PUBLISHABLE_KEY") ?: project.readSecretValue("supabase_publishable_key") ?: localProps.getProperty("SUPABASE_PUBLISHABLE_KEY", ""),
        )
        // syncForkConfig:buildkonfig:end
    }
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.components.resources)
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
    // Koin API bindings, derived from @ApiBinding on the API types. Metadata-only: the bindings are
    // ONE commonMain file every target shares, unlike ktorfit's per-target `create*Api()` factories.
    add("kspCommonMainMetadata", project(":tools:network-ksp"))
    add("kspAndroid", libs.ktorfit.ksp)
    add("kspJs", libs.ktorfit.ksp)
    add("kspWasmJs", libs.ktorfit.ksp)
    add("kspDesktop", libs.ktorfit.ksp)
    add("kspIosArm64", libs.ktorfit.ksp)
    add("kspIosSimulatorArm64", libs.ktorfit.ksp)
}

// Bundled files (`composeResources/files/…`, read with `Res.readBytes`): a fork ships its own data here.
compose.resources {
    publicResClass = true
    generateResClass = always
    packageOfResClass = "kpt.core.network.generated.resources"
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

/*
 * The access-point KINDS, passed to :tools:network-ksp.
 *
 * A class cannot know whether its endpoint is REST or Supabase in a given fork — that is the point's
 * `type:`, and the whole endpoint topology (URL, kind, owner, secrets) deliberately stays in
 * app-profile as per-fork deployment config. `@ApiBinding` carries only the class<->point link, so
 * the processor still needs the kind to pick the binding shape. Feeding the DECLARED ids in also
 * makes an `@ApiBinding("typo")` a build error instead of a binding that fails at Koin graph
 * construction on a device.
 */
val accessPointKinds = providers.provider {
    val yaml = rootProject.file("app-profile/app.yaml")
    if (!yaml.isFile) return@provider ""
    val rows = mutableListOf<String>()
    var inPoints = false
    var id: String? = null
    yaml.forEachLine { raw ->
        val line = raw.substringBefore('#').trimEnd()
        when {
            line.matches(Regex("^  access_points:\\s*$")) -> inPoints = true
            line.matches(Regex("^  [a-zA-Z_]+:.*$")) -> inPoints = false
            line.matches(Regex("^[a-zA-Z_]+:.*$")) -> inPoints = false
            inPoints -> {
                Regex("^\\s*- id:\\s*([A-Za-z0-9_-]+)").find(line)?.let { id = it.groupValues[1] }
                Regex("^\\s*type:\\s*([a-z]+)").find(line)?.let { m ->
                    id?.let { rows += "$it:${m.groupValues[1]}" }
                }
            }
        }
    }
    rows.joinToString("|")
}

ksp {
    arg("kpt.network.accessPointKinds", accessPointKinds.get())
}
