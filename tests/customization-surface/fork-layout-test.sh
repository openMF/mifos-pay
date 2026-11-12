#!/usr/bin/env bash
# Functional test: secrets/fork-layout.yaml (fork-owned) adds the fork's secret rows on top of the
# template-owned secrets/LAYOUT.yaml, through the ONE resolver (build_secrets.rb).
#
#   L-1 a fork row is resolvable (spec + rel)
#   L-2 a fork row that names a LAYOUT key is ignored — the template's row wins
#   L-3 no fork-layout.yaml → LAYOUT.yaml alone (unchanged behaviour)
set -uo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$HERE/../.." && pwd)"
WORK="$(mktemp -d)"; trap 'rm -rf "$WORK"' EXIT
fail=0; pass() { echo "  ✅ $1"; }; bad() { echo "  ❌ $1"; fail=1; }
# shellcheck source=/dev/null
. "$REPO/scripts/ruby-exec.sh"

mkdir -p "$WORK/deployment/_shared/lib" "$WORK/secrets"
cp "$REPO/deployment/_shared/lib/build_secrets.rb" "$WORK/deployment/_shared/lib/"
cp "$REPO/secrets/LAYOUT.yaml" "$WORK/secrets/"
probe='require "yaml"; require ARGV[0]; a = BuildSecrets.for
       puts "fork_rel=#{(a.rel("fork_client_key") rescue "missing")}"
       puts "tpl_rel=#{a.rel("upload_keystore")}"'
# Runs the probe against the copied resolver in $WORK and prints its fork_rel= / tpl_rel= lines.
run() { (cd "$WORK" && ruby_exec -e "$probe" "$WORK/deployment/_shared/lib/build_secrets.rb" 2>/dev/null); }

base="$(run)"
printf '%s\n' "$base" | grep -qx 'fork_rel=missing' && pass "L-3 no fork-layout.yaml → template rows only" || bad "L-3 got: $base"
tpl_rel="$(printf '%s\n' "$base" | sed -n 's/^tpl_rel=//p')"

cat > "$WORK/secrets/fork-layout.yaml" <<'YAML'
secrets:
  fork_client_key:
    kind: value
    rel: android/fork-client-key.txt
  upload_keystore:
    kind: file
    rel: hijacked/path.keystore
YAML
out="$(run)"
printf '%s\n' "$out" | grep -qx 'fork_rel=android/fork-client-key.txt' && pass "L-1 fork row resolvable" || bad "L-1 got: $out"
printf '%s\n' "$out" | grep -qx "tpl_rel=$tpl_rel" && pass "L-2 a fork row cannot redirect a template secret" || bad "L-2 got: $out"

[ "$fail" = 0 ] && echo "fork-layout-test: PASS" || echo "fork-layout-test: FAIL"
exit "$fail"
