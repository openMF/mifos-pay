#!/usr/bin/env bash
# scripts/product-health/lib.sh — shared helpers for the product-health harness.
#
# gradle/fork.properties is the SINGLE SOURCE OF TRUTH for project-level data (identity,
# signing, testers, firebase, store listing, legal). EVERY health check reads it through
# fp_get() so there is exactly one parser and one SoT — a check never re-implements the
# read. Sourced by project-health.sh and each checks/*.sh; never executed directly.

# fp_file + fp_get now live in scripts/_shared/fork-props.sh — the ONE bash reader, promoted out of
# this harness so every bash caller shares the same parsing semantics rather than re-deriving them.
# This file keeps the rule it always stated; it just no longer owns the only copy of it.
# shellcheck source=../_shared/fork-props.sh
. "$(cd "$(dirname "${BASH_SOURCE[0]}")/../_shared" && pwd)/fork-props.sh"

# health_resolve_sot <repo_root> — the harness-facing name for fp_file. Kept as a thin alias so the
# 14 checks/*.sh that call it are untouched, and any caller-provided $FORK_PROPERTIES fixture path
# still wins (that is what lets each canary point the checks at its own tree).
health_resolve_sot() { fp_file "$1"; }

# Colors — only when stdout is a tty.
if [ -t 1 ]; then
  C_RED=$'\033[0;31m'; C_GRN=$'\033[0;32m'; C_YEL=$'\033[0;33m'; C_DIM=$'\033[2m'; C_BLD=$'\033[1m'; C_RST=$'\033[0m'
else
  C_RED=; C_GRN=; C_YEL=; C_DIM=; C_BLD=; C_RST=
fi

# ── White-label placeholder vocabulary (WHITE_LABEL_PLACEHOLDERS.yaml) ─────────
# The neutral upstream template ships every identity/brand field as a DECLARED
# placeholder. The three identity checks (fork-identity / deployment-whitelabel /
# store-listing) load that single-source vocabulary through wl_placeholders_load so
# they are directional + template-aware (pure-white-label-100 WS6, RULE-TEMPLATE-
# MODULE-FIX-UPSTREAM-001-class generalization). Pure bash + grep + awk — no jq.
: "${WL_PLACEHOLDERS_YAML:=}"
if [ -z "$WL_PLACEHOLDERS_YAML" ]; then
  # 1. Template-local vendored copy — next to this lib. This is what a STANDALONE template
  #    checkout (CI) and every FORK resolve, since neither has the framework monorepo on disk.
  _self_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
  if [ -f "$_self_dir/WHITE_LABEL_PLACEHOLDERS.yaml" ]; then
    WL_PLACEHOLDERS_YAML="$_self_dir/WHITE_LABEL_PLACEHOLDERS.yaml"
  else
    # 2. Dev fallback: walk up from HEALTH_ROOT to the framework's core/registries copy (monorepo).
    _d="${HEALTH_ROOT:-$(pwd)}"
    while [ "$_d" != "/" ] && [ -n "$_d" ]; do
      if [ -f "$_d/core/registries/WHITE_LABEL_PLACEHOLDERS.yaml" ]; then
        WL_PLACEHOLDERS_YAML="$_d/core/registries/WHITE_LABEL_PLACEHOLDERS.yaml"; break
      fi
      _d="$(dirname "$_d")"
    done
  fi
fi
export WL_PLACEHOLDERS_YAML

# wl_placeholders_load <category> — emit one regex pattern per line under placeholders.<category>.
# The reset guard (any top-level `^key:` other than placeholders: closes the section) keeps this
# robust when a sibling top-level block (e.g. example_identity:) follows placeholders: in the file.
wl_placeholders_load() {
  local cat="$1"
  [ -f "$WL_PLACEHOLDERS_YAML" ] || { echo "wl_placeholders_load: WHITE_LABEL_PLACEHOLDERS.yaml not found" >&2; return 2; }
  awk -v cat="$cat" '
    /^placeholders:/   { in_ph=1; next }
    /^[a-zA-Z_]+:/ && !/^placeholders:/ { in_ph=0 }
    in_ph && /^  [a-z_]+:[[:space:]]*$/ { curr=$1; sub(/:$/,"",curr); next }
    in_ph && curr==cat && /^    - / {
      val=$0; sub(/^    - /,"",val)
      gsub(/^["'\''"]/,"",val); gsub(/["'\''"]$/,"",val)
      print val
    }
  ' "$WL_PLACEHOLDERS_YAML"
}

