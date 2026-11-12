#!/usr/bin/env bash
# Functional test: strings-union keeps the fork's own strings in the fork's order, so re-merging an
# unchanged file is a no-op. Runs the REAL cs_merge_strings from scripts/customization-surface.sh.
#
#   SU-1 fork-only entries follow the template's entries in the FORK's order (not awk hash order)
#   SU-2 a second merge of the result changes nothing (a sync no longer churns an untouched file)
#   SU-3 a key both sides define keeps the fork's value
set -uo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$HERE/../.." && pwd)"
READER="${SU_READER:-$REPO/scripts/customization-surface.sh}"
WORK="$(mktemp -d)"; trap 'rm -rf "$WORK"' EXIT
fail=0; pass() { echo "  ✅ $1"; }; bad() { echo "  ❌ $1"; fail=1; }

cat > "$WORK/theirs.xml" <<'XML'
<resources>
    <string name="app_name">App Toolkit</string>
    <string name="cd_back">Back</string>
</resources>
XML
cat > "$WORK/ours.xml" <<'XML'
<resources>
    <string name="app_name">Field Officer</string>
    <string name="zeta">Z</string>
    <string name="alpha">A</string>
    <string name="tab_groups">Groups</string>
    <string name="mid">M</string>
    <string name="search_title">Search</string>
</resources>
XML
: > "$WORK/base.xml"

# shellcheck disable=SC1090
. "$READER" >/dev/null 2>&1
cs_merge_strings "$WORK/ours.xml" "$WORK/base.xml" "$WORK/theirs.xml" "$WORK/out1.xml" || bad "merge 1 failed"
names() { sed -n 's/.*name="\([^"]*\)".*/\1/p' "$1" | tr '\n' ' '; }

[ "$(names "$WORK/out1.xml")" = "app_name cd_back zeta alpha tab_groups mid search_title " ] \
  && pass "SU-1 fork-only strings keep the fork's order" || bad "SU-1 order: $(names "$WORK/out1.xml")"
cs_merge_strings "$WORK/out1.xml" "$WORK/base.xml" "$WORK/theirs.xml" "$WORK/out2.xml" || bad "merge 2 failed"
cmp -s "$WORK/out1.xml" "$WORK/out2.xml" && pass "SU-2 re-merging the result is a no-op" \
  || bad "SU-2 second merge changed the file: $(names "$WORK/out2.xml")"
grep -q '<string name="app_name">Field Officer</string>' "$WORK/out1.xml" \
  && pass "SU-3 the fork's value wins on a shared key" || bad "SU-3 app_name lost the fork's value"

[ "$fail" = 0 ] && echo "strings-union-order-test: PASS" || echo "strings-union-order-test: FAIL"
exit "$fail"
