#!/usr/bin/env bash
# bash4-required: it runs the engine's own functions, which read the associative EXCLUSIONS map.
# Functional test: a merge-owned file the template did NOT change since the last sync (BASE == THEIRS)
# is the fork's, so every strategy returns OURS byte-for-byte with no conflict.
#
# A re-serializing merger does not get there on its own: plist-union rebuilt the file around the
# template's comment, so a fork whose only delta was syncForkConfig's identity substitution (team id
# and app id written into cmp-desktop/mac-app-store.entitlements, comment included) was reported as
# "merge would DROP lines the fork added" — a conflict on every sync for a file nobody changed.
#
#   every strategy, BASE == THEIRS, fork edited                 → OURS, rc 0
#   the real sync_directory on that entitlements case          → fork's copy, no conflict recorded
set -uo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$HERE/../.." && pwd)"
ENGINE="$REPO/scripts/white-label/sync-dirs.sh"
WORK="$(mktemp -d)"; trap 'rm -rf "$WORK"' EXIT
fail=0; pass() { echo "  ✅ $1"; }; bad() { echo "  ❌ $1"; fail=1; }

F="$WORK/fork"; mkdir -p "$F/scripts/white-label" "$F/cmp-desktop"
cp "$REPO/scripts/customization-surface.sh" "$REPO/scripts/ruby-exec.sh" "$F/scripts/"
cp "$REPO/scripts/white-label/merge-plist.rb" "$REPO/scripts/white-label/merge-pbxproj.rb" "$F/scripts/white-label/"
cp "$REPO/.ruby-version" "$F/"
printf 'version: 1\nrules:\n  - glob: "cmp-desktop/*.entitlements"\n    owner: merge\n    strategy: plist-union\n  - glob: "**"\n    owner: template\n    default: true\n' > "$F/customization-surface.yaml"
printf 'SYNC_DIRS=(\n    "cmp-desktop"\n)\nSYNC_FILES=(\n)\n' > "$F/scripts/white-label/sync-dirs.sh"

# The engine's merge functions, sourced from a file under $F so their repo root (the mergers) is $F.
fns="$F/scripts/white-label/fns.sh"
{
  echo 'print_step(){ :; }; print_warning(){ :; }; print_error(){ :; }; show_progress(){ :; }; declare -A EXCLUSIONS=(); SYNC_CONFLICTS=(); SYNC_FAILED=(); SYNC_MERGED=()'
  for fn in owner_of is_held is_excluded fork_deleted_dir resolve_merge_base remove_stale_path propagate_template_deletions \
            mirror_template_owned migrated_from migrate_base merge_settings_include_union merge_libs_catalog_3way \
            merge_pbxproj_3way merge_plist_union merge_by_strategy sync_directory; do
    awk -v f="$fn" '$0 ~ "^"f"\\(\\) \\{" {p=1} p{print} p&&/^}/{p=0}' "$ENGINE"
  done
} > "$fns"
# Runs bash code inside the fixture with the reader and the engine's functions loaded.
run() {  # run <bash code> — in the fixture, with the reader and the engine's functions loaded
  ( cd "$F" && SCRIPT_DIR="$F/scripts/white-label" CS_CONTRACT="$F/customization-surface.yaml" MIGRATE_DIR="" \
      DRY_RUN=false MIRROR=false BASE_BRANCH=main FORK_STATE=fork PREV_TEMPLATE_SHA="${BASE:-}" \
      bash -c "source scripts/customization-surface.sh; source '$fns'; $1" )
}

echo "── every strategy: template unchanged → the fork's copy ──"
if ! grep -q '^merge_by_strategy() {' "$fns"; then
  bad "the engine has no single merge dispatcher (merge_by_strategy) — strategies are guarded unevenly"
else
  printf '# template comment\nkey=TEMPLATE\nother=1\n' > "$WORK/base"; cp "$WORK/base" "$WORK/theirs"
  printf '# fork comment\nkey=FORK\nother=1\nfork.only=1\n' > "$WORK/ours"
  for s in 3way kotlin-3way manifest-union strings-union xml-union properties-3way include-union \
           catalog-3way plist-union pbxproj-3way yaml-schema-merge; do
    rm -f "$WORK/out"
    run "merge_by_strategy $s '$WORK/ours' '$WORK/base' '$WORK/theirs' '$WORK/out'" >/dev/null 2>&1; rc=$?
    { [ "$rc" = 0 ] && cmp -s "$WORK/ours" "$WORK/out"; } && pass "$s → ours, rc 0" || bad "$s rc=$rc, output differs from ours"
  done
fi

echo "── sync_directory: entitlements whose only fork delta is the identity substitution ──"
cat > "$F/cmp-desktop/mac-app-store.entitlements" <<'XML'
<?xml version="1.0" encoding="UTF-8"?>
<plist version="1.0">
<!-- syncForkConfig substitutes YOUR_TEAM_ID and YOUR_TEAM_ID.com.example.app below -->
<dict>
    <key>com.apple.application-identifier</key>
    <string>YOUR_TEAM_ID.com.example.app</string>
    <key>com.apple.developer.team-identifier</key>
    <string>YOUR_TEAM_ID</string>
</dict>
</plist>
XML
# git in the fixture repo, with a throwaway identity.
g() { git -C "$F" -c user.email=t@t -c user.name=t "$@"; }
echo v1 > "$F/cmp-desktop/build.gradle.kts"
g init -q -b main; g add -A; g commit -q -m "last sync"; BASE="$(g rev-parse HEAD)"
g checkout -q -b fork
sed -i.bak 's/YOUR_TEAM_ID/L432S2FZP5/g; s/com\.example\.app/org.fork.app/g' "$F/cmp-desktop/mac-app-store.entitlements"
rm -f "$F/cmp-desktop/mac-app-store.entitlements.bak"; g commit -qam "syncForkConfig identity"
cp "$F/cmp-desktop/mac-app-store.entitlements" "$WORK/fork-copy"
g checkout -q -b tpl main; echo v2 > "$F/cmp-desktop/build.gradle.kts"; g commit -qam "template: unrelated change"
g checkout -q fork

conflicts="$(run "sync_directory cmp-desktop tpl >/dev/null 2>&1; printf '%s\n' \"\${SYNC_CONFLICTS[@]}\"")"
[ -z "$conflicts" ] && pass "no conflict reported for a file the template did not change" \
  || bad "phantom conflict: $conflicts"
cmp -s "$WORK/fork-copy" "$F/cmp-desktop/mac-app-store.entitlements" && pass "the fork's entitlements kept byte-for-byte" \
  || bad "the fork's entitlements changed"
[ "$(cat "$F/cmp-desktop/build.gradle.kts")" = v2 ] && pass "the template's other change still lands" || bad "template change lost"

[ "$fail" = 0 ] && echo "template-unchanged-test: PASS" || echo "template-unchanged-test: FAIL"
exit "$fail"
