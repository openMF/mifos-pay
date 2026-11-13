#!/usr/bin/env bash
# run.sh — canary for sync-dirs.sh SELF-HEAL on a stale / pre-white-label fork. Two heals:
#
#   A) STEP-0 merge-protection bootstrap — is_excluded()'s fork-ownership check AND the 3-way merge
#      engine both silently NO-OP when the contract (customization-surface.yaml + scripts/
#      customization-surface.sh) is absent. Since `scripts/` sits mid-SYNC_DIRS and the yaml is a
#      SYNC_FILE (after every dir), a not-yet-white-labelled fork clobbers merge-owned files
#      (AndroidManifest.xml → dropped CAMERA/RECORD_AUDIO). Fix: bootstrap the contract BEFORE the loop.
#
#   B) SELF-UPDATE + RE-EXEC — a fork runs its OWN (possibly stale) engine copy, which only self-
#      propagates MID-loop, too late. Fix: if the template's engine differs, commit the fresh engine
#      onto a new sync branch and re-exec so the whole run uses the latest engine (tree stays clean →
#      no bash re-read / checkout hazard).
set -uo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SYNC="$(cd "$HERE/../../../white-label" && pwd)/sync-dirs.sh"
rc=0

# ══ A-STATIC: bootstrap precedes the dir loop ══════════════════════════════════════════
boot="$(grep -n 'bootstrapped merge-protection contract' "$SYNC" | head -1 | cut -d: -f1)"
loop="$(grep -n 'for dir in "${SYNC_DIRS\[@\]}"' "$SYNC" | tail -1 | cut -d: -f1)"
if [ -n "$boot" ] && [ -n "$loop" ] && [ "$boot" -lt "$loop" ]; then
  echo "   ✅ A-STATIC: STEP-0 bootstrap (line $boot) precedes the SYNC_DIRS apply loop (line $loop)"
else
  echo "   ❌ A-STATIC: bootstrap must precede the SYNC_DIRS loop (boot=$boot loop=$loop)"; rc=1
fi

# ══ B-STATIC: self-update block present, guarded, precedes the branch dance ═════════════
su="$(grep -n 'SYNC_DIRS_SELF_UPDATED=1 .*SYNC_PREBUILT_BRANCH=' "$SYNC" | head -1 | cut -d: -f1)"
dance="$(grep -n 'git checkout -b "\$TEMP_BRANCH"' "$SYNC" | head -1 | cut -d: -f1)"
if [ -n "$su" ] && [ -n "$dance" ] && [ "$su" -lt "$dance" ] \
   && grep -q 'exec bash "\$_self_rel"' "$SYNC" && grep -q 'SYNC_DIRS_SELF_UPDATED:-0' "$SYNC"; then
  echo "   ✅ B-STATIC: self-update re-exec (line $su, guarded) precedes the branch dance (line $dance)"
else
  echo "   ❌ B-STATIC: guarded self-update re-exec must precede the branch dance (su=$su dance=$dance)"; rc=1
fi

# ══ A-DYNAMIC: the bootstrap loop restores an absent contract from the template ref ═════
work="$(mktemp -d)"
(
  cd "$work"; git init -q; git config user.email t@t; git config user.name t
  mkdir -p scripts; echo "version: 1" > customization-surface.yaml
  echo "#!/usr/bin/env bash" > scripts/customization-surface.sh
  git add -A; git commit -qm tmpl; git branch tmpl
  rm -f customization-surface.yaml scripts/customization-surface.sh
  TEMP_BRANCH=tmpl
  for _bp in customization-surface.yaml scripts/customization-surface.sh; do
    git cat-file -e "$TEMP_BRANCH:$_bp" 2>/dev/null && { mkdir -p "$(dirname "$_bp")"; git show "$TEMP_BRANCH:$_bp" > "$_bp" 2>/dev/null; }
  done
  [ -f customization-surface.yaml ] && [ -f scripts/customization-surface.sh ]
) && echo "   ✅ A-DYNAMIC: bootstrap restored the absent contract from the template ref" \
  || { echo "   ❌ A-DYNAMIC: bootstrap failed to restore the contract"; rc=1; }
rm -rf "$work"

# ══ B-DYNAMIC: a stale engine self-updates + re-execs onto the fresh one ════════════════
w2="$(mktemp -d)"; tpl="$w2/tpl"; fork="$w2/fork"
BLK='ORIGINAL_ARGS=("$@"); TEMPLATE_REMOTE=template; BASE_BRANCH=dev; DRY_RUN=false
gsbn(){ echo "sync-test-$$"; }
_self_rel="engine.sh"
if [ "${SYNC_DIRS_SELF_UPDATED:-0}" != "1" ] && [ "$DRY_RUN" = false ] && git cat-file -e "$TEMPLATE_REMOTE/$BASE_BRANCH:$_self_rel" 2>/dev/null; then
  _t=$(git rev-parse "$TEMPLATE_REMOTE/$BASE_BRANCH:$_self_rel" 2>/dev/null||echo ""); _l=$(git hash-object "$_self_rel" 2>/dev/null||echo "")
  if [ -n "$_t" ] && [ "$_t" != "$_l" ] && git diff --quiet && git diff --cached --quiet; then
    _b=$(gsbn)
    if git checkout -b "$_b" "$BASE_BRANCH" >/dev/null 2>&1; then
      git show "$TEMPLATE_REMOTE/$BASE_BRANCH:$_self_rel" > "$_self_rel"; git add "$_self_rel" >/dev/null 2>&1
      git commit -qm self-update >/dev/null 2>&1 || true
      SYNC_DIRS_SELF_UPDATED=1 SYNC_PREBUILT_BRANCH="$_b" exec bash "$_self_rel" ${ORIGINAL_ARGS[@]+"${ORIGINAL_ARGS[@]}"}
    fi; fi; fi'
mkdir -p "$tpl"; ( cd "$tpl"; git init -qb dev; git config user.email t@t; git config user.name t
  { echo '#!/bin/bash'; echo "$BLK"; echo 'echo "ENGINE=v2 guard=${SYNC_DIRS_SELF_UPDATED:-0}"'; } > engine.sh
  git add -A; git commit -qm v2 )
git clone -q "$tpl" "$fork"
out="$( cd "$fork"; git config user.email t@t; git config user.name t   # CI has no global git identity
        git remote rename origin template >/dev/null 2>&1; git checkout -qb work
        sed 's/ENGINE=v2/ENGINE=v1/' "$tpl/engine.sh" > engine.sh; git add -A; git commit -qm v1 >/dev/null 2>&1
        bash engine.sh 2>&1 )"
if echo "$out" | grep -q 'ENGINE=v2' && echo "$out" | grep -q 'guard=1'; then
  echo "   ✅ B-DYNAMIC: stale engine self-updated + re-exec'd onto the fresh one (guard set)"
else
  echo "   ❌ B-DYNAMIC: stale engine did not re-exec fresh (got: $out)"; rc=1
fi
rm -rf "$w2"

exit "$rc"
