#!/usr/bin/env bash
# bash4-required: it runs the engine's own functions, which read the associative EXCLUSIONS map.
# Functional test: a product-health canary FIXTURE reaches a fork even when its path looks like content
# the engine otherwise preserves or skips (`…/feature/demo/…`, a `strings.xml`). Runs the REAL
# sync_directory with the REAL ownership contract. Before the fix the engine dropped such fixtures as
# demo/merge files "the fork removed", and the template's canaries failed in every fork.
#
#   a fixture under scripts/product-health/tests/** with `/demo/` in its path is delivered
set -uo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$HERE/../.." && pwd)"
ENGINE="$REPO/scripts/white-label/sync-dirs.sh"
WORK="$(mktemp -d)"; trap 'rm -rf "$WORK"' EXIT
fail=0; pass() { echo "  ✅ $1"; }; bad() { echo "  ❌ $1"; fail=1; }

F="$WORK/fork"; mkdir -p "$F/scripts/white-label"
cp "$REPO/scripts/customization-surface.sh" "$REPO/customization-surface.yaml" "$F/scripts/" 2>/dev/null
mv "$F/scripts/customization-surface.yaml" "$F/"
printf 'SYNC_DIRS=(\n    "scripts"\n)\nSYNC_FILES=(\n)\n' > "$F/scripts/white-label/sync-dirs.sh"

# git, run in the fixture repo with a fixed identity.
g() { git -C "$F" -c user.email=t@t -c user.name=t "$@"; }
g init -q -b main; g add -A; g commit -q -m "last sync"
X="scripts/product-health/tests/x-canary/green/feature/demo/src/commonMain/composeResources/values/strings.xml"
g checkout -q -b tpl; mkdir -p "$F/$(dirname "$X")"; echo '<resources/>' > "$F/$X"; g add -A; g commit -q -m "template: new canary"
g checkout -q main

fnsrc="$WORK/fns.sh"
{
  echo 'print_step(){ :; }; print_warning(){ :; }; print_error(){ :; }; show_progress(){ :; }; declare -A EXCLUSIONS=(); SYNC_CONFLICTS=(); SYNC_FAILED=()'
  for fn in owner_of is_held is_excluded fork_deleted_dir resolve_merge_base remove_stale_path propagate_template_deletions mirror_template_owned sync_directory; do
    awk -v f="$fn" '$0 ~ "^"f"\\(\\) \\{" {p=1} p{print} p&&/^}/{p=0}' "$ENGINE"
  done
} > "$fnsrc"
grep -q 'sync_directory()' "$fnsrc" || { echo "  ❌ sync_directory not found in engine"; exit 1; }

( cd "$F" && SCRIPT_DIR="$F/scripts/white-label" CS_ROOT="$F" BASE_BRANCH=main FORK_STATE=main PREV_TEMPLATE_SHA=main \
    DRY_RUN=false MIRROR=true bash -c ". scripts/customization-surface.sh; $(cat "$fnsrc")
    sync_directory scripts tpl" ) >/dev/null 2>&1

[ -f "$F/$X" ] && pass "canary fixture with /demo/ in its path is delivered" \
  || bad "canary fixture dropped: $X"

[ "$fail" = 0 ] && echo "fixture-delivery-test: PASS" || echo "fixture-delivery-test: FAIL"
exit "$fail"
