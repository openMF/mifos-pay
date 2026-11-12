/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
import com.android.build.api.variant.KotlinMultiplatformAndroidComponentsExtension
import org.convention.BASE_MODULE_NAMESPACE
import org.convention.configureKotlinMultiplatform
import org.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * Plugin that applies the unified KMP+AGP library plugin and configures them.
 * Applies com.android.kotlin.multiplatform.library (AGP 9+, Android side) alongside
 * org.jetbrains.kotlin.multiplatform (KGP, Kotlin side). Both are required: AGP's plugin
 * only handles the Android target; KGP registers KotlinMultiplatformExtension.
 * Android DSL configuration is done via KotlinMultiplatformAndroidComponentsExtension.finalizeDsl.
 */
class KMPCoreBaseLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.kotlin.multiplatform.library")
                // KGP must be applied explicitly: com.android.kotlin.multiplatform.library
                // does not register KotlinMultiplatformExtension on its own. Without this,
                // KSP would be the first to apply KGP (via its KMP support), causing AGP's
                // withPlugin("kotlin.multiplatform") callback to fire during KSP evaluation
                // — before the Android target is set up — and crash with
                // UninitializedPropertyAccessException on KotlinMultiplatformAndroidHandlerImpl.
                apply("org.jetbrains.kotlin.multiplatform")
                apply("org.convention.kmp.flavors")
                apply("org.convention.detekt.plugin")
                apply("org.convention.spotless.plugin")
                apply("org.convention.kover.plugin")
                apply("org.jetbrains.kotlin.plugin.serialization")
                apply("org.jetbrains.kotlin.plugin.parcelize")
            }

            // configureKotlinMultiplatform() must run BEFORE kmp.koin (KSP) is applied.
            // Applying KGP above triggers AGP's withPlugin("kotlin.multiplatform") callback
            // which registers the Android target internally. KSP then registers its own
            // withPlugin("com.android.kotlin.multiplatform.library") callback that calls
            // getAndroidTarget() immediately — which works because the target is already set up.
            configureKotlinMultiplatform()

            pluginManager.apply("org.convention.kmp.koin")

            val baseNamespace = BASE_MODULE_NAMESPACE
            val compileSdkVersion = libs.findVersion("compileSdk").get().requiredVersion.toInt()
            val minSdkVersion = libs.findVersion("minSdk").get().requiredVersion.toInt()
            // core-base uses dots (kpt.core.base.*), not underscores — replace "-" with "."
            val moduleNamespace = baseNamespace + target.path.replace(":", ".").replace("-", ".").lowercase()
            // The resource prefix is derived from the module path so resources inside
            // ":core:module1" must be prefixed with "core_module1_"
            val moduleResourcePrefix = target.path
                .split("""\W""".toRegex())
                .drop(1).distinct()
                .joinToString(separator = "_")
                .lowercase() + "_"

            // KotlinMultiplatformAndroidLibraryExtension is not registered as a top-level
            // project extension when both com.android.kotlin.multiplatform.library and
            // org.jetbrains.kotlin.multiplatform are applied together. Access it via
            // KotlinMultiplatformAndroidComponentsExtension.finalizeDsl(), which fires after
            // DSL evaluation and before variant creation — the correct time to set compileSdk/minSdk.
            extensions.configure<KotlinMultiplatformAndroidComponentsExtension> {
                finalizeDsl { androidExt ->
                    androidExt.compileSdk = compileSdkVersion
                    androidExt.minSdk = minSdkVersion
                    androidExt.namespace = moduleNamespace
                    androidExt.enableCoreLibraryDesugaring = true
                    // Android host (JVM) unit tests are OPT-IN under the AGP-9 KMP library
                    // plugin: without this, every module with a commonTest source set builds
                    // its tests for desktop/ios/native and silently runs ZERO on Android,
                    // emitting "the 'commonTest' source directory exists, but android host
                    // tests are not enabled". Measured 2026-09-17: 31 such warnings across the
                    // 25 core/core-base modules — commonTest was authored everywhere and never
                    // executed on the app's primary production platform.
                    androidExt.withHostTest {
                        // android.jar in unit tests is stubs that throw by default; returning
                        // defaults is what lets commonTest assertions run unmodified on Android.
                        isReturnDefaultValues = true
                        // CMP modules carry composeResources — without this their unit tests
                        // cannot resolve a resource and fail at first access.
                        isIncludeAndroidResources = true
                    }
                    androidExt.androidResources {
                        // Official AGP-9 fix (JetBrains CMP-9547): the new
                        // `com.android.kotlin.multiplatform.library` plugin does NOT process
                        // Android resources by default, so Compose Multiplatform's
                        // CopyResourcesToAndroidAssetsTask never gets its outputDirectory wired
                        // and the module's composeResources (values/*.cvr) are silently dropped
                        // from any consuming APK → MissingResourceException at launch. Enabling
                        // android-resource processing here (once, for every core-base module via
                        // the convention plugin) makes CMP package them correctly.
                        // Ref: https://kotlinlang.org/docs/multiplatform/multiplatform-project-agp-9-migration.html
                        enable = true
                        resourcePrefix = moduleResourcePrefix
                    }
                    // Enable BuildConfig generation for all core-base library modules.
                    // In AGP 9 unified KMP plugin, buildFeatures is not exposed on
                    // KotlinMultiplatformAndroidLibraryExtension — use experimentalProperties.
                    androidExt.experimentalProperties["android.experimental.kmp.enableAndroidBuildConfig"] = true
                    if (target.file("consumer-rules.pro").exists()) {
                        androidExt.optimization {
                            consumerKeepRules.files("consumer-rules.pro")
                        }
                    }
                }
            }

            // Compose UI tests cannot run as ANDROID HOST tests. `runComposeUiTest` routes through
            // androidx.compose.ui.test.RobolectricIdlingStrategy, which reads
            // `android.os.Build.FINGERPRINT.toLowerCase()` to detect Robolectric — and under a host
            // test that field comes from the android.jar STUB and is null, so every such test dies
            // with an NPE inside Compose's own harness, before reaching a single assertion.
            //
            // Measured 2026-09-17 across the whole repo: 899 tests run on android host, 30 failed,
            // and the split was total — every failure was a `*UiTest` (24 classes) and NOT ONE
            // `*UiTest` passed. So this excludes exactly the environmentally-impossible set and
            // costs zero real coverage; the other 869 tests are the coverage android gains.
            //
            // These screens stay covered: the same `runComposeUiTest` sources execute on desktop,
            // iOS and (for core:database) the browser web targets. Running them on android too
            // would need Robolectric, which needs a JUnit4 `@RunWith` that multiplatform
            // `commonTest` classes cannot carry.
            tasks.withType(org.gradle.api.tasks.testing.Test::class.java)
                .matching { it.name == "testAndroidHostTest" }
                .configureEach {
                    filter.excludeTestsMatching("*UiTest*")
                    // A module whose android-host tests are ENTIRELY Compose UI tests (e.g.
                    // :feature:showcase) is left with an empty set, and Gradle treats an
                    // empty filter result as an error ("No tests found for given includes").
                    // That is the expected outcome here, not a misconfiguration.
                    filter.isFailOnNoMatchingTests = false
                }

            dependencies {
                add("coreLibraryDesugaring", libs.findLibrary("android.desugarJdkLibs").get())
                add("commonMainImplementation", libs.findLibrary("kotlinx.serialization.json").get())
                add("commonTestImplementation", libs.findLibrary("kotlin.test").get())
                add("commonTestImplementation", libs.findLibrary("kotlinx.coroutines.test").get())
            }
        }
    }
}
