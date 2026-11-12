#!/usr/bin/env bash
# bash4-required: it runs the engine's own functions, which read the associative EXCLUSIONS map.
# Functional test for a CASE-ONLY rename in the template (encodeX.kt → EncodeX.kt), run through the
# REAL sync_directory with --mirror and deletion propagation on. On a case-insensitive filesystem
# (macOS) the two names are one file: removing the old name used to delete the file the overlay had
# just written, and the fork lost it. On a case-sensitive filesystem the test passes either way.
#
#   the renamed file exists after the sync, with the template's content, under the new name
set -uo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$HERE/../.." && pwd)"
ENGINE="$REPO/scripts/white-label/sync-dirs.sh"
WORK="$(mktemp -d)"; trap 'rm -rf "$WORK"' EXIT
fail=0; pass() { echo "  ✅ $1"; }; bad() { echo "  ❌ $1"; fail=1; }

F="$WORK/fork"; mkdir -p "$F/scripts/white-label"
cp "$REPO/scripts/customization-surface.sh" "$F/scripts/"
printf 'version: 1\nrules:\n  - glob: "**"\n    owner: template\n    default: true\n' > "$F/customization-surface.yaml"
printf 'SYNC_DIRS=(\n    "core-base"\n)\nSYNC_FILES=(\n)\n' > "$F/scripts/white-label/sync-dirs.sh"

# git, run in the fixture repo with a fixed identity.
g() { git -C "$F" -c user.email=t@t -c user.name=t "$@"; }
g init -q -b main
mkdir -p "$F/core-base/a"; echo v1 > "$F/core-base/a/encodeX.kt"
g add -A; g commit -q -m "last sync"
g checkout -q -b tpl
g -c core.ignorecase=false mv -f core-base/a/encodeX.kt core-base/a/EncodeX.kt; echo v2 > "$F/core-base/a/EncodeX.kt"
g add -A; g commit -q -m "template: case-only rename"
g checkout -q main

fnsrc="$WORK/fns.sh"
{
  echo 'print_step(){ :; }; print_warning(){ :; }; print_error(){ :; }; show_progress(){ :; }; declare -A EXCLUSIONS=(); SYNC_CONFLICTS=(); SYNC_FAILED=()'
  for fn in owner_of is_held is_excluded fork_deleted_dir resolve_merge_base remove_stale_path propagate_template_deletions mirror_template_owned sync_directory; do
    awk -v f="$fn" '$0 ~ "^"f"\\(\\) \\{" {p=1} p{print} p&&/^}/{p=0}' "$ENGINE"
  done
} > "$fnsrc"
grep -q 'sync_directory()' "$fnsrc" || { echo "  ❌ sync_directory not found in engine"; exit 1; }

( cd "$F" && SCRIPT_DIR="$F/scripts/white-label" BASE_BRANCH=main FORK_STATE=main PREV_TEMPLATE_SHA=main \
    DRY_RUN=false MIRROR=true bash -c "$(cat "$fnsrc")
    sync_directory core-base tpl" ) >/dev/null 2>&1

if [ -f "$F/core-base/a/EncodeX.kt" ] && [ "$(cat "$F/core-base/a/EncodeX.kt")" = v2 ] \
   && ls "$F/core-base/a" | grep -qx 'EncodeX.kt'; then
  pass "case-only rename survives: EncodeX.kt present with the template's content"
else
  bad "case-only rename lost the file: $(ls "$F/core-base/a" 2>/dev/null | tr '\n' ' ')"
fi

[ "$fail" = 0 ] && echo "case-rename-test: PASS" || echo "case-rename-test: FAIL"
exit "$fail"
