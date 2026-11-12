#!/usr/bin/env bash
# scripts/docs/kdoc-coverage.sh — who is missing KDoc, and where.
#
# The API reference can only be as good as the KDoc it extracts: every symbol without one renders as
# `_No KDoc at source._`. This reports that gap per module, and lists the exact undocumented symbols
# so the work is a worklist rather than a hunt.
#
# It uses the SAME "is there a KDoc directly above this declaration" walk as api-docs-gen.sh — an
# annotation or blank line between the block and the declaration is skipped, because that is how
# Kotlin is actually written. A simpler grep (count `/**` vs count declarations) reports above 100%
# for any module that documents MEMBERS as well as top-level symbols, which is not a coverage figure.
#
# Usage:
#   scripts/docs/kdoc-coverage.sh                 # per-module summary + total
#   scripts/docs/kdoc-coverage.sh <layer>/<mod>   # list that module's UNDOCUMENTED symbols
#   scripts/docs/kdoc-coverage.sh --min <pct>     # exit 1 if total coverage is below <pct>
# Env: TEMPLATE_PATH
# Exit: 0 ok · 1 below --min · 2 usage
set -uo pipefail

# Byte-deterministic text processing. Generated output must be a pure function of the tree, and the
# locale silently breaks that in two ways: `sort` collates differently (a UTF-8 locale folds case, so
# `di/SecurityModule.kt` sorts before `FailedAttemptTracker.kt` while C order puts it after), and `.`
# in a length-bounded regex counts CHARACTERS under UTF-8 but BYTES under C — so a 241-character KDoc
# summary containing an em dash (243 bytes) kept its second sentence on a Mac and lost it on the
# Linux CI runner. Pinning to C fixes the comparison basis everywhere; no script here runs Python, so
# this cannot force a Python stdout to ASCII.
export LC_ALL=C
ROOT="${TEMPLATE_PATH:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)}"
cd "$ROOT" || exit 2
. "$(dirname "${BASH_SOURCE[0]}")/_kdoc-lib.sh"

MIN=""
TARGET=""
while [ $# -gt 0 ]; do
  case "$1" in
    --min) MIN="${2:-}"; shift 2 ;;
    --*)   shift ;;
    *)     [ -z "$TARGET" ] && TARGET="$1"; shift ;;
  esac
done

DECL_RE='^(public |internal )?(expect |actual )?(annotation |data |sealed |enum |value |abstract |open )*(suspend )?(fun|val|var|class|interface|object|typealias)[[:space:]]'

# has_kdoc <file> <declaration-line-no> → 0 when a KDoc block closes directly above it
has_kdoc() {
  local f="$1" ln="$2" i t
  i="$(kdoc_close_line "$f" "$ln")"
  [ "$i" -gt 0 ] || return 1
  kdoc_line "$f" "$i"; kdoc_ltrim "$KL"; t="$KT"
  # Both KDoc shapes count. A single-line `/** Summary. */` opens and closes on one line, so testing
  # only for a leading `*/` under-reports every one-line doc — the same miss that made the published
  # reference claim "_No KDoc at source._" for symbols that had it.
  case "$t" in
    "/**"*"*/") return 0 ;;
    "*/"|"*/ "*)  return 0 ;;
  esac
  return 1
}

scan() {  # $1 = layer/module, $2 = "list" to print undocumented symbols
  local lm="$1" mode="${2:-}" src total=0 documented=0 f hit ln sig
  src="$ROOT/$lm/src/commonMain"
  [ -d "$src" ] || { echo "0 0"; return; }
  while IFS= read -r f; do
    [ -z "$f" ] && continue
    kdoc_load "$f"
    while IFS= read -r hit; do
      [ -z "$hit" ] && continue
      ln="${hit%%:*}"; sig="${hit#*:}"
      sig="$(printf '%s' "$sig" | sed -E 's/[[:space:]]*\{[[:space:]]*$//; s/[[:space:]]+$//')"
      total=$((total+1))
      if has_kdoc "$f" "$ln"; then
        documented=$((documented+1))
      elif [ "$mode" = list ]; then
        printf '  %s:%s\n    %s\n' "${f#$ROOT/}" "$ln" "$sig"
      fi
    done < <(grep -nE "$DECL_RE" "$f" 2>/dev/null)
  done < <(find "$src" -name '*.kt' -type f 2>/dev/null | LC_ALL=C sort)
  [ "$mode" = list ] || echo "$total $documented"
}

if [ -n "$TARGET" ]; then
  echo "Undocumented public symbols in $TARGET:"
  scan "$TARGET" list
  read -r t d <<<"$(scan "$TARGET")"
  echo
  echo "  $((t-d)) of $t undocumented ($(( t>0 ? d*100/t : 100 ))% covered)"
  exit 0
fi

printf '%-26s %7s %7s %7s %6s\n' MODULE symbols documented missing pct
T=0; D=0
for d in "$ROOT"/core-base/*/ "$ROOT"/core/*/; do
  [ -d "$d/src/commonMain" ] || continue
  lm="${d%/}"; lm="${lm#$ROOT/}"
  read -r t dd <<<"$(scan "$lm")"
  [ "${t:-0}" -gt 0 ] || continue
  T=$((T+t)); D=$((D+dd))
  printf '%-26s %7s %7s %7s %5s%%\n' "$lm" "$t" "$dd" "$((t-dd))" "$((dd*100/t))"
done
echo "──────────────────────────────────────────────────────────────"
PCT=$(( T>0 ? D*100/T : 100 ))
printf '%-26s %7s %7s %7s %5s%%\n' TOTAL "$T" "$D" "$((T-D))" "$PCT"

if [ -n "$MIN" ]; then
  if [ "$PCT" -lt "$MIN" ]; then
    echo
    echo "KDOC-COVERAGE FAIL: $PCT% is below the $MIN% floor."
    echo "  List a module's gaps:  scripts/docs/kdoc-coverage.sh <layer>/<module>"
    exit 1
  fi
  echo "KDOC-COVERAGE PASS: $PCT% ≥ $MIN% floor"
fi
exit 0
