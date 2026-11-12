#!/usr/bin/env bash
# scan-bounded: reads only the GENERATED api-docs blocks under docs/ (RULE-CI-001). Never idea-layer.
#
# scripts/docs/api-index-gen.sh — the generator-facing slice of the API reference.
#
# WHY A SLICE AND NOT THE REFERENCE
# `docs/architecture/tree/**` answers "what exists" in full: 647 KB of signatures, KDoc and worked
# call sites. That is right for a human and impossible for a generator — the whole kmp IMPL_CONTEXT_PACK
# is 274 KB, so transcluding the reference would be 2.4x the entire budget and break
# RULE-IMPL-CONTEXT-PACK-SCOPED-001 on every subagent.
#
# Measured curation levels over the same source:
#     every symbol + summary, core* + feature   139 KB
#     types only + summary,   core*              51 KB   <- this
#     types only, no summary, core*              14 KB
#
# Types-only is the right cut because of WHICH defect this is for. A generator does not fail by
# forgetting a property; it fails by RE-DECLARING something core-base already owns — the
# duplicate-the-framework defect CORE_DATABASE.md warns about ("two invalidation paths, two converter
# registries"). Preventing that needs the set of type NAMES and one line on each, not their members.
#
# feature/* is deliberately EXCLUDED from the slice even though it is documented: a generator writing
# feature X gains nothing from feature Y's internal types, and including them costs 88 KB.
#
# DERIVED FROM THE GENERATED PAGES, not re-parsed from Kotlin: the pages are already produced from
# source, cross-platform reproducible and gated by doc-audit A4/A7. A second independent Kotlin parser
# would be a second thing to drift.
#
# Usage:  scripts/docs/api-index-gen.sh            # emit to stdout
#         scripts/docs/api-index-gen.sh --write    # write docs/architecture/API_INDEX.md
# Exit:   0 ok · 2 no generated pages to read
set -uo pipefail
export LC_ALL=C

TMPL="${TEMPLATE_PATH:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)}"
TREE="$TMPL/docs/architecture/tree"
[ -d "$TREE" ] || { echo "api-index-gen: no generated tree at $TREE" >&2; exit 2; }

emit() {
  printf '<!-- api-index:begin generator=scripts/docs/api-index-gen.sh -->\n'
  printf '# Template API index — public types of `core-base/*` and `core/*`\n\n'
  printf 'Generated. Every type below ALREADY EXISTS in the template: call it, never re-declare it.\n'
  printf 'Members, parameters and worked call sites live in `docs/architecture/tree/<layer>/<module>.md`.\n\n'
  local f layer mod blk n=0
  while IFS= read -r f; do
    [ -f "$f" ] || continue
    grep -q 'api-docs:begin' "$f" || continue
    layer="$(basename "$(dirname "$f")")"; mod="$(basename "$f" .md)"
    blk="$(sed -n '/api-docs:begin/,/api-docs:end/p' "$f" \
           | sed '/<details>/,/<\/details>/d')"   # drop worked samples; the slice is names + intent
    # A fenced signature, then the first prose line under it. awk over the block rather than a
    # per-symbol subprocess: 422 rows x a grep each is the difference between a step and a stall.
    local rows
    rows="$(printf '%s\n' "$blk" | awk '
      /^```kotlin$/ { inf = 1; sig = ""; next }
      inf && /^```$/ { inf = 0; want = 1; next }
      inf { if (sig == "") sig = $0; next }
      want {
        if ($0 ~ /^[[:space:]]*$/) next
        if ($0 ~ /^(###|<!--|```)/) { want = 0; next }
        # types only — a generator re-declares TYPES, not members
        if (sig ~ /(^|[[:space:]])(class|interface|object)([[:space:]]|$)/) {
          s = $0; gsub(/^[[:space:]]+|[[:space:]]+$/, "", s)
          # Truncate on a WORD BOUNDARY. A mid-token cut turns `kpt.core.model.banking.Loan` into
          # `kpt.core.model.banking.Lo`, which TTD-15 then reports as an API that does not exist —
          # a phantom defect manufactured by the formatter. Drop the partial token instead.
          if (length(s) > 90) {
            s = substr(s, 1, 90)
            if (s ~ /[[:space:]]/) sub(/[[:space:]][^[:space:]]*$/, "", s)
            s = s "…"
          }
          printf "- `%s` — %s\n", sig, s
        }
        want = 0
      }')"
    [ -n "$rows" ] || continue
    printf '## `%s/%s`\n\n%s\n\n' "$layer" "$mod" "$rows"
    n=$((n+1))
  done < <(find "$TREE/core-base" "$TREE/core" -maxdepth 1 -name '*.md' -type f 2>/dev/null | LC_ALL=C sort)
  printf '<!-- api-index:end modules=%s -->\n' "$n"
}

if [ "${1:-}" = "--write" ]; then
  OUT="$TMPL/docs/architecture/API_INDEX.md"
  emit > "$OUT.tmp" && mv "$OUT.tmp" "$OUT"
  printf 'api-index-gen: wrote %s (%s KB, %s types)\n' \
    "${OUT#"$TMPL"/}" "$(( $(wc -c < "$OUT") / 1024 ))" "$(grep -c '^- `' "$OUT")"
else
  emit
fi
