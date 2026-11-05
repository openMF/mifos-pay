#!/usr/bin/env bash
# run.sh — RED/GREEN canary for checks/fork-props-single-reader.sh.
#
# The gate exists because independent parsers of gradle/fork.properties silently diverge, twice
# already in this repo (see the check's header). A gate that cannot FAIL is decoration, so this
# proves both directions against fixture trees:
#
#   tree containing a hand-rolled `grep … | cut -d=` reader → FAIL (exit 1)
#   tree reading through scripts/_shared/fork-props.sh      → PASS (exit 0)
set -uo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CHECK="$(cd "$HERE/../../checks" && pwd)/fork-props-single-reader.sh"
rc_ok=0

# $4 (optional) = a string the output must contain. Exit code alone cannot distinguish "the check
# found the rogue reader" from "the check broke and failed for another reason" — and a check whose
# scan silently yields nothing would still let a RED fixture exit non-zero on some other rule.
cell() { # <fixture> <expected-exit> <label> [expected-substring]
  local fx="$1" exp="$2" lbl="$3" out rc
  out="$(HEALTH_ROOT="$HERE/$fx" FORK_PROPERTIES="$HERE/$fx/gradle/fork.properties" bash "$CHECK" 2>&1)"; rc=$?
  local want="${4:-}"
  if [ "$rc" = "$exp" ] && { [ -z "$want" ] || printf '%s' "$out" | grep -q "$want"; }; then
    echo "   ✅ $lbl → exit $rc${want:+ (found: $want)}"
  else
    echo "   ❌ $lbl → exit $rc (expected $exp)"; printf '%s\n' "$out" | sed 's/^/        /'; rc_ok=1
  fi
}

echo "── single-reader gate (fork-props-single-reader.sh) ──"
cell red   1 "hand-rolled parser  → FAIL" "rogue-reader.sh"
cell green 0 "reads via fp_get    → PASS"
exit "$rc_ok"
