#!/usr/bin/env bash
# bash4-required: associative arrays (EXCLUSIONS, kept empty) and the contract reader; see the guard below.

# scripts/white-label/sync-dirs.sh
# Script to sync directories and files from upstream repository

# ── bash 4+ REQUIRED — fail loudly rather than silently overwrite a fork's branding ──────────────
#
# This script uses associative arrays, and bash 3.2 — still /bin/bash on macOS — does not have them.
# What it preserves for the fork is now decided by the ownership contract alone (is_excluded); the
# EXCLUSIONS map below is kept EMPTY, but the failure mode recorded next is why the guard stays.
#
# The old `#!/bin/bash` shebang pinned this script to 3.2 there, and the failure was SILENT and
# DESTRUCTIVE rather than loud: `declare -A` is rejected, but the `=( [k]=v … )` initializer still
# runs as an INDEXED assignment, so every key evaluates arithmetically to 0, all nine entries
# collide, and the last one wins. Measured: `${!EXCLUSIONS[@]}` yields the single key `0`, so 8 of
# 9 modules get NO exclusions and the sync overwrites exactly the files the map exists to protect.
#
# `env bash` picks up a Homebrew bash 5 where present; this guard catches the case where it does not.
if [ -z "${BASH_VERSINFO:-}" ] || [ "${BASH_VERSINFO[0]}" -lt 4 ]; then
    echo "error: sync-dirs.sh requires bash 4+ (found ${BASH_VERSION:-unknown} at $(command -v bash))." >&2
    echo "       macOS ships bash 3.2 as /bin/bash. Install a newer one and re-run, e.g.:" >&2
    echo "         brew install bash && exec \"\$(brew --prefix)/bin/bash\" \"$0\" \"\$@\"" >&2
    echo "       Refusing to continue: without associative arrays the exclusion map silently" >&2
    echo "       collapses and this sync would OVERWRITE fork-owned branded files." >&2
    exit 1
fi

# Repo root (this script lives at the template repo ROOT) — used to source the
# customization-surface contract library (white-label-template-completion E0/T3).
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# ── SOURCE the contract library — without this the entire merge engine is dead code ─────────────
# Four sites in this file gate on `declare -F cs_match_g` / `declare -F cs_merge`, including the
# per-directory 3-way merge loop and merge_contract_root_files(). Nothing ever sourced the library,
# so every one of those guards was permanently FALSE and every `owner: merge` path was full-copied
# in silence — the exact clobber the contract's merge class exists to prevent, on every fork, on
# every sync. The guards read as defensive ("no-op on forks that don't ship the reader"); they were
# unconditional.
#
# Proven end-to-end 2026-09-10 by running the real sync on a clone of mbs/cappy: the CappyWidgets
# app-extension target vanished from project.pbxproj (40 refs → 0) and the fork's
# com.apple.security.application-groups entitlement was dropped, while the 12 widget source files
# stayed on disk as unbuildable orphans. No conflict, no warning, exit 0.
#
# A comment at the is_excluded() call site claimed the library must be a SUBPROCESS because it uses
# "bash-4 syntax". It does not: sourcing it under macOS's /bin/bash 3.2.57 defines cs_match_g and
# cs_merge correctly (the only `mapfile` matches are a local VARIABLE of that name, not the bash-4
# builtin). Sourcing is fail-soft — a fork without the reader keeps the old full-copy behaviour.
if [ -f "$SCRIPT_DIR/../customization-surface.sh" ]; then
    # shellcheck source=/dev/null
    . "$SCRIPT_DIR/../customization-surface.sh" >/dev/null 2>&1 || true
fi

# Capture the invocation args verbatim so the self-update re-exec (below) can restart the sync with
# the SAME flags (--dry-run / --only / --force …) on the freshly-materialized engine.
ORIGINAL_ARGS=("$@")

# ── Atomic self-guard (E0/T3, FIX-01-R2-ATOMIC) ──────────────────────────────
# The operative-contract flip preserves fork paths by reading customization-surface.yaml. If a consumer
# pulled this mechanism flip WITHOUT the T1 ownership fix (mid-atomic-window), the contract is wrong and
# preserving off it would clobber. HALT before any sync rather than silently clobber.
if [ -f "$SCRIPT_DIR/../customization-surface.sh" ]; then
    if ! bash "$SCRIPT_DIR/../customization-surface.sh" require-flip-preconditions >/dev/null 2>&1; then
        echo "❌ scripts/white-label/sync-dirs.sh HALT: customization-surface.yaml ownership rows (E0/T1) are not present in this tree." >&2
        echo "   You pulled the fork-preservation flip without the ownership fix. Re-sync to land both atomically." >&2
        exit 1
    fi
fi

# Colors and formatting
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BOLD='\033[1m'
NC='\033[0m'    # No Color
CHECKMARK='\xE2\x9C\x94'
CROSS='\xE2\x9C\x98'

# Default upstream URL
DEFAULT_UPSTREAM_URL="https://github.com/openMF/kmp-project-template.git"

# Script options
DRY_RUN=false
POST_SYNC_ONLY=false   # --post-sync: run only post_sync_fork_steps (after a hand-resolved conflicted sync)
MIRROR=false   # --mirror: template-owned sync roots become a strict mirror (see mirror_template_owned)
FORCE=false
ONLY_ITEMS=""   # --only <a,b,c>: restrict the sync to this subset of SYNC_DIRS/SYNC_FILES
TEMPLATE_REF_ARG="${SYNC_TEMPLATE_REF:-}"   # --template-ref <commit|ref>: sync from this template commit (dev mode), not the merged default branch
LOG_FILE="sync-$(date +%d%m%Y-%H%M%S).log"

# Directories and files to sync
SYNC_DIRS=(
    "cmp-android"
    "cmp-desktop"
    "cmp-ios"
    "cmp-web"
    "cmp-shared"
    "cmp-navigation" # shared root-nav module (include(":cmp-navigation")) — was missing pre-2026-07
    "sync"           # shared :sync module (include(":sync")) — was missing pre-2026-07
    "core-base"
    "core"          # framework infra (incl. core/*/infra) syncs; **/demo/** + core/store seam preserved per-fork by sync_directory's convention block (base-branch re-assert)
    "build-logic"
    "deployment"     # 18-target deploy infra — FULL-COPY, zero exclusions (fork state → app-profile/deploy-targets.yaml; deploy history is framework-level deployment-layer/deploy-state/; E1/D-4)
    "fastlane"
    "fastlane-config"
    "spotless"       # shared copyright/format config
    "gradle/wrapper" # Gradle wrapper (properties + jar) — pins the version across forks; NOT gradle/ root (libs.versions.toml is consumer-local + auto-healed)
    "scripts"
    "config"
    "secrets"        # secrets SCAFFOLD only — secrets/sample/** placeholder tree + LAYOUT.yaml sync; real secrets/live/** is gitignored + fork-local + excluded below (never synced)
    "docs"           # blueprint documentation — architecture patterns, claude pattern guides, deploy/ios/secrets/setup references; fork-ADDED docs survive (checkout never deletes fork-only files), template docs refresh
    ".github"
    ".run"
    "kotlin-js-store"       # JS lockfile tree (owner: template) — full copy (WS4/T5)
    "tools"                 # KSP processors (store/database/network/data) — the codegen engine
                            # the @StoreProvider / @DbEntity / @ApiBinding annotations depend on.
                            # Was unreachable AND unowned: no fork ever got a processor update.
    # app-profile — REQUIRED for the yaml-schema-merge to ever run. app.yaml + platforms/**/*.yaml
    # are `owner: merge / yaml-schema-merge` precisely so the template can deliver freshly-added
    # keys and demo access-points while the fork's identity scalars win. Unreachable, that merge
    # NEVER executed and every fork's schema silently skewed — the exact defect the blanket
    # `app-profile/** -> fork` glob was removed to fix. sync_directory runs cs_merge per-file for
    # owner:merge paths, and fork-owned subpaths (icons, media, app-content, store json, README)
    # carry explicit `fork` rules preserved by is_excluded.
    "app-profile"
    "legal"                 # generator + rendered output (owner: template); legal/*/template.md is
                            # the fork's real legal document and stays fork-owned by contract.
    "tests"                 # contract test harness (E0/T1)
    "META-INF"              # MANIFEST.MF
)

SYNC_FILES=(
    ".bundle/config"        # BUNDLE_PATH for the vendored Ruby toolchain (owner: template)
    "LICENSE"
    "CONTRIBUTING.md"
    "CODE_OF_CONDUCT.md"
    "Gemfile"
    "Gemfile.lock"
    "ci-prepush.bat"
    "ci-prepush.sh"
    "gradlew"                       # wrapper launcher scripts — pin the Gradle version with gradle/wrapper
    "gradlew.bat"
    "compose_compiler_config.conf"  # compose compiler config referenced by AndroidCompose.kt
    "scripts/white-label/sync-dirs.sh"                  # self-propagate: each sync updates the consumer's own copy for next time
    # --- blueprint infra files (2026-07 audit: full white-label blueprint coverage) ---
    ".editorconfig"                 # shared formatting rules
    ".gitattributes"                # shared line-ending / linguist rules
    ".gitignore"                    # shared ignore baseline (build dirs, sync-*.log, secrets, local.properties)
    ".actrc"                        # act (local GitHub Actions) config — pairs with .github/
    ".ruby-version"                 # Ruby pin for Fastlane — pairs with Gemfile/Gemfile.lock
    ".kover-floor.yml"              # coverage-floor config — pairs with config/ (kover)
    ".claudeignore"                 # Claude tooling ignore baseline
    # --- blueprint setup / customization scripts (root-level, not under scripts/) ---
    "setup-project.sh"              # master fork setup script
    "scripts/white-label/fork-init.sh"                 # fork customization driver
    "scripts/white-label/keystore.sh"           # keystore generate/encode/add operations
    "scripts/white-label/firebase.sh"             # Firebase project configuration
    "generateModuleGraphs.sh"       # module dependency-graph generator
    # --- fork identity SCHEMA (the .template is committed + syncable; the filled-in
    #     gradle/fork.properties is gitignored + fork-local — NEVER synced) ---
    "gradle/fork.properties.template"
    "gradle/gradle-daemon-jvm.properties"  # pins the Gradle daemon JVM toolchain (17) + foojay URLs across forks
    # --- WS4/T6: close the missed-template-update gap for owner:template root paths ---
    "customization-surface.yaml"    # ownership contract — self-syncs (closes the false "self-syncs" claim in customization-surface.yaml)
    "build.gradle.kts"              # root Gradle build (owner: template, WS4/T6)
    "SECURITY.md"                   # community file, same class as CODE_OF_CONDUCT.md (owner: template)
    "template-migrations.yaml"      # structural-migration record forks consult — was owner: template but never synced
    "FEATURE_AUTHORING.md"          # in-template feature-authoring guide (owner: template) — was declared "synced" but had no SYNC_FILES vector (WS4/T6)
)

# Root files that are `owner: merge` and reach the fork ONLY through merge_contract_root_files, never
# through SYNC_FILES: a full checkout would clobber the fork's half before the merge reads it, and a
# held file would stay clobbered. Coverage checks read this list as a third sync vector.
CONTRACT_ROOT_MERGES=(
    "settings.gradle.kts"           # include-union — template modules in, fork-local includes kept
    "gradle/libs.versions.toml"     # catalog-3way — template bumps in, fork pins kept
    "gradle.properties"             # properties-3way — template build-tuning keys in, the fork's fork.project.name kept
    "lib-integrate.properties"      # properties-3way — template library rows in, /lib-integrate's per-fork `<lib>.local=true` links kept
)

# EXCLUSIONS — intentionally EMPTY. What a sync keeps in the fork's state is decided by the ownership
# contract alone (customization-surface.yaml + core/*/module-packages.yaml, via is_excluded). The map
# used to restate nine of those rows by hand and drifted from them; the loops that read it are no-ops.
declare -A EXCLUSIONS=()

# Display help information
show_help() {
    echo -e "${BOLD}Usage:${NC} ./sync-dirs.sh [options]"
    echo
    echo -e "${BOLD}Description:${NC}"
    echo "  This script syncs directories and files from an upstream repository."
    echo "  It preserves excluded files and directories as defined in the script."
    echo
    echo -e "${BOLD}Options:${NC}"
    echo "  -h, --help      Display this help message and exit"
    echo "  --list          Print the canonical sync surface (SYNC_DIRS + SYNC_FILES) and exit"
    echo "  --dry-run       Show what would be done without making changes"
    echo "  -f, --force     Skip confirmation prompts and proceed automatically"
    echo "  --only <list>   Restrict the sync to a comma/space-separated subset of the declared"
    echo "                  SYNC_DIRS/SYNC_FILES (e.g. --only build-logic,gradle/wrapper,spotless)."
    echo "                  Use it when a diverged fork only wants specific infra refreshed."
    echo "  --mirror        Template-owned sync roots become a STRICT mirror: fork-only files there are"
    echo "                  removed unless held in .template-sync-holds (path<TAB>reason). Opt-in — run"
    echo "                  /kmp-project-template-sync, which classifies the fork's extras first."
    echo "  --template-ref <ref>  Sync from this template commit or ref instead of the merged default branch"
    echo "                  (dev mode: iterate on an unmerged template fix by its commit hash). Env: SYNC_TEMPLATE_REF."
    echo
    echo -e "${BOLD}Examples:${NC}"
    echo "  ./sync-dirs.sh              # Run with interactive prompts"
    echo "  ./sync-dirs.sh --dry-run    # Test run without changes"
    echo "  ./sync-dirs.sh --force      # Run with no prompts"
    echo "  ./sync-dirs.sh --only build-logic,gradle/wrapper --dry-run   # only that infra subset"
}

