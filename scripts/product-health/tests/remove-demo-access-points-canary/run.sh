#!/usr/bin/env bash
# remove-demo-access-points-canary — step 4d deletes the DEMO points and KEEPS template + fork.
#
# It shipped inverted: the awk dropped `owner: template` while the set feeding step 4c was named
# TEMPLATE_POINTS but collected `o == "demo"`. So 4c deleted the demo PACKAGES and 4d deleted the
# template ENTRY — leaving six demo endpoints declared with no code, and one template endpoint with
# code but no declaration (an @ApiBinding naming an undeclared id: NAP-4).
#
# Exit 0 = PASS · 1 = FAIL.
set -uo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "$DIR/../../../.." && pwd)"

owners() {
  awk '/^[[:space:]]*-[[:space:]]*id:/{id=$0;sub(/.*id:[[:space:]]*/,"",id);sub(/[[:space:]].*$/,"",id);next}
       /^[[:space:]]*owner:/{o=$0;sub(/.*owner:[[:space:]]*/,"",o);sub(/[[:space:]].*$/,"",o);
         if(id!=""){print id":"o;id=""}}' "$1"
}

# Run the SAME awk step 4d uses, extracted from the shipped script so the canary cannot drift from
# it. Take the lines strictly BETWEEN the `awk '` opener and the line that closes the quote — an
# earlier attempt kept the closing `' app-profile/app.yaml > …` line and awk choked on it.
prog="$(awk "
  /── 4d\\./        { in4d = 1 }
  in4d && /awk '/  { body = 1; next }
  body && /^  ' /   { exit }
  body             { print }
" "$ROOT/scripts/remove-demo.sh")"
[ -n "$prog" ] || { echo "  ✗ could not extract step 4d awk from remove-demo.sh"; exit 1; }

out="$(mktemp)"; trap 'rm -f "$out"' EXIT
awk "$prog" "$DIR/app.yaml" > "$out"

fail=0
after="$(owners "$out")"
for keep in "main:fork" "projectref:template"; do
  printf '%s\n' "$after" | grep -qx "$keep" || { echo "  ✗ $keep was DELETED — it must be kept"; fail=1; }
done
for gone in "coingecko:demo" "worldbank:demo"; do
  printf '%s\n' "$after" | grep -qx "$gone" && { echo "  ✗ $gone survived — demo points must be deleted"; fail=1; }
done
[ "$fail" -eq 0 ] && echo "  ✓ step 4d keeps fork+template, deletes demo"
exit "$fail"
