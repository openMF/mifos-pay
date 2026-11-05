#!/usr/bin/env bash
# checks/fork-props-single-reader.sh — gradle/fork.properties has ONE reader per language.
#
# app-profile is the fork SoT → derive.rb materializes gradle/fork.properties → everything else
# READS that bridge. The readers are:
#
#   Gradle  build-logic/convention/src/main/kotlin/org/convention/KptAppProfileProperties.kt
#   bash    scripts/_shared/fork-props.sh                      (fp_file / fp_get)
#   Ruby    deployment/_shared/project_config.rb               (FORK[...])
#
# WHY this is gated rather than merely documented: independent parsers are how resolution silently
# diverges, and it has already happened twice in this repo.
#   · 2026-09-06 — deployment/Appfile preferred the FILE while _shared/project_config.rb preferred
#     the ENV for the same apple.team.id, so the two could resolve DIFFERENT signing teams.
#   · Six Gradle files each ran Properties().load() with three helper shapes and three different
#     missing-key defaults; one of them silently shipped "App" as the settings-footer app name.
# Both were invisible until someone diffed the readers by hand. This check does that diff on every
# run, so a NEW hand-rolled parser fails immediately instead of years later at a signing desk.
#
# exit 0 PASS / 1 FAIL. Pure bash + grep (harness house style — no jq, no Ruby).
set -uo pipefail
# shellcheck source=../lib.sh
source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib.sh"
: "${HEALTH_ROOT:?fork-props-single-reader: HEALTH_ROOT not set (run via product-health.sh)}"

cd "$HEALTH_ROOT" || exit 1
fail=0

# ── Allowlist ─────────────────────────────────────────────────────────────────
# Every entry is a DELIBERATE exception with a stated reason. Adding a path here is a decision;
# adding one without a reason is how the allowlist becomes a rubber stamp.
is_allowed() {
  case "$1" in
    # The readers themselves, and this check (it necessarily NAMES the idioms it detects).
    */org/convention/KptAppProfileProperties.kt|*/scripts/_shared/fork-props.sh) return 0 ;;
    */checks/fork-props-single-reader.sh) return 0 ;;
    # WRITERS + the generator. SyncForkConfigPlugin owns app-profile → ENV → catalog because it
    # produces the bridge; derive.rb writes it; fork-init.sh authors app.id; setup-project.sh seeds
    # the file from .template and writes the keystore DN. A writer cannot go through a reader.
    */SyncForkConfigPlugin.kt|*/white-label/derive.rb|*/white-label/fork-init.sh|*/setup-project.sh) return 0 ;;
    # deployment/Appfile is self-sufficient ON PURPOSE: fastlane's CredentialsManager evaluates the
    # Appfile BEFORE the Fastfile imports config.rb, which raised
    # `uninitialized constant CredentialsManager::AppfileConfig::FastlaneConfig`. Its own header
    # documents this. Routing it through the Ruby reader would reintroduce that load-order bug.
    */deployment/Appfile|*/fastlane/Appfile) return 0 ;;
    # The Ruby reader + its manifest.
    */deployment/_shared/project_config.rb|*/deployment/_shared/config.rb) return 0 ;;
    # (The four iOS/keystore signing scripts were migrated to fp_get on 2026-09-06 and are
    # deliberately NOT exempt any more — they are held to the same rule as everything else.)
    *) return 1 ;;
  esac
}

# candidates <ext...> — files of the given extensions that MENTION fork.properties, found in ONE
# grep pass rather than a grep per file.
#
# The naive shape (walk every *.kt, then `grep -q` each) spawns thousands of processes on this repo
# and measured 43s — it would have turned a ~10s CI job into ~50s. Only ~38 files in the tree mention
# fork.properties at all, so finding THOSE first and examining only them is ~1s for the same answer.
#
# --exclude-dir prunes during the walk. product-health/tests/** is excluded because a canary's RED
# fixture is a DELIBERATE violation used to prove this gate can fail; scanning it would pin the gate
# red and make the canary self-defeating.
candidates() {
  local includes=() e
  for e in "$@"; do includes+=(--include="$e"); done
  grep -rl "fork\.properties" . "${includes[@]}" \
    --exclude-dir=build --exclude-dir=.git --exclude-dir=node_modules \
    --exclude-dir=.gradle --exclude-dir=tests 2>/dev/null
}

# Comments are stripped before matching: a file that only DESCRIBES the banned idiom (every migrated
# call site carries a "use the reader instead of Properties().load()" note) must not trip the gate.
strip_kt_comments() { sed -e 's://.*::' "$1" | tr '\n' '\001' | sed -e 's:/\*[^\001]*\*/::g' | tr '\001' '\n'; }
# Remove `#` comments from shell source before scanning it.
# Without this, a comment that MENTIONS fork.properties counted as a hand-rolled
# read and the check failed on its own documentation.
strip_sh_comments() { sed -e 's/^[[:space:]]*#.*//' -e 's/[[:space:]]#[^"'"'"']*$//' "$1"; }

report() { # <file> <language> <how>
  echo "❌ $1"
  echo "     hand-rolls a $2 read of gradle/fork.properties ($3)"
  fail=1
}

# ── Gradle/Kotlin: builds its own Properties() ────────────────────────────────────────────────────
for f in $(candidates '*.kt' '*.kts'); do
  strip_kt_comments "$f" | grep -qE "Properties\(\)" || continue
  is_allowed "$f" && continue
  report "$f" "Gradle" "Properties().load() — use org.convention.KptAppProfileProperties / forkProp()"
done

# ── bash: splits the bridge itself ────────────────────────────────────────────────────────────────
# The split must operate on a fork-properties operand — otherwise a script that merely MENTIONS
# fork.properties while parsing an unrelated file (sync-dirs.sh reads .template-version) trips it.
for f in $(candidates '*.sh'); do
  strip_sh_comments "$f" | grep -E "cut -d=|awk -F=" | grep -qiE "fork|props" || continue
  is_allowed "$f" && continue
  report "$f" "bash" "cut -d= / awk -F= — source scripts/_shared/fork-props.sh and use fp_get"
done

# ── Ruby: regex-extracts a key itself ─────────────────────────────────────────────────────────────
for f in $(candidates '*.rb' 'Appfile'); do
  grep -qE '\[/\^\\s\*|\[/\^\[a-z]' "$f" 2>/dev/null || continue
  is_allowed "$f" && continue
  report "$f" "Ruby" "inline regex — use FORK[...] from deployment/_shared/project_config.rb"
done

if [ "$fail" = "0" ]; then
  echo "one reader per language: Gradle=KptAppProfileProperties.kt · bash=_shared/fork-props.sh · Ruby=project_config.rb"
  exit 0
fi
echo "→ Fix: read through your language's reader. A genuinely new exception goes in is_allowed() WITH a reason."
exit 1
