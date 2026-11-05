#!/usr/bin/env bash
# template-migrations.sh — make a repo follow the renames the template made.
#
# When the template renames a file or an identifier, every fork still has the old one: a fork-owned
# seam the template now calls by its new name stops compiling, and a renamed merge-owned file loses its
# 3-way base. The template records each rename ONCE in `template-migrations.yaml`; this applies it.
# The sync engine runs it on the fork before merging (reading the ledger from the template commit being
# synced) and commits the result, so every merge already sees the new names. The template ran the same
# ledger on itself — one source for both sides.
#
#   template-migrations.yaml
#     migrations:
#       - id: <name>
#         words:                      # whole-word identifier rewrites
#           - "OldName -> NewName"    #   a tracked file named OldName.<ext> is also renamed NewName.<ext>
#         files:                      # path renames
#           - "a/Old.kt -> a/New.kt"  #   the path string is rewritten too (docs, xcfilelists)
#
#   template-migrations.sh [--ledger <file>] [--dry-run] [-- <pathspec>…]
#     stdout: one line per change — `R <old>\t<new>` (rename) · `E <file>` (rewritten) ·
#             `C <old>\t<new>` (both exist — left alone, needs a human)
#     Only tracked text files are touched (pathspec narrows them). The ledger itself is never rewritten.
#     A file containing the marker `template-migrations: keep` is never rewritten either (a fixture that
#     needs the OLD names, e.g. the test of this script).
#     Idempotent: an applied migration finds nothing left to change.
#   template-migrations.sh --ledger <file> --file <path>
#     rewrite ONE file's content (words + path strings; no renames, no git) — the sync runs a 3-way
#     merge's BASE through it, so the template's renames do not show up as template edits.
#   template-migrations.sh --ledger <file> --old-path <path>
#     print the path <path> had before the ledger renamed it (nothing if no row renames it) — where a
#     3-way merge finds the base of a renamed file in an older template commit.
# ponytail: no applied-id ledger — idempotence covers re-runs; add one if a fork must re-introduce an old name.
set -uo pipefail
LEDGER="template-migrations.yaml"; DRY=0; SPEC=(); ONE=""; OLDOF=""
while [ $# -gt 0 ]; do
  case "$1" in
    --ledger) LEDGER="$2"; shift 2 ;;
    --dry-run) DRY=1; shift ;;
    --file) ONE="$2"; shift 2 ;;
    --old-path) OLDOF="$2"; shift 2 ;;
    --) shift; SPEC=("$@"); break ;;
    *) echo "usage: $0 [--ledger <file>] [--dry-run] [-- <pathspec>…]" >&2; exit 2 ;;
  esac
done
[ -f "$LEDGER" ] || exit 0
[ "${#SPEC[@]}" -gt 0 ] || SPEC=(.)
EXCL=":(exclude)template-migrations.yaml"

# kind<TAB>from<TAB>to for every `"a -> b"` row under a `words:` or `files:` key.
rows="$(awk '
  /^[[:space:]]*(words|files):/ { k=$0; sub(/^[[:space:]]*/,"",k); sub(/:.*/,"",k); next }
  /^[[:space:]]*[a-z_]+:/      { k=""; next }
  k!="" && /->/ { s=$0; sub(/^[[:space:]]*-[[:space:]]*/,"",s); gsub(/"/,"",s); sub(/[[:space:]]*#.*/,"",s)
                  n=index(s,"->"); a=substr(s,1,n-1); b=substr(s,n+2); gsub(/[[:space:]]/,"",a); gsub(/[[:space:]]/,"",b)
                  if (a!="" && b!="") print k"\t"a"\t"b }' "$LEDGER")"

if [ -n "$OLDOF" ]; then
  while IFS=$'\t' read -r kind a b; do
    if [ "$kind" = files ]; then [ "$OLDOF" = "$b" ] && { echo "$a"; exit 0; }
    elif [ "$kind" = words ]; then
      f="${OLDOF##*/}"
      case "$f" in "$b".kt|"$b".kts|"$b".swift) echo "${OLDOF%/*}/$a.${f##*.}"; exit 0 ;; esac
    fi
  done <<< "$rows"
  exit 0
fi

if [ -n "$ONE" ]; then
  while IFS=$'\t' read -r kind a b; do
    [ -n "$kind" ] || continue
    if [ "$kind" = words ]; then A="$a" B="$b" perl -pi -e 's/\b\Q$ENV{A}\E\b/$ENV{B}/g' -- "$ONE"
    else A="$a" B="$b" perl -pi -e 's/\Q$ENV{A}\E/$ENV{B}/g' -- "$ONE"; fi
  done <<< "$rows"
  exit 0
fi

# mv_path <old> <new> — git-mv a tracked file; a target that already exists is reported (C), never overwritten.
mv_path() {
  # A case-only rename (main.kt → Main.kt) on a case-insensitive filesystem: the target "exists" because
  # it IS the source — rename through a temporary name.
  if [ -e "$2" ] && ! [ "$1" -ef "$2" ]; then printf 'C %s\t%s\n' "$1" "$2"; return; fi
  printf 'R %s\t%s\n' "$1" "$2"
  [ "$DRY" = 1 ] && return
  mkdir -p "$(dirname "$2")"
  if [ -e "$2" ]; then git mv -- "$1" "$1.tm-case" && git mv -- "$1.tm-case" "$2"; else git mv -- "$1" "$2"; fi
}
# rewrite word|path <from> <to> — replace a whole-word identifier, or a literal path string, in every
# tracked text file (within the pathspec) that contains it.
rewrite() {
  local mode="$1" a="$2" b="$3" f prog='s/\Q$ENV{A}\E/$ENV{B}/g' w=()
  [ "$mode" = word ] && { prog='s/\b\Q$ENV{A}\E\b/$ENV{B}/g'; w=(-w); }
  while IFS= read -r f; do
    [ -n "$f" ] || continue
    echo "E $f"
    [ "$DRY" = 1 ] || A="$a" B="$b" perl -pi -e "$prog" -- "$f"
  done < <(git grep -lI ${w[@]+"${w[@]}"} -F -e "$a" -- "${SPEC[@]}" "$EXCL" 2>/dev/null | grep -vxF -f <(git grep -lF "template-migrations: keep" -- "${SPEC[@]}" 2>/dev/null; echo /dev/null))
}

while IFS=$'\t' read -r kind a b; do
  [ -n "$kind" ] || continue
  if [ "$kind" = words ]; then
    while IFS= read -r f; do mv_path "$f" "$(dirname "$f")/$b.${f##*.}"; done \
      < <(git ls-files -- "${SPEC[@]}" | grep -E "(^|/)$a\.(kt|kts|swift)$")
    rewrite word "$a" "$b"
  else
    git ls-files --error-unmatch -- "$a" >/dev/null 2>&1 && mv_path "$a" "$b"
    rewrite path "$a" "$b"
  fi
done <<< "$rows"
exit 0
