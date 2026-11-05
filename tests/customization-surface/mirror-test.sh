#!/usr/bin/env bash
# bash4-required: it runs the engine's own functions, which read the associative EXCLUSIONS map.
# Functional test for sync-dirs.sh --mirror (mirror_template_owned). Runs the REAL function, extracted
# from the engine, against a throwaway git repo and the REAL ownership reader.
#
#   template-owned, fork-only        → removed        (the overlay defect --mirror exists to close)
#   template-owned, held             → kept           (.template-sync-holds — proposed upstream)
#   fork-owned (contract)            → kept
#   demo (convention)                → kept
#   template has it                  → untouched by the mirror (the overlay already wrote it)
#   --mirror absent                  → nothing removed (opt-in)
set -uo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$HERE/../.." && pwd)"
ENGINE="$REPO/scripts/white-label/sync-dirs.sh"
WORK="$(mktemp -d)"; trap 'rm -rf "$WORK"' EXIT
fail=0; pass() { echo "  ✅ $1"; }; bad() { echo "  ❌ $1"; fail=1; }

F="$WORK/fork"; mkdir -p "$F/scripts/white-label"
cp "$REPO/scripts/customization-surface.sh" "$F/scripts/"
cat > "$F/customization-surface.yaml" <<'YAML'
version: 1
rules:
  - glob: "core-base/**/fork-owned/**"
    owner: fork
  - glob: "**/demo/**"
    owner: demo-showcase
  - glob: "core-base/**"
    owner: template
  - glob: "**"
    owner: template
    default: true
YAML
printf 'SYNC_DIRS=(\n    "core-base"\n)\nSYNC_FILES=(\n)\n' > "$F/scripts/white-label/sync-dirs.sh"

g() { git -C "$F" -c user.email=t@t -c user.name=t "$@"; }
mk() { mkdir -p "$(dirname "$F/$1")"; echo "$2" > "$F/$1"; }
g init -q -b main
mk core-base/a/Shared.kt template-v1
mk core-base/a/ForkExtra.kt fork-only
mk core-base/a/Held.kt fork-only-held
mk core-base/a/fork-owned/Mine.kt fork-territory
mk core-base/a/demo/Demo.kt demo
printf 'core-base/a/Held.kt\tproposed upstream: openMF/kmp-project-template#999\n' > "$F/.template-sync-holds"
g add -A; g commit -q -m fork
g checkout -q -b tpl; g rm -q -r --cached core-base >/dev/null; rm -rf "$F/core-base"
mk core-base/a/Shared.kt template-v2; g add -A; g commit -q -m template; g checkout -q main

# Pull the real functions out of the engine; stub only the printing helpers.
fnsrc="$WORK/fns.sh"
{
  echo 'print_step(){ :; }; print_warning(){ :; }; declare -A EXCLUSIONS=()'
  awk '/^(owner_of|is_held|is_excluded)\(\) \{/{p=1} p{print} p&&/^}/{p=0}' "$ENGINE"
  awk '/^(mirror_template_owned|remove_stale_path)\(\) \{/{p=1} p{print} p&&/^}/{p=0}' "$ENGINE"
} > "$fnsrc"
grep -q 'mirror_template_owned()' "$fnsrc" || { echo "  ❌ mirror_template_owned not found in engine"; exit 1; }

run() {  # run <MIRROR>
  ( cd "$F" && SCRIPT_DIR="$F/scripts/white-label" BASE_BRANCH=main FORK_STATE=main MIRROR="$1" bash -c "
      $(cat "$fnsrc")
      mirror_template_owned core-base tpl" )
}

echo "── without --mirror (opt-in) ──"
run false
[ -f "$F/core-base/a/ForkExtra.kt" ] && pass "nothing removed without --mirror" || bad "removed without --mirror"

echo "── with --mirror ──"
run true
[ ! -e "$F/core-base/a/ForkExtra.kt" ] && pass "template-owned fork-only file removed" || bad "fork-only extra survived"
[ -f "$F/core-base/a/Held.kt" ]        && pass "held file kept (.template-sync-holds)" || bad "held file removed"
[ -f "$F/core-base/a/fork-owned/Mine.kt" ] && pass "contract fork-owned path kept"     || bad "fork-owned path removed"
[ -f "$F/core-base/a/demo/Demo.kt" ]   && pass "demo convention path kept"            || bad "demo removed"
[ -f "$F/core-base/a/Shared.kt" ]      && pass "file the template has is not touched" || bad "shared file removed"

echo "── a hold also protects a template file from the OVERLAY ──"
# The directory sync restores every path is_excluded flags. A held file the template also ships (a
# fork fix proposed upstream) must be flagged, or the overlay overwrites the fix it is waiting on.
excluded() {  # excluded <path> → 0 when is_excluded preserves it
  ( cd "$F" && SCRIPT_DIR="$F/scripts/white-label" bash -c "$(cat "$fnsrc")
      is_excluded core-base '$1' file" )
}
printf 'core-base/a/Shared.kt\tproposed upstream: openMF/kmp-project-template#998\n' >> "$F/.template-sync-holds"
excluded core-base/a/Shared.kt && pass "held template file is preserved from the overlay" \
                               || bad "held template file would be overwritten"
excluded core-base/a/Other.kt  && bad "an unheld template path was preserved" \
                               || pass "an unheld template path is still synced"

echo "── a hold also protects a merge-owned file from the 3-WAY MERGE ──"
# Both merge loops (per-directory merge-owned files, root files) must consult is_held BEFORE merging:
# holds were honoured by the overlay and the mirror but not by the merge, and a held project.pbxproj
# was merged against a stale base. Structural, because the loops live inside sync_directory.
for loop in '_mf' 'f'; do
  body="$(awk -v v="$loop" '$0 ~ "cs_match_g \"\\$" v "\"" {p=1} p{print} p && /cs_merge /{exit}' "$ENGINE")"
  printf '%s\n' "$body" | grep -q "is_held \"\\\$$loop\"" \
    && pass "merge loop over \$$loop checks holds before merging" \
    || bad "merge loop over \$$loop merges a held path"
done

[ "$fail" = 0 ] && echo "mirror-test: PASS" || echo "mirror-test: FAIL"
exit "$fail"
