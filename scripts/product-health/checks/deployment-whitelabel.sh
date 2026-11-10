#!/usr/bin/env bash
# checks/deployment-whitelabel.sh — the white-label boundary between the fork-owned deployment SoT
# (app-profile/) and the template-owned deployment LOGIC (deployment/**) must hold.
#
# app-profile/ carries ALL fork-owned deployment data (identity, org, store copy, distribution ids,
# media). deployment/** is template logic that READS it via _shared/config.rb (AppProfile.get). This
# check asserts the seam: (B1) the SoT exists + is authored for THIS fork, (B2) no template org
# identity leaked as a LITERAL into template-owned deployment logic (must be tokenized), (B3) any
# unset distribution placeholder is surfaced, (B4) the per-platform media dirs are present, (B5) every
# app.yaml#targets platform intent maps to a DEPLOYMENT_MANIFEST platform (no orphan intent), (B6) no
# duplicate/competing Android metadata root shadows the canonical deployment/android/metadata tree,
# (B7) the MANAGED store schema (structured keys — app.yaml#store.*, platforms/<p>/*.yaml, incl.
# trade_representative) is authored + placeholder-free, (B8) a fork must not carry the template's own
# default store copy in that schema, (B9) deployment/**/metadata (DERIVED by syncForkConfig from the
# keys) must not have drifted from the app-profile key SoT.
#
# exit 0 PASS · 1 FAIL (B1/B2/B7/B8 block) · 2 WARN (B3/B4/B5/B6/B9 need attention, non-blocking).
# DIRECTIONAL, placeholder-aware (pure-white-label-100 WS6): B1 (app_id) + B3 (Firebase / bundle /
# display placeholders) + B7 (store marker) load the single-source vocabulary
# (core/registries/WHITE_LABEL_PLACEHOLDERS.yaml, resolved by lib.sh) instead of the old blanket
# TEMPLATE_SELF_BUILD=1 skip. On the neutral template every placeholder-typed field MUST match a
# declared placeholder (real-brand leak → FAIL); on a fork NO placeholder-typed field may still match
# (unfilled placeholder → FAIL). RULE-TEMPLATE-MODULE-FIX-UPSTREAM-001 identity-field generalization.
set -uo pipefail
# shellcheck source=scripts/product-health/lib.sh
source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib.sh"

: "${HEALTH_ROOT:?deployment-whitelabel: HEALTH_ROOT not set (run via product-health.sh)}"
is_template="${TEMPLATE_SELF_BUILD:-0}"

fail=0; warn=0
APP_YAML="$HEALTH_ROOT/app-profile/app.yaml"

# ── B1 — the fork-owned SoT exists, parses, and carries a DIRECTIONALLY-correct app id ────
# (uses WHITE_LABEL_PLACEHOLDERS.yaml#bundle_id: template MUST be a placeholder, fork MUST be authored)
if [ ! -f "$APP_YAML" ]; then
  echo "❌ B1: app-profile/app.yaml is missing — the fork-owned white-label deployment SoT must exist."
  fail=1
else
  app_id="$(ruby -ryaml -e 'begin; y=YAML.load_file(ARGV[0]); print((y.dig("identity","app_id")||"").to_s); rescue => e; STDERR.puts e.message; exit 9; end' "$APP_YAML" 2>/dev/null)"
  if [ $? -ne 0 ]; then
    echo "❌ B1: app-profile/app.yaml does not parse as YAML."
    fail=1
  elif [ -z "$app_id" ]; then
    echo "❌ B1: app-profile/app.yaml identity.app_id is empty — set your fork's app id."
    fail=1
  elif wl_matches_any "$app_id" bundle_id; then
    [ "$is_template" != "1" ] && { echo "❌ B1: identity.app_id '$app_id' is a template placeholder (WHITE_LABEL_PLACEHOLDERS.yaml#bundle_id) — set your fork's app id."; fail=1; }
  elif wl_matches_example "$app_id" bundle_id; then
    [ "$is_template" != "1" ] && { echo "❌ B1: identity.app_id '$app_id' is the template's Mifos REFERENCE id — fork must rebrand to its own (WHITE_LABEL_PLACEHOLDERS.yaml#example_identity.bundle_id)."; fail=1; }
  else
    [ "$is_template" = "1" ] && { echo "❌ B1: identity.app_id '$app_id' is a foreign brand on the TEMPLATE — must be the Mifos reference id or a placeholder (WHITE_LABEL_PLACEHOLDERS.yaml#bundle_id / #example_identity.bundle_id)."; fail=1; }
  fi
