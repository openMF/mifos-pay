#!/usr/bin/env bash
# checks/shell-portability.sh — tracked shell scripts must run on the RUNNERS, not just on a Mac.
#
# The trap this guards (hit 2026-09-09): scripts/remove-demo.sh used `sed -i ''`, which is BSD/macOS
# syntax. GNU sed reads that '' as the SCRIPT argument and exits non-zero, so the ENTIRE demo strip
# died on every Linux runner. Two product-health checks failed as a result — demo-strip-coherence
# ("a cleaned fork cannot even be produced") and demo-strip-codegen-canary — and neither pointed at
# the real cause, because the check swallows the strip's output.
#
# It survived for as long as the script existed: the quality-gate job short-circuited before
# product-health ever ran, so nothing on Linux had executed it. A macOS developer cannot reproduce
# it, and CI could not report it. That combination is exactly what a static gate is for.
#
#   SP-1  no `sed -i ''` / `sed -i ""` — BSD-only. Use `sed -i.bak … && rm -f <file>.bak`, which
#         both seds accept. (`sed -i` with NO suffix is the inverse trap: GNU-only, BSD reads the
#         next argument as the suffix — so it is rejected too.)
#   SP-2  no bare `readlink -f` / `stat -f` / `md5` without a GNU fallback on the same line.
#         These are the other three BSD/GNU splits this repo's scripts actually reach for.
#   SP-3  no comment line spliced into a `\` line continuation. A trailing backslash joins the next
#         line onto the command, so a `# …` line there ENDS the command: every argument below it is
#         silently dropped, and the first orphaned line runs as a command of its own. Put the
#         explanation ABOVE the command.
#   SP-5  no bash-4-only builtin (`declare -A`, `mapfile`, `readarray`, `${x,,}`/`${x^^}`) unless the
#         file opts out with `# bash4-required: <reason>`. macOS ships bash 3.2 as /bin/bash, and a
#         `#!/bin/bash` shebang pins a script to it outright — no PATH escape.
#   SP-4  no bare `"${arr[@]}"` expansion of an array that is initialised EMPTY, unless a count
#         guard (`if … ${#arr[@]} -gt/-ne 0`) stands within the three lines above it. On bash 3.2 —
#         still `/bin/bash` on macOS — expanding an empty array under `set -u` raises
#         "unbound variable" and kills the script. Use `${arr[@]+"${arr[@]}"}`, which keeps the
#         inner quoting so multi-word entries survive.
#
# Scope: TRACKED *.sh only (git ls-files), comment lines stripped (a commented-out form never
# runs). Two exclusions: product-health/tests/** (canary fixtures deliberately contain the
# broken forms so the RED leg has something to detect) and THIS file, which cannot describe
# the trap without spelling it in both its header and the error text it prints.
#
# exit 0 = PASS · 1 = FAIL (blocks).
set -uo pipefail
# shellcheck source=scripts/product-health/lib.sh
source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib.sh"
: "${HEALTH_ROOT:?shell-portability: HEALTH_ROOT not set (run via product-health.sh)}"
cd "$HEALTH_ROOT" || exit 2

fail=0

scripts="$(git ls-files -- '*.sh' 2>/dev/null \
             | grep -v '^scripts/product-health/tests/' \
             | grep -v '^scripts/product-health/checks/shell-portability.sh$' || true)"

# Comment-stripped view of a file: a commented-out `sed -i ''` never runs, so matching it would
# report prose as a defect. Blank the comment lines rather than deleting them, so grep -n still
# reports the true line number of a real hit.
uncommented() { sed 's/^[[:space:]]*#.*$//' "$1"; }
[ -z "$scripts" ] && { echo "no tracked shell scripts (ok)"; exit 0; }

# ── SP-1 — the in-place-edit split ─────────────────────────────────────────────────────────────
# BSD requires a suffix argument; GNU treats a separate '' as the script. `-i.bak` is the only
# spelling both accept, so it is the one this repo uses.
bsd_inplace="$(printf '%s\n' "$scripts" | while IFS= read -r f; do
  uncommented "$f" | grep -nE "sed[[:space:]]+(-[a-zA-Z]+[[:space:]]+)*-i[[:space:]]+(''|\"\")" 2>/dev/null \
    | sed "s|^|${f}:|"
done)"
if [ -n "$bsd_inplace" ]; then
  echo "${C_RED}✗ SP-1${C_RST}: BSD-only \`sed -i ''\` — GNU sed reads the '' as its script and exits 1,"
  echo "       so this line silently kills the whole script on every Linux runner:"
  printf '%s\n' "$bsd_inplace" | sed 's/^/       /'
  echo "       Use:  sed -i.bak … \"\$f\"  &&  rm -f \"\$f.bak\""
  fail=1
