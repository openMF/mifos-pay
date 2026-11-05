#!/usr/bin/env bash
# run.sh — canary for the LOCAL-RUN TEMPLATE-MODE HINT (lib.sh#wl_identity_is_reference +
# product-health.sh's footer).
#
# The decision this locks: mode is decided ONLY by TEMPLATE_SELF_BUILD, which quality-gate.yml
# sets from `github.repository` ('1' upstream, '' on every fork). A local clone has no such
# signal, so it runs in FORK mode and the three directional identity checks (fork-identity /
# deployment-whitelabel B1+B8 / secrets-alias-namespace) FAIL against the committed Mifos
# reference identity. That default is correct and STAYS — the footer explains the red rather
# than inferring it away.
#
# Why no auto-detect: a real white-label fork commonly also has `upstream` pointing at openMF, so
# remote-based inference would hand it template mode and a SILENT PASS on exactly the checks that
# exist to make it rebrand. A missing hint is an annoyance; a false PASS is the failure mode this
# suite exists to prevent. L5 below is the standing guard against that "fix".
#
# This canary must NOT invoke product-health.sh: self-test-canaries.sh runs from INSIDE
# product-health.sh, so doing so would recurse. The predicate is exercised directly; the footer's
# wiring is asserted structurally.
set -uo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PH_DIR="$(cd "$HERE/../.." && pwd)"                     # …/scripts/product-health
# shellcheck source=../../lib.sh
source "$PH_DIR/lib.sh"
rc_ok=0

REF_ID="org.mifos.kmp.template"; REF_ORG="Mifos Initiative"
FORK_ID="com.acme.budget";       FORK_ORG="Acme Inc"

pred() { # <app_id> <org> <expected-exit> <label>
  local id="$1" org="$2" exp="$3" lbl="$4" rc
  wl_identity_is_reference "$id" "$org"; rc=$?
  if [ "$rc" = "$exp" ]; then
    echo "   ✅ $lbl → exit $rc (expected $exp)"
  else
    echo "   ❌ $lbl → exit $rc (expected $exp)"; rc_ok=1
  fi
}

echo "── template-mode hint · wl_identity_is_reference ──"
# L1 both fields still the committed reference → this checkout LOOKS like the upstream template.
pred "$REF_ID"  "$REF_ORG"  0 "L1 reference appId + reference org  → is-reference"
# L2-L4 expect FAIL (exit 1). A HALF-rebranded fork is a genuine finding and must NOT be talked
# out of its failure by the hint — requiring BOTH fields is the whole point of the predicate.
pred "$FORK_ID" "$FORK_ORG" 1 "L2 rebranded appId + rebranded org → not-reference"
pred "$FORK_ID" "$REF_ORG"  1 "L3 rebranded appId + reference org → not-reference (half-rebranded)"
pred "$REF_ID"  "$FORK_ORG" 1 "L4 reference appId + rebranded org → not-reference (half-rebranded)"

# ── L5 structural: the footer stays a HINT, and mode stays env-only ─────────────────────────
PH="$PH_DIR/product-health.sh"
echo "── template-mode hint · product-health.sh wiring ──"

# The footer must be gated on BOTH the env mode AND the predicate — an ungated footer would print
# on a properly-rebranded fork, where the red IS the real finding.
if grep -q 'TEMPLATE_SELF_BUILD:-}" != "1" \] && \[ "$fail" -gt 0 \] && wl_identity_is_reference' "$PH"; then
  echo "   ✅ L5a footer gated on TEMPLATE_SELF_BUILD != 1 AND fail>0 AND wl_identity_is_reference"
else
  echo "   ❌ L5a footer is not gated on env-mode + fail-count + wl_identity_is_reference"; rc_ok=1
fi

# The footer must never ASSIGN the mode — it explains, it does not infer. Anchored at STATEMENT
# position: the hint's own text legitimately contains the string `TEMPLATE_SELF_BUILD=1 bash …`
# as the command to copy, and that is prose inside an echo, not an assignment.
if grep -qE '^[[:space:]]*(export[[:space:]]+)?TEMPLATE_SELF_BUILD=' "$PH"; then
  echo "   ❌ L5b product-health.sh ASSIGNS TEMPLATE_SELF_BUILD — mode must stay caller/env-owned"; rc_ok=1
else
  echo "   ✅ L5b product-health.sh never assigns TEMPLATE_SELF_BUILD (mode stays env-owned)"
fi

# No git-remote / GITHUB_REPOSITORY inference may creep back in as a "convenience fix".
if grep -qE '^[^#]*(git remote|git config --get remote|GITHUB_REPOSITORY)' "$PH"; then
  echo "   ❌ L5c product-health.sh infers mode from a git remote — a fork with upstream=openMF"
  echo "        would get a SILENT PASS on the rebrand checks. Mode is env-only by decision."
  rc_ok=1
else
  echo "   ✅ L5c no git-remote/GITHUB_REPOSITORY mode inference"
fi

exit "$rc_ok"
