#!/usr/bin/env bash
# store-archetype-coverage.sh — the 8 Store5 archetypes each keep a real showcase.
#
# WHY THIS EXISTS
# The template's product IS the archetype demos. That contract used to live only as prose in
# CLAUDE.md, and the two "archetype contract" tests were `assertTrue(true, "...")` — unfailable. So a
# well-meaning "make every read cache-first" refactor converted MacroIndicatorStore from
# createMemoryStore to a Room-backed createStore, deleting the ONLY MEMORY_ONLY showcase, and every
# test still went green. This check makes that impossible.
#
# CONTRACT (core/store/STORE_ARCHETYPES.yaml is the source of truth)
#   AC-1 every archetype declares >= 1 showcase
#   AC-2 every declared showcase file exists
#   AC-3 every showcase actually calls its declared StoreFactory factory
#   AC-4 a showcase declaring cache_first:false carries a non-empty reason
#
# AC-3 is the load-bearing one: it compares the DECLARATION against the SOURCE, so changing a
# showcase's factory without updating the registry fails here.
#
# Exit 0 = PASS · 1 = FAIL (blocks) · 2 = WARN. Pure bash + grep/awk — no Kotlin reflection, so it
# runs identically on every platform and in CI.
set -uo pipefail

ROOT="${1:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)}"

# The archetype showcases ARE the template's demo stores. A fork that stripped the demo (every demo
# package `status: removed`, see wl_demos_stripped) no longer ships them and owns its own stores, so
# the template's "demonstrate all 8" promise does not apply to it.
# shellcheck source=../lib.sh
. "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib.sh"
if wl_demos_stripped "$ROOT"; then
  echo "  ⊘ skip — the demo showcase is stripped (core/store demo packages all removed); archetype showcases are template-only"
  exit 0
fi
REG="$ROOT/core/store/STORE_ARCHETYPES.yaml"

# Print a passing assertion.
pass() { printf '  ✓ %s\n' "$1"; }
# Print a failing assertion and set FAILED, so the check reports every problem in one run
# rather than stopping at the first.
fail() { printf '  ✗ %s\n' "$1"; FAILED=1; }
FAILED=0

if [ ! -f "$REG" ]; then
    echo "  ✗ missing $REG — the archetype contract has no source of truth"
    exit 1
fi

# The 8 archetypes the template promises. Hardcoded on purpose: if someone deletes a row from the
# registry to make this check pass, the archetype is still required here and the check still fails.
EXPECTED="NETWORK_WITH_CACHE NETWORK_ONLY CACHE_ONLY OFFLINE_LOCAL_ONLY MEMORY_ONLY PERIODIC LOAD_ONCE MUTABLE"

# Flatten the YAML into "archetype<TAB>factory<TAB>cache_first<TAB>has_reason<TAB>showcase" rows.
ROWS=$(awk '
    /^  [A-Z_]+:$/            { arch=$1; sub(/:$/,"",arch); factory=""; cf=""; reason=0; next }
    /^    factory:/           { factory=$2; next }
    /^    cache_first:/       { cf=$2; next }
    /^    reason:/            { reason=1; next }
    /^      - /               { print arch "\t" factory "\t" cf "\t" reason "\t" $2 }
' "$REG")

for arch in $EXPECTED; do
    rows=$(printf '%s\n' "$ROWS" | awk -F'\t' -v a="$arch" '$1==a')
    if [ -z "$rows" ]; then
        fail "AC-1 $arch has NO showcase declared — the template must demonstrate all 8 archetypes"
        continue
    fi

    n=0; bad=0
    while IFS=$'\t' read -r _a factory cf reason path; do
        [ -z "$path" ] && continue
        n=$((n + 1))
        if [ ! -f "$ROOT/$path" ]; then
            fail "AC-2 $arch showcase missing on disk: $path"
            bad=1; continue
        fi
        if ! grep -q "StoreFactory\.$factory\|= *$factory(" "$ROOT/$path" 2>/dev/null; then
            fail "AC-3 $arch declares factory '$factory' but $path does not call it — either restore the factory or nominate a different showcase in STORE_ARCHETYPES.yaml"
            bad=1; continue
        fi
        if [ "$cf" = "false" ] && [ "$reason" != "1" ]; then
            fail "AC-4 $arch declares cache_first:false with no reason — a non-cache-first read must justify itself or it reads as a bug to the next auditor"
            bad=1
        fi
    done <<< "$rows"

    [ "$n" -gt 0 ] && [ "$bad" = "0" ] && pass "$arch → $n showcase(s), factory verified in source"
done

[ "$FAILED" = "0" ] || exit 1
exit 0
