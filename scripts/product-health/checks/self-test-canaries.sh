#!/usr/bin/env bash
# checks/self-test-canaries.sh — runs every product-health canary (tests/*/run.sh) so the RED/GREEN
# fixtures are LOAD-BEARING in CI, not decorative. Discovered + executed by product-health.sh (which
# runs every checks/*.sh), which quality-gate.yml already invokes. Each canary is self-contained
# (own GREEN/RED fixtures) and deterministic — runs in both fork and TEMPLATE_SELF_BUILD context.
#
#   exit 0 = every canary PASS · exit 1 = one or more canaries FAIL (blocks).
#
# Two meta-rules run before the canaries themselves: CI-1 (a RED leg must assert WHICH check
# fired, not just a non-zero exit) and CI-2 (a fixture on disk must be tracked, or CI runs
# against different fixtures than the author did).
#
# Locks (among others): flip-precondition-canary (guard↔YAML ownership drift, incl. og-images=generated)
# and sync-dirs-template-remote-canary (URL-match template resolution + ungated dry-run remote-add).
set -uo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"        # …/checks
TESTS_DIR="$(cd "$HERE/.." && pwd)/tests"                    # …/product-health/tests

shopt -s nullglob
canaries=("$TESTS_DIR"/*/run.sh)
[ "${#canaries[@]}" -eq 0 ] && { echo "self-test-canaries: no canaries found (skip)"; exit 0; }

# ── CI-1: a canary with a RED leg must assert WHICH check failed, not just a non-zero exit ──────
#
# This is the load-bearing rule, not a style preference. A gate that breaks VACUOUSLY — its scan
# yields zero items, so it reports success having examined nothing — still makes a RED fixture exit
# non-zero, because RED fixtures typically violate several rules at once. An exit-code-only canary
# therefore stays green while the rule it exists to prove has stopped running entirely.
#
# That is not hypothetical. G-WHITE-LABEL-SEPARATION's demo-import check was disabled for a while by
# an `unbound variable` in the pipeline feeding its loop (set -u killed the process substitution, the
# loop read nothing, the check passed on everything). Its canary stayed green throughout: the RED
# fixture still failed on four OTHER checks. Two blind spots covering for each other.
#
# A canary that greps the output for its own identifier cannot be fooled that way — a vacuous gate
# emits no such line. So: any canary with a red* fixture, or that says "expect FAIL", must inspect
# output, not just $?.
ci1_fail=0
for run in "${canaries[@]}"; do
  name="$(basename "$(dirname "$run")")"
  has_red=0
  compgen -G "$(dirname "$run")/red*" >/dev/null 2>&1 && has_red=1
  # Detection must be broad: canaries express "this leg must fail" in several shapes — a red* fixture,
  # "expect FAIL", `expect_fail_on`, or a `cell <fixture> 1 "..."` row. Missing one is a false
  # negative in the very rule meant to catch false negatives, which is how this check first passed
  # over network-access-points-canary (its legs say "→ FAIL", not "expect FAIL").
  grep -qiE 'expect (FAIL|a failure)|expect_fail|FAIL"|[[:space:]]1[[:space:]]+"' "$run" 2>/dev/null && has_red=1
  [ "$has_red" = "1" ] || continue
  # "inspects output" = pipes/greps the check's stdout somewhere, rather than only testing $? / if <cmd>
  if grep -qE 'grep -q|grep -oE|grep -c|\| *grep' "$run" 2>/dev/null; then continue; fi
  echo "  ❌ CI-1 $name has a RED leg but asserts only the exit code"
  echo "       → a vacuously-passing gate still makes RED exit non-zero; assert the specific check id"
  ci1_fail=1
done
[ "$ci1_fail" = "0" ] && echo "  ✓ CI-1 every RED leg asserts a specific check (${#canaries[@]} canaries)"

# ── CI-2: every fixture file must SURVIVE A CLONE ──────────────────────────────────────────────
#
# A canary is only load-bearing if CI receives the same fixtures the author ran against. A fixture
# that is present on disk but matched by .gitignore is invisible in that gap: it passes locally and
# fails in CI, and the failure names the CHECK's rule rather than the missing file, so the report
# points away from the cause.
#
# Not hypothetical — it happened while this very directory was being added. The repo ignores
# `.bundle/` globally (correct: it is bundler scratch), and the re-includes were anchored at the two
# real app roots. The ruby-toolchain-coherence fixtures each carry their own .bundle/config as
# FIXTURE DATA, matched that global ignore, and silently did not stage. Every leg passed locally.
# In CI the green fixture would have arrived without its root config, RT-6 would have fired, and the
# canary would have reported "root .bundle/config is UNTRACKED" about a fixture — true, useless.
#
# So: compare what is on disk against what git would ship. `git ls-files -o -i --exclude-standard`
# lists exactly the ignored-but-present files, which is the defect, stated directly.
ci2_fail=0
if git -C "$TESTS_DIR" rev-parse --git-dir >/dev/null 2>&1; then
  ignored="$(git -C "$TESTS_DIR" ls-files -o -i --exclude-standard -- . 2>/dev/null || true)"
  if [ -n "$ignored" ]; then
    echo "  ❌ CI-2 fixture file(s) exist on disk but are gitignored — absent from a fresh clone:"
    printf '%s\n' "$ignored" | sed 's|^|       |'
    echo "       → these pass locally and fail in CI for a reason the output does not name."
    echo "       → un-ignore them (a scoped '!' re-include) or stop generating them into the tree."
    ci2_fail=1
  else
    echo "  ✓ CI-2 every fixture on disk is tracked (survives a clone)"
  fi
fi

fail=$(( ci1_fail | ci2_fail ))
for run in "${canaries[@]}"; do
  name="$(basename "$(dirname "$run")")"
  out="$(bash "$run" 2>&1)"; rc=$?
  if [ "$rc" -eq 0 ]; then
    echo "  ✅ $name"
  else
    echo "  ❌ $name (exit $rc)"
    printf '%s\n' "$out" | sed 's/^/       /'
    fail=1
  fi
done
exit "$fail"
