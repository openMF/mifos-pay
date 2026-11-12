#!/usr/bin/env bash
#
# verify-demo-convention.sh — static gate for the demo⇄framework separation convention
# (epic showcase-framework-separation). Fails (exit 1) on any convention drift so the
# `customizer --clean` removal + `sync-dirs` sync guarantees stay sound.
#
# The template ships a demo showcase that a fork removes with `scripts/white-label/fork-init.sh --clean`.
# That removal is line-based (strips `// demo:begin … // demo:end` blocks + deletes
# `**/demo/**` packages + the demo feature modules), so the convention MUST hold or the
# strip corrupts source. This gate enforces it. Pure bash + grep + find — no Gradle, no
# network — so it runs in a few hundred ms locally and in CI.
#
# Checks:
#   C1  demo-location    — every file whose package is `*.demo[.…]` lives under a `/demo/` dir
#   C2  marker-balance   — every `.kt`/`.kts` has equal `// demo:begin` and `// demo:end` counts
#   C3  no-prose-markers — the tokens `demo:begin`/`demo:end` appear ONLY as clean markers
#                          (a stray token in prose makes the awk stripper delete real code)
#   C4  settings↔dirs    — every `feature:X` inside the settings demo-block has a `feature/X/` dir
#   C5  sync-exclusion   — `scripts/white-label/sync-dirs.sh` excludes `*/demo/*` (fork demo never synced/overwritten)
#
# Usage: bash scripts/verify-demo-convention.sh      # exit 0 clean, exit 1 on any violation
set -euo pipefail
cd "$(dirname "$0")/.."

violations=0
# Record a convention violation and keep scanning, so one run lists every offending file.
viol() { echo "VIOLATION [$1]: $2" >&2; violations=$((violations + 1)); }

# ── C1: demo package only under a demo/ directory ────────────────────────────────────
# A file declaring `package kpt.…​.demo…` must physically sit under a `/demo/` path segment.
while IFS= read -r f; do
  [ -z "$f" ] && continue
  case "$f" in */demo/*) : ;; *) viol C1-demo-location "$f declares a .demo package but is not under a demo/ directory" ;; esac
done < <(grep -rlE '^package +[a-zA-Z0-9_.]*\.demo(\.|$)' core feature --include='*.kt' 2>/dev/null | grep -v '/build/' || true)

# ── C2 + C3: marker integrity across every surviving .kt/.kts/.yaml ───────────────────
# .yaml is IN SCOPE: app-profile/app.yaml carries real fences (the database block), and a prose
# `demo:begin` there is worse than in Kotlin — an unpaired begin token makes the stripper skip from
# that line to the next `demo:end`, silently deleting whole top-level keys. That is not theoretical:
# a comment ABOUT the fence mechanism, written one line above `access_points:`, deleted the entire
# access-point list and the head of the database block.
while IFS= read -r f; do
  [ -z "$f" ] && continue
  # `//` (Kotlin) and `#` (YAML) are both clean marker styles — the stripper is line-based and
  # comment-agnostic, so the convention has to be too.
  begins=$(grep -cE '^[[:space:]]*(//|#) demo:begin([[:space:]].*)?$' "$f" || true)
  ends=$(grep -cE '^[[:space:]]*(//|#) demo:end$' "$f" || true)
  [ "$begins" != "$ends" ] && viol C2-marker-balance "$f has $begins clean 'demo:begin' vs $ends 'demo:end' (must match)"
  # any line mentioning a marker token that is NOT a clean standalone marker = prose token
  while IFS=: read -r ln _; do
    viol C3-prose-marker "$f:$ln contains a demo:begin/demo:end token in prose (corrupts the stripper)"
  done < <(grep -nE 'demo:(begin|end)' "$f" 2>/dev/null \
             | grep -vE ':[[:space:]]*(//|#) demo:begin([[:space:]].*)?$' \
             | grep -vE ':[[:space:]]*(//|#) demo:end$' || true)
done < <(grep -rlE 'demo:(begin|end)' . --include='*.kt' --include='*.kts' --include='*.yaml' 2>/dev/null \
           | grep -v '/build/' | grep -v '/product-health/tests/' || true)

# ── C4: every demo-block feature module has a matching directory ──────────────────────
while IFS= read -r feat; do
  [ -z "$feat" ] && continue
  [ -d "feature/$feat" ] || viol C4-settings-dirs "settings.gradle.kts demo-block declares feature:$feat but feature/$feat/ does not exist"
done < <(awk '/demo:begin/{s=1;next} /demo:end/{s=0} s' settings.gradle.kts 2>/dev/null \
           | grep -oE 'feature:[a-z-]+' | sed 's/feature://' || true)

# ── C5: scripts/white-label/sync-dirs.sh excludes the demo convention ─────────────────────────────────────
if [ -f scripts/white-label/sync-dirs.sh ]; then
  grep -qE '\*/demo/\*' scripts/white-label/sync-dirs.sh || viol C5-sync-exclusion "scripts/white-label/sync-dirs.sh is_excluded() does not exclude */demo/* (fork demo would be synced/overwritten)"
else
  viol C5-sync-exclusion "scripts/white-label/sync-dirs.sh not found at repo root"
fi

# ── verdict ───────────────────────────────────────────────────────────────────────────
if [ "$violations" -eq 0 ]; then
  echo "✅ demo-convention: clean (C1 location · C2 marker-balance · C3 no-prose-markers · C4 settings↔dirs · C5 sync-exclusion)"
  exit 0
else
  echo "❌ demo-convention: $violations violation(s) — see above" >&2
  exit 1
fi
