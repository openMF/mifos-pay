#!/usr/bin/env bash
# run.sh — the generated API bindings must SURVIVE `remove-demo.sh` and be left coherent.
#
# GeneratedApiBindings.kt is derived from the FORK's app-profile, so it is not demo content — but it
# used to live under `core/network/.../demo/di/`, and remove-demo.sh deletes every `**/demo/**`
# package outright. That combination was silently destructive: `fork-init.sh` strips the demo BY
# DEFAULT ("forking = starting clean"), so a fork lost the generated file, and the generator's
# `if (!dir.isDirectory) return` guard then made every later syncForkConfig a NO-OP. A fork could
# declare an endpoint forever and never get a binding, with nothing reporting it.
#
# Moving the file to `di/` fixed that but created the opposite hazard: it survived while the demo API
# classes it bound were deleted, so remove-demo.sh had to reset it to its empty-module form. Neither
# hazard exists any more: the binding is declared by `@ApiBinding` ON the API class and generated
# into build/, so deleting an endpoint package deletes its declaration and there is no committed
# output to strand. remove-demo.sh still strips the fenced demo access points from app-profile, so a
# cleaned fork stops DECLARING endpoints it no longer has.
#
# `AppDatabase.kt` USED to carry the same hazard, and no longer can. Its entities/DAOs/converters
# were declared in `app-profile/app.yaml#database` and projected into committed `gen-*` regions, so
# the strip had to empty those regions by text surgery or a cleaned fork kept entities referencing
# classes step 4 had just deleted. The declaration is now the `@DbEntity` / `@DbDao` /
# `@DbConverters` annotation ON the class, and AppDatabase is generated into build/. Deleting a
# package therefore deletes its declaration — the output cannot outlive its classes, because there
# is no committed output. What this canary asserts for the database is that the DECLARATIONS go with
# their packages, and that the FRAMEWORK ones (core-base/database/module-schema.yaml) do not.
#
# The exported schema JSONs still go: they describe the TEMPLATE's lineage (v1 is a lone `samples`
# table), so a fork that later bumps its ledger version would have Room validate an auto-migration
# between two unrelated schemas.
#
# Asserted against the REAL repo files in a throwaway copy — never the working tree.
set -uo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "$HERE/../../../.." && pwd)"
# The strip is exercised against the REAL demo files. A fork that already stripped them has nothing
# to strip, so the fixture would be vacuous — skip, and say so, rather than fail on missing input.
# shellcheck source=../../lib.sh
. "$ROOT/scripts/product-health/lib.sh"
if wl_demos_stripped "$ROOT"; then
  echo "── demo strip vs generated bindings (remove-demo.sh) ──"
  echo "   ⊘ skip — this checkout's demo is already stripped; the strip is verified on the template"
  exit 0
fi
NET_PKGS="core/network/src/commonMain/kotlin/kpt/core/network"
DB_PKGS="core/database/src/commonMain/kotlin/kpt/core/database"
INFRA_SCHEMA_REL="core-base/database/module-schema.yaml"
SCHEMA_REL="core/database/schemas/kpt.core.database.AppDatabase"
LEDGER_REL="app-profile/migration-ledger.yaml"
rc=0
ok()  { echo "   ✅ $1"; }
bad() { echo "   ❌ $1"; rc=1; }

