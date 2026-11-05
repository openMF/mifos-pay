#!/bin/bash
#
# fork-init — turn a template clone into a branded, demo-free consumer fork.
#
# Usage:
#   bash scripts/white-label/fork-init.sh <package_id> <project_name> [app_display_name] [ios_team_id] [--keep-demo]
#   bash scripts/white-label/fork-init.sh --clean [--apply]            # standalone demo removal (no identity change)
#
# Example:
#   bash scripts/white-label/fork-init.sh com.mybank.app MyBankApp "My Bank" ABCDE12345
#   bash scripts/white-label/fork-init.sh com.mybank.app MyBankApp "My Bank" ABCDE12345 --keep-demo
#
# What this does (identity mode):
#   1. Brands `app-profile/app.yaml` — THE source of truth. identity.app_id / app_name / namespace,
#      plus the `org:` block (vendor, copyright) when --org= is given.
#   2. Runs ./gradlew syncForkConfig, which DERIVES gradle/fork.properties, libs.versions.toml,
#      iOS Config.xcconfig, local.properties (Fastlane) and gradle.properties FROM app-profile.
#   3. Removes the demo showcase (scripts/remove-demo.sh) so the fork starts from a
#      clean, branded framework shell — this is the DEFAULT. Pass --keep-demo to retain
#      the Money-Toolkit demo (for exploring the framework's reference features).
#
# SCOPE: app-profile only — app.yaml (identity + org block) and platforms/apple/apple.yaml's
# ORG-scope signing identity (team id, Match git url/branch), inherited from the workspace's
# _org/company.yaml via --org=. Per-app deployment values that are NOT org-wide — Firebase app ids,
# store metadata, per-target config — stay DEPLOYMENT-LAYER scope and arrive via promote.
#
# fork.properties and libs.versions.toml are NOT written here at all — syncForkConfig derives both
# from app-profile. Branding them directly is a no-op that reports success; see the note at the
# derived-files section below.
#
# No source file scanning, no package renaming, no sync-dirs conflicts. The convention plugin
# derives all module namespaces from the framework-owned BASE_MODULE_NAMESPACE constant (kpt) —
# a fixed template label never exposed to the consumer. Fork identity is never touched by the demo removal.
#

set -e

# ── Colors ──────────────────────────────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
BOLD='\033[1m'
NC='\033[0m'

print_success() { echo -e "${GREEN}✅ $1${NC}"; }
print_info()    { echo -e "${BLUE}⚙️  $1${NC}"; }
print_warning() { echo -e "${YELLOW}⚠️  $1${NC}"; }
print_error()   { echo -e "${RED}✘ $1${NC}"; exit 1; }


# yaml_value <file> <key> — first matching key's value, comment + surrounding quotes stripped.
# `cut -d: -f2-` rejoins on ':' so a value that contains one (a Match git URL) survives intact.
yaml_value() {
  grep -m1 -E "^[[:space:]]*$2:" "$1" 2>/dev/null \
    | cut -d: -f2- | sed -e 's/[[:space:]]*#.*$//' -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//' \
                         -e 's/^"//' -e 's/"$//'
}

# brand_assert_toml <file> <key> <expected> <label> — brand_assert for a `key = "value"` catalog line.
brand_assert_toml() {
  local file="$1" key="$2" want="$3" label="$4" got
  got="$(grep -m1 -E "^[[:space:]]*$key[[:space:]]*=" "$file" 2>/dev/null \
         | sed -e 's/^[^=]*=[[:space:]]*//' -e 's/[[:space:]]*#.*$//' -e 's/^"//' -e 's/"[[:space:]]*$//')"
  if [[ "$got" == "$want" ]]; then
    print_success "$label"
  else
    print_warning "$label — NOT written: $file has no '$key =' carrying that value (found: '${got:-<key absent>}')"
  fi
}

