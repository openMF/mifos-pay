#!/usr/bin/env bash
# bash4-required: it runs the engine's own functions, which read the associative EXCLUSIONS map.
# Functional test: a sync follows the renames recorded in template-migrations.yaml. Runs the REAL
# apply_template_migrations + sync_directory (and the real reader's 3-way merge) on a throwaway repo
# where the template renamed a fork seam, the template-owned file calling it, and a merge-owned file.
#
#   M-1 the fork seam is renamed and keeps the FORK's content (fork-owned: never overwritten)
#   M-2 nothing is left under the old name
#   M-3 the template-owned caller arrives as the template's new version
#   M-4 the renamed merge-owned file carries the template's change AND the fork's edit — its 3-way
#       base was found under the old path
#   M-5 FORK_STATE carries the renames before the merge, as a commit object: the branch does not move
#       and no commit hook runs (a fork's pre-commit is Spotless + Detekt — minutes)
#   M-6 the applier is idempotent
#   M-7 a merge-owned file the template did NOT rename, whose identifier rename sits next to a fork
#       edit, merges cleanly — the base went through the same renames, so a rename is not a template edit
# template-migrations: keep — the fixture below uses the OLD names on purpose; the applier must not rewrite it.
set -uo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$HERE/../.." && pwd)"
ENGINE="${TM_ENGINE:-$REPO/scripts/white-label/sync-dirs.sh}"
WORK="$(mktemp -d)"; trap 'rm -rf "$WORK"' EXIT
fail=0; pass() { echo "  ✅ $1"; }; bad() { echo "  ❌ $1"; fail=1; }

F="$WORK/fork"; mkdir -p "$F/scripts/white-label"
cp "$REPO/scripts/customization-surface.sh" "$F/scripts/"
cat > "$F/customization-surface.yaml" <<'YAML'
version: 1
rules:
  - glob: "core/store/**/Fork*Module.kt"
    owner: fork
  - glob: "core/ui/**/KptFork*"
    owner: merge
    strategy: kotlin-3way
  - glob: "core/ui/**/Nav.kt"
    owner: merge
    strategy: kotlin-3way
  - glob: "core/**"
    owner: template
  - glob: "**"
    owner: template
    default: true
YAML
printf 'SYNC_DIRS=(\n    "core"\n)\nSYNC_FILES=(\n)\n' > "$F/scripts/white-label/sync-dirs.sh"

S="core/store/di"; U="core/ui"
mk() { mkdir -p "$(dirname "$F/$1")"; printf '%s\n' "$2" > "$F/$1"; }
# git, run in the fixture repo with a fixed identity.
g() { git -C "$F" -c user.email=t@t -c user.name=t "$@"; }
mk "$S/ProjectStoreModule.kt" "val ProjectStoreModule = empty"
mk "$S/Wiring.kt" "uses ProjectStoreModule v1"
mk "$U/Nav.kt" "$(printf 'import ProjectStoreModule\nL2\nL3')"
mk "$U/KptScaffold.kt" "$(printf 'L1\nL2\nL3\nL4\nL5')"
g init -q -b main; g add -A; g commit -q -m "last sync"; BASE="$(g rev-parse HEAD)"

g checkout -q -b fork
mk "$S/ProjectStoreModule.kt" "val ProjectStoreModule = filled-by-fork"
mk "$U/KptScaffold.kt" "$(printf 'L1\nL2\nL3\nL4\nL5-fork')"
mk "$U/Nav.kt" "$(printf 'import ProjectStoreModule\nimport ForkExtra\nL2\nL3')"
g add -A; g commit -q -m "fork work"

g checkout -q -b tpl main
g mv "$S/ProjectStoreModule.kt" "$S/ForkStoreModule.kt"; mk "$S/ForkStoreModule.kt" "val ForkStoreModule = empty"
mk "$S/Wiring.kt" "uses ForkStoreModule v2"
mk "$U/Nav.kt" "$(printf 'import ForkStoreModule\nL2\nL3')"
g mv "$U/KptScaffold.kt" "$U/KptForkScaffold.kt"; mk "$U/KptForkScaffold.kt" "$(printf 'L1-tpl\nL2\nL3\nL4\nL5')"
cp "$REPO/scripts/white-label/template-migrations.sh" "$F/scripts/white-label/"
cat > "$F/template-migrations.yaml" <<'YAML'
migrations:
  - id: t
    words:
      - "ProjectStoreModule -> ForkStoreModule"
    files:
      - "core/ui/KptScaffold.kt -> core/ui/KptForkScaffold.kt"
YAML
g add -A; g commit -q -m "template renames"
g checkout -q fork
FORK_TIP="$(g rev-parse fork)"
printf '#!/bin/sh\ntouch "%s"\n' "$WORK/hook-ran" > "$F/.git/hooks/pre-commit"; chmod +x "$F/.git/hooks/pre-commit"