fi

# ── B2 — no template org identity as a LITERAL in template-owned deployment logic ─
# The value must be TOKENIZED (read from app-profile via config.rb), never hardcoded.
# Comments and metadata .txt / README are excluded.
PATTERNS='Mifos Initiative|org\.mifos\.kmp\.template|mifos-x-web|MifosInitiative\.MoneyToolkit'
b2_hits=()
if [ -d "$HEALTH_ROOT/deployment" ]; then
  while IFS= read -r f; do
    case "$f" in
      *.appxmanifest) stripped="$(sed 's/<!--.*-->//g' "$f")" ;;   # strip XML comments
      *)              stripped="$(sed 's/#.*$//' "$f")" ;;          # strip rb/yaml/yml/toml line comments
    esac
    if grep -qE "$PATTERNS" <<<"$stripped"; then
      b2_hits+=("${f#$HEALTH_ROOT/}")
    fi
  done < <(find "$HEALTH_ROOT/deployment" -type f \
             \( -name 'config.yaml' -o -name 'lane.rb' -o -name 'workflow-snippet.yml' \
                -o -name '*.appxmanifest' -o -name 'wrangler.toml' \) 2>/dev/null)
fi
if [ "${#b2_hits[@]}" -ne 0 ]; then
  echo "❌ B2: template org identity found as a LITERAL in template-owned deployment logic —"
  echo "        these must be TOKENIZED (read from app-profile/ via config.rb), not hardcoded:"
  for h in "${b2_hits[@]}"; do echo "        · $h"; done
  fail=1
fi

# ── B3 — placeholder detection driven by the registry, not hardcoded (directional) ──
# Load firebase_project_number + bundle_id + app_display_name patterns from
# WHITE_LABEL_PLACEHOLDERS.yaml and scan app-profile/ for any match. On a fork an unfilled
# placeholder BLOCKS; on the neutral template the placeholders ARE the intended state (no fail).
b3=()
if [ -d "$HEALTH_ROOT/app-profile" ]; then
  for cat in firebase_project_number bundle_id app_display_name; do
    while IFS= read -r pat; do
      [ -n "$pat" ] || continue
      if grep -rqE "$pat" "$HEALTH_ROOT/app-profile" 2>/dev/null; then
        b3+=("app-profile/ carries placeholder pattern from WHITE_LABEL_PLACEHOLDERS.yaml#$cat: $pat")
      fi
    done < <(wl_placeholders_load "$cat")
  done
fi
if [ "${#b3[@]}" -ne 0 ] && [ "$is_template" != "1" ]; then
  echo "❌ B3: unfilled placeholders in app-profile/ (fork must author):"
  for w in "${b3[@]}"; do echo "        · $w"; done
  fail=1
fi

# ── B4 — per-platform media dirs present (WARN, non-blocking) ─────────────────
missing_media=()
for p in android apple/ios apple/macos web windows ubuntu; do
  [ -d "$HEALTH_ROOT/app-profile/platforms/$p/media" ] || missing_media+=("app-profile/platforms/$p/media")
done
if [ "${#missing_media[@]}" -ne 0 ]; then
  echo "⚠️  B4: expected per-platform media dir(s) missing:"
  for m in "${missing_media[@]}"; do echo "        · $m"; done
  warn=1
fi

