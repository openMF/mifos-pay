#!/usr/bin/env bash
# checks/sync-merge-base.sh — an `owner: merge` row must actually 3-way, not silently full-copy.
#
# THE FAILURE THIS CATCHES
# `owner: merge` is the contract's promise that a template update and a fork's edits BOTH survive.
# The engine keeps that promise with `git merge-file ours base theirs` — and the promise is only as
# good as `base`. Two fork topologies exist:
#
#   forked  `gh repo fork` / clone of the template. Shares history, so `git merge-base` works.
#   copied  the template TREE copied into a fresh repo — the common case in practice. Different root
#           commits, ZERO shared commits, and `git merge-base` returns EMPTY.
#
# With an empty base the engine used `ours` as the base. `git merge-file` then sees every template
# difference as a clean addition onto an untouched base and takes ALL of theirs — reproducing exactly
# the full-copy that `merge` exists to prevent. It printed "Merged (…) — fork edits preserved" while
# doing it, so the loss was invisible in the log AND in the diff.
#
# Measured on mbs/cappy 2026-09-10: zero shared commits with openMF/kmp-project-template, and 11
# platform-shell files (Info.plist's OAuth CFBundleURLTypes, the WidgetKit app-group entitlement, a
# whole CappyWidgets Xcode target) that a degraded merge would have taken from the template wholesale.
#
# `.template-version#template_sha` is the anchor for the copied case — it records the template commit
# the tree last synced FROM, which is precisely the ancestor git cannot compute across unrelated
# histories. Its own file header already said it "anchors the next sync's 3-way merge base"; the merge
# loops simply were not reading it. That gap is what these checks pin.
#
#   MB-1  resolve_merge_base() exists and consults PREV_TEMPLATE_SHA
#   MB-2  every merge loop resolves its base through it — no bare `git merge-base` assignment
#   MB-3  a missing base is WARNED about, never reported as a successful merge
#   MB-4  the engine refuses to line-merge a binary
#   MB-5  the cmp-* files carrying fork platform wiring resolve `merge` (no other seam exists for an
#         Xcode target or a plist key)
#   MB-6  .template-version carries a parseable template_sha (the anchor MB-1 depends on)
#   MB-7  build-logic + the rest of the cmp-* shells resolve `template` (replaced in full); kotlin/local/** stays `fork`
#
# exit 0 PASS / 1 FAIL. Pure bash + grep.
set -uo pipefail
: "${HEALTH_ROOT:?sync-merge-base: HEALTH_ROOT not set (run via product-health.sh)}"
ENGINE="${MB_ENGINE:-$HEALTH_ROOT/scripts/white-label/sync-dirs.sh}"
CS="${MB_CS:-$HEALTH_ROOT/scripts/customization-surface.sh}"
TV="${MB_TV:-$HEALTH_ROOT/.template-version}"

fail=0
# Print one check result, prefixed with its id ($1) so a failure names the rung it broke.
say() { printf '  %-7s %s\n' "$1" "$2"; }
# Print a failure via `say` and set the fail flag.
bad() { say "❌ $1" "$2"; fail=1; }

[ -f "$ENGINE" ] || { bad MB-0 "engine not found: $ENGINE"; exit 1; }

# ── MB-1 ─────────────────────────────────────────────────────────────────────
if grep -q '^resolve_merge_base()' "$ENGINE" \
   && sed -n '/^resolve_merge_base()/,/^}/p' "$ENGINE" | grep -q 'PREV_TEMPLATE_SHA'; then
  say "✅ MB-1" "resolve_merge_base() falls back to PREV_TEMPLATE_SHA"
else
  bad MB-1 "resolve_merge_base() missing or does not consult PREV_TEMPLATE_SHA — copied forks get base==ours"
fi