# Logging function
log_message() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" >> "$LOG_FILE"
    echo -e "$1"
}

# Error handling function
handle_error() {
    log_message "${RED}${CROSS} Error: $1${NC}"
    exit 1
}

# Print error message
print_error() {
    log_message "${RED}${CROSS} Error: $1${NC}"
}

# Simple progress indicator function
# show_progress — kept as a no-op hook for its ~60 call sites. It drew a sleep-animated bar (0.3 s a
# call, ~20 s a sync) that told the operator nothing the step lines do not.
show_progress() { :; }

# Fancy banner
print_banner() {
    echo -e "${BLUE}╔════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║${BOLD}        Project Directory Sync Tool         ${NC}${BLUE}║${NC}"
    echo -e "${BLUE}╚════════════════════════════════════════════╝${NC}"
    echo
}

# Print step with color and symbol
print_step() {
    log_message "${GREEN}${CHECKMARK} $1${NC}"
}

# Print warning with color
print_warning() {
    log_message "${YELLOW}⚠ $1${NC}"
}

# Function to generate unique branch name
get_sync_branch_name() {
    local date_suffix=$(date +%Y%m%d-%H%M%S)
    echo "sync/upstream-${date_suffix}"
}

# Print directories and files to be synced
print_items() {
    echo -e "${BLUE}Items to sync:${NC}"
    echo -e "${BOLD}Directories:${NC}"
    # Loop variables are declared local even in a pure-print helper — same dynamic-scope hazard that
    # made is_excluded() rewrite its caller's `dir`. This one is called at top level today, which is
    # exactly how it stays harmless until someone calls it from inside a function.
    local dir file
    for dir in "${SYNC_DIRS[@]}"; do
        echo -e "  ${BOLD}→${NC} $dir"
    done

    echo -e "\n${BOLD}Files:${NC}"
    for file in "${SYNC_FILES[@]}"; do
        echo -e "  ${BOLD}→${NC} $file"
    done
    echo
}

# apply_template_migrations — follow the template's renames BEFORE anything is merged.
MIGRATE_DIR=""
# The ledger (template-migrations.yaml) and its applier come from the commit being synced (a fork's
# copies may predate a rename). FORK_STATE moves to a commit object holding the result (no hooks, branch
# unmoved): every merge reads "ours" from FORK_STATE, so it must already carry the new names —
# otherwise a renamed merge-owned file looks fork-deleted and a renamed fork seam keeps the old name
# the template no longer calls. Keeps the ledger + applier in MIGRATE_DIR for migrated_from() and
# migrate_base(), which serve every merge of the run.
apply_template_migrations() {
    git cat-file -e "$TEMPLATE_REF:template-migrations.yaml" 2>/dev/null \
        && git cat-file -e "$TEMPLATE_REF:scripts/white-label/template-migrations.sh" 2>/dev/null || return 0
    local _mig _c _n; _mig="$(mktemp -d)"; MIGRATE_DIR="$_mig"
    git show "$TEMPLATE_REF:template-migrations.yaml" > "$_mig/ledger.yaml"
    git show "$TEMPLATE_REF:scripts/white-label/template-migrations.sh" > "$_mig/apply.sh"
    bash "$_mig/apply.sh" --ledger "$_mig/ledger.yaml" > "$_mig/out" || handle_error "template migrations failed"
    while IFS=$'\t' read -r _c _n; do
        print_warning "migration rename ${BOLD}${_c#C }${NC} → $_n skipped: the new path already exists — reconcile by hand"
    done < <(grep '^C ' "$_mig/out")
    awk -F'\t' '/^[RE] /{sub(/^[RE] /,"",$1); print $1; if ($2!="") print $2}' "$_mig/out" | sort -u > "$_mig/paths"
    if [ -s "$_mig/paths" ]; then
        # A commit OBJECT for FORK_STATE, made with plumbing: no hooks (a fork's pre-commit runs
        # Spotless + Detekt — minutes, for a mechanical rename) and the branch does not move; the
        # renames land in the sync's own commit with everything else.
        # Only paths that still exist: an old name git-mv already removed would fail the whole `add`,
        # leaving the rewritten contents out of the tree.
        while IFS= read -r _p; do [ -e "$_p" ] && printf '%s\n' "$_p"; done < "$_mig/paths" > "$_mig/present"
        git add -A --pathspec-from-file="$_mig/present" || handle_error "could not stage the template migrations"
        FORK_STATE="$(git commit-tree "$(git write-tree)" -p "$(git rev-parse "$FORK_STATE^{commit}")" \
            -m "template migrations (renames) applied to $FORK_STATE")" \
            || handle_error "could not record the template migrations"
        print_step "Applied template migrations: $(grep -c '^R ' "$_mig/out") rename(s), $(grep -c '^E ' "$_mig/out") file(s) rewritten"
    fi
}

# migrated_from <path> — the path <path> had before a ledger row renamed it, if any (from the ledger,
# not this run's renames: a fork that received the rename in an earlier sync needs it just the same).
migrated_from() {
    [ -n "$MIGRATE_DIR" ] || return 0
    bash "$MIGRATE_DIR/apply.sh" --ledger "$MIGRATE_DIR/ledger.yaml" --old-path "$1" 2>/dev/null
}

# migrate_base <file> — run a 3-way merge's BASE (an older template copy) through the same renames, so
# a rename the template made — and the fork already received from apply_template_migrations — is not
# mistaken for a template edit colliding with the fork's own changes nearby.
migrate_base() {
    [ -n "$MIGRATE_DIR" ] && [ -s "$1" ] || return 0
    bash "$MIGRATE_DIR/apply.sh" --ledger "$MIGRATE_DIR/ledger.yaml" --file "$1" >/dev/null 2>&1 || true
}

# post_sync_fork_steps — re-derive the fork's own state on top of the synced tree, before the commit.
# A sync restores whatever the template ships, so two things must be redone after EVERY sync, or the
# fork does not build (measured 2026-11-03 on mifos-x-field-officer-app: template demo code back in
# sync/DataSyncWorker.kt, and the template's sample access points over the fork's generated ones):
#   1. the demo strip — `scripts/remove-demo.sh --apply`, ONLY when the fork stripped its demo, which
#      it records as `status: removed` rows in its own app-profile/package-ledger.yaml (a fork that
#      keeps the demo is left alone); it keeps the fork's database lineage
#   2. `./gradlew syncForkConfig` — every app-profile projection (access points, BuildKonfig fields,
#      identity, icons) regenerated from the fork's app-profile/
# Either failing stops the sync before it commits: the tree would not be the fork's.
post_sync_fork_steps() {
    if [ -f app-profile/package-ledger.yaml ] && grep -q 'status: removed' app-profile/package-ledger.yaml \
       && [ -f scripts/remove-demo.sh ]; then
        print_step "Re-applying the fork's demo strip (app-profile/package-ledger.yaml records it)"
        bash scripts/remove-demo.sh --apply --no-format --no-regen >/dev/null \
            || handle_error "remove-demo.sh failed after the sync — the tree still carries template demo code"
        # The strip cuts demo blocks but leaves the imports they used; --no-format skips the slow Gradle
        # spotlessApply, so restore the formatting here with the fast conform tool. Its rc also carries
        # Detekt findings, which are the hooks' and CI's to report, not this step's.
        local stripped; stripped="$(git diff --name-only -- '*.kt' | tr '\n' ' ')"
        [ -n "$stripped" ] && [ -f scripts/ci/kotlin-conform.sh ] \
            && { bash scripts/ci/kotlin-conform.sh $stripped >/dev/null 2>&1 || true; }
    fi
    if [ -x ./gradlew ]; then
        print_step "Regenerating app-profile projections (./gradlew syncForkConfig)"
        ./gradlew syncForkConfig --console=plain -q >/dev/null \
            || handle_error "./gradlew syncForkConfig failed after the sync — generated files are the template's, not the fork's"
        # A merged catalog moves dependency versions; the dependency-guard baseline must move with it
        # or the fork's Dependency Guard check fails on the sync's own PR.
        local catalog_rc; git diff --quiet HEAD -- gradle/libs.versions.toml 2>/dev/null; catalog_rc=$?
        if [ "$catalog_rc" = 1 ]; then   # 1 = changed; anything else (no repo, no HEAD) is not a change
            print_step "Re-baselining dependency-guard (the catalog changed)"
            ./gradlew dependencyGuardBaseline --console=plain -q >/dev/null \
                || handle_error "./gradlew dependencyGuardBaseline failed after the sync"
        fi
    fi
    # docs/ is generated from the fork's own source, and the sync just changed that source: leave it
    # current, or the fork's "Docs are current with source" check goes red on the sync's own PR.
    if [ -f scripts/docs/refresh.sh ]; then
        print_step "Regenerating docs/ from the synced source (scripts/docs/refresh.sh)"
        bash scripts/docs/refresh.sh >/dev/null 2>&1 \
            || handle_error "scripts/docs/refresh.sh failed after the sync — docs/ no longer matches the source"
    fi
}

# dirty_paths — every modified/untracked path, one per line, sorted (a snapshot for stage_new_dirty).
dirty_paths() { git status --porcelain --untracked-files=all | cut -c4- | sort; }

# stage_new_dirty <snapshot> — stage every path that became dirty since <snapshot>. post_sync_fork_steps
# writes fork-owned paths OUTSIDE the synced roots (module-packages.yaml status stamps, app-profile
# projections, generated docs); staging only the roots left those uncommitted after "Sync committed".
# A path that was already dirty before the steps ran (the operator's own work) is left alone.
stage_new_dirty() {
    comm -13 <(printf '%s\n' "$1") <(dirty_paths) \
        | while IFS= read -r _p; do [ -n "$_p" ] && git add -A -- "$_p" 2>/dev/null; done || true
}

# is_excluded <dir> <path> <type> — 0 when the sync must leave <path> in the FORK's state.
# ONE source decides: the ownership contract, read through scripts/customization-surface.sh (its rules,
# the per-module module-packages.yaml manifests it loads, the sync roots). A path is kept when it is
#   · held in .template-sync-holds (a fork change waiting for the template to adopt it), or
#   · owned `fork` (the fork's own code, config, branding, schemas), or
#   · owned `demo-showcase` (a demo package — a fork that stripped the demo stays stripped; one that
#     kept it keeps its copy).
# The engine has no ownership knowledge of its own. It used to: an EXCLUSIONS map and nine hard-coded
# name patterns, which disagreed with the contract — KptErrorMapper.kt, KptScreenStateDefaults.kt and
# StoreModule.kt were preserved as fork files while the contract said template, so template updates to
# them never reached a fork.
# owner_of <path> — sets OWNER to the path's contract owner. In-process and memoized: the reader is
# sourced at the top of this script. A subprocess per path re-parsed the whole contract every call
# (~0.8 s), and a sync asks ~3,500 paths several times — most of an hour spent re-reading one file.
# Re-sourcing the reader (STEP 0 bootstrap lands the template's contract) resets CS_LOADED, which drops
# this cache too. The subprocess is only the fallback for a run without the reader sourced.
owner_of() {
    if declare -F cs_match_g >/dev/null 2>&1; then
        [ "${CS_LOADED:-0}" = 1 ] && [ -n "${_OWNER_CACHE_ON:-}" ] || { declare -gA _OWNER_CACHE=(); _OWNER_CACHE_ON=1; }
        OWNER="${_OWNER_CACHE[$1]:-}"
        if [ -z "$OWNER" ]; then cs_match_g "$1"; OWNER="${CS_M_OWNER:-template}"; _OWNER_CACHE["$1"]="$OWNER"; fi
    elif [ -f "$SCRIPT_DIR/../customization-surface.sh" ]; then
        OWNER="$(bash "$SCRIPT_DIR/../customization-surface.sh" resolve "$1" 2>/dev/null | awk 'NR==1{print $2}')"
    else
        OWNER=""
    fi
}

# is_held <path> — listed in the fork's .template-sync-holds (read once per run; the sync never edits it).
is_held() {
    if [ -z "${_HOLDS_LOADED:-}" ]; then
        declare -gA _HELD=(); _HOLDS_LOADED=1
        [ -f .template-sync-holds ] && while IFS=$'\t' read -r _h _; do [ -n "$_h" ] && _HELD["$_h"]=1; done < .template-sync-holds
    fi
    [ -n "${_HELD[$1]:-}" ]
}

