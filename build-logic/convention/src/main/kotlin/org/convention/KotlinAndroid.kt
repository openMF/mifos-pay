/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.convention

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * Configure base Kotlin with Android options
 */
internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension,
) {
    val compileSdkVersion = libs.findVersion("compileSdk").get().requiredVersion.toInt()
    val minSdkVersion = libs.findVersion("minSdk").get().requiredVersion.toInt()
    val javaVer = JavaVersion.toVersion(libs.findVersion("javaVersion").get().requiredVersion)

    commonExtension.apply {
        compileSdk = compileSdkVersion
        defaultConfig.minSdk = minSdkVersion
        compileOptions.sourceCompatibility = javaVer
        compileOptions.targetCompatibility = javaVer
        compileOptions.isCoreLibraryDesugaringEnabled = true
    }

    configureKotlin()

    dependencies {
        add("coreLibraryDesugaring", libs.findLibrary("android.desugarJdkLibs").get())
    }
}

/**
 * Configure base Kotlin options for JVM (non-Android)
 */
internal fun Project.configureKotlinJvm() {
    val javaVer = JavaVersion.toVersion(libs.findVersion("javaVersion").get().requiredVersion)
    extensions.configure<JavaPluginExtension> {
        sourceCompatibility = javaVer
        targetCompatibility = javaVer
    }

    configureKotlin()
}

/**
 * Configure base Kotlin options
 */
private fun Project.configureKotlin() {
    val jvmTargetVersion = JvmTarget.fromTarget(
        libs.findVersion("javaVersion").get().requiredVersion,
    )
    // Use withType to workaround https://youtrack.jetbrains.com/issue/KT-55947
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget = jvmTargetVersion
            // Treat all Kotlin warnings as errors (disabled by default)
            // Override by setting warningsAsErrors=true in your ~/.gradle/gradle.properties
            val warningsAsErrors: String? by project
            allWarningsAsErrors = warningsAsErrors.toBoolean()
            freeCompilerArgs.add(
                // Enable experimental coroutines APIs, including Flow
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            )
        }
    }
}
