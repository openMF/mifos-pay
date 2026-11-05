/*
 * Copyright 2022 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
import com.android.build.api.dsl.ApplicationExtension
import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension
import org.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * Adds Firebase (Analytics, Crashlytics, Performance) to an Android application module.
 *
 * Separate from the base application plugin so a fork that ships without Firebase simply does not apply it, rather
 * than having to strip config out of a shared one.
 */
class AndroidApplicationFirebaseConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "com.google.gms.google-services")
            apply(plugin = "com.google.firebase.crashlytics")

            dependencies {
                val bom = libs.findLibrary("firebase-bom").get()
                "implementation"(platform(bom))
                "implementation"(libs.findLibrary("firebase.analytics").get())
                "implementation"(libs.findLibrary("firebase.crashlytics").get())
            }

            // The Crashlytics mapping-file upload runs at release-build time and returns 400 (failing
            // the WHOLE build) when the fork's Firebase app has no Crashlytics provisioned. Gate it on a
            // gradle property — default true (backward-compatible for configured forks); an unconfigured
            // fork sets `firebase.crashlytics.mappingUploadEnabled=false` in gradle.properties to skip it.
            // (Was hardcoded `true`, which contradicted the comment's own "only if configured" intent.)
            val crashlyticsMappingUpload = providers
                .gradleProperty("firebase.crashlytics.mappingUploadEnabled")
                .orNull?.toBooleanStrictOrNull() ?: true
            extensions.configure<ApplicationExtension> {
                buildTypes.configureEach {
                    // Mapping-file upload only when a Firebase backend is available + configured in
                    // google-services.json — governed by the gate above.
                    configure<CrashlyticsExtension> {
                        mappingFileUploadEnabled = crashlyticsMappingUpload
                    }
                }
            }
        }
    }
}
