#!/usr/bin/env bash
# bash4-required: it runs the engine's own functions, which read the associative EXCLUSIONS map.
# Functional test: a FORK-owned file the template ships for the first time reaches the fork as a seed;
# one the fork deliberately deleted stays deleted. Runs the REAL sync_directory with the REAL ownership
# reader. Before the fix both were "Dropped upstream-added (fork removed it)": a fork never received
# spotless/license-header.txt, and spotlessApply then stripped the license header from ~2,500 files.
#
#   new fork-owned seam (absent at the last sync)         → seeded with the template's copy
#   fork-owned file present at the last sync, fork deleted → stays deleted
#   new fork-owned seam in a directory that is new upstream too              → seeded
#   new fork-owned file in a package the fork deleted (shipped at the last sync) → not seeded
#   fork-owned Room schema (the fork's own lineage)                        → not seeded
set -uo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$HERE/../.." && pwd)"
ENGINE="$REPO/scripts/white-label/sync-dirs.sh"
WORK="$(mktemp -d)"; trap 'rm -rf "$WORK"' EXIT
fail=0; pass() { echo "  ✅ $1"; }; bad() { echo "  ❌ $1"; fail=1; }

F="$WORK/fork"; mkdir -p "$F/scripts/white-label"
cp "$REPO/scripts/customization-surface.sh" "$F/scripts/"
printf 'version: 1\nrules:\n  - glob: "spotless/license-header.txt"\n    owner: fork\n  - glob: "spotless/old-seam.txt"\n    owner: fork\n  - glob: "spotless/samplepkg/**"\n    owner: fork\n  - glob: "spotless/app/fork/**"\n    owner: fork\n  - glob: "spotless/schemas/**"\n    owner: fork\n  - glob: "**"\n    owner: template\n    default: true\n' > "$F/customization-surface.yaml"
printf 'SYNC_DIRS=(\n    "spotless"\n)\nSYNC_FILES=(\n)\n' > "$F/scripts/white-label/sync-dirs.sh"

# git, run in the fixture repo with a fixed identity.
g() { git -C "$F" -c user.email=t@t -c user.name=t "$@"; }
mkdir -p "$F/spotless/samplepkg"; echo keep > "$F/spotless/copyright.kt"; echo seam > "$F/spotless/old-seam.txt"; echo old > "$F/spotless/samplepkg/Old.kt"
g init -q -b main; g add -A; g commit -q -m "last sync"; BASE="$(g rev-parse HEAD)"
g checkout -q -b fork; g rm -q spotless/old-seam.txt; g rm -rq spotless/samplepkg; g commit -q -m "fork deletes its old seam + the sample package"
g checkout -q -b tpl main; mkdir -p "$F/spotless/samplepkg/sub" "$F/spotless/schemas" "$F/spotless/app/fork"
echo doc > "$F/spotless/samplepkg/README.md"; echo new > "$F/spotless/samplepkg/sub/New.kt"; echo {} > "$F/spotless/schemas/12.json"
echo 'Copyright $YEAR $ORG_NAME' > "$F/spotless/license-header.txt"; echo 'fun onForkCreate() {}' > "$F/spotless/app/fork/Hooks.kt"
g add -A; g commit -q -m "template: new fork seams"
g checkout -q fork

fnsrc="$WORK/fns.sh"
{
  echo 'print_step(){ :; }; print_warning(){ :; }; print_error(){ :; }; show_progress(){ :; }; declare -A EXCLUSIONS=(); SYNC_CONFLICTS=(); SYNC_FAILED=()'
  for fn in owner_of is_held is_excluded fork_deleted_dir resolve_merge_base remove_stale_path propagate_template_deletions mirror_template_owned sync_directory; do
    awk -v f="$fn" '$0 ~ "^"f"\\(\\) \\{" {p=1} p{print} p&&/^}/{p=0}' "$ENGINE"
  done
} > "$fnsrc"
grep -q 'sync_directory()' "$fnsrc" || { echo "  ❌ sync_directory not found in engine"; exit 1; }

