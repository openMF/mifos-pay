#!/usr/bin/env bash
# scripts/product-health/product-health.sh — product health / sanity harness.
#
# Reads gradle/fork.properties (the project-level SINGLE SOURCE OF TRUTH) and runs every
# scripts/product-health/checks/*.sh against it. This is the ONE place a fork learns whether its
# customization is complete + correct: signing/org identity re-forked, appId consolidated, store
# listing authored. Runs in CI (quality-gate.yml) and at the end of scripts/white-label/fork-init.sh so a fresh fork
# gets an immediate sanity report. Pure bash — no Gradle, no network.
#
# Check contract (each checks/*.sh): exit 0 = PASS · exit 1 = FAIL (blocks) · exit 2 = WARN
# (needs attention, non-blocking). Fork-only checks self-skip when TEMPLATE_SELF_BUILD=1.
#
# product-health exit: 0 when no check FAILs (WARNs allowed) · 1 when any check FAILs.
#
# Checks run in PARALLEL (they are independent processes, read-only against HEALTH_ROOT). Measured on
# the template: 111s serial → 34s at the machine's core count, byte-identical output. Concurrency is
# capped at the core count; override with PH_JOBS=<n> or --jobs=<n>, or use --serial to run them one
# at a time when debugging a check that misbehaves under concurrency.
set -uo pipefail

HEALTH_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"   # …/scripts/product-health
HEALTH_ROOT="$(cd "$HEALTH_DIR/../.." && pwd)"               # repo root
# shellcheck source=scripts/product-health/lib.sh
source "$HEALTH_DIR/lib.sh"

FORK_PROPERTIES="$(health_resolve_sot "$HEALTH_ROOT")" || {
  echo "${C_RED}❌ project-health: no gradle/fork.properties(.template) found — is this a fork of kmp-project-template?${C_RST}" >&2
  exit 1
}
export FORK_PROPERTIES HEALTH_ROOT

# ── Header — name the project from its SoT ───────────────────────────────────
org="$(fp_get org.name)"; [ -n "$org" ] || org="(unset)"
app_id="$(fp_get app.id)"
[ -n "$app_id" ] || app_id="$(grep -E '^\s*appId\s*=' "$HEALTH_ROOT/gradle/libs.versions.toml" 2>/dev/null | head -1 | sed 's/.*=[[:space:]]*//; s/[[:space:]]*#.*$//; s/"//g; s/[[:space:]]*$//')"
[ -n "$app_id" ] || app_id="?"
echo "── ${C_BLD}Product Health${C_RST} · ${org} ${C_DIM}(${app_id})${C_RST} ──"
echo "   ${C_DIM}SoT: ${FORK_PROPERTIES#$HEALTH_ROOT/}${C_RST}"
[ "${TEMPLATE_SELF_BUILD:-}" = "1" ] && echo "   ${C_DIM}TEMPLATE_SELF_BUILD=1 — fork-only checks skip (this IS the upstream template)${C_RST}"
echo ""

# ── Run every check ──────────────────────────────────────────────────────────
# PARALLEL by default. The checks are independent by construction — each is its own process, reads
# HEALTH_ROOT and writes nothing back (the one that mutates, demo-strip-coherence, works inside a
# throwaway copy). So the serial loop was spending wall-clock for no isolation benefit: measured on
# the template, 182s serial against a 45s slowest check.
#
# Output stays IDENTICAL to the serial run. Each check's stdout+stderr is buffered to its own file
# and the report is printed afterwards in glob order, so the ordering does not depend on which check
# happened to finish first — a parallel harness whose output reorders between runs is one nobody can
# diff.
#
# Concurrency is capped rather than "launch all 29": several checks shell out heavily (the strip
# copies a tree, the canaries run sub-suites) and oversubscribing a 2-core CI runner makes the slowest
# check slower. PH_JOBS overrides; --serial restores the old one-at-a-time behaviour for debugging a
# check that misbehaves under concurrency.
PH_SERIAL=0
# Fail-fast is the DEFAULT for an interactive run and OFF in CI. A developer wants the first failure
# now; CI wants the complete picture in one pass, because a run that stops at the first failure hides
# the other four and turns one red build into five sequential ones. PH_FAILFAST=0/1 overrides either.
PH_FAILFAST="${PH_FAILFAST:-$([ -n "${CI:-}" ] && echo 0 || echo 1)}"
for a in "$@"; do
  case "$a" in
    --serial)     PH_SERIAL=1 ;;
    --jobs=*)     PH_JOBS="${a#--jobs=}" ;;
    --fail-fast)  PH_FAILFAST=1 ;;
    --no-fail-fast|--all) PH_FAILFAST=0 ;;
  esac
done
PH_FAILFAST_NOTE=""
[ "$PH_FAILFAST" -eq 1 ] && PH_FAILFAST_NOTE="  — stopping early (--all to run everything)"
if [ -z "${PH_JOBS:-}" ]; then
  PH_JOBS="$( (sysctl -n hw.ncpu 2>/dev/null || nproc 2>/dev/null || echo 4) | head -1 )"
  case "$PH_JOBS" in (''|*[!0-9]*) PH_JOBS=4 ;; esac
fi

fail=0; warn=0; pass=0; skip=0
PH_TMP="$(mktemp -d)"
trap 'rm -rf "$PH_TMP"' EXIT