fi

gnu_inplace="$(printf '%s\n' "$scripts" | while IFS= read -r f; do
  uncommented "$f" | grep -nE "sed[[:space:]]+(-[a-zA-Z]+[[:space:]]+)*-i[[:space:]]+[\"']?(s|/|-e|-E)" 2>/dev/null \
    | sed "s|^|${f}:|"
done)"
if [ -n "$gnu_inplace" ]; then
  echo "${C_RED}✗ SP-1${C_RST}: GNU-only \`sed -i\` with no suffix — BSD sed consumes the NEXT argument"
  echo "       as the backup suffix and edits the wrong thing:"
  printf '%s\n' "$gnu_inplace" | sed 's/^/       /'
  echo "       Use:  sed -i.bak … \"\$f\"  &&  rm -f \"\$f.bak\""
  fail=1
fi

# ── SP-2 — the other BSD/GNU splits, allowed only WITH a fallback on the same line ─────────────
for probe in 'readlink -f' 'stat -f ' 'md5 '; do
  hits="$(printf '%s\n' "$scripts" | while IFS= read -r f; do
    uncommented "$f" | grep -nF -- "$probe" 2>/dev/null | grep -vE '\|\||2>/dev/null' | sed "s|^|${f}:|"
  done)"
  if [ -n "$hits" ]; then
    echo "${C_RED}✗ SP-2${C_RST}: \`${probe}\` has no GNU fallback on the line (BSD/GNU split):"
    printf '%s\n' "$hits" | sed 's/^/       /'
    echo "       Give it one, e.g.  stat -f \"%A\" \"\$f\" 2>/dev/null || stat -c \"%a\" \"\$f\""
    fail=1
  fi
done

# ── SP-3 — a comment inside a `\` continuation silently truncates the command ──────────────────
#
# Hit 2026-09-17 in cmp-ios/scripts/embed-xcframework.sh. The Xcode Run Script phase read:
#
#     "$GRADLEW" -p "$REPO_ROOT" ":cmp-shared:linkDebugFrameworkIosArm64" \
#       # Exclude the WHOLE workerKmpAppCodegen family, never a subset.
#       -x :cmp-shared:workerKmpAppCodegenAll ... \
#       -x :sync:workerKmpAppCodegenWeb
#
# The backslash splices the comment onto the command line, so the command ENDS at the `#`. All
# twelve `-x` exclusions vanished — discarding the configuration cache those very lines existed to
# preserve — and the orphaned `-x …` block then ran as a command: "-x: command not found", exit 127.
# Under `set -e` that failed the build phase AFTER a 7-minute link, with no Gradle error to show.
# `bash -n` does not catch it: both spellings parse. Only reading the argv does.
cont_comment="$(printf '%s\n' "$scripts" | while IFS= read -r f; do
  awk 'BEGIN { prev = -1 }
       # A comment line: report it when a REAL command line armed the rule on the line above.
       # It never arms the rule itself — a usage block wrapping its example across `\`-terminated
       # comment lines is prose, not a truncated command.
       /^[[:space:]]*#/ { if (prev == NR-1) printf "%d:%s\n", NR, $0; prev = -1; next }
       /\\$/            { prev = NR; next }
                        { prev = -1 }' "$f" 2>/dev/null | sed "s|^|${f}:|"
done)"
if [ -n "$cont_comment" ]; then
  echo "${C_RED}✗ SP-3${C_RST}: comment line spliced into a \`\\\` continuation — the command ENDS here and"
  echo "       every argument below it is silently dropped (then runs as its own command):"
  printf '%s\n' "$cont_comment" | sed 's/^/       /'
  echo "       Move the comment ABOVE the command."
  fail=1
fi

