#!/usr/bin/env bash
# checks/store-listing.sh — DIRECTIONAL, placeholder-aware store-copy guard.
#
# fork-identity.sh covers the SIGNING/org fields (Apple rejects a wrong team). This is the softer
# twin: the store *content* (title, subtitle, description, promo, copyright) that ships to the App
# Store / Play listing. It loads the single-source placeholder vocabulary
# (core/registries/WHITE_LABEL_PLACEHOLDERS.yaml, resolved by lib.sh) — replacing the old hardcoded
# TEMPLATE_MARKER regex and the blanket TEMPLATE_SELF_BUILD=1 skip (pure-white-label-100 WS6).
#
#   template (TEMPLATE_SELF_BUILD=1) → every store-copy key MUST match a placeholder marker; an
#                                      authored value is a real-brand leak → FAIL (exit 1).
#   fork     (TEMPLATE_SELF_BUILD unset) → NO key may still match a placeholder marker; WARN (exit 2),
#                                      since prose is human-authored and must not ship-block a build.
#
# exit 0 PASS / 1 FAIL (template leak) / 2 WARN (fork unfilled).
set -uo pipefail
# shellcheck source=scripts/product-health/lib.sh
source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib.sh"
: "${FORK_PROPERTIES:?store-listing: FORK_PROPERTIES not set (run via product-health.sh)}"

is_template="${TEMPLATE_SELF_BUILD:-0}"
# Union placeholder regex across the three categories that describe "template store copy".
MARKER_RE="$(
  {
    wl_placeholders_load app_display_name
    wl_placeholders_load org_name
    wl_placeholders_load store_copy_marker
  } | paste -sd'|' -
)"
# The Mifos REFERENCE store copy the template ships (example_identity.store_copy_reference).
REF_RE="$(wl_example_load store_copy_reference | paste -sd'|' -)"
KEYS=(store.title store.subtitle store.description store.promotional.text app.description store.copyright)

leaked=(); reference=(); authored=()
for key in "${KEYS[@]}"; do
  val="$(fp_get "$key")"
  # printf (NOT echo): store copy carries literal '\n' paragraph breaks — echo would interpret them
  # into real newlines, and the empty-value marker '^$' would then false-match a blank paragraph line.
  if [ -n "$MARKER_RE" ] && grep -qiE "$MARKER_RE" <<<"$val"; then
    leaked+=("$key")
  elif [ -n "$REF_RE" ] && grep -qiE "$REF_RE" <<<"$val"; then
    reference+=("$key")
  else
    authored+=("$key")
  fi
done

if [ "$is_template" = "1" ]; then
  # The template presents store copy in one of TWO valid shapes, and store-listing reads it via fp_get
  # (gradle/fork.properties → falls back to fork.properties.template):
  #   • CI checkout: no generated fork.properties → the neutral fork.properties.template PLACEHOLDERS.
  #   • local post-syncForkConfig: the committed Mifos "Money Toolkit" example copy (brand keys carry
  #     the reference; descriptive keys like the subtitle are brand-free prose).
  # Both are legitimate. PASS if there is ANY template signal — the Mifos reference appears anywhere
  # (→ example copy; brand-free descriptive keys are fine) OR any key is a placeholder marker (→ neutral
  # fallback; not every key carries a marker, e.g. the .template's store.description is plain prose). FAIL
  # ONLY when the copy is WHOLESALE foreign — no reference and no placeholder marker anywhere (a real
  # rebrand leak). Structured identity leaks are caught by fork-identity + deployment-whitelabel B1/B8.
  if [ "${#reference[@]}" -gt 0 ] || [ "${#leaked[@]}" -gt 0 ]; then
    echo "template store copy OK (Mifos reference example or neutral placeholders — directional PASS)"
    exit 0
  fi
  echo "❌ store copy on the TEMPLATE is WHOLESALE foreign — no Mifos reference and no placeholder marker present (a real-brand leak):"
  for k in "${authored[@]}"; do echo "     · $k"; done
  exit 1
fi

# fork: NO key may still be a placeholder marker OR the Mifos reference copy; WARN (exit 2), since prose is human-authored.
if [ "${#leaked[@]}" -ne 0 ] || [ "${#reference[@]}" -ne 0 ]; then
  echo "⚠️  store copy still carries template placeholder(s) or the Mifos reference copy — author for your app in gradle/fork.properties (WHITE_LABEL_PLACEHOLDERS.yaml):"
  for k in "${leaked[@]}" "${reference[@]}"; do echo "     · $k"; done
  echo "   (non-blocking: the listing ships to App Store / Play — write your own before you release.)"
  exit 2
fi
echo "store listing is authored for this fork"
exit 0
