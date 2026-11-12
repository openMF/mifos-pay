#!/usr/bin/env bash
# scripts/white-label/doctor.sh — the SINGLE white-label lifecycle command for a fork of this template.
#
# It DRIVES and VERIFIES every stage of adopting the white-label template — and re-audits ALL stages on
# every run, so a fork is never silently half-set-up or drifted from the template. app-profile/
# {app.yaml, platforms/**} is the ONE fork source of truth; everything else is DERIVED. This one command
# replaces the scattered surface (scripts/white-label/fork-init.sh + scripts/white-label/firebase.sh + scripts/white-label/keystore.sh +
# setup_ios_complete.sh + scripts/white-label/sync-dirs.sh + syncForkConfig + secrets pull).
#
# Stages (each is DRIVEN when incomplete, and VERIFIED every run):
#   1 customize   — package/appId is the fork's, not the template default (customizer ran)
#   2 identity    — app-profile SoT filled → gradle/fork.properties derived + current
#   3 derive      — syncForkConfig applied (Config.xcconfig, metadata, icons, catalog current)
#   4 template    — fork is current with upstream template + sync-dirs coverage has no gap
#   5 materialize — firebase / keystore / iOS-match / vault secrets present or correctly deferred
#   6 health      — every product-health/checks/* passes (fork-identity, appid, whitelabel B1-B10, …)
#
# Modes:
#   white-label-doctor.sh            # DIAGNOSE all stages + auto-heal the idempotent ones (derive,
#                                    #   syncForkConfig) + report. Risky stages (customizer, sync-dirs)
#                                    #   are reported as gaps with the exact fix — never auto-run.
#   white-label-doctor.sh --check    # DIAGNOSE only — full end-to-end sanity, mutates NOTHING (CI/gate).
#   white-label-doctor.sh --fix      # heal everything it safely can (adds customizer-if-needed +
#                                    #   materialize hints), then re-diagnose.
#   white-label-doctor.sh --sync     # ADOPT the latest template end-to-end: scripts/white-label/sync-dirs.sh (pull, fork
#                                    #   files preserved via customization-surface) → --fix → report.
#                                    #   Run this to stay current as the template keeps developing.
#
# NEVER prompts — a fork is configured by editing app-profile, not by answering questions (SoT-driven,
# CI-friendly, idempotent). Materialization steps auto-detect prerequisites and SKIP-with-a-note if absent.
#
# Exit: 0 ok · 1 a stage failed / a gap needs action · 3 app-profile SoT incomplete (fill it, re-run).
set -uo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
cd "$REPO_ROOT"

C_GRN=$'\033[0;32m'; C_RED=$'\033[0;31m'; C_YEL=$'\033[0;33m'; C_CYN=$'\033[0;36m'; C_RST=$'\033[0m'
say()  { printf '%s\n' "$*"; }
step() { printf '\n%s▶ %s%s\n' "$C_CYN" "$*" "$C_RST"; }
ok()   { printf '  %s✅%s %s\n' "$C_GRN" "$C_RST" "$*"; }
warn() { printf '  %s⚠️ %s %s\n' "$C_YEL" "$C_RST" "$*"; }
err()  { printf '  %s❌%s %s\n' "$C_RED" "$C_RST" "$*"; }

MODE="doctor"
case "${1:-}" in
  --check) MODE="check" ;;
  --fix)   MODE="fix" ;;
  --sync)  MODE="sync" ;;
  "")      MODE="doctor" ;;
  -h|--help)
    grep -E '^#( |$)' "$0" | sed 's/^# \{0,1\}//'; exit 0 ;;
  *) err "unknown arg: $1 (use (no arg) | --check | --fix | --sync)"; exit 1 ;;
esac

[ -f "$REPO_ROOT/app-profile/app.yaml" ] || { err "app-profile/app.yaml missing — not a white-label fork of kmp-project-template"; exit 3; }

