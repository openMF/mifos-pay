#!/usr/bin/env bash
# checks/pbxproj-merge.sh — the Xcode project survives a template sync intact.
#
# `project.pbxproj` is the one file in the sync surface where a line-based 3-way is not merely worse
# but UNUSABLE. It is a serialized object graph keyed by 24-hex UUIDs, so "add a target" rewrites
# UUID arrays three levels away from the object it adds; git merge-file reads adjacent array
# insertions as a conflict, and a conflict marker inside a pbxproj makes the project unopenable —
# a human cannot even use Xcode to resolve what the merge produced.
#
# It is tractable anyway because the UUIDs are STABLE ACROSS THE FORK BOUNDARY: a fork's project
# descends from the template's, so the same object carries the same key on both sides (measured:
# 33 of 36 shared). That turns a hopeless text merge into an ordinary keyed 3-way.
#
# Run against tests/fixtures/pbxproj-3way-canary/ — real captured data, both sides genuinely
# diverged from the base. See that directory's README for what each file is.
#
#   PM-1  the merger exists, is valid ruby, and the contract routes pbxproj to pbxproj-3way
#   PM-2  the canary merges with exit 0 and ZERO conflict markers
#          (and git merge-file on the same inputs really does conflict — else PM-2 is vacuous)
#   PM-3  both sides' content survives: template SPM migration AND the fork's extension target
#   PM-4  no dangling UUID references — every UUID named in a list has an object
#   PM-5  the merged graph re-opens through the xcodeproj gem (the real structural validation)
#   PM-6  Info.plist / *.entitlements union: both sides' keys survive and the result PARSES
#
# PM-6 covers the same sync surface with a different failure. Both files are a flat <dict> each
# side adds its OWN keys to, and every one of those keys is required: the template's
# keychain-access-groups (without it core-base/datastore fails errSecMissingEntitlement -34018 at
# launch) alongside the fork's app-group (widget shared container) and CFBundleURLTypes (the OAuth
# redirect — delete it and sign-in silently never returns). Union is the only answer that keeps the
# app working, so PM-6 asserts BOTH sides' keys survive AND the output parses.
# Measured: `iosApp.entitlements` is add/add against the fork's base, where a line merge conflicts
# and the result fails plutil. `Info.plist` line-merges cleanly today and is covered for the same
# shape, not for a present breakage.
#
# Skips (exit 0, WARN) when ruby/bundler is unavailable — the same degradation the engine takes,
# where the fallback is KEEP-OURS, never take-theirs.
#
# exit 0 PASS / 1 FAIL. Bash + ruby.
set -uo pipefail
: "${HEALTH_ROOT:?pbxproj-merge: HEALTH_ROOT not set (run via product-health.sh)}"
# PM_MERGER lets the canary point at a deliberately-broken copy while still using THIS repo's
# bundler/ruby toolchain — a RED fixture that fakes HEALTH_ROOT instead loses the gem environment
# and fails for the wrong reason, which proves nothing.
MERGER="${PM_MERGER:-$HEALTH_ROOT/scripts/white-label/merge-pbxproj.rb}"
FIX="${PM_FIXTURE:-$HEALTH_ROOT/tests/fixtures/pbxproj-3way-canary}"
CS="$HEALTH_ROOT/scripts/customization-surface.sh"

fail=0
say() { printf '  %-8s %s\n' "$1" "$2"; }
bad() { say "❌ $1" "$2"; fail=1; }

# ── PM-1 ───────────────────────────────────────────────────────────────────
if [ ! -f "$MERGER" ]; then
  bad PM-1 "merge-pbxproj.rb missing — pbxproj would fall back to a line merge"
elif ! { . "$HEALTH_ROOT/scripts/ruby-exec.sh" 2>/dev/null; ruby_exec -c "$MERGER" >/dev/null 2>&1; }; then
  # Pinned interpreter, never PATH ruby: macOS ships 2.6.10, which rejects 3.x syntax the mergers
  # legitimately use (endless method defs). A PATH check would fail a perfectly valid script.
  bad PM-1 "merge-pbxproj.rb is not valid ruby"
