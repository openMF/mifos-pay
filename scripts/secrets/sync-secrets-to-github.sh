#!/usr/bin/env bash
# scripts/secrets/sync-secrets-to-github.sh
#
# Reads credentials from secrets/ and pushes them to GitHub Actions secrets.
# Run this once after filling secrets/ to wire up your fork's CI pipeline.
#
# Prerequisites:
#   - gh CLI installed and authenticated (gh auth login)
#   - secrets/ folder populated (copy from secrets/sample/, fill in real values)
#
# Usage:
#   bash scripts/secrets/sync-secrets-to-github.sh
#   bash scripts/secrets/sync-secrets-to-github.sh --repo owner/repo
#   bash scripts/secrets/sync-secrets-to-github.sh --dry-run
#   bash scripts/secrets/sync-secrets-to-github.sh --only ios
#   bash scripts/secrets/sync-secrets-to-github.sh --only android
#   bash scripts/secrets/sync-secrets-to-github.sh --only firebase
#   bash scripts/secrets/sync-secrets-to-github.sh --only mac
#   bash scripts/secrets/sync-secrets-to-github.sh --only windows
#   bash scripts/secrets/sync-secrets-to-github.sh --only linux
#   bash scripts/secrets/sync-secrets-to-github.sh --only microsoft-store
#   bash scripts/secrets/sync-secrets-to-github.sh --only azure-signing
#   bash scripts/secrets/sync-secrets-to-github.sh --only web
#
# Play App Signing model (per https://support.google.com/googleplay/android-developer/answer/9842756):
#   Google holds the app signing key; developer holds ONLY the upload key.
#   Android requires ONE keystore family:
#     UPLOAD_KEYSTORE_FILE + _PASSWORD + _ALIAS + _ALIAS_PASSWORD → Play Console upload key
#   90%+ of new apps use this default; Google's KMS holds the app signing key.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
SECRETS_DIR="$REPO_ROOT/secrets"

# ── Resolve secret paths through the build-secrets resolver (Phase 7 SoT: secrets/LAYOUT.yaml) ──
# NO static secret paths for LAYOUT-declared secrets — the resolver owns every path
# (live-wins-else-sample), so a secrets/ layout change is a one-file LAYOUT.yaml edit with
# zero impact on this script. `_p <layout-key>` → absolute path to the resolved file.
BS="$REPO_ROOT/deployment/scripts/build-secrets"
# Resolve alias $1 (optionally for flavor $2) to an absolute path, via the build-secrets
# resolver rather than a hardcoded layout. Returns non-zero when the alias is unknown.
_p() {  # _p <layout-key> [flavor]  → absolute path to the resolved file
  local rel
  rel="$( cd "$REPO_ROOT" && "$BS" path "$1" ${2:+--flavor "$2"} 2>/dev/null )" || return 1
  [ -n "$rel" ] && printf '%s/%s\n' "$REPO_ROOT" "$rel"
}

DRY_RUN=false
REPO=""
ONLY=""

# ── Parse args ────────────────────────────────────────────────────────────────
while [[ $# -gt 0 ]]; do
  case "$1" in
    --dry-run) DRY_RUN=true ;;
    --repo)    REPO="$2"; shift ;;
    --only)    ONLY="$2"; shift ;;
    *) echo "Unknown arg: $1"; exit 1 ;;
  esac
  shift
done

# ── Validate environment ──────────────────────────────────────────────────────
if ! command -v gh &>/dev/null; then
  echo "❌  gh CLI not found. Install: https://cli.github.com"
  exit 1
fi

if [[ -z "$REPO" ]]; then
  REPO=$(gh repo view --json nameWithOwner -q .nameWithOwner 2>/dev/null || true)
fi

if [[ -z "$REPO" ]]; then
  echo "❌  Could not detect repo. Pass --repo owner/repo"
  exit 1
fi

if [[ ! -d "$SECRETS_DIR" ]]; then
  echo "❌  secrets/ not found at $SECRETS_DIR"
  echo "    Copy secrets/sample/ → secrets/ and fill in real values first."
  exit 1
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  Sync secrets → GitHub Actions"
echo "  Repo:    $REPO"
echo "  Dry run: $DRY_RUN"
if [[ -n "$ONLY" ]]; then
  echo "  Filter:  --only $ONLY"