# ── Ruby runner — delegates to the ONE resolver (scripts/ruby-exec.sh). ─────────────────────────
# This used to inline its own fallback, with two defects that only bite on a machine nobody tests on:
#   * the framework helper was found by a fixed five-level `../` climb, so it resolved ONLY at
#     workspaces/{ws}/{proj}/source/{proj}/ and silently missed for a standalone clone — the shape an
#     OSS fork actually has. ruby-exec.sh searches upward instead.
#   * `RBENV_VERSION="${RBENV_VERSION:-3.3.6}"` hardcoded the pin, so bumping .ruby-version would
#     leave this asking for a stale interpreter. ruby-exec.sh reads .ruby-version, the SoT RT-1 guards.
. "$REPO_ROOT/scripts/ruby-exec.sh"
run_ruby() {  # run_ruby <abs-script> [args...]
  if [ -d "$REPO_ROOT/deployment" ]; then
    ruby_bundle "$REPO_ROOT/deployment" -- exec ruby "$@" 2>&1 | grep -avE 'ruby toolchain ready|exec under ruby'
    return "${PIPESTATUS[0]}"
  fi
  ruby_exec "$@"
}

DERIVE="$REPO_ROOT/scripts/white-label/derive.rb"

# ── check: SoT complete? ───────────────────────────────────────────────────────────────────────────
check_sot() {
  step "SoT completeness — app-profile/{app.yaml,platforms/**}"
  local out; out="$(run_ruby "$DERIVE" --check 2>&1)"; local rc=$?
  if [ "$rc" -eq 3 ]; then err "app-profile has unfilled required keys:"; printf '%s\n' "$out" | grep -E 'unfilled|Fill them' | sed 's/^/     /'; return 3; fi
  [ "$rc" -eq 0 ] || { err "derive failed:"; printf '%s\n' "$out" | tail -3 | sed 's/^/     /'; return 1; }
  ok "all required SoT keys resolved ($(printf '%s' "$out" | grep -cE '^[a-z].*=') derivable keys)"
  return 0
}

# ── check: fork.properties current (derived == on disk)? ─────────────────────────────────────────────
check_fork_props_current() {
  step "fork.properties freshness (derived == on disk)"
  local fp="$REPO_ROOT/gradle/fork.properties"
  [ -f "$fp" ] || { warn "gradle/fork.properties absent — run --apply to derive it"; return 2; }
  local tmp; tmp="$(mktemp)"; run_ruby "$DERIVE" --out "$tmp" >/dev/null 2>&1
  # Every key derive emits must be on disk with the same value (header + ordering ignored). syncForkConfig
  # also writes keys derive does not own (project.name, windows.msix.*), so a two-way diff called every
  # freshly fixed fork STALE.
  if [ -s "$tmp" ] && [ -z "$(comm -13 <(grep -E '^[a-z].*=' "$fp" | sort) <(grep -E '^[a-z].*=' "$tmp" | sort))" ]; then
    ok "fork.properties matches app-profile"; rm -f "$tmp"; return 0
  fi
  warn "fork.properties is STALE vs app-profile — run --fix (or --apply) to re-derive"; rm -f "$tmp"; return 2
}

