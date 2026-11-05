#!/usr/bin/env bash
# Functional test for cs_merge_yaml_schema, the merge a sync runs over app-profile/*.yaml. Runs the
# REAL function from scripts/customization-surface.sh against a fork / base / template triple shaped
# like the 2026-11-01 field-officer sync, where the previous merge reported success after:
#   - replacing the fork's multi-line store copy with the template's demo copy
#   - emptying the fork's populated map because the template ships `links: {}`
#   - dropping a fork key the template only documents in a comment
#   - dropping every fork comment
#
#   Y-1 a fork block scalar (`|-` / `>-`) survives             Y-6 a template default change lands when the
#   Y-2 a fork map survives a template `{}`                         fork kept the old default
#   Y-3 a fork-only key survives                                Y-7 a fork customization beats a template change
#   Y-4 fork comments survive                                   Y-8 a key the fork deleted stays deleted
#   Y-5 a new template key arrives, with its comment            Y-9 a key the template retired goes
#                                                               Y-10 the result is valid YAML
# With a schema (app-profile/app.schema.yaml shape):
#   Y-11 a template-owned key takes the template value over a fork edit
#   Y-12 a retired key the fork CHANGED is still removed, and the removal is reported
#   Y-13 a key the fork added itself is never removed
#   Y-14 a list row the template owns (`owner: template`) follows the template; a fork row stays
set -uo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$HERE/../.." && pwd)"
WORK="$(mktemp -d)"; trap 'rm -rf "$WORK"' EXIT
fail=0; pass() { echo "  ✅ $1"; }; bad() { echo "  ❌ $1"; fail=1; }
# shellcheck disable=SC1091
. "$REPO/scripts/customization-surface.sh" 2>/dev/null || { echo "  ❌ cannot source customization-surface.sh"; exit 1; }

cat > "$WORK/base.yaml" <<'YAML'
network:
  demo_base_url: https://demo.example.com
  log_tag: App
org:
  name: Your Organization
  community_url: ""
  links: {}
store:
  description: 'Template demo copy.'
YAML
cat > "$WORK/ours.yaml" <<'YAML'
# Fork identity — kept in sync with the Play listing.
network:
  # demo_base_url removed: its host times out.
  log_tag: FieldApp
org:
  name: Mifos Initiative
  community_url: ""
  links:
    community: https://chat.example.org/
store:
  description: |-
    Field operations for loan officers.

    Works offline.
security:
  signed_in_by_default: false
YAML
cat > "$WORK/theirs.yaml" <<'YAML'
network:
  demo_base_url: https://demo.example.com
  log_tag: Toolkit
  # Network privacy: what a release build may log.
  logging:
    release: none
org:
  name: Your Organization
  links: {}
store:
  description: 'Newer template demo copy.'
security:
  # signed_in_by_default: ABSENT means true (the template has no login).
  screen_capture_allowed_by_default: false
YAML

( CS_ROOT="$REPO" cs_merge_yaml_schema "$WORK/ours.yaml" "$WORK/base.yaml" "$WORK/theirs.yaml" "$WORK/out.yaml" ) \
  || { echo "  ❌ merge failed"; exit 1; }
O="$WORK/out.yaml"
has() { grep -qF -- "$1" "$O"; }

has 'Field operations for loan officers.' && has 'Works offline.' && ! has 'demo copy' \
  && pass "Y-1 fork block scalar kept, template copy not taken" || bad "Y-1 fork block scalar lost"
has 'community: https://chat.example.org/' && ! has 'links: {}' \
  && pass "Y-2 fork map kept over the template's {}" || bad "Y-2 fork map emptied"
has 'signed_in_by_default: false' && pass "Y-3 fork-only key kept" || bad "Y-3 fork-only key dropped"
has '# Fork identity' && has '# demo_base_url removed' && pass "Y-4 fork comments kept" || bad "Y-4 fork comments dropped"
has 'release: none' && has '# Network privacy' && has 'screen_capture_allowed_by_default: false' \
  && pass "Y-5 new template keys arrive with their comments" || bad "Y-5 new template key missing"
