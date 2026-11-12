/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mobile-wallet/blob/master/LICENSE.md
 */
import com.mobilebytelabs.kmpflavors.KmpFlavorExtension
import org.convention.forkProp
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotlin.serialization)
    // Kover applied explicitly (cmp-desktop doesn't use a base convention plugin
    // that would chain it — unlike feature/*, core/*, cmp-shared, cmp-navigation,
    // cmp-android which get kover via Android/KMP/CMP convention plugins).
    alias(libs.plugins.kover.convention)
    alias(libs.plugins.kmp.flavors.convention)
}

kotlin {
    jvm()

    jvmToolchain(libs.versions.jvmToolchain.get().toInt())

    sourceSets {
        jvmMain.dependencies {
            implementation(projects.cmpShared)

            implementation(libs.kotlinx.coroutines.swing)
            implementation(compose.desktop.currentOs)
            implementation(libs.jb.kotlin.stdlib)
            implementation(libs.kotlin.reflect)

            implementation(libs.koin.core)
        }
    }
}

val appName: String = libs.versions.desktopAppName.get()
val packageNameSpace: String = libs.versions.appId.get()
val appVersion: String = libs.versions.desktopPackageVersion.get()

// Fork identity tokens — sourced from gradle/fork.properties (RULE-WORKSPACE-ORG-IDENTITY-001 WOI-3).
// NEVER hardcode the template's org identity here — a fork that doesn't set org.name/org.copyright
// in fork.properties must NOT ship a brand name. Last-resort default is the fork's OWN appName
// (libs.versions.desktopAppName, always fork-set) — never the template's org (matches the fastlane
// lanes' no-mifos-fallback). fork-identity.sh still FAILs CI if org.name is left at the template value.
// Read through the ONE Gradle-side reader (org.convention.KptAppProfileProperties). Hoisted to top-level
// vals because the consumers below sit inside nested DSL blocks whose receiver is not Project.
val forkOrgName = forkProp("org.name", appName)
val forkDescription = forkProp("app.description", "$appName desktop")
val forkCopyright = forkProp("org.copyright", "© 2026 $forkOrgName. All rights reserved.")
val forkMacCategory = forkProp("mac.app.category", "public.app-category.finance")

// macOS CFBundleVersion (the *build* number, distinct from the marketing version).
// TestFlight / App Store REJECT an upload whose CFBundleVersion is not strictly greater
// than the previously uploaded one. Compose Desktop leaves `packageBuildVersion` unset by
// default, so CFBundleVersion falls back to `packageVersion` (e.g. "1.0.0") on every build —
// the 2nd+ macOS TestFlight upload then 409s ("bundle version must be higher than … '1.0.0'").
// Derive a monotonic build number so each upload is unique + increasing:
//   1. explicit `-PmacBuildVersion=<n>` (lets a lane / local build override), else
//   2. the CI run number (`GITHUB_RUN_NUMBER`, monotonic per workflow run) as `1.0.<n>`, else
//   3. fall back to `appVersion` (local DMG builds are never uploaded to Apple).
// The marketing version (`packageVersion` / CFBundleShortVersionString) is unchanged.
val macBuildVersion: String =
    (findProperty("macBuildVersion") as? String)?.takeIf { it.isNotBlank() }
        ?: providers.environmentVariable("GITHUB_RUN_NUMBER").orNull
            ?.takeIf { it.isNotBlank() }?.let { "1.0.$it" }
        ?: appVersion

// Resolve the active flavor (Gradle property -PkmpFlavor=demo|prod; falls back to DSL default).
val kmpFlavorExt = extensions.getByType<KmpFlavorExtension>()
val activeFlavor: String = (findProperty("kmpFlavor") as? String)
    ?: kmpFlavorExt.flavors.find { it.isDefault.getOrElse(false) }?.name
    ?: "prod"
val activeFlavorConfig = kmpFlavorExt.flavors.findByName(activeFlavor)

val windowTitle = appName + (activeFlavorConfig?.desktopWindowTitleSuffix?.orNull ?: "")
val macBundleId = packageNameSpace + (activeFlavorConfig?.applicationIdSuffix?.orNull ?: "")