# ── check: template currency (fork vs upstream template + sync-dirs coverage) ─────────────────────────
check_sync_currency() {
  step "template currency (fork vs upstream kmp-project-template — is a sync overdue?)"
  # `template` when the fork has it: a fork hosted beside its own canonical repo keeps `upstream` for
  # that repo, and measuring it answered "current" about the wrong project.
  local up="" r
  for r in template upstream; do git -C "$REPO_ROOT" remote 2>/dev/null | grep -qx "$r" && { up="$r"; break; }; done
  if [ -z "$up" ]; then warn "no 'template' remote — add it to track template currency: git remote add template https://github.com/openMF/kmp-project-template.git"; return 2; fi
  # No tags: the template's release tags would land in the fork and steer its version calculation.
  git -C "$REPO_ROOT" fetch -q --no-tags "$up" 2>/dev/null || { warn "could not fetch $up — skipping currency check"; return 2; }
  local def; def="$(git -C "$REPO_ROOT" symbolic-ref -q --short "refs/remotes/$up/HEAD" 2>/dev/null | sed "s|^$up/||")"; def="${def:-dev}"
  # A sync copies files, it never merges template commits, so "commits in $up/$def not in HEAD" is
  # always non-zero. The sync records what it synced from; current means that commit already contains
  # the template's default branch.
  local synced; synced="$(sed -n 's/^template_sha=//p' "$REPO_ROOT/.template-version" 2>/dev/null)"
  [ -n "$synced" ] || { warn "no .template-version — this fork has never been synced; run --sync"; return 2; }
  if ! git -C "$REPO_ROOT" merge-base --is-ancestor "$up/$def" "$synced" 2>/dev/null; then
    local behind; behind="$(git -C "$REPO_ROOT" rev-list --count "$synced..$up/$def" 2>/dev/null || echo '?')"
    warn "last sync (${synced:0:12}) is ${behind} commit(s) behind $up/${def} — run --sync to adopt the latest template"; return 2
  fi
  ok "fork is current with $up/${def} (synced from ${synced:0:12})"
  return 0
}

# ── check: product-health aggregate — this IS the customize + identity + health verification (stages
#    1 + 6): fork-identity (no template package leftover = customizer ran), appid-consistency
#    (customizer output consistent), ios-pbxproj-identity, deployment-whitelabel B1-B10, listing-sync. ──
check_product_health() {
  step "product-health checks (customize + identity + health — the fork sanity suite)"
  local ph="$REPO_ROOT/scripts/product-health/product-health.sh"
  [ -x "$ph" ] || { warn "product-health.sh not found — skipping"; return 2; }
  bash "$ph"; return $?
}

do_check() {
  local rc=0
  check_sot || rc=$?
  [ "$rc" -eq 3 ] && return 3   # SoT incomplete is terminal
  check_fork_props_current || { [ "$rc" -eq 0 ] && rc=2; }
  check_sync_currency      || { [ "$rc" -eq 0 ] && rc=2; }
  check_product_health     || rc=1
  printf '\n'
  if [ "$rc" -eq 0 ]; then say "${C_GRN}✅ white-label-doctor: every stage complete — fork is fully + correctly white-labeled + current${C_RST}"
  elif [ "$rc" -eq 2 ]; then say "${C_YEL}⚠️  white-label-doctor: fork is usable, but a stage is stale/overdue — run --fix (or --sync)${C_RST}"
  else say "${C_RED}❌ white-label-doctor: a stage FAILED — see above (gaps left unresolved block release)${C_RST}"; fi
  return "$rc"
}

# ── doctor (default): heal the IDEMPOTENT stages (derive + syncForkConfig — always safe to re-run),
#    then diagnose every stage. Risky stages (customizer, sync-dirs) are reported as gaps, never auto-run. ─
do_doctor() {
  step "Heal idempotent stages (derive fork.properties + syncForkConfig — always safe)"
  if run_ruby "$DERIVE" >/dev/null 2>&1 && ./gradlew -q syncForkConfig >/dev/null 2>&1; then
    ok "fork.properties derived + syncForkConfig applied"
  else
    warn "idempotent heal skipped (SoT incomplete or gradle unavailable) — see diagnosis below"
  fi
  do_check
}

