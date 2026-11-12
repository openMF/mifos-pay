#!/usr/bin/env bash
# checks/ownership-naming.sh — a source file's name prefix says who owns it, and the contract agrees.
#
#   Kpt*       template-owned — a sync replaces it in full; never edit it in a fork
#   KptFork*   merge-owned    — template code with fork pieces; a sync 3-way merges it
#   Fork*      fork-owned     — the fork's own; a sync never touches it
#
# The name is for the reader: at a glance, before opening the contract, a fork developer knows
# whether an edit survives the next template sync. It is only worth that if it never lies, so every
# Kotlin/Swift/Gradle-script file carrying one of the prefixes must RESOLVE to the matching owner
# (scripts/customization-surface.sh — the one reader). The rule runs one way: fork code at large needs
# no prefix (every feature a fork writes is fork-owned without one).
#
#   ON-1 every Kpt* / KptFork* / Fork* file resolves to the owner its prefix names
#
# Fixtures (tests/, scripts/product-health/tests/) mimic real trees and are skipped.
# exit 0 PASS / 1 FAIL. Pure bash.
set -uo pipefail
: "${HEALTH_ROOT:?ownership-naming: HEALTH_ROOT not set (run via product-health.sh)}"
CS="${ON_CS:-$HEALTH_ROOT/scripts/customization-surface.sh}"
[ -f "$CS" ] || { echo "ownership-naming: no customization-surface.sh (skip)"; exit 0; }

bad="$(cd "$HEALTH_ROOT" && CS_ROOT="$HEALTH_ROOT" bash -c '. "$1" >/dev/null 2>&1
  git ls-files "*.kt" "*.kts" "*.swift" | grep -E "/(Kpt|Fork)[^/]*$" | grep -vE "^(tests|scripts/product-health/tests)/" |
  while IFS= read -r f; do
    case "${f##*/}" in KptFork*) want=merge ;; Fork*) want=fork ;; *) want=template ;; esac
    cs_match_g "$f"
    [ "$CS_M_OWNER" = "$want" ] || printf "  %s — named %s, contract says %s\n" "$f" "$want" "$CS_M_OWNER"
  done' _ "$CS")"

if [ -n "$bad" ]; then
  echo "ON-1 FAIL — file name and ownership disagree (rename the file, or fix its contract row):"
  echo "$bad"
  exit 1
fi
echo "ownership naming coherent (Kpt* template · KptFork* merge · Fork* fork)"
exit 0
