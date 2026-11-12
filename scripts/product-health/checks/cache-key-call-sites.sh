#!/usr/bin/env bash
# cache-key-call-sites.sh — no production call site spells a cacheKey string literal.
#
# WHY THIS EXISTS
# A cacheKey is what `asScreenStream` uses to key per-stream freshness. Declaring it with `@CacheKey`
# on the store provider is what lets `tools/store-ksp` guarantee uniqueness — it fails the build on
# two stores sharing a key, because two streams sharing a key share a fetched-at stamp and one
# refresh silently marks the other fresh.
#
# A literal typed at the CALL SITE is invisible to that check. It compiles, it looks right, and it
# defeats the guarantee: `core/store/README` has said "never inline a cacheKey string at a call site"
# since the annotation shipped, and four production repositories were doing exactly that anyway
# (emi, amortizationCalc, profile, userData) — none of their stores declared a @CacheKey at all, so
# there was nothing to call.
#
#   CK-1 no `cacheKey = "…"` literal in production source
#   CK-2 no string literal inside a `cacheKeyFor = { … }` lambda — the keyFlow overload's form.
#        This one is worse than CK-1 and was missed on the first pass: both economic repositories
#        called the generated builder in one method and RE-SPELLED the same format inline in their
#        keyFlow sibling, so the annotation and the lambda were two copies of one format string,
#        free to diverge silently.
#   CK-3 the audit is non-vacuous — it actually found call sites to inspect
#
# Tests and KDoc samples are exempt: a fixture key is local to the test and never shares a stamp with
# a real stream.
#
# exit 0 PASS / 1 FAIL.
set -uo pipefail
: "${HEALTH_ROOT:?cache-key-call-sites: HEALTH_ROOT not set (run via product-health.sh)}"
cd "$HEALTH_ROOT" || exit 2
fails=0

# Production source only: skip test source sets and KDoc lines (` * ...`).
# `mapfile` is bash 4+; macOS still ships bash 3.2 as /bin/bash, where it is "command not found"
# and the array silently stays empty — a check that then passes having examined nothing. Read the
# lines in a loop instead, which both shells accept.
hits=()
while IFS= read -r _l; do [ -n "$_l" ] && hits+=("$_l"); done < <(
  grep -rn 'cacheKey = "' --include='*.kt' \
    --exclude-dir=build core core-base feature cmp-navigation cmp-shared 2>/dev/null \
  | grep -vE '/(commonTest|desktopTest|androidUnitTest|jsTest|wasmJsTest|nativeTest|iosTest)/' \
  | grep -vE ':[[:space:]]*\*' || true
)

if [ "${#hits[@]}" -gt 0 ]; then
  for h in "${hits[@]}"; do
    echo "  ❌ CK-1 inlined cacheKey literal: ${h%%:*}"
    echo "       → declare it with @CacheKey on the store provider and use AppCacheKeys.<Store>.…"
  done
  fails=1
fi

# CK-2 — the keyFlow overload takes a BUILDER lambda, so a literal hides one line lower than CK-1
# looks. Scan the lambda body (the arg line plus the two following lines) for a quoted string.
lambda_hits=()
while IFS= read -r _l; do [ -n "$_l" ] && lambda_hits+=("$_l"); done < <(
  grep -rn -A2 'cacheKeyFor = ' --include='*.kt' \
    --exclude-dir=build core core-base feature cmp-navigation cmp-shared 2>/dev/null \
  | grep -vE '/(commonTest|desktopTest|androidUnitTest|jsTest|wasmJsTest|nativeTest|iosTest)/' \
  | grep -E '"' | grep -vE ':[[:space:]]*\*' || true
)
if [ "${#lambda_hits[@]}" -gt 0 ]; then
  for h in "${lambda_hits[@]}"; do
    echo "  ❌ CK-2 key format spelled inline in a cacheKeyFor lambda: ${h%%:*}"
    echo "       → call the generated builder instead: AppCacheKeys.<Store>.of(…)"
  done
  fails=1
fi

# CK-3 — a rename of `cacheKey` (or of asScreenStream's parameter) would make CK-1 scan for a token
# that no longer exists and pass having examined nothing. Assert the parameter is still in use.
uses="$(grep -rl 'cacheKey = ' --include='*.kt' --exclude-dir=build core feature 2>/dev/null | wc -l | tr -d ' ')"
if [ "${uses:-0}" -eq 0 ]; then
  echo "  ❌ CK-3 no 'cacheKey = ' call site found at all — the parameter was renamed and CK-1/CK-2 are vacuous"
  fails=1
fi

[ "$fails" -eq 0 ] && { echo "✅ cache-key-call-sites: CK-1..CK-3 pass ($uses file(s) pass a cacheKey, none inlined)"; exit 0; }
echo "❌ cache-key-call-sites: inlined cacheKey literal(s) in production source"; exit 1