fnsrc="$WORK/fns.sh"
{
  echo 'print_step(){ :; }; print_warning(){ :; }; print_error(){ :; }; show_progress(){ :; }; handle_error(){ echo "HALT $*" >&2; exit 1; }'
  echo 'declare -A EXCLUSIONS=(); SYNC_CONFLICTS=(); SYNC_FAILED=()'
  for fn in owner_of is_held is_excluded fork_deleted_dir apply_template_migrations migrated_from migrate_base merge_by_strategy resolve_merge_base remove_stale_path \
            propagate_template_deletions mirror_template_owned sync_directory; do
    awk -v f="$fn" '$0 ~ "^"f"\\(\\) \\{" {p=1} p{print} p&&/^}/{p=0}' "$ENGINE"
  done
} > "$fnsrc"
for fn in apply_template_migrations migrated_from sync_directory; do
  grep -q "^$fn() {" "$fnsrc" || { echo "  ❌ $fn not found in engine"; exit 1; }
done

( cd "$F" && SCRIPT_DIR="$F/scripts/white-label" CS_ROOT="$F" CS_CONTRACT="$F/customization-surface.yaml" \
    BASE_BRANCH=main FORK_STATE=fork TEMPLATE_REF=tpl PREV_TEMPLATE_SHA="$BASE" DRY_RUN=false MIRROR=true \
    bash -c ". scripts/customization-surface.sh >/dev/null 2>&1; $(cat "$fnsrc")
    apply_template_migrations
    echo \"\$FORK_STATE\" > \"$WORK/fork-state\"
    sync_directory core tpl" ) >"$WORK/log" 2>&1

[ "$(cat "$F/$S/ForkStoreModule.kt" 2>/dev/null)" = "val ForkStoreModule = filled-by-fork" ] \
  && pass "M-1 fork seam renamed, fork content kept" || bad "M-1 ForkStoreModule.kt: $(cat "$F/$S/ForkStoreModule.kt" 2>&1)"
[ ! -e "$F/$S/ProjectStoreModule.kt" ] && [ ! -e "$F/$U/KptScaffold.kt" ] \
  && pass "M-2 nothing left under the old names" || bad "M-2 an old-name file survived"
[ "$(cat "$F/$S/Wiring.kt")" = "uses ForkStoreModule v2" ] \
  && pass "M-3 template-owned caller is the template's new version" || bad "M-3 Wiring.kt: $(cat "$F/$S/Wiring.kt")"
sc="$(cat "$F/$U/KptForkScaffold.kt" 2>/dev/null)"
if echo "$sc" | grep -qx 'L1-tpl' && echo "$sc" | grep -qx 'L5-fork' && ! echo "$sc" | grep -q '^<<<<<<<'; then
  pass "M-4 renamed merge-owned file: template change + fork edit, no conflict"
else bad "M-4 KptForkScaffold.kt: $(echo "$sc" | tr '\n' '|')"; fi
fs="$(cat "$WORK/fork-state")"
if [ "$(g rev-parse fork)" = "$FORK_TIP" ] && [ ! -e "$WORK/hook-ran" ] \
   && g cat-file -e "$fs:$S/ForkStoreModule.kt" 2>/dev/null && ! g cat-file -e "$fs:$S/ProjectStoreModule.kt" 2>/dev/null; then
  pass "M-5 FORK_STATE carries the renames; branch unmoved, no commit hook ran"
else bad "M-5 fork tip $(g rev-parse fork) vs $FORK_TIP, hook-ran=$([ -e "$WORK/hook-ran" ] && echo yes || echo no), FORK_STATE=$fs"; fi
nav="$(cat "$F/$U/Nav.kt" 2>/dev/null)"
[ "$nav" = "$(printf 'import ForkStoreModule\nimport ForkExtra\nL2\nL3')" ] \
  && pass "M-7 a rename next to a fork edit is not a conflict (base migrated)" || bad "M-7 Nav.kt: $(echo "$nav" | tr '\n' '|')"
g show tpl:template-migrations.yaml > "$WORK/ledger.yaml"
[ -z "$(cd "$F" && bash "$REPO/scripts/white-label/template-migrations.sh" --ledger "$WORK/ledger.yaml" 2>&1)" ] \
  && pass "M-6 a second run changes nothing" || bad "M-6 the applier is not idempotent"

# M-8 — the REAL ledger is one file with two readers: /kmp-project-template-sync STEP 0.ANALYZE (the
# structural rows: order/detect/migrate/blocking) and this applier (words:/files:). Overwriting it with
# rename rows alone silently dropped the five structural migrations (2026-11-02), so every row must keep
# the structural schema.
miss="$(awk '/^  - id:/{if (id!="") print id": "keys; id=$3; keys=""; next}
             id!="" && /^    (order|detect|migrate|blocking):/{k=$1; sub(/:$/,"",k); keys=keys" "k}
             END{if (id!="") print id": "keys}' "$REPO/template-migrations.yaml" \
        | awk '{n=0; for(i=2;i<=NF;i++) if ($i~/^(order|detect|migrate|blocking)$/) n++; if (n<4) print $1}')"
rows="$(grep -c '^  - id:' "$REPO/template-migrations.yaml")"
[ -z "$miss" ] && [ "$rows" -ge 6 ] && grep -q '^schema: 1' "$REPO/template-migrations.yaml" \
  && pass "M-8 every ledger row keeps the structural schema ($rows rows)" || bad "M-8 rows missing structural keys: ${miss:-none} (rows=$rows)"

[ "$fail" = 0 ] && echo "template-migrations-test: PASS" || { echo "template-migrations-test: FAIL"; sed 's/^/    /' "$WORK/log" | tail -20; }
exit "$fail"
