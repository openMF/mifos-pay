#!/usr/bin/env bash
# Canary for CMD-1..3 (core-module-deps.sh). Two RED legs, one GREEN.
#   red-noapply — build file does not apply the seam → the fork's deps are inert (CMD-1)
#   red-merge   — seam applied but build file back to owner:merge → merge surface returned (CMD-3)
set -uo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
CHECK="$HERE/../../checks/core-module-deps.sh"
rc=0
leg_out() { HEALTH_ROOT="$HERE/$1" bash "$CHECK" 2>&1; }
leg() { leg_out "$1" >/dev/null 2>&1; }
# Each RED must fail on ITS OWN check — red-merge exists to prove CMD-3 specifically (ownership
# silently reverting to `merge` while the seam still works), and an exit-code-only assertion would
# accept it failing on CMD-1 instead.
expect_fail_on() {
  local out; out="$(leg_out "$1")"
  if echo "$out" | grep -q "❌ $2"; then echo "  ✓ $1 failed on $2"
  else echo "  ✗ $1 did not fail on $2 — reported: $(echo "$out" | grep -oE '❌ CMD-[0-9]' | sort -u | tr '\n' ' ')"; rc=1; fi
}
echo "── RED legs (each must fail on its OWN check) ──"
expect_fail_on red-noapply CMD-1
expect_fail_on red-merge   CMD-3
echo "── GREEN (expect PASS) ──"
if leg green; then echo "  ✓ GREEN passed"; else echo "  ✗ GREEN failed"; rc=1; fi
echo ""; [ $rc = 0 ] && echo "CANARY: ✅ PASS" || echo "CANARY: ❌ FAIL"; exit $rc
