#!/usr/bin/env bash
# Canary for deployment-template-owned.sh — GREEN passes; each RED fails on the rule it breaks.
set -uo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"; CHECK="$HERE/../../checks/deployment-template-owned.sh"; rc=0
red() { local out r; out="$(bash "$CHECK" "$HERE/$1" 2>&1)"; r=$?
  if [ $r -eq 1 ] && echo "$out" | grep -q "✗ $2"; then echo "  ✓ $1 failed on $2"; else echo "  ✗ $1 did not fail on $2 (exit $r)"; rc=1; fi; }
red red-identity DT-1   # the pre-2026-10-29 plugin: wrangler.toml / appxmanifest / StoreBroker writes
red red-walk DT-2       # a blanket walk that rewrites every target file
if bash "$CHECK" "$HERE/green" >/dev/null 2>&1; then echo "  ✓ GREEN passed"; else echo "  ✗ GREEN failed"; rc=1; fi
[ $rc = 0 ] && echo "CANARY: ✅ PASS" || echo "CANARY: ❌ FAIL"; exit $rc
