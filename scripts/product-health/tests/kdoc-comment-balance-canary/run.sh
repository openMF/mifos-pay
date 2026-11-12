#!/usr/bin/env bash
# run.sh — RED/GREEN canary for checks/kdoc-comment-balance.sh.
#
# Kotlin block comments NEST, so a path in prose can change comment depth: `feature/*/di` opens a
# comment that never closes, while `core/database/**/AppDatabase.kt` opens and closes one. The two
# read identically to a human. The gate exists because that mistake landed three times in one
# session, each costing a full Gradle cycle to surface.
#
# `green/` also pins the case that made this canary necessary: a comment closing where the next one
# opens (`*//**`). The gate used to strip the leading `*` as a continuation marker, leaving `//**`,
# and flagged a line the compiler was perfectly happy with. A gate that cries wolf on valid code gets
# ignored, so the false positive is fixtured alongside the true ones.
#
# The check reads `git ls-files`, so each fixture is staged in its own throwaway git repo — run in
# place, the file list would be THIS repository's and the fixtures would be invisible to it.
set -uo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CHECK="$(cd "$HERE/../../checks" && pwd)/kdoc-comment-balance.sh"
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

echo "── kdoc comment balance (kdoc-comment-balance.sh) ──"
cell red-glob-opens  1 "\`feature/*/di\` opens a comment → FAIL"        "CB-1"
cell red-stray-close 1 "stray \`*/\` mid-text → FAIL"                   "CB-1"
cell green           0 "balanced globs + glued \`*//**\` → PASS"        "every block comment balances"
exit "$rc_ok"
