#!/usr/bin/env bash
# run.sh — RED/GREEN canary for checks/shell-portability.sh.
#
# The gate exists because a BSD-only `sed -i ''` in remove-demo.sh killed the entire demo strip on
# every Linux runner while working perfectly on the author's Mac — the one class of bug a developer
# cannot reproduce locally and CI could not report (the quality-gate job short-circuited before
# product-health ever ran). A gate that cannot FAIL is decoration, so this proves both directions.
#
# The check reads `git ls-files`, so each fixture is staged in its own throwaway git repo — run in
# place, the file list would be THIS repository's and the fixtures would be invisible to it.
set -uo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CHECK="$(cd "$HERE/../../checks" && pwd)/shell-portability.sh"
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
  out="$(HEALTH_ROOT="$dir" bash "$CHECK" 2>&1)"; rc=$?
  if [ "$rc" = "$exp" ] && { [ -z "$want" ] || printf '%s' "$out" | grep -q "$want"; }; then
    echo "   ✅ $lbl → exit $rc${want:+ (found: $want)}"
  else
    echo "   ❌ $lbl → exit $rc (expected $exp)"; printf '%s\n' "$out" | sed 's/^/        /'; rc_ok=1
  fi
}

echo "── shell portability (shell-portability.sh) ──"
cell red-bsd-inplace  1 "BSD-only \`sed -i ''\` → FAIL"        "SP-1"
cell red-no-fallback  1 "\`stat -f\` with no GNU fallback → FAIL" "SP-2"
cell red-continuation-comment 1 "comment inside a \` \\\` continuation → FAIL" "SP-3"
cell red-empty-array-expansion 1 "bare expansion of an empty array under \`set -u\` → FAIL" "SP-4"
cell red-bash4-only   1 "bash-4-only builtin on a 3.2 system → FAIL" "SP-5"
cell green            0 "portable spellings → PASS"            "shell portable"
exit "$rc_ok"
