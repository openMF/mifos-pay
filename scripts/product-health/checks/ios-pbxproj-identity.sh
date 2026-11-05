#!/usr/bin/env bash
# checks/ios-pbxproj-identity.sh — the iOS Xcode project MUST derive its bundle id + provisioning
# profile from Config.xcconfig (which `./gradlew syncForkConfig` writes APP_BUNDLE_ID into), NEVER
# hardcode identity inside cmp-ios/iosApp.xcodeproj/project.pbxproj.
#
# Why this class breaks forks (xcodebuild Exit status: 65):
#   `fastlane gym` enumerates EVERY build configuration in the project to assemble the
#   export_options.provisioningProfiles map — including dead/vestigial base Debug/Release configs.
#   A config that hardcodes a bundle id (the template's own `org.mifos.kmp.template`, or a stale
#   fork id) or a hardcoded `PROVISIONING_PROFILE_SPECIFIER = "match AppStore <id>"` makes gym sign
#   against a profile that was never minted for THIS fork's distribution cert →
#   "Provisioning profile … doesn't include signing certificate …". syncForkConfig only writes
#   Config.xcconfig; it never touches project.pbxproj, so a hardcoded id survives every fork.
#
# Invariant (holds on the template AND every fork — no TEMPLATE_SELF_BUILD skip):
#   PBX-1  project.pbxproj contains NO literal `org.mifos.kmp.template` (identity lives in
#          Config.xcconfig / libs.versions.toml#appId, never in the Xcode project).
#   PBX-2  every PRODUCT_BUNDLE_IDENTIFIER value ∈ { "$(APP_BUNDLE_ID)", "$(inherited)" }.
#   PBX-3  every PROVISIONING_PROFILE_SPECIFIER is empty "" OR references $(APP_BUNDLE_ID) —
#          never a hardcoded profile name (let match / automatic signing resolve it).
#   PBX-4  every DEVELOPMENT_TEAM is "" or "$(TEAM_ID)" — never a literal 10-char Apple team id.
#
# PBX-4 exists because the gap was not hypothetical: this template shipped
# `DEVELOPMENT_TEAM = L432S2FZP5` — one real organisation's Apple team id — to every fork, through
# TWO separate commits (862f2e4a, 99083ed2), and it survived until 2026-09-10. PBX-1..3 all passed
# the whole time; none of them looks at DEVELOPMENT_TEAM. The value reached at least one downstream
# fork whose own team is different, where it would sign against the wrong team.
#
# `team_id` is already a first-class app-profile field with a complete projection chain —
# app-profile/platforms/apple/apple.yaml#apple.team_id → fork.properties#apple.team.id →
# SyncForkConfigPlugin → Config.xcconfig#TEAM_ID → $(TEAM_ID). A literal in the pbxproj bypasses all
# of it, and because syncForkConfig never touches project.pbxproj, nothing downstream corrects it.
#
# exit 0 = PASS · 1 = FAIL (blocks customize + CI). No cmp-ios project → PASS (nothing to guard).
set -uo pipefail
# shellcheck source=scripts/product-health/lib.sh
source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib.sh"
: "${HEALTH_ROOT:?ios-pbxproj-identity: HEALTH_ROOT not set (run via product-health.sh)}"

PBX="$HEALTH_ROOT/cmp-ios/iosApp.xcodeproj/project.pbxproj"
[ -f "$PBX" ] || { echo "no cmp-ios Xcode project — nothing to guard (ok)"; exit 0; }

fail=0

# PBX-1 — no literal template bundle id anywhere in the Xcode project.
if grep -q 'org\.mifos\.kmp\.template' "$PBX"; then
  echo "${C_RED}✗ PBX-1${C_RST}: project.pbxproj hardcodes 'org.mifos.kmp.template' — identity must derive from Config.xcconfig (\$(APP_BUNDLE_ID)):"
  grep -nE 'org\.mifos\.kmp\.template' "$PBX" | sed 's/^/       /'
  fail=1
fi