# brand_assert <file> <key> <expected> <label>
# Report what is TRUE of the file, not that a command exited 0.
#
# `perl -pi -e 's{...}{...}'` exits 0 whether it rewrote every line or matched nothing at all, so
# `perl … && print_success` prints a green tick for a write that never happened. That is not
# hypothetical: this script claimed `org.vendor ← org (The Mifos Initiative)` on every run while
# app-profile has no `vendor:` key anywhere — a success message for a permanent no-op. Assert the
# value is in the file afterwards (RULE-MEASUREMENT-INTEGRITY-001 MI-4/MI-5); a key that is not
# there is a WARNING naming it, never a tick.
brand_assert() {
  local file="$1" key="$2" want="$3" label="$4" got
  got="$(yaml_value "$file" "$key")"
  if [[ "$got" == "$want" ]]; then
    print_success "$label"
  else
    print_warning "$label — NOT written: $file has no '$key:' carrying that value (found: '${got:-<key absent>}')"
  fi
}
# clear_stale_note <file> <key>
# Strip an authoring note that stops being true the moment the line is branded.
#
# Two markers mean "this value is not yours yet":
#   `# PLACEHOLDER — …`  the value is unset; fill it in
#   `# REFERENCE …`      the value is the template's own example
# Every substitution in this script preserves the trailing comment, so a branded fork ends up
# asserting `team_id: L432S2FZP5   # PLACEHOLDER — …` and `app_id: org.mifos.acme   # REFERENCE
# example identity — the template ships as … (a fork re-brands via customize / app-profile)` — both
# of which deny exactly what just happened, and the second still names `customize`, a script this
# file replaced. Clear the marker on keys we actually brand; keep the durable half of the comment
# (what the field feeds), and leave untouched keys marked, so what stays marked is what is still unset.
clear_stale_note() {
  local file="$1" key="$2"
  [[ -f "$file" ]] || return 0
  FI_K="$key" perl -pi -e '
    my $k = $ENV{FI_K};
    if (/^(\s*)\Q$k\E(:\s*)(\S[^#\n]*?)(\s*)#(.*)$/) {
      my ($ind, $sep, $val, $gap, $c) = ($1, $2, $3, $4, $5);
      if ($c =~ /^\s*PLACEHOLDER\b/) {
        $_ = "$ind$k$sep$val\n";                     # the whole note was the marker
      } elsif ($c =~ /^\s*REFERENCE\b/) {
        $c =~ s/^\s*REFERENCE\s+//;                  # drop the marker word
        $c =~ s/^example identity[^.]*\.\s*//;       # and "…the template ships as … ."
        $_ = ($c =~ /\S/) ? "$ind$k$sep$val$gap# $c\n" : "$ind$k$sep$val\n";
      }
    }
  ' "$file" 2>/dev/null || true
}

# ── Verify bash version ──────────────────────────────────────────────────────
if [[ ${BASH_VERSINFO[0]} -lt 4 ]]; then
  print_error "Bash 4+ required. macOS ships with Bash 3 — run: brew install bash"
fi

# ── --clean: strip the demo showcase (showcase-framework-separation) ─────────
# Delegates to scripts/remove-demo.sh. Dry-run by default; pass --apply to perform.
# Removal only — does NOT read or write the app-namespace identity key (out of scope).
if [[ "${1:-}" == "--clean" ]]; then
  shift
  print_info "Demo-showcase removal (fork-init --clean)…"
  bash "$(dirname "$0")/../remove-demo.sh" "$@"
  exit $?
fi

# ── --verify: is this fork still fully initialised? ──────────────────────────
# Read-only. Answers the question you cannot answer by eye once a fork is months old and has taken
# template syncs: did every identity value actually land, and does the DERIVED layer still agree
# with the SoT?
#
# The agreement check is the point. Each layer is internally consistent even when branding failed —
# that is exactly how this script once reported success while leaving the template's identity in
# place — so checking app-profile alone, or the catalog alone, proves nothing. FV-2/FV-3 compare
# them, which is the only way a silent revert shows up.
if [[ "${1:-}" == "--verify" ]]; then
  fv_fail=0
  # Print a passing verification line.
  fv_ok()   { echo -e "${GREEN}  ✓ $1${NC}"; }
  # Print a failing verification line and set fv_fail, so --verify reports every mismatch.
  fv_bad()  { echo -e "${RED}  ✗ $1${NC}"; fv_fail=1; }
  # Read scalar key $2 from YAML file $1, stripping any trailing `# comment`.
  # Deliberately not a YAML parser: --verify must run on a bare clone with no toolchain.
  yval()    { grep -E "^[[:space:]]*$2:" "$1" 2>/dev/null | head -1 | cut -d: -f2- \
                | sed -e 's/[[:space:]]*#.*$//' -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//' -e 's/^"//' -e 's/"$//'; }
  # Read the double-quoted value of key $2 from TOML file $1 — the version-catalog shape.
  tval()    { grep -E "^$2[[:space:]]*=" "$1" 2>/dev/null | head -1 | cut -d'"' -f2; }

  echo -e "${BOLD}fork-init --verify${NC}"
  AY="app-profile/app.yaml"; LT="gradle/libs.versions.toml"
  FP="gradle/fork.properties"; AP="app-profile/platforms/apple/apple.yaml"

  # FV-1 — the SoT is branded, not still the template's reference identity.
  a_id="$(yval "$AY" app_id)"; a_nm="$(yval "$AY" app_name)"
  if [[ -z "$a_id" ]]; then                       fv_bad "FV-1 $AY has no identity.app_id"
  elif [[ "$a_id" == "org.mifos.kmp.template" ]]; then
    fv_bad "FV-1 app_id is still the template's reference identity ($a_id) — fork-init never branded this tree"
  else                                            fv_ok  "FV-1 app-profile app_id=$a_id app_name=$a_nm"; fi

  # FV-2 / FV-3 — the DERIVED layer agrees with the SoT (a mismatch means a write was reverted).
  l_id="$(tval "$LT" appId)"
  [[ "$l_id" == "$a_id" ]] && fv_ok "FV-2 libs.versions.toml appId agrees with app-profile" \
    || fv_bad "FV-2 appId disagrees — app-profile='$a_id' catalog='$l_id'. Run ./gradlew syncForkConfig."
  if [[ -f "$FP" ]]; then
    f_id="$(grep -E '^app\.id=' "$FP" 2>/dev/null | head -1 | cut -d= -f2-)"
    [[ "$f_id" == "$a_id" ]] && fv_ok "FV-3 fork.properties app.id agrees with app-profile" \
      || fv_bad "FV-3 app.id disagrees — app-profile='$a_id' bridge='$f_id'. Run ./gradlew syncForkConfig."
  fi

  # FV-4 — projectName reached the catalog (its own SoT, so nothing else can prove it).
  p_nm="$(tval "$LT" projectName)"
  [[ -n "$p_nm" && "$p_nm" != "kmp-project-template" ]] \
    && fv_ok  "FV-4 projectName=$p_nm" \
    || fv_bad "FV-4 projectName is still '$p_nm' — the fork builds under the template's name"

  # FV-5 — ORG signing identity was inherited, not left as a placeholder.
  if [[ -f "$AP" ]]; then
    t_id="$(yval "$AP" team_id)"; m_url="$(grep -E '^[[:space:]]*url:' "$AP" | head -1 | sed -e 's/^[[:space:]]*url:[[:space:]]*//' -e 's/[[:space:]]*#.*$//')"
    [[ -n "$t_id" && "$t_id" != "YOUR_TEAM_ID" ]] && fv_ok "FV-5 apple team_id=$t_id" \
      || fv_bad "FV-5 apple team_id is a placeholder ($t_id) — pass --org=<ws>/_org/company.yaml"
    case "$m_url" in
      *YOUR_ORG*|"") fv_bad "FV-5 Match git url is a placeholder ($m_url)" ;;
      git.com:*)     fv_bad "FV-5 Match git url is CORRUPT ($m_url) — the '@host' was eaten by shell interpolation" ;;
      *)             fv_ok  "FV-5 Match git url set" ;;
    esac
  fi

  # FV-6 — the demo is gone (a fork that still ships it has features that are not its own).
  d_mod=0; [[ -d feature ]] && d_mod="$(ls feature 2>/dev/null | grep -cE '^(showcase|loans|crypto|bills|alerts|watchlist|cloudtodo|macro|rates|amortization|calculators|currency-rates|emi-calculator|add-to-watchlist)$' || true)"
  # Fences are checked SEPARATELY from modules: a strip interrupted partway (or run with an older
  # remove-demo) can delete every demo module and still leave `// demo:begin` blocks behind in
  # settings.gradle.kts, the registries and feature-deps — a tree that looks clean by module count
  # and does not configure. Counting only modules missed exactly that state.
  #
  # product-health/tests/** is excluded: those fixtures carry demo markers DELIBERATELY as canary
  # input for the DI-seam and access-point gates. Stripping them would break the gates they prove.
  d_fence="$(grep -rl 'demo:begin' . --include='*.kt' --include='*.kts' --include='*.yaml' 2>/dev/null \
               | grep -v '/build/' | grep -v '/.git/' | grep -vc '/product-health/tests/' || true)"
  if [[ "$d_mod" -eq 0 ]]; then fv_ok "FV-6 no demo feature modules remain"
  else fv_bad "FV-6 $d_mod demo feature module(s) still present — run fork-init --clean --apply"; fi
  if [[ "${d_fence:-0}" -eq 0 ]]; then fv_ok "FV-6 no demo:begin fences remain"
  else fv_bad "FV-6 $d_fence file(s) still carry a demo:begin fence — the strip did not complete; re-run fork-init --clean --apply"; fi

  echo
  [[ "$fv_fail" -eq 0 ]] && { echo -e "${GREEN}${BOLD}fork identity intact (FV-1..FV-6)${NC}"; exit 0; }
  echo -e "${RED}${BOLD}fork identity INCOMPLETE — see failures above${NC}"; exit 1