else
  got="$( (cd "$HEALTH_ROOT" && bash "$CS" resolve "cmp-ios/iosApp.xcodeproj/project.pbxproj" 2>/dev/null) )"
  case "$got" in
    *merge*pbxproj-3way*) say "✅ PM-1" "merger present; contract routes pbxproj → pbxproj-3way" ;;
    *) bad PM-1 "contract resolves pbxproj to '${got#* }' — expected merge (strategy: pbxproj-3way)" ;;
  esac
fi

for f in base ours theirs; do
  [ -f "$FIX/$f.pbxproj" ] || { bad PM-0 "canary input missing: $FIX/$f.pbxproj"; fail=1; }
done
[ "$fail" -eq 0 ] || exit 1

# ── ruby availability (skip, don't fail) ────────────────────────────────────
# shellcheck source=/dev/null
. "$HEALTH_ROOT/scripts/ruby-exec.sh" 2>/dev/null || true
if ! declare -F ruby_exec >/dev/null 2>&1; then
  say "⚠️ PBX" "ruby-exec.sh unavailable — merge checks skipped (engine falls back to KEEP-OURS)"
  # ── PM-6 — plist union ──────────────────────────────────────────────────────
PLIST_MERGER="${PM_PLIST_MERGER:-$HEALTH_ROOT/scripts/white-label/merge-plist.rb}"
if [ ! -f "$PLIST_MERGER" ]; then
  bad PM-6 "merge-plist.rb missing — Info.plist/entitlements would fall back to a line merge"
else
  pm6=0
  for pair in "entitlements:com.apple.security.application-groups:keychain-access-groups" \
              "infoplist:CFBundleURLTypes:CFBundleIdentifier"; do
    n="${pair%%:*}"; rest="${pair#*:}"; forkkey="${rest%%:*}"; tmplkey="${rest##*:}"
    [ -f "$FIX/$n.ours" ] && [ -f "$FIX/$n.theirs" ] || { bad PM-6 "canary input missing: $FIX/$n.*"; pm6=1; continue; }
    out="$WORK/$n.union"
    # `--base -` is the measured reality: both files are add/add against the fork's base.
    if ruby_exec "$PLIST_MERGER" --ours "$FIX/$n.ours" --base - --theirs "$FIX/$n.theirs" \
         --out "$out" >"$WORK/plog.$n" 2>&1 && [ -f "$out" ]; then
      grep -q "<key>$forkkey</key>" "$out" || { bad PM-6 "$n: lost the fork's $forkkey"; pm6=1; }
      grep -q "<key>$tmplkey</key>" "$out" || { bad PM-6 "$n: lost the template's $tmplkey"; pm6=1; }
      if command -v plutil >/dev/null 2>&1 && ! plutil -lint "$out" >/dev/null 2>&1; then
        bad PM-6 "$n: merged plist does not parse"; pm6=1
      fi
      grep -q '^<<<<<<<' "$out" && { bad PM-6 "$n: conflict markers in an XML plist"; pm6=1; }
    else
      bad PM-6 "$n: merger failed — $(tail -2 "$WORK/plog.$n" | tr '\n' ' ')"; pm6=1
    fi
  done
  [ "$pm6" -eq 0 ] && say "✅ PM-6" "plist union keeps both sides' keys and the result parses"
fi

# 0 PASS · 1 FAIL (blocks) · 2 WARN (gem unavailable — canary not exercised, non-blocking)
exit "$fail"
fi

WORK="$(mktemp -d)"; trap 'rm -rf "$WORK"' EXIT
mkdir -p "$WORK/out.xcodeproj"
OUT="$WORK/out.xcodeproj/project.pbxproj"

