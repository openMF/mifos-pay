#!/usr/bin/env bash
# Canary for compose-strings-apostrophe.sh — RED must fail on CS-1 (\') and CS-2 (%%), GREEN must pass.
set -uo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
CHECK="$HERE/../../checks/compose-strings-apostrophe.sh"
rc=0
out="$(bash "$CHECK" "$HERE/red" 2>&1)"; r=$?
if [ $r -eq 1 ] && echo "$out" | grep -q '✗ CS-1'; then echo "  ✓ RED failed on CS-1"; else echo "  ✗ RED did not fail on CS-1 (exit $r)"; rc=1; fi
if bash "$CHECK" "$HERE/green" >/dev/null 2>&1; then echo "  ✓ GREEN passed"; else echo "  ✗ GREEN failed"; rc=1; fi
out="$(bash "$CHECK" "$HERE/red-percent" 2>&1)"; r=$?
if [ $r -eq 1 ] && echo "$out" | grep -q '✗ CS-2'; then echo "  ✓ RED-PERCENT failed on CS-2"; else echo "  ✗ RED-PERCENT did not fail on CS-2 (exit $r)"; rc=1; fi
if bash "$CHECK" "$HERE/green-percent" >/dev/null 2>&1; then echo "  ✓ GREEN-PERCENT passed"; else echo "  ✗ GREEN-PERCENT failed"; rc=1; fi
[ $rc = 0 ] && echo "CANARY: ✅ PASS" || echo "CANARY: ❌ FAIL"; exit $rc
