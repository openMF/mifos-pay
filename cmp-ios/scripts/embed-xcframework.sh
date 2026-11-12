#!/usr/bin/env bash
#
# embed-xcframework.sh — flavor-aware KMP framework embed for Xcode (E6).
#
# Invoked as an Xcode Run-Script build phase on the `iosApp` target (see
# iosApp.xcodeproj/project.pbxproj → PBXShellScriptBuildPhase "[KMP] Embed and
# Sign ComposeApp XCFramework"). It runs BEFORE Compile Sources so the exported
# `ComposeApp` framework is built, code-signed and copied into the app bundle for
# every build. This is the SwiftPM/XCFramework embed path; the framework is linked
# from the XCFramework this script assembles, not from any package-manager phase.
#
# ── Flavor-aware build-type mapping ──
# Every `{flavor}{BuildType}` Xcode configuration (demoDebug, demoStaging,
# demoRelease, prodDebug, prodStaging, prodRelease — from the kmp-product-flavors DSL)
# maps to a Kotlin/Native build type: a *debuggable* build type → DEBUG, everything
# else → RELEASE. That selection is derived here from Xcode's `$CONFIGURATION`:
#     *Debug  -> Debug   (debuggable)
#     *        -> Release (Staging + Release are optimized/non-debuggable)
# The KMP `embedAndSignAppleFrameworkForXcode` task only understands the canonical
# `Debug`/`Release` configuration names, so we hand it the mapped name via the
# `CONFIGURATION` env var — that is what makes per-variant iOS builds work.
set -euo pipefail

# SRCROOT is exported by Xcode (…/cmp-ios). The Gradle wrapper lives at the repo root,
# one level up from cmp-ios. Fall back to a relative resolve when run outside Xcode.
SRCROOT="${SRCROOT:-$(cd "$(dirname "$0")/.." && pwd)}"
REPO_ROOT="$(cd "$SRCROOT/.." && pwd)"
GRADLEW="$REPO_ROOT/gradlew"

CONFIG="${CONFIGURATION:-prodRelease}"
case "$CONFIG" in
  *Debug|*debug) KOTLIN_BUILD_TYPE="Debug" ;;   # debuggable → DEBUG slice
  *)             KOTLIN_BUILD_TYPE="Release" ;;  # Staging + Release → RELEASE slice
esac

echo "note: [KMP] embed-xcframework — Xcode CONFIGURATION=$CONFIG → Kotlin build type $KOTLIN_BUILD_TYPE"

if [ ! -x "$GRADLEW" ]; then
  echo "error: gradlew not found or not executable at $GRADLEW" >&2
  exit 1
fi

# A module that cinterops a pod (the Kotlin cocoapods plugin) runs a nested `xcodebuild` from its
# podBuild* tasks, and xcodebuild reads environment variables as build settings. Xcode exports the app
# target's EMBED_PACKAGE_RESOURCE_BUNDLE_NAMES (the SwiftPM Firebase_*.bundle list) to this script, so
# it leaked into the pod's resource-bundle target and failed it ("Firebase_FirebaseCore.bundle couldn't
# be opened" — mbs/reels-downloader, GoogleMobileAds/UMP). Gradle never needs it.
unset EMBED_PACKAGE_RESOURCE_BUNDLE_NAMES

