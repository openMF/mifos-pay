#!/usr/bin/env bash
# kotlin-conform.sh [--check] <files…> — make Kotlin and XML files Spotless-formatted and Detekt-clean,
# in seconds and without Gradle. The ONE implementation of the style rules outside Gradle: the git hooks
# (kotlin-checks.sh) and the framework's write-time hook both call it, so code is held to the same
# rules when it is written, committed and pushed — and CI's `spotlessCheck detekt` can be reproduced in
# seconds (kotlin-checks.sh changed | module <dir> | all).
#
# Scope mirrors Gradle exactly — a file is checked only if Spotless/Detekt would check it:
#   • a module file counts when its module's build.gradle.kts applies a convention that applies Spotless
#     (cmp.feature · kmp.library · kmp.core.base.library · android.application · spotless.plugin);
#     tools/*-ksp (plain kotlin("jvm")) does not, so it is skipped, as Gradle skips it
#   • the root project's own targets: build-logic/**/*.kt(s), root *.kts, app-profile/icons/**/*.xml
#   • build output and the repo's tests/ fixtures are never checked
#
# For each in-scope file (paths relative to the repo root):
#   1. licence header (.kt/.kts) — added when missing, rendered as build-logic LicenseHeader.kt does
#      (spotless/license-header.txt; $ORG_NAME ← app-profile/app.yaml#org.name; $YEAR ← this year)
#   2. licence header (.xml) — Spotless's format("xml"): everything before the first real tag (not a
#      comment, not the <?xml?> declaration) IS the header — `<?xml …?>` + a `<!-- … -->` block from the
#      same source. A file whose prefix differs is rewritten (or reported, with --check). The year
#      already in the file is kept, as Spotless keeps it; a file already carrying the template's
#      copyright (LicenseHeader.TEMPLATE_HOLDER) is skipped, as Spotless's NOT_TEMPLATE_CODE skips it.
#   3. ktlint at the version Spotless pins (Spotless.kt KTLINT_VERSION), IntelliJ IDEA code style;
#      build-logic keeps Spotless's override (no-unused-imports off: it cannot see Gradle's `assign`)
#   4. Detekt — the repo's config/detekt/detekt.yml + the Compose rules plugin, .kt only, build-logic
#      excluded as in Detekt.kt
# --check reports instead of applying (pre-push, CI parity). Exit 0 clean · 1 findings (stderr).
#
# Why not Gradle: a module's spotlessApply costs ~15 s of configuration, the root project's ~3 min, a
# whole-repo `spotlessCheck detekt dependencyGuard` ~13 min and `detekt` alone up to 25 min. Here the
# tools are the official release jars (cached once in ~/.cache/kpt-tools, KPT_TOOLS_DIR) and every
# file goes through ONE ktlint and ONE detekt process: measured 4 s for a 30-file PR and 34 s for a
# 2,399-file repo.
set -uo pipefail
CHECK=0; [ "${1:-}" = --check ] && { CHECK=1; shift; }
cd "$(git rev-parse --show-toplevel)" || exit 1
ROOT="$PWD"

