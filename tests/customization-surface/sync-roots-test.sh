#!/usr/bin/env bash
# Each case builds its own root and points CS_CONTRACT at it; the reader resolves the sync roots from
# the engine BESIDE that contract (a case with a different list proves it is not reading the real one).
#
# Fixture test for the SYNC-ROOTS rule in cs_match_g — the template's default (`**` → template) claims
# only what the template SHIPS. A path under no SYNC_DIRS / SYNC_FILES root is fork territory.
#
# The defect it locks (2026-10-31 ownership audit): fork-only directories (libs/, branding/, maestro/,
# server-layer/) resolved `template` through the default, so an audit — or a strict-mirror sync —
# would have treated 156 fork-authored files across 10 forks as template drift to delete.
set -uo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
READER="$HERE/../../scripts/customization-surface.sh"
WORK="$(mktemp -d)"; trap 'rm -rf "$WORK"' EXIT
fail=0; pass() { echo "  ✅ $1"; }; bad() { echo "  ❌ $1"; fail=1; }

mkdir -p "$WORK/root/scripts/white-label"
cat > "$WORK/root/customization-surface.yaml" <<'YAML'
version: 1
rules:
  - glob: "core/**"
    owner: fork
  - glob: "**"
    owner: template
    default: true
YAML
cat > "$WORK/root/scripts/white-label/sync-dirs.sh" <<'SH'
SYNC_DIRS=(
    "deployment"     # full copy
    "core"
)
SYNC_FILES=(
    "LICENSE"
)
SH

owner() {  # owner <root> <path>
  ( export CS_ROOT="$1" CS_CONTRACT="$1/customization-surface.yaml"
    # shellcheck source=/dev/null
    source "$READER"; cs_resolve_owner "$2" )
}

echo "── with the engine present (sync roots known) ──"
[ "$(owner "$WORK/root" deployment/android/lane.rb)" = template ] && pass "under a SYNC_DIRS root → template" || bad "deployment/** not template"
[ "$(owner "$WORK/root" LICENSE)" = template ]                   && pass "a SYNC_FILES root → template"     || bad "LICENSE not template"
[ "$(owner "$WORK/root" libs/feature/x.kt)" = fork ]             && pass "under no root → fork (was template)" || bad "libs/** not fork"
[ "$(owner "$WORK/root" deploymentX/y)" = fork ]                 && pass "prefix look-alike is not under the root" || bad "deploymentX/ treated as under deployment/"
[ "$(owner "$WORK/root" core/data/x.kt)" = fork ]                && pass "explicit rules still win first"   || bad "explicit core/** rule lost"

echo "── engine absent (bare checkout / fixture) → previous behaviour ──"
mkdir -p "$WORK/bare"; cp "$WORK/root/customization-surface.yaml" "$WORK/bare/"
[ "$(owner "$WORK/bare" libs/feature/x.kt)" = template ] && pass "no engine → default stays template" || bad "no-engine default changed"

echo "── a root list that differs from the real template engine (proves it reads the fixture's) ──"
mkdir -p "$WORK/alt/scripts/white-label"; cp "$WORK/root/customization-surface.yaml" "$WORK/alt/"
printf 'SYNC_DIRS=(\n    "libs"\n)\nSYNC_FILES=(\n)\n' > "$WORK/alt/scripts/white-label/sync-dirs.sh"
[ "$(owner "$WORK/alt" libs/feature/x.kt)" = template ]      && pass "libs/ is a root HERE → template"      || bad "fixture engine ignored (libs)"
[ "$(owner "$WORK/alt" deployment/android/x)" = fork ]      && pass "deployment/ is NOT a root here → fork" || bad "fixture engine ignored (deployment)"

[ "$fail" = 0 ] && echo "sync-roots-test: PASS" || echo "sync-roots-test: FAIL"
exit "$fail"
