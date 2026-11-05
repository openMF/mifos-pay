#!/usr/bin/env bash
#
# remove-demo.sh — strip the kmp-project-template demo showcase, leaving a clean fork.
#
# Invoked by `scripts/white-label/fork-init.sh --clean`. Convention (showcase-framework-separation epic):
#   • demo domain code lives under **/demo/** packages,
#   • demo feature modules are include()d inside a `// demo:begin … // demo:end` block
#     in settings.gradle.kts,
#   • demo entries in central files (@Database, Koin modules, store registry) are wrapped
#     in `// demo:begin … // demo:end` markers.
#
# Removal = strip every marked block + delete every **/demo/** package + delete the demo
# feature modules + reset the DB schema to a fresh-fork baseline + swap the demo-coupled
# app shell (home dashboard + nav) for a minimal placeholder.
#
# NOTE: feature-name regex accepts [A-Za-z0-9_-]+ (Kotlin-idiomatic include(":feature:PascalCase")
# + digit-suffixed feature dirs are legal). Regression-tested by
# tests/fixtures/remove-demo-regex-canary/{red,green}/.
#
# Usage:
#   remove-demo.sh                 # DRY RUN (default) — report what would change, touch nothing
#   remove-demo.sh --apply         # actually perform the removal
#   remove-demo.sh --apply --all   # explicit; --all is the only supported scope today
#
set -euo pipefail

APPLY=0
FORMAT=1
REGEN=1
for arg in "$@"; do
  case "$arg" in
    --apply) APPLY=1 ;;
    --all)   : ;;                    # only supported scope today; per-feature is a later enhancement
    --dry-run) APPLY=0 ;;
    --no-format) FORMAT=0 ;;         # skip closing spotlessApply (fast iteration)
    --no-regen)  REGEN=0 ;;          # skip the post-strip syncForkConfig (gradle-free callers only)
    *) echo "remove-demo.sh: unknown arg '$arg'" >&2; exit 2 ;;
  esac
done

# repo root = parent of scripts/
cd "$(dirname "$0")/.."

MODE=$([ "$APPLY" -eq 1 ] && echo apply || echo dry-run)
# Print a progress line for one strip step.
say() { if [ "$APPLY" -eq 1 ]; then echo "  $*"; else echo "  [dry-run] would $*"; fi; }

# ── 1. Demo feature names (parsed from the settings demo-block BEFORE it is stripped) ──
DEMO_FEATURES=$(awk '/demo:begin/{s=1;next} /demo:end/{s=0} s' settings.gradle.kts \
                  | grep -oE 'feature:[A-Za-z0-9_-]+' | sed 's/feature://' || true)

