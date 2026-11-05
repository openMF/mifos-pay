#!/usr/bin/env bash
# scan-bounded: pure bash + grep/find/sed over one template module tree (RULE-CI-001). Never idea-layer.
#
# scripts/docs/api-docs-gen.sh — function/class-level API reference for a template module, FROM SOURCE.
#
# WHY
# ───
# `core-base/**` is template-owned and READ-ONLY to generators (D9), which makes its documentation the
# entire interface: a generator that cannot see what the base provides RE-IMPLEMENTS it in `core/**`.
# CORE_DATABASE.md already warns about that outcome ("...compiles and then duplicates the framework:
# two invalidation paths, two converter registries") — the warning shipped while the information
# needed to obey it did not.
#
# Measured 2026-09-26 on kmp-project-template:
#   · core-base/* commonMain: 1,674 public declarations, 959 KDoc blocks (57% documented at source)
#   · the generator-facing CORE_BASE_*.md guides: 38 callable signatures documented (2%)
#   · docs/architecture/tree/core-base/*.md: prose-level ("you do not write here"), ~9 API lines
# So the KDoc largely EXISTS in source and simply never reaches a reader. This script is the bridge.
#
# SoT IS THE TEMPLATE, ALWAYS
# ───────────────────────────
# Every line is derived from template source at the revision being scanned. Nothing here is authored,
# so it cannot drift from the code it documents — the failure mode a hand-written API reference has by
# construction, and the one TTD-6 already exists to catch for the corpus. Re-run after any template
# change and the docs are current by definition.
#
# OUTPUT is a managed block, so authored prose survives:
#   <!-- api-docs:begin module=<m> sha=<tree-sha> -->  …generated…  <!-- api-docs:end -->
# `architecture-docs-scaffold.sh` already uses the same convention (`<!-- scaffold:end -->`) to
# preserve hand-written sections; this follows it rather than inventing a second mechanism.
#
# Usage:
#   scripts/docs/api-docs-gen.sh <layer>/<module>            # e.g. core-base/store, core/data
#   scripts/docs/api-docs-gen.sh --all                       # every core-base/*, core/* and feature/* module
#   scripts/docs/api-docs-gen.sh <layer>/<module> --sha       # print the module tree sha only
# Env: TEMPLATE_PATH  override the template root (canaries)
# Exit: 0 ok · 2 usage / module not found
set -uo pipefail

# Byte-deterministic text processing. Generated output must be a pure function of the tree, and the
# locale silently breaks that in two ways: `sort` collates differently (a UTF-8 locale folds case, so
# `di/SecurityModule.kt` sorts before `FailedAttemptTracker.kt` while C order puts it after), and `.`
# in a length-bounded regex counts CHARACTERS under UTF-8 but BYTES under C — so a 241-character KDoc
# summary containing an em dash (243 bytes) kept its second sentence on a Mac and lost it on the
# Linux CI runner. Pinning to C fixes the comparison basis everywhere; no script here runs Python, so
# this cannot force a Python stdout to ASCII.
export LC_ALL=C

# This file SHIPS IN THE TEMPLATE: the tree it documents is the repository it lives in, so a fork
# and a GitHub Action both get the generator with no framework checkout. TEMPLATE_PATH overrides it
# for the framework (which drives the same script against a checkout elsewhere) and for canaries.
TMPL="${TEMPLATE_PATH:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)}"
FW="$TMPL"
. "$(dirname "${BASH_SOURCE[0]}")/_kdoc-lib.sh"

TARGET="${1:-}"
SHA_ONLY=0
for a in "$@"; do [ "$a" = "--sha" ] && SHA_ONLY=1; done
WANT_SIDEBAR=0
for a in "$@"; do [ "$a" = "--sidebar" ] && WANT_SIDEBAR=1; done
# `--sidebar` needs no module target, so it must not trip the target requirement (it did, printing a
# spurious "no such module --sidebar" before doing the right thing anyway).
if [ "$WANT_SIDEBAR" -eq 1 ]; then TARGET="${TARGET:---sidebar}"; fi
if [ "$WANT_SIDEBAR" -eq 0 ] && [ -z "$TARGET" ]; then
  echo "usage: scripts/docs/api-docs-gen.sh <layer>/<module>|--all [--sha|--write] | --sidebar" >&2; exit 2
fi