# is_excluded <dir> <path> <type> — 0 when the sync must leave <path> in the fork's state: held, or
# owned `fork` / `demo-showcase` (see the ownership note above owner_of).
is_excluded() {
    local full_path="${2#./}"
    is_held "$full_path" && return 0
    owner_of "$full_path"
    case "$OWNER" in fork|demo-showcase) return 0 ;; esac
    return 1
}

# Remove the `temp_*` working directories a sync creates.
# Called on completion AND on failure, so an aborted sync does not leave a half-fetched
# tree that the next run would mistake for real content.
cleanup_temp_dirs() {
    print_step "Cleaning up temporary directories..."
    # Only the repo root: every temp_<dir> is created there. A full-tree find crawled build/ and .gradle/
    # (12 s a sync) to find nothing.
    find . -maxdepth 1 -type d -name "temp_*" -exec rm -rf {} +
    show_progress
}

# Function to preserve excluded paths
preserve_excluded_paths() {
    local dir=$1
    local destination=$2

    if [ -n "${EXCLUSIONS[$dir]}" ]; then
        local IFS=' '
        read -ra EXCLUDE_ITEMS <<< "${EXCLUSIONS[$dir]}"

        for item in "${EXCLUDE_ITEMS[@]}"; do
            local IFS=':'
            read -ra PARTS <<< "$item"
            local exclude_path="${PARTS[0]}"
            local exclude_type="${PARTS[1]}"
            local full_source_path="$dir/$exclude_path"
            local full_dest_path="$destination/$exclude_path"

            if [ -e "$full_source_path" ]; then
                print_step "Preserving excluded ${exclude_type}: ${BOLD}$exclude_path${NC}"
                mkdir -p "$(dirname "$full_dest_path")"
                cp -r "$full_source_path" "$(dirname "$full_dest_path")"
            fi
        done
    fi
}

# ── Contract-keyed merge strategies (pure-white-label-store5-network E0/T4) ──────────
# Two root config files carry template updates a fork MUST receive but that a blind
# copy would clobber. The customization-surface contract declares their strategy; these
# implement it for real (no stub) so template module-includes AND version bumps reach forks:
#
#   settings.gradle.kts        → include-union   (union the include(":module") declarations)
#   gradle/libs.versions.toml  → catalog-3way    (genuine BASE/OURS/THEIRS per-key merge)
#
# Both dispatch off the declared `strategy:` in customization-surface.yaml, so contract
# and engine agree. They REPLACE the old skip-of-settings.gradle.kts + the one-way
# heal_libs_versions_toml (kept below only as a fallback for forks without the reader).

# include-union: union the fork's and the template's `include(...)` module declarations.
# Start from the TEMPLATE file (it carries pluginManagement / dependencyResolutionManagement
# / structural updates), then append every include(...) line the fork has that the template
# lacks — so template-added modules reach the fork AND fork-local :feature:* includes survive.
#   merge_settings_include_union <ours> <base> <theirs> [<out>]
merge_settings_include_union() {
    local ours="$1" theirs="$3" out="${4:-$1}"
    [ -f "$ours" ]   || { [ -f "$theirs" ] && cp "$theirs" "$out"; return 0; }
    [ -f "$theirs" ] || { cp "$ours" "$out"; return 0; }
    local tmp fork_only last
    tmp="$(mktemp)"
    cp "$theirs" "$tmp"
    fork_only="$(comm -23 \
        <(grep -E '^[[:space:]]*include\(' "$ours"   | sed 's/[[:space:]]*$//' | sort -u) \
        <(grep -E '^[[:space:]]*include\(' "$theirs" | sed 's/[[:space:]]*$//' | sort -u))"
    if [ -n "$fork_only" ]; then
        last="$(grep -nE '^[[:space:]]*include\(' "$tmp" | tail -1 | cut -d: -f1)"
        if [ -n "$last" ]; then
            { head -n "$last" "$tmp"; printf '%s\n' "$fork_only"; tail -n +"$((last + 1))" "$tmp"; } > "${tmp}.2"
            mv "${tmp}.2" "$tmp"
        else
            printf '%s\n' "$fork_only" >> "$tmp"
        fi
        print_step "include-union: preserved $(printf '%s\n' "$fork_only" | grep -c . ) fork-local module include(s) in ${BOLD}$(basename "$out")${NC}"
    fi
    # Prune PHANTOM includes: the union starts from the TEMPLATE file, which declares the template's OWN
    # demo/app feature modules (feature/showcase, feature/loans, feature/rates, …) that a downstream fork
    # does NOT have and the sync does NOT copy in. Left in, they reference non-existent module dirs and
    # Gradle configuration hard-fails ("Configuring project ':feature:showcase' without an existing
    # directory"), blocking even syncForkConfig. Drop every include(":a:b") whose resolved dir a/b/ is
    # absent on disk (relative to the settings.gradle.kts dir) — keep the fork's real + synced modules.
    local _iu_root _iu_out _iu_dropped=0 _iu_coord
    _iu_root="$(cd "$(dirname "$out")" 2>/dev/null && pwd)"; [ -n "$_iu_root" ] || _iu_root="$(pwd)"
    _iu_out="$(mktemp)"
    while IFS= read -r _iu_line || [ -n "$_iu_line" ]; do
        _iu_coord="$(printf '%s' "$_iu_line" | sed -nE 's/^[[:space:]]*include\("?:([A-Za-z0-9:_.-]+)"?\).*/\1/p')"
        if [ -n "$_iu_coord" ] && [ ! -d "$_iu_root/${_iu_coord//://}" ]; then _iu_dropped=$((_iu_dropped + 1)); continue; fi
        printf '%s\n' "$_iu_line"
    done < "$tmp" > "$_iu_out"
    mv "$_iu_out" "$tmp"
    [ "$_iu_dropped" -gt 0 ] && print_step "include-union: pruned ${BOLD}$_iu_dropped${NC} phantom include(s) (module dir absent in this fork) from ${BOLD}$(basename "$out")${NC}"
    mv "$tmp" "$out"
    return 0
}

# catalog-3way: a genuine per-key 3-way merge of a Gradle version catalog across its
# [versions] / [libraries] / [plugins] / [bundles] sections.
#   base = last-synced template catalog · ours = fork · theirs = new template
# Per key (standard 3-way semantics, applied key-wise not line-wise so unrelated additions
# never spurious-conflict):
#   • key only in theirs, not in base  → template-ADDED  → add
#   • key only in ours                 → fork-added/pin  → keep
#   • both, ours == base (fork untouched), theirs != base → template BUMPED → take theirs (major surfaced)
#   • both, theirs == base (template untouched)          → fork PINNED     → keep ours
#   • both diverged (ours != base, theirs != base, ≠)    → CONFLICT        → keep ours + surface
# The fork's file formatting/comments are preserved (we walk OURS and only swap bumped values
# + append template-added keys). Returns 0 clean · 1 if a real conflict was surfaced.
# ── project.pbxproj — a STRUCTURAL 3-way, delegated to merge-pbxproj.rb ──────
# A line merge is not merely suboptimal on a pbxproj, it is unusable: the file is a serialized
# object graph, so "add a target" edits UUID arrays three levels from the object it adds, and
# git merge-file calls adjacent array insertions a conflict. Worse, conflict markers make the
# project UNOPENABLE — a human cannot even use Xcode to resolve what the merge produced. Measured
# on mbs/cappy: git merge-file emitted 6 conflict markers where the structural merge has 0.
#
# FALLBACK IS "KEEP OURS", NEVER "TAKE THEIRS". Without ruby the merge cannot run, and the two
# failure modes are not symmetric: taking the template's copy silently deletes the fork's targets
# (unrecoverable without git archaeology), while keeping the fork's copy merely means this sync
# skipped the Xcode project — visible, warned, and re-tried on the next run.
merge_pbxproj_3way() {
    local ours="$1" base="$2" theirs="$3" out="${4:-$1}"
    [ -f "$ours" ] && [ -f "$theirs" ] || { [ -f "$theirs" ] && cp "$theirs" "$out"; return 0; }
    [ -f "$base" ] || cp "$ours" "$base"

    local repo_root; repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
    local merger="$repo_root/scripts/white-label/merge-pbxproj.rb"
    if [ ! -f "$merger" ]; then
        print_warning "merge-pbxproj.rb absent — KEPT the fork's $out (this sync skipped the Xcode project)"
        cp "$ours" "$out"; return 3
    fi

    # shellcheck source=/dev/null
    . "$repo_root/scripts/ruby-exec.sh" 2>/dev/null || true
    if ! declare -F ruby_exec >/dev/null 2>&1; then
        print_warning "ruby-exec.sh unavailable — KEPT the fork's $out (this sync skipped the Xcode project)"
        cp "$ours" "$out"; return 3
    fi

    local tmp_out; tmp_out="$(mktemp -d)/project.pbxproj"
    mkdir -p "$(dirname "$tmp_out")"

    # PLAIN ruby first, bundler only as a fallback. `xcodeproj` ships as a fastlane transitive, so on
    # a machine that has ever set up iOS deployment it is already installed for the pinned ruby and
    # loads with a bare `require` — measured 1.27.0 on both the template and a fresh fork clone.
    # Going through bundler FIRST breaks that: `bundle exec` demands a fully-installed Gemfile in the
    # FORK, and a fork that has never run `bundle install` gets bundler resolving against whatever
    # ruby it lands on. Observed on a real run — the merge died in bundler under macOS's system ruby
    # 2.6 with `uninitialized constant Gem::Resolver::APISet::GemParser`, nothing to do with the
    # merge, and the sync fell back to keep-ours for a file it could have merged cleanly.
    # merge-pbxproj.rb exits 2 (and only 2) when the gem is genuinely unavailable, so that is the
    # single signal worth retrying under bundler.
    ruby_exec "$merger" --ours "$ours" --base "$base" --theirs "$theirs" --out "$tmp_out" --report
    local _rc=$?
    if [ "$_rc" -eq 2 ] && declare -F ruby_bundle >/dev/null 2>&1; then
        ruby_bundle "$repo_root" -- exec ruby "$merger" \
            --ours "$ours" --base "$base" --theirs "$theirs" --out "$tmp_out" --report
        _rc=$?
    fi
    if [ "$_rc" -eq 0 ]; then
        cp "$tmp_out" "$out"; rm -rf "$(dirname "$tmp_out")"; return 0
    fi
    rm -rf "$(dirname "$tmp_out")"
    # Exit 1 = a difference the merger refuses to decide; exit 2 = the gem/toolchain is missing.
    print_warning "pbxproj merge did not complete — KEPT the fork's $out. Review the reason above;"
    print_warning "  a per-fork identity setting belongs in FORK_IDENTITY_KEYS in merge-pbxproj.rb."
    cp "$ours" "$out"
    return 1
}

# ── Info.plist / *.entitlements — key-level union, delegated to merge-plist.rb ───────────────
# Template and fork each add their OWN keys to a flat top-level <dict>, so the additions land at the
# same point in the text and a line merge calls non-overlapping changes a conflict. Both files are
# also ADD/ADD against a fork's older base (neither existed at mbs/cappy's b97eb73d), so there is no
# ancestor and EVERY line conflicts. A `<<<<<<<` in an XML plist does not parse — the build breaks.
# Same keep-ours fallback rationale as the pbxproj merger.
merge_plist_union() {
    local ours="$1" base="$2" theirs="$3" out="${4:-$1}"
    [ -f "$ours" ] && [ -f "$theirs" ] || { [ -f "$theirs" ] && cp "$theirs" "$out"; return 0; }

    local repo_root; repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
    local merger="$repo_root/scripts/white-label/merge-plist.rb"
    if [ ! -f "$merger" ]; then
        print_warning "merge-plist.rb absent — KEPT the fork's $out (this sync skipped it)"
        cp "$ours" "$out"; return 3
    fi
    # shellcheck source=/dev/null
    . "$repo_root/scripts/ruby-exec.sh" 2>/dev/null || true
    if ! declare -F ruby_exec >/dev/null 2>&1; then
        print_warning "ruby-exec.sh unavailable — KEPT the fork's $out (this sync skipped it)"
        cp "$ours" "$out"; return 3
    fi
    # `-` tells the merger there is no ancestor for THIS FILE, which is different from the repo
    # having no merge base: a file added on both sides since the last sync has a valid repo base
    # and still no file base.
    local base_arg="$base"; [ -s "$base" ] || base_arg="-"
    local tmp; tmp="$(mktemp)"
    if ruby_exec "$merger" --ours "$ours" --base "$base_arg" --theirs "$theirs" --out "$tmp" --report; then
        cp "$tmp" "$out"; rm -f "$tmp"; return 0
    fi
    rm -f "$tmp"
    print_warning "plist union did not complete — KEPT the fork's $out. Review the reason above."
    cp "$ours" "$out"
    return 1
}

