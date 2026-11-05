#!/usr/bin/env bash
# Canary for supabase-client-security.sh — GREEN passes; each RED fails on the rule it breaks.
# Fixtures are BUILT at run time in a temp dir: they need fake privileged-shaped values (`sb_secret_…`,
# a service_role JWT) that must never be committed, and secrets/live/ is gitignored anyway.
set -uo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"; CHECK="$HERE/../../checks/supabase-client-security.sh"
T="$(mktemp -d)"; trap 'rm -rf "$T"' EXIT; rc=0
mk() {  # mk <dir> <publishable_key_env> <vault_alias> <live-value> <sample-value> <kotlin-auth-line>
  local d="$T/$1"; mkdir -p "$d/app-profile" "$d/secrets/live/supabase" "$d/secrets/sample/supabase" "$d/core/x/src/commonMain/kotlin"
  printf 'network:\n  access_points:\n    - id: proj\n      type: supabase\n      base_url: https://proj.supabase.co\n      publishable_key_env: %s\n' "$2" > "$d/app-profile/app.yaml"
  printf 'roots: { live: secrets/live, sample: secrets/sample }\nsecrets:\n  k: { kind: value, source_env: %s, vault_alias: %s, rel: supabase/publishable_key }\n' "$2" "$3" > "$d/secrets/LAYOUT.yaml"
  printf '%s' "$4" > "$d/secrets/live/supabase/publishable_key"; printf '%s' "$5" > "$d/secrets/sample/supabase/publishable_key"
  printf 'import io.github.jan.supabase.auth.Auth\nimport io.github.jan.supabase.compose.auth.ComposeAuth\nfun f() {\n    %s\n}\n' "$6" > "$d/core/x/src/commonMain/kotlin/A.kt"
}
b64() { printf '%s' "$1" | base64 | tr '+/' '-_' | tr -d '=\n'; }
SERVICE_JWT="eyJhbGciOiJIUzI1NiJ9.$(b64 '{"role":"service_role"}').c2ln"   # fake: header.payload.fake-signature
ANON_JWT="eyJhbGciOiJIUzI1NiJ9.$(b64 '{"role":"anon"}').c2ln"
SECURE='kmpSupabaseAuthInstall(config)'
mk green           SUPABASE_PUBLISHABLE_KEY   ws-supabase-publishable-key    "$ANON_JWT"           ""        "$SECURE"
mk red-name        SUPABASE_SERVICE_ROLE_KEY  ws-supabase-publishable-key    "$ANON_JWT"           ""        "$SECURE"
mk red-alias       SUPABASE_PUBLISHABLE_KEY   ws-supabase-service-role-key   "$ANON_JWT"           ""        "$SECURE"
mk red-jwt         SUPABASE_PUBLISHABLE_KEY   ws-supabase-publishable-key    "$SERVICE_JWT"        ""        "$SECURE"
mk red-sbsecret    SUPABASE_PUBLISHABLE_KEY   ws-supabase-publishable-key    "sb_secret_fakefake"  ""        "$SECURE"
mk red-sample      SUPABASE_PUBLISHABLE_KEY   ws-supabase-publishable-key    "$ANON_JWT"           "x"       "$SECURE"
mk red-plain-auth  SUPABASE_PUBLISHABLE_KEY   ws-supabase-publishable-key    "$ANON_JWT"           ""        'install(Auth) { }'
mk red-compose-auth SUPABASE_PUBLISHABLE_KEY  ws-supabase-publishable-key    "$ANON_JWT"           ""        'install(ComposeAuth) { }'
red() { local out r; out="$(bash "$CHECK" "$T/$1" 2>&1)"; r=$?
  if [ $r -eq 1 ] && echo "$out" | grep -q "✗ $2"; then echo "  ✓ $1 failed on $2"; else echo "  ✗ $1 did not fail on $2 (exit $r)"; rc=1; fi
  echo "$out" | grep -qE 'sb_secret_fake|eyJhbGciOiJIUzI1NiJ9\.' && { echo "  ✗ $1 PRINTED a value"; rc=1; }; true; }
red red-name SC-1; red red-alias SC-2; red red-jwt SC-3; red red-sbsecret SC-3; red red-sample SC-4; red red-plain-auth SC-5; red red-compose-auth SC-5
if bash "$CHECK" "$T/green" >/dev/null 2>&1; then echo "  ✓ GREEN passed (legacy anon JWT as publishable key, empty sample, kmpSupabaseAuthInstall)"; else echo "  ✗ GREEN failed"; rc=1; fi
[ $rc = 0 ] && echo "CANARY: ✅ PASS" || echo "CANARY: ❌ FAIL"; exit $rc