# The app consumes ComposeApp as a SwiftPM BINARY TARGET (cmp-ios/Package.swift → the XCFramework at
# cmp-shared/build/XCFrameworks/<type>/ComposeApp.xcframework). Kotlin REJECTS
# `embedAndSignAppleFrameworkForXcode` — the direct-integration task — the moment the Xcode project
# carries SwiftPM dependencies ("error: You have SwiftPM dependencies with embedAndSign integration"),
# which it now does: the native Firebase SDK + the ComposeApp binary target itself. So we ASSEMBLE the
# XCFramework the binary target points at; Xcode/SwiftPM embed + sign the binary product automatically
# when resolving the package. This runs BEFORE Compile Sources so the freshly-built framework is what
# links. (Debug/Release maps from Xcode's flavored $CONFIGURATION above.)
# ── FAST PATH: a Debug build needs ONE architecture, not an XCFramework ──────────────────────
# An XCFramework contains every slice BY DEFINITION, so assembleComposeApp*XCFramework compiles BOTH
# iosArm64 (device) and iosSimulatorArm64 even when the destination is a simulator. Measured
# 2026-09-16 on this project: 11m26s of Gradle / 1576 tasks inside ONE xcodebuild, dominated by
# checkIosArm64* + the device-arch Kotlin/Native compile that a simulator run never loads.
#
# The app links ComposeApp from FRAMEWORK_SEARCH_PATHS (see the staging block below), NOT as a
# SwiftPM product — only the Firebase SDK is. So a Debug build links the SINGLE arch matching
# $SDK_NAME and stages it directly, skipping the other arch and the fat/XCFramework packaging.
# That is the same principle `embedAndSignAppleFrameworkForXcode` applies (build what $SDK_NAME /
# $ARCHS asks for); it is hand-rolled here only because Kotlin REJECTS embedAndSign once the project
# carries SwiftPM dependencies, as explained above.
#
# RELEASE still assembles the full XCFramework: Package.swift's binaryTarget and the deploy lanes
# consume it, and a distribution build must carry every slice.
FAST_SINGLE_ARCH=0
if [ "$KOTLIN_BUILD_TYPE" = "Debug" ] && [ "${KMP_FORCE_XCFRAMEWORK:-0}" != "1" ]; then
  case "${SDK_NAME:-iphoneos}" in
    *simulator*) KMP_ARCH_TARGET="IosSimulatorArm64"; KMP_ARCH_DIR="iosSimulatorArm64" ;;
    *)           KMP_ARCH_TARGET="IosArm64";          KMP_ARCH_DIR="iosArm64" ;;
  esac
  echo "note: [KMP] fast path — linking ${KMP_ARCH_TARGET} only (KMP_FORCE_XCFRAMEWORK=1 to assemble the full XCFramework)"
  # CONFIG-CACHE-SAFE (the single biggest cost, measured 2026-09-16). Without these four
  # exclusions Gradle reports "no cached configuration is available" on EVERY Xcode build and
  # re-executes ~1367 tasks -> 8m14s, even when nothing changed. The worker-kmp codegen tasks
  # capture `Project`, which discards the configuration cache. `gradle-fast-build-guard.sh` refuses
  # a raw build for exactly this reason, but an Xcode Run Script phase never passes through that
  # guard — so the exclusions have to be here, matching what /idea-build-kmp runs.
  # Exclude the WHOLE workerKmpAppCodegen family, never a subset. Each of these captures
  # `Project`, so ONE unexcluded variant discards the configuration cache for the entire build.
  # This list named only Android+AutoShim, leaving Ios/Desktop/Web in — so every iOS build
  # reconfigured from cold (measured 9m28s vs 2m08s once Ios was added) while reporting nothing
  # but "Configuration cache entry discarded because incompatible task was found".
  #
  # These comments sit ABOVE the command, never inside its `\` continuation. A comment line spliced
  # in by a trailing backslash ENDS the command — bash silently dropped every `-x` below it, then ran
  # the leftover `-x …` line as a command ("-x: command not found", exit 127). Under `set -e` that
  # failed the Xcode Run Script phase AFTER a full 7-minute link, with no Gradle error to show for it,
  # while ALSO discarding the very configuration cache these lines exist to preserve.
  # The aggregate-resources task runs WITH the link: the raw framework has no composeResources, and the
  # staging below takes them from this task's output. Linking alone left that output to whatever an
  # earlier full XCFramework assembly happened to leave behind — on a machine that never ran one, the
  # resources were absent and the guard below failed every fast-path build (measured 2026-10-25).
  "$GRADLEW" -p "$REPO_ROOT" ":cmp-shared:link${KOTLIN_BUILD_TYPE}Framework${KMP_ARCH_TARGET}" \
    ":cmp-shared:${KMP_ARCH_DIR}AggregateResources" \
    -x :cmp-shared:workerKmpAppCodegenAll -x :cmp-shared:workerKmpAppCodegenAndroid \
    -x :cmp-shared:workerKmpAppCodegenAutoShim -x :cmp-shared:workerKmpAppCodegenDesktop \
    -x :cmp-shared:workerKmpAppCodegenIos -x :cmp-shared:workerKmpAppCodegenWeb \
    -x :sync:workerKmpAppCodegenAll -x :sync:workerKmpAppCodegenAndroid \
    -x :sync:workerKmpAppCodegenAutoShim -x :sync:workerKmpAppCodegenDesktop \
    -x :sync:workerKmpAppCodegenIos -x :sync:workerKmpAppCodegenWeb
  FAST_SINGLE_ARCH=1