# merge_libs_catalog_3way <ours> <base> <theirs> [out] — key-level 3-way merge of a TOML version
# catalog: a key the fork never changed takes the template's value (a major bump is reported), a key
# only the fork changed keeps the fork's, both-changed keeps the fork's and is reported as a conflict,
# and keys new in the template are appended to their section.
merge_libs_catalog_3way() {
    local ours="$1" base="$2" theirs="$3" out="${4:-$1}"
    [ -f "$ours" ] && [ -f "$theirs" ] || { [ -f "$theirs" ] && cp "$theirs" "$out"; return 0; }
    [ -f "$base" ] || cp "$ours" "$base"
    local merged notes rc=0
    merged="$(mktemp)"; notes="$(mktemp)"
    awk -v basef="$base" -v theirsf="$theirs" '
      function trim(s){ gsub(/^[ \t]+|[ \t]+$/,"",s); return s }
      function keyof(line,   p){ if(line ~ /^[ \t]*[A-Za-z0-9_.\-]+[ \t]*=/){ p=index(line,"="); return trim(substr(line,1,p-1)) } return "" }
      function valof(line,   p){ p=index(line,"="); return trim(substr(line,p+1)) }
      function major(v,   x){ x=v; sub(/^[^0-9]*/,"",x); sub(/[^0-9].*$/,"",x); return x }
      function flushadd(sec,   m,arr,j,kk,key){
        if(sec=="") return
        m=split(tkeys[sec],arr,SUBSEP)
        for(j=1;j<=m;j++){ kk=arr[j]; if(kk=="") continue; key=sec SUBSEP kk
          if(!(key in oursseen) && !(key in baseseen)) print theirsline[key] }
      }
      BEGIN{
        while((getline l < basef) > 0){
          if(l ~ /^[ \t]*\[/){ bsec=trim(l); continue }
          k=keyof(l); if(k!=""){ base[bsec SUBSEP k]=valof(l); baseseen[bsec SUBSEP k]=1 }
        }
        close(basef)
        while((getline l < theirsf) > 0){
          if(l ~ /^[ \t]*\[/){ tsec=trim(l); if(!(tsec in tsectseen)){ tsectseen[tsec]=1; tsorder[++tnsec]=tsec } continue }
          k=keyof(l); if(k!=""){ key=tsec SUBSEP k; theirs[key]=valof(l); theirsline[key]=l; theirsseen[key]=1; tkeys[tsec]=tkeys[tsec] SUBSEP k }
        }
        close(theirsf)
      }
      {
        line=$0
        if(line ~ /^[ \t]*\[/){ flushadd(cursec); cursec=trim(line); ourssect[cursec]=1; print line; next }
        k=keyof(line)
        if(k==""){ print line; next }
        key=cursec SUBSEP k; oursseen[key]=1; o=valof(line)
        if(key in theirsseen){
          t=theirs[key]; hasb=(key in baseseen); b=(hasb?base[key]:"")
          if(o==t){ print line }
          else if(hasb && o==b){ print theirsline[key]; if(major(o)!=major(t)) print "MAJOR\t" k "\t" o " -> " t > "/dev/stderr" }
          else if(hasb && t==b){ print line }
          else { print line; print "CONFLICT\t" k "\t" o " | " t > "/dev/stderr" }
        } else { print line }
        next
      }
      END{
        flushadd(cursec)
        for(i=1;i<=tnsec;i++){ s=tsorder[i]; if(!(s in ourssect)){ print s
          m=split(tkeys[s],arr,SUBSEP); for(j=1;j<=m;j++){ kk=arr[j]; if(kk=="") continue; key=s SUBSEP kk; if(!(key in baseseen)) print theirsline[key] } } }
      }
    ' "$ours" > "$merged" 2>"$notes"
    mv "$merged" "$out"
    grep -q '^CONFLICT' "$notes" 2>/dev/null && rc=1
    while IFS="$(printf '\t')" read -r kind key detail; do
        [ "$kind" = "MAJOR" ]    && print_warning "catalog-3way: MAJOR version bump on ${BOLD}$key${NC} ($detail) — review before release"
        [ "$kind" = "CONFLICT" ] && print_warning "catalog-3way: CONFLICT on ${BOLD}$key${NC} (fork kept: $detail) — reconcile manually"
    done < "$notes"
    rm -f "$notes"
    return $rc
}

# merge_by_strategy <strategy> <ours> <base> <theirs> <out> — the one dispatcher every merge-owned file
# goes through. A file the template did not change since BASE is the fork's, so OURS is the answer for
# every strategy. Checked here rather than left to each merger: a re-serializing one (plist-union,
# pbxproj-3way, properties-3way) rebuilds the file around the template's comments and layout, and the
# fork-lines-kept guard then calls the fork's own unchanged lines dropped — a conflict nobody caused.
merge_by_strategy() {
    local strat="$1" ours="$2" base="$3" theirs="$4" out="$5"
    if [ -s "$base" ] && cmp -s "$base" "$theirs"; then
        [ "$ours" = "$out" ] || cp "$ours" "$out"
        return 0
    fi
    case "$strat" in
        include-union) merge_settings_include_union "$ours" "$base" "$theirs" "$out" ;;
        catalog-3way)  merge_libs_catalog_3way      "$ours" "$base" "$theirs" "$out" ;;
        pbxproj-3way)  merge_pbxproj_3way          "$ours" "$base" "$theirs" "$out" ;;
        plist-union)   merge_plist_union           "$ours" "$base" "$theirs" "$out" ;;
        *)             cs_merge "$strat" "$ours" "$base" "$theirs" "$out" ;;
    esac
}

# Drive the two contract-keyed root-file merges (settings.gradle.kts + libs.versions.toml)
# after the directory sync, replacing the old one-way heal_libs_versions_toml. Resolves each
# file's strategy from the contract; ours=BASE_BRANCH, theirs=TEMP_BRANCH, base=merge-base.
# Falls back to heal_libs_versions_toml on a fork that ships no contract reader.
merge_contract_root_files() {
    if ! declare -F cs_match_g >/dev/null 2>&1; then
        if [ -f "$SCRIPT_DIR/../customization-surface.sh" ]; then
            # shellcheck source=/dev/null
            source "$SCRIPT_DIR/../customization-surface.sh" 2>/dev/null || true
        fi
    fi
    if ! declare -F cs_match_g >/dev/null 2>&1; then
        print_warning "No customization-surface reader — falling back to one-way libs heal."
        heal_libs_versions_toml
        return
    fi

    echo -e "\n${BLUE}${BOLD}Contract-keyed root merges (settings include-union + catalog-3way)...${NC}"
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}\n"

    # Anchor via resolve_merge_base, NOT `git merge-base` alone — a copied (non-forked) fork has no
    # shared history and the bare call returns empty, which silently degrades every merge below into
    # a full-copy. See resolve_merge_base().
    local mbase; mbase="$(resolve_merge_base "$FORK_STATE" "$TEMP_BRANCH")" || mbase=""
    local f strat o b t rc
    for f in "${CONTRACT_ROOT_MERGES[@]}"; do
        cs_match_g "$f"
        [ "${CS_M_OWNER:-}" = "merge" ] || continue
        is_held "$f" && { print_step "Held ${BOLD}$f${NC} — kept the fork's copy (.template-sync-holds)"; continue; }
        git show "$TEMP_BRANCH:$f" >/dev/null 2>&1 || { print_warning "Template has no $f — skipping."; continue; }
        strat="${CS_M_STRAT:-3way}"
        o="$(mktemp)"; b="$(mktemp)"; t="$(mktemp)"
        git show "$FORK_STATE:$f" > "$o" 2>/dev/null || cp "$f" "$o" 2>/dev/null || : > "$o"
        git show "$TEMP_BRANCH:$f" > "$t" 2>/dev/null
        git show "${mbase}:$f"     > "$b" 2>/dev/null || cp "$o" "$b"
        migrate_base "$b"
        mkdir -p "$(dirname "$f")"
        merge_by_strategy "$strat" "$o" "$b" "$t" "$f"; rc=$?
        case "${rc:-0}" in
            0) print_step "Merged (${strat}) ${BOLD}$f${NC} — template update + fork edits reconciled"; SYNC_MERGED+=("$f") ;;
            3) print_warning "SKIPPED (${strat}) ${BOLD}$f${NC} — kept the fork's copy; template changes NOT applied" ;;
            *) print_warning "Merged (${strat}) ${BOLD}$f${NC} WITH conflicts — resolve before committing the sync"
               SYNC_CONFLICTS+=("$f") ;;
        esac
        rm -f "$o" "$b" "$t"
    done
}

# ── The 3-way merge BASE, for true forks AND copied ones ─────────────────────
# `owner: merge` is only worth declaring if the 3-way actually has a common ancestor to diff
# against. Two fork topologies exist and only one has it:
#
#   forked   — `gh repo fork` / `git clone` of the template. Shares history, so
#              `git merge-base $BASE_BRANCH $TEMP_BRANCH` returns a real commit.
#   copied   — the template TREE copied into a fresh repo (the common case in practice: measured
#              on mbs/cappy 2026-09-10 — different root commits, ZERO shared commits).
#              `git merge-base` returns EMPTY.
#
# An empty base is not a harmless degradation. `git merge-file ours <base> theirs` with base==ours
# sees every template difference as a clean addition onto an untouched base and takes ALL of theirs
# — so the merge silently produces exactly the full-copy that declaring `merge` was meant to prevent,
# with a reassuring "Merged (…) — fork edits preserved" line printed over the top of the loss.
#
# `.template-version#template_sha` is the anchor for the copied case, and its own header already
# says so ("anchors the next sync's 3-way merge base") — the merge loops simply were not reading it.
# It records the template commit this tree last synced FROM, which is precisely the common ancestor
# git cannot compute across unrelated histories.
#
# Order: real merge-base (most accurate) → PREV_TEMPLATE_SHA (correct for copied forks) → empty
# (first-ever sync of a copied fork with no anchor — the caller then falls back to ours, and the
# loop WARNS rather than claiming a merge it did not perform).
resolve_merge_base() {
    local a="$1" b="$2" mb
    mb="$(git merge-base "$a" "$b" 2>/dev/null)"
    if [ -n "$mb" ]; then printf '%s' "$mb"; return 0; fi
    if [ -n "${PREV_TEMPLATE_SHA:-}" ] && git cat-file -e "$PREV_TEMPLATE_SHA" 2>/dev/null; then
        printf '%s' "$PREV_TEMPLATE_SHA"; return 0
    fi
    return 1
}

# Remove $1, a path the template at $2 no longer ships under that exact name.
# A CASE-ONLY rename (encodeImageAsPng.kt → EncodeImageAsPng.kt) is one file on a case-insensitive
# filesystem — macOS by default. The overlay has already written the new name into that file, so
# deleting the old name deletes the new one with it: a sync on 2026-11-02 lost both
# EncodeImageAsPng actuals this way and the fork stopped compiling. When the template ships the path
# under another case, only the old index entry goes; the file on disk is the template's new one.
remove_stale_path() {
    local f=$1 tb=$2 lc
    lc="$(printf '%s' "$f" | tr '[:upper:]' '[:lower:]')"
    if git ls-tree -r --name-only "$tb" -- "$(dirname "$f")/" 2>/dev/null \
         | tr '[:upper:]' '[:lower:]' | grep -qxF -- "$lc"; then
        git rm --cached -r --quiet -- "$f" 2>/dev/null || true
        return 0
    fi
    git rm -rf --quiet -- "$f" 2>/dev/null || rm -f -- "$f"
}

# Function to sync directory with exclusions
# Propagate the template's OWN deletions into a fork that inherited the now-removed file from
# a previous sync (the `core/platform/notification/bill/**` class: the template consolidated its
# bill-reminder scheduler into worker-kmp and deleted the old per-platform WorkManager scheduler,
# but `git checkout $temp_branch -- $dir` only ever OVERWRITES files present in $temp_branch — it
# never deletes files the fork's tree has that $temp_branch doesn't, so the dead files sat in every
# fork's tree indefinitely, silently breaking the next dependency-surface change that touched them).
#
# Conservative by design — this NEVER deletes anything the fork customized:
#   1. Find files under $dir the template HAD at PREV_TEMPLATE_SHA but no longer has at $temp_branch
#      (a real template-side deletion since the fork's last sync).
#   2. Skip anything is_excluded() already treats as fork-owned/convention (demo/**, schemas/**, the
#      core/store seam, customization-surface.yaml owner:fork paths) — deletion-propagation only
#      ever applies to owner:template surfaces.
#   3. Skip anything the fork already doesn't have (nothing to do) or already customized — i.e. the
#      fork's current content differs from what PREV_TEMPLATE_SHA shipped. A diverged file is NEVER
#      auto-deleted; it's surfaced as a warning for a human to review.
#   4. Only auto-delete when the fork's copy is BYTE-IDENTICAL to the old template content — proof
#      the fork never touched it, so dropping it just mirrors the template's own completed migration.
propagate_template_deletions() {
    local dir=$1
    local temp_branch=$2

    [ -n "$PREV_TEMPLATE_SHA" ] || return 0

    local deleted_f
    while IFS= read -r deleted_f; do
        [ -z "$deleted_f" ] && continue
        [ -e "$deleted_f" ] || continue                                   # fork doesn't have it either
        is_excluded "$dir" "$deleted_f" "file" && continue                # fork-owned/convention path

        if git diff --quiet "$PREV_TEMPLATE_SHA" -- "$deleted_f" 2>/dev/null; then
            remove_stale_path "$deleted_f" "$temp_branch"
            print_step "Dropped template-removed ${BOLD}$deleted_f${NC} (unmodified since last sync)"
        else
            print_warning "Template removed ${BOLD}$deleted_f${NC} but your fork customized it — review manually (kept)."
        fi
    done < <(git diff --name-only --diff-filter=D "$PREV_TEMPLATE_SHA" "$temp_branch" -- "$dir" 2>/dev/null)
}

