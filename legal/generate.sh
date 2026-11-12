#!/usr/bin/env bash
# legal/generate.sh
#
# Renders privacy-policy and terms templates into legal/output/.
#
# Usage (from repo root):
#   bash legal/generate.sh
#
# Prerequisites:
#   - gradle/fork.properties filled in from gradle/fork.properties.template
#   - envsubst available (part of GNU gettext — brew install gettext on macOS)
#
# Output:
#   legal/output/privacy-policy.md  — commit this file; use its raw GitHub URL
#   legal/output/terms.md           — commit this file; use its raw GitHub URL
#
# CI override (skip fork.properties, inject via ENV):
#   COMPANY_NAME="Acme" JURISDICTION="India" EFFECTIVE_DATE="June 1, 2026" \
#   LEGAL_CONTACT_EMAIL="privacy@acme.com" bash legal/generate.sh

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LEGAL_DIR="$REPO_ROOT"
OUTPUT_DIR="$LEGAL_DIR/output"

# ── Dependency check ──────────────────────────────────────────────────────────

if ! command -v envsubst &>/dev/null; then
  echo "ERROR: envsubst not found."
  echo "  macOS: brew install gettext && brew link gettext --force"
  echo "  Linux: apt-get install gettext / yum install gettext"
  exit 1
fi

# ── Load config ───────────────────────────────────────────────────────────────

# shellcheck source=_shared/config.sh
source "$LEGAL_DIR/_shared/config.sh"

# ── Validate required fields ──────────────────────────────────────────────────

MISSING=()
[[ -z "${APP_NAME:-}"            ]] && MISSING+=("APP_NAME (gradle/libs.versions.toml → appDisplayName)")
[[ -z "${COMPANY_NAME:-}"        ]] && MISSING+=("COMPANY_NAME (gradle/fork.properties → legal.company.name or org.name)")
[[ -z "${JURISDICTION:-}"        ]] && MISSING+=("JURISDICTION (gradle/fork.properties → legal.jurisdiction)")
[[ -z "${EFFECTIVE_DATE:-}"      ]] && MISSING+=("EFFECTIVE_DATE (gradle/fork.properties → legal.effective.date)")
[[ -z "${LEGAL_CONTACT_EMAIL:-}" ]] && MISSING+=("LEGAL_CONTACT_EMAIL (gradle/fork.properties → legal.contact.email or org.email)")

if [[ ${#MISSING[@]} -gt 0 ]]; then
  echo "ERROR: Missing required values:"
  for m in "${MISSING[@]}"; do echo "  • $m"; done
  echo ""
  echo "Copy gradle/fork.properties.template → gradle/fork.properties and fill in the legal.* keys."
  exit 1
fi

# ── Render ────────────────────────────────────────────────────────────────────

mkdir -p "$OUTPUT_DIR"

# Render template $1 to $2, substituting only the variables in LEGAL_VARS.
# The explicit list matters: a bare `envsubst` would also expand anything else
# that looks like a shell variable in the legal text.
render() {
  local template="$1"
  local output="$2"
  envsubst "${LEGAL_VARS}" < "$template" > "$output"
  echo "  ✅ $(basename "$output")"
}

echo ""
echo "📄 Generating legal documents for: ${APP_NAME} (${APP_ID})"
echo "   Company:       ${COMPANY_NAME}"
echo "   Jurisdiction:  ${JURISDICTION}"
echo "   Effective:     ${EFFECTIVE_DATE}"
echo "   Contact:       ${LEGAL_CONTACT_EMAIL}"
echo ""
echo "Output:"

render "$LEGAL_DIR/privacy-policy/template.md" "$OUTPUT_DIR/privacy-policy.md"
render "$LEGAL_DIR/terms/template.md"          "$OUTPUT_DIR/terms.md"

echo ""
echo "Raw GitHub URLs (after committing legal/output/):"

# Derive raw GitHub URL from git remote if possible
REMOTE_URL="$(git -C "$REPO_ROOT" remote get-url upstream 2>/dev/null \
  || git -C "$REPO_ROOT" remote get-url origin 2>/dev/null || echo "")"
GITHUB_SLUG="$(echo "$REMOTE_URL" \
  | sed 's|.*github\.com[:/]||;s|\.git$||' 2>/dev/null || echo "")"

if [[ -n "$GITHUB_SLUG" ]]; then
  BASE="https://raw.githubusercontent.com/${GITHUB_SLUG}/main/legal/output"
  echo "  Privacy policy: ${BASE}/privacy-policy.md"
  echo "  Terms:          ${BASE}/terms.md"
  echo ""
  echo "Set org.privacy.url in gradle/fork.properties to the privacy policy URL above,"
  echo "then run ./gradlew syncForkConfig to propagate it to all store metadata."
else
  echo "  Privacy policy: legal/output/privacy-policy.md"
  echo "  Terms:          legal/output/terms.md"
fi

echo ""
echo "Next: commit legal/output/ and update org.privacy.url in gradle/fork.properties"