# spotless_module <dir> — 0 when <dir>/build.gradle.kts applies a convention that applies Spotless + Detekt.
SPOTLESS_MARK='libs\.plugins\.(cmp\.feature|kmp\.library|kmp\.core\.base\.library|android\.application|spotless)\.convention([^.a-z]|$)|"org\.convention\.(cmp\.feature|kmp\.library|kmp\.core\.base\.library|android\.application|spotless\.plugin)"'
# Answers cached per module as newline-delimited lists (no `declare -A`: macOS ships bash 3.2).
SCOPE_YES=$'\n'; SCOPE_NO=$'\n'
spotless_module() {
  local d="$1"
  case "$SCOPE_YES" in *$'\n'"$d"$'\n'*) return 0 ;; esac
  case "$SCOPE_NO" in *$'\n'"$d"$'\n'*) return 1 ;; esac
  if grep -qE "$SPOTLESS_MARK" "$d/build.gradle.kts" 2>/dev/null; then SCOPE_YES+="$d"$'\n'; return 0; fi
  SCOPE_NO+="$d"$'\n'; return 1
}
# in_scope <file> — does Gradle's Spotless (and so this script) check <file>?
in_scope() {
  local f="$1" d
  case "$f" in
    build-logic/*) case "$f" in *.kt|*.kts) return 0 ;; *) return 1 ;; esac ;;   # root project targets
    app-profile/icons/*.xml|app-profile/icons/*/*) case "$f" in *.xml) return 0 ;; esac; return 1 ;;
    */*) ;;
    *.kts) return 0 ;;                                                                 # root *.kts
    *) return 1 ;;
  esac
  d="$(dirname "$f")"
  while [ "$d" != . ] && [ ! -f "$d/build.gradle.kts" ]; do d="$(dirname "$d")"; done
  [ "$d" != . ] && spotless_module "$d"
}

files=(); for f in "$@"; do
  f="${f#"$ROOT"/}"
  case "$f" in *.kt|*.kts|*.xml) ;; *) continue ;; esac
  case "$f" in */build/*|build/*|*/tests/*|tests/*) continue ;; esac
  [ -f "$f" ] && in_scope "$f" && files+=("$f")
done
[ "${#files[@]}" -gt 0 ] || exit 0

rc=0
hdr=spotless/license-header.txt
org="$(awk '/^[^[:space:]]/{o=($0 ~ /^org:/)} o && /^  name:/{sub(/^  name:[[:space:]]*/,""); sub(/[[:space:]]*#.*/,""); gsub(/^["\x27]|["\x27]$/,""); print; exit}' app-profile/app.yaml 2>/dev/null)"
year="$(date +%Y)"
# header_lines — the header source with trailing blank lines dropped and $ORG_NAME resolved ($YEAR kept).
header_lines() { sed -e :a -e '/^\n*$/{$d;N;ba' -e '}' "$hdr" | while IFS= read -r l; do echo "${l//\$ORG_NAME/${org:-the project authors}}"; done; }

kt_files=(); xml_files=()
for f in "${files[@]}"; do case "$f" in *.xml) xml_files+=("$f") ;; *) kt_files+=("$f") ;; esac; done

# 1. .kt/.kts licence header (a fix, so not in --check)
if [ "$CHECK" = 0 ] && [ -s "$hdr" ]; then
  for f in "${kt_files[@]+"${kt_files[@]}"}"; do
    head -1 "$f" | grep -q '^/\*' && continue
    { echo "/*"
      header_lines | while IFS= read -r l; do
        l="${l//\$YEAR/$year}"
        if [ -z "${l// /}" ]; then echo " *"; else echo " * $l"; fi
      done
      echo " */"; cat "$f"; } > "$f.kc" && mv "$f.kc" "$f"
  done
fi

# 2. .xml licence header — Spotless format("xml") with delimiter `<[^!?]`
if [ "${#xml_files[@]}" -gt 0 ] && [ -s "$hdr" ]; then
  xml_hdr="$(mktemp)"; header_lines > "$xml_hdr"; want="$(mktemp)"
  # Spotless's onlyIfContentMatches(LicenseHeader.NOT_TEMPLATE_CODE): a file that already carries the
  # TEMPLATE's copyright is template code and its header is left alone — only other files are rewritten.
  holder="$(sed -n 's/.*const val TEMPLATE_HOLDER = "\(.*\)".*/\1/p' build-logic/convention/src/main/kotlin/org/convention/LicenseHeader.kt)"
  bad=()
  for f in "${xml_files[@]}"; do
    [ -n "$holder" ] && grep -qE "Copyright [0-9]{4} $holder" "$f" && continue
    # Writes the file's correct form: the rendered header (keeping the year already in the file), a
    # newline, then everything from the first real tag. Exit 3 when the file has no real tag at all.
    # awk rebuilds the text line by line, so it would add a final newline a file lacks; Spotless's header
    # step never touches the end of a file, so the original EOF is kept (eol=0 → drop the last "\n").
    eol=1; [ -n "$(tail -c1 "$f")" ] && eol=0
    awk -v hfile="$xml_hdr" -v year="$year" -v eol="$eol" '
      BEGIN { while ((getline l < hfile) > 0) h[++n] = l }
      { s = s $0 "\n" }
      END {
        p = match(s, /<[^!?]/); if (!p) exit 3
        pre = substr(s, 1, p - 1)
        if (match(pre, /Copyright [0-9][0-9][0-9][0-9]/)) year = substr(pre, RSTART + 10, 4)
        out = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<!--\n"
        for (i = 1; i <= n; i++) { l = h[i]; gsub(/\$YEAR/, year, l); out = out ((l ~ /^[ \t]*$/) ? "" : "    " l) "\n" }
        body = substr(s, p); if (eol == 0) body = substr(body, 1, length(body) - 1)
        printf "%s-->\n%s", out, body
      }' "$f" > "$want" || continue
    cmp -s "$want" "$f" && continue
    if [ "$CHECK" = 1 ]; then bad+=("$f"); else cp "$want" "$f"; fi
  done
  rm -f "$xml_hdr" "$want"
  [ "${#bad[@]}" -gt 0 ] && { printf 'xml licence header (Spotless) — run without --check to fix:\n'; printf '  %s\n' ${bad[@]+"${bad[@]}"}; } >&2 && rc=1