SB="$(mktemp -d)"; trap 'rm -rf "$SB"' EXIT
mkdir -p "$SB/scripts" "$SB/app-profile" "$SB/feature" "$SB/$NET_PKGS"
cp "$ROOT/scripts/remove-demo.sh" "$SB/scripts/"
# Package lifecycle now lives in core/<module>/module-packages.yaml, which the sweep reads directly.
# Without these the sweep finds nothing to delete and the canary would assert against a strip that
# examined an empty list — a vacuous green of exactly the kind CI-1 exists to prevent.
for f in "$ROOT"/core/*/module-packages.yaml; do
  [ -f "$f" ] || continue
  mod="$(basename "$(dirname "$f")")"
  mkdir -p "$SB/core/$mod"
  cp "$f" "$SB/core/$mod/"
done
# The demo endpoints, copied with their @ApiBinding annotations — those ARE the binding declaration.
for d in coingecko frankfurter fred jsonplaceholder worldbank; do
  [ -d "$ROOT/$NET_PKGS/$d" ] && cp -R "$ROOT/$NET_PKGS/$d" "$SB/$NET_PKGS/"
done
mkdir -p "$SB/$DB_PKGS" "$SB/$SCHEMA_REL" "$SB/$(dirname "$INFRA_SCHEMA_REL")"
# The demo tables, copied with their annotations — those ARE the declaration now.
for d in alerts banking cloudtodo crypto currency economic watchlist; do
  [ -d "$ROOT/$DB_PKGS/$d" ] && cp -R "$ROOT/$DB_PKGS/$d" "$SB/$DB_PKGS/"
done
cp "$ROOT/$INFRA_SCHEMA_REL" "$SB/$INFRA_SCHEMA_REL" 2>/dev/null || true
cp "$ROOT/$LEDGER_REL" "$SB/$LEDGER_REL" 2>/dev/null || true
mkdir -p "$SB/core/database"
cp "$ROOT/core/database/migration-units.yaml" "$SB/core/database/" 2>/dev/null || true
cp "$ROOT/$SCHEMA_REL"/*.json "$SB/$SCHEMA_REL/" 2>/dev/null || true
# app.yaml needs BOTH the network block and the database block for this fixture.
sed -n '/^network:/,/^org:/p' "$ROOT/app-profile/app.yaml"  > "$SB/app-profile/app.yaml"
sed -n '/^database:/,/^org:/p' "$ROOT/app-profile/app.yaml" >> "$SB/app-profile/app.yaml"
printf '// demo:begin\ninclude(":feature:probe")\n// demo:end\n' > "$SB/settings.gradle.kts"

# A declared `owner: template` module package must actually be DELETED by --apply. The declarations
# live inside `# demo:begin … # demo:end` fences, and the fence strip runs BEFORE the package sweep,
# so reading them at sweep time found nothing: --apply printed its heading, deleted ZERO packages and
# exited 0, leaving every demo store and entity in a supposedly clean fork. Dry-run looked right
# precisely because it mutates nothing, which is why this needs an --apply assertion.
PKG_PROBE="core/database/src/commonMain/kotlin/kpt/core/database/alerts"
mkdir -p "$SB/$PKG_PROBE"
printf 'package kpt.core.database.alerts\n' > "$SB/$PKG_PROBE/Probe.kt"

echo "── demo strip vs generated bindings (remove-demo.sh) ──"
before_pts="$(grep -cE '^    - id:' "$SB/app-profile/app.yaml")"
before_bind="$(grep -rl '@ApiBinding' "$SB/$NET_PKGS" 2>/dev/null | wc -l | tr -d ' ')"
before_ent="$(grep -rl '@DbEntity' "$SB/$DB_PKGS" 2>/dev/null | wc -l | tr -d ' ')"
before_dao="$(grep -rl '@DbDao' "$SB/$DB_PKGS" 2>/dev/null | wc -l | tr -d ' ')"
before_conv="$(grep -rl '@DbConverters' "$SB/$DB_PKGS" 2>/dev/null | wc -l | tr -d ' ')"
before_schema="$(find "$SB/$SCHEMA_REL" -name '*.json' 2>/dev/null | wc -l | tr -d ' ')"
[ "$before_bind" -gt 0 ] || bad "fixture is vacuous — no @ApiBinding classes were copied in to strip"

# --no-regen: this sandbox is a handful of copied files with no gradlew, and the strip HARD-FAILS if
# it cannot re-derive. Its exit code is checked — it was not, and every green this canary reported
# after the regen step was added came from a strip that had exited 1 partway through. The assertions
# still held only because steps 1-4 run BEFORE the regen step, which is luck, not verification.
if ( cd "$SB" && bash scripts/remove-demo.sh --apply --all --no-format --no-regen ) >/dev/null 2>&1; then
  ok "strip completed (exit 0)"
else
  bad "strip FAILED — every assertion below is measuring a partially-stripped tree"
fi

[ -d "$SB/$PKG_PROBE" ] \
  && bad "declared owner:demo package '$PKG_PROBE' SURVIVED --apply — the package sweep read app-profile AFTER the fence strip had already removed the declarations" \
  || ok "declared owner:demo module package deleted by --apply"

# The binding declaration IS the annotated class, so deleting the endpoint package deletes it. There
# is no committed generated file left to reset — which is the point: it cannot outlive its classes.
after_bind="$(grep -rl '@ApiBinding' "$SB/$NET_PKGS" 2>/dev/null | wc -l | tr -d ' ')"
[ "$after_bind" = "0" ] \
  && ok "all $before_bind @ApiBinding declaration(s) went with their endpoint packages" \
  || bad "$after_bind @ApiBinding survive — the next build binds an API class the strip deleted"

[ ! -f "$SB/$NET_PKGS/di/GeneratedApiBindings.kt" ] \
  && ok "GeneratedApiBindings.kt is not committed source (generated from the annotations)" \
  || bad "GeneratedApiBindings.kt exists in src/ — a cleaned fork would ship stale demo bindings"

demo_api="$(grep -c 'api: kpt.core.network' "$SB/app-profile/app.yaml")"
[ "$demo_api" = "0" ] \
  && ok "app.yaml declares no api: FQN at all (the class carries it now)" \
  || bad "$demo_api api: line(s) survive — that field was retired with @ApiBinding"

after_pts="$(grep -cE '^    - id:' "$SB/app-profile/app.yaml")"
[ "$after_pts" -gt 0 ] && [ "$after_pts" -lt "$before_pts" ] \
  && ok "kept the $after_pts non-demo access point(s), dropped $((before_pts - after_pts)) demo one(s)" \
  || bad "access-point strip wrong: $before_pts → $after_pts (expected a partial drop, not all/none)"


echo "── demo strip vs the @Db* schema declarations ──"
[ "$before_ent" -gt 0 ] || bad "fixture is vacuous — no @DbEntity classes were copied in to strip"
[ "$before_dao" -gt 0 ] || bad "fixture is vacuous — no @DbDao classes were copied in to strip"
[ "$before_conv" -gt 0 ] || bad "fixture is vacuous — no @DbConverters classes were copied in to strip"
[ "$before_schema" -gt 0 ] || bad "fixture is vacuous — no exported schema JSONs to drop"

# The declaration IS the annotated class, so deleting the package deletes the declaration. If any
# survived, the next build would put a table back on the @Database whose package is gone.
after_ent="$(grep -rl '@DbEntity' "$SB/$DB_PKGS" 2>/dev/null | wc -l | tr -d ' ')"
after_dao="$(grep -rl '@DbDao' "$SB/$DB_PKGS" 2>/dev/null | wc -l | tr -d ' ')"
after_conv="$(grep -rl '@DbConverters' "$SB/$DB_PKGS" 2>/dev/null | wc -l | tr -d ' ')"
[ "$after_ent" = "0" ] \
  && ok "all $before_ent @DbEntity declaration(s) went with their packages" \
  || bad "$after_ent @DbEntity survive — the next build regenerates a table whose package is deleted"
[ "$after_dao" = "0" ] \
  && ok "all $before_dao @DbDao declaration(s) went with their packages" \
  || bad "$after_dao @DbDao survive — AppDatabase would declare an accessor for a deleted interface"
[ "$after_conv" = "0" ] \
  && ok "all $before_conv @DbConverters declaration(s) went with their packages" \
  || bad "$after_conv @DbConverters survive — @ColumnTypeConverters would name a deleted class"

# The FRAMEWORK tables are NOT on the demo lifecycle. They live in another module and are declared
# in its own schema yaml, which the strip must leave alone: a cleaned fork with zero entities does
# not compile at all, which is worse than any dangling reference.
infra_e="$(grep -cE '^  - kpt\.core\.base\.database' "$SB/$INFRA_SCHEMA_REL" 2>/dev/null | head -1)"
[ "${infra_e:-0}" -gt 0 ] 2>/dev/null \
  && ok "cleaned fork KEEPS its ${infra_e} framework entities (core-base is not demo-lifecycle)" \
  || bad "cleaned fork has ${infra_e:-0} framework entities — a @Database with no tables does not compile"

# AppDatabase must NOT reappear as committed source. If something re-materialises it, the merge
# surface this whole design removed is silently back.
[ ! -f "$SB/$DB_PKGS/AppDatabase.kt" ] \
  && ok "AppDatabase.kt is not committed source (generated from the annotations)" \
  || bad "AppDatabase.kt exists in src/ — the hand-merged file is back and forks will 3-way merge it"

ver="$(grep -oE '^version:[[:space:]]*[0-9]+' "$SB/$LEDGER_REL" 2>/dev/null | grep -oE '[0-9]+')"
# The sandbox ledger is the running repo's own. A FORK whose ledger records its own migration units
# (neither a declared framework unit nor demo-*) has installed users on that version, and remove-demo.sh
# deliberately KEEPS its ledger + schema JSONs (FORK_UNITS). Asserting the fresh-fork reset there failed
# every such fork for doing the right thing — so assert the branch the ledger actually calls for.
fork_units=""
if [ -f "$ROOT/$LEDGER_REL" ]; then
  fork_units="$(grep -oE 'unit:[[:space:]]*[A-Za-z0-9_-]+' "$ROOT/$LEDGER_REL" | sed 's/.*unit:[[:space:]]*//' \
    | while read -r u; do
        case "$u" in demo-*) continue ;; esac
        grep -qE "id:[[:space:]]*${u}([[:space:],}]|\$)" "$ROOT/core/database/migration-units.yaml" 2>/dev/null || echo "$u"
      done | paste -sd' ' - || true)"
