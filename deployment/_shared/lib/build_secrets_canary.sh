#!/usr/bin/env bash
# build_secrets_canary.sh — deterministic, vault-free parity + regression guard for
# the LAYOUT-aware secrets pipeline (PLAN secrets-pull-layout-aware, T4).
#
# WHY: `/secrets pull` materializes a developer's local secrets/live/ by decrypting
# each vault_alias-linked LAYOUT secret and handing it to `build-secrets vault-plan`
# + `build-secrets materialize-all` — the EXACT code path GitHub Actions runs in
# pre_fastlane_script. Because BOTH local and CI drive the same build_secrets.rb,
# proving materialize-all's decode + path behavior HERE proves local↔CI byte parity.
#
# WHAT: builds a throwaway repo skeleton (REPO_ROOT is hardcoded relative to the
# script, so we copy build_secrets.rb into a tmp tree), drops a fixture LAYOUT +
# fixture ENV, and asserts:
#   1. vault-plan emits exactly the vaulted rows, with b64=1 for file kinds only.
#   2. materialize-all (prod) + (demo) lands every secret at its LAYOUT rel path
#      with correctly DECODED bytes (base64 → raw for file; raw for env_var/value;
#      literal constant; by_flavor prod+demo both).
#   3. a non-vaulted file secret is absent from vault-plan (no over-emission).
#   4. re-running materialize-all is byte-idempotent.
#   5. RED: a LAYOUT with zero vault_alias emits zero vault-plan rows.
#
# Runs on macOS system Ruby 2.6 (no filter_map) and the CI runner alike. Zero vault,
# zero network, zero real secrets.
set -euo pipefail

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SRC_BS="$HERE/build_secrets.rb"
[ -f "$SRC_BS" ] || { echo "FATAL: build_secrets.rb not found at $SRC_BS" >&2; exit 2; }

PASS=0; FAIL=0
# Record a passing assertion.
ok()  { PASS=$((PASS+1)); printf '  ✅ %s\n' "$1"; }
# Record a failing assertion and print what was expected.
bad() { FAIL=$((FAIL+1)); printf '  ❌ %s\n' "$1"; }
# Assert two values are equal. $1 label, $2 actual, $3 expected.
eq()  { if [ "$2" = "$3" ]; then ok "$1"; else bad "$1 (want [$3] got [$2])"; fi; }

# ── build a throwaway repo skeleton so build_secrets.rb's REPO_ROOT resolves here ──
ROOT="$(mktemp -d "${TMPDIR:-/tmp}/bs-canary.XXXXXX")"
trap 'rm -rf "$ROOT"' EXIT
mkdir -p "$ROOT/deployment/_shared/lib" "$ROOT/secrets" "$ROOT/gradle"
cp "$SRC_BS" "$ROOT/deployment/_shared/lib/build_secrets.rb"
BS="$ROOT/deployment/_shared/lib/build_secrets.rb"
printf 'appId = "org.canary.app"\n' > "$ROOT/gradle/libs.versions.toml"

cat > "$ROOT/secrets/LAYOUT.yaml" <<'YAML'
roots: { live: secrets/live, sample: secrets/sample }
precedence: [live, sample]
secrets:
  # base64 file — vaulted; materialize base64-decodes ENV back to raw bytes
  creds_json:   { kind: file, encoding: base64, source_env: CREDS_B64, rel: svc/creds.json, vault_alias: v_creds }
  # pem-or-base64 file — vaulted; fed base64 (won't match -----BEGIN) so it decodes
  auth_p8:      { kind: file, encoding: pem-or-base64, source_env: AUTH_P8, rel: apple/AuthKey.p8, vault_alias: v_p8 }
  # env_var — vaulted; raw value, lands at live/_env/<VAR>
  api_token:    { kind: env_var, source_env: API_TOKEN, vault_alias: v_token }
  # literal — NOT vaulted; constant from LAYOUT, always materialized, never in vault-plan
  git_branch:   { kind: literal, source_env: GIT_BRANCH, rel: apple/git_branch, value: master }
  # NON-vaulted file — has NO vault_alias → must be absent from vault-plan
  local_only:   { kind: file, encoding: base64, source_env: LOCAL_ONLY_B64, rel: local/only.bin }
  # by_flavor value — vaulted per flavor; prod+demo distinct source_env + vault_alias
  app_id:
    kind: value
    by_flavor:
      prod: { source_env: APP_ID_PROD, rel: fb/app_id,      vault_alias: v_app_prod }
      demo: { source_env: APP_ID_DEMO, rel: fb/app_id_demo, vault_alias: v_app_demo }
YAML

echo "── 1. vault-plan shape ─────────────────────────────────────────────"
PLAN="$(ruby "$BS" vault-plan | sort)"
eq "vault-plan row count = 5 (creds,p8,token,app_prod,app_demo — literal+local_only excluded)" \
   "$(printf '%s\n' "$PLAN" | grep -c .)" "5"
eq "file kinds fed base64 (b64=1) = 2 (creds,p8)" \
   "$(printf '%s\n' "$PLAN" | awk -F'\t' '$3==1' | grep -c .)" "2"
