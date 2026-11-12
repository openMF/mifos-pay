#!/usr/bin/env bash
# Functional test: `derive.rb --check --fork <dir>` verifies a PRE-white-label fork's app-profile with
# this checkout's resolver. Such a fork has app-profile/ but none of the template-owned machinery
# (deployment/_shared/config.rb, scripts/) that arrives only with the sync — so before --fork it had to be
# verified in a scratch tree.
#
#   D-1 the fork's identity is read (not the template's placeholder)
#   D-2 --check writes nothing into the fork
set -uo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$HERE/../.." && pwd)"
WORK="$(mktemp -d)"; trap 'rm -rf "$WORK"' EXIT
fail=0; pass() { echo "  ✅ $1"; }; bad() { echo "  ❌ $1"; fail=1; }
# shellcheck source=/dev/null
. "$REPO/scripts/ruby-exec.sh"

F="$WORK/legacy-fork"; mkdir -p "$F/gradle"
cp -R "$REPO/app-profile" "$F/"
cp "$REPO/gradle/libs.versions.toml" "$F/gradle/"
# The fork's own identity, distinct from the template's.
sed -i.bak 's/^\(  app_id:\).*/\1 org.example.legacyfork/' "$F/app-profile/app.yaml" && rm -f "$F/app-profile/app.yaml.bak"

out="$(cd "$REPO" && ruby_exec scripts/white-label/derive.rb --check --fork "$F" 2>&1)"; rc=$?
printf '%s\n' "$out" | grep -qx 'app.id=org.example.legacyfork' \
  && pass "D-1 the fork's app-profile is read (rc=$rc)" \
  || { bad "D-1 fork identity not read (rc=$rc)"; printf '%s\n' "$out" | tail -3; }
[ ! -e "$F/gradle/fork.properties" ] && [ ! -e "$F/deployment" ] \
  && pass "D-2 --check wrote nothing into the fork" || bad "D-2 --check wrote into the fork"

[ "$fail" = 0 ] && echo "derive-fork-test: PASS" || echo "derive-fork-test: FAIL"
exit "$fail"