# ── 1b. TEMPLATE-owned ACCESS-POINT ids (declared, not inferred) ──────────────────────────────────
#     Every access point carries `owner: fork|template|demo`. Only `demo` is the showcase
#     ships it and a clean fork must not: the entry AND its `kpt/core/network/<id>/` package go.
#
#     This used to be inferred by awk-ing between `# demo:begin`/`# demo:end` in app.yaml, which is
#     positional and could not express `supabase_data` — its point sat OUTSIDE the fence while its
#     `api:` sat inside, so the facade survived the strip and needed a second, separate parser. One
#     declared field per entry replaces both parsers and cannot drift from what it describes.
DEMO_POINTS=$(awk '
  /^[[:space:]]*-[[:space:]]*id:[[:space:]]*/ { id=$0; sub(/.*id:[[:space:]]*/,"",id); sub(/[[:space:]].*$/,"",id); owner="" ; next }
  /^[[:space:]]*owner:[[:space:]]*/ { o=$0; sub(/.*owner:[[:space:]]*/,"",o); sub(/[[:space:]].*$/,"",o); if (id != "" && o == "demo") { print id; id="" } }
' app-profile/app.yaml 2>/dev/null || true)

# ── 2. Marked files to strip (convention-discovered) ──────────────────────────────────
# *.yaml included so app-profile's fenced demo blocks are stripped too. Test fixtures are EXCLUDED:
# `scripts/product-health/tests/**` holds RED/GREEN canary trees whose whole purpose is to CONTAIN
# demo fences (wls3-seam-fenced, wls4-demo-loose, network-buildkonfig). Stripping them turns every
# RED fixture green, silently disabling the gates that verify the white-label machinery — the strip
# would quietly break its own safety net.
#
# `core/*/module-packages.yaml` is excluded for a different reason: its demo rows must SURVIVE the
# strip so each can be stamped `status: removed`. Deleting the row instead deletes the RECORD — and
# the record is the whole point, because `/kmp-project-template-sync` then cannot tell a package the
# customizer removed on purpose from one that went missing, and "restore it" is right in exactly one
# of those cases. The rows stay; only their status changes.
MARKED_FILES=$(grep -rl 'demo:begin' --include='*.kt' --include='*.kts' --include='*.yaml' . 2>/dev/null \
  | grep -v '/build/' | grep -v '/product-health/tests/' \
  | grep -v '/module-packages\.yaml$' || true)

# ── 2a. The fork's database lineage — decided BEFORE anything is stripped ─────────────
#     A ledger recording a migration unit that is neither a declared framework unit
#     (core/database/migration-units.yaml) nor a `demo-*` unit belongs to a fork that has shipped its
#     own schema: it has installed users on that version. For such a fork the strip must not touch the
#     database lineage at all. It used to: the fence strip (step 3) cut the demo-fenced rows out of the
#     fork's own ledger, and the package sweep (4b2) deleted the @Entity packages behind tables the
#     shipped schema still holds — mbs/reels-downloader v15 lost 158 files and syncForkConfig then
#     re-appended the framework units as migrations 15→20, re-running applied migrations on every
#     installed database. So: the ledger is never fence-stripped, and a core/database demo package
#     whose table is in the latest exported schema is kept.
LEDGER="app-profile/migration-ledger.yaml"
SCHEMA_DIR="core/database/schemas/kpt.core.database.AppDatabase"
FORK_UNITS=""
SHIPPED_TABLES=""
if [ -f "$LEDGER" ]; then
  FORK_UNITS=$(grep -oE 'unit:[[:space:]]*[A-Za-z0-9_-]+' "$LEDGER" | sed 's/.*unit:[[:space:]]*//' \
    | while read -r u; do
        case "$u" in demo-*) continue ;; esac
        grep -qE "id:[[:space:]]*${u}([[:space:],}]|\$)" core/database/migration-units.yaml 2>/dev/null || echo "$u"
      done | paste -sd' ' - || true)
fi
if [ -n "$FORK_UNITS" ]; then
  MARKED_FILES=$(printf '%s\n' "$MARKED_FILES" | grep -v "/${LEDGER}\$" || true)
  latest=$(ls "$SCHEMA_DIR"/*.json 2>/dev/null | sed 's#.*/##; s#\.json$##' | sort -n | tail -1)
  [ -n "$latest" ] && SHIPPED_TABLES=$(grep -oE '"tableName":[[:space:]]*"[^"]+"' "$SCHEMA_DIR/$latest.json" \
    | sed 's/.*"\([^"]*\)"$/\1/' | sort -u | paste -sd' ' - || true)
fi

echo "remove-demo ($MODE): demo features = ${DEMO_FEATURES//$'\n'/ }"
echo "remove-demo ($MODE): demo access points = ${DEMO_POINTS//$'\n'/ }"

# ── 2b. Per-module package + file declarations — READ BEFORE THE FENCE STRIP ───────────
#     These live inside `# demo:begin … # demo:end` fences in app-profile, and step 3 below deletes
#     fenced blocks. Reading them afterwards therefore finds NOTHING: in --apply the module-package
#     sweep silently deleted zero packages while still printing its heading, so a "clean" fork kept
#     every demo store and entity. (Dry-run looked correct precisely because it mutates nothing.)
#     `DEMO_POINTS` above already had to be captured early for the same reason; these now are too.
# Each module declares its own packages in `core/<module>/module-packages.yaml` (three-value
# vocabulary: demo | template | fork). ONLY `owner: demo` is deleted — `template` is framework code a
# clean fork keeps, and `fork` is the fork's own. The two-value scheme this replaced used
# `owner: template` to mean "delete me", which read as the opposite of the truth for every framework
# package and is exactly the confusion the third value removes.
TEMPLATE_MODULE_PKGS=$(for f in core/*/module-packages.yaml; do
  [ -f "$f" ] || continue
  mod="${f#core/}"; mod="${mod%/module-packages.yaml}"
  awk -v mod="$mod" '
    /^demo:[[:space:]]*$/                 { sec="demo"; next }
    /^(template|fork):[[:space:]]*$/      { sec=""; next }
    /^[a-z_]+:/                           { sec="" }
    sec == "demo" && /^[[:space:]]*-[[:space:]]*\{/ {
      id=$0; sub(/.*id:[[:space:]]*/,"",id); sub(/[,}[:space:]].*$/,"",id); print mod "\t" id
    }
  ' "$f" 2>/dev/null || true
