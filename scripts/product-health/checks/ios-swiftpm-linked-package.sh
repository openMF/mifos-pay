#!/usr/bin/env bash
# checks/ios-swiftpm-linked-package.sh — cmp-ios/KotlinMultiplatformLinkedPackage MUST stay
# committed, and its shim version MUST track the resolved GitLive version.
#
# What this tree is: KGP synthesises a SwiftPM package per native SPM dependency declared in a
# klib's cinterop metadata (GitLive publishes firebase-*-iosArm64Cinterop-swiftPMImportMain klibs
# that pull firebase-ios-sdk). Xcode references the umbrella as an XCLocalSwiftPackageReference.
#
# Why it must be COMMITTED (measured 2026-09-08, not assumed):
#   Gradle generates this tree into cmp-shared/build/kotlin/swiftImport + .swiftpm-locks/ — it
#   NEVER writes cmp-ios/. Deleting cmp-ios/KotlinMultiplatformLinkedPackage and running the
#   generate tasks did not recreate it, and `xcodebuild` then failed with exit 74:
#     "Could not resolve package dependencies: the package at '…/KotlinMultiplatformLinkedPackage'
#      cannot be accessed (… doesn't exist in file system)"
#   Xcode resolves the package graph BEFORE any Gradle task runs, so a fresh clone that lacks this
#   directory cannot build iOS at all. Gitignoring it is therefore a fork-breaking change.
#
# Why it goes STALE: nothing regenerates it, so a GitLive bump (e.g. the KmpToolkit 3.5.21 move
# from 3.0.0-alpha01 → alpha02) leaves the shim names pinned to the OLD version while the klibs
# advance. That drift is invisible — the build stays green — which is exactly why it needs a gate.
#
# Invariants:
#   SLP-1  if project.pbxproj declares the XCLocalSwiftPackageReference, the directory exists AND
#          is tracked by git (a fresh clone must receive it).
#   SLP-2  every dev_gitlive_* shim's version token matches the dev.gitlive version resolved in the
#          committed dependency baseline (cmp-android/dependencies/*.tree.txt). Regenerate both
#          together: ./gradlew dependencyGuardBaseline, then rename the shim dirs/targets to match.
#
# exit 0 = PASS · 1 = FAIL (blocks). No cmp-ios project / no reference → PASS (nothing to guard).
set -uo pipefail
# shellcheck source=scripts/product-health/lib.sh
source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib.sh"
: "${HEALTH_ROOT:?ios-swiftpm-linked-package: HEALTH_ROOT not set (run via product-health.sh)}"

PBX="$HEALTH_ROOT/cmp-ios/iosApp.xcodeproj/project.pbxproj"
PKG_DIR="$HEALTH_ROOT/cmp-ios/KotlinMultiplatformLinkedPackage"
REL="cmp-ios/KotlinMultiplatformLinkedPackage"

[ -f "$PBX" ] || { echo "no cmp-ios Xcode project — nothing to guard (ok)"; exit 0; }
if ! grep -q 'XCLocalSwiftPackageReference "KotlinMultiplatformLinkedPackage"' "$PBX"; then
  echo "pbxproj declares no KotlinMultiplatformLinkedPackage reference — nothing to guard (ok)"
  exit 0
fi

fail=0

# SLP-1 — present on disk AND tracked (a fresh clone must get it, or xcodebuild exits 74).
if [ ! -d "$PKG_DIR" ]; then
  echo "${C_RED}✗ SLP-1${C_RST}: $REL is MISSING but project.pbxproj references it —"
  echo "       xcodebuild will fail 'Could not resolve package dependencies' (exit 74)."
  fail=1
else
  tracked=$(git -C "$HEALTH_ROOT" ls-files "$REL" 2>/dev/null | wc -l | tr -d ' ')
  if [ "$tracked" -eq 0 ]; then
    echo "${C_RED}✗ SLP-1${C_RST}: $REL exists but is NOT tracked by git (gitignored?) —"
    echo "       Gradle never regenerates it at this path, so a fresh clone cannot build iOS."
    fail=1
  fi
fi

# SLP-2 — shim version token must match the resolved dev.gitlive version.
shims=$(find "$PKG_DIR/subpackages" -maxdepth 1 -type d -name 'dev_gitlive_*' 2>/dev/null | wc -l | tr -d ' ')
if [ "${shims:-0}" -eq 0 ]; then
  echo "  (SLP-2 skipped: no dev_gitlive_* shims present — nothing to version-match)"
else
  want=$(grep -rhoE 'dev\.gitlive:firebase-[a-z]+:[0-9][^ ]*' \
           "$HEALTH_ROOT"/cmp-android/dependencies/*.tree.txt 2>/dev/null \
         | sed -E 's/.*:([0-9].*)$/\1/' | sort -u)
  n_want=$(printf '%s\n' "$want" | grep -c . || true)
  if [ "$n_want" -eq 0 ]; then
    echo "${C_RED}✗ SLP-2${C_RST}: $shims dev_gitlive_* shim(s) present but NO dev.gitlive version found"
    echo "       in cmp-android/dependencies/*.tree.txt — cannot verify staleness. Run:"
    echo "         ./gradlew dependencyGuardBaseline"
    fail=1
  elif [ "$n_want" -gt 1 ]; then
    echo "${C_RED}✗ SLP-2${C_RST}: dependency baseline resolves MULTIPLE dev.gitlive versions:"
    printf '%s\n' "$want" | sed 's/^/       /'
    fail=1
  else
    tok="$(printf '%s' "$want" | tr '.-' '__')"   # 3.0.0-alpha02 -> 3_0_0_alpha02
    bad=0
    while IFS= read -r d; do
      [ -n "$d" ] || continue
      case "$(basename "$d")" in
        *"_$tok") : ;;
        *) echo "${C_RED}✗ SLP-2${C_RST}: shim '$(basename "$d")' does not match resolved dev.gitlive $want"; bad=1 ;;
      esac
    done <<< "$(find "$PKG_DIR/subpackages" -maxdepth 1 -type d -name 'dev_gitlive_*' 2>/dev/null)"
    if [ "$bad" -eq 1 ]; then
      echo "       The shim tree is STALE. Rename the subpackage dirs, their Sources/<target>/,"
      echo "       the .m/.h files, the internal Package.swift ids and the umbrella's"
      echo "       .package/.product refs to _$tok — then rebuild iOS to verify."
      fail=1
    fi
  fi
fi

if [ "$fail" -eq 0 ]; then
  echo "linked SwiftPM package tracked + version-matched (dev.gitlive ${want:-n/a}, $shims shim(s))"
fi
exit "$fail"