# ── PM-2 — and prove the check is not vacuous ──────────────────────────────
cp "$FIX/ours.pbxproj" "$WORK/lineattempt"
git merge-file "$WORK/lineattempt" "$FIX/base.pbxproj" "$FIX/theirs.pbxproj" >/dev/null 2>&1
lm="$(grep -c '^<<<<<<<\|^>>>>>>>' "$WORK/lineattempt" 2>/dev/null || true)"; lm="${lm:-0}"
if [ "$lm" -eq 0 ]; then
  bad PM-2 "git merge-file does NOT conflict on this fixture — the canary no longer proves anything"
fi

# Plain interpreter first, bundler only if the gem is genuinely absent — mirrors what
# merge_pbxproj_3way does in the engine, so this check exercises the real code path.
ruby_exec "$MERGER" --ours "$FIX/ours.pbxproj" --base "$FIX/base.pbxproj" \
  --theirs "$FIX/theirs.pbxproj" --out "$OUT" >"$WORK/log" 2>&1
mrc=$?
if [ "$mrc" -eq 2 ] && declare -F ruby_bundle >/dev/null 2>&1; then
  ruby_bundle "$HEALTH_ROOT" -- exec ruby "$MERGER" \
    --ours "$FIX/ours.pbxproj" --base "$FIX/base.pbxproj" --theirs "$FIX/theirs.pbxproj" \
    --out "$OUT" >"$WORK/log" 2>&1
  mrc=$?
fi

# Exit 2 means the `xcodeproj` gem is unavailable — NOT that the merge is broken. merge-pbxproj.rb
# reserves that code for exactly this, and the engine treats it the same way (keep-ours, never
# take-theirs). A runner without the gem cannot exercise the canary, and failing the build there
# would block every fork whose CI does not install it for a capability the fork may never use.
# It is a WARN — visible and clearable — not a pass and not a failure.
GEM_MISSING=0
if [ "$mrc" -eq 2 ]; then
  GEM_MISSING=1
  say "⚠️ PM-2" "the xcodeproj gem is unavailable here — pbxproj merge NOT exercised (PM-3..PM-5 skipped)"
  echo "           clear it with:  gem install xcodeproj --no-document   (it also ships as a fastlane transitive)"
  [ "$fail" -eq 0 ] && fail=2
elif [ "$mrc" -eq 0 ]; then
  m="$(grep -c '^<<<<<<<\|^>>>>>>>' "$OUT" 2>/dev/null || true)"; m="${m:-0}"
  if [ "$m" -eq 0 ]; then
    say "✅ PM-2" "structural merge clean (git merge-file left $lm marker(s) on the same inputs)"
  else
    bad PM-2 "$m conflict marker(s) in the merged pbxproj — the project would not open"
  fi
else
  bad PM-2 "merger exited non-zero: $(tail -3 "$WORK/log" | tr '\n' ' ')"
fi

# A sub-check that quietly does not run reads exactly like one that passed. When the merge produced
# no output these three used to disappear from the report entirely — say so instead.
if [ ! -f "$OUT" ] && [ "$GEM_MISSING" -eq 0 ]; then
  bad PM-3 "no merged pbxproj was produced — PM-3..PM-5 could not run"
fi
if [ -f "$OUT" ]; then
  # ── PM-3 ─────────────────────────────────────────────────────────────────
  miss=""
  for tok in XCLocalSwiftPackageReference KotlinMultiplatformLinkedPackage \
             CappyWidgets PayCraftStoreKit2.swift CODE_SIGN_ENTITLEMENTS; do
    grep -q "$tok" "$OUT" || miss="$miss $tok"
  done
  if [ -z "$miss" ]; then
    say "✅ PM-3" "template SPM migration landed AND the fork's extension target survived"
  else
    bad PM-3 "merged pbxproj lost:$miss"
  fi

  # ── PM-4 ─────────────────────────────────────────────────────────────────
  # Every 24-hex UUID that appears as a LIST ELEMENT must have an object definition. A dangling
  # reference is the silent-loss failure: the gem drops it on load and the merge still "passes".
  defined="$WORK/def"; referenced="$WORK/ref"
  grep -oE '^	*[0-9A-F]{24} ' "$OUT" | tr -d ' \t' | sort -u > "$defined"
  grep -oE '^				[0-9A-F]{24} ' "$OUT" | tr -d ' \t' | sort -u > "$referenced"
  dang="$(comm -13 "$defined" "$referenced" | wc -l | tr -d ' ')"; dang="${dang:-0}"
  if [ "$dang" -eq 0 ]; then
    say "✅ PM-4" "no dangling UUID references"
  else
    bad PM-4 "$dang UUID(s) referenced in a list with no object definition"
    comm -13 "$defined" "$referenced" | head -5 | sed 's/^/           /'
  fi

  # ── PM-5 ─────────────────────────────────────────────────────────────────
  # The merger already round-trips through Xcodeproj::Project on write; re-open independently so a
  # regression that removes that validation is still caught here.
  cat > "$WORK/verify.rb" <<'RB'
