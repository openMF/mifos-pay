#!/usr/bin/env bash
# kdoc-comment-balance.sh — a glob written in a KDoc must not open or close a comment.
#
# WHY THIS EXISTS
# Kotlin block comments NEST, so what matters inside a `/** … */` is whether `/*` and `*/` BALANCE.
# A path or glob in prose can silently unbalance them:
#
#     /** … every `feature/*/di` Koin module … */
#                          ^^ the `/` + `*` OPENS a nested comment that never closes
#                             → "Unclosed comment", pointing at the END of the file
#
#     /** … `core/database/**/AppDatabase.kt` … */
#                          ^^^^ `/*` opens, `*/` closes — BALANCED, and therefore fine
#
# The two read identically to a human and behave oppositely. This was written after the same mistake
# landed three times in one session — in a generator's emitted KDoc, in a hand-written registry KDoc,
# and in a convention plugin's class doc. Each cost a full Gradle cycle to surface, and the compiler's
# message ("Unclosed comment" at some line far below) never names the glob that caused it.
#
# The compiler does catch these eventually. The point of this check is (a) seconds instead of a
# multi-minute build, (b) an error that names the offending line, and (c) covering GENERATED output,
# where a broken comment is emitted by code rather than typed by a person.
#
#   CB-1 an unbalanced `/*` or `*/` inside a block comment in tracked Kotlin source
#
# A deliberate inline example — `{ /* ... */ }` inside a KDoc — is balanced and passes untouched.
#
# Exit 0 = PASS · 1 = FAIL (blocks) · 2 = WARN. Pure bash + awk — no Kotlin parsing.
set -uo pipefail

ROOT="${1:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)}"
cd "$ROOT" 2>/dev/null || { echo "  ✗ CB-0 cannot enter $ROOT"; exit 2; }

pass() { printf '  ✓ %s\n' "$1"; }
fail() { printf '  ✗ %s\n' "$1"; FAILED=1; }
FAILED=0

# product-health/tests/** is excluded for the same reason shell-portability.sh excludes it: a RED
# canary fixture is DELIBERATELY broken, and it only proves the gate can fail if the gate still fails
# on it. Scanning the fixtures would make this check report its own test data as a defect — and,
# because the fixtures are tracked, would fail the repo the moment the canary was committed. That is
# exactly what happened: the canary landed, CI went red, and the "defect" was the proof of correctness.
FILES="$(git ls-files '*.kt' 2>/dev/null | grep -v '^scripts/product-health/tests/' || true)"
if [ -z "$FILES" ]; then
    echo "  ✗ CB-0 found 0 tracked .kt files — refusing a vacuous pass"
    exit 1
fi

# Only COMMENT-DECORATION lines are analysed — a line whose first non-space token is `/**`, `/*`, or
# a continuation `*`. That deliberately sidesteps Kotlin lexing: globs live overwhelmingly in STRING
# LITERALS (`exclude("**/*.kts")`, `targetExclude("**/build/**/*.kt")`), and a depth tracker with no
# string awareness reports every one of them. Measured on this tree: 11 findings, all strings, none
# real. A comment-decoration line has no such ambiguity.
#
# Counting is NON-OVERLAPPING, left to right, exactly as the compiler scans. That is what makes
# `feature/*/di` a bug and `core/database/**/AppDatabase.kt` fine:
#
#   feature/*/di   →  "/" "*" "/"      → one `/*`, zero `*/`   → depth +1, never closed
#   /**/           →  "/" "*" "*" "/"  → one `/*`, one  `*/`   → balanced
REPORT="$(printf '%s\n' "$FILES" | while IFS= read -r f; do
    [ -f "$f" ] || continue
    awk -v FNAME="$f" '
    {
        line = $0
        # Keep only comment-decoration lines; everything else may contain string literals.
        if (line !~ /^[ \t]*(\/\*|\*)/) next
        body = line
        sub(/^[ \t]+/, "", body)
        # A LEADING closer, before the continuation-star strip below. `*//**` — one comment ending
        # where the next begins, which Kotlin accepts — otherwise lost its `*` to that strip and the
        # remaining `//**` read as an opener with no close. That is a balanced line, and flagging it
        # sent a reader hunting for a nesting bug in a file the compiler was perfectly happy with.
        sub(/^\*\//, "", body)
        sub(/^\/\*\*?/, "", body)          # strip a leading /** or /*
        sub(/^\*/, "", body)                # strip a leading continuation *
        # …and the comment CLOSER. A one-line `/** … */` carries both; leaving the trailing `*/`
        # counted made every single-line KDoc look like it closed a comment it never opened.
        sub(/\*\/[ \t]*$/, "", body)
        opens  = gsub(/\/\*/, "", body)
        closes = gsub(/\*\//, "", body)
        if (opens != closes) {
            printf "%s:%d: comment text opens %d nested comment(s) and closes %d — a glob such as `a/*/b` reads as prose but changes comment depth\n", FNAME, NR, opens, closes
        }
    }' "$f"
done)"

if [ -n "$REPORT" ]; then
    printf '%s\n' "$REPORT" | while IFS= read -r l; do fail "CB-1 $l"; done
    FAILED=1
fi

n="$(printf '%s\n' "$FILES" | wc -l | tr -d ' ')"
if [ "$FAILED" -eq 0 ]; then
    pass "CB-1 $n Kotlin file(s) — every block comment balances"
fi
exit "$FAILED"