fi

# ── Flags (extracted before positional parsing so they can appear anywhere) ──
# Identity mode removes the demo showcase BY DEFAULT (the point of the separation:
# forking = starting clean). --keep-demo retains it; --no-format forwards to the strip.
KEEP_DEMO=0
STRIP_FORMAT_FLAG=""
# ── Scope split, forced by RULE-CI-001 ───────────────────────────────────────
# PROJECT scope (package_name / display_name / name) is declared in the plan file under the
# project's Idea SoT directory. Shell tooling is barred there, so this script NEVER parses it: the
# caller resolves those values with Claude's Read tool and passes them as the positional args
# below. Passing them in IS the contract, not a convenience.
#
# ORG scope lives in `workspaces/<ws>/_org/company.yaml` — ordinary org config, bash-readable.
# --org= merges every org-wide value a fork must not restate: vendor + copyright into app.yaml's
# `org:` block, and the signing identity (apple team id, Match git url/branch) into
# platforms/apple/apple.yaml. Per-app deployment values that are not org-wide stay with promote.
ORG_YAML=""
SECRETS_MANIFEST=""
POSITIONAL=()
for a in "$@"; do
  case "$a" in
    --keep-demo)  KEEP_DEMO=1 ;;
    --no-format)  STRIP_FORMAT_FLAG="--no-format" ;;
    --org=*)      ORG_YAML="${a#--org=}" ;;
    --manifest=*) SECRETS_MANIFEST="${a#--manifest=}" ;;
    *)            POSITIONAL+=("$a") ;;
  esac
