#!/usr/bin/env bash
# Canary for core-package-ownership.sh — the fork half of module-packages.yaml.
#
# The module-packages.yaml files are template-owned and full-copied, so a check that demanded every
# fork package be declared there, and held a fork's stripped demo packages to the template's
# `status:`, could only be satisfied by editing a file the next sync overwrites. These legs pin the
# two ways a fork now passes WITHOUT that edit — and that dropping the ledger still fails.
#
#   GREEN-fork-package   an undeclared package that resolves `fork` passes MP-5
#   GREEN-ledger         a template package the fork stripped, recorded `removed`, passes MP-7
#   RED-no-ledger        the same strip with no ledger row fails — on MP-7, nothing else
#   GREEN-template       the template's own tree passes unchanged
set -uo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$HERE/../../../.." && pwd)"
CHECK="$REPO/scripts/product-health/checks/core-package-ownership.sh"
W="$(mktemp -d)"; trap 'rm -rf "$W"' EXIT
rc=0

mk() {  # mk <dir> — a minimal fork tree: one module (core/common) with two template packages
  local d="$W/$1"
  mkdir -p "$d/scripts/white-label" "$d/app-profile" "$d/core/common/src/commonMain/kotlin/kpt/core/common"
  cp "$REPO/scripts/customization-surface.sh" "$d/scripts/"
  cp "$REPO/customization-surface.yaml" "$d/"
  cp "$REPO/scripts/white-label/sync-dirs.sh" "$d/scripts/white-label/"
  printf 'module_packages:\n  - core/common/module-packages.yaml\n  - feature/module-packages.yaml\n' > "$d/app-profile/app.yaml"
  # MP-8 needs the feature registry: one backbone module, present.
  mkdir -p "$d/feature/home"
  printf 'schema_version: "1.0"\ntemplate:\n  - { id: home, status: exists }\n' > "$d/feature/module-packages.yaml"
  printf 'schema_version: "1.0"\nmodule: common\ntemplate:\n  - { id: config, status: exists }\n  - { id: format, status: exists }\nfork: []\n' \
    > "$d/core/common/module-packages.yaml"
  mkdir -p "$d/core/common/src/commonMain/kotlin/kpt/core/common/config" \
           "$d/core/common/src/commonMain/kotlin/kpt/core/common/format"
  echo "$d"
}
run() {  # run <root> → prints output, returns the check's exit code
  ( cd "$1" && HEALTH_ROOT="$1" MP_MODULES="common:common:yes" bash "$CHECK" 2>&1 )
}
expect() {  # expect <label> <want-rc> <output> <rc> [must-contain]
  local ok=1
  [ "$4" -eq "$2" ] || ok=0
  [ -z "${5:-}" ] || printf '%s' "$3" | grep -q -- "$5" || ok=0
  if [ "$ok" = 1 ]; then echo "  ✅ $1"; else echo "  ❌ $1 (rc=$4, want $2${5:+, must contain '$5'})"; printf '%s\n' "$3" | sed 's/^/       /' | head -6; rc=1; fi
}

# GREEN-fork-package — the fork's own package needs no row.
d="$(mk fork-package)"; mkdir -p "$d/core/common/src/commonMain/kotlin/kpt/core/common/fieldofficer"
out="$(run "$d")"; r=$?; expect "GREEN-fork-package (undeclared fork package passes MP-5)" 0 "$out" "$r"

# GREEN-ledger — the fork stripped `format` and recorded it.
d="$(mk ledger)"; rm -rf "$d/core/common/src/commonMain/kotlin/kpt/core/common/format"
printf 'packages:\n  - { module: core/common, id: format, status: removed }\n' > "$d/app-profile/package-ledger.yaml"
out="$(run "$d")"; r=$?; expect "GREEN-ledger (stripped package recorded in the ledger passes MP-7)" 0 "$out" "$r"

# RED-no-ledger — same strip, no record: MP-7 must catch it (and only MP-7).
d="$(mk no-ledger)"; rm -rf "$d/core/common/src/commonMain/kotlin/kpt/core/common/format"
out="$(run "$d")"; r=$?; expect "RED-no-ledger (unrecorded strip fails on MP-7)" 1 "$out" "$r" "MP-7 core/common package 'format'"

# GREEN-template — the real tree.
out="$( cd "$REPO" && HEALTH_ROOT="$REPO" bash "$CHECK" 2>&1 )"; r=$?
expect "GREEN-template (the template's own tree)" 0 "$out" "$r"

[ "$rc" -eq 0 ] && echo "core-package-ownership-canary: PASS" || echo "core-package-ownership-canary: FAIL"
exit "$rc"