fi

[ "${#kt_files[@]}" -gt 0 ] || exit "$rc"

TOOLS="${KPT_TOOLS_DIR:-$HOME/.cache/kpt-tools}"; mkdir -p "$TOOLS"
kv="$(sed -n 's/^const val KTLINT_VERSION = "\(.*\)"/\1/p' build-logic/convention/src/main/kotlin/org/convention/Spotless.kt)"
dv="$(sed -n 's/^detekt = "\(.*\)"/\1/p' gradle/libs.versions.toml)"
cv="$(sed -n 's/^twitter-detekt-compose = "\(.*\)"/\1/p' gradle/libs.versions.toml)"
KTLINT="$TOOLS/ktlint-$kv"; DETEKT="$TOOLS/detekt-cli-$dv-all.jar"; COMPOSE="$TOOLS/compose-rules-$cv"
# fetch <url> <dest> — download once into the tool cache (a partial download never counts as cached).
fetch() { [ -s "$2" ] || { curl -fsSL -o "$2.part" "$1" && mv "$2.part" "$2"; } || { rm -f "$2.part"; echo "kotlin-conform: could not download $1" >&2; return 1; }; }
fetch "https://github.com/pinterest/ktlint/releases/download/$kv/ktlint" "$KTLINT" && chmod +x "$KTLINT" || exit 1
fetch "https://github.com/detekt/detekt/releases/download/v$dv/detekt-cli-$dv-all.jar" "$DETEKT" || exit 1
mkdir -p "$COMPOSE"
for a in detekt core-detekt core-common common; do
  fetch "https://repo1.maven.org/maven2/com/twitter/compose/rules/$a/$cv/$a-$cv.jar" "$COMPOSE/$a-$cv.jar" || exit 1
done

# 3. ktlint — build-logic with Spotless's override, the rest with the plain style
ktlint_run() { # <editorconfig-or-empty> <files…>
  local ec="$1"; shift; [ "$#" -gt 0 ] || return 0
  local args=(--code-style=intellij_idea --relative); [ "$CHECK" = 0 ] && args+=(-F); [ -n "$ec" ] && args+=(--editorconfig="$ec")
  "$KTLINT" "${args[@]}" "$@" 2>/dev/null | grep -E '^[^ ].*:[0-9]+:[0-9]+:'
}
bl=(); rest=(); for f in "${kt_files[@]}"; do case "$f" in build-logic/*) bl+=("$f") ;; *) rest+=("$f") ;; esac; done
ec="$(mktemp)"; printf '[*.{kt,kts}]\nktlint_standard_no-unused-imports = disabled\n' > "$ec"
out="$(ktlint_run "" "${rest[@]+"${rest[@]}"}"; ktlint_run "$ec" "${bl[@]+"${bl[@]}"}")"; rm -f "$ec"
[ -n "$out" ] && { printf 'ktlint (Spotless)%s:\n%s\n' "$([ "$CHECK" = 0 ] && echo ' — not auto-fixable')" "$out" >&2; rc=1; }

# 4. Detekt
kt=(); for f in "${kt_files[@]}"; do case "$f" in *.kts|build-logic/*) ;; *) kt+=("$f") ;; esac; done
if [ "${#kt[@]}" -gt 0 ]; then
  out="$(java -jar "$DETEKT" --input "$(IFS=,; echo "${kt[*]}")" --config config/detekt/detekt.yml \
         --plugins "$(ls "$COMPOSE"/*.jar | paste -sd, -)" 2>&1 | grep -E '^/.*:[0-9]+:[0-9]+: ' | sed "s|^$ROOT/||")"
  [ -n "$out" ] && { printf 'Detekt:\n%s\n' "$out" >&2; rc=1; }
fi
exit "$rc"