# Tree sha of the module — the anchor that makes staleness detectable with the mechanism already in
# use for training surfaces (module_tree_sha). Falls back to a content hash outside a git tree.
module_sha() {  # $1 = layer/module → the ONE shared working-tree anchor
  # Delegated on purpose: a second copy of the hashing rule would drift, and a drifted anchor
  # fails silently green because both sides compute the same wrong value.
  bash "$(dirname "${BASH_SOURCE[0]}")/module-hash.sh" "$1" 2>/dev/null
}

# ── Summary text transforms, as shell expansions on KS ────────────────────────────────────────────
# They were a `sed` per documented symbol (~2,700 per `--all`). Each matches the sed expression it
# names, under the LC_ALL=C pinned above; `refresh.sh --check` byte-compares the result, so a
# divergence cannot go unnoticed.
SENTENCE_RE='^(.{0,240}[.!?])([[:space:]].*)?$'    # s/^(.{0,240}[.!?])([[:space:]].*)?$/\1/

# _kd_links — unwraps KDoc links in KS: `s/\[([A-Za-z0-9_.]+)\]/`\1`/g`. The leftmost regex match
# is always the first literal occurrence of the matched text, so slicing on it is exact.
_kd_links() {
  local out="" rest="$KS" re='\[([A-Za-z0-9_.]+)\]' m
  while [[ "$rest" =~ $re ]]; do
    m="${BASH_REMATCH[0]}"
    out+="${rest%%"$m"*}\`${BASH_REMATCH[1]}\`"; rest="${rest#*"$m"}"
  done
  KS="$out$rest"
}

# _kd_squeeze — collapses whitespace runs in KS to one space and trims it:
# `s/[[:space:]]+/ /g; s/^ //; s/ $//`.
_kd_squeeze() {
  KS="${KS//[[:space:]]/ }"
  while [[ "$KS" == *"  "* ]]; do KS="${KS//  / }"; done
  KS="${KS# }"; KS="${KS% }"
}

# ── KDoc summary immediately ABOVE a declaration ─────────────────────────────────────────────────
# Walks upward from the declaration line while the lines belong to a `/** … */` block, then sets KSUM
# to its first sentence (status 1 when there is no KDoc). Markdown/KDoc tags (@param, @return, @see)
# are dropped: the summary is what a generator needs to pick the right call, and the tags are noise
# at index density.
kdoc_summary() {  # $1 = file  $2 = declaration line number
  local f="$1" ln="$2" i txt line acc="" _kl=""
  # Shared walk: crosses blank lines, single-line annotations AND multi-line annotation arg lists.
  kdoc_close_line_v "$f" "$ln"; i="$KCL"
  # The line must now CLOSE a KDoc block — in either of its two shapes. A single-line
  # `/** Summary. */` closes and opens on the same line, so a test that only accepts a leading `*/`
  # misses it entirely. That is not rare styling: it is how nearly every one-line doc in this
  # template is written, and the miss printed "_No KDoc at source._" in the PUBLISHED reference for
  # symbols that were documented all along (measured on SubmitStateExtensions.kt, where all six
  # extension properties carry one-line KDoc).
  kdoc_line "$f" "$i"; kdoc_ltrim "$KL"; _kl="$KT"
  case "$_kl" in
    "/**"*"*/")
      # Single-line: the summary is the text between the delimiters — no upward walk needed.
      KS="${_kl#/\*\*}"; KS="${KS#"${KS%%[![:space:]]*}"}"
      KS="${KS%\*/}"; KS="${KS%"${KS##*[![:space:]]}"}"
      _kd_links; _kd_squeeze; KSUM="$KS"
      return 0 ;;
    "*/"|"*/ "*) ;;
    *) return 1 ;;
  esac
  i=$((i-1))
  while [ "$i" -gt 0 ]; do
    kdoc_line "$f" "$i"; line="$KL"
    # = sed -E 's/^[[:space:]]*\*?[[:space:]]?//' : leading blanks, one optional `*`, one optional blank
    kdoc_ltrim "$line"; txt="${KT#\*}"; txt="${txt#[[:space:]]}"
    case "$KT" in
      "/**"*) break ;;
    esac
    case "$txt" in
      "@"*) acc="" ;;                       # a tag block ends the summary
      *)    [ -n "$txt" ] && acc="$txt${acc:+ }$acc" ;;
    esac
    i=$((i-1))
  done
  [ -n "$acc" ] || return 1
  # first sentence, collapsed, KDoc links unwrapped
  KS="$acc"; _kd_links
  [[ "$KS" =~ $SENTENCE_RE ]] && KS="${BASH_REMATCH[1]}"
  _kd_squeeze; KSUM="$KS"
}