if [ "$PH_SERIAL" -eq 0 ]; then
  # `jobs -r` rather than `wait -n`: bash 4.3+ only, and macOS ships 3.2. This harness runs on a dev
  # machine as often as on CI, so the throttle has to work on both.
  for chk in "$HEALTH_DIR"/checks/*.sh; do
    [ -f "$chk" ] || continue
    name="$(basename "$chk" .sh)"
    # FAIL FAST: stop launching the moment something has already failed. Parallel made the suite
    # faster; it did NOT make it fail faster, because the report only prints once every check is in.
    # A failure found at second 2 was invisible until second 34, which is the opposite of what a
    # pre-commit harness is for.
    if [ "$PH_FAILFAST" -eq 1 ] && [ -e "$PH_TMP/.failed" ]; then break; fi
    while [ "$(jobs -r 2>/dev/null | wc -l | tr -d ' ')" -ge "$PH_JOBS" ]; do
      if [ "$PH_FAILFAST" -eq 1 ] && [ -e "$PH_TMP/.failed" ]; then break; fi
      sleep 0.05
    done
    (
      bash "$chk" >"$PH_TMP/$name.out" 2>&1; _rc=$?
      printf '%s' "$_rc" >"$PH_TMP/$name.rc"
      # Announce a failure THE MOMENT it happens, out of order and clearly marked. The ordered report
      # below is still the record; this is the early warning, so `^C` is a real option on a long run.
      if [ "$_rc" -ne 0 ] && [ "$_rc" -ne 2 ]; then
        : > "$PH_TMP/.failed"
        printf '  %s✗ FAILED%s  %s%s\n' "$C_RED" "$C_RST" "$name" "${PH_FAILFAST_NOTE}" >&2
      fi
    ) &
  done
  wait
fi

for chk in "$HEALTH_DIR"/checks/*.sh; do
  [ -f "$chk" ] || continue
  name="$(basename "$chk" .sh)"
  if [ "$PH_SERIAL" -eq 1 ]; then
    out="$(bash "$chk" 2>&1)"; rc=$?
  else
    # Never launched (fail-fast broke the loop) → SKIPPED, not PASS. Counting an unrun check as a
    # pass is how a fail-fast harness silently shrinks its own coverage.
    if [ ! -e "$PH_TMP/$name.rc" ] && [ ! -e "$PH_TMP/$name.out" ]; then
      skip=$((skip+1)); printf '  %s⊘ SKIP%s   %s %s(not run — earlier failure)%s\n' "$C_DIM" "$C_RST" "$name" "$C_DIM" "$C_RST"
      continue
    fi
    out="$(cat "$PH_TMP/$name.out" 2>/dev/null)"
    rc="$(cat "$PH_TMP/$name.rc" 2>/dev/null)"
    # A missing .rc means the subshell died without recording one (OOM-killed, or the check exec'd
    # something that took the process down). Treat it as FAIL: a check whose verdict is unknown has
    # not passed, and defaulting it to 0 is how a parallel harness silently loses coverage.
    case "${rc:-}" in (''|*[!0-9]*) rc=1; out="${out}
  ❌ no exit status recorded — the check did not complete" ;; esac
  fi
  case "$rc" in
    0) pass=$((pass+1)); printf '  %s✅ PASS%s  %s\n' "$C_GRN" "$C_RST" "$name" ;;
    2) warn=$((warn+1)); printf '  %s⚠️  WARN%s  %s\n' "$C_YEL" "$C_RST" "$name" ;;
    *) fail=$((fail+1)); printf '  %s❌ FAIL%s  %s\n' "$C_RED" "$C_RST" "$name" ;;
  esac
  # Show the check's own lines (indented) whenever it wasn't a clean pass.
  [ "$rc" -ne 0 ] && [ -n "$out" ] && printf '%s\n' "$out" | sed 's/^/         /'
done

echo ""
if [ "${skip:-0}" -gt 0 ]; then
  echo "── ${pass} passed · ${warn} warn · ${fail} failed · ${skip} not run ──"
  echo "   ${C_DIM}stopped at the first failure; run with --all for the full picture${C_RST}"
else
  echo "── ${pass} passed · ${warn} warn · ${fail} failed ──"
fi
[ "$warn" -gt 0 ] && [ "$fail" = 0 ] && echo "   ${C_DIM}(warnings don't block — but resolve them before releasing)${C_RST}"

# ── Local-run hint: fork mode is the SAFE DEFAULT, so say why, don't infer around it ─────────
#
# Mode comes only from TEMPLATE_SELF_BUILD, which quality-gate.yml sets from `github.repository`
# ('1' upstream, '' on every fork). A LOCAL clone has no such signal, so it runs in fork mode —
# and in the template checkout the three directional identity checks (fork-identity /
# deployment-whitelabel B1/B8 / secrets-alias-namespace) then FAIL by design: they are reading the
# committed Mifos reference identity as "a fork that has not rebranded yet".
#
# That default is deliberate and stays. Auto-detecting template mode from git remotes would give a
# real white-label fork — which commonly also has `upstream` pointing at openMF — a silent local
# pass on the very checks that exist to tell it to rebrand. A missing hint is an annoyance; a
# false PASS is the failure this suite exists to prevent. So: explain the red, never remove it.
if [ "${TEMPLATE_SELF_BUILD:-}" != "1" ] && [ "$fail" -gt 0 ] && wl_identity_is_reference "$app_id" "$org"; then
  echo ""
  echo "   ${C_DIM}Note: identity still matches the committed Mifos reference${C_RST}"
  echo "   ${C_DIM}(${app_id} / ${org}), so the directional identity checks read this as an${C_RST}"
  echo "   ${C_DIM}un-rebranded FORK. If this checkout IS the upstream template, re-run:${C_RST}"
  echo "   ${C_DIM}  TEMPLATE_SELF_BUILD=1 bash scripts/product-health/product-health.sh${C_RST}"
  echo "   ${C_DIM}If it is a fork, this is the real finding — rebrand in app-profile/.${C_RST}"
fi

[ "$fail" -gt 0 ] && exit 1
exit 0
