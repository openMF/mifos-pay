#!/usr/bin/env bash
# Functional test: the engine's self-update on a caller-pinned branch (SYNC_PREBUILT_BRANCH, e.g. a
# session branch carrying fork work the default branch does not have yet). Runs the REAL engine, not in
# --dry-run (self-update is skipped there), against a throwaway template whose engine is newer than the
# fork's.
#
# Before the fix the self-update cut a fresh sync branch from the default branch and re-ran there, so the
# sync was computed against the fork's older default and the session branch's work was left out.
#
#   stale engine + pinned branch → the fresh engine is committed onto the pinned branch, the run stays there
#   the pinned branch keeps the fork work it carried, and no extra sync branch is cut
set -uo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$HERE/../.." && pwd)"
WORK="$(mktemp -d)"; trap 'rm -rf "$WORK"' EXIT
fail=0; pass() { echo "  ✅ $1"; }; bad() { echo "  ❌ $1"; fail=1; }
# git, with a fixed identity for the fixture commits.
g() { git -c user.email=t@t -c user.name=t "$@"; }

T="$WORK/template"; mkdir -p "$T/scripts/white-label"
cp "$REPO/scripts/white-label/sync-dirs.sh" "$T/scripts/white-label/"
cp "$REPO/scripts/customization-surface.sh" "$T/scripts/"
cp "$REPO/customization-surface.yaml" "$T/"
for m in "$REPO"/core/*/module-packages.yaml; do d="$T/${m#$REPO/}"; mkdir -p "$(dirname "$d")"; cp "$m" "$d"; done
g -C "$T" init -q -b dev; g -C "$T" add -A; g -C "$T" commit -q -m base

F="$WORK/fork"; g clone -q "$T" "$F"; g -C "$F" remote rename origin template
g -C "$F" init -q --bare "$WORK/origin.git" >/dev/null; g -C "$F" remote add origin "$WORK/origin.git"
g -C "$F" push -q origin dev
# The fork runs an OLDER engine than the template ships, so the run self-updates.
echo "# older engine" >> "$F/scripts/white-label/sync-dirs.sh"; g -C "$F" commit -qam "fork: older engine"
g -C "$F" push -q origin dev
# The session branch carries fork work the default branch does not have yet.
g -C "$F" checkout -q -b session; echo work > "$F/SESSION_WORK"; g -C "$F" add -A; g -C "$F" commit -q -m "session work"
g -C "$F" push -q origin session

( cd "$F" && TEMPLATE_URL="$T" SYNC_PREBUILT_BRANCH=session bash scripts/white-label/sync-dirs.sh --force ) \
  > "$WORK/run.log" 2>&1

[ "$(g -C "$F" branch --show-current)" = session ] \
  && pass "the run stays on the pinned branch" \
  || bad "the run moved to $(g -C "$F" branch --show-current)"
g -C "$F" cat-file -e session:SESSION_WORK 2>/dev/null \
  && pass "the pinned branch keeps the fork work it carried" \
  || bad "the session work is missing from the pinned branch"
[ "$(g -C "$F" rev-parse session:scripts/white-label/sync-dirs.sh)" = "$(g -C "$T" rev-parse dev:scripts/white-label/sync-dirs.sh)" ] \
  && pass "the fresh engine is committed onto the pinned branch" \
  || bad "the pinned branch still carries the older engine"
[ -z "$(g -C "$F" branch --list 'sync/*')" ] \
  && pass "no extra sync branch is cut from the default branch" \
  || bad "an extra sync branch was cut: $(g -C "$F" branch --list 'sync/*' | tr -d ' ')"

[ "$fail" = 0 ] && echo "self-update-pinned-test: PASS" || { tail -15 "$WORK/run.log"; echo "self-update-pinned-test: FAIL"; }
exit "$fail"
