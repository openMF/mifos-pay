#!/usr/bin/env bash
# ownership-naming-canary — checks/ownership-naming.sh fails when a prefix lies about ownership.
#
#   N-1 GREEN: Kpt* → template, KptFork* → merge, Fork* → fork, unprefixed fork file → PASS
#   N-2 RED:   a Kpt* file the contract gives the fork → FAIL naming it
#   N-3 RED:   a Fork* file the contract gives the template → FAIL naming it
set -uo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "$HERE/../../../.." && pwd)"
SB="$(mktemp -d)"; trap 'rm -rf "$SB"' EXIT
rc=0; ok() { echo "   ✅ $1"; }; bad() { echo "   ❌ $1"; rc=1; }

mkdir -p "$SB/scripts/white-label" "$SB/core/ui/src"
cp "$ROOT/scripts/customization-surface.sh" "$SB/scripts/"
printf 'SYNC_DIRS=(\n    "core"\n)\nSYNC_FILES=(\n)\n' > "$SB/scripts/white-label/sync-dirs.sh"
contract() { printf 'version: 1\nrules:\n%s\n  - glob: "**"\n    owner: template\n    default: true\n' "$1" > "$SB/customization-surface.yaml"; }
for f in KptScaffold.kt KptForkNav.kt ForkTheme.kt Feature.kt; do echo "package x" > "$SB/core/ui/src/$f"; done
git -C "$SB" init -q; git -C "$SB" add -A
run() { HEALTH_ROOT="$SB" bash "$ROOT/scripts/product-health/checks/ownership-naming.sh" 2>&1; }

contract '  - glob: "core/ui/**/KptFork*"
    owner: merge
  - glob: "core/ui/**/Fork*"
    owner: fork
  - glob: "core/ui/**/Feature.kt"
    owner: fork'
out="$(run)" && ok "N-1 coherent names pass" || bad "N-1 coherent names failed: $out"

contract '  - glob: "core/ui/**/KptFork*"
    owner: merge
  - glob: "core/ui/**"
    owner: fork'
out="$(run)"; [ $? = 1 ] && echo "$out" | grep -q 'KptScaffold.kt — named template, contract says fork' \
  && ok "N-2 a fork-owned Kpt* file fails" || bad "N-2 not caught: $out"

contract '  - glob: "core/ui/**/KptFork*"
    owner: merge'
out="$(run)"; [ $? = 1 ] && echo "$out" | grep -q 'ForkTheme.kt — named fork, contract says template' \
  && ok "N-3 a template-owned Fork* file fails" || bad "N-3 not caught: $out"

exit "$rc"
