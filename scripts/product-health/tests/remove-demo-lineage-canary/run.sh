#!/usr/bin/env bash
# remove-demo-lineage-canary — `remove-demo.sh --apply` never resets a FORK's database lineage.
#
# The strip resets app-profile/migration-ledger.yaml to v1 and deletes core/database/schemas/*.json,
# which is right for a fresh fork (its ledger and schemas are the template's demo lineage, no user has
# installed it). Run on a fork that has shipped its own migrations, the same two steps would erase
# the upgrade path of every installed user. Found 2026-11-02 running STEP 4.7 on mifos-x-field-officer-
# app: ledger v20 with six `fork-*` units and its own schemas 13..20.
#
#   L-1 a ledger with a unit that is neither a declared framework unit nor `demo-*` is KEPT, with its
#       schema JSONs
#   L-2 a template-lineage ledger (framework + demo-* units only) is still reset to v1, schemas dropped
set -uo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "$HERE/../../../.." && pwd)"
SB="$(mktemp -d)"; trap 'rm -rf "$SB"' EXIT
rc=0; ok() { echo "   ✅ $1"; }; bad() { echo "   ❌ $1"; rc=1; }

mk() {  # mk <dir> <ledger-units...>
  local d="$1"; shift
  mkdir -p "$d/app-profile" "$d/core/database/schemas/kpt.core.database.AppDatabase" "$d/scripts"
  cp "$ROOT/scripts/remove-demo.sh" "$d/scripts/"
  printf 'units:\n  - { id: framework-fetched-at, adds_table: framework_fetched_at }\n' > "$d/core/database/migration-units.yaml"
  printf "network:\n  access_points: []\n" > "$d/app-profile/app.yaml"
  { echo "version: 20"; echo "baseline_units: []"; echo "migrations:"
    for u in "$@"; do echo "  - { unit: $u, from: 1, to: 2 }"; done; } > "$d/app-profile/migration-ledger.yaml"
  echo '{}' > "$d/core/database/schemas/kpt.core.database.AppDatabase/20.json"
}
run() { ( cd "$1" && bash scripts/remove-demo.sh --apply --no-format --no-regen ) >/dev/null 2>&1; }

mk "$SB/fork" framework-fetched-at fork-offline-sync
run "$SB/fork"
grep -q '^version: 20' "$SB/fork/app-profile/migration-ledger.yaml" \
  && [ -f "$SB/fork/core/database/schemas/kpt.core.database.AppDatabase/20.json" ] \
  && ok "L-1 fork lineage (unit fork-offline-sync) kept: ledger v20 and its schema JSON" \
  || bad "L-1 fork lineage destroyed: $(head -1 "$SB/fork/app-profile/migration-ledger.yaml")"

mk "$SB/tpl" framework-fetched-at demo-banking-tables
run "$SB/tpl"
grep -q '^version: 1$' "$SB/tpl/app-profile/migration-ledger.yaml" \
  && [ ! -f "$SB/tpl/core/database/schemas/kpt.core.database.AppDatabase/20.json" ] \
  && ok "L-2 template lineage still reset to v1, schemas dropped" \
  || bad "L-2 template lineage was not reset: $(head -1 "$SB/tpl/app-profile/migration-ledger.yaml")"

[ "$rc" -eq 0 ] && { echo "✅ remove-demo-lineage-canary: L-1..L-2 pass"; exit 0; }
echo "❌ remove-demo-lineage-canary: failures above"; exit 1