# ── STRICT MIRROR (--mirror, 2026-10-31) ─────────────────────────────────────────────────────────
# `git checkout $temp_branch -- $dir` OVERLAYS: it writes every file the template has and never
# removes a file only the fork has. propagate_template_deletions covers files the template itself
# deleted; nothing covered files the template NEVER shipped, so a fork's extras under a template-owned
# root (a component dropped into kpt/core/designsystem/component/, fork code in core-base/) lived on
# forever as unowned drift. With --mirror, after the copy lands, every file under $dir that the fork
# has, the template does not, and the contract resolves EXACTLY `template` is removed — unless the
# fork holds it in `.template-sync-holds` (one `path<TAB>reason` per line: an extra proposed upstream
# waits there until the template adopts it). NEVER touched: is_excluded paths (fork-owned, demo, the
# core/store seam, schemas), merge-owned paths, demo-showcase paths. Run it only after the fork's
# extras were classified (/kmp-project-template-sync does that first); a bare --mirror on an
# unclassified fork deletes its work, which is why it is opt-in.
mirror_template_owned() {
    local dir=$1 temp_branch=$2 f holds=".template-sync-holds"
    [ "$MIRROR" = true ] || return 0
    # The template's files under $dir, listed once (one `git cat-file` per fork file was a process each).
    local -A _tpl=(); while IFS= read -r f; do _tpl["$f"]=1; done < <(git ls-tree -r --name-only "$temp_branch" -- "$dir" 2>/dev/null)
    while IFS= read -r f; do
        [ -n "$f" ] || continue
        [ -n "${_tpl[$f]:-}" ] && continue                                       # template has it
        if is_held "$f"; then
            print_step "Mirror: HELD ${BOLD}$f${NC} ($(grep -F -- "$f" "$holds" | head -1 | cut -f2))"
            continue
        fi
        owner_of "$f"; [ "$OWNER" = template ] || continue                       # fork / merge / demo kept
        remove_stale_path "$f" "$temp_branch"
        print_step "Mirror: removed fork-only ${BOLD}$f${NC} from template-owned $dir"
    done < <(git ls-tree -r --name-only "$FORK_STATE" -- "$dir" 2>/dev/null)
}

# fork_deleted_dir <path> — 0 when the fork deleted the directory <path> would land in: the nearest
# ancestor the template already shipped at the last sync is gone from the fork. A directory that is
# new upstream was never the fork's to delete, so a file landing in it does not count as removed.
fork_deleted_dir() {
    local d; d="$(dirname "$1")"
    while [ "$d" != . ] && ! git cat-file -e "${PREV_TEMPLATE_SHA}:$d" 2>/dev/null; do d="$(dirname "$d")"; done
    [ "$d" != . ] && [ -z "$(git ls-tree --name-only "$FORK_STATE" -- "$d/" 2>/dev/null | head -1)" ]
}

# Sync one directory $1 from the upstream branch checked out at $2.
# Ownership is decided per path by customization-surface.yaml, so a fork-owned path inside
# a synced directory is preserved rather than overwritten.
sync_directory() {
    local dir=$1
    local temp_branch=$2

    if [ -d "$dir" ]; then
        print_step "Syncing ${BOLD}$dir${NC}..."

        if [ "$DRY_RUN" = false ]; then
            # Create temporary directory for original content
            mkdir -p "temp_$dir"

            # Store original directory for excluded items
            if [ -d "$dir" ]; then
                # First handle directory exclusions
                if [ -n "${EXCLUSIONS[$dir]}" ]; then
                    local IFS=' '
                    read -ra EXCLUDE_ITEMS <<< "${EXCLUSIONS[$dir]}"

                    for item in "${EXCLUDE_ITEMS[@]}"; do
                        local IFS=':'
                        read -ra PARTS <<< "$item"
                        local exclude_path="$dir/${PARTS[0]}"
                        local exclude_type="${PARTS[1]}"

                        if [ "$exclude_type" = "dir" ] && [ -e "$exclude_path" ]; then
                            print_step "Preserving excluded directory: ${BOLD}${PARTS[0]}${NC}"
                            mkdir -p "$(dirname "temp_$exclude_path")"
                            cp -r "$exclude_path" "$(dirname "temp_$exclude_path")"
                        elif [ "$exclude_type" = "file" ] && [ -f "$exclude_path" ]; then
                            print_step "Preserving excluded file: ${BOLD}${PARTS[0]}${NC}"
                            mkdir -p "$(dirname "temp_$exclude_path")"
                            cp "$exclude_path" "temp_$exclude_path"
                        fi
                    done
                fi
            fi

            # Checkout from upstream
            git checkout "$temp_branch" -- "$dir" || {
                print_error "Failed to sync $dir"
                rm -rf "temp_$dir"
                return 1
            }

            # ── Convention exclusions on a DIRECTORY sync (the is_excluded rules:
            #    **/demo/** + the core/store seam). Without this, a whole-dir sync of
            #    `core`/`core-base` clobbers the fork's branded seam files and re-adds
            #    demo packages the fork removed via `customizer --clean`. Re-assert the
            #    fork's own state from the base branch (BASE_BRANCH) for every synced
            #    path the convention flags: restore the fork's version if it had one,
            #    else drop the upstream-added file the fork never had.
            while IFS= read -r conv_f; do
                [ -z "$conv_f" ] && continue
                if is_excluded "$dir" "$conv_f" "file"; then
                    if git cat-file -e "${FORK_STATE}:${conv_f}" 2>/dev/null; then
                        git checkout "$FORK_STATE" -- "$conv_f" 2>/dev/null \
                            && print_step "Preserved fork's ${BOLD}$conv_f${NC} (convention)"
                    elif [ -n "${PREV_TEMPLATE_SHA:-}" ] \
                         && ! git cat-file -e "${PREV_TEMPLATE_SHA}:${conv_f}" 2>/dev/null \
                         && case "$conv_f" in */schemas/*|*/demo/*|*/demo) false ;; *) true ;; esac \
                         && ! fork_deleted_dir "$conv_f" \
                         && { owner_of "$conv_f"; [ "$OWNER" = fork ]; }; then
                        # A fork-owned file the template ships for the FIRST time (absent at the last
                        # sync) is a new SEAM the fork has never had — not one it removed: keep the
                        # template's copy as the seed. That holds when its directory is new as well:
                        # template-owned code calls such seams (AndroidApp.onCreate → onForkCreate in
                        # cmp/android/app/fork/), so dropping one breaks the fork's build, as dropping
                        # spotless/license-header.txt once stripped the license header from ~2,500 files.
                        # Not seeded: a file in a package the fork deleted (fork_deleted_dir), a Room
                        # schema (the fork's own lineage) or demo code.
                        print_step "Seeded new fork-owned ${BOLD}$conv_f${NC} from the template"
                    else
                        git rm -f --quiet "$conv_f" 2>/dev/null || rm -f "$conv_f"
                        print_step "Dropped upstream-added ${BOLD}$conv_f${NC} (fork removed it)"
                    fi
                fi
            done < <(git diff --name-only "$FORK_STATE" -- "$dir" 2>/dev/null)

            # ── Deletion propagation (template-side removals the fork inherited) ──
            propagate_template_deletions "$dir" "$temp_branch"

            # ── Strict mirror (opt-in) — fork-only files under template-owned paths ──
            mirror_template_owned "$dir" "$temp_branch"

            # ── 3-way merge for merge-owned files (customization-surface contract) ──
            # Replaces a blind upstream clobber with a real merge so fork edits (e.g.
            # AndroidManifest permissions) survive a template update. ours=fork
            # (BASE_BRANCH), theirs=upstream (temp_branch), base=merge-base. Guarded:
            # no-op on forks that don't ship the reader.
            if declare -F cs_match_g >/dev/null 2>&1 && declare -F cs_merge >/dev/null 2>&1; then
                local _mbase; _mbase="$(resolve_merge_base "$FORK_STATE" "$temp_branch")" || _mbase=""
                while IFS= read -r _mf; do
                    [ -z "$_mf" ] && continue
                    cs_match_g "$_mf"; [ "${CS_M_OWNER:-}" = "merge" ] || continue
                    # A HELD path keeps the fork's copy on the merge path too, not only on the overlay and
                    # the mirror: a held project.pbxproj was 3-way merged against a stale base and lost the
                    # fork's SwiftPM packages (mbs/reels-downloader, 2026-11-03).
                    if is_held "$_mf"; then
                        print_step "Held ${BOLD}$_mf${NC} — kept the fork's copy (.template-sync-holds)"
                        continue
                    fi
                    # A line-based 3-way on a binary produces corruption that still LOOKS like a
                    # successful merge. The blanket `merge` rows cover whole platform shells, so a
                    # binary reaching here is expected (icons, .webp, .ico) rather than exceptional —
                    # most are carved out as `fork`, and this catches whatever is not.
                    if [ -f "$_mf" ] && ! LC_ALL=C grep -qI . "$_mf" 2>/dev/null \
                       && [ -s "$_mf" ]; then
                        print_warning "Binary ${BOLD}$_mf${NC} is merge-owned — took the template's copy (no line merge possible)"
                        continue
                    fi
                    local _o _b _t; _o="$(mktemp)"; _b="$(mktemp)"; _t="$(mktemp)"
                    if git show "$FORK_STATE:$_mf" > "$_o" 2>/dev/null \
                       && git show "$temp_branch:$_mf" > "$_t" 2>/dev/null; then
                        # ── The per-FILE ancestor, which is not the same as the repo having one ──
                        # A file ADDED on both sides since the last sync (add/add) has a valid repo
                        # merge base and NO base blob of its own. Seeding `base` with OURS in that
                        # case is not a neutral fallback — it actively inverts the result: every key
                        # the fork owns then looks like something present in the ancestor and deleted
                        # upstream, so the merge DROPS it and reports success.
                        #
                        # Measured 2026-09-10: cmp-ios/iosApp/iosApp.entitlements is add/add for
                        # mbs/cappy (absent at b97eb73d; the template added its own in 99083ed2, the
                        # fork added its own). With base:=ours the union emitted ONE key —
                        # keychain-access-groups — silently dropping the fork's
                        # com.apple.security.application-groups, the WidgetKit shared container.
                        #
                        # An EMPTY base states the truth: no common ancestor. A union strategy then
                        # unions (correct), and a line 3-way conflicts (honest, and visible) instead
                        # of quietly handing the file to the template.
                        local _mold=""; declare -F migrated_from >/dev/null 2>&1 && _mold="$(migrated_from "$_mf")"
                        if [ -n "$_mbase" ] && { git show "${_mbase}:$_mf" > "$_b" 2>/dev/null \
                             || { [ -n "$_mold" ] && git show "${_mbase}:$_mold" > "$_b" 2>/dev/null; }; }; then
                            :   # the base — under the file's old path when a template migration renamed it
                        else
                            : > "$_b"
                            print_warning "No common ancestor for ${BOLD}$_mf${NC} (added on both sides) — merging as add/add"
                        fi
                        migrate_base "$_b"
                        mkdir -p "$(dirname "$_mf")"
                        # Route the contract-declared strategy (merge_by_strategy).
                        local _ms="${CS_M_STRAT:-3way}" _mrc _sch=""
                        if [ "$_ms" = yaml-schema-merge ]; then
                            # The TEMPLATE's key-ownership schema for this file (`<file>.schema.yaml`),
                            # read from the commit being synced so a newly retired key is honoured.
                            _sch="$(mktemp)"
                            git show "$temp_branch:${_mf%.yaml}.schema.yaml" > "$_sch" 2>/dev/null || : > "$_sch"
                        fi
                        CS_YAML_SCHEMA="$_sch" merge_by_strategy "$_ms" "$_o" "$_b" "$_t" "$_mf"; _mrc=$?
                        [ -z "$_sch" ] || rm -f "$_sch"
                        # A "clean" line merge must still keep every line the FORK added (in ours, not in
                        # base). A stale base (.template-version older than the fork's content) made
                        # pbxproj-3way and manifest-union report success while dropping the fork's
                        # SwiftPM packages and 61 manifest lines. Losing a template change is visible on
                        # the next sync; losing fork work is not — so keep the fork's copy and surface it.
                        case "$_ms" in
                            3way|kotlin-3way|pbxproj-3way|plist-union|manifest-union)
                                if [ "${_mrc:-0}" = 0 ] && declare -F cs_fork_lines_kept >/dev/null 2>&1 \
                                   && ! cs_fork_lines_kept "$_o" "$_b" "$_mf"; then
                                    cp "$_o" "$_mf"
                                    print_warning "Merge of ${BOLD}$_mf${NC} (${_ms}) would DROP lines the fork added — kept the fork's copy; apply the template's change by hand"
                                    _mrc=4
                                fi ;;
                        esac
                        # THREE outcomes, not two. A strategy that could not run keeps the fork's
                        # copy and returns 3; reporting that as "Merged — fork edits preserved" is
                        # true about the file and false about what happened, and it is exactly the
                        # reassuring-success-over-a-silent-skip pattern this engine keeps being bitten
                        # by. The helper already warned WHY; this must not contradict it.
                        case "${_mrc:-0}" in
                            0) print_step "Merged (${_ms}) ${BOLD}$_mf${NC} — fork edits preserved"; SYNC_MERGED+=("$_mf") ;;
                            3) print_warning "SKIPPED (${_ms}) ${BOLD}$_mf${NC} — kept the fork's copy; template changes NOT applied" ;;
                            *) print_warning "CONFLICT in ${BOLD}$_mf${NC} (${_ms}) — resolve markers before committing the sync"
                               SYNC_CONFLICTS+=("$_mf") ;;
                        esac
                    fi
                    rm -f "$_o" "$_b" "$_t"
                done < <(git diff --name-only "$FORK_STATE" "$temp_branch" -- "$dir" 2>/dev/null)
            fi

            # Restore excluded files and directories
            if [ -n "${EXCLUSIONS[$dir]}" ]; then
                local IFS=' '
                read -ra EXCLUDE_ITEMS <<< "${EXCLUSIONS[$dir]}"

                for item in "${EXCLUDE_ITEMS[@]}"; do
                    local IFS=':'
                    read -ra PARTS <<< "$item"
                    local exclude_path="$dir/${PARTS[0]}"
                    local exclude_type="${PARTS[1]}"
                    local temp_path="temp_$exclude_path"

                    if [ -e "$temp_path" ]; then
                        print_step "Restoring excluded ${exclude_type}: ${BOLD}${PARTS[0]}${NC}"
                        mkdir -p "$(dirname "$exclude_path")"
                        if [ "$exclude_type" = "dir" ]; then
                            rm -rf "$exclude_path"
                            cp -r "$temp_path" "$(dirname "$exclude_path")"
                        else
                            cp "$temp_path" "$exclude_path"
                        fi
                    fi
                done
            fi
        fi
    else
        print_warning "Directory ${BOLD}$dir${NC} not found. Creating it..."
        if [ "$DRY_RUN" = false ]; then
            mkdir -p "$dir"
            git checkout "$temp_branch" -- "$dir" || {
                handle_error "Failed to sync $dir"
                cleanup_temp_dirs
            }
        fi
    fi
    show_progress
}