else
  "$GRADLEW" -p "$REPO_ROOT" \
    ":cmp-shared:assembleComposeApp${KOTLIN_BUILD_TYPE}XCFramework" \
    -x :cmp-shared:workerKmpAppCodegenAll -x :cmp-shared:workerKmpAppCodegenAndroid \
    -x :cmp-shared:workerKmpAppCodegenAutoShim -x :cmp-shared:workerKmpAppCodegenDesktop \
    -x :cmp-shared:workerKmpAppCodegenIos -x :cmp-shared:workerKmpAppCodegenWeb \
    -x :sync:workerKmpAppCodegenAll -x :sync:workerKmpAppCodegenAndroid \
    -x :sync:workerKmpAppCodegenAutoShim -x :sync:workerKmpAppCodegenDesktop \
    -x :sync:workerKmpAppCodegenIos -x :sync:workerKmpAppCodegenWeb
fi

# Stage the SDK-matching `.framework` slice OUT of the freshly-assembled XCFramework into the
# project's `FRAMEWORK_SEARCH_PATHS`
#   $(SRCROOT)/../cmp-shared/build/xcode-frameworks/$(CONFIGURATION|KMPF_VARIANT)/$(SDK_NAME)
# — the app links `ComposeApp` via those search paths (it is NOT a SwiftPM package product; only the
# Firebase SDK is), so this is what makes `import ComposeApp` resolve at LINK time now that embedAndSign
# (which used to write that dir) is gone. Assemble-then-stage works for every flavor because $CONFIG is
# the raw Xcode configuration. (bash 3.2 on the Xcode runner: lowercase via tr, no ${x,,}.)
SDK="${SDK_NAME:-iphoneos}"
TYPE_DIR="$(printf '%s' "$KOTLIN_BUILD_TYPE" | tr '[:upper:]' '[:lower:]')"
XCF="$REPO_ROOT/cmp-shared/build/XCFrameworks/$TYPE_DIR/ComposeApp.xcframework"
if [ "${FAST_SINGLE_ARCH:-0}" = "1" ]; then
  # Fast path: stage straight out of the single-arch link output.
  SRC_FW="$REPO_ROOT/cmp-shared/build/bin/${KMP_ARCH_DIR}/${TYPE_DIR}Framework/ComposeApp.framework"
  [ -d "$SRC_FW" ] || SRC_FW=""
  # The raw link output does NOT contain composeResources — the XCFramework ASSEMBLY folds them in.
  # Staging the bare framework therefore yields a build that succeeds and then dies at the first
  # resource lookup with MissingResourceException. Take the aggregated resources for this arch
  # (verified 2026-09-16: the same 1218-file set the XCFramework slice carries). Their absence is
  # FATAL, not cosmetic, so abort rather than stage a framework that crashes at runtime.
  FAST_RES="$REPO_ROOT/cmp-shared/build/kotlin-multiplatform-resources/aggregated-resources/${KMP_ARCH_DIR}/composeResources"
  if [ -n "$SRC_FW" ] && [ ! -d "$FAST_RES" ]; then
    echo "error: [KMP] fast path: composeResources missing at $FAST_RES — refusing to stage a framework that would crash at runtime. Re-run with KMP_FORCE_XCFRAMEWORK=1." >&2
    exit 1
  fi
else
  case "$SDK" in
    *simulator*) SRC_FW="$(ls -d "$XCF"/*simulator*/ComposeApp.framework 2>/dev/null | head -1)" ;;
    *)           SRC_FW="$(ls -d "$XCF"/ios-arm64/ComposeApp.framework 2>/dev/null | head -1)" ;;
  esac
fi
DST_DIR="$REPO_ROOT/cmp-shared/build/xcode-frameworks/$CONFIG/$SDK"
if [ -n "${SRC_FW:-}" ] && [ -d "$SRC_FW" ]; then
  echo "note: [KMP] staging $(basename "$(dirname "$SRC_FW")") slice → xcode-frameworks/$CONFIG/$SDK for FRAMEWORK_SEARCH_PATHS"
  mkdir -p "$DST_DIR"
  rm -rf "${DST_DIR:?}/ComposeApp.framework"
  cp -a "$SRC_FW" "$DST_DIR/"
  if [ "${FAST_SINGLE_ARCH:-0}" = "1" ] && [ -d "${FAST_RES:-}" ]; then
    cp -a "$FAST_RES" "$DST_DIR/ComposeApp.framework/"
    echo "note: [KMP] fast path — folded in composeResources ($(find "$FAST_RES" -type f | wc -l | tr -d ' ') files)"
  fi
else
  echo "warning: [KMP] no ComposeApp slice for SDK=$SDK under $XCF — FRAMEWORK_SEARCH_PATHS may be unset" >&2
fi