# Y-6 needs a key the fork left at the base default: give it one and re-merge.
sed 's/log_tag: FieldApp/log_tag: App/' "$WORK/ours.yaml" > "$WORK/ours6.yaml"
( CS_ROOT="$REPO" cs_merge_yaml_schema "$WORK/ours6.yaml" "$WORK/base.yaml" "$WORK/theirs.yaml" "$WORK/out6.yaml" )
grep -qF 'log_tag: Toolkit' "$WORK/out6.yaml" && pass "Y-6 template default change lands on an untouched key" \
  || bad "Y-6 template default change not applied"
has 'log_tag: FieldApp' && has 'name: Mifos Initiative' && pass "Y-7 fork customizations beat template changes" \
  || bad "Y-7 a fork customization was overwritten"
! grep -q '^ *demo_base_url:' "$O" && pass "Y-8 key the fork deleted stays deleted" || bad "Y-8 deleted key came back"
! has 'community_url' && pass "Y-9 key the template retired is dropped" || bad "Y-9 retired key kept"
if command -v ruby >/dev/null 2>&1; then
  ruby -ryaml -e 'YAML.load_file(ARGV[0])' "$O" 2>/dev/null && pass "Y-10 result is valid YAML" || bad "Y-10 result is not valid YAML"
fi

# ── schema-driven ownership ──
cat > "$WORK/schema.yaml" <<'YAML'
default: fork
keys:
  network.log_tag: template
YAML
{ sed 's|community_url: ""|community_url: https://forum.example.org|' "$WORK/ours.yaml"; echo 'fork_extra: kept'; } > "$WORK/ours11.yaml"
( CS_ROOT="$REPO" CS_YAML_SCHEMA="$WORK/schema.yaml" cs_merge_yaml_schema "$WORK/ours11.yaml" "$WORK/base.yaml" "$WORK/theirs.yaml" "$WORK/out11.yaml" ) 2> "$WORK/err11"
grep -qF 'log_tag: Toolkit' "$WORK/out11.yaml" && ! grep -qF 'log_tag: FieldApp' "$WORK/out11.yaml" \
  && pass "Y-11 template-owned key takes the template value over a fork edit" || bad "Y-11 template-owned key kept the fork value"
! grep -q community_url "$WORK/out11.yaml" && grep -q 'retired key org.community_url removed (fork had: community_url: https://forum.example.org)' "$WORK/err11" \
  && pass "Y-12 retired key removed even though the fork changed it, and reported" || bad "Y-12 retired key kept or removed silently"
grep -qF 'fork_extra: kept' "$WORK/out11.yaml" && pass "Y-13 a key the fork added itself stays" || bad "Y-13 fork extension removed"

# ── row-level ownership in a union list ──
cat > "$WORK/ap-base.yaml" <<'YAML'
network:
  access_points:
    - id: shared
      owner: template
      anon_key_env: OLD_KEY
YAML
cat > "$WORK/ap-ours.yaml" <<'YAML'
network:
  access_points:
    - id: shared
      owner: template
      anon_key_env: OLD_KEY
    - id: main
      owner: fork
      base_url: https://fork.example.org/
YAML
cat > "$WORK/ap-theirs.yaml" <<'YAML'
network:
  access_points:
    - id: shared
      owner: template
      publishable_key_env: NEW_KEY
    - id: main
      owner: fork
      base_url: https://template.example.org/
YAML
( CS_ROOT="$REPO" cs_merge_yaml_schema "$WORK/ap-ours.yaml" "$WORK/ap-base.yaml" "$WORK/ap-theirs.yaml" "$WORK/ap-out.yaml" )
grep -qF 'publishable_key_env: NEW_KEY' "$WORK/ap-out.yaml" && ! grep -qF OLD_KEY "$WORK/ap-out.yaml" \
  && grep -qF 'fork.example.org' "$WORK/ap-out.yaml" && ! grep -qF 'template.example.org' "$WORK/ap-out.yaml" \
  && [ "$(grep -c 'id: shared' "$WORK/ap-out.yaml")" = 1 ] \
  && pass "Y-14 template-owned row follows the template; the fork row stays" || bad "Y-14 row-level ownership wrong"

[ "$fail" = 0 ] && echo "yaml-schema-merge-test: PASS" || echo "yaml-schema-merge-test: FAIL"
exit "$fail"