# ── kdoc_example — the authored ```kotlin block inside a symbol's KDoc ────────────────────────────
# `kdoc_summary` returns the first SENTENCE, which is right for an index and wrong for codegen: the
# 81 hand-written code fences in this template's KDoc are the most useful thing in it for a model
# deciding HOW to call an API, and they were being discarded. Emitted verbatim, because a
# reformatted example is a different example.
kdoc_example() {  # $1 = file  $2 = declaration line number → KEX (empty = no example)
  local f="$1" ln="$2" start=0 i
  KEX=""
  # walk up to the `/**` that opens this symbol's KDoc — same shared walk as kdoc_summary
  kdoc_close_line_v "$f" "$ln"; i="$KCL"
  [ "$i" -gt 0 ] || return 1
  kdoc_line "$f" "$i"; kdoc_ltrim "$KL"
  case "$KT" in
    "*/"|"*/ "*) ;;
    *) return 1 ;;     # single-line KDoc cannot hold a fence
  esac
  local close="$i"
  while [ "$i" -gt 0 ]; do
    kdoc_line "$f" "$i"; kdoc_ltrim "$KL"
    case "$KT" in "/**"*) start="$i"; break ;; esac
    i=$((i-1))
  done
  [ "$start" -gt 0 ] || return 1
  # No fence in the block → no example. Checked on the loaded lines first: 81 KDocs in this template
  # carry a fence and over a thousand do not, and the pipeline below is four processes per symbol.
  local k=$start fenced=0
  while [ "$k" -le "$close" ]; do
    kdoc_line "$f" "$k"; case "$KL" in *'```'*) fenced=1; break ;; esac
    k=$((k+1))
  done
  [ "$fenced" -eq 1 ] || return 0
  # strip the leading ` * ` gutter, then keep only the fenced region
  KEX="$(sed -n "${start},${close}p" "$f" \
    | sed -E 's|^[[:space:]]*/?\*+/?[[:space:]]?||' \
    | awk '/^```/{f=!f; next} f' \
    | sed -E 's/[[:space:]]+$//')"
}

# ── call-site index — built ONCE, then looked up per symbol ───────────────────────────────────────
# The demo features exist to showcase these APIs, so the template already contains a correct,
# compiling call for nearly everything. That is better evidence than a synthesised snippet: it cannot
# drift (CI compiles it) and it shows the surrounding wiring a bare signature omits.
#
# Built as ONE index rather than a grep per symbol. The per-symbol version ran a full `grep -r` over
# feature/ + core/ for each of 127 symbols in core-base/store alone and blew a 10-minute timeout;
# this is a single pass, reused for every lookup.
# NOTE the index is built EAGERLY, at start-up, into a path fixed for this process.
#
# It was lazy — `call_site` built it on first use and cached it in CALLSITE_INDEX. But call_site runs
# inside `$( … )`, which is a SUBSHELL: the assignment never reached the parent, so the "cache" was
# rebuilt for EVERY symbol. 914 symbols × a full-tree scan is why `--all` ran past ten minutes.
# A variable cannot memoise across command substitution; the file has to exist before the loop starts.
CALLSITE_INDEX="${TMPDIR:-/tmp}/kpt-callsites-$$.idx"

