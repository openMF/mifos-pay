#!/usr/bin/env bash
# Canary for NAP-8 (network-access-points.sh) — RED must FAIL, GREEN must PASS.
#
# RED reproduces the shipped bug: an access point declares `publishable_key_env: MY_SUPABASE_KEY`,
# the codegen emits `BuildKonfig.MY_SUPABASE_KEY` into AppAccessPoints, and NOTHING declares the
# field — `Unresolved reference` on :core:network. GREEN is the same tree with the field present in
# the generated `syncForkConfig:buildkonfig` region.
set -uo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
CHECK="$HERE/../../checks/network-access-points.sh"
rc=0
run_leg_out() {
  local leg="$1"
  HEALTH_ROOT="$HERE/$leg" \
  NAP_YAML="$HERE/$leg/app.yaml" \
  NAP_NET_DIR="$HERE/$leg/core/network/src/commonMain/kotlin/kpt/core/network" \
  NAP_BUILD_FILE="$HERE/$leg/core/network/build.gradle.kts" \
    bash "$CHECK" 2>&1
}
run_leg() { run_leg_out "$1" >/dev/null 2>&1; }
# Assert the SPECIFIC check, not just a non-zero exit: a RED leg that trips some unrelated NAP rule
# would otherwise look like proof while the rule under test never ran.
echo "── RED (expect FAIL on NAP-8) ──"
red_out="$(run_leg_out red)"
if echo "$red_out" | grep -q '❌ NAP-8'; then
  echo "  ✓ RED failed on NAP-8"
else
  echo "  ✗ RED did not fail on NAP-8 — reported: $(echo "$red_out" | grep -oE '❌ NAP-[0-9]+' | sort -u | tr '\n' ' ')"; rc=1
fi
echo "── GREEN (expect PASS) ──"
if run_leg green; then echo "  ✓ GREEN passed"; else echo "  ✗ GREEN failed"; rc=1; fi
echo ""
[ $rc = 0 ] && echo "CANARY: ✅ PASS" || echo "CANARY: ❌ FAIL"
exit $rc