( cd "$F" && SCRIPT_DIR="$F/scripts/white-label" BASE_BRANCH=main FORK_STATE=fork PREV_TEMPLATE_SHA="$BASE" \
    DRY_RUN=false MIRROR=true bash -c "$(cat "$fnsrc")
    sync_directory spotless tpl" ) >/dev/null 2>&1

[ -f "$F/spotless/license-header.txt" ] && pass "new fork-owned seam seeded from the template" \
  || bad "new fork-owned seam dropped (spotless/license-header.txt)"
[ ! -f "$F/spotless/old-seam.txt" ] && pass "fork-owned file the fork deleted stays deleted" \
  || bad "the fork's deletion was undone (spotless/old-seam.txt came back)"

# A seam template-owned code calls (AndroidApp.onCreate -> onForkCreate) ships in a directory that is
# new upstream; dropping it left the fork unable to compile.
[ -f "$F/spotless/app/fork/Hooks.kt" ] && pass "new fork-owned seam in a new directory seeded" \
  || bad "new seam in a new directory dropped (spotless/app/fork/Hooks.kt)"
[ ! -e "$F/spotless/samplepkg/README.md" ] && [ ! -e "$F/spotless/samplepkg/sub/New.kt" ] \
  && pass "new file in a package the fork deleted is not seeded" \
  || bad "a package the fork deleted was re-seeded (spotless/samplepkg/)"
[ ! -e "$F/spotless/schemas/12.json" ] && pass "a Room schema is not seeded into the fork's lineage" \
  || bad "a template schema was seeded (spotless/schemas/12.json)"

# The R8 rules seam: proguard-rules.pro is template-owned (replaced every sync), so the fork's own keep
# rules need a fork-owned file the release build actually applies.
[ "$( (cd "$REPO" && bash scripts/customization-surface.sh resolve cmp-android/fork-proguard-rules.pro) | cut -f2)" = fork ] \
  && [ -f "$REPO/cmp-android/fork-proguard-rules.pro" ] \
  && [ "$(grep -c '"fork-proguard-rules.pro"' "$REPO/cmp-android/build.gradle.kts")" -ge 2 ] \
  && pass "R8 seam: fork-proguard-rules.pro is fork-owned, shipped as a seed, applied by staging + release" \
  || bad "R8 seam missing — a fork's keep rules have nowhere to live that a sync keeps"

# The Android app-shell seam: a fork's deps/placeholders and its Application startup have fork-owned homes
# that the template-owned shell actually reaches.
own() { (cd "$REPO" && bash scripts/customization-surface.sh resolve "$1") | cut -f2; }
[ "$(own cmp-android/module-deps.gradle.kts)" = fork ] \
  && grep -q 'file("module-deps.gradle.kts").takeIf { it.exists() }?.let { apply(from = it) }' "$REPO/cmp-android/build.gradle.kts" \
  && pass "Android deps seam: cmp-android/module-deps.gradle.kts is fork-owned and applied" \
  || bad "Android deps seam missing"
[ "$(own cmp-android/src/main/kotlin/cmp/android/app/fork/ForkAndroidHooks.kt)" = fork ] \
  && grep -q '^fun Application.onForkCreate()' "$REPO/cmp-android/src/main/kotlin/cmp/android/app/fork/ForkAndroidHooks.kt" \
  && grep -q '^        onForkCreate()' "$REPO/cmp-android/src/main/kotlin/cmp/android/app/AndroidApp.kt" \
  && pass "Android startup seam: ForkAndroidHooks is fork-owned, seeded, called from AndroidApp.onCreate" \
  || bad "Android startup seam missing"
# The iOS platform-bridge seam: Swift-only objects (a StoreKit 2 shim) reach the fork's Kotlin hook, and
# the Swift call site is merged, so a fork's argument survives a sync.
[ "$(own cmp-shared/src/nativeMain/kotlin/kpt/shared/fork/ForkIosHooks.kt)" = fork ] \
  && [ "$(own cmp-ios/iosApp/ContentView.swift)" = merge ] \
  && grep -q 'platformBridges.onForkIosConfigure()' "$REPO/cmp-shared/src/nativeMain/kotlin/kpt/shared/ViewController.kt" \
  && grep -q 'viewController(platformBridges:' "$REPO/cmp-ios/iosApp/ContentView.swift" \
  && pass "iOS bridge seam: ForkIosHooks fork-owned, called by viewController, ContentView merge-owned" \
  || bad "iOS bridge seam missing"
# Firebase on iOS: the fork's plist is fork-owned, bundled when present, and configured only then.
[ "$(own cmp-ios/iosApp/firebase/GoogleService-Info.plist)" = fork ] \
  && grep -q 'iosApp/firebase/GoogleService-Info.plist' "$REPO/cmp-ios/scripts/copy-compose-resources.sh" \
  && grep -q 'FirebaseApp.configure()' "$REPO/cmp-ios/iosApp/iOSApp.swift" \
  && pass "iOS Firebase seam: fork plist bundled when present, FirebaseApp.configure() guarded" \
  || bad "iOS Firebase seam missing"

[ "$fail" = 0 ] && echo "seam-seed-test: PASS" || echo "seam-seed-test: FAIL"
exit "$fail"
