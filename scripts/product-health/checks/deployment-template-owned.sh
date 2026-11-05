#!/usr/bin/env bash
# deployment-template-owned.sh — syncForkConfig never writes fork identity into deployment/.
#
# WHY THIS EXISTS
# deployment/** is owner: template in customization-surface.yaml — a full copy on every template sync.
# syncForkConfig used to regex-rewrite fork values into it (alias prefixes in every secrets-needs.yaml,
# the Cloudflare project name, the MSIX identity, a generated StoreBroker config), so each sync
# overwrote a fork's files with the template's and the next syncForkConfig rewrote them again
# (2026-10-29). Fork values now reach deployment at RUN time (deployment/_shared/scripts/
# fork-identity.sh) or through the one generated root secrets-manifest.yaml.
#
#   DT-1 every deployment/ path syncForkConfig names is inside a GENERATED area (store metadata,
#        screenshots, og-images, app-content — owner: generated) or is the resolver it runs
#   DT-2 no blanket walk of deployment/ (File(root, "deployment").walk…), which rewrote every target
#
# Exit 0 = PASS · 1 = FAIL (blocks).
set -uo pipefail
ROOT="$(cd "${1:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)}" 2>/dev/null && pwd)" || { echo "  ✗ DT-0 cannot enter ${1:-}"; exit 2; }
P="$ROOT/build-logic/convention/src/main/kotlin/SyncForkConfigPlugin.kt"
[ -f "$P" ] || { echo "  ✓ DT-1 no SyncForkConfigPlugin.kt in this tree — nothing to check"; exit 0; }
ALLOWED='^deployment/[^"]*/(metadata|screenshots|og-images|app-content)(/|$)|^deployment/_shared/lib/build_secrets\.rb$'
fail=0
# Code lines only (a // comment may name a path); each "deployment/…" string literal must be allowed.
bad="$(grep -nE '"deployment/' "$P" | grep -vE '^[0-9]+:[[:space:]]*(//|\*)' \
        | grep -oE '^[0-9]+:|"deployment/[^"]*"' | awk '/^[0-9]+:$/{n=$0; next} {gsub(/"/,""); print n" "$0}' \
        | while read -r n path; do printf '%s' "$path" | grep -qE "$ALLOWED" || echo "$n $path"; done)"
if [ -n "$bad" ]; then
  echo "  ✗ DT-1 syncForkConfig names template-owned deployment/ paths (render fork values at run time instead —"
  echo "        deployment/_shared/scripts/fork-identity.sh — or move the file into a generated area):"
  printf '%s\n' "$bad" | sed 's/^/      line /'; fail=1
else
  echo "  ✓ DT-1 syncForkConfig writes deployment/ only inside generated areas"
fi
if grep -nE 'File\(root, "deployment"\)\.walk' "$P" | grep -vqE '^[0-9]+:[[:space:]]*//'; then
  echo "  ✗ DT-2 a blanket walk of deployment/ is back:"; grep -nE 'File\(root, "deployment"\)\.walk' "$P" | sed 's/^/      line /'; fail=1
else
  echo "  ✓ DT-2 no blanket walk of deployment/"
fi
exit $fail
