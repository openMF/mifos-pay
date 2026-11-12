#!/usr/bin/env bash
#
# ci-equivalent-check.sh — Local CI parity for the kmp-project-template consumer.
#
# Mirrors what openMF/mifos-x-actionhub@v1.0.11 pr-check.yaml runs on every PR:
#   1. Static Analysis     — :spotlessCheck + :detekt + :dependencyGuard + build-logic check
#   2. Build Android       — :cmp-android:assembleDemoDebug
#   3. Build Desktop       — :cmp-desktop:packageDistributionForCurrentOS
#   4. Build Web           — :cmp-web:jsBrowserDistribution + :cmp-web:wasmJsBrowserDistribution
#   5. Build iOS           — :cmp-shared:compileKotlinIosArm64 + IosSimulatorArm64
#   6. Badging gate        — :cmp-android:checkProdReleaseBadging
#
# CRITICAL: this script flips `lib-integrate.properties` `.local=true` → `.local=false`
# before running and restores it after. This matches CI's resolution view (Maven
# Central for cmp-network-monitor, kmp-product-flavors, etc.) rather than the
# local-composite-include view (which only works on machines with the sibling
# workspaces/ layout).
#
# Lesson learned the hard way: `:cmp-android:assembleDemoDebug` with the default
# `.local=true` will compile cleanly and pass locally, then break in CI on
# dependency-guard or non-Android targets because the resolution tree differs.
#
# Usage:
#   bash scripts/ci/ci-equivalent-check.sh
#
# Exit code:
#   0 = all gates green (safe to push)
#   1 = at least one gate failed (DO NOT push without fixing)

set -uo pipefail

cd "$(dirname "$0")/../.."

YELLOW='\033[1;33m'
GREEN='\033[1;32m'
RED='\033[1;31m'
NC='\033[0m'

FAILED=()
PASSED=()
RESTORE_NEEDED=0

restore_local_flag() {
  if [[ "$RESTORE_NEEDED" -eq 1 ]]; then
    sed -i.bak 's/\.local=false/.local=true/g' lib-integrate.properties && rm -f lib-integrate.properties.bak
    echo "♻ Restored lib-integrate.properties .local=true"
  fi
}
trap restore_local_flag EXIT

run_gate() {
  local name="$1"
  shift
  echo ""
  printf "${YELLOW}▶ %s${NC}\n" "$name"
  if ./gradlew "$@" --no-daemon --no-configuration-cache --max-workers=2; then
    PASSED+=("$name")
    printf "${GREEN}✓ %s${NC}\n" "$name"
  else
    FAILED+=("$name")
    printf "${RED}✗ %s${NC}\n" "$name"
  fi
}

# Flip to CI view: kmp-toolkit resolves from Maven Central, not composite-include.
echo "🔁 Flipping lib-integrate.properties .local=true → .local=false (CI view)"
sed -i.bak 's/\.local=true/.local=false/g' lib-integrate.properties
rm -f lib-integrate.properties.bak
RESTORE_NEEDED=1

# Gate 1 — Static Analysis Check (mirrors CI's Static Analysis job)
run_gate "spotlessCheck"              spotlessCheck
run_gate "detekt"                     detekt
run_gate "build-logic check"          check -p build-logic
run_gate "dependencyGuard (verify)"   :cmp-android:dependencyGuard

# Gate 2 — Build Android
run_gate ":cmp-android:assembleDemoDebug" :cmp-android:assembleDemoDebug

# Gate 3 — Build Desktop (current OS)
run_gate ":cmp-desktop:packageDistributionForCurrentOS" :cmp-desktop:packageDistributionForCurrentOS

# Gate 4 — Build Web (both JS and Wasm)
run_gate ":cmp-web:jsBrowserDistribution"     :cmp-web:jsBrowserDistribution
run_gate ":cmp-web:wasmJsBrowserDistribution" :cmp-web:wasmJsBrowserDistribution

# Gate 5 — Build iOS (only the targets CI actually builds; iosX64 is deprecated Intel-Mac simulator)
run_gate ":cmp-shared:compileKotlinIosArm64"          :cmp-shared:compileKotlinIosArm64
run_gate ":cmp-shared:compileKotlinIosSimulatorArm64" :cmp-shared:compileKotlinIosSimulatorArm64

# Gate 6 — Badging
run_gate ":cmp-android:checkProdReleaseBadging" :cmp-android:checkProdReleaseBadging

# Summary
echo ""
echo "════════════════════════════════════════════"
echo "Summary"
echo "════════════════════════════════════════════"
for p in ${PASSED[@]+"${PASSED[@]}"}; do printf "  ${GREEN}✓${NC} %s\n" "$p"; done
for f in ${FAILED[@]+"${FAILED[@]}"}; do printf "  ${RED}✗${NC} %s\n" "$f"; done
echo ""
echo "Passed: ${#PASSED[@]}"
echo "Failed: ${#FAILED[@]}"

if [[ ${#FAILED[@]} -gt 0 ]]; then
  echo ""
  printf "${RED}REFUSING TO PUSH — fix failures above first.${NC}\n"
  exit 1
fi

echo ""
printf "${GREEN}All gates green — safe to push.${NC}\n"
exit 0