# Declaration kind and declared name, matched with `[[ =~ ]]` (see emit_module). Kept in variables:
# a regex written inline after `=~` must not be quoted, and these contain spaces and `|`.
KIND_RE='(annotation class|data class|sealed class|sealed interface|enum class|value class|abstract class|class|interface|object|typealias|fun|val|var)'
SYM_RE='(fun|val|var|class|interface|object|typealias)[[:space:]]+(<[^>]*>[[:space:]]*)?([A-Za-z0-9_]+(<[^>]*>)?\.)?[A-Za-z0-9_]+'
build_callsite_index() {
  [ -s "$CALLSITE_INDEX" ] && return 0
  # ONE awk pass over every .kt under feature/ + core/, emitting `identifier|path|line`.
  #
  # Import, package and comment lines are skipped AT INDEX TIME. Without that the first hit for a
  # symbol is almost always its own import statement — which rendered an "example" that was seven
  # lines of imports and demonstrated nothing.
  #
  # One pass rather than grep-per-symbol: the per-symbol version blew a 10-minute timeout on a single
  # module, and a grep|awk pipeline still re-read the tree. Everything here is read once.
  #
  # `| sort` is LOAD-BEARING, not tidiness. call_site() picks a symbol's example with `head -1`, so
  # the example it renders is whichever matching file this index lists FIRST. `find` emits directory
  # order, which is a property of the filesystem — APFS and ext4 disagree — so without the sort the
  # generated API blocks differ between a contributor's Mac and the Linux CI runner, and
  # `refresh.sh --check` reports DRIFT on one while reporting IN STEP on the other. Generated output
  # has to be a pure function of the tree; every other `find` in this file is already sorted.
  # THREE PASSES, IN PREFERENCE ORDER — not one sorted list. call_site() takes `head -1`, so the
  # order here decides WHICH example a reader is shown, and the useful answer is "the one closest to
  # what a generator writes": a feature call site beats a core one, which beats a core-base internal.
  # A single `sort` over all three would put `core-base/...` first for every symbol by collation
  # alone and demote every feature example. Each pass is sorted INTERNALLY, so the result is still a
  # pure function of the tree.
  #
  # Within a pass, `commonMain` sorts before `commonTest`, so production usage is preferred over a
  # test automatically — and tests still provide the fallback. That fallback is why `core-base` is
  # scanned at all: nothing in feature/ or core/ calls `core-base/security`, so its 21 symbols had
  # ZERO examples while its own 7 test files exercised them (77 such files across core-base).
  {
    find "$TMPL/feature"   -name build -prune -o -name '*.kt' -type f -print 2>/dev/null | LC_ALL=C sort
    find "$TMPL/core"      -name build -prune -o -name '*.kt' -type f -print 2>/dev/null | LC_ALL=C sort
    find "$TMPL/core-base" -name build -prune -o -name '*.kt' -type f -print 2>/dev/null | LC_ALL=C sort
  } \
    | xargs awk '
        /^[[:space:]]*(import|package)[[:space:]]/ { next }
        /^[[:space:]]*(\/\/|\*|\/\*)/          { next }
        {
          line = $0
          # `[A-Za-z0-9_][A-Za-z0-9_]+` rather than the equivalent-looking `{2,}`: mawk — which is
          # /usr/bin/awk on Ubuntu, so on the CI runner — ACCEPTS the interval but does not apply it
          # greedily. It matched `Kpt`, `The`, `Foo` where BSD awk and gawk match `KptTheme`, so the
          # index filled with three-letter fragments, every symbol lookup missed, and the published
          # pages silently lost ALL their "Used in the template" examples on Linux while keeping
          # them on a Mac. `+` is greedy in every awk.
          while (match(line, /[A-Z][A-Za-z0-9_][A-Za-z0-9_]+/)) {
            print substr(line, RSTART, RLENGTH) "|" FILENAME "|" FNR
            line = substr(line, RSTART + RLENGTH)
          }
        }' 2>/dev/null \
    | awk -F'|' '++n[$1] <= 40' > "$CALLSITE_INDEX" 2>/dev/null || true
  # Capped at 40 rows per symbol: call_site reads at most the first 40 (`grep -m 40`), so the cap
  # changes no result and turns each per-symbol grep from a scan of every token in the tree into a
  # scan of a few thousand rows.
}

call_site() {  # $1 = symbol name  $2 = declaring file → KCS="relpath|line" (status 1 = none)
  local sym="$1" own="${2#$TMPL/}" hit="" row file line rest
  # One grep reads the index; the own-file filter, first-hit pick and field split are shell work
  # (they were three more processes per symbol: `grep -v | head -1`, then `cut` twice).
  while IFS= read -r row; do
    case "$row" in *"|${TMPL}/${own}|"*) continue ;; esac
    hit="$row"; break
  done < <(grep -m 40 "^${sym}|" "$CALLSITE_INDEX" 2>/dev/null)
  [ -n "$hit" ] || return 1
  rest="${hit#*|}"; file="${rest%%|*}"; file="${file#$TMPL/}"
  line="${rest#*|}"; line="${line%%|*}"
  [ "$file" = "$own" ] && return 1
  case "$line" in ''|*[!0-9]*) return 1 ;; esac
  KCS="$file|$line"
}

