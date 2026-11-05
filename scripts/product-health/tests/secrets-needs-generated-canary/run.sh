#!/usr/bin/env bash
# Canary for secrets-needs-generated.sh — GREEN passes; each RED fails on the rule it breaks.
set -uo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
CHECK="$HERE/../../checks/secrets-needs-generated.sh"
rc=0
expect_red() { local dir="$1" rule="$2" out r
  out="$(bash "$CHECK" "$HERE/$dir" 2>&1)"; r=$?
  if [ $r -eq 1 ] && echo "$out" | grep -q "✗ $rule"; then echo "  ✓ $dir failed on $rule"; else echo "  ✗ $dir did not fail on $rule (exit $r)"; rc=1; fi; }
expect_red red-stale SN-1      # hand-edit below the GENERATED marker
expect_red red-unknown SN-1    # a target needs a secret LAYOUT does not declare
expect_red red-gha SN-2        # LAYOUT names a CI secret no workflow reads
expect_red red-manifest SN-1   # hand-edit to the generated secrets-manifest.yaml
out="$(bash "$CHECK" "$HERE/warn-legacy" 2>&1)"; r=$?   # an unmigrated v1 target: kept, reported, not fatal
if [ $r -eq 2 ] && echo "$out" | grep -q "⚠ SN-1"; then echo "  ✓ warn-legacy warned (v1 left untouched)"; else echo "  ✗ warn-legacy did not WARN (exit $r)"; rc=1; fi
if bash "$CHECK" "$HERE/green" >/dev/null 2>&1; then echo "  ✓ GREEN passed"; else echo "  ✗ GREEN failed"; rc=1; fi
[ $rc = 0 ] && echo "CANARY: ✅ PASS" || echo "CANARY: ❌ FAIL"; exit $rc
