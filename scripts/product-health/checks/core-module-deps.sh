#!/usr/bin/env bash
# checks/core-module-deps.sh — every core/<mod> build file full-copies, and its fork deps live in a seam.
#
# `core/<mod>/build.gradle.kts` used to be `owner: merge` (kotlin-3way) for ONE reason: a fork adds
# module dependencies its own code compiles against, and a template plugin/version bump must not drop
# them. That put a permanent merge surface on every core module — the template could not evolve its
# build files freely, and an adopting fork got a 3-way merge per module on every sync.
#
# The fix mirrors `feature-deps.gradle.kts`: the fork's deps move to `core/<mod>/module-deps.gradle.kts`
# (owner:fork, never synced) which the template build file applies. Nothing fork-specific is then
# hand-written in the build file, so it becomes `owner: template` and FULL-COPIES.
#
#   CMD-1  every core/<mod>/build.gradle.kts applies module-deps.gradle.kts (guarded)
#   CMD-2  every core/<mod>/module-deps.gradle.kts resolves owner:fork      (survives a sync)
#   CMD-3  every core/<mod>/build.gradle.kts resolves owner:template        (the flip is the payoff)
#
# CMD-3 is not cosmetic: if the glob silently reverts to `merge`, the seam still works but the merge
# surface is back and the whole exercise bought nothing — a silent regression the other two miss.
#
# exit 0 PASS / 1 FAIL. Pure bash + grep.
set -uo pipefail
: "${HEALTH_ROOT:?core-module-deps: HEALTH_ROOT not set (run via product-health.sh)}"
CORE="${CMD_CORE_DIR:-$HEALTH_ROOT/core}"
SURFACE_SH="${CMD_SURFACE_SH:-$HEALTH_ROOT/scripts/customization-surface.sh}"
SURFACE_YAML="${CMD_SURFACE_YAML:-$HEALTH_ROOT/customization-surface.yaml}"
fail=0

[ -d "$CORE" ] || { echo "no core/ — nothing to audit (ok)"; exit 0; }

resolve() { CS_CONTRACT="$SURFACE_YAML" bash "$SURFACE_SH" resolve-owner "$1" 2>/dev/null; }
have_surface=0
[ -x "$SURFACE_SH" ] && [ -f "$SURFACE_YAML" ] && have_surface=1

n=0
for bf in "$CORE"/*/build.gradle.kts; do
  [ -f "$bf" ] || continue
  n=$((n + 1))
  mod="$(basename "$(dirname "$bf")")"
  seam="$(dirname "$bf")/module-deps.gradle.kts"
  rel_bf="${bf#$HEALTH_ROOT/}"
  rel_seam="${seam#$HEALTH_ROOT/}"

  # CMD-1 — the build file must APPLY the seam, or the fork's deps are inert.
  if ! grep -q 'module-deps.gradle.kts' "$bf"; then
    echo "❌ CMD-1 core/$mod/build.gradle.kts does not apply module-deps.gradle.kts"
    echo "     → add: project.file(\"module-deps.gradle.kts\").takeIf { it.exists() }?.let { apply(from = it) }"
    fail=1
  fi

  [ "$have_surface" = "1" ] || continue

  # CMD-2 — the seam must survive a sync. owner:template would blind-copy the template's empty one.
  if [ -f "$seam" ]; then
    owner="$(resolve "$rel_seam")"
    if [ "$owner" != "fork" ]; then
      echo "❌ CMD-2 $rel_seam resolves owner:${owner:-<unmatched>} — must be 'fork'"
      echo "     → a sync would replace the fork's deps with the template's empty seam"
      fail=1
    fi
  fi

  # CMD-3 — the build file must be full-copy; anything else means the merge surface came back.
  owner="$(resolve "$rel_bf")"
  if [ "$owner" != "template" ]; then
    echo "❌ CMD-3 $rel_bf resolves owner:${owner:-<unmatched>} — must be 'template' (full-copy)"
    echo "     → fork deps belong in module-deps.gradle.kts; the build file has no fork-specific content"
    fail=1
  fi
done

[ "$have_surface" = "1" ] || echo "⚠️  CMD-2/CMD-3 skipped — customization-surface contract not found ($SURFACE_YAML)"
[ "$fail" = "0" ] || exit 1
echo "core module deps: $n core build file(s) full-copy; fork deps isolated in module-deps.gradle.kts seams"