emit_module() {  # $1 = layer/module
  local lm="$1" layer mod src sha
  layer="${lm%%/*}"; mod="${lm##*/}"
  [ -d "$TMPL/$lm" ] || { echo "template-api-docs-gen: no such module $lm under $TMPL" >&2; return 2; }
  src="$TMPL/$lm/src/commonMain"
  [ -d "$src" ] || src="$TMPL/$lm/src"
  sha="$(module_sha "$lm")"

  if [ "$SHA_ONLY" -eq 1 ]; then printf '%s\n' "$sha"; return 0; fi

  printf '<!-- api-docs:begin module=%s sha=%s -->\n' "$lm" "$sha"
  printf '## API reference\n\n'
  printf '_Generated from `%s` at tree `%s` by `scripts/docs/api-docs-gen.sh`._\n' "$lm" "${sha:0:12}"
  printf '_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._\n\n'
  if [ "$layer" = "feature" ]; then
    printf 'This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published\n'
    printf 'here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,\n'
    printf 'with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`\n'
    printf 'rather than re-declaring what they already own.\n\n'
  fi
  if [ "$layer" = "core-base" ]; then
    printf 'This module is **framework-shared and read-only to generators** (D9). Everything below is\n'
    printf 'something a feature CALLS; re-declaring one of these in `core/**` is the duplicate-the-\n'
    printf 'framework defect. A change here is a TEMPLATE change and flows upstream as a draft PR\n'
    printf '(RULE-TEMPLATE-MODULE-FIX-UPSTREAM-001), never a local fix.\n\n'
  fi

  local n_types=0 n_funs=0 n_doc=0 n_ex=0 n_cs=0 _ex='' _sym='' _cs='' _csf='' _csl='' _from=0
  local f rel

  while IFS= read -r f; do
    [ -z "$f" ] && continue
    rel="${f#"$TMPL"/}"
    kdoc_load "$f"   # one read per file; the $(kdoc_…) calls below inherit it
    local file_emitted=0 ln sig kind name doc indent

    # Top-level declarations only (indent 0) — a nested member is emitted under its container below.
    while IFS= read -r hit; do
      [ -z "$hit" ] && continue
      ln="${hit%%:*}"
      sig="${hit#*:}"
      # Trim a trailing `{` and trailing blanks, then pick the declaration kind — with shell expansion
      # and `[[ =~ ]]` (POSIX leftmost-longest, the same match `grep -oE | head -1` returned), not three
      # processes per declaration.
      sig="${sig%"${sig##*[![:space:]]}"}"
      case "$sig" in *"{") sig="${sig%\{}"; sig="${sig%"${sig##*[![:space:]]}"}" ;; esac
      kind=""; [[ "$sig" =~ $KIND_RE ]] && kind="${BASH_REMATCH[1]}"
      case "$kind" in fun|val|var) n_funs=$((n_funs+1)) ;; *) n_types=$((n_types+1)) ;; esac

      if [ "$file_emitted" -eq 0 ]; then
        printf '### `%s`\n\n' "$rel"
        file_emitted=1
      fi
      printf '```kotlin\n%s\n```\n' "$sig"
      # Helpers return through KSUM / KEX / KCS, not stdout: a `$( … )` around each forked a subshell
      # per declaration, three times over.
      if kdoc_summary "$f" "$ln"; then doc="$KSUM"
        n_doc=$((n_doc+1))
        printf '%s\n\n' "$doc"
      else
        printf '_No KDoc at source._\n\n'
      fi

      # ── examples, for a reader deciding HOW to call this ──
      kdoc_example "$f" "$ln"; _ex="$KEX"
      if [ -n "$_ex" ]; then
        n_ex=$((n_ex+1))
        printf '<details><summary>Example</summary>\n\n```kotlin\n%s\n```\n\n</details>\n\n' "$_ex"
      fi
      _sym=""
      if [[ "$sig" =~ $SYM_RE ]]; then
        _sym="${BASH_REMATCH[0]}"; [[ "$_sym" =~ [A-Za-z0-9_]+$ ]] && _sym="${BASH_REMATCH[0]}"
      fi
      if [ -n "$_sym" ] && call_site "$_sym" "$f"; then _cs="$KCS"
        n_cs=$((n_cs+1))
        _csf="${_cs%%|*}"; _csl="${_cs##*|}"
        _from=$(( _csl > 2 ? _csl - 2 : 1 ))
        printf '<details><summary>Used in the template — <code>%s:%s</code></summary>\n\n```kotlin\n%s\n```\n\n</details>\n\n' \
          "$_csf" "$_csl" "$(sed -E -n "${_from},$((_csl+4)){s/[[:space:]]+$//;p;}" "$TMPL/$_csf")"
      fi

      # Container members — what a caller actually invokes. `object StoreFactory` on its own tells a
      # generator nothing; its six create* functions ARE the Store5 entry points.
      case "$kind" in
        object|interface|"abstract class")
          local mem mln msig mdoc c=0
          while IFS= read -r mem; do
            [ -z "$mem" ] && continue
            [ "$c" -ge 14 ] && { printf -- "  _…more members; read the file._\n"; break; }
            mln="${mem%%:*}"; msig="${mem#*:}"
            msig="${msig#"${msig%%[![:space:]]*}"}"; msig="${msig%"${msig##*[![:space:]]}"}"
            case "$msig" in *"{") msig="${msig%\{}"; msig="${msig%"${msig##*[![:space:]]}"}" ;; esac
            c=$((c+1)); n_funs=$((n_funs+1))
            # `--` is required: a format string starting with `-` is parsed as a printf OPTION.
            printf -- '- `%s`' "$msig"
            if kdoc_summary "$f" "$mln"; then mdoc="$KSUM" n_doc=$((n_doc+1)); printf ' — %s' "$mdoc"; fi
            printf '\n'
          done < <(
            # Members must be scoped to THIS container's line range, not the whole file. Grepping the
            # file listed the first container's members under every later one — measured when a new
            # FeatureFlagStore.kt declared `interface FeatureFlagStore` followed by
            # `object TemplateFeatureFlags`, and the object was documented with the interface's four
            # functions. The range ends at the next TOP-LEVEL declaration (column 0) or EOF.
            #
            # `val`/`const val` are included alongside `fun`: a constants object's API IS its vals,
            # and listing only functions documented such an object as if it were empty.
            awk -v start="$ln" '
              NR <= start { next }
              /^[A-Za-z@]/ { exit }                      # next top-level declaration ends the range
              /^[[:space:]]+(public[[:space:]]+)?(abstract[[:space:]]+|open[[:space:]]+|inline[[:space:]]+|const[[:space:]]+)*(suspend[[:space:]]+)?(fun|val)[[:space:]]/ {
                print NR ":" $0
              }
            ' "$f" 2>/dev/null
          )
          [ "$c" -gt 0 ] && printf '\n' ;;
      esac
    done < <(grep -nE '^(public )?(expect |actual )?(annotation |data |sealed |enum |value |abstract |open )*(suspend )?(fun|val|var|class|interface|object|typealias)[[:space:]]' "$f" 2>/dev/null)
  done < <(find "$src" -name '*.kt' -type f 2>/dev/null | LC_ALL=C sort)

  printf -- '---\n\n'
  printf '_%s type(s), %s function(s)/property(ies); %s carry KDoc at source; %s authored example(s); %s live call site(s)._\n' "$n_types" "$n_funs" "$n_doc" "$n_ex" "$n_cs"
  printf '<!-- api-docs:end -->\n'
}

