#!/usr/bin/env bash
# run.sh — RED/GREEN canary for checks/web-db-test-realtime.sh.
#
# `red-turbine/` is the harness that actually produced a false verdict on 2026-09-17: turbine's
# `awaitItem()` under `runTest` (virtual time) waiting for an emission that can only arrive from a
# real browser-Worker round trip. It does not fail the test — it wedges the JS event loop, Karma
# drops the browser on a ping timeout, and the result reads as "Room lost the invalidation".
#
# The two `green-*` legs matter as much as the red one. This rule's failure mode is being MUTED:
# turbine over fakes (most of the store suite) and turbine over a real database in `nonWebTest`
# (never compiled for web) are both correct and must stay silent. A gate that flagged them would be
# switched off within a week, and the real trap would come back with it.
#
# The check reads `git ls-files`, so each fixture is staged in its own throwaway git repo — run in
# place, the file list would be THIS repository's and the fixtures would be invisible to it.
set -uo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CHECK="$(cd "$HERE/../../checks" && pwd)/web-db-test-realtime.sh"
rc_ok=0
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

# $4 = a string the output must contain. Exit code alone cannot tell "the rule fired" from "the
# check broke for another reason" — CI-1 of self-test-canaries.sh.
cell() { # <fixture> <expected-exit> <label> [expected-substring]
  local fx="$1" exp="$2" lbl="$3" want="${4:-}" dir out rc
  dir="$TMP/$fx"
  cp -R "$HERE/$fx" "$dir"
  git -C "$dir" init -q 2>/dev/null
  git -C "$dir" add -A 2>/dev/null
  out="$(cd "$dir" && bash "$CHECK" "$dir" 2>&1)"; rc=$?
  if [ "$rc" = "$exp" ] && { [ -z "$want" ] || printf '%s' "$out" | grep -q "$want"; }; then
    echo "   ✅ $lbl → exit $rc${want:+ (found: $want)}"
  else
    echo "   ❌ $lbl → exit $rc (expected $exp)"; printf '%s\n' "$out" | sed 's/^/        /'; rc_ok=1
  fi
}

echo "── web db tests wait in real time (web-db-test-realtime.sh) ──"
cell red-turbine      1 "turbine awaitItem() in a jsTest database test → FAIL" "WDT-1"
cell green            0 "same scenario on real-time polling → PASS"            "wait in real time"
cell green-fakes-only 0 "turbine over FAKES (no database) → PASS"              "wait in real time"
cell green-non-web    0 "turbine + database under nonWebTest → PASS"           "wait in real time"
exit "$rc_ok"