# ── SP-4 — an empty array expanded under `set -u` kills the script on bash 3.2 ─────────────────
#
# macOS still ships bash 3.2 as /bin/bash while every CI runner has bash 5, so this ENTIRE class is
# invisible to CI by construction — it can only ever bite a contributor, on their machine, and CI
# will stay green while it does. That asymmetry is the whole reason it needs a static gate.
#
# Hit 2026-09-02 in ci-prepush.sh: `successful_tasks` is empty when every task fails, so the summary
# died at its own first loop and the "Failed tasks" list below it never printed — the script aborted
# precisely when it had something useful to say, after a 15-25 minute run.
#
# A count guard within the three lines above is accepted, because that is how the healed/failed
# loops in the same file are already written. The guard must be a CONDITIONAL: an
# `echo "count: ${#arr[@]}"` on the line above is not a guard, and reading one as such is exactly
# how the ci-prepush.sh loop looked safe.
#
# Only arrays declared `arr=()` are considered. One populated from a literal is not at risk, and
# flagging it would push this rule into the false positives that get a check switched off.
empty_arr_expand="$(printf '%s\n' "$scripts" | while IFS= read -r f; do
  vars="$(grep -oE '^[[:space:]]*(declare -a[[:space:]]+)?[A-Za-z_][A-Za-z0-9_]*=\(\)[[:space:]]*$' "$f" 2>/dev/null \
          | sed -E 's/^[[:space:]]*(declare -a[[:space:]]+)?//; s/=\(\)[[:space:]]*$//')"
  [ -z "$vars" ] && continue
  for v in $vars; do
    # A line ALREADY using the guarded idiom contains `${v[@]+"${v[@]}"}` — whose inner half matches
    # the bare pattern. Excluding it is what keeps the rule from flagging the very fix it prescribes.
    uncommented "$f" | grep -nE "\"\\\$\{$v\[@\]\}\"" 2>/dev/null | grep -vE "\\\$\{$v\[@\]\+" | while IFS= read -r hit; do
      ln="${hit%%:*}"
      # three lines above must carry `if … ${#v[@]} … -gt|-ne 0` for this to be guarded
      start=$(( ln > 3 ? ln - 3 : 1 ))
      sed -n "${start},$(( ln - 1 ))p" "$f" 2>/dev/null \
        | grep -qE "if[^#]*\\\$\{#$v\[@\]\}[^#]*-(gt|ne)[[:space:]]*0" && continue
      printf '%s:%s\n' "$f" "$hit"
    done
  done
done)"
if [ -n "$empty_arr_expand" ]; then
  echo "${C_RED}✗ SP-4${C_RST}: bare expansion of an array initialised empty — on bash 3.2 (macOS /bin/bash)"
  echo "       this raises \"unbound variable\" under \`set -u\` and aborts the script:"
  printf '%s\n' "$empty_arr_expand" | sed 's/^/       /'
  echo "       Use:  \${arr[@]+\"\\\${arr[@]}\"}   (keeps inner quoting; multi-word entries survive)"
  fail=1
fi

# ── SP-5 — bash-4-only builtins on a system whose /bin/bash is 3.2 ─────────────────────────────
#
# Hit 2026-09-18 in deployment/_shared/scripts/keystore-manager.sh, and it did NOT announce itself.
# That file's shebang is `#!/bin/bash` — pinned to 3.2 on macOS, so a Homebrew bash 5 does not save
# it — and it used `declare -A` for four maps. bash 3.2 REJECTS the declaration, then treats the
# name as an INDEXED array and evaluates each subscript arithmetically, so
# `MAP["google-services.json"]=X` fails with "invalid arithmetic operator" and the map stays EMPTY.
# None of that trips `set -e`. Measured before the fix: the secrets scan found 0 of 3 present files
# and reported success. Silent corruption in a credential tool, on every Mac.
#
# `mapfile`/`readarray` fail more quietly still — "command not found", leaving the array empty, so
# a check built on one passes having examined nothing.
#
# Opt-out (the script genuinely requires bash 4+, and says so):  # bash4-required: <reason>
bash4_only="$(printf '%s\n' "$scripts" | while IFS= read -r f; do
  grep -q 'bash4-required:' "$f" 2>/dev/null && continue
  uncommented "$f" \
    | grep -nE "(^|[^A-Za-z0-9_])(declare|typeset)[[:space:]]+-[A-Za-z]*A[[:space:]]|(^|[[:space:]])(mapfile|readarray)[[:space:]]|\\\$\{[A-Za-z_][A-Za-z0-9_]*(\[[^]]*\])?(,,|\^\^)" 2>/dev/null \
    | sed "s|^|${f}:|"
done)"
if [ -n "$bash4_only" ]; then
  echo "${C_RED}✗ SP-5${C_RST}: bash-4-only builtin — macOS /bin/bash is 3.2, and a \`#!/bin/bash\` shebang"
  echo "       pins the script to it. \`declare -A\` there silently yields an EMPTY map, not an error:"
  printf '%s\n' "$bash4_only" | sed 's/^/       /'
  echo "       Use: \"key|value\" pair lists, index-matched parallel arrays, a \`case\` lookup, or"
  echo "            a sentinel-delimited string for sets. \`mapfile\` → \`while IFS= read -r l; do a+=(\"\$l\"); done < <(…)\`."
  echo "       Genuinely needs bash 4+? Add:  # bash4-required: <reason>"
  fail=1
fi

[ "$fail" -eq 0 ] && echo "shell portable ($(printf '%s\n' "$scripts" | wc -l | tr -d ' ') tracked scripts; runs on macOS + Linux runners)"
exit "$fail"
