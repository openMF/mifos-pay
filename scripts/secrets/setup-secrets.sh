#!/usr/bin/env bash
# =============================================================================
# scripts/secrets/setup-secrets.sh — interactive, platform-wise secret setup
# =============================================================================
# Walks the MISSING secrets a platform needs (from secrets-needs.yaml), shows
# how to obtain each, and writes it to the right canonical path. Pure bash —
# any OSS forker can run this with no framework, no vault, no yq.
#
#   scripts/secrets/setup-secrets.sh apple        # one platform
#   scripts/secrets/setup-secrets.sh all          # every platform
#   scripts/secrets/setup-secrets.sh apple --force # re-prompt already-present
#
# Secret VALUES are read with hidden input or copied from a file path you give —
# never passed on the command line, never echoed back.
# =============================================================================
set -euo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"; . "$DIR/_lib.sh"

platforms="${1:-}"; force=0
[ "${2:-}" = "--force" ] && force=1
[ -z "$platforms" ] && { echo "usage: $0 <${SECRETS_PLATFORMS// /|}|all> [--force]"; exit 2; }
[ "$platforms" = all ] && platforms="$SECRETS_PLATFORMS"

# Write stdin to repo-relative path $1 with mode $2 (default 0600).
# Creates parent directories and chmods AFTER writing, so a secret is never briefly
# world-readable between creation and the mode change.
_write_file() {  # path  mode(0600|0644)
  local dest="$REPO_ROOT/$1" mode="${2:-0600}"
  mkdir -p "$(dirname "$dest")"; cat > "$dest"; chmod "$mode" "$dest"
}

done_n=0; skip_n=0
for p in $platforms; do
  secrets_platform_dir "$p" >/dev/null 2>&1 || { echo "${C_R}unknown platform: $p${C_0}"; exit 2; }
  echo "${C_B}━━ Setting up ${p} secrets ━━━━━━━━━━━━━━━━━━━━━━━━━━━${C_0}"
  while IFS=$'\t' read -r canon hint gha ph tgts; do
    [ -n "$canon" ] || continue
    if [ "$force" = 0 ] && secrets_canonical_present "$canon"; then
      printf '  %s✓ already set%s  %s\n' "$C_G" "$C_0" "$canon"; continue
    fi
    # Resolve where it lands + whether it's a password-style value or a file.
    is_env=0; dest="$canon"
    case "$canon" in env:*) is_env=1; dest="$(secrets_env_local_file "${canon#env:}")" ;; esac
    echo
    echo "  ${C_B}${canon}${C_0}  ${C_D}(${tgts})${C_0}"
    [ -n "$hint" ] && echo "    ${C_Y}how:${C_0} $hint"
    [ -n "$gha" ]  && echo "    ${C_D}CI secret: $gha${C_0}"
    echo "    ${C_D}→ $dest${C_0}"

    # Pick input mode by placeholder shape.
    case "$ph" in
      *json*|*p8*|*key*|*keystore*|*cert*|*provision*|*.jks*)
        # file-backed secret: ask for a path to copy
        printf '    file path (or [s]kip): '; read -r src
        [ "$src" = s ] || [ -z "$src" ] && { echo "    ${C_D}skipped${C_0}"; skip_n=$((skip_n+1)); continue; }
        src="${src/#\~/$HOME}"
        [ -f "$src" ] || { echo "    ${C_R}no such file — skipped${C_0}"; skip_n=$((skip_n+1)); continue; }
        _write_file "$dest" 0600 < "$src"
        echo "    ${C_G}✓ stored ($(wc -c < "$REPO_ROOT/$dest" | tr -d ' ')B)${C_0}"; done_n=$((done_n+1)) ;;
      *)
        # short value / password: hidden input (no echo, no shell history)
        printf '    value (hidden, or empty to skip): '; read -rs val; echo
        [ -z "$val" ] && { echo "    ${C_D}skipped${C_0}"; skip_n=$((skip_n+1)); continue; }
        printf '%s' "$val" | _write_file "$dest" 0600
        echo "    ${C_G}✓ stored ($([ "$is_env" = 1 ] && echo 'env-fallback file' || echo 'value'))${C_0}"; done_n=$((done_n+1)); unset val ;;
    esac
  done < <(secrets_platform_inputs "$p")
  echo
done

echo "${C_B}Done: ${done_n} written · ${skip_n} skipped.${C_0}"
echo "${C_D}Verify with: scripts/secrets/secrets-status.sh ${1}${C_0}"
[ "$skip_n" -gt 0 ] && echo "${C_Y}Re-run any time to fill the skipped ones.${C_0}" || true