require 'xcodeproj'
proj = Xcodeproj::Project.open(File.dirname(ARGV[0]))
abort "no targets" if proj.targets.empty?
puts "#{proj.objects.count} objects, targets: #{proj.targets.map(&:name).sort.join(', ')}"
RB
  # ruby_exec, NOT ruby_bundle — the same path the merger itself took two checks above. Routing this
  # one through bundler asks for a fully-installed Gemfile in the repo, which CI deliberately does
  # not have (`bundler-cache: false`, so the ~10s job does not drag in the fastlane tree). It died in
  # `<internal:gem_prelude>` on the runner while PM-2..PM-4 passed on the very same merged file —
  # a failure about bundler, reported as "the merged graph does not re-open".
  if out5="$(ruby_exec "$WORK/verify.rb" "$OUT" 2>&1 | tail -1)"; then
    say "✅ PM-5" "graph re-opens — $out5"
  else
    bad PM-5 "merged graph does not re-open: $out5"
  fi
fi

# ── PM-6 — plist union ──────────────────────────────────────────────────────
PLIST_MERGER="${PM_PLIST_MERGER:-$HEALTH_ROOT/scripts/white-label/merge-plist.rb}"
if [ ! -f "$PLIST_MERGER" ]; then
  bad PM-6 "merge-plist.rb missing — Info.plist/entitlements would fall back to a line merge"
else
  pm6=0
  for pair in "entitlements:com.apple.security.application-groups:keychain-access-groups" \
              "infoplist:CFBundleURLTypes:CFBundleIdentifier"; do
    n="${pair%%:*}"; rest="${pair#*:}"; forkkey="${rest%%:*}"; tmplkey="${rest##*:}"
    [ -f "$FIX/$n.ours" ] && [ -f "$FIX/$n.theirs" ] || { bad PM-6 "canary input missing: $FIX/$n.*"; pm6=1; continue; }
    out="$WORK/$n.union"
    # `--base -` is the measured reality: both files are add/add against the fork's base.
    if ruby_exec "$PLIST_MERGER" --ours "$FIX/$n.ours" --base - --theirs "$FIX/$n.theirs" \
         --out "$out" >"$WORK/plog.$n" 2>&1 && [ -f "$out" ]; then
      grep -q "<key>$forkkey</key>" "$out" || { bad PM-6 "$n: lost the fork's $forkkey"; pm6=1; }
      grep -q "<key>$tmplkey</key>" "$out" || { bad PM-6 "$n: lost the template's $tmplkey"; pm6=1; }
      if command -v plutil >/dev/null 2>&1 && ! plutil -lint "$out" >/dev/null 2>&1; then
        bad PM-6 "$n: merged plist does not parse"; pm6=1
      fi
      grep -q '^<<<<<<<' "$out" && { bad PM-6 "$n: conflict markers in an XML plist"; pm6=1; }
    else
      bad PM-6 "$n: merger failed — $(tail -2 "$WORK/plog.$n" | tr '\n' ' ')"; pm6=1
    fi
  done
  [ "$pm6" -eq 0 ] && say "✅ PM-6" "plist union keeps both sides' keys and the result parses"
fi

exit "$fail"