# Function to sync individual file with exclusions
sync_file() {
    local file=$1
    local temp_branch=$2

    # Check if file should be excluded (root-level or directory-specific)
    if is_excluded "$(dirname "$file")" "$file" "file"; then
        print_step "Skipping excluded file: ${BOLD}$file${NC}"
        return
    fi

    print_step "Syncing ${BOLD}$file${NC}..."
    if [ "$DRY_RUN" = false ]; then
        if [ -f "$file" ]; then
            # Create directory for excluded files if it doesn't exist
            mkdir -p "temp_files"
            # Store original file if it exists
            cp "$file" "temp_files/$(basename "$file")"
        fi

        if ! git checkout "$temp_branch" -- "$file"; then
            if [ -f "temp_files/$(basename "$file")" ]; then
                # Restore original file if checkout fails
                cp "temp_files/$(basename "$file")" "$file"
            fi
            print_error "Failed to sync $file"
            return 1
        fi
    fi
    show_progress
}

# Function to get default branch name
get_default_branch() {
    local default_branch
    default_branch=$(git remote show origin | grep 'HEAD branch' | cut -d' ' -f5)
    echo "$default_branch"
}

# Function to preserve root-level excluded files
preserve_root_files() {
    if [ -n "${EXCLUSIONS["root"]}" ] && [ "$DRY_RUN" = false ]; then
        print_step "Preserving root-level excluded files..."
        mkdir -p "temp_root"

        local IFS=' '
        read -ra ROOT_EXCLUDE_ITEMS <<< "${EXCLUSIONS["root"]}"

        for item in "${ROOT_EXCLUDE_ITEMS[@]}"; do
            local IFS=':'
            read -ra PARTS <<< "$item"
            local exclude_path="${PARTS[0]}"
            local exclude_type="${PARTS[1]}"

            if [ "$exclude_type" = "file" ] && [ -f "$exclude_path" ]; then
                print_step "Preserving root file: ${BOLD}$exclude_path${NC}"
                cp "$exclude_path" "temp_root/"
            fi
        done
    fi
}

# Function to restore root-level excluded files
restore_root_files() {
    if [ -n "${EXCLUSIONS["root"]}" ] && [ "$DRY_RUN" = false ]; then
        print_step "Restoring root-level excluded files..."

        local IFS=' '
        read -ra ROOT_EXCLUDE_ITEMS <<< "${EXCLUSIONS["root"]}"

        for item in "${ROOT_EXCLUDE_ITEMS[@]}"; do
            local IFS=':'
            read -ra PARTS <<< "$item"
            local exclude_path="${PARTS[0]}"
            local exclude_type="${PARTS[1]}"

            if [ "$exclude_type" = "file" ] && [ -f "temp_root/$(basename "$exclude_path")" ]; then
                print_step "Restoring root file: ${BOLD}$exclude_path${NC}"
                cp "temp_root/$(basename "$exclude_path")" "$exclude_path"
            fi
        done

        rm -rf "temp_root"
    fi
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_help
            exit 0
            ;;
        --list)
            # Print the canonical sync surface (one entry per line) and exit — read-only,
            # no git/side-effects. Consumed by framework-verify-sync-reachability (AC11) and
            # by maintainers auditing what the backbone-widened surface now propagates (WS4).
            printf '%s\n' "${SYNC_DIRS[@]}" "${SYNC_FILES[@]}"
            exit 0
            ;;
        --dry-run)
            DRY_RUN=true
            shift
            ;;
        --mirror)
            MIRROR=true
            shift
            ;;
        --post-sync)
            # Only the post-sync fork steps (demo strip + syncForkConfig) — after resolving a
            # conflicted sync by hand, before committing it.
            POST_SYNC_ONLY=true
            shift
            ;;
        --template-ref)
            TEMPLATE_REF_ARG="${2:-}"
            [ -z "$TEMPLATE_REF_ARG" ] && handle_error "--template-ref requires a commit hash or ref"
            shift 2
            ;;
        -f|--force)
            FORCE=true
            shift
            ;;
        --only)
            ONLY_ITEMS="${2:-}"
            [ -z "$ONLY_ITEMS" ] && handle_error "--only requires a comma/space-separated list of dirs/files (e.g. --only build-logic,gradle/wrapper)"
            shift 2
            ;;
        *)
            handle_error "Unknown option: $1. Use --help for usage information."
            ;;
    esac
done

if [ "$POST_SYNC_ONLY" = true ]; then
    post_sync_fork_steps
    print_step "Post-sync fork steps done — review, then commit the sync"
    exit 0
fi

# --only: restrict SYNC_DIRS/SYNC_FILES to the named subset (targeted sync for diverged forks
# that only want specific infra refreshed). Each named item MUST already be a declared SYNC_DIR
# or SYNC_FILE — this narrows the canonical set, it never adds un-vetted paths.
if [ -n "$ONLY_ITEMS" ]; then
    ONLY_ITEMS="${ONLY_ITEMS//,/ }"
    declare -a _KEEP_DIRS=() _KEEP_FILES=()
    for _want in $ONLY_ITEMS; do
        _found=false
        for _d in "${SYNC_DIRS[@]}";  do [ "$_d" = "$_want" ] && { _KEEP_DIRS+=("$_want");  _found=true; break; }; done
        for _f in "${SYNC_FILES[@]}"; do [ "$_f" = "$_want" ] && { _KEEP_FILES+=("$_want"); _found=true; break; }; done
        [ "$_found" = false ] && handle_error "--only: '$_want' is not a declared SYNC_DIR or SYNC_FILE. Choose from: ${SYNC_DIRS[*]} ${SYNC_FILES[*]}"
    done
    SYNC_DIRS=("${_KEEP_DIRS[@]}")
    SYNC_FILES=("${_KEEP_FILES[@]}")
    echo -e "${YELLOW}${BOLD}Targeted sync (--only):${NC} ${SYNC_DIRS[*]} ${SYNC_FILES[*]}"
fi

# Check git configuration
if ! git config user.name > /dev/null || ! git config user.email > /dev/null; then
    handle_error "Git user.name or user.email not configured"
fi

# Main script
clear
print_banner
print_items

# Print configured exclusions
echo -e "${BLUE}Configured Exclusions:${NC}"
OLD_IFS="$IFS"  # Save original IFS
for dir in "${!EXCLUSIONS[@]}"; do
    echo -e "  ${BOLD}${dir}${NC}:"
    IFS=' '
    read -ra EXCLUDE_ITEMS <<< "${EXCLUSIONS[$dir]}"
    for item in "${EXCLUDE_ITEMS[@]}"; do
        IFS=':'
        read -ra PARTS <<< "$item"
        if [ "$dir" = "root" ]; then
            echo -e "    → ${PARTS[0]} (${PARTS[1]}) [root level]"
        else
            echo -e "    → ${PARTS[0]} (${PARTS[1]})"
        fi
    done
done
IFS="$OLD_IFS"  # Restore original IFS
echo

# Resolve the TEMPLATE remote. The sync source is ALWAYS the template repo — but a consumer app may use
# its own `upstream` remote for its OWN upstream (e.g. openMF/mifos-x-group-banking), NOT the template.
# So find whichever remote points at the template URL (backward-compatible: a direct fork's `upstream`
# IS the template); if none, add a dedicated `template` remote. Override name via TEMPLATE_REMOTE_NAME,
# URL via TEMPLATE_URL.
DEFAULT_UPSTREAM_URL="${TEMPLATE_URL:-$DEFAULT_UPSTREAM_URL}"
# Normalize a git URL to `host/owner/repo` so SSH, scp-like, and HTTPS forms of the SAME repo compare
# equal (e.g. git@github.com:openMF/kmp-project-template.git ≡ https://github.com/openMF/kmp-project-template).
# Portable sed only (no GNU \L) — runs on macOS (BSD sed) + Linux CI.
_norm_url() {
    echo "$1" | sed -E '
        s|\.git$||
        s|/$||
        s|^[a-zA-Z][a-zA-Z0-9+.-]*://||
        s|^[^@/]+@||
        s|^([^/:]+):|\1/|
    '
}
TEMPLATE_REMOTE=""
for _r in $(git remote); do
    if [ "$(_norm_url "$(git remote get-url "$_r" 2>/dev/null)")" = "$(_norm_url "$DEFAULT_UPSTREAM_URL")" ]; then
        TEMPLATE_REMOTE="$_r"; break
    fi
done
if [ -z "$TEMPLATE_REMOTE" ]; then
    TEMPLATE_REMOTE="${TEMPLATE_REMOTE_NAME:-template}"
    print_warning "No remote points at the template — using dedicated remote '${BOLD}$TEMPLATE_REMOTE${NC}' → $DEFAULT_UPSTREAM_URL"
    # A git remote is config-only (no working-tree/history change) and is a read-side prerequisite for
    # computing the diff — so it is added even on --dry-run, which must fetch the template to show what
    # WOULD sync. Skipping it in dry-run would make the fetch below fail on a fork with no template remote.
    git remote get-url "$TEMPLATE_REMOTE" >/dev/null 2>&1 \
        || git remote add "$TEMPLATE_REMOTE" "$DEFAULT_UPSTREAM_URL" \
        || handle_error "Failed to add $TEMPLATE_REMOTE remote"
    show_progress
fi

# Fetch from the template
print_step "Fetching from template remote (${BOLD}$TEMPLATE_REMOTE${NC})..."
if ! git fetch "$TEMPLATE_REMOTE"; then
    handle_error "Failed to fetch from $TEMPLATE_REMOTE"
fi
show_progress

# Get default branch if dev doesn't exist
DEFAULT_BRANCH=$(get_default_branch)
BASE_BRANCH="dev"
if ! git rev-parse --verify "origin/dev" >/dev/null 2>&1; then
    print_warning "dev branch not found, using default branch: ${BOLD}$DEFAULT_BRANCH${NC}"
    BASE_BRANCH="$DEFAULT_BRANCH"
fi