fi
if [ -n "$fork_units" ]; then
  root_ver="$(grep -oE '^version:[[:space:]]*[0-9]+' "$ROOT/$LEDGER_REL" | grep -oE '[0-9]+')"
  [ "$ver" = "$root_ver" ] \
    && ok "fork lineage (units: $fork_units) — ledger KEPT at version $ver, not reset" \
    || bad "fork ledger changed $root_ver → ${ver:-<unset>} — the strip reset a lineage with installed users"
  after_schema="$(find "$SB/$SCHEMA_REL" -name '*.json' 2>/dev/null | wc -l | tr -d ' ')"
  [ "$after_schema" = "$before_schema" ] \
    && ok "fork lineage — all $before_schema exported schema JSON(s) kept (they carry its upgrade path)" \
    || bad "$before_schema → $after_schema schema JSON(s) — the strip dropped a fork's exported lineage"
  exit "$rc"
fi
[ "$ver" = "1" ] \
  && ok "ledger version reset to 1 (fresh-fork baseline)" \
  || bad "ledger version is ${ver:-<unset>}, not 1 — the reset silently missed (it did, for a while)"

# NOTE: `grep -c` prints 0 AND exits 1 on no-match, so `|| echo 0` would yield "0\n0".
rows="$(grep -cE '^[[:space:]]*-[[:space:]]*\{.*from:' "$SB/$LEDGER_REL" 2>/dev/null | head -1)"
rows="${rows:-0}"
[ "$rows" = "0" ] \
  && ok "ledger migrations emptied (a fresh fork has no installed users to migrate)" \
  || bad "$rows migration row(s) survive against the v1 baseline"
# baseline_units is what stops the next syncForkConfig re-appending units the fresh v1 already has.
base="$(grep -oE '^baseline_units:.*' "$SB/$LEDGER_REL" 2>/dev/null)"
case "$base" in
  *"[]"*|"") bad "baseline_units is empty — syncForkConfig would append every template unit as a migration the fresh v1 already contains" ;;
  *)         ok "baseline_units records the units folded into the fresh schema" ;;
esac

after_schema="$(find "$SB/$SCHEMA_REL" -name '*.json' 2>/dev/null | wc -l | tr -d ' ')"
[ "$after_schema" = "0" ] \
  && ok "dropped all $before_schema exported schema JSON(s) — the fork exports its own v1 on first build" \
  || bad "$after_schema schema JSON(s) of the TEMPLATE's lineage survive alongside the v1 reset"

exit "$rc"
