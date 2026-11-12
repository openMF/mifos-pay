#!/usr/bin/env bash
# Canary for migration-ledger.sh (LG-1..LG-7). One GREEN leg, seven REDs — one per invariant.
#
# The ledger's invariants are the kind that erode silently: the tree still compiles and the schema
# JSON still matches while a renumbered row strands every installed device. Each RED reproduces one
# real failure rather than a synthetic one.
set -uo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
CHECK="$HERE/../../checks/migration-ledger.sh"
rc=0
# A RED leg must fail on ITS OWN invariant. Exit-code-only assertions let a leg pass on an unrelated
# cascade — that is exactly how LG-2 (the append-only check, the one that strands installed devices)
# went completely untested while this canary reported green: its override never reached the script,
# so red-renumber was failing on LG-1/LG-6 instead.
leg_out() {
  local d="$HERE/$1"; shift
  export LG_HEAD
  HEALTH_ROOT="$d" \
  LG_LEDGER="$d/app-profile/migration-ledger.yaml" \
  LG_UNITS="$d/core/database/migration-units.yaml" \
  LG_CFG="$d/core/database/src/commonMain/kotlin/kpt/core/database/config/ForkDatabaseConfig.kt" \
  LG_DB="$d/core/database/src/commonMain/kotlin/kpt/core/database/AppDatabase.kt" \
  "$@" bash "$CHECK" 2>&1
}
# $1=leg $2=the check id this leg exists to trip
expect_fail_on() {
  local out; out="$(leg_out "$1")"
  if echo "$out" | grep -q "❌ $2"; then
    echo "  ✓ $1 failed on $2"
  else
    echo "  ✗ $1 did not fail on $2 — it reported: $(echo "$out" | grep -oE '❌ [A-Z]+-[0-9]' | sort -u | tr '\n' ' ')"
    rc=1
  fi
}
LG_HEAD=""   # these legs have no committed predecessor; LG-2 correctly skips
echo "── RED legs (each must fail on its OWN invariant) ──"
expect_fail_on red-gap         LG-1
expect_fail_on red-dup         LG-3
expect_fail_on red-destructive LG-4
expect_fail_on red-version     LG-5
# LG-6 had NO leg, which is how it went vacuous: AppDatabase stopped being committed source, its
# File.exist? guard turned false, and the check silently stopped running while the suite stayed green.
expect_fail_on red-lg6         LG-6
# LG-7: the fused-id ledger remove-demo.sh used to write (`paste -d', '` cycles its delimiters).
expect_fail_on red-baseline    LG-7
# LG-2 needs a committed ledger to diff against; feed it explicitly.
LG_HEAD="$(cat "$HERE/red-renumber/head-ledger.yaml")"
expect_fail_on red-renumber LG-2
echo "── GREEN (expect PASS) ──"
LG_HEAD=""
if leg_out green >/dev/null 2>&1; then echo "  ✓ GREEN passed"; else echo "  ✗ GREEN failed"; rc=1; fi
echo ""; [ $rc = 0 ] && echo "CANARY: ✅ PASS" || echo "CANARY: ❌ FAIL"; exit $rc
