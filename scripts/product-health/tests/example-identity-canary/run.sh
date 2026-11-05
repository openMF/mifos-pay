#!/usr/bin/env bash
# run.sh — RED/GREEN canary for the EXAMPLE-IDENTITY model in checks/fork-identity.sh.
#
# The kmp-project-template ships as a WORKING Mifos "Money Toolkit" reference example
# (WHITE_LABEL_PLACEHOLDERS.yaml#example_identity), so identity is directional in BOTH senses:
#   Mifos reference · template mode → PASS   (the committed demo identity is the intended state)
#   Mifos reference · fork mode     → FAIL   (a fork MUST rebrand off the reference)
#   foreign brand   · template mode → FAIL   (a non-reference real brand is a leak on the template)
#   foreign brand   · fork mode     → PASS   (a properly rebranded fork)
# Signing fields stay placeholders in the ref fixture (never committed, injected at deploy).
set -uo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CHECK="$(cd "$HERE/../../checks" && pwd)/fork-identity.sh"
rc_ok=0

cell() { # <fixture> <template:1/0> <expected-exit> <label>
  local fx="$1" tmpl="$2" exp="$3" lbl="$4" out rc
  if [ "$tmpl" = "1" ]; then
    out="$(TEMPLATE_SELF_BUILD=1 FORK_PROPERTIES="$HERE/$fx/gradle/fork.properties" HEALTH_ROOT="$HERE/$fx" bash "$CHECK" 2>&1)"; rc=$?
  else
    out="$(env -u TEMPLATE_SELF_BUILD FORK_PROPERTIES="$HERE/$fx/gradle/fork.properties" HEALTH_ROOT="$HERE/$fx" bash "$CHECK" 2>&1)"; rc=$?
  fi
  # A leg expected to FAIL must emit a diagnostic — exit 1 with silent output means the check bailed
  # (missing fixture / unset var) instead of detecting the identity leak under test.
  if [ "$exp" = "1" ] && ! printf '%s' "$out" | grep -qE '❌|✗'; then
    echo "   ❌ $lbl → exit $rc but produced no diagnostic (check bailed, did not detect)"; rc_ok=1; return
  fi
  if [ "$rc" = "$exp" ]; then
    echo "   ✅ $lbl → exit $rc (expected $exp)"
  else
    echo "   ❌ $lbl → exit $rc (expected $exp)"; printf '%s\n' "$out" | sed 's/^/        /'; rc_ok=1
  fi
}

echo "── example-identity model (fork-identity.sh) ──"
cell ref     1 0 "Mifos reference · template mode → PASS"
cell ref     0 1 "Mifos reference · fork mode     → FAIL (rebrand)"
cell foreign 1 1 "Foreign brand   · template mode → FAIL (leak)"
cell foreign 0 0 "Foreign brand   · fork mode     → PASS"
exit "$rc_ok"
