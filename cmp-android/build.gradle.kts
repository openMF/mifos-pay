/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
import com.android.build.api.instrumentation.InstrumentationScope
import org.convention.dynamicVersion
import org.convention.resolveSecretPath
import java.util.Properties

plugins {
    alias(libs.plugins.android.application.convention)
    alias(libs.plugins.android.application.compose.convention)
    alias(libs.plugins.baselineprofile)
    alias(libs.plugins.roborazzi)
    alias(libs.plugins.aboutLibraries)
    alias(libs.plugins.ksp)
}

val appId: String = libs.versions.appId.get()
val appDisplayName: String = libs.versions.appDisplayName.get()

// Release signing — resolved from vault-materialized secrets, NEVER a committed literal
// (RULE-NO-SOURCE-SECRET-LITERAL-001, 2026-08-31). `/secrets pull` writes the keystore +
// passwords into the gitignored secrets/live/android/keystores/; CI may override via KEYSTORE_* env.
// A hardcoded fallback here is FORBIDDEN even as a "sample": forks swap the sample for the real
// credential (observed in 4+ apps), and a literal turns every ordinary grep/sed of this file into
// a secret spill that the output guards cannot catch.
val ksPropsFile = project.resolveSecretPath("upload_keystore_properties")
val signingProps = Properties().apply {
    if (ksPropsFile.exists()) ksPropsFile.inputStream().use { load(it) }
}
val ksFile = System.getenv("KEYSTORE_PATH")?.let { file(it) }
    ?: project.resolveSecretPath("upload_keystore")
// The two transports use DIFFERENT names, by design: env vars are KEYSTORE_* (CI), while the
// properties file uses the `name:` fields secrets/LAYOUT.yaml declares (storePassword / keyAlias /
// keyPassword). In LAYOUT, `source_env:` is only where the VALUE comes from — NOT the key it is
// written under. (An earlier revision changed these reads to KEYSTORE_* after observing a fork whose
// properties file had those keys; that fork was simply missing secrets/LAYOUT.yaml, so its file had
// been written by the manifest/env path instead of the LAYOUT pass. Reverted 2026-09-03.)
val ksStorePass = System.getenv("KEYSTORE_PASSWORD") ?: signingProps.getProperty("storePassword")
val ksKeyAlias = System.getenv("KEYSTORE_ALIAS") ?: signingProps.getProperty("keyAlias")
val ksKeyPass = System.getenv("KEYSTORE_ALIAS_PASSWORD") ?: signingProps.getProperty("keyPassword")
val releaseSigningReady = ksFile.exists() && !ksStorePass.isNullOrBlank() &&
    !ksKeyAlias.isNullOrBlank() && !ksKeyPass.isNullOrBlank()

android {
    // Namespace is the STABLE code-gen package (BuildConfig / R live here) — deliberately fixed to the
    // module's own source package, NOT the white-label applicationId. Decoupling the two is what lets a
    // fork rebrand `applicationId` (market identity) with zero source edits while `BuildConfig` stays at a
    // stable import path (`cmp.android.app.BuildConfig`, same package as this module's Kotlin).
    namespace = "cmp.android.app"

    defaultConfig {
        applicationId = appId
        // app_name is injected from libs.versions.toml — no hardcoded strings.xml entry needed
        resValue("string", "app_name", appDisplayName)
        versionName = System.getenv("VERSION") ?: project.dynamicVersion
        versionCode = System.getenv("VERSION_CODE")?.toIntOrNull() ?: 1
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            if (releaseSigningReady) {
                storeFile = ksFile
                storePassword = ksStorePass
                keyAlias = ksKeyAlias
                keyPassword = ksKeyPass
                enableV1Signing = true
                enableV2Signing = true
            } else {
                logger.warn(
                    "⚠ release signing not configured — run `/secrets pull` to materialize " +
                        "secrets/live/android/keystores/ (or set KEYSTORE_* env). Build will be UNSIGNED.",
                )
            }
        }
    }

    buildTypes {
        // debug/staging/release are registered by org.convention.kmp.flavors via
        // KMPFlavorsConventionPlugin (isDebuggable, applicationIdSuffix, isMinifyEnabled).
        // Only Android-app-specific settings that the plugin doesn't own live here.
        getByName("staging") {
            isJniDebuggable = false
            signingConfig = if (releaseSigningReady) signingConfigs.getByName("release") else null
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro", "fork-proguard-rules.pro")
        }
        release {
            isShrinkResources = true
            isJniDebuggable = false
            signingConfig = if (releaseSigningReady) signingConfigs.getByName("release") else null
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro", "fork-proguard-rules.pro")
        }
    }

    buildFeatures {
        dataBinding = true
        buildConfig = true
        resValues = true
    }

    packaging {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    // TODO:: Workaround for Ktor(3.2.0) R8/ProGuard Issue
    androidComponents {
        onVariants { variant ->
            variant.instrumentation.transformClassesWith(
                FieldSkippingClassVisitor.Factory::class.java,
                scope = InstrumentationScope.ALL,
            ) { params ->
                params.classes.add("io.ktor.client.plugins.Messages")
            }
        }
    }
}

dependencies {
    implementation(platform(libs.firebase.bom))

    implementation(projects.cmpShared)
    implementation(projects.core.ui)
    implementation(projects.core.firebase)
    implementation(projects.core.model)
    implementation(projects.core.data)
    implementation(projects.core.datastore)
    implementation(projects.core.platform)
    implementation(projects.sync)

    // Compose
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.core.splashscreen)

    implementation(libs.androidx.profileinstaller)
    implementation(libs.androidx.tracing.ktx)

    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)

    implementation(libs.kermit.koin)

    implementation(libs.coil.kt)

    implementation(libs.filekit.core)
    implementation(libs.filekit.compose)
    implementation(libs.filekit.dialog.compose)
    implementation(libs.filekit.coil)

    runtimeOnly(libs.androidx.compose.runtime)
    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.androidx.compose.ui.test)

    androidTestImplementation(libs.androidx.compose.ui.test)
    androidTestImplementation(libs.androidx.test.ext.junit)

    testImplementation(libs.koin.test.junit4)
}

dependencyGuard {
    configuration("prodReleaseRuntimeClasspath") {
        modules = true
        tree = true
    }
}

// ── Fork-owned seam (white-label, mirrors core/<mod>/module-deps.gradle.kts) ────────────────────
// A fork's own Android-app dependencies, manifest placeholders and BuildConfig fields go in
// `cmp-android/module-deps.gradle.kts` — a file a template sync never touches. String configuration
// names (`"implementation"(...)`) there: type-safe `libs.` accessors are not generated for script plugins.
project.file("module-deps.gradle.kts").takeIf { it.exists() }?.let { apply(from = it) }

baselineProfile {
    // Don't build on every iteration of a full assemble.
    // Instead enable generation directly for the release build variant.
    automaticGenerationDuringBuild = false

    // Make use of Dex Layout Optimizations via Startup Profiles
    dexLayoutOptimization = true
}