# ── B5 — app.yaml#targets platform intent maps to a DEPLOYMENT_MANIFEST platform (WARN) ─
# Every platform a fork toggles in app-profile/app.yaml#targets must correspond to a platform
# block in deployment/DEPLOYMENT_MANIFEST.yaml — no orphan platform intent. macOS, Windows, and
# Ubuntu deploy targets all live UNDER the manifest's single `desktop` platform block (no separate
# windows/linux manifest platform exists), so `macos`/`windows`/`ubuntu` intent all map to `desktop`.
MANIFEST="$HEALTH_ROOT/deployment/DEPLOYMENT_MANIFEST.yaml"
b5=()
if [ -f "$APP_YAML" ] && [ -f "$MANIFEST" ]; then
  target_platforms="$(ruby -ryaml -e 'begin; y=YAML.load_file(ARGV[0]); print((y["targets"]||{}).keys.join(" ")); rescue; end' "$APP_YAML" 2>/dev/null)"
  manifest_platforms="$(ruby -ryaml -e 'begin; y=YAML.load_file(ARGV[0]); print((y["platforms"]||{}).keys.join(" ")); rescue; end' "$MANIFEST" 2>/dev/null)"
  for pk in $target_platforms; do
    look="$pk"
    case "$pk" in macos|windows|ubuntu) look="desktop" ;; esac
    grep -q " $look " <<<" $manifest_platforms " || b5+=("app.yaml#targets.$pk has no matching platform in DEPLOYMENT_MANIFEST.yaml")
  done
fi
if [ "${#b5[@]}" -ne 0 ]; then
  echo "⚠️  B5: orphan platform intent in app-profile/app.yaml#targets (no manifest platform block):"
  for w in "${b5[@]}"; do echo "        · $w"; done
  warn=1
fi

# ── B6 — no duplicate/competing Android metadata root (WARN) ──────────────────
# The canonical Play metadata root is deployment/android/metadata (the lanes' metadata_path). A
# second fastlane-default root at deployment/fastlane/metadata/android competes with it and drifts.
canon_imgs="$HEALTH_ROOT/deployment/android/metadata/en-US/images"
dup_root="$HEALTH_ROOT/deployment/fastlane/metadata/android/en-US"
if [ -d "$canon_imgs" ] && [ -n "$(ls -A "$canon_imgs" 2>/dev/null)" ] \
   && [ -d "$dup_root" ] && [ -n "$(find "$dup_root" -type f 2>/dev/null | head -1)" ]; then
  echo "⚠️  B6: duplicate Android metadata root — both trees are populated:"
  echo "        · deployment/android/metadata/en-US/images (canonical — the lanes' metadata_path)"
  echo "        · deployment/fastlane/metadata/android/en-US (fastlane-default duplicate — remove)"
  warn=1
fi

# ── B7 — the managed store schema is authored + placeholder-free (blocking) ──
# Store copy is MANAGED STRUCTURED KEYS (app.yaml#store.*, platforms/<p>/*.yaml — NOT a file dump).
# syncForkConfig binds those keys → deployment/**/metadata. Verify the schema is filled, not blank/placeholder.
APP_YAML="$HEALTH_ROOT/app-profile/app.yaml"
APPLE_YAML="$HEALTH_ROOT/app-profile/platforms/apple/apple.yaml"
STORE_YAMLS="$APP_YAML $APPLE_YAML $HEALTH_ROOT/app-profile/platforms/android/android.yaml"
if ! grep -qE '^[[:space:]]*title:[[:space:]]*\S' "$APP_YAML" 2>/dev/null; then
  echo "❌ B7: app-profile/app.yaml#store.title is missing or empty (store schema not authored)."; fail=1
fi
# App Store trade-representative contact must be filled when the block is present (Apple review requires it).
if [ -f "$APPLE_YAML" ] && grep -q 'trade_representative:' "$APPLE_YAML" 2>/dev/null; then
  if ! grep -qE '^[[:space:]]*email_address:[[:space:]]*\S' "$APPLE_YAML"; then
    echo "❌ B7: apple.yaml#trade_representative.email_address is empty (App Store review contact incomplete)."; fail=1
  fi
fi
# Placeholder markers loaded from WHITE_LABEL_PLACEHOLDERS.yaml#store_copy_marker (registry-driven,
# not hardcoded). Directional: on a fork an unfilled marker blocks; on the template it's expected.
marker_re="$(wl_placeholders_load store_copy_marker | paste -sd'|' -)"
# shellcheck disable=SC2086
if [ -n "$marker_re" ] && grep -rIlE "$marker_re" $STORE_YAMLS >/dev/null 2>&1; then
  [ "$is_template" != "1" ] && { echo "❌ B7: placeholder marker (WHITE_LABEL_PLACEHOLDERS.yaml#store_copy_marker) in the app-profile store schema — author your copy."; fail=1; }