# TEMPLATE_REF — the template commit this run syncs FROM. Prod mode: the template's merged default
# branch. Dev mode (--template-ref <sha>): an unmerged commit, e.g. a template PR's head, so a fix
# found during a sync is tried by its hash without waiting for a merge. Fetched by hash when the
# remote-tracking refs do not already carry it.
if [ -n "$TEMPLATE_REF_ARG" ]; then
    if ! git rev-parse --verify -q "${TEMPLATE_REF_ARG}^{commit}" >/dev/null; then
        git fetch "$TEMPLATE_REMOTE" "$TEMPLATE_REF_ARG" >/dev/null 2>&1 \
            || handle_error "--template-ref '$TEMPLATE_REF_ARG' is not reachable from $TEMPLATE_REMOTE"
    fi
    TEMPLATE_REF="$(git rev-parse --verify -q "${TEMPLATE_REF_ARG}^{commit}" 2>/dev/null || git rev-parse FETCH_HEAD)"
    TEMPLATE_REF_LABEL="$TEMPLATE_REF_ARG"
    print_warning "DEV MODE: syncing from template commit ${BOLD}${TEMPLATE_REF:0:12}${NC} (not the merged $BASE_BRANCH)"
else
    TEMPLATE_REF="$TEMPLATE_REMOTE/$BASE_BRANCH"
    TEMPLATE_REF_LABEL="$BASE_BRANCH"
fi

# ── SELF-UPDATE + RE-EXEC — always run the LATEST engine (close the stale-engine chicken-and-egg) ──
# sync-dirs.sh is template-owned + self-propagating, but a fork runs its OWN copy, which only updates
# MID dir-loop (in `scripts/`) — too late for the current run. A fork carrying a stale engine (e.g. one
# missing the STEP-0 merge-protection bootstrap above) would clobber before it ever picks up the fix.
# So: if the template's engine differs from the running one, create the sync branch NOW, COMMIT the
# fresh engine + merge-protection contract onto it, and RE-EXEC — the whole run then executes the latest
# engine. Committing first keeps the working tree CLEAN across the branch dance (no mid-run edit of the
# running script → no bash re-read hazard, no `checkout` clobber). Guarded (SYNC_DIRS_SELF_UPDATED)
# against an infinite loop; requires a clean tree; skipped in --dry-run (warns instead of mutating).
_self_rel="scripts/white-label/sync-dirs.sh"
if [ "${SYNC_DIRS_SELF_UPDATED:-0}" != "1" ] && [ "$DRY_RUN" = false ] \
   && git cat-file -e "$TEMPLATE_REF:$_self_rel" 2>/dev/null; then
    _tpl_engine="$(git rev-parse "$TEMPLATE_REF:$_self_rel" 2>/dev/null || echo "")"
    _loc_engine="$(git hash-object "$_self_rel" 2>/dev/null || echo "")"
    if [ -n "$_tpl_engine" ] && [ "$_tpl_engine" != "$_loc_engine" ]; then
        if ! git diff --quiet 2>/dev/null || ! git diff --cached --quiet 2>/dev/null; then
            print_warning "sync engine is stale but the tree is dirty — skipping self-update; running the CURRENT engine (commit/clean the tree to auto-update next run)."
        else
            _su_branch="$(get_sync_branch_name)"
            print_step "Self-updating the sync engine from the template on ${BOLD}$_su_branch${NC}, then re-running on the latest…"
            if git checkout -b "$_su_branch" "$BASE_BRANCH" >/dev/null 2>&1; then
                for _up in "$_self_rel" scripts/customization-surface.sh customization-surface.yaml; do
                    git cat-file -e "$TEMPLATE_REF:$_up" 2>/dev/null || continue
                    mkdir -p "$(dirname "$_up")"
                    git show "$TEMPLATE_REF:$_up" > "$_up" 2>/dev/null
                done
                chmod +x "$_self_rel" scripts/customization-surface.sh 2>/dev/null || true
                git add "$_self_rel" scripts/customization-surface.sh customization-surface.yaml >/dev/null 2>&1
                git commit -q -m "chore(sync): self-update engine + merge-protection contract from template" >/dev/null 2>&1 || true
                SYNC_DIRS_SELF_UPDATED=1 SYNC_PREBUILT_BRANCH="$_su_branch" exec bash "$_self_rel" ${ORIGINAL_ARGS[@]+"${ORIGINAL_ARGS[@]}"}
            fi
            print_warning "self-update could not create its branch — proceeding on the CURRENT engine."
        fi
    fi
fi

# Create sync branch (or REUSE the one the self-update pre-built the fresh engine onto)
if [ -n "${SYNC_PREBUILT_BRANCH:-}" ]; then
    SYNC_BRANCH="$SYNC_PREBUILT_BRANCH"
    print_step "Reusing self-update sync branch: ${BOLD}$SYNC_BRANCH${NC}"
else
    SYNC_BRANCH=$(get_sync_branch_name)
    print_step "Creating sync branch: ${BOLD}$SYNC_BRANCH${NC}"
fi

# FORK_STATE — the fork's own pre-sync state: "ours" in every 3-way merge, and the source a preserved
# (excluded or held) path is restored from. It is the branch the sync lands on, captured before the
# engine writes anything. That is the default branch only when the engine cuts a fresh sync branch from
# it; a branch the CALLER pins (SYNC_PREBUILT_BRANCH, e.g. a session branch carrying unmerged fork
# work) IS the fork's state. Reading the default branch there treated the fork's older default as
# "the fork": held files absent from it were dropped and preserved files restored from it.
if [ -n "${SYNC_PREBUILT_BRANCH:-}" ]; then
    FORK_STATE="$(git rev-parse --verify -q "${SYNC_PREBUILT_BRANCH}^{commit}")" \
        || handle_error "SYNC_PREBUILT_BRANCH '$SYNC_PREBUILT_BRANCH' does not resolve to a commit"
else
    FORK_STATE="$BASE_BRANCH"
fi
# The engine deletes a sync branch only when it created it: a fresh one, or the self-update branch.
# A caller-pinned branch is never deleted.
SYNC_BRANCH_OWNED=true
if [ -n "${SYNC_PREBUILT_BRANCH:-}" ] && [ "${SYNC_DIRS_SELF_UPDATED:-0}" != "1" ]; then
    SYNC_BRANCH_OWNED=false
fi

if [ "$DRY_RUN" = false ]; then
    # Create sync branch from base branch — SKIP when the self-update already created + checked it out
    # (we are already on it, with the fresh engine committed and the tree clean).
    if [ -z "${SYNC_PREBUILT_BRANCH:-}" ] && ! git checkout -b "$SYNC_BRANCH" "$BASE_BRANCH"; then
        handle_error "Failed to create sync branch"
    fi
    show_progress

    # Create temporary branch for upstream changes
    TEMP_BRANCH="temp-${SYNC_BRANCH}"
    print_step "Creating temporary branch: ${BOLD}$TEMP_BRANCH${NC}"
    if ! git checkout -b "$TEMP_BRANCH" "$TEMPLATE_REF"; then
        handle_error "Failed to create temporary branch"
    fi
    show_progress

    # Switch back to sync branch
    print_step "Switching back to sync branch..."
    if ! git checkout "$SYNC_BRANCH"; then
        handle_error "Failed to switch to sync branch"
    fi
    show_progress

    apply_template_migrations

    # Preserve root-level excluded files
    preserve_root_files

    # ── Capture the PRIOR synced template commit (deletion-propagation anchor) ──
    # Read BEFORE any dir sync touches the tree — `.template-version` is fork-owned
    # (never clobbered by a dir sync) and still holds the sha from the LAST sync at this
    # point. Used by propagate_template_deletions() below to detect files the template
    # deleted since we last synced. Empty on a fork's first-ever sync (nothing to diff
    # against) — that's fine, deletion-propagation just no-ops.
    PREV_TEMPLATE_SHA=""
    if [ -f .template-version ]; then
        PREV_TEMPLATE_SHA="$(grep '^template_sha=' .template-version 2>/dev/null | cut -d= -f2)"
        if [ -n "$PREV_TEMPLATE_SHA" ] && ! git cat-file -e "$PREV_TEMPLATE_SHA" 2>/dev/null; then
            print_warning "recorded template_sha ($PREV_TEMPLATE_SHA) is not reachable locally — skipping deletion-propagation this run (fetch more template history to re-enable)."
            PREV_TEMPLATE_SHA=""
        fi
    fi

    # ── STEP 0 BOOTSTRAP — the merge-protection contract MUST be on disk before ANY dir syncs ──
    # is_excluded()'s fork-ownership check AND the 3-way merge engine (cs_merge) both silently
    # NO-OP when scripts/customization-surface.sh or the root customization-surface.yaml are absent.
    # Because `scripts/` sits mid-SYNC_DIRS (after cmp-android / cmp-shared / core) and
    # customization-surface.yaml is a SYNC_FILE (synced AFTER every dir), a fork that has not yet
    # committed the contract (a legacy / pre-white-label base — e.g. syncing onto a dev branch whose
    # white-label adoption is still on an unmerged PR) would sync cmp-android + ~14 dirs with ZERO
    # merge protection — blind-overwriting merge-owned files like AndroidManifest.xml and dropping
    # the fork's CAMERA / RECORD_AUDIO permissions. Materialize the contract from the template FIRST
    # (both files are owner:template / copy-exact, so this is idempotent + safe) so EVERY subsequent
    # dir gets full is_excluded + 3-way-merge protection. Self-bootstrap → auto-resolve; this is what
    # makes the sync run cleanly on a not-yet-white-labelled base instead of clobbering or halting.
    # The merge HELPERS bootstrap with the contract, for the same reason the contract does.
    # `scripts/` is SYNC_DIRS #18; cmp-ios is #3. So on a fork that does not yet carry
    # merge-pbxproj.rb / merge-plist.rb, cmp-ios (and cmp-android, cmp-desktop) are merged FIFTEEN
    # directories before `scripts/` delivers them — every one silently degrading to keep-ours with
    # only a warning, on the very first sync, which is precisely the sync that most needs them.
    # Measured 2026-09-11 on a real mbs/cappy clone against merged dev: "merge-pbxproj.rb absent —
    # KEPT the fork's project.pbxproj". Reordering SYNC_DIRS would be the fragile fix (it re-breaks
    # the moment someone sorts the list); materializing the helpers up front is the same move STEP 0
    # already makes for customization-surface.{yaml,sh}, and is equally idempotent — all four files
    # are owner:template / copy-exact.
    # ruby-exec.sh is in the set because the two mergers SOURCE it — bootstrapping them without it
    # just moves the failure one step ("ruby-exec.sh unavailable — KEPT the fork's project.pbxproj").
    # This list is a dependency CLOSURE, not a file list: anything STEP 0's consumers need before
    # `scripts/` syncs belongs here.
    for _bp in customization-surface.yaml scripts/customization-surface.sh \
               scripts/ruby-exec.sh \
               scripts/white-label/merge-pbxproj.rb scripts/white-label/merge-plist.rb; do
        if git cat-file -e "$TEMP_BRANCH:$_bp" 2>/dev/null; then
            mkdir -p "$(dirname "$_bp")"
            if git show "$TEMP_BRANCH:$_bp" > "$_bp" 2>/dev/null; then
                print_step "bootstrapped merge-protection contract: ${BOLD}$_bp${NC}"
            fi
        fi
    done
    [ -f scripts/customization-surface.sh ] && chmod +x scripts/customization-surface.sh 2>/dev/null || true
fi

# ── customization-surface advisory (fork-ownership contract) ──────────────────
# Non-breaking: reports which about-to-be-synced paths are `merge`-owned per
# customization-surface.yaml, so a maintainer can see which fork edits
# preserves fork edits (e.g. AndroidManifest permissions). The mechanical sync
# below is UNCHANGED — the contract is the declared source of truth the merge
# engine adopts next. Fully guarded so it can never abort a sync.
CS_READER="$SCRIPT_DIR/../customization-surface.sh"
if [ -f "$CS_READER" ] && [ -f "$SCRIPT_DIR/../../customization-surface.yaml" ]; then
    # shellcheck source=/dev/null
    source "$CS_READER" 2>/dev/null || true
    if declare -F cs_match_g >/dev/null 2>&1; then
        echo -e "\n${BLUE}${BOLD}Fork-ownership contract — merge-owned paths in the sync surface${NC}"
        cs_merge_hits=0
        while IFS= read -r _csf; do
            cs_match_g "$_csf" || continue
            if [ "${CS_M_OWNER:-}" = "merge" ]; then
                echo -e "  ${YELLOW}merge${NC} $_csf ${YELLOW}(${CS_M_STRAT:-3way} — do NOT blind-overwrite)${NC}"
                cs_merge_hits=$((cs_merge_hits + 1))
            fi
        done < <(git ls-files -- "${SYNC_DIRS[@]}" 2>/dev/null)
        [ "$cs_merge_hits" -gt 0 ] && echo -e "  ${YELLOW}⚠ $cs_merge_hits merge-owned path(s) — each is 3-way merged so fork edits survive (permissions, catalog, nav).${NC}"
    fi
fi