done
set -- "${POSITIONAL[@]+"${POSITIONAL[@]}"}"

# ── Args ─────────────────────────────────────────────────────────────────────
if [[ $# -lt 2 ]]; then
  echo -e "${BOLD}Usage:${NC} bash scripts/white-label/fork-init.sh <package_id> <project_name> [app_display_name] [ios_team_id]"
  echo
  echo -e "${BOLD}Examples:${NC}"
  echo "  bash scripts/white-label/fork-init.sh com.mybank.app MyBankApp"
  echo "  bash scripts/white-label/fork-init.sh com.mybank.app MyBankApp \"My Bank\" ABCDE12345"
  exit 2
fi

PACKAGE=$1
PROJECT_NAME=$2
APPNAME=${3:-$PROJECT_NAME}
TEAM_ID=${4:-"XXXXXXXXXX"}

# NOTE: module Android namespaces (kpt.*) are a FRAMEWORK-owned label, fixed in build-logic
# (org.convention.BASE_MODULE_NAMESPACE) — NOT exposed to the consumer, so nothing to derive/write here.

LIBS_TOML="gradle/libs.versions.toml"

if [[ ! -f "$LIBS_TOML" ]]; then
  print_error "$LIBS_TOML not found. Run this script from the project root."
fi

echo
echo -e "${BLUE}╔══════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║         Kotlin Multiplatform Fork Initializer        ║${NC}"
echo -e "${BLUE}╚══════════════════════════════════════════════════════╝${NC}"
echo
print_info "Package ID:       $PACKAGE"
print_info "Project name:     $PROJECT_NAME"
print_info "Display name:     $APPNAME"
print_info "iOS Team ID:      $TEAM_ID (arg; --org= overrides when unset)"
echo

# ── gradle/fork.properties + gradle/libs.versions.toml are NOT written here ──
# Both are DERIVED: `syncForkConfig` regenerates them from app-profile on every run, and
# fork.properties carries that in its own header — "DERIVED from app-profile by syncForkConfig.
# DO NOT EDIT. Edit app-profile/app.yaml … instead."
#
# An earlier revision wrote both directly, before calling syncForkConfig. Every write was reverted
# moments later by the very next step, so the script printed
#   ✅ libs.versions.toml updated
#   ✅ fork.properties app.id=com.acme.app
# and finished with the TEMPLATE's identity still in place. Branding the derived layer is not a
# redundant safety net; it is a no-op that reports success. The SoT write is below.

# ── ORG scope — merge workspaces/<ws>/_org/company.yaml into app-profile ─────
# Org-wide values belong in ONE place. Restating apple_team_id or the Match git URL per project is
# how a workspace ends up with N answers to one question; the resolver's precedence
# (deployment-layer > app-profile > _org) only helps if the org tier is actually populated.
#
# HARD, not best-effort: --org= naming a file that does not exist is an ERROR. A silent skip here is
# precisely the failure this whole step exists to remove — a fork that looks branded and carries the
# template's signing identity.
APP_YAML="app-profile/app.yaml"   # defined BEFORE the ORG block, which writes into it

orgval() {  # $1 = key under org_identity:
  [[ -f "$ORG_YAML" ]] || return 1
  grep -E "^[[:space:]]+$1:" "$ORG_YAML" | head -1 \
    | cut -d: -f2- | sed -e 's/[[:space:]]*#.*$//' -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//' -e 's/^"//' -e 's/"$//'
}

if [[ -n "$ORG_YAML" ]]; then
  if [[ ! -f "$ORG_YAML" ]]; then
    print_error "--org=$ORG_YAML does not exist — refusing to continue with template signing identity."
  fi
  print_info "Merging ORG scope from $ORG_YAML"
  ORG_TEAM="$(orgval apple_team_id)"
  ORG_MATCH_URL="$(orgval apple_match_git_url)"
  ORG_MATCH_BRANCH="$(orgval apple_match_git_branch)"
  ORG_VENDOR="$(orgval vendor)"
  ORG_COPYRIGHT="$(orgval copyright)"

  # SCOPE: this script writes app-profile/app.yaml ONLY.
  #
  # platforms/**/*.yaml (apple team id, Match git URL, Firebase app ids, store metadata) are
  # DEPLOYMENT-LAYER driven and reach app-profile through deployment-layer promote — not from here.
  # An earlier revision wrote apple.yaml's team_id and Match URL directly; that put a second writer
  # on files the promote flow owns, which is the same two-writers-one-artifact split that produced
  # two competing secrets-manifests. fork-init brands the app; it does not provision signing.
  #

  # So the ORG merge lands in app.yaml's own `org:` block, and nowhere else.
  #
  # The target key is `name:`, which is what the block actually has. An earlier revision wrote
  # `vendor:` — a key that exists in _org/company.yaml but NOWHERE in app-profile — so the
  # substitution matched nothing on every run and still printed a green tick, because perl exits 0
  # when it matches nothing. `name:` is also why the write must be scoped to the `org:` block:
  # app.yaml carries many unrelated `name:` keys, and an unscoped substitution would rewrite them all.
  ORG_NAME="$(orgval name)"
  [[ -z "$ORG_NAME" ]] && ORG_NAME="$ORG_VENDOR"
  if [[ -n "$ORG_NAME" ]]; then
    FI_N="$ORG_NAME" perl -pi -e '
      if (/^org:/)   { $in = 1 }
      elsif (/^\S/) { $in = 0 }
      if ($in) { s{^(\s*name:\s*)([^#\n]*?)(\s*)(#.*)?$}{$1$ENV{FI_N}$3$4} }
    ' "$APP_YAML" 2>/dev/null
    clear_stale_note "$APP_YAML" name
    brand_assert "$APP_YAML" name "$ORG_NAME" "org.name ← org ($ORG_NAME)"
  fi
  if [[ -n "$ORG_COPYRIGHT" ]]; then
    FI_C="$ORG_COPYRIGHT" perl -pi -e 's{^(\s*copyright:\s*)([^#\n]*?)(\s*)(#.*)?$}{$1"$ENV{FI_C}"$3$4}' "$APP_YAML" 2>/dev/null
    clear_stale_note "$APP_YAML" copyright
    brand_assert "$APP_YAML" copyright "$ORG_COPYRIGHT" "org.copyright ← org"
  fi
  APPLE_YAML="app-profile/platforms/apple/apple.yaml"
  if [[ -f "$APPLE_YAML" ]]; then
    if [[ -n "$ORG_TEAM" ]]; then
      FI_TEAM="$ORG_TEAM" perl -pi -e 's{^(\s*team_id:\s*)(\S+)}{$1$ENV{FI_TEAM}}' "$APPLE_YAML" 2>/dev/null
      clear_stale_note "$APPLE_YAML" team_id
      brand_assert "$APPLE_YAML" team_id "$ORG_TEAM" "apple.team_id ← org ($ORG_TEAM)"
    fi
    if [[ -n "$ORG_MATCH_URL" ]]; then
      FI_URL="$ORG_MATCH_URL" perl -pi -e 's{^(\s*url:\s*)(\S+)}{$1$ENV{FI_URL}}' "$APPLE_YAML" 2>/dev/null
      clear_stale_note "$APPLE_YAML" url
      brand_assert "$APPLE_YAML" url "$ORG_MATCH_URL" "apple.match.git.url ← org"
    fi
    if [[ -n "$ORG_MATCH_BRANCH" ]]; then
      FI_BR="$ORG_MATCH_BRANCH" perl -pi -e 's{^(\s*branch:\s*)([^#\n]*?)(\s*)(#.*)?$}{$1$ENV{FI_BR}$3$4}' "$APPLE_YAML" 2>/dev/null
      clear_stale_note "$APPLE_YAML" branch
      brand_assert "$APPLE_YAML" branch "$ORG_MATCH_BRANCH" "apple.match.git.branch ← org ($ORG_MATCH_BRANCH)"
    fi
  fi
fi

# ── Brand app-profile — THE SoT. Everything else is derived from it. ─────────
# This must happen BEFORE syncForkConfig, and it is the only write that actually sticks.
#
# This is the ONLY identity write in the script. syncForkConfig (next step) derives
# gradle/fork.properties, gradle/libs.versions.toml, Config.xcconfig, local.properties and
# gradle.properties from what is written here.
if [[ -f "$APP_YAML" ]]; then
  print_info "Branding $APP_YAML (the SoT)…"
  # Value-only substitution that preserves each line's trailing `# comment`. PACKAGE/APPNAME reach
  # perl through the ENVIRONMENT, never interpolated into the -e program: an app id or display name
  # containing @ or $ would otherwise be mangled by perl's own interpolation.
  FI_PKG="$PACKAGE" perl -pi -e 's{^(\s*app_id:\s*)\S+}{$1$ENV{FI_PKG}}'        "$APP_YAML"
  FI_PKG="$PACKAGE" perl -pi -e 's{^(\s*namespace:\s*)\S+}{$1$ENV{FI_PKG}}'     "$APP_YAML"
  FI_APP="$APPNAME" perl -pi -e 's{^(\s*app_name:\s*)([^#\n]*?)(\s*)(#.*)?$}{$1$ENV{FI_APP}$3$4}' "$APP_YAML"
  for k in app_id namespace app_name; do clear_stale_note "$APP_YAML" "$k"; done
  print_success "app-profile app_id=$PACKAGE app_name=$APPNAME"
else
  print_warning "$APP_YAML not found — identity will not survive syncForkConfig"
fi

# ── Vault ALIAS re-point — deployment/**/secrets-needs.yaml + secrets/LAYOUT.yaml ────
# The template ships alias names that are CORRECT for the template: project-tier rows carry the
# template slug (`kmp-project-template-upload-keystore`) and org-tier rows carry the template's
# origin org (`mifos-x-firebase-service-account`). A fork that never re-points them either keeps
# mifos-x's names, or gets them blanket-substituted downstream — and a blanket substitution is
# what breaks the convention twice over: it uses the DISPLAY name (PascalCase) instead of the
# kebab slug, and it rewrites the ORG prefix too, collapsing org-tier secrets into project-tier.
#
# Observed on a fork initialised from this template (2026-09-23): 29 rows of `Spacely-*`, five of
# them org-tier (playstore-service-account, appstore-auth-key-p8, appstore-issuer-id,
# match-cert-bundle, firebase-service-account) that belong to the WORKSPACE vault, not the app.
# Because the fork's secrets-manifest is generated FROM these files, every consumer inherited it.
#
# The two prefixes are therefore re-pointed SEPARATELY and from the two values already in scope:
#   project-tier -> $PROJECT_NAME  (the kebab slug — NEVER $APPNAME, which is the display name)
#   org-tier     -> the fork workspace's vault name from --org company.yaml
# Only alias NAME tokens are rewritten; no secret VALUE is read or written (SV32-safe).
ORG_VAULT="$(orgval vault 2>/dev/null || true)"
ORG_VAULT="${ORG_VAULT//\"/}"
repoint_aliases() {
  local f="$1" n_proj n_org
  [[ -f "$f" ]] || return 0
  n_proj=$(grep -c "kmp-project-template-" "$f" 2>/dev/null || true)
  n_org=$(grep -c "mifos-x-" "$f" 2>/dev/null || true)
  [[ "$n_proj" == "0" && "$n_org" == "0" ]] && return 0
  FI_SLUG="$PROJECT_NAME" perl -pi -e 's{\bkmp-project-template-}{$ENV{FI_SLUG}-}g' "$f"
  if [[ -n "$ORG_VAULT" ]]; then
    FI_ORG="$ORG_VAULT" perl -pi -e 's{\bmifos-x-}{$ENV{FI_ORG}-}g' "$f"
  fi
  echo "    $f  (project:$n_proj org:$n_org)"
}
if [[ -n "$PROJECT_NAME" && "$PROJECT_NAME" != "kmp-project-template" ]]; then
  print_info "Re-pointing vault aliases → project '$PROJECT_NAME', org '${ORG_VAULT:-<unset>}'…"
  if [[ -z "$ORG_VAULT" ]]; then
    # Never guess an org prefix: mis-prefixing an org-tier secret points a fork at the WRONG
    # vault, which fails at deploy time with a credential that was never meant for it.
    print_warning "org vault name unresolved (no --org=) — org-tier aliases left as mifos-x-*; re-run with --org= or repoint them before deploying"
  fi
  while IFS= read -r f; do repoint_aliases "$f"; done < <(find deployment -name secrets-needs.yaml 2>/dev/null)
  repoint_aliases "secrets/LAYOUT.yaml"
  print_success "vault aliases re-pointed (kebab slug + org tier preserved)"
fi

# ── projectName — the ONE identity value whose SoT is the catalog, not app-profile ──
# Everything else here is written to app-profile and derived outward. `projectName` is the documented
# exception: `fork-props-manifest-parity.sh` lists it under NOT_APP_PROFILE — "SoT is
# gradle/libs.versions.toml#projectName (TRACKED); syncForkConfig mirrors it into the bridge as a
# convenience". Giving it an app-profile home would hand derive.rb a second resolution order, which
# is the two-opinions failure that gate exists to prevent — so it is written HERE, directly.
#
# Without this the positional <project_name> never reaches anything: syncForkConfig reads the key
# from the catalog, so a fork initialised as "acme-app" still built as "kmp-project-template".
FI_PROJ="$PROJECT_NAME" perl -pi -e 's{^(projectName\s*=\s*)"[^"]*"}{$1"$ENV{FI_PROJ}"}' "$LIBS_TOML" 2>/dev/null
  brand_assert_toml "$LIBS_TOML" projectName "$PROJECT_NAME" \
    "libs.versions.toml projectName=$PROJECT_NAME (its declared SoT)"

# platforms/**/*.yaml is deliberately NOT written here — see the ORG-scope note above.
# Signing identity, Firebase ids and store metadata arrive via deployment-layer promote.

# ── Regenerate all platform config files ─────────────────────────────────────
print_info "Running ./gradlew syncForkConfig..."
if ./gradlew syncForkConfig --quiet; then
  print_success "iOS Config.xcconfig regenerated"
  print_success "local.properties updated (Fastlane)"
  print_success "gradle.properties updated (rootProject.name)"
else
  print_warning "syncForkConfig failed — you may need to run it manually after Gradle syncs."
fi

# ── Remove the demo showcase (DEFAULT — forking = clean start) ────────────────
if [[ "$KEEP_DEMO" -eq 0 ]]; then
  echo
  print_info "Removing demo showcase (default — pass --keep-demo to retain it)…"
  if bash "$(dirname "$0")/../remove-demo.sh" --apply --all $STRIP_FORMAT_FLAG; then
    print_success "Demo showcase removed — clean, branded framework shell ready"
  else
    print_warning "Demo removal reported an issue — review the scripts/remove-demo.sh output above."
  fi
else
  echo
  print_info "Keeping demo showcase (--keep-demo)."
fi

echo
# ── Project health — surface anything still template-default ──────────────────
# app-profile is the project-level source of truth; run the sanity harness so the fork
# immediately sees what customization left un-forked (signing/org identity, store copy).
# Non-fatal here — CI's quality-gate is the hard gate; this is guidance right after forking.
if [[ -f "$(dirname "$0")/../product-health/product-health.sh" ]]; then
  print_info "Running product health check (fork identity sanity)…"
  bash "$(dirname "$0")/../product-health/product-health.sh" \
    || print_warning "Project health flagged items above — set them in app-profile/ before releasing."
  echo
fi

echo -e "${GREEN}${BOLD}✨ Customization complete!${NC}"
echo
echo -e "${YELLOW}Next steps:${NC}"
echo "  1. Replace cmp-android/google-services.json with your Firebase config"
echo "  2. Update fastlane-config/project_config.rb Firebase App IDs"
echo "  3. Replace cmp-ios/iosApp/Assets.xcassets with your app icon"
echo "  4. Run ./gradlew build to verify everything compiles"
echo
echo -e "  To update identity later: edit ${BOLD}app-profile/app.yaml${NC} → run ${BOLD}./gradlew syncForkConfig${NC}"