# ── --write: inject the block into the module's architecture page, in place ───────────────────────
# The page is the ARCHITECTURE SoT and already carries authored prose ("you do not write here", the
# upstream-fix contract). That prose is the half a human needs and the generator cannot produce, so
# the block is injected between markers and everything outside them is preserved byte-for-byte —
# the same contract `architecture-docs-scaffold.sh` uses for `<!-- scaffold:end -->`.
page_for() {  # $1 = layer/module → docs path
  # Module guides live under the TREE — one hierarchy, not a parallel modules/ directory.
  printf '%s/docs/architecture/tree/%s/%s.md' "$TMPL" "${1%%/*}" "${1##*/}"
}

# Regenerate one module's api-docs block in place.
# Builds the body into a temp file first, so a failed generation leaves the page
# untouched rather than half-written.
write_module() {  # $1 = layer/module
  local lm="$1" page tmp body
  page="$(page_for "$lm")"
  [ -f "$page" ] || { echo "template-api-docs-gen: no architecture page for $lm at ${page#"$TMPL"/} — run architecture-docs-scaffold.sh first" >&2; return 2; }
  local bodyf; bodyf="$(mktemp -t apidocsbody.XXXXXX)"
  emit_module "$lm" > "$bodyf" || { rm -f "$bodyf"; return 2; }
  [ -s "$bodyf" ] || { rm -f "$bodyf"; echo "template-api-docs-gen: generator produced NOTHING for $lm — refusing to write" >&2; return 2; }
  tmp="$(mktemp -t apidocs.XXXXXX)"
  if grep -q '<!-- api-docs:begin' "$page"; then
    # Replace the block, keeping everything around it. The replacement is streamed FROM A FILE, never
    # passed through `awk -v`: an -v value cannot carry newlines, so the multi-line body made awk
    # abort ("newline in string") mid-write and the redirect left the page TRUNCATED TO ZERO. That
    # destroyed core-base/store.md on first re-run — recovered from git, and guarded three ways now:
    # a file-based replacement, the empty-body refusal above, and the non-empty assertion below.
    awk -v bf="$bodyf" '
      /<!-- api-docs:begin/ { while ((getline l < bf) > 0) print l; close(bf); skip=1; next }
      /<!-- api-docs:end -->/ { skip=0; next }
      !skip { print }
    ' "$page" > "$tmp"
  else
    cat "$page" > "$tmp"
    printf '\n' >> "$tmp"
    cat "$bodyf" >> "$tmp"
  fi
  rm -f "$bodyf"
  # A rewrite that lost the authored prose (or everything) is never written out.
  if [ ! -s "$tmp" ]; then
    rm -f "$tmp"; echo "template-api-docs-gen: rewrite of ${page#"$TMPL"/} came out EMPTY — refused" >&2; return 2
  fi
  if ! grep -q '<!-- api-docs:end -->' "$tmp"; then
    rm -f "$tmp"; echo "template-api-docs-gen: rewrite of ${page#"$TMPL"/} lost its end marker — refused" >&2; return 2
  fi
  if cmp -s "$tmp" "$page"; then rm -f "$tmp"; echo "  = ${page#"$TMPL"/} (current)"; return 0; fi
  mv "$tmp" "$page"
  echo "  ✎ ${page#"$TMPL"/}"
}