# PBX-2 — PRODUCT_BUNDLE_IDENTIFIER must be DERIVED from $(APP_BUNDLE_ID), never a literal id.
#
# A SUFFIXED derivation is correct and must pass. Apple requires an app extension's bundle id to be
# a child of its host app's ("$(APP_BUNDLE_ID).widgets"), so any fork shipping a WidgetKit widget,
# share extension, or notification-service extension has one — that is the platform's rule, not a
# fork's shortcut, and it contains no literal. An exact-match-only regex rejected it and would have
# blocked that whole class of fork: measured on mbs/cappy, whose real project carries
# "$(APP_BUNDLE_ID).widgets" and "$(APP_BUNDLE_ID).demo.widgets" for its CappyWidgets target.
#
# The invariant this check exists for is "no hardcoded identity", and a value BUILT FROM the token
# satisfies it. PBX-3 next door already matched on substring for exactly this reason; PBX-2 was the
# inconsistent one.
bad_bid="$(grep -oE 'PRODUCT_BUNDLE_IDENTIFIER = "[^"]*"' "$PBX" \
  | grep -vE 'PRODUCT_BUNDLE_IDENTIFIER = "[^"]*\$\((APP_BUNDLE_ID|inherited)\)[^"]*"' || true)"
if [ -n "$bad_bid" ]; then
  echo "${C_RED}✗ PBX-2${C_RST}: PRODUCT_BUNDLE_IDENTIFIER must derive from \"\$(APP_BUNDLE_ID)\" (a \".suffix\" is fine) or be \"\$(inherited)\", found:"
  printf '%s\n' "$bad_bid" | sort -u | sed 's/^/       /'
  fail=1
fi

# PBX-3 — PROVISIONING_PROFILE_SPECIFIER: empty, or references $(APP_BUNDLE_ID); never hardcoded.
bad_prof="$(grep -oE 'PROVISIONING_PROFILE_SPECIFIER = "[^"]*"' "$PBX" \
  | grep -vE 'PROVISIONING_PROFILE_SPECIFIER = ""|PROVISIONING_PROFILE_SPECIFIER = "[^"]*\$\(APP_BUNDLE_ID\)[^"]*"' || true)"
if [ -n "$bad_prof" ]; then
  echo "${C_RED}✗ PBX-3${C_RST}: PROVISIONING_PROFILE_SPECIFIER must be empty or \$(APP_BUNDLE_ID)-derived, found hardcoded:"
  printf '%s\n' "$bad_prof" | sort -u | sed 's/^/       /'
  fail=1
fi

# PBX-4 — DEVELOPMENT_TEAM must be the $(TEAM_ID) indirection or empty; never a literal team id.
# Apple team ids are exactly 10 uppercase alphanumerics, which is specific enough to match on
# without flagging "" or $(TEAM_ID).
bad_team="$(grep -oE 'DEVELOPMENT_TEAM = [^;]*' "$PBX" \
  | grep -vE 'DEVELOPMENT_TEAM = ""|DEVELOPMENT_TEAM = "?\$\(TEAM_ID\)"?' || true)"
if [ -n "$bad_team" ]; then
  echo "${C_RED}✗ PBX-4${C_RST}: DEVELOPMENT_TEAM must be \"\$(TEAM_ID)\" or empty — found a literal team id:"
  printf '%s\n' "$bad_team" | sort -u | sed 's/^/       /'
  echo "       The team id belongs in app-profile/platforms/apple/apple.yaml#apple.team_id;"
  echo "       ./gradlew syncForkConfig projects it to Config.xcconfig#TEAM_ID."
  fail=1
fi

if [ "$fail" = 0 ]; then
  echo "${C_GRN}✓${C_RST} iOS project derives bundle id + team + provisioning from Config.xcconfig (no hardcoded identity)"
  exit 0
fi
echo "  ↳ fix: on the offending XCBuildConfiguration(s) in cmp-ios/iosApp.xcodeproj/project.pbxproj set"
echo "         PRODUCT_BUNDLE_IDENTIFIER = \"\$(APP_BUNDLE_ID)\"   (an app extension may suffix it: \".widgets\")"
echo "         DEVELOPMENT_TEAM          = \"\$(TEAM_ID)\""
echo "         PROVISIONING_PROFILE_SPECIFIER = \"\""
echo "       The real values come from app-profile — app.yaml / platforms/apple/apple.yaml#apple.team_id —"
echo "       via ./gradlew syncForkConfig, which writes Config.xcconfig. It never edits project.pbxproj,"
echo "       so a literal here is invisible to the SoT and survives every sync."
exit 1
