#!/usr/bin/env bash
# bash4-required: it runs the engine's own functions, which read the associative EXCLUSIONS map.
# Functional test: the sync engine takes ownership from the contract ALONE (rules + the per-module
# module-packages.yaml it expands), with no name patterns of its own. Runs the REAL sync_directory with
# the REAL reader against a throwaway repo.
#
#   a template-owned file the engine used to hard-code as fork (KptErrorMapper.kt) → updated
#   a demo package declared in module-packages.yaml that the fork stripped         → not re-added
#   a fork-owned seam (ForkErrorMapper.kt)                                      → kept
set -uo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$HERE/../.." && pwd)"
ENGINE="$REPO/scripts/white-label/sync-dirs.sh"
WORK="$(mktemp -d)"; trap 'rm -rf "$WORK"' EXIT
fail=0; pass() { echo "  ✅ $1"; }; bad() { echo "  ❌ $1"; fail=1; }

F="$WORK/fork"; mkdir -p "$F/scripts/white-label" "$F/core/store"
cp "$REPO/scripts/customization-surface.sh" "$F/scripts/"
cat > "$F/customization-surface.yaml" <<'YAML'
version: 1
rules:
  - glob: "core/store/**/Fork*.kt"
    owner: fork
  - packages: module-packages
  - glob: "core/**"
    owner: template
  - glob: "**"
    owner: template
    default: true
YAML
printf 'module: store\ndemo:\n  - { id: banking, status: exists }\ntemplate:\n  - { id: config, status: exists }\n' > "$F/core/store/module-packages.yaml"
printf 'SYNC_DIRS=(\n    "core"\n)\nSYNC_FILES=(\n)\n' > "$F/scripts/white-label/sync-dirs.sh"

P="core/store/src/commonMain/kotlin/kpt/core/store"
mk() { mkdir -p "$(dirname "$F/$1")"; echo "$2" > "$F/$1"; }
# git, run in the fixture repo with a fixed identity.
g() { git -C "$F" -c user.email=t@t -c user.name=t "$@"; }
mk "$P/config/KptErrorMapper.kt" v1; mk "$P/config/ForkErrorMapper.kt" seam-v1
g init -q -b main; g add -A; g commit -q -m "last sync"; BASE="$(g rev-parse HEAD)"
g checkout -q -b fork; mk "$P/config/ForkErrorMapper.kt" fork-seam; g add -A; g commit -q -m "fork seam"
g checkout -q -b tpl main
mk "$P/config/KptErrorMapper.kt" v2; mk "$P/banking/impl/LoanStore.kt" demo; g add -A; g commit -q -m "template v2 + demo"
g checkout -q fork

fnsrc="$WORK/fns.sh"
{
  echo 'print_step(){ :; }; print_warning(){ :; }; print_error(){ :; }; show_progress(){ :; }; declare -A EXCLUSIONS=(); SYNC_CONFLICTS=(); SYNC_FAILED=()'
  for fn in owner_of is_held is_excluded fork_deleted_dir resolve_merge_base remove_stale_path propagate_template_deletions mirror_template_owned sync_directory; do
    awk -v f="$fn" '$0 ~ "^"f"\\(\\) \\{" {p=1} p{print} p&&/^}/{p=0}' "$ENGINE"
  done
} > "$fnsrc"
grep -q 'sync_directory()' "$fnsrc" || { echo "  ❌ sync_directory not found in engine"; exit 1; }

( cd "$F" && SCRIPT_DIR="$F/scripts/white-label" CS_ROOT="$F" BASE_BRANCH=main FORK_STATE=fork PREV_TEMPLATE_SHA="$BASE" \
    DRY_RUN=false MIRROR=true bash -c "$(cat "$fnsrc")
    sync_directory core tpl" ) >/dev/null 2>&1

[ "$(cat "$F/$P/config/KptErrorMapper.kt")" = v2 ] && pass "template-owned KptErrorMapper.kt receives the template update" \
  || bad "KptErrorMapper.kt not updated: $(cat "$F/$P/config/KptErrorMapper.kt")"
[ ! -e "$F/$P/banking/impl/LoanStore.kt" ] && pass "a demo package the fork stripped is not re-added (module-packages.yaml)" \
  || bad "the stripped demo package was re-added"
[ "$(cat "$F/$P/config/ForkErrorMapper.kt")" = fork-seam ] && pass "the fork-owned seam is kept" \
  || bad "the fork seam was overwritten: $(cat "$F/$P/config/ForkErrorMapper.kt")"
grep -q '\*/demo/\*\|KptErrorMapper' <(sed -n '/^is_excluded() {/,/^}/p' "$ENGINE") \
  && bad "is_excluded still carries hard-coded name patterns" || pass "is_excluded carries no ownership patterns of its own"

[ "$fail" = 0 ] && echo "single-reader-test: PASS" || echo "single-reader-test: FAIL"
exit "$fail"
