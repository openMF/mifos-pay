#!/usr/bin/env bash
# scripts/configure-release-environments.sh
#
# Creates + protects the GitHub Environments the multi-platform release pipeline
# binds to, so that store/distribution stages PAUSE for a manual "Approve and
# deploy" before publishing. Run this once per repo that DISPATCHES releases.
#
# WHY THIS EXISTS
#   The publish-* reusable workflows declare `environment: <name>` on every
#   store stage (e.g. publish-android-kmp/release.yaml → `android-play-internal`).
#   GitHub resolves that environment against the REPO WHERE THE RUN EXECUTES.
#   If the environment is absent OR has no required-reviewer rule there, GitHub
#   runs the job immediately — NO approval prompt. (Observed empirically: a run
#   on openMF/kmp-project-template went straight to the Play upload and failed,
#   `approvals: []`, because openMF's `android-play-internal` had 0 reviewers.)
#   Configuring required reviewers here is what turns the stage into a gate.
#
# Prerequisites:
#   - gh CLI installed and authenticated (gh auth login)
#   - You MUST have ADMIN on the target repo (environment protection rules are
#     an admin-only API — write/maintain is NOT enough; you'll get HTTP 403
#     "Must have admin rights to Repository.").
#
# Usage:
#   bash scripts/configure-release-environments.sh --repo owner/repo --reviewer-team <org>/<slug>
#   bash scripts/configure-release-environments.sh --repo owner/repo --reviewer <login-or-id>
#   bash scripts/configure-release-environments.sh --repo openMF/kmp-project-template --reviewer-team openMF/mobile-release-approvers
#   bash scripts/configure-release-environments.sh --repo owner/repo --reviewer 5724482 --dry-run
#   bash scripts/configure-release-environments.sh --repo owner/repo --reviewer-team openMF/mobile-release-approvers --only android,ios
#   bash scripts/configure-release-environments.sh --repo owner/repo --reviewer-team openMF/mobile --reviewer me   # team + a fallback individual
#
# Flags:
#   --repo owner/repo        Target repo (REQUIRED). Where releases are dispatched.
#   --reviewer-team <org>/<slug>
#                            Required-reviewer TEAM added to every gated environment.
#                            RECOMMENDED over --reviewer: any member of the team can
#                            approve, so there is no single-approver bottleneck and the
#                            gate survives personnel changes (resilient over time). The
#                            team needs >=write access on the target repo.
#   --reviewer <id|login>    Required-reviewer USER added to every gated environment.
#                            Accepts a numeric user id, a @login, or the literal `me`
#                            (resolves to the authenticated user). The user needs >=write
#                            access on the target repo.
#
#                            At least one of --reviewer-team / --reviewer is REQUIRED;
#                            both may be given (a team plus a named fallback reviewer).
#                            Prefer --reviewer-team for shared/org repos.
#   --only <list>            Comma-separated platform subset to gate:
#                            android,ios,mac,desktop,web (default: all).
#   --desktop-target <slug>  Desktop artifact slug used in env names
#                            `desktop-<slug>-{prerelease,beta,stable}`
#                            (default: linux-deb). Repeatable via comma list.
#   --web-host <slug>        Web host slug used in env names
#                            `web-<slug>-{preview,staging,production}`
#                            (default: gh-pages). Repeatable via comma list.
#   --wait-prod <minutes>    Optional wait timer (minutes) on the top rung
#                            (production / app-store / stable) (default: 0).
#   --production-only        Gate ONLY the external / production-facing stages
#                            (closed/beta/production, testflight-external, app-store,
#                            web staging/production, desktop beta/stable). Leaves the
#                            internal / firebase / preview / prerelease TEST channels
#                            ungated so routine test builds don't need an approval
#                            click. RECOMMENDED for most teams.
#   --dry-run                Print what would change; make no API calls.
#
# Firebase note: `android-firebase` / `ios-firebase` (App Distribution) ARE gated by
# default — Stage 0 is the FIRST deploy point, so gating it makes EVERY rung require an
# approval click. The publish-* ladder workflows already declare
# `environment: <platform>-firebase` on the Stage-0 job, so setting these reviewers is
# all that's needed. Pass --production-only to leave Firebase (and the other
# internal/preview/prerelease TEST channels) ungated.
#
# Team vs individual: prefer --reviewer-team for shared/org repos. A team reviewer
# means any team member can approve (no single point of failure), the gate keeps
# working across personnel changes, and — because THIS script sets the team on the
# environment — the config never drifts from what the org intends (vs. adding the
# team by hand after a --reviewer run, which leaves script + environment out of sync).
#
# Idempotent: re-running re-applies the same reviewer set (PUT replaces config).

set -euo pipefail

