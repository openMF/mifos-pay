#!/usr/bin/env bash
# generate-screenshots.sh
# Renders Claude-intelligence-generated HTML screenshots to PNG at store-submission resolution.
#
# Resolutions used (CSS pixels = PNG pixels at scale-factor 1):
#   Android phone     1080 × 1920   (HD portrait)
#   iOS iPhone 6.9"   1320 × 2868   (iPhone 16 Pro Max native ÷ 3 × 3 = store minimum)
#   iOS iPhone 6.3"   1206 × 2622   (iPhone 16 Pro native)
#   iOS iPad Pro 13"  2064 × 2752   (iPad Pro 13 M4)
#   macOS             2560 × 1600   (Retina 16" MacBook Pro)
#
# Prerequisites: Google Chrome (headless)
#   macOS: /Applications/Google Chrome.app
#   Linux: google-chrome or chromium-browser in PATH
#
# Usage:
#   ./scripts/store/generate-screenshots.sh             # all platforms
#   ./scripts/store/generate-screenshots.sh android
#   ./scripts/store/generate-screenshots.sh ios
#   ./scripts/store/generate-screenshots.sh macos

set -euo pipefail
REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$REPO_ROOT"

# ── Detect Chrome ──────────────────────────────────────────────────────────────
if [[ "$(uname)" == "Darwin" ]]; then
  CHROME="/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
else
  CHROME="$(command -v google-chrome 2>/dev/null || command -v chromium-browser 2>/dev/null || echo "")"
fi
if [[ ! -f "$CHROME" ]] && [[ ! -x "$CHROME" ]]; then
  echo "❌  Chrome not found. Install Google Chrome and retry."
  exit 1
fi

# Render HTML $1 to PNG $2 at $3×$4 using headless Chrome.
# Fixed dimensions because store screenshots must be submitted at exact resolutions —
# a scaled image is rejected at upload.
chrome_png() {
  local html="$1" png="$2" w="$3" h="$4"
  "$CHROME" \
    --headless=new \
    --disable-gpu \
    --no-sandbox \
    --disable-software-rasterizer \
    --screenshot="$png" \
    --window-size="${w},${h}" \
    --force-device-scale-factor=1 \
    --hide-scrollbars \
    --disable-extensions \
    "file://${html}" 2>/dev/null
}

# Render every .html in $2 to a sibling PNG at $3×$4, labelled $1 in the log.
render_dir() {
  local label="$1" dir="$2" w="$3" h="$4"
  echo "  [$label  ${w}×${h}]"
  for f in "$dir"/*.html; do
    [[ -f "$f" ]] || continue
    png="${f%.html}.png"
    chrome_png "$f" "$png" "$w" "$h"
    kb=$(du -k "$png" | cut -f1)
    echo "    ✅  $(basename "$png")  (${kb} KB)"
  done
}

# Copy the Android screenshot HTML into both iPhone size directories.
# One set of source HTML, rendered at each store's required resolution — otherwise the
# same screenshot has to be authored twice and the two drift.
sync_ios_from_android() {
  local android_dir="$REPO_ROOT/deployment/android/metadata/images/phoneScreenshots"
  local iphone_69="$REPO_ROOT/deployment/ios/appstore/metadata/screenshots/iPhone_6.9/en-GB"
  local iphone_63="$REPO_ROOT/deployment/ios/appstore/metadata/screenshots/iPhone_6.3/en-GB"
  for f in "$android_dir"/*.html; do
    [[ -f "$f" ]] || continue
    cp "$f" "$iphone_69/$(basename "$f")"
    cp "$f" "$iphone_63/$(basename "$f")"
  done
}

# Render the Android phone set at the Play Store's 1080×1920.
do_android() {
  echo "📱  Android phone (1080×1920)…"
  render_dir "phone" \
    "$REPO_ROOT/deployment/android/metadata/images/phoneScreenshots" \
    1080 1920
}

# Sync the HTML from Android, then render both iPhone sizes the App Store requires.
do_ios() {
  echo "📱  Syncing Android HTML → iOS phone dirs…"
  sync_ios_from_android

  echo "📱  iOS iPhone 6.9\" (1320×2868)…"
  render_dir "iPhone_6.9" \
    "$REPO_ROOT/deployment/ios/appstore/metadata/screenshots/iPhone_6.9/en-GB" \
    1320 2868

  echo "📱  iOS iPhone 6.3\" (1206×2622)…"
  render_dir "iPhone_6.3" \
    "$REPO_ROOT/deployment/ios/appstore/metadata/screenshots/iPhone_6.3/en-GB" \
    1206 2622

  echo "📲  iOS iPad Pro 13\" (2064×2752)…"
  render_dir "iPad_Pro_13" \
    "$REPO_ROOT/deployment/ios/appstore/metadata/screenshots/iPad_Pro_13/en-GB" \
    2064 2752
}

# Render the Mac App Store set at 2560×1600.
do_macos() {
  echo "💻  macOS (2560×1600)…"
  render_dir "macOS" \
    "$REPO_ROOT/deployment/desktop/mac-app-store/metadata/screenshots/en-GB" \
    2560 1600
}

PLATFORM="${1:-all}"
case "$PLATFORM" in
  android) do_android ;;
  ios)     do_ios ;;
  macos)   do_macos ;;
  all)     do_android; do_ios; do_macos ;;
  *)       echo "Usage: $0 [android|ios|macos|all]"; exit 1 ;;
esac

echo ""
echo "🎉  Screenshot generation complete."
echo ""
echo "    Next steps:"
echo "    1. Review PNGs in deployment/*/metadata/"
echo "    2. bundle exec fastlane ios frame_ios_screenshots   (add device bezels)"
echo "    3. bundle exec fastlane ios upload_ios_screenshots"
echo "    4. bundle exec fastlane android upload_android_screenshots"
echo "    5. bundle exec fastlane mac upload_macos_screenshots"
