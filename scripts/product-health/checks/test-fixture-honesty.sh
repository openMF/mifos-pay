#!/usr/bin/env bash
# test-fixture-honesty.sh — a test fixture must PRODUCE the failure mode it claims.
#
# WHY THIS EXISTS
# `ErrorCategory.categorize()` keys Network off the exception's CLASS-NAME chain, not its message —
# commonMain has no shared `IOException` to test against, so the name is all there is. That makes one
# particular fixture silently wrong:
#
#     Fetcher.of { throw RuntimeException("no network") }      // reads like an offline failure
#
# It categorizes `Generic`, so `isExpectedWhenOffline()` rejects it, `DecisionEngine` falls through to
# NoNetwork instead of the offline-first Empty, and the test asserting offline-first behaviour passes
# for the wrong reason — or races, passing on a fast machine and failing under instrumentation.
#
# That is not hypothetical. It shipped in `ScreenDataStreamIntegrationTest`, presented as a flaky
# test, and the honest fix was `FakeIOException` — a NAME the categorizer recognises. The message was
# always right; the type was always wrong.
#
# The tell is mechanical: the MESSAGE claims network semantics while the CLASS NAME does not deliver
# them. That gap is exactly what this check looks for.
#
#   TF-1 an exception whose message claims network/offline/connection semantics, thrown with a type
#        whose name is NOT Network-categorized (the patterns mirror ErrorCategory exactly)
# NOT IN SCOPE — unfailable assertions (`assertTrue(true …)`) are already owned by the shipped
# `no-vacuous-assert.sh` (VA-1/VA-2), which also handles the case where Spotless has WRAPPED the call
# across lines. A second scanner for the same defect would be weaker than the one that exists and
# would drift from it; this check deliberately covers only the fixture-type gap that nothing else does.
#
# TF-1 accepts a DECLARED exemption within three lines above (`// fixture-ok: <reason>`) — never an
# implied one. A fixture deliberately asserting the Generic path says so.
#
# Exit 0 = PASS · 1 = FAIL (blocks) · 2 = WARN. Pure bash + grep — no Kotlin reflection.
set -uo pipefail

ROOT="${1:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)}"

# A match inside a COMMENT is prose about the defect, not the defect — this file's own docs, and
# the very comments explaining the historical bug, would otherwise fail the check that documents it.
is_comment() { printf '%s' "$1" | grep -qE '^[[:space:]]*(//|\*|/\*)'; }

# Markers sit on the line above, but an explanation often wraps across two or three comment lines,
# so scan a small window rather than exactly one line.
has_marker() {  # $1 file · $2 line · $3 marker
    local f="$1" ln="$2" m="$3" from=$((ln - 3))
    [ "$from" -lt 1 ] && from=1
    sed -n "${from},$((ln - 1))p" "$f" 2>/dev/null | grep -q "$m"
}

# Print a passing assertion.
pass() { printf '  ✓ %s\n' "$1"; }
# Print a failing assertion and set FAILED, so the check reports every problem in one run
# rather than stopping at the first.
fail() { printf '  ✗ %s\n' "$1"; FAILED=1; }
FAILED=0

# Mirrors ErrorCategory.matchesNetworkClassName(). Kept as one string so the two can be diffed by eye
# when a pattern is added there — a name the categorizer accepts must be accepted here too, or this
# check would reject a fixture that actually works.
NET_NAMES='IOException|Connect|Timeout|UnknownHost|SSLException|Offline|HttpRequestException|Socket'
# Message words a developer reaches for when they mean "the network failed".
NET_WORDS='no network|offline|network error|connection refused|connection reset|unreachable|cannot connect|failed to connect|no internet'

TESTS="$(find "$ROOT" -type d -name commonTest -not -path '*/build/*' -not -path '*/scripts/*' 2>/dev/null)"
if [ -z "$TESTS" ]; then
    echo "  – no commonTest source sets in this fork — nothing to check"
    exit 0
fi

n_files=0
while IFS= read -r f; do
    [ -f "$f" ] || continue
    n_files=$((n_files + 1))
    rel="${f#"$ROOT"/}"

    # ── TF-1 ─────────────────────────────────────────────────────────────────
    while IFS=: read -r ln body; do
        [ -z "$ln" ] && continue
        # The thrown TYPE, e.g. `throw RuntimeException("no network")` -> RuntimeException
        is_comment "$body" && continue
        typ="$(printf '%s' "$body" | sed -n 's/.*throw \([A-Za-z0-9_]*\).*/\1/p')"
        [ -z "$typ" ] && continue
        printf '%s' "$typ" | grep -qiE "$NET_NAMES" && continue          # name delivers — fine
        has_marker "$f" "$ln" 'fixture-ok:' && continue
        fail "TF-1 $rel:$ln throws $typ with a message claiming network failure — categorize() keys Network off the CLASS NAME, so this is Generic and the offline-first branch is never entered. Use an ${typ}-style name containing one of: IOException/Connect/Timeout/UnknownHost/SSLException/Offline/HttpRequestException/Socket"
    done < <(grep -niE "throw [A-Za-z0-9_]+\(\"[^\"]*($NET_WORDS)" "$f" 2>/dev/null || true)


done < <(find $TESTS -name '*.kt' -type f 2>/dev/null)

if [ "$n_files" -eq 0 ]; then
    echo "  ✗ TF-0 found 0 commonTest .kt files — refusing a vacuous pass"
    exit 1
fi

if [ "$FAILED" -eq 0 ]; then
    pass "TF-1 $n_files commonTest file(s) — no fixture claims a failure mode it does not produce"
fi

exit "$FAILED"