eq "non-vaulted local_only absent from plan" \
   "$(printf '%s\n' "$PLAN" | grep -c 'v_local\|local_only\|LOCAL_ONLY')" "0"
eq "literal git_branch absent from plan" \
   "$(printf '%s\n' "$PLAN" | grep -c 'GIT_BRANCH')" "0"
eq "by_flavor demo present" \
   "$(printf '%s\n' "$PLAN" | grep -c 'v_app_demo	APP_ID_DEMO	0')" "1"

echo "── 2. materialize-all decodes + lands at LAYOUT rel paths ──────────"
# Fixture ENV: base64 for file kinds, raw for env_var/value.
export CREDS_B64="$(printf '{"k":"creds-body"}' | base64 | tr -d '\n')"
export AUTH_P8="$(printf -- '-----BEGIN PRIVATE KEY-----\nZZZ\n-----END PRIVATE KEY-----\n' | base64 | tr -d '\n')"
export API_TOKEN="tok_canary_123"
export APP_ID_PROD="1:111:android:prod"
export APP_ID_DEMO="1:222:android:demo"
( cd "$ROOT"
  ruby "$BS" materialize-all >/dev/null
  ruby "$BS" materialize-all --flavor demo >/dev/null )
LIVE="$ROOT/secrets/live"
eq "creds.json base64-decoded to raw JSON"     "$(cat "$LIVE/svc/creds.json" 2>/dev/null)" '{"k":"creds-body"}'
eq "AuthKey.p8 base64-decoded to raw PEM (1st line)" "$(head -1 "$LIVE/apple/AuthKey.p8" 2>/dev/null)" '-----BEGIN PRIVATE KEY-----'
eq "api_token raw at live/_env/API_TOKEN"      "$(cat "$LIVE/_env/API_TOKEN" 2>/dev/null)" 'tok_canary_123'
eq "literal git_branch materialized as constant" "$(cat "$LIVE/apple/git_branch" 2>/dev/null)" 'master'
eq "by_flavor prod app_id landed"              "$(cat "$LIVE/fb/app_id" 2>/dev/null)" '1:111:android:prod'
eq "by_flavor demo app_id landed"              "$(cat "$LIVE/fb/app_id_demo" 2>/dev/null)" '1:222:android:demo'
# local_only has no ENV set → materialize skips it (body empty) → must NOT exist
if [ -e "$LIVE/local/only.bin" ]; then bad "non-vaulted local_only should NOT materialize (no ENV)"; else ok "non-vaulted local_only correctly skipped"; fi

echo "── 2b. export-paths parity + generated Gradle map ─────────────────"
# The JVM/Gradle surface: `export-paths` projects the SAME `path()` algorithm to a flat map, and
# `materialize-all` also writes it to gradle/secrets-paths.properties (write_paths_map!). Assert the
# map is a faithful, drift-free projection and that env_var (env-line) keys are excluded.
XP="$(ruby "$BS" export-paths | sort)"
eq "export-paths row count = 5 (env_var api_token excluded)" \
   "$(printf '%s\n' "$XP" | grep -c .)" "5"
eq "export-paths[creds_json] == path creds_json (no drift)" \
   "$(printf '%s\n' "$XP" | sed -n 's/^creds_json=//p')" "$(ruby "$BS" path creds_json)"
eq "env_var api_token absent from export-paths (Gradle never needs it)" \
   "$(printf '%s\n' "$XP" | grep -c '^api_token=')" "0"
eq "materialize-all wrote gradle/secrets-paths.properties" \
   "$([ -f "$ROOT/gradle/secrets-paths.properties" ] && echo yes || echo no)" "yes"
eq "gradle map carries the same creds_json path" \
   "$(sed -n 's/^creds_json=//p' "$ROOT/gradle/secrets-paths.properties" 2>/dev/null)" "$(ruby "$BS" path creds_json)"

echo "── 3. idempotency ─────────────────────────────────────────────────"
H1="$(find "$LIVE" -type f | sort | xargs shasum -a256 | shasum -a256 | cut -d' ' -f1)"
( cd "$ROOT"; ruby "$BS" materialize-all >/dev/null; ruby "$BS" materialize-all --flavor demo >/dev/null )
H2="$(find "$LIVE" -type f | sort | xargs shasum -a256 | shasum -a256 | cut -d' ' -f1)"
eq "materialize-all byte-idempotent across runs" "$H1" "$H2"

echo "── 4. RED: LAYOUT without any vault_alias → empty plan ─────────────"
cat > "$ROOT/secrets/LAYOUT.yaml" <<'YAML'
roots: { live: secrets/live, sample: secrets/sample }
precedence: [live, sample]
secrets:
  plain_token: { kind: env_var, source_env: PLAIN }
YAML
eq "vault-plan on vault_alias-free LAYOUT = 0 rows" "$(ruby "$BS" vault-plan | grep -c .)" "0"

echo "───────────────────────────────────────────────────────────────────"
printf 'build-secrets canary: %d passed · %d failed\n' "$PASS" "$FAIL"
[ "$FAIL" -eq 0 ] || exit 1
