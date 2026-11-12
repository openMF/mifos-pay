#!/usr/bin/env bash
# run.sh — RED/GREEN canary for customization-surface.sh `require-flip-preconditions` (the E0/T1 atomic
# self-guard that scripts/white-label/sync-dirs.sh calls before any sync).
#
# Heals the 2026-08-10 defect: og-images were reclassified `owner: generated` in customization-surface.yaml
# but the guard still hard-asserted `og-images/01_home.png == fork`, so the guard failed → sync-dirs.sh
# HALTed on EVERY tree (incl. the template itself). This canary locks guard-expectation ↔ YAML-classification
# consistency so the two can never drift again.
#
#   GREEN: the REAL customization-surface.yaml → require-flip-preconditions exit 0.
#   RED:   a fixture generated FROM the real YAML with the og-images owner flipped back to `fork`
#          (the pre-reclassification / stale state) → the guard (expects `generated`) fires → exit 1.
#          Generated at runtime so it never drifts from the real contract's shape.
set -uo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO="$(cd "$HERE/../../../.." && pwd)"           # tests/<c>/ → product-health/ → scripts/ → repo root
CS="$REPO/scripts/customization-surface.sh"
REAL_YAML="$REPO/customization-surface.yaml"
rc_ok=0

echo "── GREEN (real contract) ──"
out="$(CS_CONTRACT="$REAL_YAML" bash "$CS" require-flip-preconditions 2>&1)"; g=$?
printf '%s\n' "$out" | sed 's/^/   /'
if [ "$g" -eq 0 ]; then echo "   ✅ GREEN exit 0 (expected 0)"; else echo "   ❌ GREEN exit $g (expected 0)"; rc_ok=1; fi

echo "── RED (og-images flipped back to fork) ──"
FIX="$(mktemp)"; trap 'rm -f "$FIX"' EXIT
# Flip ONLY the og-images row's owner (leave metadata/screenshots generated) — always in sync with real shape.
awk '
  /glob: "deployment\/\*\*\/og-images\/\*\*"/ { print; f=1; next }
  f && /owner:/ { sub(/owner:.*/, "owner: fork"); f=0 }
  { print }
' "$REAL_YAML" > "$FIX"
out="$(CS_CONTRACT="$FIX" bash "$CS" require-flip-preconditions 2>&1)"; r=$?
printf '%s\n' "$out" | sed 's/^/   /'
# Cite the guard's own message, not just exit 1: this fixture could exit non-zero for an unrelated
# contract error, and a guard whose scan silently found nothing would look identical.
if [ "$r" -ne 1 ]; then
  echo "   ❌ RED exit $r (expected 1 — guard did NOT catch og-images drift)"; rc_ok=1
elif printf '%s' "$out" | grep -q 'flip-precondition:.*og-images.*owner=fork'; then
  echo "   ✅ RED exit 1 on the og-images owner drift"
else
  echo "   ❌ RED exit 1 but not on og-images drift:"; printf '%s' "$out" | grep '❌' | sed 's/^/        /'; rc_ok=1
fi

echo ""
[ "$rc_ok" -eq 0 ] && echo "canary: PASS" || echo "canary: FAIL"
exit "$rc_ok"