# ── MB-2 ─────────────────────────────────────────────────────────────────────
# A bare `mbase=$(git merge-base …)` assignment is the regression. Calls INSIDE resolve_merge_base
# are the legitimate ones, so exclude that function's body.
# Match the ASSIGNMENT, in any of its shell spellings — `mbase=$(…)`, `local mbase=$(…)` and
# `local mbase; mbase=$(…)`. An earlier pattern anchored `local[[:space:]]+mbase=` and so missed the
# semicolon form, which is the one the engine actually used: the check passed on a fixture with the
# defect fully reintroduced. Keyed on `merge base` + assignment, not on declaration syntax.
bare="$(sed '/^resolve_merge_base()/,/^}/d' "$ENGINE" \
        | grep -cE '_?mbase="?\$\(git merge-base' || true)"
bare="${bare:-0}"
if [ "$bare" -eq 0 ]; then
  say "✅ MB-2" "every merge loop resolves its base via resolve_merge_base"
else
  bad MB-2 "$bare merge loop(s) still assign a bare \`git merge-base\` — empty on a copied fork"
fi

# ── MB-3 ─────────────────────────────────────────────────────────────────────
# Two distinct absences, and BOTH must be visible rather than silently resolved:
#   no repo merge base    a copied (non-forked) tree — resolve_merge_base falls back to the anchor
#   no per-FILE ancestor  add/add, where the file has no base blob even though the repo has a base
# The second is the subtle one: seeding base:=ours there makes every fork-owned key look like an
# upstream deletion, so the merge drops it and still reports "fork edits preserved". Keyed on the
# `: > "$_b"` empty-base seeding plus a warning, not on one literal string, so rewording the message
# does not silently disarm the check (which is exactly what happened when it read 'No merge base for').
if grep -qE 'No (merge base|common ancestor) for' "$ENGINE" \
   && grep -q ': > "\$_b"' "$ENGINE"; then
  say "✅ MB-3" "missing base (repo-level or per-file add/add) warns and seeds an EMPTY base"
else
  bad MB-3 "an absent base is silently seeded from ours — fork-only content is dropped as an 'upstream deletion' while the merge reports success"
fi

# ── MB-4 ─────────────────────────────────────────────────────────────────────
if grep -q 'is merge-owned — took the template' "$ENGINE"; then
  say "✅ MB-4" "binaries are excluded from the line merge"
else
  bad MB-4 "no binary guard in the merge loop — a 3-way on a .webp/.ico corrupts it silently"
fi

# ── MB-5 ─────────────────────────────────────────────────────────────────────
# Probe a real fork-wiring path per shell — the file that actually carries fork additions, not the
# directory, so a carve-out that accidentally reclaims it is caught too.
if [ -x "$CS" ] || [ -f "$CS" ]; then
  mb5=0
  for probe in \
      "cmp-ios/iosApp/Info.plist" \
      "cmp-ios/iosApp/iosApp.entitlements" \
      "cmp-ios/iosApp.xcodeproj/project.pbxproj" \
      "cmp-android/src/main/kotlin/cmp/android/app/MainActivity.kt" \
      "cmp-desktop/mac-app-store.entitlements" \
      "cmp-web/build.gradle.kts" \
      "cmp-ios/iosApp/iOSApp.swift" ; do
    got="$( (cd "$HEALTH_ROOT" && bash "$CS" resolve "$probe" 2>/dev/null) | awk '{print $2}' )"
    [ "$got" = "merge" ] || { bad MB-5 "$probe resolves '$got', expected merge"; mb5=1; }
  done
  [ "$mb5" -eq 0 ] && say "✅ MB-5" "the shell files carrying fork platform wiring resolve merge (fork wiring survives)"
  # MB-7: build-logic and the rest of the cmp-* shells are template code, replaced in full on sync; the
  # fork's build hook is the `kotlin/local/**` seam, which must stay the fork's.
  mb7=0
  for pair in "build-logic/convention/src/main/kotlin/SyncForkConfigPlugin.kt:template" \
              "build-logic/convention/build.gradle.kts:template" \
              "cmp-navigation/src/commonMain/kotlin/cmp/navigation/ComposeApp.kt:template" \
              "build-logic/convention/src/main/kotlin/org/convention/Detekt.kt:template" \
              "build-logic/convention/src/main/kotlin/local/LocalFlavors.kt:fork"; do
    probe="${pair%:*}"; want="${pair##*:}"
    got="$( (cd "$HEALTH_ROOT" && bash "$CS" resolve "$probe" 2>/dev/null) | awk '{print $2}' )"
    [ "$got" = "$want" ] || { bad MB-7 "$probe resolves '$got', expected $want"; mb7=1; }
  done
  [ "$mb7" -eq 0 ] && say "✅ MB-7" "build-logic + cmp-* shell code resolve template (replaced in full); the local/ seam stays fork"
else
  bad MB-5 "customization-surface.sh not found at $CS"
fi

# ── MB-6 ─────────────────────────────────────────────────────────────────────
if [ -f "$TV" ] && grep -qE '^template_sha=[0-9a-f]{7,40}$' "$TV"; then
  say "✅ MB-6" "template_sha anchor present ($(grep '^template_sha=' "$TV" | cut -d= -f2 | cut -c1-8))"
else
  bad MB-6 ".template-version has no parseable template_sha — a copied fork has no anchor to merge against"
fi

exit "$fail"
