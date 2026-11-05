#!/usr/bin/env bash
# =============================================================================
# scripts/secrets/_lib.sh — shared helpers for the platform-wise secrets toolkit
# =============================================================================
# SELF-CONTAINED: bash + the repo's own stdlib-Ruby secrets resolver, ZERO framework/vault/yq
# dependency. Any OSS forker can run the toolkit after a plain `git clone`.
#
# Source of truth: each deployment/<platform>/<target>/secrets-needs.yaml lists only WHICH
# secrets/LAYOUT.yaml entries the target needs (`needs:`); LAYOUT holds every per-secret fact. The
# rows below (canonical · how to obtain it · CI secret · placeholder) are expanded from those two at
# run time by `deployment/_shared/lib/build_secrets.rb needs-expand` — nothing under deployment/
# stores them, so a target file is identical in the template and every fork. A legacy v1 file (one
# that still hand-authors vault_aliases/manual_inputs) is parsed directly, as before.
# =============================================================================
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"

# Colors (no-op if not a TTY)
if [ -t 1 ]; then C_B=$'\033[1m'; C_G=$'\033[32m'; C_Y=$'\033[33m'; C_R=$'\033[31m'; C_D=$'\033[2m'; C_0=$'\033[0m'
else C_B=; C_G=; C_Y=; C_R=; C_D=; C_0=; fi

# platform → deployment subdir. "apple" covers the iOS App Store / TestFlight /
# Firebase targets; the Apple-signed mac targets live under desktop/ but share
# the same secrets/apple/ files, so they resolve to the same canonical paths.
secrets_platform_dir() {
  case "$1" in
    android) echo "deployment/android" ;;
    apple|ios) echo "deployment/ios" ;;
    desktop) echo "deployment/desktop" ;;
    web) echo "deployment/web" ;;
    *) return 1 ;;
  esac
}

SECRETS_PLATFORMS="android apple web desktop"

# List every secrets-needs.yaml for a platform (one per deploy target).
secrets_needs_files() {
  local dir; dir="$(secrets_platform_dir "$1")" || return 1
  find "$REPO_ROOT/$dir" -name secrets-needs.yaml 2>/dev/null | sort
}

SECRETS_RESOLVER="$REPO_ROOT/deployment/_shared/lib/build_secrets.rb"

# Expand one v2 needs file via the resolver (stdout TSV, exit 0) — exit 3 means a legacy v1 file.
_secrets_expand() {  # <file> <manual|vault>
  ruby "$SECRETS_RESOLVER" needs-expand --file "$1" --kind "$2"
}

# One needs file → TSV: canonical \t source_hint \t gha_secret_var \t placeholder
_secrets_parse_manual_inputs() {
  local out rc
  out="$(_secrets_expand "$1" manual)"; rc=$?
  [ "$rc" -eq 3 ] || { [ -n "$out" ] && printf '%s\n' "$out"; return "$rc"; }
  # legacy v1: pure awk; tolerates quoted/unquoted values + the leading "- " on the first key.
  awk '
    function strip(s){ sub(/^[ \t]*-?[ \t]*[A-Za-z_]+:[ \t]*/,"",s); sub(/^"/,"",s); sub(/"[ \t]*$/,"",s); sub(/^'\''/,"",s); sub(/'\''[ \t]*$/,"",s); sub(/[ \t]+$/,"",s); return s }
    /^[A-Za-z]/ { in_mi=0 }                         # any top-level key closes the block
    /^manual_inputs:[ \t]*$/ { in_mi=1; next }
    in_mi && /^[ \t]+-[ \t]*canonical:/ {
      if (canon!="") print canon"\t"hint"\t"gha"\t"ph
      canon=strip($0); hint=""; gha=""; ph=""; next
    }
    in_mi && /source_hint:/    { hint=strip($0); next }
    in_mi && /gha_secret_var:/ { gha=strip($0);  next }
    in_mi && /placeholder:/    { ph=strip($0);   next }
    END { if (canon!="") print canon"\t"hint"\t"gha"\t"ph }
  ' "$1"
}

# One needs file → TSV: alias \t canonical
_secrets_parse_vault_aliases() {
  local out rc
  out="$(_secrets_expand "$1" vault)"; rc=$?
  [ "$rc" -eq 3 ] || { [ -n "$out" ] && printf '%s\n' "$out"; return "$rc"; }
  awk '
    function strip(s){ sub(/^[ \t]*-?[ \t]*[A-Za-z_]+:[ \t]*/,"",s); sub(/^"/,"",s); sub(/"[ \t]*$/,"",s); sub(/^'\''/,"",s); sub(/'\''[ \t]*$/,"",s); sub(/[ \t]+$/,"",s); return s }
    /^[A-Za-z]/ { in_va=0 }
    /^vault_aliases:[ \t]*$/ { in_va=1; next }
    in_va && /^[ \t]+-[ \t]*alias:/ { if (al!="") print al"\t"cn; al=strip($0); cn=""; next }
    in_va && /canonical:/ { cn=strip($0); next }
    END { if (al!="") print al"\t"cn }
  ' "$1"
}

