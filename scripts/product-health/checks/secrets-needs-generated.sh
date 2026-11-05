#!/usr/bin/env bash
# secrets-needs-generated.sh — deployment/**/secrets-needs.yaml agree with secrets/LAYOUT.yaml.
#
# WHY THIS EXISTS
# Both files used to declare the same per-secret facts by hand (vault alias, path, CI secret) and had
# drifted in 27 places: 14 aliases LAYOUT did not know, so `/secrets pull` could not materialize them,
# and 13 CI names that matched nothing release-multi-platform.yml reads (2026-10-29). Now each target
# lists only `needs: [{secret, required_for}]` (LAYOUT keys — identical in every fork), LAYOUT owns every
# per-secret fact, and ./gradlew syncForkConfig generates the ONE derived file, the root
# secrets-manifest.yaml (deployment/_shared/lib/build_secrets.rb `needs-generate` — this check runs that
# SAME code with --check).
#
#   SN-1 every target lists only needs: (no stored vault_aliases/manual_inputs, no old generated block),
#        every needed secret is declared in LAYOUT, and secrets-manifest.yaml is current
#   SN-2 every LAYOUT `gha_secret` is a secret the workflows actually read (secrets.<NAME>), so the
#        CI name shown to a human cannot drift from the one CI uses
#
# Exit 0 = PASS · 1 = FAIL (blocks) · 2 = WARN (a legacy v1 target, not yet migrated to needs:).
set -uo pipefail
# Resolve this repo's resolver BEFORE any cd: BASH_SOURCE may be relative.
SELF_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)"
RESOLVER="$SELF_ROOT/deployment/_shared/lib/build_secrets.rb"
ROOT="$(cd "${1:-$SELF_ROOT}" 2>/dev/null && pwd)" || { echo "  ✗ SN-0 cannot enter ${1:-}"; exit 2; }
cd "$ROOT"
[ -f "secrets/LAYOUT.yaml" ] || { echo "  ✓ SN-1 no secrets/LAYOUT.yaml in this tree — nothing to check"; exit 0; }
command -v ruby >/dev/null 2>&1 || { echo "  ✗ SN-0 ruby unavailable — cannot verify secrets-needs"; exit 1; }
fail=0
warn=0
if out="$(ruby "$RESOLVER" needs-generate --check --root "$ROOT" 2>&1)"; then
  echo "  ✓ SN-1 $(printf '%s' "$out" | grep '^✓' | sed 's/^✓ secrets-needs: //')"
  if printf '%s' "$out" | grep -q '^⚠ legacy'; then
    echo "  ⚠ SN-1 targets still hand-authored (v1) — they get none of this drift protection:"
    printf '%s\n' "$out" | grep '^⚠ legacy' | sed 's/^⚠ legacy v1 (hand-authored, not generated): /      /; s/ — migrate.*//'
    warn=1
  fi
else
  echo "  ✗ SN-1 secrets-needs.yaml disagree with secrets/LAYOUT.yaml — run ./gradlew syncForkConfig"
  printf '%s\n' "$out" | sed 's/^/      /'; fail=1
fi
missing="$(ruby -ryaml -e '
  names = Dir[File.join(ARGV[0], ".github", "workflows", "*.{yml,yaml}")].flat_map { |f| File.read(f).scan(/secrets\.([A-Z0-9_]+)/).flatten }
  doc = YAML.load_file(File.join(ARGV[0], "secrets", "LAYOUT.yaml"))
  (doc["secrets"] || {}).each do |k, s|
    [s, *((s["by_flavor"] || {}).values)].each do |e|
      g = e["gha_secret"]
      puts "#{k}: #{g}" if g && g != "GITHUB_TOKEN" && !names.include?(g)
    end
  end' "$ROOT" 2>&1)"
if [ -n "$missing" ]; then
  echo "  ✗ SN-2 LAYOUT gha_secret not read by any workflow (secrets.<NAME>) — fix the name in secrets/LAYOUT.yaml:"
  printf '%s\n' "$missing" | sed 's/^/      /'; fail=1
else
  echo "  ✓ SN-2 every LAYOUT gha_secret is a secret the workflows read"
fi
[ "$fail" = 1 ] && exit 1
[ "$warn" = 1 ] && exit 2
exit 0
