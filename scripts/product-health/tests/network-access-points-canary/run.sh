#!/usr/bin/env bash
# run.sh — RED/GREEN canary for checks/network-access-points.sh.
#
# The generated network surfaces are COMMITTED, so a repo whose app.yaml has moved on still compiles
# and is still wrong. Every NAP rule therefore gets its own RED tree: one fixture, one defect, so a
# rule that silently stops detecting is caught here rather than years later.
#
# The failure modes are not equally loud, which is the point of gating all of them:
#   NAP-3 and NAP-6 produce NO compile error at all — a missing UrlType constant silently resolves to
#   MAIN's base URL, and a committed publishable key works fine until someone needs to rotate it.
set -uo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CHECK="$(cd "$HERE/../../checks" && pwd)/network-access-points.sh"
rc_ok=0

cell() { # <tree> <expected-exit> <label> [app.yaml variant]
  local tree="$1" exp="$2" lbl="$3" yaml="$HERE/${4:-app.yaml}" out rc
  out="$(HEALTH_ROOT="$HERE" NAP_YAML="$yaml" NAP_NET_DIR="$HERE/$tree" bash "$CHECK" 2>&1)"; rc=$?
  # The label names the rule (e.g. "NAP-3 ..."); when a leg is expected to FAIL, require the output
  # to actually cite that rule. Exit code alone cannot distinguish "NAP-3 caught it" from "some other
  # rule fired" — nor from a vacuously-passing NAP-3 while a different rule keeps the exit non-zero.
  local want=""
  [ "$exp" = "1" ] && want="$(printf '%s' "$lbl" | grep -oE '^NAP-[0-9]+' || true)"
  if [ -n "$want" ] && ! printf '%s' "$out" | grep -q "$want"; then
    echo "   ❌ $lbl → exit $rc but did not cite $want (got: $(printf '%s' "$out" | grep -oE 'NAP-[0-9]+' | sort -u | tr '\n' ' '))"
    rc_ok=1; return
  fi
  if [ "$rc" = "$exp" ]; then
    echo "   ✅ $lbl → exit $rc (expected $exp)"
  else
    echo "   ❌ $lbl → exit $rc (expected $exp)"; printf '%s\n' "$out" | sed 's/^/        /'; rc_ok=1
  fi
}

echo "── network access points (network-access-points.sh) ──"
cell good                 0 "all surfaces match app-profile    → PASS"
cell nap1-missing-point   1 "NAP-1 point absent from registry  → FAIL"
cell nap2-stale-url       1 "NAP-2 registry base_url stale     → FAIL"
cell nap3-missing-urltype 1 "NAP-3 no UrlType const (silent!)  → FAIL"
cell nap4-missing-binding 1 "NAP-4 @ApiBinding, not bound      → FAIL"
cell nap5-missing-spec    1 "NAP-5 Supabase point, no spec     → FAIL"
cell nap6-literal-key     1 "NAP-6 publishable key committed   → FAIL"
cell nap7-handwired       1 "NAP-7 hand-wired binding          → FAIL"
# The subtlest cell: `pay-gw` HAS a UrlType constant, so an existence-only NAP-3 passes. Its KEY was
# sanitized to PAY_GW while AccessPoint.type carries UrlType("PAY-GW"), so restBaseUrl never matches
# and getBaseUrl silently returns MAIN's URL. Nothing fails to compile.
cell nap3-sanitized-key   1 "NAP-3 UrlType key sanitized too   → FAIL"
cell good                 1 "NAP-13 restates JSON Content-Type → FAIL" nap13-json-declared.yaml
cell good                 0 "NAP-13 non-JSON override allowed  → PASS" nap13-override.yaml
exit "$rc_ok"
