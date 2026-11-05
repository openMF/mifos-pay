#!/usr/bin/env bash
# checks/white-label-di-seams.sh — every core layer keeps a FORK DI seam that survives `--clean`.
#
# `fork-init.sh` strips the demo BY DEFAULT ("forking = starting clean"), and `remove-demo.sh` does
# that by deleting every `**/demo/**` package plus every `// demo:begin … // demo:end` block. So
# anything a FORK needs must live outside both.
#
# It did not. Each core layer's DI aggregator was named `Fork<X>Module` — which reads as the fork's
# seam — but sat in `<layer>/demo/di/` and contained only demo bindings. `ForkFeatureRegistry`'s whole
# `featureKoinModules` list was fenced too. A cleaned fork therefore got `listOf()` and no per-layer
# seam anywhere: nowhere to register a repository, a DAO, a store or a network single. The split is
# `demo/di/Demo<X>Module` (stripped) vs `di/Fork<X>Module` (survives, empty on the template).
#
#   WLS-1  a layer with a Demo<X>Module has a sibling di/Fork<X>Module
#   WLS-2  no Fork*Module lives under **/demo/**            (it would be deleted)
#   WLS-3  ForkFeatureRegistry lists every Fork*Module OUTSIDE the demo fence  (it must survive)
#   WLS-4  ForkFeatureRegistry lists every Demo*Module INSIDE the demo fence      (it must not)
#   WLS-5  every surviving di/Fork<X>Module is owner:fork in customization-surface.yaml
#
# WLS-1..4 prove the seam survives `--clean`. WLS-5 proves it survives the OTHER thing that
# rewrites a fork's tree: `/kmp-project-template-sync`. Ownership is what decides that — a seam
# resolving `owner: template` is BLIND-COPIED, so the template's EMPTY Fork<X>Module silently
# replaces the fork's filled-in one on every sync. Surviving the demo strip and then being wiped
# by the next sync is the same loss the seam exists to prevent, just on a different trigger.
#
# exit 0 PASS / 1 FAIL. Pure bash + grep.
set -uo pipefail
: "${HEALTH_ROOT:?white-label-di-seams: HEALTH_ROOT not set (run via product-health.sh)}"
CORE="${WLS_CORE_DIR:-$HEALTH_ROOT/core}"
REG="${WLS_REGISTRY:-$HEALTH_ROOT/cmp-navigation/src/commonMain/kotlin/cmp/navigation/registry/ForkFeatureRegistry.kt}"
SURFACE_SH="${WLS_SURFACE_SH:-$HEALTH_ROOT/scripts/customization-surface.sh}"
SURFACE_YAML="${WLS_SURFACE_YAML:-$HEALTH_ROOT/customization-surface.yaml}"
fail=0

[ -d "$CORE" ] || { echo "no core/ — nothing to audit (ok)"; exit 0; }

# ── WLS-1 / WLS-2 — file placement ───────────────────────────────────────────
demo_mods=$(find "$CORE" -path '*/demo/di/Demo*Module.kt' -not -path '*/build/*' 2>/dev/null | sort)
while IFS= read -r f; do
  [ -z "$f" ] && continue
  layer="${f#$CORE/}"; layer="${layer%%/*}"
  base="$(basename "$f")"; seam="${base/Demo/Fork}"
  if ! find "$CORE/$layer" -path "*/di/$seam" -not -path '*/demo/*' -not -path '*/build/*' 2>/dev/null | grep -q .; then
    echo "❌ WLS-1 core/$layer has $base but no surviving seam di/$seam"
    echo "     → a cleaned fork would have nowhere to register $layer DI"
    fail=1
  fi
done <<< "$demo_mods"


stranded=$(find "$CORE" -path '*/demo/*' -name 'Fork*Module.kt' -not -path '*/build/*' 2>/dev/null)
if [ -n "$stranded" ]; then
  echo "❌ WLS-2 Fork*Module under demo/ — remove-demo.sh deletes it:"
  printf '%s\n' "$stranded" | sed "s|^$HEALTH_ROOT/|     |"
  echo "     → rename it Demo*Module, or move it to the layer's di/ package"
  fail=1
fi

# ── WLS-3 / WLS-4 — registry fence placement ─────────────────────────────────
if [ -f "$REG" ]; then
  # Split the registry at the demo fence: outside = survives --clean, inside = stripped.
  outside=$(awk '/demo:begin/{s=1} /demo:end/{s=0; next} !s' "$REG")
  inside=$(awk  '/demo:begin/{s=1; next} /demo:end/{s=0} s' "$REG")
  for f in $(find "$CORE" -path '*/di/Fork*Module.kt' -not -path '*/demo/*' -not -path '*/build/*' 2>/dev/null | sort); do
    n="$(basename "$f" .kt)"
    if ! printf '%s' "$outside" | grep -qE "^\s*$n,"; then
      if printf '%s' "$inside" | grep -qE "^\s*$n,"; then
        echo "❌ WLS-3 $n is listed INSIDE ForkFeatureRegistry's demo fence — --clean would drop the fork's seam"
      else
        echo "❌ WLS-3 $n is not registered in ForkFeatureRegistry.featureKoinModules at all"
      fi
      fail=1
    fi
  done
  for f in $demo_mods; do
    [ -z "$f" ] && continue
    n="$(basename "$f" .kt)"
    printf '%s' "$outside" | grep -qE "^\s*$n," && {
      echo "❌ WLS-4 $n is listed OUTSIDE the demo fence — it survives --clean but its bindings do not"
      fail=1
    }
  done
fi

# ── WLS-5 — the seam must survive a TEMPLATE SYNC, not just --clean ──────────
if [ -x "$SURFACE_SH" ] && [ -f "$SURFACE_YAML" ]; then
  for f in $(find "$CORE" -path '*/di/Fork*Module.kt' -not -path '*/demo/*' -not -path '*/build/*' 2>/dev/null | sort); do
    rel="${f#$HEALTH_ROOT/}"
    owner="$(CS_CONTRACT="$SURFACE_YAML" bash "$SURFACE_SH" resolve-owner "$rel" 2>/dev/null)"
    if [ "$owner" != "fork" ]; then
      echo "❌ WLS-5 $rel resolves owner:${owner:-<unmatched>} in customization-surface.yaml — must be 'fork'"
      echo "     → a sync BLIND-COPIES it, replacing the fork's filled-in seam with the template's empty one"
      fail=1
    fi
  done
else
  echo "⚠️  WLS-5 skipped — customization-surface contract not found ($SURFACE_YAML)"
fi

[ "$fail" = "0" ] || { echo "     → seams: demo/di/Demo*Module is stripped; di/Fork*Module survives --clean AND sync."; exit 1; }
n_seam=$(find "$CORE" -path '*/di/Fork*Module.kt' -not -path '*/demo/*' -not -path '*/build/*' 2>/dev/null | wc -l | tr -d ' ')
echo "white-label DI seams: $n_seam fork seam(s) survive --clean, demo aggregators correctly fenced"
