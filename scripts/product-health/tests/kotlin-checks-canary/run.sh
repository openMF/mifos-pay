#!/usr/bin/env bash
# kotlin-checks-canary — the git hooks and the on-demand CI-parity modes check exactly the files Gradle's
# Spotless/Detekt would, with the same XML licence-header rule. (The ktlint/Detekt tools themselves are
# pinned by the framework's kotlin-write-conform-canary; this pins the selection and the XML rule, so it
# needs no tool download and runs in well under a second.)
#
#   K-3 kotlin_files keeps existing .kt/.kts/.xml only, dropping build output and */tests/ fixtures
#   K-4 scope mirrors Gradle: a module whose build.gradle.kts applies no Spotless convention (tools/*-ksp)
#       is skipped; a module applying one is checked
#   K-5 the 2026-11-05 CI failure shape — an XML file headed by a non-copyright comment — is reported by
#       --check and rewritten by a fix to exactly Spotless's header + the content from the first real tag
#   K-6 a file already carrying the TEMPLATE's copyright is left alone (Spotless NOT_TEMPLATE_CODE)
#   K-7 a correct header keeps the year already in the file (Spotless keeps it) — no churn
#   K-8 `kotlin-checks.sh module <dir>` / `changed <base>` select those files and fail on the finding
set -uo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "$HERE/../../../.." && pwd)"
SB="$(mktemp -d)"; trap 'rm -rf "$SB"' EXIT
rc=0; ok() { echo "   ✅ $1"; }; bad() { echo "   ❌ $1"; rc=1; }

mkdir -p "$SB/core/ui/src/x" "$SB/feature/a/b/src" "$SB/build-logic/convention/src" "$SB/core/ui/build/gen" "$SB/scripts/tests"
touch "$SB/build.gradle.kts" "$SB/core/ui/build.gradle.kts" "$SB/feature/a/b/build.gradle.kts" \
      "$SB/core/ui/src/x/A.kt" "$SB/feature/a/b/src/B.kt" "$SB/build-logic/convention/src/C.kt" \
      "$SB/core/ui/build/gen/G.kt" "$SB/scripts/tests/F.kt" "$SB/core/ui/src/x/notes.md" "$SB/core/ui/src/x/s.xml"
fns="$(awk '/^(kotlin_files)\(\) \{/{p=1} p{print} p&&/^}/{p=0}' "$ROOT/scripts/ci/kotlin-checks.sh")"
( cd "$SB" || exit 1
  eval "$fns"
  got="$(printf '%s\n' core/ui/src/x/A.kt core/ui/build/gen/G.kt scripts/tests/F.kt core/ui/src/x/notes.md core/ui/src/x/s.xml gone/Missing.kt build.gradle.kts | kotlin_files | tr '\n' ' ')"
  [ "$got" = "core/ui/src/x/A.kt core/ui/src/x/s.xml build.gradle.kts " ] ) \
  && ok "K-3 only existing .kt/.kts/.xml, no build output or fixtures" || bad "K-3 kotlin_files selection"

# A minimal template-lineage repo: header source, org name, the template-holder constant, two modules.
R="$SB/repo"; mkdir -p "$R/scripts/ci" "$R/spotless" "$R/app-profile" "$R/build-logic/convention/src/main/kotlin/org/convention" \
  "$R/feature/a/src/commonMain/composeResources/values" "$R/tools/x-ksp/src/main/resources"
cp "$ROOT/scripts/ci/kotlin-conform.sh" "$ROOT/scripts/ci/kotlin-checks.sh" "$R/scripts/ci/"
printf 'Copyright $YEAR $ORG_NAME\n\nLine two.\n' > "$R/spotless/license-header.txt"
printf 'org:\n  name: Acme Labs\n' > "$R/app-profile/app.yaml"
printf 'object LicenseHeader {\n    const val TEMPLATE_HOLDER = "Template Org"\n}\n' \
  > "$R/build-logic/convention/src/main/kotlin/org/convention/LicenseHeader.kt"
