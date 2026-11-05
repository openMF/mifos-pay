#!/usr/bin/env bash
# compose-strings-apostrophe.sh — no Android-style `\'` escape in Compose Multiplatform strings.
#
# WHY THIS EXISTS
# Android's resource compiler unescapes `\'`; Compose Multiplatform resources do NOT. The backslash
# reaches the screen: a render of feature/settings showed "You\'ll see bill reminders…", and a loan
# form showed "Could not load this product\'s terms." (found in mifos-x-field-officer-app
# 2026-10-23). It looks right in the XML and in an Android-only build, so nothing caught it — 64
# occurrences across 32 files of this template.
#
#   CS-1 a `\'` inside a composeResources/values*/strings.xml
#   CS-2 a `%%` inside one — same class: Android's formatter turns `%%` into `%`, Compose's
#        `stringResource(res, args)` only substitutes `%1$s`/`%1$d` and leaves `%%` as is. Found on
#        a device (2026-11-05): Today's Rates showed "3.88%%" and loan rows "40%% paid".
#
# A plain `'` / a single `%` is valid text and renders correctly; that is the fix.
# Exit 0 = PASS · 1 = FAIL (blocks).
set -uo pipefail

ROOT="${1:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)}"
cd "$ROOT" 2>/dev/null || { echo "  ✗ CS-0 cannot enter $ROOT"; exit 2; }

# product-health/tests/** holds RED fixtures that are broken on purpose (see kdoc-comment-balance.sh).
FILES="$(find . -path '*/build' -prune -o -path './scripts/product-health/tests' -prune -o \
              -path '*/composeResources/values*/strings.xml' -type f -print 2>/dev/null)"
if [ -z "$FILES" ]; then
    echo "  ✓ CS-1 no composeResources strings.xml in this tree — nothing to check"
    exit 0
fi

# report <id> <message> <hits> — print up to 20 hits; returns 1 when there are any.
report() {
    [ -n "$3" ] || return 0
    echo "  ✗ $1 $2"
    printf '%s\n' "$3" | head -20 | sed 's/^/      /'
    local n; n=$(printf '%s\n' "$3" | wc -l | tr -d ' ')
    [ "$n" -gt 20 ] && echo "      … and $((n - 20)) more"
    return 1
}

fail=0
HITS="$(printf '%s\n' "$FILES" | xargs grep -nF "\\'" 2>/dev/null | sed 's|^\./||' || true)"
report CS-1 "\\' in Compose strings — Compose Multiplatform renders the backslash; use a plain '" "$HITS" || fail=1
# `%%` followed by a digit is a literal `%` before a placeholder (Turkish "%%1$d" → "%40") — fine.
HITS="$(printf '%s\n' "$FILES" | xargs grep -nE '%%([^0-9]|$)' 2>/dev/null | sed 's|^\./||' || true)"
report CS-2 "%% in Compose strings — Compose Multiplatform renders both; use a single %" "$HITS" || fail=1
[ "$fail" = 0 ] || exit 1
echo "  ✓ CS-1/CS-2 no \\' or %% escapes in $(printf '%s\n' "$FILES" | wc -l | tr -d ' ') Compose strings files"
exit 0
