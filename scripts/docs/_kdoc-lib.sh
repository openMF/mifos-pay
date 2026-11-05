#!/usr/bin/env bash
# scripts/docs/_kdoc-lib.sh — the ONE upward walk from a declaration to the line that closes its KDoc.
#
# Three call sites needed this and each had its own copy (`kdoc_summary` + `kdoc_example` in
# api-docs-gen.sh, `has_kdoc` in kdoc-coverage.sh). kdoc-coverage.sh even carried a comment promising
# it used "the SAME walk" — a promise nothing enforced, and the copies then diverged. One function,
# sourced by both scripts.
#
# What the walk has to cross to get from a declaration up to its doc:
#   blank lines                       — normal formatting
#   single-line annotations           — `@Serializable`
#   MULTI-LINE annotation arg lists   — `@CacheKey(\n  fn = "of",\n  …\n)`
#
# That last case is why the naive version failed: walking up from the declaration it met the `)` that
# closes `@CacheKey(`, which is neither blank nor `@`-prefixed, so it stopped and reported the symbol
# as undocumented. Four `@StoreProvider` factories in core/store were documented all along and still
# printed `_No KDoc at source._` in the published reference.
#
# It crosses such a group ONLY after confirming the matching opener is an annotation. A bare `)`
# closing the PREVIOUS declaration's parameter list must stop the walk — crossing it would attribute
# that declaration's KDoc to this one, which is a false pass, and a false pass is worse than the miss
# it replaces.
#
# SPEED: every line is read from a per-file copy (`kdoc_load`), and trimmed / counted with shell
# expansions. The walk used to start a `sed` (and `tr | wc`) process per LINE visited — ~30,000 per
# `api-docs-gen.sh --all`, 237 s of it. Output is unchanged: `kdoc_line` returns exactly what
# `sed -n "<n>p"` printed, and the expansions match the sed/tr they replace under LC_ALL=C.

_KD_FILE=""; _KD_LINES=()   # the loaded file and its lines (kdoc_load / kdoc_line)

# kdoc_load <file> — read <file> once; kdoc_line then serves its lines without a process. Call it in
# the PARENT before the `$(…)` calls for that file — a subshell inherits the copy, it cannot fill it.
kdoc_load() {
  [ "$_KD_FILE" = "$1" ] && return 0
  _KD_FILE="$1"; _KD_LINES=()
  local _l
  while IFS= read -r _l || [ -n "$_l" ]; do _KD_LINES+=("$_l"); done < "$1"
}

# kdoc_line <file> <n> — sets KL to line <n> of <file> (empty past the end), as `sed -n "<n>p"` would.
kdoc_line() {
  if [ "$_KD_FILE" = "$1" ]; then KL="${_KD_LINES[$(( $2 - 1 ))]-}"; else KL="$(sed -n "${2}p" "$1")"; fi
}

# kdoc_ltrim <text> — sets KT to <text> without leading whitespace (`sed -E 's/^[[:space:]]+//'`).
kdoc_ltrim() { KT="${1#"${1%%[![:space:]]*}"}"; }

# kdoc_count <text> <char> — sets KC to how many times <char> occurs in <text> (`tr -cd <c> | wc -c`).
kdoc_count() { local _x="${1//[^$2]/}"; KC="${#_x}"; }

# kdoc_close_line <file> <declaration-line-no>
# Prints the 1-based line number that should hold the end of this declaration's KDoc (whatever is
# actually there — the caller decides whether it IS a KDoc). Prints 0 when the walk runs off the top.
kdoc_close_line() { kdoc_close_line_v "$1" "$2"; printf '%s' "$KCL"; }

# kdoc_close_line_v <file> <declaration-line-no> — the same walk, setting KCL instead of printing it.
# For hot loops: `$(kdoc_close_line …)` forks a subshell per declaration, which across `--all` cost
# more than the walk itself.
kdoc_close_line_v() {
  local f="$1" i="$2" t closes opens depth j
  i=$((i - 1))
  while [ "$i" -gt 0 ]; do
    kdoc_line "$f" "$i"; kdoc_ltrim "$KL"; t="${KT%"${KT##*[![:space:]]}"}"
    case "$t" in
      ""|"@"*) i=$((i - 1)); continue ;;
    esac
    kdoc_count "$t" ')'; closes="$KC"
    kdoc_count "$t" '('; opens="$KC"
    if [ "$closes" -le "$opens" ]; then
      break                                  # an ordinary line — this is where the doc must end
    fi
    # Balance upward to the opener, then cross the group only if the opener is an annotation.
    depth=$((closes - opens)); j=$((i - 1))
    while [ "$j" -gt 0 ] && [ "$depth" -gt 0 ]; do
      kdoc_line "$f" "$j"
      kdoc_count "$KL" ')'; closes="$KC"; kdoc_count "$KL" '('; opens="$KC"
      depth=$((depth + closes - opens))
      [ "$depth" -le 0 ] && break
      j=$((j - 1))
    done
    [ "$j" -gt 0 ] || break
    kdoc_line "$f" "$j"; kdoc_ltrim "$KL"
    case "$KT" in
      "@"*) i=$((j - 1)); continue ;;         # annotation group — keep walking above it
      *)    break ;;                          # someone else's parameter list — stop here
    esac
  done
  KCL="$i"
}
