/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
import com.google.devtools.ksp.gradle.KspExtension
import org.convention.LicenseHeader
import org.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * Plugin that applies the Koin plugin and configures it.
 */
class KMPKoinConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.google.devtools.ksp")
            }

            // The template's KSP processors (tools/*-ksp) stamp generated sources with this header
            // instead of a hardcoded copy — one source, spotless/license-header.txt (LicenseHeader).
            // Newlines are escaped: a KSP option is one string.
            extensions.configure<KspExtension> {
                arg(LicenseHeader.KSP_OPTION, LicenseHeader.kotlin(rootDir, "2026").replace("\n", "\\n"))
            }

            dependencies {
                val bom = libs.findLibrary("koin-bom").get()
                add("commonMainImplementation", platform(bom))
                add("commonMainImplementation", libs.findLibrary("koin.core").get())
                add("commonMainImplementation", libs.findLibrary("koin.annotations").get())

                add("commonTestImplementation", libs.findLibrary("koin.test").get())
            }
        }
    }
}