done)

# EVERY declared row (id + owner), captured for the SAME reason TEMPLATE_MODULE_PKGS is: the demo
# rows live inside a `# demo:begin/end` fence that the fence strip removes, so reading these files
# after it runs finds only the kept packages and the ledger would report zero removals.
ALL_MODULE_PKGS=$(for f in core/*/module-packages.yaml; do
  [ -f "$f" ] || continue
  mod="${f#core/}"; mod="${mod%/module-packages.yaml}"
  awk -v mod="$mod" '
    /^(demo|template|fork):[[:space:]]*$/ { sec=$0; sub(/:.*/,"",sec); next }
    /^[a-z_]+:/                           { sec="" }
    sec != "" && /^[[:space:]]*-[[:space:]]*\{/ {
      id=$0; sub(/.*id:[[:space:]]*/,"",id); sub(/[,}[:space:]].*$/,"",id)
      print mod "\t" id "\t" sec
    }
  ' "$f" 2>/dev/null || true
done)

TEMPLATE_MODULE_FILES=$(for pair in database:database data:data model:model core_store:store domain:domain network:network \
            ui:ui designsystem:designsystem common:common platform:platform datastore:datastore \
            firebase:firebase; do
  key="${pair%%:*}"
  awk -v m="$key" '
    $0 ~ "^" m ":[[:space:]]*$" { inmod=1; next }
    /^[a-z_]+:[[:space:]]*$/    { inmod=0 }
    inmod && /^[[:space:]]*template_files:[[:space:]]*$/ { infl=1; next }
    inmod && infl && /^[[:space:]]*-[[:space:]]*/ {
      line=$0; sub(/^[[:space:]]*-[[:space:]]*/,"",line); sub(/[[:space:]]*#.*$/,"",line); print line; next
    }
    inmod && infl && /^[[:space:]]*[a-z_]+:/ { infl=0 }
  ' app-profile/app.yaml 2>/dev/null || true
done)

# ── 3. Strip every `// demo:begin … // demo:end` block ────────────────────────────────
echo "strip marked blocks:"
while IFS= read -r f; do
  [ -z "$f" ] && continue
  say "strip demo blocks in ${f#./}"
  if [ "$APPLY" -eq 1 ]; then
    awk '/demo:begin/{skip=1} /demo:end/{skip=0; next} !skip' "$f" > "$f.tmp" && mv "$f.tmp" "$f"
  fi
done <<< "$MARKED_FILES"

# ── 3b. Remove dangling demo imports left in surviving files by the block strip ───────
#     (an `import kpt.core.<m>.demo.…` or `import kpt.feature.<demoFeature>.…` whose target
#      was just deleted is an unresolved-reference compile error, not a warning — drop them.)
echo "remove dangling demo imports:"
DEMO_PKGS=$(echo "$DEMO_FEATURES" | sed 's/-//g' | paste -sd'|' -)
[ -z "$DEMO_PKGS" ] && DEMO_PKGS="__none__"
# The per-module demo packages step 4b2 deletes (core/<module>/<id>, e.g. kpt.core.data.currency).
# They are not under `demo/`, so the two `.demo.` patterns never matched them: a fenced template file
# (sync's DataSyncWorker) kept `import kpt.core.data.currency.CurrencyRepository` after the strip and a
# cleaned fork stopped compiling (2026-11-02). Imports cannot be fenced instead — ktlint refuses
# comments inside an import list.
DEMO_MOD_PKGS=$(printf '%s\n' "$TEMPLATE_MODULE_PKGS" | awk -F'\t' 'NF==2 && $2!=""{printf "%s%s\\.%s", sep, $1, $2; sep="|"}')
[ -z "$DEMO_MOD_PKGS" ] && DEMO_MOD_PKGS="__none__"
say "strip 'import kpt.core.*.demo.*' + demo-feature imports from surviving .kt files"
if [ "$APPLY" -eq 1 ]; then
  find . -name '*.kt' -not -path '*/build/*' -not -path '*/product-health/tests/*' -print0 | while IFS= read -r -d '' f; do
    # -i.bak (not BSD's `-i ''`): GNU sed treats a separate '' as the script and fails.
    sed -i.bak -E \
      -e '/^import kpt\.core\.[a-z]+\.demo\./d' \
      -e '/^import kpt\.feature\.[a-z]+\.demo\./d' \
      -e "/^import kpt\\.feature\\.(${DEMO_PKGS})[.]/d" \
      -e "/^import kpt\\.core\\.(${DEMO_MOD_PKGS})[.]/d" \
      "$f"
    rm -f "$f.bak"
  done
fi

# ── 4. Delete every **/demo/** package directory under core/ ──────────────────────────
echo "delete demo packages:"
while IFS= read -r d; do
  [ -z "$d" ] && continue
  say "rm -rf ${d#./}"
  [ "$APPLY" -eq 1 ] && rm -rf "$d"
done < <(find core feature -type d -name demo -not -path '*/build/*' 2>/dev/null)

# ── 4b. (removed) The generated API bindings need no reset ───────────────────────────
#     GeneratedApiBindings.kt used to be committed source, so step 4 left it binding the demo APIs it
#     had just deleted and this step rewrote it to an empty module. It is now a BUILD ARTIFACT derived
#     from `@ApiBinding` on the API types, so deleting an endpoint package deletes its binding
#     declaration with it and the next build simply emits fewer. Re-creating the file here would put
#     the committed copy back and hand a cleaned fork stale demo bindings.

# ── 4a2. (removed) The generated STORE surfaces need no reset ─────────────────────────
#     They are no longer committed source: `@StoreProvider` + store-ksp emit AppStoreRegistry,
#     AppCacheKeys and GeneratedStoreBindings into build/generated at compile time. Deleting a demo package removes
#     its annotations, so the next build simply generates fewer bindings — there is nothing to
#     reset, and nothing that can reference a provider this strip just deleted.

# ── 4b2. Delete TEMPLATE-owned per-module packages (core/<module>/<id>) ───────────────
#     Declared PER MODULE under `<module>: packages:` in app-profile — "core/database has an alerts
#     entity" is a different fact from "core/store has an alerts store", and the modules genuinely
#     differ (exchange is store-only; calc/emi have no table). One awk over each module's own block.
echo "delete template module packages:"
while IFS=$'\t' read -r mod id; do
  [ -z "${id:-}" ] && continue
  # ENUMERATE the module's source sets rather than listing them. The list was
  # `commonMain commonTest androidMain iosMain desktopMain`, which silently missed desktopTest,
  # nonAndroidMain, wasmJs*/native*/js* — so a declared package could be deleted from commonMain
  # while its TEST in desktopTest survived, referencing types that no longer exist. That is a
  # compile break in a supposedly clean fork, and it only shows up for a module that happens to
  # test in one of the missed source sets (core/model does).
  # Decided per PACKAGE (every source set together), so a kept entity keeps its tests and migrations.
  if [ "$mod" = "database" ] && [ -n "$FORK_UNITS" ]; then
    kept=""
    [ "$id" = "migrations" ] && kept="migration specs of the fork's lineage"
    if [ -z "$kept" ] && [ -n "$SHIPPED_TABLES" ]; then
      for t in $(grep -rhoE '@Entity\(tableName[[:space:]]*=[[:space:]]*"[^"]+"' "core/$mod"/src/*/kotlin/kpt/core/"$mod"/"$id" 2>/dev/null \
                   | sed 's/.*"\([^"]*\)"$/\1/'); do
        case " $SHIPPED_TABLES " in *" $t "*) kept="table '$t' is in the fork's shipped schema" ; break ;; esac
      done
    fi
    if [ -n "$kept" ]; then
      say "keep core/$mod package '$id'  ($kept — installed databases hold it)"
      continue
    fi
  fi
  for d in "core/$mod"/src/*/kotlin/kpt/core/"$mod"/"$id"; do
    [ -d "$d" ] || continue
    say "rm -rf $d  (core/$mod package '$id', owner: demo)"
    [ "$APPLY" -eq 1 ] && rm -rf "$d"
  done
done <<< "$TEMPLATE_MODULE_PKGS"

# ── 4b3. Delete TEMPLATE-owned individual FILES declared per module ───────────────────
#     Some demo-only code is a single file rather than a whole package (core/store's DemoCacheKeys).
#     Those used to be caught by the `**/demo/**` path sweep; now that ownership is declared instead
#     of inferred, they are listed under `<module>: template_files:` and deleted by path.
echo "delete template module files:"
while IFS= read -r f; do
  [ -z "${f:-}" ] && continue
  [ -f "$f" ] || continue
  say "rm -f $f  (declared template_file)"
  [ "$APPLY" -eq 1 ] && rm -f "$f"
done <<< "$TEMPLATE_MODULE_FILES"

# ── 4c. Delete the per-access-point packages of the DEMO-owned endpoints ──────────────
#     Endpoint code lives in a package named for its access point (`kpt/core/network/<id>/`), not
#     under `demo/`, so step 4's sweep does not reach it.
echo "delete template access-point packages:"
NET_PKG_ROOT="core/network/src/commonMain/kotlin/kpt/core/network"
NET_TEST_ROOT="core/network/src/commonTest/kotlin/kpt/core/network"
while IFS= read -r ap; do
  [ -z "$ap" ] && continue
  # id -> package: lowercase, non-alphanumerics dropped (supabase_data -> supabasedata). MUST match
  # scaffoldAccessPointPackages in SyncForkConfigPlugin or the strip misses what the scaffold made.
  pkg=$(printf '%s' "$ap" | tr '[:upper:]' '[:lower:]' | tr -cd '[:alnum:]')
  [ -z "$pkg" ] && continue
  for d in "$NET_PKG_ROOT/$pkg" "$NET_TEST_ROOT/$pkg"; do
    [ -d "$d" ] || continue
    say "rm -rf $d  (access point '$ap', owner: demo)"
    [ "$APPLY" -eq 1 ] && rm -rf "$d"
  done
done <<< "$DEMO_POINTS"

# ── 4d. Drop those entries from app-profile itself ────────────────────────────────────
#     Block-shaped delete: an access point is a `- id:` block, so buffer each block and drop the ones
#     whose body declares `owner: demo`. Comment-preserving, unlike a YAML round-trip.
#
#     THIS USED TO DROP `owner: template`, which is the exact inverse of the contract app.yaml
#     states: template points are "shipped to EVERY fork and KEPT by the customizer", demo points are
#     the showcase the strip deletes. The variable feeding step 4c was even named TEMPLATE_POINTS
#     while collecting `o == "demo"`, so 4c deleted the right packages under the wrong label and 4d
#     then edited a DIFFERENT set. A strip left the six demo endpoints declared in app.yaml with
#     their code gone (syncForkConfig regenerates AppAccessPoints for endpoints that no longer
#     exist), and deleted the one template endpoint from app.yaml while its package stayed — an
#     @ApiBinding naming an id app-profile no longer declares, which is precisely NAP-4.
echo "drop demo access points from app-profile:"
say "remove $(echo "$DEMO_POINTS" | grep -c . || echo 0) demo access point(s) from app-profile/app.yaml"
if [ "$APPLY" -eq 1 ]; then
  awk '
    function flush() { if (n > 0) { if (!drop) for (i = 1; i <= n; i++) print buf[i]; n = 0; drop = 0 } }
    /^[[:space:]]*-[[:space:]]*id:[[:space:]]*/ { flush(); inblk = 1; n = 1; buf[1] = $0; drop = 0; next }
    inblk && /^[[:space:]]*owner:[[:space:]]*demo[[:space:]]*$/ { drop = 1; buf[++n] = $0; next }
    inblk && /^[[:space:]]{6,}/ { buf[++n] = $0; next }
    inblk && /^[[:space:]]*#/ { buf[++n] = $0; next }
    { flush(); inblk = 0; print }
    END { flush() }
  ' app-profile/app.yaml > app-profile/app.yaml.tmp && mv app-profile/app.yaml.tmp app-profile/app.yaml
  # Anchored at line start so the `#   owner: demo …` prose in app.yaml's own contract comment is
  # not counted — an unanchored match would make this assertion unsatisfiable.
  left=$(awk '/^[[:space:]]*owner:[[:space:]]*demo/{c++} END{print c+0}' app-profile/app.yaml)
  [ "$left" = "0" ] || { echo "remove-demo: FAILED to drop demo access points ($left left)" >&2; exit 1; }
fi

# ── 5. Delete demo feature modules ────────────────────────────────────────────────────
echo "delete demo feature modules:"
while IFS= read -r feat; do
  [ -z "$feat" ] && continue
  say "rm -rf feature/$feat"
  [ "$APPLY" -eq 1 ] && rm -rf "feature/$feat"
done <<< "$DEMO_FEATURES"

# ── 6. Reset the Room schema VERSION to a fresh-fork baseline ─────────────────────────
#     (the @AutoMigration history lived inside the stripped demo block; a fresh fork has
#      no installed users to migrate, so it starts clean at v1).
#     Reset the fork-owned LEDGER, not a constant on AppDatabase: the version now lives in
#     `app-profile/migration-ledger.yaml` and AppDatabase reads it via ForkDatabaseConfig.
#     A fresh fork is CREATED with the framework tables in their current shape, so it needs no
#     migration for them — it records every declared unit in `baseline_units` instead. Without that,
#     the next syncForkConfig would append all of them as migrations the fork's v1 already contains.
# …but ONLY when the lineage is still the template's (FORK_UNITS, step 2a). A fork that has shipped its
# own schema changes has installed users on that version, and resetting it to v1 (plus dropping its
# exported schemas below) would destroy the upgrade path for every one of them. That fork keeps its
# version, migrations and schema JSONs; only the demo code goes.
if [ -n "$FORK_UNITS" ]; then
  say "keep $LEDGER + core/database/schemas/ — the fork's own lineage (units: $FORK_UNITS)"
elif [ -f "$LEDGER" ]; then
  say "reset $LEDGER → version 1, empty migrations, baseline = every declared unit"
  if [ "$APPLY" -eq 1 ]; then
    # `|| true`: grep exits 2 when the file is absent, and under `set -e` that killed the whole strip
    # silently — a fork that stripped the white-label machinery legitimately has no units file.
    # Join with ',' THEN add the space. `paste -d', '` is a delimiter LIST that paste cycles through
    # — comma, space, comma, space — so it produced `a,b c,d e`: every second pair of ids fused into
    # one unknown id, syncForkConfig treated those units as unapplied, and the first real migration
    # row made it append migrations for tables the fork already had (found in
    # mifos-x-field-officer-app's ledger, 2026-10-23).
    units=$(grep -hoE 'id:[[:space:]]*[A-Za-z0-9_-]+' core/database/migration-units.yaml 2>/dev/null \
              | sed 's/.*id:[[:space:]]*//' | paste -sd, - | sed 's/,/, /g' || true)
    sed -i.bak -e "s/^version:[[:space:]]*[0-9][0-9]*/version: 1/" \
               -e "s/^baseline_units:.*/baseline_units: [${units}]/" "$LEDGER"
    rm -f "$LEDGER.bak"
    grep -q '^version: 1$' "$LEDGER" \
      || { echo "remove-demo: FAILED to reset $LEDGER version" >&2; exit 1; }
  fi
fi

#     …and DROP the exported schema history with it. Version, autoMigrations and the exported
#     schema JSONs are ONE atomic unit: the JSONs under core/database/schemas/ describe the
#     TEMPLATE's lineage (v1 is a lone `samples` table dropped back at v10), which has nothing to do
#     with a cleaned fork's v1 of four infra tables. Leaving them behind is not cosmetic — the first
#     time the fork bumps its ledger version, Room validates its auto-migration
#     against the template's stale N.json and computes a migration between unrelated schemas.
#     Deleting them makes the fork's first build export its own v1 from its own @Database.
#     The generated-region surgery that used to live here is GONE. AppDatabase is no longer
#     committed source: `@DbEntity` / `@DbDao` / `@DbConverters` on the classes are the declaration,
#     and `tools/database-ksp` derives AppDatabase + GeneratedDaoBindings into build/generated. Step
#     4b2 deletes the demo packages, which takes their annotations with them, so the next build simply
#     generates a smaller database. There is no committed output that can outlive its classes, and no
#     way to leave an AutoMigration pointing past the version we just reset to.

# ── 4b3. Stamp `status:` on every declared package row ───────────────────────────────────────
#     The sync otherwise has to GUESS why a declared package is missing: a fork that deliberately
#     stripped the crypto showcase looks identical to one whose crypto package was lost, and
#     "restore it" is right in exactly one of those cases.
#
#     Written back into core/<module>/module-packages.yaml, beside the owner it belongs to. The
#     value is DERIVED FROM DISK, so a template sync full-copying this file cannot corrupt it — the
#     next customizer run (or `--refresh-status`) recomputes it from what is actually there. That is
#     why a status field is safe here while a hand-maintained one would not be.
echo "stamp package status:"
if [ "$APPLY" -eq 1 ]; then
  for f in core/*/module-packages.yaml; do
    [ -f "$f" ] || continue
    m="${f#core/}"; m="${m%/module-packages.yaml}"
    tmp="$f.tmp"; sec=""
    : > "$tmp"
    while IFS= read -r line; do
      case "$line" in
        demo:|template:|fork:) sec="${line%:}" ;;
      esac
      case "$line" in
        *"{ id: "*"status: "*)
          id="${line#*id: }"; id="${id%%,*}"; id="${id%% *}"
          on_disk=0
          for d in "core/$m"/src/*/kotlin/kpt/core/"$m"/"$id"; do
            if [ -d "$d" ]; then on_disk=1; break; fi
          done
          if [ "$on_disk" -eq 1 ]; then st="exists"
          elif [ "$sec" = "demo" ]; then st="removed"
          else st="missing"; fi
          printf '%s\n' "$line" | sed -E "s/status: [a-z]+/status: $st/" >> "$tmp"
          ;;
        *) printf '%s\n' "$line" >> "$tmp" ;;
      esac
    done < "$f"
    mv "$tmp" "$f"
  done
  # the feature/ registry: one row per MODULE, stamped the same way
  if [ -f feature/module-packages.yaml ]; then
    tmp="feature/module-packages.yaml.tmp"; sec=""; : > "$tmp"
    while IFS= read -r line; do
      case "$line" in demo:|template:|fork:) sec="${line%:}" ;; esac
      case "$line" in
        *"{ id: "*"status: "*)
          id="${line#*id: }"; id="${id%%,*}"; id="${id%% *}"
          if [ -d "feature/$id" ]; then st="exists"
          elif [ "$sec" = "demo" ]; then st="removed"
          else st="missing"; fi
          printf '%s\n' "$line" | sed -E "s/status: [a-z]+/status: $st/" >> "$tmp"
          ;;
        *) printf '%s\n' "$line" >> "$tmp" ;;
      esac
    done < feature/module-packages.yaml
    mv "$tmp" feature/module-packages.yaml
  fi
  n_ex=$(grep -h -o 'status: exists'  core/*/module-packages.yaml feature/module-packages.yaml 2>/dev/null | wc -l | tr -d ' ' || true)
  n_rm=$(grep -h -o 'status: removed' core/*/module-packages.yaml feature/module-packages.yaml 2>/dev/null | wc -l | tr -d ' ' || true)
  n_ms=$(grep -h -o 'status: missing' core/*/module-packages.yaml feature/module-packages.yaml 2>/dev/null | wc -l | tr -d ' ' || true)
  say "stamped status (${n_ex:-0} exists, ${n_rm:-0} removed, ${n_ms:-0} missing)"
else
  say "stamp status: on every core/*/module-packages.yaml row"
fi

SCHEMAS="core/database/schemas"
if [ -d "$SCHEMAS" ] && [ -z "$FORK_UNITS" ]; then
  n_schema=$(find "$SCHEMAS" -name '*.json' 2>/dev/null | wc -l | tr -d ' ')
  say "drop $n_schema exported schema JSON(s) under $SCHEMAS/ (template lineage — regenerated on first build)"
  if [ "$APPLY" -eq 1 ]; then
    find "$SCHEMAS" -name '*.json' -delete
    left=$(find "$SCHEMAS" -name '*.json' 2>/dev/null | wc -l | tr -d ' ')
    [ "$left" = "0" ] \
      || { echo "remove-demo: FAILED to drop exported schemas under $SCHEMAS ($left left)" >&2; exit 1; }
  fi
fi

# ── 6c. Re-derive every syncForkConfig-generated surface from the STRIPPED app-profile ───
#     Stripping the demo fence removes the DECLARATION; the generated OUTPUT is committed source and
#     must be rebuilt from what survives. `fork-init.sh` runs syncForkConfig BEFORE this script, which
#     is the wrong side of the strip — so a cleaned fork kept AppAccessPoints / AppUrlTypes /
#     AppAccessPoints Supabase specs listing the 5 demo endpoints that app.yaml no longer declares (NAP-1 + NAP-3
#     both fail on it).
#
#     The GENERATOR is the only thing that knows each file's correct empty form — notably these three
#     wrap `val points = listOf(` INSIDE their sentinel region, so text-emptying them would delete the
#     declaration and break every consumer. Hence a real regeneration, not more surgery.
if [ "$APPLY" -eq 1 ] && [ "$REGEN" -eq 1 ]; then
  echo "re-derive generated surfaces:"
  say "run ./gradlew syncForkConfig (rebuild generated files from the stripped app-profile)"
  if ./gradlew syncForkConfig --quiet --console=plain; then
    echo "  ✓ generated surfaces re-derived"
  else
    echo "remove-demo: FAILED to re-derive generated surfaces (./gradlew syncForkConfig)." >&2
    echo "  The tree still declares demo endpoints in AppAccessPoints/AppUrlTypes and will not be" >&2
    echo "  coherent. Re-run './gradlew syncForkConfig' manually, or pass --no-regen if intentional." >&2
    exit 1
  fi
elif [ "$REGEN" -eq 0 ]; then
  say "SKIP post-strip syncForkConfig (--no-regen) — generated surfaces may still list demo entries"
fi

# ── 7. Formatter pass to drop the now-unused demo imports left by the block strip ─────
#     The app shell (home dashboard + nav) needs NO swap: the demo dashboard lives under
#     feature/home/demo/ (deleted in step 4) and the framework HomeScreen shell + nav files
#     carry `// demo:begin … // demo:end` blocks (stripped in step 3). The stripped shell —
#     top bar + settings entry point + empty home body — compiles as-is for the fork to fill.
if [ "$APPLY" -eq 1 ] && [ "$FORMAT" -eq 1 ]; then
  say "run ./gradlew spotlessApply (drops now-unused imports left by the strip)"
  ./gradlew spotlessApply --console=plain -q || true
else
  say "spotless SKIPPED (--no-format or dry-run)"
fi

echo "remove-demo: done ($MODE)."