fi
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

PUSHED=0
SKIPPED=0
ERRORS=0

# ── Helper: set one GHA secret ────────────────────────────────────────────────
_set_secret() {
  local name="$1"
  local value="$2"
  local source_hint="${3:-}"

  if [[ -z "$value" ]]; then
    echo "  ⚠️  SKIP $name — empty value${source_hint:+ ($source_hint)}"
    ((SKIPPED++)) || true
    return
  fi

  if $DRY_RUN; then
    echo "  [dry-run] Would set: $name${source_hint:+ ← $source_hint}"
    ((PUSHED++)) || true
  else
    if echo "$value" | gh secret set "$name" --repo "$REPO" 2>&1; then
      echo "  ✅  $name${source_hint:+ ← $source_hint}"
      ((PUSHED++)) || true
    else
      echo "  ❌  FAILED: $name"
      ((ERRORS++)) || true
    fi
  fi
}

# Set secret from a text file (strips whitespace)
_set_from_file() {
  local name="$1"
  local path="$2"
  if [[ -f "$path" ]]; then
    local value; value=$(cat "$path" | tr -d '\r' | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//')
    if echo "$value" | grep -q "CLAUDE-PLACEHOLDER"; then
      echo "  ⚠️  SKIP $name — still a placeholder in $path"
      ((SKIPPED++)) || true
      return
    fi
    _set_secret "$name" "$value" "$path"
  else
    echo "  ⏭   SKIP $name — file not found: $path"
    ((SKIPPED++)) || true
  fi
}

# Set secret from a binary file (base64-encoded)
_set_from_binary() {
  local name="$1"
  local path="$2"
  if [[ -f "$path" ]]; then
    if [[ $(head -c 16 "$path") == "CLAUDE-PLHLD-v1" ]]; then
      echo "  ⚠️  SKIP $name — still a placeholder in $path"
      ((SKIPPED++)) || true
      return
    fi
    local value; value=$(base64 -i "$path" 2>/dev/null || base64 "$path")
    _set_secret "$name" "$value" "$path (base64)"
  else
    echo "  ⏭   SKIP $name — file not found: $path"
    ((SKIPPED++)) || true
  fi
}

# Push one keystore family from a .properties file + a .jks/.keystore binary.
#   $1 = family prefix (ORIGINAL | UPLOAD)
#   $2 = properties file path
#   $3 = keystore file path
# Properties keys recognized (case-sensitive):
#   storePassword | keyAlias | keyPassword | storeFile (informational; binary path overrides)
#
# Dual-write transition: also pushes the actionhub v2 lowercase secret names
# (google_services, upload_keystore, etc.) so consumers can use `secrets: inherit`
# while their old UPPERCASE names still work. After consumers verify lowercase
# names work, the UPPERCASE family can be deleted.
_set_keystore_family() {
  local prefix="$1"
  local props="$2"
  local jks="$3"

  if [[ ! -f "$props" ]]; then
    echo "  ⏭   ${prefix}: props file not found: $props"
    ((SKIPPED+=4)) || true
    return
  fi

  if grep -q "CLAUDE-PLACEHOLDER" "$props"; then
    echo "  ⚠️  ${prefix}: $props is still a placeholder — fill it in"
    ((SKIPPED+=4)) || true
    return
  fi

  while IFS='=' read -r k v; do
    [[ -z "$k" || "$k" =~ ^# ]] && continue
    case "$k" in
      storePassword)
        _set_secret "${prefix}_KEYSTORE_FILE_PASSWORD"  "$v" "$props"
        # Dual-write: actionhub v2 lowercase name (consumer can use `secrets: inherit`)
        _set_secret "keystore_password"                 "$v" "$props (v2 alias)"
        ;;
      keyAlias)
        _set_secret "${prefix}_KEYSTORE_ALIAS"          "$v" "$props"
        _set_secret "keystore_alias"                    "$v" "$props (v2 alias)"
        ;;
      keyPassword)
        _set_secret "${prefix}_KEYSTORE_ALIAS_PASSWORD" "$v" "$props"
        _set_secret "keystore_alias_password"           "$v" "$props (v2 alias)"
        ;;
    esac
  done < "$props"

  _set_from_binary "${prefix}_KEYSTORE_FILE" "$jks"
  # Dual-write: v2 lowercase keystore alias
  if [[ -f "$jks" && $(head -c 16 "$jks") != "CLAUDE-PLHLD-v1" ]]; then
    local b64; b64=$(base64 -i "$jks" 2>/dev/null || base64 "$jks")
    _set_secret "upload_keystore" "$b64" "$jks (base64, v2 alias)"
  fi
}

