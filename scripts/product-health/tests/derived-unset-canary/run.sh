#!/usr/bin/env bash
# run.sh — RED/GREEN canary for the UNSET (empty) identity value in checks/fork-identity.sh.
#
# Regression guard for the 2026-09-06 inversion: lib.sh's matchers piped the value with
# `printf '%s'`, so an EMPTY value presented ZERO lines to grep and could never match the declared
# '^$' placeholder pattern. An unset signing field was therefore classified as "a foreign real-brand
# value on the TEMPLATE" — the exact OPPOSITE verdict — and the template's own CI went red the
# moment gradle/fork.properties was actually derived instead of being absent.
#
#   unset signing · template mode → PASS  (empty IS the declared '^$' placeholder)
#   unset signing · fork mode     → FAIL  (a fork MUST author its own signing identity)
#
# The direction matters in BOTH senses: a fix that merely stopped failing on the template would also
# stop failing for a fork that never authored its signing identity, which is the leak this guards.
set -uo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CHECK="$(cd "$HERE/../../checks" && pwd)/fork-identity.sh"
rc_ok=0

cell() { # <template:1/0> <expected-exit> <label>
  local tmpl="$1" exp="$2" lbl="$3" out rc
  if [ "$tmpl" = "1" ]; then
    out="$(TEMPLATE_SELF_BUILD=1 FORK_PROPERTIES="$HERE/derived/gradle/fork.properties" HEALTH_ROOT="$HERE/derived" bash "$CHECK" 2>&1)"; rc=$?
  else
    out="$(env -u TEMPLATE_SELF_BUILD FORK_PROPERTIES="$HERE/derived/gradle/fork.properties" HEALTH_ROOT="$HERE/derived" bash "$CHECK" 2>&1)"; rc=$?
  fi
  if [ "$rc" = "$exp" ]; then
    echo "   ✅ $lbl → exit $rc (expected $exp)"
  else
    echo "   ❌ $lbl → exit $rc (expected $exp)"; printf '%s\n' "$out" | sed 's/^/        /'; rc_ok=1
  fi
}

echo "── unset (derived) identity model (fork-identity.sh) ──"
cell 1 0 "unset signing · template mode → PASS"
cell 0 1 "unset signing · fork mode     → FAIL (must author)"

# The exit code alone does not prove the verdict is right for the right REASON: before the fix, fork
# mode also exited 1 — but via the inverted "foreign real-brand" branch. Assert the diagnostic names
# it as a placeholder, so a future regression that re-inverts the classification is caught here.
msg="$(env -u TEMPLATE_SELF_BUILD FORK_PROPERTIES="$HERE/derived/gradle/fork.properties" HEALTH_ROOT="$HERE/derived" bash "$CHECK" 2>&1)"
if printf '%s' "$msg" | grep -q "apple.team.id=' is a template placeholder"; then
  echo "   ✅ fork-mode diagnostic classifies unset as a PLACEHOLDER (not a foreign brand)"
else
  echo "   ❌ fork-mode diagnostic mis-classifies the unset value:"; printf '%s\n' "$msg" | sed 's/^/        /'; rc_ok=1
fi
exit "$rc_ok"
