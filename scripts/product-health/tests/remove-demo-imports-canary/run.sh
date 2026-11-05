#!/usr/bin/env bash
# remove-demo-imports-canary — `remove-demo.sh --apply` leaves no import of a package it deleted.
#
# The per-module demo packages (core/<module>/<id>, e.g. kpt.core.data.currency) are deleted by step
# 4b2. A surviving template file can import one OUTSIDE its `demo:begin/end` fence — imports cannot be
# fenced, ktlint refuses comments inside an import list — so the strip must drop those imports itself.
# It only knew `kpt.core.*.demo.*`; sync's DataSyncWorker kept `import kpt.core.data.currency.…` and a
# cleaned fork stopped compiling (found 2026-11-02 on mifos-x-field-officer-app).
#
#   I-1 the demo package directory is deleted
#   I-2 a surviving file's import of it is removed; its other imports stay
set -uo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "$HERE/../../../.." && pwd)"
SB="$(mktemp -d)"; trap 'rm -rf "$SB"' EXIT
rc=0; ok() { echo "   ✅ $1"; }; bad() { echo "   ❌ $1"; rc=1; }

mkdir -p "$SB/app-profile" "$SB/scripts" "$SB/core/data/src/commonMain/kotlin/kpt/core/data/currency" \
         "$SB/sync/src/commonMain/kotlin/kpt/sync"
cp "$ROOT/scripts/remove-demo.sh" "$SB/scripts/"
printf 'network:\n  access_points: []\n' > "$SB/app-profile/app.yaml"
printf 'demo:\n  - { id: currency }\ntemplate:\n  - { id: di }\n' > "$SB/core/data/module-packages.yaml"
echo 'package kpt.core.data.currency' > "$SB/core/data/src/commonMain/kotlin/kpt/core/data/currency/CurrencyRepository.kt"
W="$SB/sync/src/commonMain/kotlin/kpt/sync/Worker.kt"
printf 'package kpt.sync\n\nimport kpt.core.base.data.infra.Synchronizer\nimport kpt.core.data.currency.CurrencyRepository\n\nclass Worker\n' > "$W"

( cd "$SB" && bash scripts/remove-demo.sh --apply --no-format --no-regen ) >/dev/null 2>&1

[ ! -d "$SB/core/data/src/commonMain/kotlin/kpt/core/data/currency" ] \
  && ok "I-1 demo package core/data/currency deleted" || bad "I-1 demo package still present"
! grep -q 'kpt.core.data.currency' "$W" && grep -q 'import kpt.core.base.data.infra.Synchronizer' "$W" \
  && ok "I-2 import of the deleted package removed; other imports kept" \
  || bad "I-2 dangling import left: $(grep '^import' "$W" | tr '\n' ' ')"

[ "$rc" -eq 0 ] && { echo "✅ remove-demo-imports-canary: I-1..I-2 pass"; exit 0; }
echo "❌ remove-demo-imports-canary: failures above"; exit 1
