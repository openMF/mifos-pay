#!/usr/bin/env bash
# run.sh — RED/GREEN canary for checks/white-label-di-seams.sh.
#
# Every cell is a way a fork silently loses its DI seam to `--clean`. None of them fails to compile on
# the TEMPLATE — the damage only appears in a consumer's repo after the demo strip, which is exactly
# why it went unnoticed until now.
set -uo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CHECK="$(cd "$HERE/../../checks" && pwd)/white-label-di-seams.sh"
SURFACE_SH="$(cd "$HERE/../../.." && pwd)/customization-surface.sh"
rc_ok=0
# $4 (optional) = the check id this cell exists to trip. A non-zero exit alone is not proof: a
# fixture that trips some OTHER WLS rule would look identical while the rule under test never ran.
cell() {
  local t="$1" exp="$2" lbl="$3" want="${4:-}" out rc
  out="$(HEALTH_ROOT="$HERE/$t" WLS_CORE_DIR="$HERE/$t/core" WLS_REGISTRY="$HERE/$t/reg/ForkFeatureRegistry.kt" \
         WLS_SURFACE_SH="$SURFACE_SH" WLS_SURFACE_YAML="$HERE/$t/customization-surface.yaml" \
         bash "$CHECK" 2>&1)"; rc=$?
  if [ "$rc" != "$exp" ]; then
    echo "   ❌ $lbl → exit $rc (expected $exp)"; printf '%s\n' "$out" | sed 's/^/        /'; rc_ok=1; return
  fi
  if [ -n "$want" ] && ! printf '%s' "$out" | grep -q "$want"; then
    echo "   ❌ $lbl → failed, but not on $want (got: $(printf '%s' "$out" | grep -oE 'WLS-[0-9]' | sort -u | tr '\n' ' '))"
    rc_ok=1; return
  fi
  echo "   ✅ $lbl → exit $rc${want:+ on $want}"
}
echo "── white-label DI seams (white-label-di-seams.sh) ──"
cell green            0 "seam survives, demo fenced      → PASS"
cell wls1-no-seam     1 "WLS-1 demo module, no seam      → FAIL" WLS-1
cell wls2-stranded    1 "WLS-2 Fork*Module under demo → FAIL" WLS-2
cell wls3-seam-fenced 1 "WLS-3 seam inside demo fence    → FAIL" WLS-3
cell wls4-demo-loose  1 "WLS-4 demo module outside fence → FAIL" WLS-4
cell wls5-template-owned 1 "WLS-5 seam owner:template       → FAIL" WLS-5
exit "$rc_ok"