# All (alias, canonical, platforms-csv) across every needs file, dedup'd by alias.
# Print every alias declared across all platform manifests, de-duplicated.
# De-duplicates on the CANONICAL name as well as the platform one, so an alias
# listed under two platforms is reported once rather than twice.
secrets_all_vault_aliases() {
  local p f plat
  local -A SEEN_CANON SEEN_PLAT
  for p in $SECRETS_PLATFORMS; do
    while IFS= read -r f; do
      [ -n "$f" ] || continue
      while IFS=$'\t' read -r al cn; do
        [ -n "$al" ] || continue
        [ -z "${SEEN_CANON[$al]:-}" ] && SEEN_CANON[$al]="$cn"
        case ",${SEEN_PLAT[$al]:-}," in *",$p,"*) : ;; *) SEEN_PLAT[$al]="${SEEN_PLAT[$al]:+${SEEN_PLAT[$al]},}$p" ;; esac
      done < <(_secrets_parse_vault_aliases "$f")
    done < <(secrets_needs_files "$p")
  done
  local a
  for a in "${!SEEN_CANON[@]}"; do printf '%s\t%s\t%s\n' "$a" "${SEEN_CANON[$a]}" "${SEEN_PLAT[$a]}"; done | sort
}

# Aggregate + DEDUP all manual_inputs for a platform by canonical path.
# Emits TSV: canonical \t source_hint \t gha_secret_var \t placeholder \t targets(csv)
secrets_platform_inputs() {
  local platform="$1" f tgt
  local -A SEEN_HINT SEEN_GHA SEEN_PH SEEN_TGT
  while IFS= read -r f; do
    [ -n "$f" ] || continue
    tgt="$(basename "$(dirname "$f")")"
    while IFS=$'\t' read -r canon hint gha ph; do
      [ -n "$canon" ] || continue
      [ -z "${SEEN_HINT[$canon]:-}" ] && SEEN_HINT[$canon]="$hint"
      [ -z "${SEEN_GHA[$canon]:-}" ]  && SEEN_GHA[$canon]="$gha"
      [ -z "${SEEN_PH[$canon]:-}" ]   && SEEN_PH[$canon]="$ph"
      SEEN_TGT[$canon]="${SEEN_TGT[$canon]:+${SEEN_TGT[$canon]},}$tgt"
    done < <(_secrets_parse_manual_inputs "$f")
  done < <(secrets_needs_files "$platform")
  local c
  for c in "${!SEEN_HINT[@]}"; do
    printf '%s\t%s\t%s\t%s\t%s\n' "$c" "${SEEN_HINT[$c]}" "${SEEN_GHA[$c]}" "${SEEN_PH[$c]}" "${SEEN_TGT[$c]}"
  done | sort
}

# For an `env:VAR` need, the local file config.rb reads as the ENV fallback
# (mirrors deployment/_shared/config.rb's `_secret("VAR", "<path>")` second arg).
# Lets the wizard persist a value locally instead of an ephemeral export.
secrets_env_local_file() {
  # Paths under secrets/live/ — the resolver root (secrets/LAYOUT.yaml roots.live);
  # keeps Path-B (vault) materialization aligned with what build-secrets reads.
  case "$1" in
    MATCH_PASSWORD)         echo "secrets/live/apple/match/.match_password" ;;
    KEYCHAIN_PASSWORD)      echo "secrets/live/apple/match/keychain_password" ;;
    CERTIFICATES_PASSWORD)  echo "secrets/live/apple/match/certificates_password" ;;
    KEYSTORE_PASSWORD)      echo "secrets/live/android/keystores/keystore_password" ;;
    KEYSTORE_ALIAS)         echo "secrets/live/android/keystores/keystore_alias" ;;
    KEYSTORE_ALIAS_PASSWORD)echo "secrets/live/android/keystores/keystore_alias_password" ;;
    *)                      echo "secrets/live/_env/$1" ;;
  esac
}

# Is a canonical entry satisfied on disk? Handles "env:VAR" (checks ENV OR its
# local fallback file) vs a plain file path.
secrets_canonical_present() {
  local canon="$1"
  case "$canon" in
    env:*) local v="${canon#env:}"; [ -n "${!v:-}" ] || [ -s "$REPO_ROOT/$(secrets_env_local_file "$v")" ] ;;
    *) [ -s "$REPO_ROOT/$canon" ] ;;
  esac
}
