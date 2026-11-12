#!/usr/bin/env bash
# Functional test: every sync re-derives the fork's own state before committing
# (sync-dirs.sh#post_sync_fork_steps). Runs the REAL function with stub remove-demo.sh / gradlew that
# record their calls.
#
#   S-1 a fork whose app-profile/package-ledger.yaml records `status: removed` gets the demo strip
#   S-2 a fork that keeps its demo does NOT (no ledger rows removed)
#   S-3 syncForkConfig runs on every sync
#   S-4 a failing step stops the sync (handle_error), so a tree that is not the fork's is never committed
#   S-5 docs/ is regenerated on every sync (the fork's "Docs are current" check)
#   S-6 the dependency-guard baseline is regenerated when the catalog changed, and only then
set -uo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$HERE/../.." && pwd)"
WORK="$(mktemp -d)"; trap 'rm -rf "$WORK"' EXIT
fail=0; pass() { echo "  ✅ $1"; }; bad() { echo "  ❌ $1"; fail=1; }

fn="$(awk '/^post_sync_fork_steps\(\) \{/{p=1} p{print} p&&/^}/{p=0}' "$REPO/scripts/white-label/sync-dirs.sh")"
[ -n "$fn" ] || { echo "  ❌ post_sync_fork_steps not found in engine"; exit 1; }

# fork <dir> <ledger-status> <gradle-rc> — a fork tree with stub tools that append to calls.log.
fork() {
  mkdir -p "$1/app-profile" "$1/scripts"
  printf 'packages:\n  - { module: core/data, id: alerts, status: %s }\n' "$2" > "$1/app-profile/package-ledger.yaml"
  printf '#!/usr/bin/env bash\necho "remove-demo $*" >> calls.log\n' > "$1/scripts/remove-demo.sh"
  printf '#!/usr/bin/env bash\necho "gradlew $*" >> calls.log\nexit %s\n' "$3" > "$1/gradlew"; chmod +x "$1/gradlew"
  mkdir -p "$1/scripts/docs" "$1/gradle"
  printf '#!/usr/bin/env bash\necho "docs-refresh" >> calls.log\n' > "$1/scripts/docs/refresh.sh"
  printf '[versions]\nkotlin = "1"\n' > "$1/gradle/libs.versions.toml"
  git -C "$1" init -q && git -C "$1" -c user.email=t@t -c user.name=t add -A \
    && git -C "$1" -c user.email=t@t -c user.name=t commit -q -m base
}
# run_in <dir> — the real function, with the engine's helpers stubbed.
run_in() {
  ( cd "$1" && bash -c "print_step(){ :; }; handle_error(){ echo \"HALT \$*\" >> calls.log; exit 1; }
    $fn
    post_sync_fork_steps" ) >/dev/null 2>&1
}

fork "$WORK/stripped" removed 0; run_in "$WORK/stripped"
grep -q '^remove-demo --apply' "$WORK/stripped/calls.log" && pass "S-1 a stripped fork gets the demo strip" || bad "S-1 strip not run"
fork "$WORK/kept" kept 0; run_in "$WORK/kept"
grep -q '^remove-demo' "$WORK/kept/calls.log" && bad "S-2 a fork that keeps its demo was stripped" || pass "S-2 a fork keeping its demo is left alone"
grep -q '^gradlew syncForkConfig' "$WORK/stripped/calls.log" && grep -q '^gradlew syncForkConfig' "$WORK/kept/calls.log" \
  && pass "S-3 syncForkConfig runs on every sync" || bad "S-3 syncForkConfig missing"
fork "$WORK/broken" removed 1; run_in "$WORK/broken"; rc=$?
[ "$rc" != 0 ] && grep -q '^HALT .*syncForkConfig' "$WORK/broken/calls.log" && pass "S-4 a failing step stops the sync" || bad "S-4 rc=$rc"

grep -q '^docs-refresh' "$WORK/stripped/calls.log" && grep -q '^docs-refresh' "$WORK/kept/calls.log" \
  && pass "S-5 docs/ is regenerated on every sync" || bad "S-5 docs refresh missing"
fork "$WORK/bumped" kept 0; printf '[versions]\nkotlin = "2"\n' > "$WORK/bumped/gradle/libs.versions.toml"; run_in "$WORK/bumped"
grep -q '^gradlew dependencyGuardBaseline' "$WORK/bumped/calls.log" && ! grep -q '^gradlew dependencyGuardBaseline' "$WORK/kept/calls.log" \
  && pass "S-6 dependency baseline regenerated only when the catalog changed" || bad "S-6 baseline: bumped=$(grep -c dependencyGuardBaseline "$WORK/bumped/calls.log") kept=$(grep -c dependencyGuardBaseline "$WORK/kept/calls.log")"

# S-7 paths the post-sync steps dirty OUTSIDE the synced roots are staged; the operator's own work is not.
stage_fns="$(awk '/^dirty_paths\(\)/{print; next} /^stage_new_dirty\(\) \{/{p=1} p{print} p&&/^}/{p=0}' "$REPO/scripts/white-label/sync-dirs.sh")"
fork "$WORK/stage" kept 0
( cd "$WORK/stage" && echo wip > operator-wip.txt && bash -c "$stage_fns
  pre=\"\$(dirty_paths)\"
  mkdir -p feature && echo stamped > feature/module-packages.yaml && echo x >> app-profile/package-ledger.yaml
  stage_new_dirty \"\$pre\"" ) >/dev/null 2>&1
staged="$(git -C "$WORK/stage" diff --cached --name-only | sort | tr '\n' ' ')"
[ "$staged" = "app-profile/package-ledger.yaml feature/module-packages.yaml " ] \
  && pass "S-7 post-sync writes outside the roots are staged, operator WIP is not" \
  || bad "S-7 staged: [$staged]"

[ "$fail" = 0 ] && echo "post-sync-test: PASS" || echo "post-sync-test: FAIL"
exit "$fail"