# Sync directories
echo -e "\n${BLUE}${BOLD}Syncing directories...${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}\n"
# Every failed path is RECORDED. The loops used to ignore the return code, so a directory that failed
# to sync was skipped and the run still stamped .template-version with the full template sha — the
# next sync then 3-way-merged against a base this tree never had (2026-10-31 audit: two forks
# recorded 81d0cc6f while 255 / 242 of their core-base files matched OLDER template versions).
SYNC_FAILED=()
# Every file a 3-way merge left with conflict markers. The engine used to warn "resolve markers before
# committing the sync" and then commit them in the same run (2026-10-31 dry run on a real fork: 19
# files with <<<<<<< in the sync commit). A conflicted sync now stops before stamping or committing.
SYNC_CONFLICTS=()
# Every file a 3-way merge produced — the only NEW code a sync writes (template-owned files were checked
# in the template, fork-owned ones are untouched), so the only code the sync itself has to lint.
SYNC_MERGED=()
# Per-section timing — each sync reports where its time went (SECONDS is bash's elapsed-time counter).
_t0=$SECONDS
for dir in "${SYNC_DIRS[@]}"; do
    _ts=$SECONDS
    sync_directory "$dir" "$TEMP_BRANCH" || SYNC_FAILED+=("$dir")
    echo -e "  ${BLUE}⏱ $dir: $((SECONDS - _ts))s${NC}"
done
echo -e "  ${BLUE}⏱ directories total: $((SECONDS - _t0))s${NC}"

# Sync files
echo -e "\n${BLUE}${BOLD}Syncing files...${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}\n"
for file in "${SYNC_FILES[@]}"; do
    sync_file "$file" "$TEMP_BRANCH" || SYNC_FAILED+=("$file")
done

# Self-heal libs.versions.toml — pull missing build-logic aliases from upstream.
# `build-logic/` is sync'd but `gradle/libs.versions.toml` is consumer-local (it
# carries project-specific package names + version overrides). When upstream adds
# new `libs.<X>` accessors in build-logic with their matching catalog entries,
# only the build-logic gets sync'd — the consumer's catalog falls behind and the
# synced build-logic references aliases that don't exist locally. This function
# detects that gap and pulls the missing alias entries (and their referenced
# version keys) directly from the upstream catalog (`$TEMP_BRANCH`).
heal_libs_versions_toml() {
    local libs_toml="gradle/libs.versions.toml"

    if [ ! -f "$libs_toml" ]; then
        return 0
    fi
    if [ ! -d "build-logic" ]; then
        return 0
    fi

    echo -e "\n${BLUE}${BOLD}Healing libs.versions.toml...${NC}"
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}\n"

    local raw_refs
    raw_refs=$(grep -rhoE '\blibs\.[a-zA-Z][a-zA-Z0-9._]*' \
            --include='*.kt' --include='*.kts' \
            build-logic 2>/dev/null \
        | sed -E 's/^libs\.//' \
        | sort -u)

    # Method-call accessors are not alias references — they take string args.
    local method_re='^(findLibrary|findVersion|findBundle|findPlugin)([(.]|$)'

    # Cache upstream's catalog content once.
    local upstream_toml
    upstream_toml=$(git show "${TEMP_BRANCH}:${libs_toml}" 2>/dev/null || true)
    if [ -z "$upstream_toml" ]; then
        print_warning "Upstream has no ${libs_toml} — skipping heal."
        return 0
    fi

    # Helper: insert a line right after [section] in the consumer's catalog.
    insert_into_section() {
        local section_name="$1"
        local line="$2"
        local line_no
        line_no=$(grep -n "^\[${section_name}\]" "$libs_toml" | head -1 | cut -d: -f1)
        if [ -z "$line_no" ]; then
            print_warning "No [${section_name}] section header — skipping insert of '$line'"
            return 1
        fi
        { head -n "$line_no" "$libs_toml"; echo "$line"; tail -n +$((line_no + 1)) "$libs_toml"; } > "${libs_toml}.tmp"
        mv "${libs_toml}.tmp" "$libs_toml"
    }

    local healed=0
    local warnings=0
    while IFS= read -r ref; do
        [ -z "$ref" ] && continue
        [[ "$ref" =~ $method_re ]] && continue

        local section="libraries"
        local lookup="$ref"
        case "$ref" in
            plugins.*)
                section="plugins"
                lookup="${ref#plugins.}"
                ;;
            versions.*)
                section="versions"
                lookup="${ref#versions.}"
                [ "$lookup" = "toml" ] && continue
                ;;
        esac
        local key
        key=$(echo "$lookup" | sed 's/\./-/g')

        if grep -qE "^${key}[[:space:]]*=" "$libs_toml"; then
            continue
        fi

        local upstream_line
        upstream_line=$(echo "$upstream_toml" | grep -E "^${key}[[:space:]]*=" | head -1)
        if [ -z "$upstream_line" ]; then
            print_warning "Missing alias '$key' (for libs.$ref) not present in upstream catalog either — manual fix needed."
            warnings=$((warnings + 1))
            continue
        fi

        # Pull the version key too, if the alias version.refs something missing.
        local ref_version
        ref_version=$(echo "$upstream_line" | grep -oE 'version\.ref = "[^"]+"' | sed -E 's/version\.ref = "([^"]+)"/\1/' | head -1)
        if [ -n "$ref_version" ] && ! grep -qE "^${ref_version}[[:space:]]*=" "$libs_toml"; then
            local ver_line
            ver_line=$(echo "$upstream_toml" | grep -E "^${ref_version}[[:space:]]*=" | head -1)
            if [ -n "$ver_line" ]; then
                if insert_into_section "versions" "$ver_line"; then
                    echo -e "  ${GREEN}${CHECKMARK}${NC} Added version '${ref_version}' (referenced by '${key}') to [versions]"
                    healed=$((healed + 1))
                fi
            fi
        fi

        if insert_into_section "$section" "$upstream_line"; then
            echo -e "  ${GREEN}${CHECKMARK}${NC} Added '${key}' to [${section}] from upstream"
            healed=$((healed + 1))
        fi
    done <<< "$raw_refs"

    if [ $healed -gt 0 ]; then
        echo -e "\n${GREEN}${BOLD}🩹 Healed ${healed} entries in ${libs_toml} — they will be included in the sync.${NC}"
    elif [ $warnings -gt 0 ]; then
        print_warning "${warnings} missing aliases could not be auto-healed (not in upstream either). Manual fix needed."
    else
        echo -e "  ${GREEN}${CHECKMARK}${NC} libs.versions.toml already in sync with build-logic — no heal needed."
    fi
}

if [ "$DRY_RUN" = false ]; then
    # Restore root-level excluded files
    restore_root_files

    cleanup_temp_dirs
    rm -rf temp_files

    # E0/T4: contract-keyed root merges (settings.gradle.kts include-union +
    # gradle/libs.versions.toml catalog-3way) so template module-includes AND version
    # bumps reach the fork. Runs BEFORE we drop the temp branch (needs upstream's copies).
    # Replaces the old one-way heal_libs_versions_toml (retained as a no-reader fallback).
    merge_contract_root_files

    # Cleanup temporary branch
    print_step "Cleaning up temporary branch..."
    git branch -D "$TEMP_BRANCH" || handle_error "Failed to delete temporary branch"
    show_progress

    # Record which template commit this sync pulled from. This is the whole point of the
    # `.template-version` marker: a fork can tell whether it is behind the template (compare
    # template_sha against the template's latest release) and the NEXT sync can anchor its 3-way
    # merge base on this exact point instead of git's guessed common ancestor. Written here (before
    # the commit gate) so a version-only bump still commits; the file is fork-owned
    # (customization-surface.yaml owner: fork) so a template dir-copy never clobbers it.
    # A PARTIAL sync never advances the base. The marker states what this tree fully holds; stamping
    # past a failed path makes the next 3-way merge treat never-received template content as the
    # fork's own deletion. Keep the previous marker, name what failed, and stop before committing.
    if [ "${#SYNC_FAILED[@]}" -gt 0 ]; then
        print_error "Sync INCOMPLETE — ${#SYNC_FAILED[@]} path(s) failed: ${SYNC_FAILED[*]}"
        print_error ".template-version NOT advanced (still the last COMPLETE sync). Fix the failures and re-run; nothing was committed."
        exit 1
    fi
    print_step "Recording template sync version (.template-version)..."
    _tv_sha="$(git rev-parse "$TEMPLATE_REF" 2>/dev/null || echo "")"
    _tv_tag="$(git describe --tags --exact-match "$_tv_sha" 2>/dev/null || echo "")"
    {
        echo "# .template-version — the openMF/kmp-project-template commit this tree last synced from."
        echo "# Managed by scripts/white-label/sync-dirs.sh — DO NOT hand-edit."
        echo "# Format: bash-parseable key=value (same convention as gradle/fork.properties)."
        echo "template_repo=openMF/kmp-project-template"
        echo "template_ref=$TEMPLATE_REF_LABEL"
        echo "template_sha=$_tv_sha"
        echo "template_tag=$_tv_tag"
        echo "synced_at=$(date -u +%Y-%m-%dT%H:%M:%SZ)"
    } > .template-version
    git add .template-version 2>/dev/null || true
    show_progress

    # Conflicts: every path DID sync, so the stamp above is right for the tree once the markers are
    # resolved — but a commit holding `<<<<<<<` must never be produced. Stop before committing; the
    # caller resolves each file (both sides' intent) and commits on this branch, stamp included.
    if [ "${#SYNC_CONFLICTS[@]}" -gt 0 ]; then
        print_error "Sync has ${#SYNC_CONFLICTS[@]} unresolved merge conflict(s) — markers are in the working tree:"
        printf '    %s\n' "${SYNC_CONFLICTS[@]}"
        print_error "Resolve each (keep both sides' intent), run: bash scripts/white-label/sync-dirs.sh --post-sync, then commit on this branch. Nothing was committed."
        exit 3
    fi

    # Check for changes
    if ! git diff --quiet --exit-code --cached; then
        print_step "Committing changes..."
        git add "${SYNC_DIRS[@]}" "${SYNC_FILES[@]}"
        # Include any catalog heal additions made by heal_libs_versions_toml.
        if [ -f "gradle/libs.versions.toml" ]; then
            git add "gradle/libs.versions.toml"
        fi
        _pre_post="$(dirty_paths)"
        post_sync_fork_steps
        git add -A -- "${SYNC_DIRS[@]}" "${SYNC_FILES[@]}" 2>/dev/null || true   # the strip deletes files
        stage_new_dirty "$_pre_post"

        # Lint only what the sync WROTE — its 3-way merge results. Template-owned files were checked in
        # the template and fork-owned files are untouched, so running the commit hook over the whole
        # staged sync re-checked hundreds of template files (206 s of a 275 s sync, measured
        # 2026-11-02) for nothing. Spotless formats the merged files; Detekt findings are reported for
        # the operator to fix on the branch (they come from the fork's side of the merge).
        _merged_kt=(); for _f in "${SYNC_MERGED[@]+"${SYNC_MERGED[@]}"}"; do
            case "$_f" in *.kt|*.kts) [ -f "$_f" ] && _merged_kt+=("$_f") ;; esac
        done
        if [ "${#_merged_kt[@]}" -gt 0 ] && [ -f scripts/ci/kotlin-checks.sh ]; then
            print_step "Spotless + Detekt on the ${#_merged_kt[@]} merged Kotlin file(s)"
            bash scripts/ci/kotlin-checks.sh files "${_merged_kt[@]}" \
                || print_warning "Spotless/Detekt flagged merged files (output above) — fix them on $SYNC_BRANCH"
            git add -- "${_merged_kt[@]}"
        fi
        git commit --no-verify -m "sync: Update directories and files from upstream

This PR syncs the following items with upstream:
- Directories: ${SYNC_DIRS[*]}
- Files: ${SYNC_FILES[*]}
- Auto-heal: gradle/libs.versions.toml (pulls missing build-logic aliases from upstream)" || handle_error "Failed to commit changes"
        show_progress

        # The engine never pushes. Pushing ran the fork's pre-push hook over the whole repo (40 min on
        # mifos-x-field-officer-app, then failed on pre-existing Detekt debt) and duplicated the push the
        # caller makes anyway — /kmp-project-template-sync commits and pushes through git-session.
        echo -e "\n${GREEN}${BOLD}✨ Sync committed on ${SYNC_BRANCH} (not pushed). ✨${NC}"
        echo -e "${YELLOW}Push it with your normal flow, e.g. ${BOLD}git push -u origin $SYNC_BRANCH${NC}${YELLOW}, then open a PR to ${BOLD}$BASE_BRANCH${NC}\n"
    else
        print_warning "No changes to commit"
        if [ "$SYNC_BRANCH_OWNED" = true ]; then
            git checkout "$BASE_BRANCH"
            git branch -D "$SYNC_BRANCH"
        fi
    fi
else
    echo -e "\n${YELLOW}${BOLD}Dry run completed. No changes were made.${NC}\n"
fi