# ── fix / apply: heal everything it safely can — derive → syncForkConfig → materialize hints ──────────
do_fix() {
  step "1/5 Derive gradle/fork.properties from app-profile (SoT)"
  local out; out="$(run_ruby "$DERIVE" 2>&1)"; local rc=$?
  if [ "$rc" -eq 3 ]; then err "app-profile SoT is incomplete — fill it, then re-run:"; printf '%s\n' "$out" | grep -E 'unfilled|Fill' | sed 's/^/     /'; return 3; fi
  [ "$rc" -eq 0 ] || { err "derive failed"; printf '%s\n' "$out" | tail -3 | sed 's/^/     /'; return 1; }
  printf '%s\n' "$out" | sed 's/^/  /'

  step "2/5 Derive platform config (syncForkConfig: Config.xcconfig, metadata, icons, catalog)"
  if ./gradlew -q syncForkConfig; then ok "syncForkConfig applied"; else err "syncForkConfig failed"; return 1; fi

  step "3/5 Firebase config (google-services.json / GoogleService-Info.plist)"
  if [ -f "$REPO_ROOT/cmp-android/google-services.json" ]; then ok "already present (tracked reference or materialized)"
  elif [ -x "$REPO_ROOT/firebase-setup.sh" ]; then warn "run ./firebase-setup.sh to register apps + fetch configs (needs firebase CLI login)"
  else warn "no firebase config + no scripts/white-label/firebase.sh — skipping"; fi

  step "4/5 Android signing keystore"
  if compgen -G "$REPO_ROOT/secrets/live/android/keystores/*.keystore" >/dev/null 2>&1 || compgen -G "$REPO_ROOT/keystores/*.keystore" >/dev/null 2>&1; then ok "keystore present"
  elif [ -x "$REPO_ROOT/keystore-manager.sh" ]; then warn "run ./keystore-manager.sh generate (or /secrets pull) to provision the upload keystore"
  else warn "no keystore + no scripts/white-label/keystore.sh — skipping"; fi

  step "5/5 iOS signing (fastlane match) — optional"
  if [ "$(run_ruby "$DERIVE" --check 2>/dev/null | grep -cE '^apple\.team\.id=')" -ge 1 ]; then
    if [ -f "$REPO_ROOT/secrets/live/apple/match/match_ci_key" ]; then ok "match SSH key present"
    elif [ -x "$REPO_ROOT/scripts/ios/setup_ios_complete.sh" ]; then warn "run scripts/ios/setup_ios_complete.sh (or /secrets pull) to provision match signing"
    else warn "iOS configured in SoT but no match key — provision via /secrets pull"; fi
  else warn "iOS team id not set in app-profile — skipping iOS signing"; fi

  printf '\n'; say "${C_GRN}✅ white-label-doctor --fix: heal done. Verifying every stage…${C_RST}"
  do_check
}

# ── sync: adopt the latest white-label template end-to-end (pull → adopt → verify) ───────────────────
# One command for a consumer fork to stay current with the template as it keeps developing. The code
# pull runs the CANONICAL scripts/white-label/sync-dirs.sh (which preserves fork-owned files via customization-surface.yaml);
# then --apply re-derives the fork's OWN branding from its app-profile (so a template change to the
# white-label machinery is re-adopted), and --check verifies the fork is still correctly branded.
# Workspace forks (with the framework) get the richer intelligence via `/kmp-project-template-sync`,
# which drives THIS same adopt+verify tail — so standalone + framework paths converge on one SoT.
do_sync() {
  local dry="${2:-}"
  step "1/3 Pull latest template (scripts/white-label/sync-dirs.sh — fork files preserved via customization-surface)"
  if [ ! -x "$REPO_ROOT/sync-dirs.sh" ]; then err "scripts/white-label/sync-dirs.sh not found — not a syncable fork"; return 1; fi
  if [ "$dry" = "--dry-run" ]; then
    bash "$REPO_ROOT/sync-dirs.sh" --dry-run || { err "sync-dirs dry-run failed"; return 1; }
    warn "dry-run: stopping before adopt/verify (re-run without --dry-run to apply)"; return 0
  fi
  bash "$REPO_ROOT/sync-dirs.sh" --force || { err "sync-dirs failed"; return 1; }
  ok "template pulled"
  step "2/3 Re-adopt white-label from app-profile (the template's machinery may have changed)"
  do_fix || return $?
  # do_fix already ran do_check as its tail (3/3 verify is that report).
}

case "$MODE" in
  doctor) do_doctor ;;
  check)  do_check ;;
  fix)    do_fix ;;
  sync)   do_sync "$@" ;;
esac