# wl_matches_any <value> <category> — return 0 if value matches ANY pattern under category, else 1.
wl_matches_any() {
  local val="$1" cat="$2" pat
  while IFS= read -r pat; do
    [ -n "$pat" ] || continue
    # printf '%s\n' (NOT '%s'): an EMPTY $val must still present one empty LINE to grep, or the
    # declared '^$' placeholder pattern can never match — grep sees zero lines and reports no match.
    # That silently classified an unset field as "a foreign real-brand value" instead of a
    # placeholder, which is the opposite verdict. Non-empty values are unaffected.
    printf '%s\n' "$val" | grep -qE "$pat" && return 0
  done < <(wl_placeholders_load "$cat")
  return 1
}

# wl_example_load <category> — emit one regex per line under example_identity.<category>.
# example_identity is the template's COMMITTED reference/demo identity (Mifos "Money Toolkit"):
# template-mode gates ACCEPT it; fork-mode gates FAIL it (a fork must diverge from the reference).
# A category with NO example block (e.g. signing: apple_team_id / apple_match_git_url) emits nothing
# → wl_matches_example is always false for it → it stays placeholder-only (signing is never committed).
wl_example_load() {
  local cat="$1"
  [ -f "$WL_PLACEHOLDERS_YAML" ] || { echo "wl_example_load: WHITE_LABEL_PLACEHOLDERS.yaml not found" >&2; return 2; }
  awk -v cat="$cat" '
    /^example_identity:/ { in_ex=1; next }
    /^[a-zA-Z_]+:/ && !/^example_identity:/ { in_ex=0 }
    in_ex && /^  [a-z_]+:[[:space:]]*$/ { curr=$1; sub(/:$/,"",curr); next }
    in_ex && curr==cat && /^    - / {
      val=$0; sub(/^    - /,"",val)
      gsub(/^["'\''"]/,"",val); gsub(/["'\''"]$/,"",val)
      print val
    }
  ' "$WL_PLACEHOLDERS_YAML"
}

# wl_identity_is_reference <app_id> <org_name> — return 0 when BOTH still carry the template's
# COMMITTED Mifos reference identity, i.e. this checkout looks like the upstream template rather
# than a rebranded fork.
#
# Deliberately NOT a mode switch. Mode is decided ONLY by TEMPLATE_SELF_BUILD (set explicitly by
# quality-gate.yml from `github.repository`), because the safe default for anything that is not
# provably the upstream repo is FORK mode — that is the direction that TELLS a fork to rebrand.
# Inferring template mode locally would hand a real fork a silent pass on exactly the checks it
# needs most. This predicate exists solely so product-health.sh can print an actionable hint when
# a LOCAL run in the template checkout trips the directional identity checks.
#
# Requires BOTH fields: a half-rebranded fork (own appId, org.name still "Mifos Initiative") is a
# genuine finding, and must not be talked out of its failure by the hint.
wl_identity_is_reference() {
  local app_id="$1" org_name="$2"
  wl_matches_example "$app_id" bundle_id || return 1
  wl_matches_example "$org_name" org_name || return 1
  return 0
}

# wl_matches_example <value> <category> — return 0 if value IS the declared reference identity.
wl_matches_example() {
  local val="$1" cat="$2" pat
  while IFS= read -r pat; do
    [ -n "$pat" ] || continue
    # printf '%s\n' (NOT '%s'): an EMPTY $val must still present one empty LINE to grep, or the
    # declared '^$' placeholder pattern can never match — grep sees zero lines and reports no match.
    # That silently classified an unset field as "a foreign real-brand value" instead of a
    # placeholder, which is the opposite verdict. Non-empty values are unaffected.
    printf '%s\n' "$val" | grep -qE "$pat" && return 0
  done < <(wl_example_load "$cat")
  return 1
}

# wl_demos_stripped <repo_root> — return 0 when the template's demo showcase has been STRIPPED from this
# checkout: core/store/module-packages.yaml declares demo rows and every one is `status: removed`.
# That status is derived from disk and proven by core-package-ownership MP-7, so it is a fact, not a
# flag a fork sets. On the template (and on a fork that kept the demos) the rows say `exists` and
# this returns 1, so demo-showcase checks keep running there.
wl_demos_stripped() {
  local reg="${1:-.}/core/store/module-packages.yaml"
  [ -f "$reg" ] || return 1
  awk '
    /^demo:[[:space:]]*$/ { in_demo=1; next }
    /^[a-z_]+:/           { in_demo=0 }
    in_demo && /id:/      { n++; if ($0 !~ /status:[[:space:]]*removed/) live++ }
    END { exit (n > 0 && live == 0) ? 0 : 1 }
  ' "$reg"
}

# Guard against direct execution — this is a library.
if [ "${BASH_SOURCE[0]}" = "${0}" ]; then
  echo "lib.sh is a library — source it, don't run it." >&2; exit 64
fi
