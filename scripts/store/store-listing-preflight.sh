#!/usr/bin/env bash
# scripts/store/store-listing-preflight.sh
#
# Store-listing PREFLIGHT — CI mirror of `/release` STEP 1.7 (Store Listing Wizard) validation.
# Fails fast if the store listing metadata is missing or exceeds store character limits, so the
# upload never gets rejected by the Play/App Store API with "missing required field".
#
# The deploy itself still UPLOADS the listing (Android via metadata_path, iOS via deliver);
# this gate only validates it is present + within limits BEFORE the gated, billable build runs.
#
# Usage: store-listing-preflight.sh <platform> [locale] [metadata_root]
#   platform       android | ios   (default: android)
#   locale         default: en-US
#   metadata_root  default: deployment/<platform>/metadata  (ios: deployment/ios/appstore/metadata)
set -euo pipefail

PLATFORM="${1:-android}"
LOCALE="${2:-en-US}"

FAIL=0
# Print a failure — a field that will be rejected at upload.
err()  { echo "❌ $*"; FAIL=1; }
# Print a passing field.
ok()   { echo "✅ $*"; }
# Print a warning: allowed by the store, but likely not what was intended.
warn() { echo "⚠️  $*"; }

# Character count of a file's content (trailing newline stripped); 0 when missing.
clen() {
  if [ -f "$1" ]; then printf '%s' "$(cat "$1")" | wc -m | tr -d ' '; else echo 0; fi
}

# check <dir> <relpath> <max> <label> <severity:hard|soft>
check() {
  local dir="$1" file="$1/$2" max="$3" label="$4" sev="${5:-hard}" len
  if [ ! -f "$file" ]; then
    [ "$sev" = "hard" ] && err "$label — MISSING ($2)" || warn "$label — missing ($2); generated at deploy"
    return
  fi
  len=$(clen "$file")
  if [ "$len" -eq 0 ]; then
    [ "$sev" = "hard" ] && err "$label — EMPTY ($2)" || warn "$label — empty ($2); generated at deploy"
    return
  fi
  if [ "$len" -gt "$max" ]; then
    err "$label — $len chars > $max limit ($2)"
    return
  fi
  ok "$label — $len/$max chars"
}

case "$PLATFORM" in
  android)
    ROOT="${3:-deployment/android/metadata}"
    DIR="$ROOT/$LOCALE"
    echo "🔎 Play Store listing preflight · $DIR"
    echo ""
    if [ ! -d "$DIR" ]; then
      err "No Play Store listing metadata at '$DIR' — the Play API rejects uploads without a title/description."
      echo "   Generate it with /release (STEP 1.7 — Store Listing Wizard)."
      exit 1
    fi
    # Play Store hard limits (required-at-upload fields).
    check "$DIR" title.txt               30 "Title"             hard
    check "$DIR" short_description.txt    80 "Short description" hard
    check "$DIR" full_description.txt   4000 "Full description"  hard
    # Release notes are (re)generated at deploy time by the lane → soft.
    check "$DIR" changelogs/default.txt  500 "Changelog (default.txt)" soft
    ;;

  ios)
    ROOT="${3:-deployment/ios/appstore/metadata}"
    DIR="$ROOT/$LOCALE"
    echo "🔎 App Store listing preflight · $DIR"
    echo ""
    if [ ! -d "$DIR" ]; then
      err "No App Store listing metadata at '$DIR' — App Store Connect rejects submission without name/description."
      echo "   Generate it with /release (STEP 1.7 — Store Listing Wizard)."
      exit 1
    fi
    # App Store Connect hard limits.
    check "$DIR" name.txt              30 "Name"             hard
    check "$DIR" subtitle.txt          30 "Subtitle"         hard
    check "$DIR" keywords.txt         100 "Keywords (total)" hard
    check "$DIR" description.txt      4000 "Description"      hard
    # Optional / regenerated fields → soft.
    check "$DIR" promotional_text.txt 170 "Promotional text" soft
    check "$DIR" release_notes.txt   4000 "Release notes"    soft
    ;;

  *)
    err "Unknown platform '$PLATFORM' (expected: android | ios)"
    exit 2
    ;;
esac

echo ""
if [ "$FAIL" -ne 0 ]; then
  echo "❌ Store-listing preflight FAILED ($PLATFORM) — fix the fields above before deploying."
  exit 1
fi
echo "✅ Store-listing preflight PASSED ($PLATFORM)"
