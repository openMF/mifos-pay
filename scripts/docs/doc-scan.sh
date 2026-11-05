#!/usr/bin/env bash
# scripts/docs/doc-scan.sh — one full-project documentation scan, every language.
#
# WHAT THIS IS FOR
# detekt answers "is there a comment on this Kotlin symbol?" for the 46 Gradle projects it is wired
# into. That leaves three blind spots, all measured:
#   · ABSTRACT MEMBERS — an interface's `val x: Color` is invisible to UndocumentedPublicProperty.
#     core-base/designsystem alone has hundreds.
#   · build-logic/convention — a separate Gradle build detekt never scans (36 files).
#   · EVERY NON-KOTLIN SURFACE — 97 shell scripts, 39 ruby/fastlane files, 13 workflows, 25 config
#     schemas. Each already has a doc convention this tree follows; nothing was checking it.
#
# So this scans the SOURCE TREE (not a diff — a diff-scoped audit reports zero on a clean checkout and
# a gate built that way passes trivially) and reports one row per public symbol.
#
# Output: TSV on stdout — path, line, visibility, symbol_kind, name, doc_state, doc_words
#   doc_state: kdoc | property-tag | doc | tag-only | none
#
# Usage:
#   scripts/docs/doc-scan.sh                  # every surface
#   scripts/docs/doc-scan.sh --surface kotlin # one surface
#   scripts/docs/doc-scan.sh --summary        # the per-surface table instead of rows
#   scripts/docs/doc-scan.sh --gaps           # only the undocumented rows
# Env: TEMPLATE_PATH
# A CRASHED SCANNER MUST NOT LOOK LIKE A CLEAN TREE. When shell.awk had a syntax error, `--gaps`
# printed "0 gaps" — indistinguishable from fully documented, and a gate built on that passes while
# measuring nothing. Every scanner invocation is checked, and any failure aborts with a message.
set -uo pipefail

# Byte-deterministic text processing. Generated output must be a pure function of the tree, and the
# locale silently breaks that in two ways: `sort` collates differently (a UTF-8 locale folds case, so
# `di/SecurityModule.kt` sorts before `FailedAttemptTracker.kt` while C order puts it after), and `.`
# in a length-bounded regex counts CHARACTERS under UTF-8 but BYTES under C — so a 241-character KDoc
# summary containing an em dash (243 bytes) kept its second sentence on a Mac and lost it on the
# Linux CI runner. Pinning to C fixes the comparison basis everywhere; no script here runs Python, so
# this cannot force a Python stdout to ASCII.
export LC_ALL=C
FAILED=0
run() {                       # run <scanner-cmd…> — abort the whole scan if it errors
  if ! "$@"; then
    echo "doc-scan: SCANNER FAILED: $*" >&2
    FAILED=1
  fi
}
ROOT="${TEMPLATE_PATH:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)}"
cd "$ROOT" || exit 2
S="scripts/docs/scanners"

WANT=""; MODE=rows
while [ $# -gt 0 ]; do
  case "$1" in
    --surface) WANT="${2:-}"; shift 2 ;;
    --summary) MODE=summary; shift ;;
    --gaps)    MODE=gaps; shift ;;
    *) shift ;;
  esac
done
want() { [ -z "$WANT" ] || [ "$WANT" = "$1" ]; }

# Canary fixtures are TEST DATA, not production code — 140 .kt under scripts/product-health/tests are
# deliberately malformed, and asking them for docs would be asking a RED fixture to stop being red.
FIXTURES='^scripts/product-health/tests/|^scripts/docs/tests/|^spotless/'

# The tree AS THE NEXT COMMIT WILL HOLD IT: tracked + untracked-not-ignored files that exist on disk.
# Plain `git ls-files` lists the INDEX, so a refresh run before committing a new file (or after
# deleting one) wrote pages that went stale the moment the commit landed, and CI then failed the
# docs check on a tree that had been "refreshed". On a clean checkout this is exactly `git ls-files`.
tree_files() {
  git ls-files -co --exclude-standard -- "$@" | while IFS= read -r f; do [ -e "$f" ] && printf '%s\n' "$f"; done \
    | LC_ALL=C sort -u
}

scan_all() {
  if want kotlin; then
    tree_files '*.kt' | grep -vE "$FIXTURES" | xargs -n 200 awk -f "$S/kotlin.awk" || FAILED=1
  fi
  if want shell; then
    tree_files '*.sh' | grep -vE "$FIXTURES" | while IFS= read -r f; do awk -f "$S/shell.awk" "$f"; done
  fi
  if want ruby; then
    tree_files | grep -E '\.rb$|/Fastfile$|/Appfile$' | grep -vE "$FIXTURES" \
      | while IFS= read -r f; do awk -f "$S/ruby.awk" "$f" || exit 1; done || FAILED=1
  fi
  if want workflow; then
    # shellcheck disable=SC2046
    bash "$S/workflow.sh" $(tree_files '.github/workflows/*.yml' '.github/workflows/*.yaml') || FAILED=1
  fi
  if want config; then
    tree_files 'app-profile/*.yaml' 'deployment/DEPLOYMENT_MANIFEST.yaml' \
                 'core/store/STORE_ARCHETYPES.yaml' 'customization-surface.yaml' 2>/dev/null \
      | while IFS= read -r f; do awk -f "$S/yamlcfg.awk" "$f" || exit 1; done || FAILED=1
  fi
}

case "$MODE" in
  rows) scan_all ;;
  gaps) scan_all | awk -F'\t' '$3=="public" && $6=="none"' ;;
  summary)
    # POSIX awk only: no `asorti` (gawk), no multi-line ternary chains (BWK awk rejects them). Sorting
    # happens in `sort`, not in awk — the third portability trap in this pipeline after `\S` and `\b`.
    scan_all | awk -F'\t' '
      $3 != "public" { next }
      {
        s = "config-schema"
        if ($1 ~ /\.kt$/) s = "kotlin"
        else if ($1 ~ /\.sh$/) s = "shell"
        else if ($1 ~ /\.rb$/ || $1 ~ /\/Fastfile$/ || $1 ~ /\/Appfile$/) s = "ruby/fastlane"
        else if ($1 ~ /^\.github\//) s = "workflow"
        d = ($6 == "none") ? 0 : 1
        printf "%s\t%s\t%d\n", s, $4, d
      }' \
    | LC_ALL=C sort | awk -F'\t' '
      { k = $1 "\t" $2; tot[k]++; doc[k] += $3; T++; D += $3; if (!(k in seen)) { seen[k]=1; ord[++n]=k } }
      END {
        printf "%-14s %-13s %7s %7s %7s %6s\n", "SURFACE","SYMBOL","total","docd","missing","pct"
        print  "─────────────────────────────────────────────────────────────────"
        for (i = 1; i <= n; i++) {
          k = ord[i]; split(k, p, "\t"); t = tot[k]; d = doc[k] + 0
          printf "%-14s %-13s %7d %7d %7d %5d%%\n", p[1], p[2], t, d, t-d, int(100*d/t+0.5)
        }
        print  "─────────────────────────────────────────────────────────────────"
        printf "%-28s %7d %7d %7d %5d%%\n", "TOTAL", T, D, T-D, int(100*D/T+0.5)
      }'
    ;;
esac

[ "$FAILED" -eq 0 ] || { echo "doc-scan: one or more scanners failed — the counts above are NOT complete" >&2; exit 3; }