printf 'plugins {\n    alias(libs.plugins.cmp.feature.convention)\n}\n' > "$R/feature/a/build.gradle.kts"
printf 'plugins {\n    kotlin("jvm")\n}\n' > "$R/tools/x-ksp/build.gradle.kts"
y="$(date +%Y)"
want_hdr() { printf '<?xml version="1.0" encoding="utf-8"?>\n<!--\n    Copyright %s Acme Labs\n\n    Line two.\n-->\n' "$1"; }
seeded='<?xml version="1.0" encoding="utf-8"?>\n<!-- locale: ar · seeded by /idea-locale · values carry SOURCE text until translated -->\n<resources>\n    <string name="a">b</string>\n</resources>\n'
S="$R/feature/a/src/commonMain/composeResources/values/strings.xml"
T="$R/tools/x-ksp/src/main/resources/strings.xml"
printf "$seeded" > "$S"; printf "$seeded" > "$T"
( cd "$R" && git init -q && git add -A && git -c user.email=c@c -c user.name=c commit -qm base && git branch -q base ) || { bad "sandbox git"; exit 1; }
conform() { ( cd "$R" && bash scripts/ci/kotlin-conform.sh "$@" ); }

out="$(conform --check "$S" "$T" 2>&1)"; r=$?
[ "$r" = 1 ] && grep -q "feature/a/src/commonMain/composeResources/values/strings.xml" <<<"$out" \
  && ok "K-5 --check reports the seeded XML file (the CI failure shape)" || bad "K-5 check rc=$r out='$out'"
grep -q "tools/x-ksp" <<<"$out" && bad "K-4 tools/x-ksp (no Spotless convention) was checked" \
  || ok "K-4 a module without a Spotless convention is out of scope"

conform "$S" >/dev/null 2>&1
expect="$(want_hdr "$y"; printf '<resources>\n    <string name="a">b</string>\n</resources>\n')"
[ "$(cat "$S")" = "$expect" ] && ok "K-5 the fix writes Spotless's header + the content from the first real tag" \
  || bad "K-5 fixed file differs: $(diff <(echo "$expect") "$S" | head -5 | tr '\n' ' ')"
conform --check "$S" >/dev/null 2>&1 && ok "K-5 the fixed file then passes --check" || bad "K-5 fixed file still flagged"

{ want_hdr 2019; printf '<resources/>\n'; } > "$S"
conform --check "$S" >/dev/null 2>&1 && ok "K-7 a correct header keeps its own year (2019), no churn" || bad "K-7 year 2019 rewritten"

{ want_hdr 2019; printf '<resources/>'; } > "$S"
conform --check "$S" >/dev/null 2>&1 && ok "K-7 a correct header on a file with no final newline is not flagged" \
  || bad "K-7 a missing final newline was treated as a header difference"

printf '<?xml version="1.0" encoding="utf-8"?>\n<!-- Copyright 2024 Template Org -->\n<resources/>\n' > "$S"
conform --check "$S" >/dev/null 2>&1 && ok "K-6 a file carrying the template's copyright is skipped" || bad "K-6 template file flagged"

printf "$seeded" > "$S"
out="$( cd "$R" && bash scripts/ci/kotlin-checks.sh module feature/a 2>&1 )"; r=$?
[ "$r" = 1 ] && grep -q "values/strings.xml" <<<"$out" && ok "K-8 module <dir> selects and fails on the finding" || bad "K-8 module rc=$r"
# The branch must CHANGE the file vs base for `changed` to select it: add a string, keep the bad header.
printf "$seeded" | sed 's|</resources>|    <string name="c">d</string>\n</resources>|' > "$S"
( cd "$R" && git -c user.email=c@c -c user.name=c commit -qam "branch edit" )
out="$( cd "$R" && bash scripts/ci/kotlin-checks.sh changed base 2>&1 )"; r=$?
[ "$r" = 1 ] && grep -q "values/strings.xml" <<<"$out" && ok "K-8 changed <base> selects the branch's files and fails" || bad "K-8 changed rc=$r out='$out'"
exit "$rc"