REPO=""
REVIEWER=""
REVIEWER_TEAM=""
ONLY="android,ios,mac,desktop,web"
DESKTOP_TARGETS="linux-deb"
WEB_HOSTS="gh-pages"
WAIT_PROD=0
DRY_RUN=false
PRODUCTION_ONLY=false

# ── Parse args ────────────────────────────────────────────────────────────────
while [[ $# -gt 0 ]]; do
  case "$1" in
    --repo)           REPO="$2"; shift ;;
    --reviewer)       REVIEWER="$2"; shift ;;
    --reviewer-team)  REVIEWER_TEAM="$2"; shift ;;
    --only)           ONLY="$2"; shift ;;
    --desktop-target) DESKTOP_TARGETS="$2"; shift ;;
    --web-host)       WEB_HOSTS="$2"; shift ;;
    --wait-prod)      WAIT_PROD="$2"; shift ;;
    --production-only) PRODUCTION_ONLY=true ;;
    --dry-run)        DRY_RUN=true ;;
    -h|--help)        sed -n '2,79p' "$0"; exit 0 ;;
    *) echo "❌ Unknown arg: $1" >&2; exit 2 ;;
  esac
  shift
done

[[ -z "$REPO" ]] && { echo "❌ --repo owner/repo is required" >&2; exit 2; }
[[ -z "$REVIEWER" && -z "$REVIEWER_TEAM" ]] && {
  echo "❌ at least one of --reviewer-team <org>/<slug> or --reviewer <id|login|me> is required" >&2
  echo "   (prefer --reviewer-team for shared/org repos — no single-approver bottleneck)" >&2
  exit 2
}

command -v gh >/dev/null || { echo "❌ gh CLI not found — install + 'gh auth login'" >&2; exit 3; }

# ── Resolve reviewers → the `reviewers` JSON array ────────────────────────────
# GitHub's environment protection API accepts reviewers of type User AND Team
# ({"type":"Team","id":<team_id>}). We resolve whichever of --reviewer / --reviewer-team
# was given into numeric ids and build one reviewers array reused for every environment.
resolve_reviewer_id () {
  local r="$1"
  if [[ "$r" == "me" ]]; then
    gh api user -q '.id'
  elif [[ "$r" =~ ^[0-9]+$ ]]; then
    echo "$r"
  else
    gh api "users/${r#@}" -q '.id'
  fi
}

declare -a REVIEWER_ENTRIES=()   # JSON objects: {"type":"Team|User","id":N}
REVIEWER_LABEL=""                # human-readable summary line

