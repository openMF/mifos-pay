#!/usr/bin/env bash
# bash4-required: it runs the engine's own functions, which read the associative EXCLUSIONS map.
# Functional test for a sync pinned to a caller's branch (SYNC_PREBUILT_BRANCH, e.g. a session branch
# carrying fork work the fork's default branch does not have yet). Runs the REAL sync_directory,
# extracted from the engine, against a throwaway git repo and the REAL ownership reader.
#
# The fork's state ("ours", and where a preserved path is restored from) is FORK_STATE — the pinned
# branch. Reading the default branch instead dropped every held file that existed only on the session
# branch and restored preserved files from the older default.
#
#   held file present only on the pinned branch   → kept, with the pinned branch's content
#   template-owned file                           → refreshed from the template
set -uo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$HERE/../.." && pwd)"
ENGINE="$REPO/scripts/white-label/sync-dirs.sh"
WORK="$(mktemp -d)"; trap 'rm -rf "$WORK"' EXIT
fail=0; pass() { echo "  ✅ $1"; }; bad() { echo "  ❌ $1"; fail=1; }

F="$WORK/fork"; mkdir -p "$F/scripts/white-label"
cp "$REPO/scripts/customization-surface.sh" "$F/scripts/"
cat > "$F/customization-surface.yaml" <<'YAML'
version: 1
rules:
  - glob: "core-base/**"
    owner: template
  - glob: "**"
    owner: template
    default: true
YAML
printf 'SYNC_DIRS=(\n    "core-base"\n)\nSYNC_FILES=(\n)\n' > "$F/scripts/white-label/sync-dirs.sh"

g() { git -C "$F" -c user.email=t@t -c user.name=t "$@"; }
mk() { mkdir -p "$(dirname "$F/$1")"; echo "$2" > "$F/$1"; }

g init -q -b main
mk core-base/a/Shared.kt template-v1
g add -A; g commit -q -m base
# The session branch carries fork work the default branch does not have yet: a held fix.
g checkout -q -b session
mk core-base/a/Held.kt session-fix
printf 'core-base/a/Held.kt\tproposed upstream: openMF/kmp-project-template#999\n' > "$F/.template-sync-holds"
g add -A; g commit -q -m "fork work"
# The template moves Shared.kt on and has no Held.kt.
g checkout -q -b tpl main; mk core-base/a/Shared.kt template-v2; g add -A; g commit -q -m template
g checkout -q session

fnsrc="$WORK/fns.sh"
{
  echo 'print_step(){ :; }; print_warning(){ :; }; print_error(){ :; }; show_progress(){ :; }; declare -A EXCLUSIONS=(); SYNC_CONFLICTS=(); SYNC_FAILED=()'
  for fn in owner_of is_held is_excluded fork_deleted_dir resolve_merge_base remove_stale_path propagate_template_deletions mirror_template_owned sync_directory; do
    awk -v f="$fn" '$0 ~ "^"f"\\(\\) \\{" {p=1} p{print} p&&/^}/{p=0}' "$ENGINE"
  done
} > "$fnsrc"
grep -q 'sync_directory()' "$fnsrc" || { echo "  ❌ sync_directory not found in engine"; exit 1; }

run() {  # run <FORK_STATE>
  ( cd "$F" && SCRIPT_DIR="$F/scripts/white-label" BASE_BRANCH=main FORK_STATE="$1" DRY_RUN=false MIRROR=false \
      bash -c "$(cat "$fnsrc")
      sync_directory core-base tpl" ) >/dev/null 2>&1
}

echo "── sync pinned to a session branch (FORK_STATE = that branch) ──"
run "$(g rev-parse session)"
[ "$(cat "$F/core-base/a/Held.kt" 2>/dev/null)" = "session-fix" ] \
  && pass "held file that exists only on the pinned branch is kept, with its content" \
  || bad "held file dropped or replaced: $(cat "$F/core-base/a/Held.kt" 2>/dev/null || echo '<missing>')"
[ "$(cat "$F/core-base/a/Shared.kt")" = "template-v2" ] \
  && pass "template-owned file refreshed from the template" \
  || bad "template file not refreshed: $(cat "$F/core-base/a/Shared.kt")"

[ "$fail" = 0 ] && echo "pinned-branch-test: PASS" || echo "pinned-branch-test: FAIL"
exit "$fail"
