#!/usr/bin/env bash
# gen-kotlin-filelist.sh — keep cmp-ios/kotlin-sources.xcfilelist in sync with the Kotlin sources.
#
# WHY IT EXISTS. The "[KMP] Embed and Sign ComposeApp XCFramework" Run Script phase declares this
# list as its input. Without declared inputs Xcode treats a script phase as ALWAYS out of date and
# re-runs Gradle on every build (~8-11 min); with them, an unchanged build skips it (~5s). Declaring
# only OUTPUTS would be worse than nothing: Xcode would skip the phase even after a Kotlin edit and
# silently ship stale code. The list of sources is what makes skipping SAFE.
#
# WHY IT CANNOT RUN DURING THE BUILD. Xcode resolves `inputFileListPaths` at graph-construction
# time, BEFORE any build phase executes — verified 2026-09-16 by inserting a generator phase ahead
# of the consumer: it never ran and the build failed with "Unable to load contents of file list".
# So this must run BEFORE xcodebuild: /idea-build-kmp's iOS step and the deployment lanes call it.
#
# WRITE-ONLY-IF-CHANGED IS LOAD-BEARING. This file is a declared build input, so rewriting it
# unconditionally would invalidate the phase on EVERY run and destroy the very speed-up it enables.
# The list is compared and replaced only when it genuinely differs.
#
# A missing list is FATAL to the build; stale ENTRIES are tolerated (a listed-but-absent file does
# not fail). That asymmetry is why the file is git-tracked rather than ignored.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
OUT="$ROOT/cmp-ios/kotlin-sources.xcfilelist"
TMP="$(mktemp)"; trap 'rm -f "$TMP"' EXIT

# $(SRCROOT)-relative, never absolute: the list is committed and inherited by forks.
( cd "$ROOT" && find cmp-shared/src core core-base feature \
    -name '*.kt' -not -path '*/build/*' 2>/dev/null | LC_ALL=C sort \
    | sed 's|^|$(SRCROOT)/../|' ) > "$TMP"

if [ -f "$OUT" ] && cmp -s "$TMP" "$OUT"; then
  echo "note: [KMP] kotlin-sources.xcfilelist up to date ($(wc -l < "$OUT" | tr -d ' ') paths)"
else
  n_old=$([ -f "$OUT" ] && wc -l < "$OUT" | tr -d ' ' || echo 0)
  cp "$TMP" "$OUT"
  echo "note: [KMP] kotlin-sources.xcfilelist regenerated ($n_old -> $(wc -l < "$OUT" | tr -d ' ') paths)"
fi
