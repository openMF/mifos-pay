#!/usr/bin/env bash
# demo-strip-fork-lineage-canary — a fork that SHIPPED a schema with the demo tables keeps them.
#
# A fork whose migration ledger records its own unit (not a framework unit, not demo-*) has installed
# users on that version. Stripping the demo there used to delete the @Entity packages behind tables
# the shipped schema still holds, plus the migration specs, and cut the demo-fenced rows out of the
# fork's own ledger — syncForkConfig then re-appended framework units as new migrations and re-ran
# them on every installed database (mbs/reels-downloader v15, 2026-11-04). This builds that fork in a
# sandbox and asserts remove-demo.sh --apply leaves the database lineage alone.
set -uo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "$HERE/../../../.." && pwd)"
. "$ROOT/scripts/product-health/lib.sh"
echo "── demo strip vs a fork's shipped database lineage (remove-demo.sh) ──"
if wl_demos_stripped "$ROOT"; then
  echo "   ⊘ skip — this checkout's demo is already stripped; verified on the template"
  exit 0
fi
DB="core/database/src/commonMain/kotlin/kpt/core/database"
SCHEMA_REL="core/database/schemas/kpt.core.database.AppDatabase"
LEDGER_REL="app-profile/migration-ledger.yaml"
rc=0
ok()  { echo "   ✅ $1"; }
bad() { echo "   ❌ $1"; rc=1; }

SB="$(mktemp -d)"; trap 'rm -rf "$SB"' EXIT
mkdir -p "$SB/scripts" "$SB/app-profile" "$SB/feature" "$SB/$DB" "$SB/$SCHEMA_REL"
cp "$ROOT/scripts/remove-demo.sh" "$SB/scripts/"
cp "$ROOT/core/database/module-packages.yaml" "$ROOT/core/database/migration-units.yaml" "$SB/core/database/"
cp -R "$ROOT/$DB/alerts" "$SB/$DB/"
[ -d "$ROOT/$DB/migrations" ] && cp -R "$ROOT/$DB/migrations" "$SB/$DB/"
grep -q '@Entity(tableName' -r "$SB/$DB/alerts" || bad "fixture is vacuous — the alerts package carries no @Entity"
# The fork's ledger: the template's, one version further, with a fork-owned unit and a demo fence.
{ sed 's/^version:.*/version: 14/' "$ROOT/$LEDGER_REL"
  printf '  # demo:begin\n  - { from: 13, to: 14, unit: fork-app-tables }\n  # demo:end\n'; } > "$SB/$LEDGER_REL"
printf '{"database":{"version":14,"entities":[{"tableName":"alerts"}]}}\n' > "$SB/$SCHEMA_REL/14.json"
printf 'network:\n  access_points: []\norg:\n  name: x\n' > "$SB/app-profile/app.yaml"
printf '// demo:begin\ninclude(":feature:probe")\n// demo:end\n' > "$SB/settings.gradle.kts"
ledger_before="$(shasum "$SB/$LEDGER_REL" | cut -d' ' -f1)"

( cd "$SB" && bash scripts/remove-demo.sh --apply --all --no-format --no-regen ) >/dev/null 2>&1 \
  && ok "strip completed (exit 0)" || bad "strip FAILED — the assertions below measure a partial strip"

[ -d "$SB/$DB/alerts" ] \
  && ok "kept core/database package 'alerts' — its table is in the fork's shipped schema" \
  || bad "deleted core/database package 'alerts' whose table installed databases still hold"
if [ -d "$ROOT/$DB/migrations" ]; then
  [ -d "$SB/$DB/migrations" ] && ok "kept core/database package 'migrations' (the fork's migration specs)" \
    || bad "deleted the migration specs of a fork with installed users"
fi
[ "$(shasum "$SB/$LEDGER_REL" | cut -d' ' -f1)" = "$ledger_before" ] \
  && ok "fork ledger byte-unchanged (no fence strip, no reset)" \
  || bad "the strip edited a fork-owned ledger — its demo-fenced rows or version changed"
[ -f "$SB/$SCHEMA_REL/14.json" ] && ok "exported schema kept" || bad "exported schema dropped"
exit "$rc"
