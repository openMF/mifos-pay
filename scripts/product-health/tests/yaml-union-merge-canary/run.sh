#!/usr/bin/env bash
# yaml-union-merge-canary — app-profile list merges union by identity, for EVERY declared list.
#
# WHY THIS EXISTS
# `cs_merge_yaml_schema` is what a template sync runs over `app-profile/app.yaml`. It unioned exactly
# ONE list — `network.access_points` — because that was the only list when it was written. Every list
# added later (core_store.stores / cache_keys / packages, database.packages) was taken from the
# TEMPLATE wholesale, so a fork that declared its own store, cache key or package lost it on the
# next sync. Silently: the generated Kotlin then regenerated faithfully without the row,
# and no merge conflict was raised because the merge believed it had done its job.
#
# That is the failure this locks. Each check asserts its own id so a broken merge cannot pass
# vacuously — a crash also exits non-zero, and "exit != 0" alone cannot tell the two apart.
#
#   U-1 a fork's row survives in every union list
#   U-2 a template-only row is appended (new demo content still arrives)
#   U-3 a row present in BOTH appears exactly once
#   U-4 identity is per-list: id · name|fn
#   U-5 network.access_points (block-style rows) still unions — regression guard on the original
set -uo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "$HERE/../../../.." && pwd)"
rc=0
ok()  { echo "   ✅ $1"; }
bad() { echo "   ❌ $1"; rc=1; }

# shellcheck disable=SC1091
. "$ROOT/scripts/customization-surface.sh" 2>/dev/null || { echo "❌ cannot source customization-surface.sh"; exit 2; }

SB="$(mktemp -d)"; trap 'rm -rf "$SB"' EXIT

cat > "$SB/base.yaml" <<'YAML'
core_store:
  stores:
    - { id: shared, owner: template }
  cache_keys:
    - { name: SHARED, key: "shared", owner: template }
    - { fn: sharedOf, key: "shared:{id}", owner: template }
  packages:
    - { id: sharedpkg, owner: template }
database:
  packages:
    - { id: shareddb, owner: template }
network:
  access_points:
    - id: main
      owner: fork
YAML

cat > "$SB/ours.yaml" <<'YAML'
core_store:
  stores:
    - { id: shared, owner: template }
    - { id: forkStore, owner: fork }
  cache_keys:
    - { name: SHARED, key: "shared", owner: template }
    - { fn: sharedOf, key: "shared:{id}", owner: template }
    - { name: FORK_KEY, key: "forkKey", owner: fork }
    - { fn: forkOf, key: "fork:{id}", owner: fork }
  packages:
    - { id: sharedpkg, owner: template }
    - { id: forkpkg, owner: fork }
database:
  packages:
    - { id: shareddb, owner: template }
    - { id: forkdb, owner: fork }
network:
  access_points:
    - id: main
      owner: fork
    - id: forkApi
      owner: fork
YAML

cat > "$SB/theirs.yaml" <<'YAML'
core_store:
  stores:
    - { id: shared, owner: template }
    - { id: newDemo, owner: template }
  cache_keys:
    - { name: SHARED, key: "shared", owner: template }
    - { fn: sharedOf, key: "shared:{id}", owner: template }
    - { name: NEW_DEMO, key: "newDemo", owner: template }
  packages:
    - { id: sharedpkg, owner: template }
    - { id: newpkg, owner: template }
database:
  packages:
    - { id: shareddb, owner: template }
    - { id: newdb, owner: template }
network:
  access_points:
    - id: main
      owner: fork
    - id: newDemoApi
      owner: template
YAML

echo "── app-profile union-by-identity (cs_merge_yaml_schema) ──"
if ! ( cd "$ROOT" && CS_ROOT="$ROOT" cs_merge_yaml_schema "$SB/ours.yaml" "$SB/base.yaml" "$SB/theirs.yaml" "$SB/out.yaml" ) >/dev/null 2>&1; then
  bad "merge itself FAILED — every assertion below would be measuring nothing"
  echo "❌ yaml-union-merge-canary: merge error"; exit 1
fi
OUT="$SB/out.yaml"

# U-1 — fork rows survive, across all four identity shapes
u1=0
for tok in forkStore FORK_KEY forkOf forkpkg forkdb forkApi; do
  grep -q "$tok" "$OUT" || { bad "U-1 fork row '$tok' was DROPPED by the merge — a fork loses its own declaration on sync"; u1=1; }
done
[ "$u1" -eq 0 ] && ok "U-1 every fork row survived (6 rows across 5 lists)"

# U-2 — template-only rows still arrive
u2=0
for tok in newDemo NEW_DEMO newpkg newdb newDemoApi; do
  grep -q "$tok" "$OUT" || { bad "U-2 template row '$tok' did NOT arrive — new demo content would never reach a fork"; u2=1; }
done
[ "$u2" -eq 0 ] && ok "U-2 every template-only row was appended"

# U-3 — a row in BOTH sides appears once
u3=0
# Patterns are ANCHORED: a loose `id: shared` also matches `id: sharedpkg`, which reported a
# phantom duplicate and would have sent someone debugging a merge that was working correctly.
for pair in "id: shared,|1" "name: SHARED,|1" "id: sharedpkg,|1" "id: shareddb,|1" "id: main$|1"; do
  tok="${pair%|*}"; want="${pair##*|}"
  got=$(grep -cE "$tok" "$OUT")
  [ "$got" = "$want" ] || { bad "U-3 '$tok' appears $got time(s), expected $want — union is duplicating shared rows"; u3=1; }
done
[ "$u3" -eq 0 ] && ok "U-3 rows present in both sides appear exactly once"

# U-4 — identity really is per-list (a name-keyed row and an fn-keyed row both resolve)
if grep -q 'FORK_KEY' "$OUT" && grep -q 'forkOf' "$OUT"; then
  ok "U-4 per-list identity works (name-keyed AND fn-keyed cache_keys both preserved)"
else
  bad "U-4 cache_keys identity is not resolving both name: and fn: rows"
fi

# U-5 — the original access_points behaviour is intact
if grep -q 'forkApi' "$OUT" && grep -q 'newDemoApi' "$OUT"; then
  ok "U-5 network.access_points still unions (no regression on the original case)"
else
  bad "U-5 access_points union REGRESSED while generalising"
fi

[ "$rc" -eq 0 ] && { echo "✅ yaml-union-merge-canary: U-1..U-5 pass"; exit 0; }
echo "❌ yaml-union-merge-canary: failures above"; exit 1