# ── iOS secrets ───────────────────────────────────────────────────────────────
_sync_ios() {
  echo "📱 iOS / App Store"
  # Paths resolved via build-secrets (LAYOUT SoT) — no static secrets/ literals.
  local kid iss p8 mk mp
  kid="$(_p appstore_key_id)"; iss="$(_p appstore_issuer_id)"; p8="$(_p appstore_auth_key)"
  mk="$(_p match_ssh_key)";    mp="$(_p match_password)"
  # Dual-write each secret: legacy UPPERCASE + v2 lowercase
  _set_from_file    "APPSTORE_KEY_ID"        "$kid"
  _set_from_file    "appstore_key_id"        "$kid"
  _set_from_file    "APPSTORE_ISSUER_ID"     "$iss"
  _set_from_file    "appstore_issuer_id"     "$iss"
  _set_from_binary  "APPSTORE_AUTH_KEY"      "$p8"
  _set_from_binary  "appstore_auth_key"      "$p8"
  _set_from_binary  "MATCH_GIT_PRIVATE_KEY"  "$mk"
  _set_from_binary  "match_ssh_private_key"  "$mk"
  _set_from_file    "MATCH_PASSWORD"         "$mp"
  _set_from_file    "match_password"         "$mp"

  # iOS identity/metadata — read from gradle/fork.properties (non-secret)
  local fork_props="gradle/fork.properties"
  if [[ -f "$fork_props" ]]; then
    echo "  Parsing $fork_props for non-secret iOS metadata …"
    while IFS='=' read -r key val; do
      [[ -z "$key" || "$key" =~ ^# ]] && continue
      [[ -z "$val" || "$val" == "YOUR_"* || "$val" == "your_"* ]] && continue
      _set_secret "IOS_ENV_${key}" "$val" "fork.properties:${key}"
      _set_secret "${key}"         "$val" "fork.properties:${key} (cross-platform)"
    done < "$fork_props"
  fi

  # (removed: redundant re-read of apple/appstore/{key_id,issuer_id} — the resolver-backed
  #  APPSTORE_KEY_ID / APPSTORE_ISSUER_ID above already resolve to the canonical path.)
  echo ""
}

# ── Android secrets ───────────────────────────────────────────────────────────
# Play App Signing model: ONE keystore (UPLOAD). Google holds the app signing key.
_sync_android() {
  echo "🤖 Android / Play Store (Play App Signing model — single UPLOAD keystore)"

  # UPLOAD keystore (Play Console upload key) — the only keystore the developer holds.
  # Keystore FILE resolves via the LAYOUT; the sibling .properties (alias/passwords — not a
  # LAYOUT secret) lives beside it, so anchor it to the resolved keystore dir (no static path).
  local ks; ks="$(_p upload_keystore)"
  _set_keystore_family "UPLOAD" "$(dirname "$ks")/upload_keystore.properties" "$ks"

  # google-services.json — required by all Android builds
  local gs; gs="$(_p google_services)"
  _set_from_binary "GOOGLESERVICES"   "$gs"
  _set_from_binary "google_services"  "$gs"

  # Firebase / Play Store service account JSON (dual-write)
  local fbc psc; fbc="$(_p firebase_service_account)"; psc="$(_p play_service_account)"
  _set_from_binary "FIREBASECREDS"  "$fbc"
  _set_from_binary "firebase_creds" "$fbc"
  _set_from_binary "PLAYSTORECREDS"  "$psc"
  _set_from_binary "playstore_creds" "$psc"
  echo ""
}

# ── Firebase secrets ──────────────────────────────────────────────────────────
_sync_firebase() {
  echo "🔥 Firebase"
  _set_from_binary "FIREBASECREDS"           "$(_p firebase_service_account)"
  _set_from_file   "FIREBASE_ANDROID_APP_ID"      "$(_p firebase_android_app_id)"
  _set_from_file   "FIREBASE_ANDROID_DEMO_APP_ID" "$(_p firebase_android_app_id demo)"
  _set_from_file   "FIREBASE_IOS_APP_ID"      "$(_p firebase_ios_app_id)"
  _set_from_file   "FIREBASE_IOS_DEMO_APP_ID" "$(_p firebase_ios_demo_app_id)"
  _set_from_file   "FIREBASE_IOS_PROD_APP_ID" "$(_p firebase_ios_prod_app_id)"
  echo ""
}

# ── Play Store secrets ────────────────────────────────────────────────────────
_sync_play() {
  echo "▶  Play Store"
  _set_from_binary "PLAYSTORECREDS" "$(_p play_service_account)"
  echo ""
}

# ── NOTE (secrets-resolver alignment, Phase 7) ────────────────────────────────
# The desktop (macOS/Windows/Linux) + Microsoft-Store + Azure-signing secrets below are
# NOT yet declared in secrets/LAYOUT.yaml (those platforms are deferred per the epic), so
# they still read static $SECRETS_DIR paths. The guard SR-12 already scans scripts/ for the
# service-account class (so android/firebase/play stay resolver-only), but does NOT flag these
# desktop $SECRETS_DIR paths. When a platform onboards: add its secrets to LAYOUT.yaml, migrate
# these to `_p <layout-key>` (as iOS/Android/Firebase/Play/Web do above), then broaden SR-12's
# pattern in scripts/ci/check-secrets-resolver.sh to cover the desktop $SECRETS_DIR paths.
# ── macOS signing secrets (Mac App Store / TestFlight) ────────────────────────
_sync_mac() {
  echo "🍏 macOS / App Store"
  _set_from_binary "MACOS_SIGNING_KEY"          "$SECRETS_DIR/mac/signing_key.p12"
  _set_from_file   "MACOS_SIGNING_PASSWORD"     "$SECRETS_DIR/mac/signing_password"
  _set_from_binary "MACOS_SIGNING_CERTIFICATE"  "$SECRETS_DIR/mac/signing_certificate.cer"
  _set_from_binary "MACOS_INSTALLER_KEY"        "$SECRETS_DIR/mac/installer_key.p12"
  _set_from_file   "MACOS_INSTALLER_PASSWORD"   "$SECRETS_DIR/mac/installer_password"
  _set_from_binary "MAC_PROVISIONING_PROFILE_BASE64" "$SECRETS_DIR/mac/provisioning_profile.provisionprofile"
  _set_from_file   "MAC_BUNDLE_IDENTIFIER"      "$SECRETS_DIR/mac/bundle_identifier"
  echo ""
}

# ── Windows signing secrets ───────────────────────────────────────────────────
_sync_windows() {
  echo "🪟 Windows / Desktop"
  _set_from_binary "WINDOWS_SIGNING_KEY"          "$SECRETS_DIR/windows/signing_key.pfx"
  _set_from_file   "WINDOWS_SIGNING_PASSWORD"     "$SECRETS_DIR/windows/signing_password"
  _set_from_binary "WINDOWS_SIGNING_CERTIFICATE"  "$SECRETS_DIR/windows/signing_certificate.cer"
  echo ""
}

# ── Linux signing secrets ─────────────────────────────────────────────────────
_sync_linux() {
  echo "🐧 Linux / Desktop"
  _set_from_binary "LINUX_SIGNING_KEY"          "$SECRETS_DIR/linux/signing_key.gpg"
  _set_from_file   "LINUX_SIGNING_PASSWORD"     "$SECRETS_DIR/linux/signing_password"
  _set_from_binary "LINUX_SIGNING_CERTIFICATE"  "$SECRETS_DIR/linux/signing_certificate.pub"
  echo ""
}

# ── Microsoft Store (Partner Center API) ──────────────────────────────────────
_sync_microsoft_store() {
  echo "🛒 Microsoft Store / Partner Center"
  _set_from_file "MS_STORE_TENANT_ID"     "$SECRETS_DIR/microsoft-store/tenant_id"
  _set_from_file "MS_STORE_CLIENT_ID"     "$SECRETS_DIR/microsoft-store/client_id"
  _set_from_file "MS_STORE_CLIENT_SECRET" "$SECRETS_DIR/microsoft-store/client_secret"
  _set_from_file "MS_STORE_SELLER_ID"     "$SECRETS_DIR/microsoft-store/seller_id"
  _set_from_file "MS_STORE_APP_ID"        "$SECRETS_DIR/microsoft-store/app_id"
  echo ""
}

# ── Azure Trusted Signing (Windows MSI signing) ───────────────────────────────
_sync_azure_signing() {
  echo "☁️  Azure Trusted Signing (Windows MSI)"
  _set_from_file "AZURE_TENANT_ID"             "$SECRETS_DIR/azure-signing/tenant_id"
  _set_from_file "AZURE_CLIENT_ID"             "$SECRETS_DIR/azure-signing/client_id"
  _set_from_file "AZURE_CLIENT_SECRET"         "$SECRETS_DIR/azure-signing/client_secret"
  _set_from_file "AZURE_SIGNING_ENDPOINT"      "$SECRETS_DIR/azure-signing/endpoint"
  _set_from_file "AZURE_SIGNING_ACCOUNT_NAME"  "$SECRETS_DIR/azure-signing/account_name"
  _set_from_file "AZURE_SIGNING_CERT_PROFILE"  "$SECRETS_DIR/azure-signing/cert_profile_name"
  echo ""
}

# ── Web hosting secrets ───────────────────────────────────────────────────────
_sync_web() {
  echo "🌐 Web / Hosting"
  _set_from_file "CLOUDFLARE_API_TOKEN"  "$(_p cloudflare_pages_api_token)"
  _set_from_file "CLOUDFLARE_ACCOUNT_ID" "$(_p cloudflare_account_id)"
  _set_from_file "NETLIFY_AUTH_TOKEN"    "$(_p netlify_auth_token)"
  _set_from_file "NETLIFY_SITE_ID"       "$(_p netlify_site_id)"
  _set_from_file "VERCEL_TOKEN"          "$(_p vercel_token)"
  _set_from_file "VERCEL_ORG_ID"         "$(_p vercel_org_id)"
  _set_from_file "VERCEL_PROJECT_ID"     "$(_p vercel_project_id)"
  echo ""
}

# ── Dispatch ──────────────────────────────────────────────────────────────────
case "${ONLY:-all}" in
  ios)              _sync_ios ;;
  android)          _sync_android ;;
  firebase)         _sync_firebase ;;
  play)             _sync_play ;;
  mac|macos)        _sync_mac ;;
  windows|win)      _sync_windows ;;
  linux)            _sync_linux ;;
  microsoft-store|ms-store) _sync_microsoft_store ;;
  azure-signing|azure)      _sync_azure_signing ;;
  web)              _sync_web ;;
  all)
    _sync_ios
    _sync_android
    _sync_firebase
    _sync_play
    _sync_mac
    _sync_windows
    _sync_linux
    _sync_microsoft_store
    _sync_azure_signing
    _sync_web
    ;;
  *)
    echo "Unknown --only value: $ONLY"
    echo "Valid: ios | android | firebase | play | mac | windows | linux | microsoft-store | azure-signing | web | all"
    exit 1
    ;;
esac

# ── Summary ───────────────────────────────────────────────────────────────────
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
if $DRY_RUN; then
  echo "  [DRY RUN] Would push: $PUSHED  |  Skipped: $SKIPPED  |  Errors: $ERRORS"
else
  echo "  Pushed: $PUSHED  |  Skipped: $SKIPPED  |  Errors: $ERRORS"
fi
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

if [[ $ERRORS -gt 0 ]]; then
  echo "⚠️  Some secrets failed to push. Check output above."
  exit 1
fi

if ! $DRY_RUN && [[ $PUSHED -gt 0 ]]; then
  echo "✅  Secrets synced to $REPO"
  echo "    Trigger a workflow run to verify: gh workflow run --repo $REPO"
fi