fi

# ── B8 — the Mifos REFERENCE store copy is the template's committed example; a FORK must replace it ──
# Registry-driven (WHITE_LABEL_PLACEHOLDERS.yaml#example_identity.store_copy_reference). Directional:
# on the template the reference copy IS the intended committed state (no fail); on a fork it is an
# un-rebranded leak that BLOCKS.
ref_re="$(wl_example_load store_copy_reference | paste -sd'|' -)"
# shellcheck disable=SC2086
if [ -n "$ref_re" ] && grep -rIlE "$ref_re" $STORE_YAMLS >/dev/null 2>&1; then
  [ "$is_template" != "1" ] && { echo "❌ B8: the Mifos reference store copy ('Money Toolkit' / 'Mifos Initiative' / 'org.mifos') is present — this fork must author its own listing (WHITE_LABEL_PLACEHOLDERS.yaml#example_identity.store_copy_reference)."; fail=1; }
fi

# ── B9 — deployment/ metadata drifted from the app-profile key SoT (WARN, non-blocking) ──
# deployment/**/metadata is DERIVED from the keys by syncForkConfig; a mismatch means it wasn't re-derived.
dp_name="$HEALTH_ROOT/deployment/ios/appstore/metadata/en-US/name.txt"
sot_title="$(grep -E '^[[:space:]]*title:[[:space:]]*' "$APP_YAML" 2>/dev/null | head -1 | sed -E 's/^[[:space:]]*title:[[:space:]]*//; s/[[:space:]]*#.*$//; s/^["'"'"']//; s/["'"'"']$//')"
if [ -f "$dp_name" ] && [ -n "$sot_title" ] && [ "$(cat "$dp_name")" != "$sot_title" ]; then
  echo "⚠️  B9: deployment/ios/appstore/metadata/en-US/name.txt drifted from app-profile store.title ('$sot_title') — run ./gradlew syncForkConfig."; warn=1
fi

# ── B10 — deliver screenshots must be FLAT per locale (App Store iOS + macOS) (WARN) ──
# deliver globs "<screenshots_path>/<locale>/*.png" and infers each device by image RESOLUTION — it does
# NOT recurse into device subfolders (Deliver::Loader). A <locale>/<device>/NN.png tree is INVISIBLE to
# deliver, so it "successfully uploads all" ZERO screenshots (silent vacuous success — the listing ships
# with no media). syncForkConfig flattens the per-device SoT to <locale>/<device>-NN.png; assert it held.
for base in deployment/ios/appstore/metadata/screenshots deployment/desktop/mac-app-store/metadata/screenshots; do
  d="$HEALTH_ROOT/$base"
  [ -d "$d" ] || continue
  nested="$(find "$d" -mindepth 2 -type d 2>/dev/null | head -1)"
  if [ -n "$nested" ]; then
    echo "⚠️  B10: $base has nested device subfolder(s) (e.g. ${nested#"$HEALTH_ROOT"/}) — deliver reads <locale>/*.png FLAT and would see ZERO screenshots. Run ./gradlew syncForkConfig."; warn=1
  fi
  for loc in "$d"/*/; do
    [ -d "$loc" ] || continue
    cnt="$(find "$loc" -maxdepth 1 -type f \( -iname '*.png' -o -iname '*.jpg' -o -iname '*.jpeg' \) 2>/dev/null | wc -l | tr -d ' ')"
    [ "$cnt" -eq 0 ] && { echo "⚠️  B10: $base/$(basename "$loc") holds 0 flat screenshots — deliver would upload none."; warn=1; }
  done
done

if [ "$fail" -ne 0 ]; then
  echo "→ Fix: author app-profile/app.yaml for your fork + tokenize any hardcoded identity in deployment/**."
  exit 1
fi
if [ "$warn" -ne 0 ]; then
  echo "   (warnings don't block — resolve before releasing.)"
  exit 2
fi
echo "white-label boundary holds: app-profile/ is the authored fork SoT; deployment/** carries no identity literals"
exit 0
