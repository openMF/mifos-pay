#!/usr/bin/env bash
# run.sh — RED/GREEN canary for checks/fork-props-manifest-parity.sh.
#
# gradle/fork.properties has two writers (derive.rb via AppProfile::MAP, syncForkConfig §5b via
# APP_PROFILE_MAP). If their manifests drift, the same repo yields a DIFFERENT bridge depending on
# which one ran, and consumers silently fall back for whatever the running writer omitted. A gate
# that cannot fail would not have caught the real 2026-09-06 drift, so both directions are proven:
#
#   identical manifests        → PASS
#   key missing from one       → FAIL  (the real incident's shape)
#   same key, different path   → FAIL
#   non-app-profile key mapped → FAIL  (even when BOTH manifests agree)
set -uo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CHECK="$(cd "$HERE/../../checks" && pwd)/fork-props-manifest-parity.sh"
rc_ok=0

cell() { # <rb-fixture> <expected-exit> <label> [kt-fixture]
  local rb="$1" exp="$2" lbl="$3" kt="${4:-kt-in-sync.kt}" out rc
  out="$(HEALTH_ROOT="$HERE" FORK_PARITY_KT="$HERE/$kt" FORK_PARITY_RB="$HERE/$rb" bash "$CHECK" 2>&1)"; rc=$?
  # A leg expected to FAIL must actually emit a diagnostic. Exit 1 with no ❌ line means the check
  # bailed early (missing fixture, unset var) rather than detecting the defect under test.
  if [ "$exp" = "1" ] && ! printf '%s' "$out" | grep -q '❌'; then
    echo "   ❌ $lbl → exit $rc but produced no diagnostic (check bailed, did not detect)"; rc_ok=1; return
  fi
  if [ "$rc" = "$exp" ]; then
    echo "   ✅ $lbl → exit $rc (expected $exp)"
  else
    echo "   ❌ $lbl → exit $rc (expected $exp)"; printf '%s\n' "$out" | sed 's/^/        /'; rc_ok=1
  fi
}

echo "── manifest parity (fork-props-manifest-parity.sh) ──"
cell rb-in-sync.rb     0 "manifests identical        → PASS"
cell rb-missing-key.rb 1 "key missing from one       → FAIL"
cell rb-wrong-path.rb  1 "same key, different path   → FAIL"
# Both manifests agree here — so pure parity would PASS. It must still fail: project.name's SoT is
# the tracked version catalog, and giving it an app-profile home hands derive.rb a second resolution
# order. This is the cell that stops a well-meaning "fix" of a deliberate asymmetry.
cell rb-mismapped.rb   1 "non-app-profile key mapped → FAIL" kt-mismapped.kt
exit "$rc_ok"
