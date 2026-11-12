#!/usr/bin/env bash
# The template's own coverage check: every tracked path the contract says a sync delivers (owner
# template or merge) is reachable by one of the engine's three vectors — SYNC_DIRS, SYNC_FILES or
# CONTRACT_ROOT_MERGES. A path that is not is a promise no sync keeps: the template changes it, and no
# fork ever receives the change.
#
# Ownership comes from the reader itself (`customization-surface.sh report`), so the sync-roots rule
# and first-match order are exactly the engine's. CS_CONTRACT points it at another contract; the
# engine is the one beside that contract, as for the reader.
set -uo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$HERE/../.." && pwd)"
export CS_CONTRACT="${CS_CONTRACT:-$REPO/customization-surface.yaml}"
ENGINE="$(dirname "$CS_CONTRACT")/scripts/white-label/sync-dirs.sh"
fail=0; pass() { echo "  ✅ $1"; }; bad() { echo "  ❌ $1"; fail=1; }

vectors="$(awk '/^(SYNC_DIRS|SYNC_FILES|CONTRACT_ROOT_MERGES)=\(/{on=1; next} on&&/^\)/{on=0}
                on{ if (match($0,/"[^"]+"/)) print substr($0,RSTART+1,RLENGTH-2) }' "$ENGINE")"
[ -n "$vectors" ] || { bad "parsed no sync vectors from $ENGINE"; exit 1; }

leaks="$(bash "$REPO/scripts/customization-surface.sh" report 2>/dev/null | V="$vectors" awk -F'\t' '
  BEGIN { n = split(ENVIRON["V"], v, "\n") }
  $1 == "template" || $1 == "merge" {
    for (i = 1; i <= n; i++) if ($2 == v[i] || index($2, v[i] "/") == 1) next
    print $1 "\t" $2
  }')"

if [ -z "$leaks" ]; then
  pass "every template/merge-owned path is reachable by SYNC_DIRS, SYNC_FILES or CONTRACT_ROOT_MERGES"
else
  bad "$(printf '%s\n' "$leaks" | grep -c .) path(s) promised to forks but unreachable:"
  printf '%s\n' "$leaks" | sed 's/^/       /'
fi

[ "$fail" = 0 ] && echo "sync-reach-test: PASS" || echo "sync-reach-test: FAIL"
exit "$fail"