if [[ -n "$REVIEWER_TEAM" ]]; then
  [[ "$REVIEWER_TEAM" == */* ]] || { echo "❌ --reviewer-team must be <org>/<slug> (e.g. openMF/mobile-release-approvers)" >&2; exit 2; }
  TEAM_ORG="${REVIEWER_TEAM%%/*}"
  TEAM_SLUG="${REVIEWER_TEAM##*/}"
  TEAM_ID="$(gh api "orgs/${TEAM_ORG}/teams/${TEAM_SLUG}" -q '.id' 2>/dev/null || true)"
  [[ -z "$TEAM_ID" ]] && {
    echo "❌ Could not resolve team '$REVIEWER_TEAM' to a team id." >&2
    echo "   Check the <org>/<slug> is correct and your token can read the org's teams." >&2
    exit 4
  }
  REVIEWER_ENTRIES+=("$(printf '{"type":"Team","id":%s}' "$TEAM_ID")")
  REVIEWER_LABEL="team ${REVIEWER_TEAM} (id=${TEAM_ID})"
fi

if [[ -n "$REVIEWER" ]]; then
  REVIEWER_ID="$(resolve_reviewer_id "$REVIEWER")"
  [[ -z "$REVIEWER_ID" ]] && { echo "❌ Could not resolve reviewer '$REVIEWER' to a user id" >&2; exit 4; }
  REVIEWER_ENTRIES+=("$(printf '{"type":"User","id":%s}' "$REVIEWER_ID")")
  REVIEWER_LABEL="${REVIEWER_LABEL:+$REVIEWER_LABEL + }user ${REVIEWER} (id=${REVIEWER_ID})"
fi

# Join the entries into the reviewers array body reused for every PUT.
REVIEWERS_JSON="$(IFS=,; echo "${REVIEWER_ENTRIES[*]}")"

# ── Admin pre-flight (fail fast with a clear message) ─────────────────────────
PERM="$(gh api "repos/$REPO" -q '.permissions.admin' 2>/dev/null || echo "unknown")"
if [[ "$PERM" != "true" ]]; then
  echo "⚠️  You do not appear to have ADMIN on $REPO (admin=$PERM)." >&2
  echo "    Environment protection rules are an admin-only API. Expect HTTP 403." >&2
  echo "    Ask a repo/org owner to run this script, or grant admin first." >&2
  $DRY_RUN || { echo "    (continuing anyway — the first PUT will surface the 403)"; }
fi

# ── Build the gate list per --only ────────────────────────────────────────────
# Firebase Stage-0 (`*-firebase`) is listed FIRST — it is the earliest deploy point, and
# gating it makes EVERY rung require an approval click. `--production-only` strips it (and
# the other test channels) below.
declare -a ENVS=()
# True when platform $1 was requested on the command line, so only the
# environments for the selected platforms are created.
has () { [[ ",$ONLY," == *",$1,"* ]]; }

if has android; then
  ENVS+=(android-firebase android-play-internal android-play-closed android-play-beta android-play-production)
fi
if has ios; then
  ENVS+=(ios-firebase ios-testflight-internal ios-testflight-external ios-app-store)
fi
if has mac; then
  ENVS+=(mac-testflight-internal mac-testflight-external mac-app-store)
fi
if has desktop; then
  IFS=',' read -ra DT <<< "$DESKTOP_TARGETS"
  for t in "${DT[@]}"; do
    ENVS+=("desktop-${t}-prerelease" "desktop-${t}-beta" "desktop-${t}-stable")
  done
fi
if has web; then
  IFS=',' read -ra WH <<< "$WEB_HOSTS"
  for h in "${WH[@]}"; do
    ENVS+=("web-${h}-preview" "web-${h}-staging" "web-${h}-production")
  done
fi

# --production-only: gate ONLY the external / production-facing stages; leave the
# internal / firebase / preview / prerelease test channels ungated so routine test
# distribution doesn't need an approval click. Recommended for most teams.
if $PRODUCTION_ONLY; then
  declare -a FILTERED=()
  for e in ${ENVS[@]+"${ENVS[@]}"}; do
    case "$e" in
      *-internal|*-firebase|*-preview|*-prerelease) ;;   # skip test channels
      *) FILTERED+=("$e") ;;
    esac
  done
  ENVS=(${FILTERED[@]+"${FILTERED[@]}"})
fi

[[ ${#ENVS[@]} -eq 0 ]] && { echo "❌ no environments selected (check --only / --production-only)" >&2; exit 2; }

# Top-rung envs that get the optional wait timer.
is_top_rung () {
  case "$1" in
    *-production|*-app-store|*-stable) return 0 ;;
    *) return 1 ;;
  esac
}

echo "════════════════════════════════════════════════════════════════"
echo " Configure release approval gates"
echo "   repo:      $REPO"
echo "   reviewer:  $REVIEWER_LABEL"
echo "   platforms: $ONLY"
echo "   scope:     $($PRODUCTION_ONLY && echo 'production-facing stages only (Firebase/internal/preview left ungated)' || echo 'all stages incl. Firebase Stage 0 — approval at every point')"
echo "   gates:     ${#ENVS[@]} environments"
$DRY_RUN && echo "   MODE:      DRY-RUN (no changes)"
echo "════════════════════════════════════════════════════════════════"

fail=0
for env in ${ENVS[@]+"${ENVS[@]}"}; do
  wait=0
  is_top_rung "$env" && wait="$WAIT_PROD"
  body="$(printf '{"wait_timer":%s,"reviewers":[%s],"deployment_branch_policy":null}' "$wait" "$REVIEWERS_JSON")"
  if $DRY_RUN; then
    printf "  [dry-run] PUT %-32s reviewers=1 wait=%smin\n" "$env" "$wait"
    continue
  fi
  if revs=$(printf '%s' "$body" | gh api -X PUT "repos/$REPO/environments/$env" --input - 2>/dev/null \
        | python3 -c "import sys,json; d=json.load(sys.stdin); print(sum(len(r.get('reviewers',[])) for r in d.get('protection_rules',[]) if r.get('type')=='required_reviewers'))" 2>/dev/null); then
    printf "  ✅ %-32s reviewers=%s wait=%smin\n" "$env" "${revs:-?}" "$wait"
  else
    printf "  ❌ %-32s FAILED (admin required? reviewer/team lacks write?)\n" "$env"
    fail=1
  fi
done

echo "════════════════════════════════════════════════════════════════"
if [[ $fail -ne 0 ]]; then
  echo "⚠️  Some environments failed. Most common cause: you lack ADMIN on $REPO,"
  echo "    or the reviewer/team ($REVIEWER_LABEL) does not have >=write access there."
  exit 5
fi
$DRY_RUN || echo "✅ Done. Dispatched releases on $REPO now pause at each store stage for approval."
