#!/usr/bin/env bash
# Functional test for dev mode (--template-ref <commit>): runs the REAL engine, in --dry-run, against a
# throwaway template whose fix lives only on an unmerged branch, and checks the run resolves THAT
# commit — fetched by hash — rather than the template's merged default branch.
#
#   --template-ref <sha on an unmerged branch>   → resolved, announced as DEV MODE
#   --template-ref <unknown sha>                 → refused (non-zero), never a silent fallback to dev
#   no --template-ref                            → prod mode, no DEV MODE banner
set -uo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$HERE/../.." && pwd)"
WORK="$(mktemp -d)"; trap 'rm -rf "$WORK"' EXIT
fail=0; pass() { echo "  ✅ $1"; }; bad() { echo "  ❌ $1"; fail=1; }
g() { git -c user.email=t@t -c user.name=t "$@"; }

T="$WORK/template"; mkdir -p "$T/scripts/white-label"
cp "$REPO/scripts/white-label/sync-dirs.sh" "$T/scripts/white-label/"
cp "$REPO/scripts/customization-surface.sh" "$REPO/customization-surface.yaml" "$T/scripts/" 2>/dev/null
mv "$T/scripts/customization-surface.yaml" "$T/" 2>/dev/null
# The per-module package manifests the contract expands demo ownership from.
for m in "$REPO"/core/*/module-packages.yaml; do d="$T/${m#$REPO/}"; mkdir -p "$(dirname "$d")"; cp "$m" "$d"; done
g -C "$T" init -q -b dev; g -C "$T" add -A; g -C "$T" commit -q -m base
g -C "$T" config uploadpack.allowAnySHA1InWant true
# The fix lives on an unmerged branch, which is then deleted: only its hash can reach it.
g -C "$T" checkout -q -b fix; echo fix > "$T/FIX"; g -C "$T" add -A; g -C "$T" commit -q -m fix
SHA="$(g -C "$T" rev-parse HEAD)"; g -C "$T" checkout -q dev; g -C "$T" branch -q -D fix

F="$WORK/fork"; g clone -q "$T" "$F"; g -C "$F" remote rename origin template
g -C "$F" init -q --bare "$WORK/origin.git" >/dev/null; g -C "$F" remote add origin "$WORK/origin.git"
g -C "$F" push -q origin dev
run() { ( cd "$F" && TEMPLATE_URL="$T" bash scripts/white-label/sync-dirs.sh --dry-run "$@" ) 2>&1; }

out="$(run --template-ref "$SHA")"; rc=$?
[ "$rc" = 0 ] && echo "$out" | grep "DEV MODE" | grep -q "${SHA:0:12}" \
  && pass "an unmerged commit is fetched by hash and synced from" \
  || bad "dev mode did not resolve $SHA (rc=$rc): $(echo "$out" | tail -3)"

out="$(run --template-ref 0123456789abcdef0123456789abcdef01234567)"; rc=$?
[ "$rc" != 0 ] && pass "an unreachable commit is refused" || bad "an unreachable commit was accepted"

out="$(run)"; rc=$?
[ "$rc" = 0 ] && ! echo "$out" | grep -q "DEV MODE" \
  && pass "without --template-ref the run stays in prod mode" \
  || bad "prod-mode run failed or announced dev mode (rc=$rc)"

[ "$fail" = 0 ] && echo "template-ref-test: PASS" || echo "template-ref-test: FAIL"
exit "$fail"
