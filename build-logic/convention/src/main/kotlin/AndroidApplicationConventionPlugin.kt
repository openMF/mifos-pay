/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import org.convention.configureBadgingTasks
import org.convention.configureGradleManagedDevices
import org.convention.configureKotlinAndroid
import org.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType

/**
 * Plugin that applies the Android application plugin and configures it.
 */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("com.dropbox.dependency-guard")
                apply("org.convention.detekt.plugin")
                apply("org.convention.spotless.plugin")
                apply("org.convention.kover.plugin")
                apply("org.convention.git.hooks")
                apply("org.convention.android.application.lint")
                apply("org.convention.android.application.firebase")
                // Apply the KMP flavors contract — registers demo/prod AGP
                // productFlavors + debug/staging/release buildTypes + FlavorConfig
                // codegen. Consumers extend via local/LocalFlavors.kt.
                apply("org.convention.kmp.flavors")
            }

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = libs.findVersion("targetSdk").get().requiredVersion.toInt()
                @Suppress("UnstableApiUsage")
                testOptions.animationsDisabled = true
                configureGradleManagedDevices(this)
            }

            // Wire badging tasks (generate/check/update<Variant>Badging) for every AGP
            // application variant. These compare APK manifest output (aapt2 dump badging)
            // against a committed golden — catches accidental manifest/permission/locale
            // drift in PRs.
            val componentsExt = extensions.getByType<ApplicationAndroidComponentsExtension>()
            configureBadgingTasks(componentsExt)
        }
    }
}
