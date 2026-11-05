// swift-tools-version:5.9
//
// cmp-ios/Package.swift — SwiftPM binary target for the KMP `ComposeApp` framework (E6).
//
// Declares the Kotlin `ComposeApp` framework as a SwiftPM binary dependency.
// It exposes the Kotlin-exported `ComposeApp` framework — assembled as an XCFramework
// by `cmp-shared/build.gradle.kts` (the `XCFramework("ComposeApp")` DSL) — as a Swift
// package product the `iosApp` target links against.
//
// Producing the binary the `path:` below points at:
//     ./gradlew :cmp-shared:assembleComposeAppReleaseXCFramework   # → build/XCFrameworks/release/ComposeApp.xcframework
//     ./gradlew :cmp-shared:assembleComposeAppDebugXCFramework     # → build/XCFrameworks/debug/ComposeApp.xcframework
// (the umbrella `assembleComposeAppXCFramework` builds both). The path here resolves
// the RELEASE slice; SwiftPM binary targets require the artifact to exist at resolve
// time, so assemble it before an SPM-driven build/archive.
//
// For day-to-day Xcode builds the app does NOT rely on SPM resolution — the
// `[KMP] Embed and Sign ComposeApp XCFramework` Run-Script build phase
// (`cmp-ios/scripts/embed-xcframework.sh`) assembles, signs and embeds the
// flavor-matched framework on every build. This manifest gives forks that prefer a
// pure-SwiftPM consumption (or a Swift package that itself depends on ComposeApp) a
// first-class binary target, with no Ruby toolchain in the loop.
import PackageDescription

let package = Package(
    name: "ComposeApp",
    platforms: [
        .iOS(.v16),
    ],
    products: [
        .library(name: "ComposeApp", targets: ["ComposeApp"]),
    ],
    targets: [
        .binaryTarget(
            name: "ComposeApp",
            path: "../cmp-shared/build/XCFrameworks/release/ComposeApp.xcframework"
        ),
    ]
)