build_callsite_index
trap 'rm -f "$CALLSITE_INDEX"' EXIT

DO_WRITE=0
for a in "$@"; do [ "$a" = "--write" ] && DO_WRITE=1; done

if [ "$WANT_SIDEBAR" -eq 1 ]; then
  :   # sidebar-only run; handled at the end, after write_sidebar is defined
elif [ "$TARGET" = "--all" ]; then
  # feature/ is IN SCOPE. The generator-facing corpus teaches how to BUILD a feature, and the 17
  # feature modules here are the only worked examples of that in the repo — 254 .kt files that
  # compile, against 11 Kotlin fences in FEATURE_LAYER.md. Leaving them undocumented meant 0 of 17
  # feature pages carried an API block while every core*/ page did.
  # --write runs the modules in PARALLEL: each page is independent (its own temp files; the call-site
  # index is built above and only read), so the pages are byte-identical to a sequential run — only
  # the order of the "✎ page" log lines can vary. Batches of DOCS_JOBS (default: the core count;
  # DOCS_JOBS=1 restores sequential). A failed module still fails the run. stdout mode stays
  # sequential, because there the ORDER is the output.
  jobs_max="${DOCS_JOBS:-$(getconf _NPROCESSORS_ONLN 2>/dev/null || echo 4)}"
  pids=(); fail=0
  for d in "$TMPL"/core-base/*/ "$TMPL"/core/*/ "$TMPL"/feature/*/; do
    [ -d "$d/src" ] || continue
    p="${d%/}"; lm="${p#"$TMPL"/}"
    if [ "$DO_WRITE" -eq 1 ]; then
      write_module "$lm" & pids+=("$!")
      if [ "${#pids[@]}" -ge "$jobs_max" ]; then
        for pid in "${pids[@]}"; do wait "$pid" || fail=1; done; pids=()
      fi
    else
      emit_module "$lm"; echo
    fi
  done
  for pid in "${pids[@]+"${pids[@]}"}"; do wait "$pid" || fail=1; done
  [ "$fail" -eq 0 ] || { echo "template-api-docs-gen: a module failed to generate — see above" >&2; exit 2; }
else
  if [ "$DO_WRITE" -eq 1 ]; then write_module "$TARGET"; else emit_module "$TARGET"; fi
fi

# ── --sidebar: regenerate docs/_sidebar.md FROM THE TREE ──────────────────────────────────────────
# Generated, not authored: a hand-kept sidebar silently omits a page the moment one is added, and an
# omitted page is an undiscoverable page. Sections are ordered so the architecture SoT leads.
write_sidebar() {
  local out="$TMPL/docs/_sidebar.md" tmp d f title
  tmp="$(mktemp -t sidebar.XXXXXX)"
  {
    printf '<!-- GENERATED by scripts/docs/api-docs-gen.sh --sidebar — do not hand-edit. -->\n\n'
    printf -- '- **Architecture**\n'
    [ -f "$TMPL/docs/architecture/ARCHITECTURE.md" ] && printf -- '  - [Overview](/architecture/ARCHITECTURE.md)\n'
    for d in cross-cutting patterns; do
      if [ -d "$TMPL/docs/architecture/$d" ]; then
        while IFS= read -r f; do
          [ -z "$f" ] && continue
          title="$(basename "$f" .md)"
          printf -- '  - [%s](/architecture/%s/%s)\n' "$title" "$d" "$(basename "$f")"
        done < <(find "$TMPL/docs/architecture/$d" -maxdepth 1 -name '*.md' 2>/dev/null | LC_ALL=C sort)
      fi
    done
    # Project tree FIRST among the sections: it is the map, and a reader who does not yet know the
    # repo needs the shape before the per-module API reference.
    if [ -f "$TMPL/docs/architecture/PROJECT_TREE.md" ]; then
      printf -- '\n- **Project tree**\n'
      printf -- '  - [All areas](/architecture/PROJECT_TREE.md)\n'
      while IFS= read -r f; do
        [ -z "$f" ] && continue
        printf -- '  - [%s/](/architecture/tree/%s)\n' "$(basename "$f" .md)" "$(basename "$f")"
      done < <(find "$TMPL/docs/architecture/tree" -maxdepth 1 -name '*.md' 2>/dev/null | LC_ALL=C sort)
    fi
    for layer in core-base core; do
      if [ -d "$TMPL/docs/architecture/tree/$layer" ]; then
        printf -- '\n- **`%s/` API reference**\n' "$layer"
        while IFS= read -r f; do
          [ -z "$f" ] && continue
          printf -- '  - [%s/%s](/architecture/tree/%s/%s)\n' "$layer" "$(basename "$f" .md)" "$layer" "$(basename "$f")"
        done < <(find "$TMPL/docs/architecture/tree/$layer" -maxdepth 1 -name '*.md' 2>/dev/null | LC_ALL=C sort)
      fi
    done
    for d in setup deployment release secrets ios claude reports; do
      if [ -d "$TMPL/docs/$d" ]; then
        local any=0
        while IFS= read -r f; do
          [ -z "$f" ] && continue
          [ "$any" -eq 0 ] && { printf -- '\n- **%s**\n' "$d"; any=1; }
          printf -- '  - [%s](/%s/%s)\n' "$(basename "$f" .md)" "$d" "$(basename "$f")"
        done < <(find "$TMPL/docs/$d" -maxdepth 1 -name '*.md' 2>/dev/null | LC_ALL=C sort)
      fi
    done
    local any_root=0
    while IFS= read -r f; do
      [ -z "$f" ] && continue
      [ "$any_root" -eq 0 ] && { printf -- '\n- **Reference**\n'; any_root=1; }
      printf -- '  - [%s](/%s)\n' "$(basename "$f" .md)" "$(basename "$f")"
    done < <(find "$TMPL/docs" -maxdepth 1 -name '*.md' ! -name '_*' 2>/dev/null | LC_ALL=C sort)
  } > "$tmp"
  [ -s "$tmp" ] || { rm -f "$tmp"; echo "sidebar generation produced nothing — refused" >&2; return 2; }
  if cmp -s "$tmp" "$out" 2>/dev/null; then rm -f "$tmp"; echo "  = docs/_sidebar.md (current)"; return 0; fi
  mv "$tmp" "$out"; echo "  ✎ docs/_sidebar.md"
  # .nojekyll stops GitHub Pages discarding files whose names begin with an underscore.
  [ -f "$TMPL/docs/.nojekyll" ] || { : > "$TMPL/docs/.nojekyll"; echo "  ✎ docs/.nojekyll"; }
}

[ "$WANT_SIDEBAR" -eq 1 ] && { write_sidebar; exit $?; }
exit 0
