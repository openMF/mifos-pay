/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
import com.mobilebytelabs.kmpflavors.KmpFlavorExtension
import com.mobilebytelabs.kmpflavors.KmpFlavorPlugin
import org.convention.KptAppProfileProperties
import org.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register

/**
 * Convention plugin that wires `kmp-product-flavors` with the BASE flavor contract
 * every downstream consumer app inherits:
 *
 * - `tier`: `demo` (default — demo credentials for testers / public demo APK) and
 *   `prod` (real customers).
 * - `buildType`: `debug` (default — debuggable), `staging`, `release`.
 *
 * ## AGP bridge
 *
 * `bridgeAgpProductFlavors` and `bridgeAgpBuildTypes` default to `true` in the plugin
 * and use `AgpProductFlavorRegistrar` (hooked via `pluginManager.withPlugin`) — the
 * correct AGP lifecycle hook (fires synchronously before AGP's afterEvaluate). No manual
 * `android { productFlavors {} }` block is needed in build-logic, including for pure
 * `com.android.application` modules that do not apply `kotlin("multiplatform")`.
 *
 * Consumer apps extend this contract by creating
 * `build-logic/convention/src/main/kotlin/local/LocalFlavors.kt` — see
 * [LocalFlavorsLoader] for the hook and `docs/FLAVORS_EXTENSION.md` for examples.
 */
class KMPFlavorsConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // 1. Apply the upstream plugin (provides KmpFlavorExtension + codegen + source-set wiring
            //    + AGP bridge via AgpProductFlavorRegistrar.whenObjectAdded).
            pluginManager.apply(KmpFlavorPlugin::class.java)

            // 1b. Fork-owned endpoints/creds/log-tag (B4/T10 white-label seam). This TEMPLATE-synced
            //     plugin READS them from the fork-owned `gradle/fork.properties` (never synced), so a
            //     fork changes its API base URLs, demo credentials, and log tag WITHOUT editing this
            //     file — the hardcoded values below are the template defaults when a key is absent.
            // Read through the ONE Gradle-side reader (org.convention.KptAppProfileProperties) instead of a
            // local Properties().load(). Same keys, same defaults — see that file for why six
            // independent parsers were the defect class.
            fun forkProp(key: String, default: String): String =
                KptAppProfileProperties.get(target, key, default)

            // 2. Configure the KMP-side flavor contract.
            //    buildConfigPackage comes from gradle/libs.versions.toml ([versions].appId)
            //    so forks change the brand by editing ONE line.
            extensions.configure<KmpFlavorExtension> {
                buildConfigPackage.set(libs.findVersion("appId").get().requiredVersion)
                // App identity for KmpFlavorsRuntime — single-sourced from libs.versions.toml
                // so forks rebrand by editing one line. appId gets the active flavor's id
                // suffix appended; appDisplayName is the human-facing name.
                appId.set(libs.findVersion("appId").get().requiredVersion)
                appDisplayName.set(libs.findVersion("appDisplayName").get().requiredVersion)
                enableBuildTypes.set(true)

                // iOS xcconfig generation + variants.json export are provided by the plugin
                // (kmp-product-flavors 2.8.3+). Identity stays in Config.xcconfig
                // ($(APP_BUNDLE_ID) / $(TEAM_ID), synced from libs.versions.toml via
                // syncForkConfig), so the per-variant xcconfigs reference those vars.
                // Replaces the former hand-maintained GenerateIosFlavorXcconfigsTask +
                // ExportKmpFlavorsManifestTask in build-logic.
                iosXcconfigGeneration.set(true)
                iosManifestExport.set(true)
                iosBundleIdBaseExpr.set("\$(APP_BUNDLE_ID)")
                iosDevelopmentTeamExpr.set("\$(TEAM_ID)")

                // OFF, and it must stay off. When true the generator appends an optional
                // `#include? "../Pods/…"` to every generated xcconfig. `#include?` never errors on
                // a missing file, so dead wiring would survive silently in every fork that
                // syncs this template. Enforced by G-IOS-SWIFTPM (IOS-7 flag / IOS-6 output).
                //
                // The flag emits one optional xcconfig include for brownfield apps that take the
                // KMP framework via SPM while still using another package manager for OTHER
                // native SDKs. Not our case — this template is SPM end to end.
                iosIncludePodsXcconfig.set(false)

                // SwiftPM distribution (kmp-product-flavors 2.9).
                //
                // 2.9 flipped `spm.generateManifest` to default TRUE. This convention plugin is
                // applied to EVERY KMP module and only `cmp-shared` exports an XCFramework, so the
                // new `requireXcframework` check fired 45 times per build — once per library module
                // that has an iOS target but publishes klibs rather than a framework. The check is a
                // good one; it simply does not apply to a library.
                //
                // OFF because this repo OWNS its manifest: `cmp-ios/Package.swift` is hand-written,
                // reviewed and already referenced by the Xcode project. Two manifests in one tree,
                // with nothing stating which one Xcode resolves, is worse than one we maintain.
                // Adopting the generated manifest is a real option but an Xcode-side migration
                // (project references + the embed Run Script), not a flag flip.
                spm {
                    generateManifest.set(false)
                    // Named for the day that flag flips: our aggregator is XCFramework("ComposeApp"),
                    // not the plugin's "Shared" default, so the resolver would otherwise look for
                    // `assembleShared{BuildType}XCFramework` and find nothing.
                    xcframeworkName.set("ComposeApp")
                    // We ship our own reviewed embed script (flavor-aware, stages the SDK-matching
                    // slice, referenced by the Xcode Run Script phase).
                    generateEmbedScript.set(false)
                }

                flavorDimensions {
                    register("contentType") { priority.set(0) }
                }

                flavors {
                    register("demo") {
                        dimension.set("contentType")
                        isDefault.set(true)
                        applicationIdSuffix.set(".demo")
                        bundleIdSuffix.set(".demo")
                        desktopWindowTitleSuffix.set(" (Demo)")
                        webTitleSuffix.set(" (Demo)")
                        buildConfigField("Boolean", "IS_DEMO_BUILD", "true")
                        buildConfigField("String", "BASE_URL", "\"${forkProp("network.base.url.demo", "https://demo.example.com")}\"")
                        buildConfigField("String", "DEMO_USERNAME", "\"${forkProp("demo.username", "demo")}\"")
                        buildConfigField("String", "DEMO_PASSWORD", "\"${forkProp("demo.password", "demo")}\"")
                    }
                    register("prod") {
                        dimension.set("contentType")
                        buildConfigField("Boolean", "IS_DEMO_BUILD", "false")
                        buildConfigField("String", "BASE_URL", "\"${forkProp("network.base.url.prod", "https://api.example.com")}\"")
                        buildConfigField("String", "DEMO_USERNAME", "\"\"")
                        buildConfigField("String", "DEMO_PASSWORD", "\"\"")
                    }
                }

                buildTypes {
                    register("debug") {
                        isDefault.set(true)
                        isDebuggable.set(true)
                        applicationIdSuffix.set(".debug")
                        buildConfigField("Boolean", "ENABLE_LOGGING", "true")
                        buildConfigField("Boolean", "SHOW_DEBUG_OVERLAY", "true")
                        buildConfigField("String", "LOG_TAG", "\"${forkProp("log.tag", "KMPTemplate")}-DEBUG\"")
                    }
                    register("staging") {
                        isDebuggable.set(false)
                        applicationIdSuffix.set(".staging")
                        buildConfigField("Boolean", "ENABLE_LOGGING", "true")
                        buildConfigField("Boolean", "SHOW_DEBUG_OVERLAY", "false")
                        buildConfigField("String", "LOG_TAG", "\"${forkProp("log.tag", "KMPTemplate")}-STAGING\"")
                    }
                    register("release") {
                        isDebuggable.set(false)
                        isMinifyEnabled.set(true)
                        buildConfigField("Boolean", "ENABLE_LOGGING", "false")
                        buildConfigField("Boolean", "SHOW_DEBUG_OVERLAY", "false")
                        buildConfigField("String", "LOG_TAG", "\"${forkProp("log.tag", "KMPTemplate")}\"")
                    }
                }

                // Consumer extension hook — must be the LAST statement so the
                // local file sees the fully-populated extension.
                LocalFlavorsLoader.applyIfPresent(this, target)
            }

            // iOS xcconfig generation + variants.json export (generateIosFlavorXcconfigs /
            // kmpFlavorsBootstrapXcode / exportKmpFlavorsManifest) are registered by the
            // kmp-product-flavors plugin itself, driven by the ios* DSL flags set above —
            // no hand-maintained build-logic tasks. (Was: registerIosFlavorXcconfigsTask() +
            // registerExportKmpFlavorsManifestTask(), removed in the 2.8.3 adoption.)
        }
    }
}