compose.desktop {
    application {
        mainClass = "MainKt"
        // Use the same JDK that compiled cmp-shared (toolchain version 21).
        // The plugin defaults to the Gradle daemon's java.home (JDK 17 on this machine),
        // which can't load Java 21 bytecode from cmp-shared at runtime.
        javaHome = javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(libs.versions.jvmToolchain.get().toInt()))
        }.get().metadata.installationPath.asFile.absolutePath
        jvmArgs("-Dapp.name=$windowTitle")
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Pkg, TargetFormat.Msi, TargetFormat.Exe, TargetFormat.Deb)
            packageName = windowTitle
            packageVersion = appVersion
            description = forkDescription
            copyright = forkCopyright
            vendor = forkOrgName
            licenseFile.set(project.file("../LICENSE"))
            includeAllModules = true

            macOS {
                bundleID = macBundleId
                // CFBundleVersion — must strictly increase per TestFlight/App Store upload.
                // See `macBuildVersion` above for the derivation + why this is required.
                packageBuildVersion = macBuildVersion
                dockName = windowTitle
                // LSApplicationCategoryType — token-driven (fork.properties), else Compose writes "Unknown"
                // and altool rejects with 90249. Per RULE-WORKSPACE-ORG-IDENTITY-001 WOI-3.
                appCategory = forkMacCategory
                iconFile.set(project.file("icons/ic_launcher.icns"))
                // Mac App Store signing.
                // MAC_SIGNING_IDENTITY: identity string passed by Fastlane lane via -P property.
                // MAC_KEYCHAIN_PATH: explicit keychain file path from the lane's temp keychain.
                // Identity format: "Apple Distribution: Org Name (TEAMID)"
                // Signing is skipped when MAC_SIGNING_IDENTITY is absent/empty (e.g. local DMG).
                val macSigningId = (project.findProperty("MAC_SIGNING_IDENTITY") as? String)
                    ?.takeIf { it.isNotEmpty() }
                    ?: providers.environmentVariable("MAC_SIGNING_IDENTITY").orNull
                        ?.takeIf { it.isNotEmpty() }
                // Explicit keychain path passed by Fastlane lane (MAC_KEYCHAIN_PATH).
                // Bypasses the execOperations.exec() keychain-search-list issue in
                // Gradle 9.x: security find-certificate with an explicit path works
                // even when the search-list lookup fails from a subprocess context.
                val macKeychainPath = (project.findProperty("MAC_KEYCHAIN_PATH") as? String)
                    ?.takeIf { it.isNotEmpty() }
                    ?: providers.environmentVariable("MAC_KEYCHAIN_PATH").orNull
                        ?.takeIf { it.isNotEmpty() }
                signing {
                    sign.set(macSigningId != null)
                    identity.set(macSigningId ?: "")
                    if (macKeychainPath != null) {
                        keychain.set(macKeychainPath)
                    }
                }
                // Sandbox entitlements required for Mac App Store submission.
                entitlementsFile.set(project.file("mac-app-store.entitlements"))
                // Provisioning profile embeds team + distribution cert info.
                // Set MAC_PROVISIONING_PROFILE_PATH to the .provisionprofile file path.
                // Capture projectDirectory eagerly (config-cache serializable Directory value)
                // so the .map{} lambda never captures an implicit Project reference.
                val projectDirectory = layout.projectDirectory
                provisioningProfile.set(
                    providers.environmentVariable("MAC_PROVISIONING_PROFILE_PATH")
                        .map { projectDirectory.file(it) },
                )
                notarization {
                    val providers = project.providers
                    appleID.set(providers.environmentVariable("NOTARIZATION_APPLE_ID"))
                    password.set(providers.environmentVariable("NOTARIZATION_PASSWORD"))
                    teamID.set(providers.environmentVariable("NOTARIZATION_TEAM_ID"))
                }
            }

            windows {
                menuGroup = windowTitle
                shortcut = true
                dirChooser = true
                perUserInstall = true
                iconFile.set(project.file("icons/ic_launcher.ico"))
            }

            linux {
                modules("jdk.security.auth")
                iconFile.set(project.file("icons/ic_launcher.png"))
            }
        }
        buildTypes.release.proguard {
            configurationFiles.from(file("compose-desktop.pro"))
            obfuscate.set(true)
            optimize.set(true)
        }
    }
}
