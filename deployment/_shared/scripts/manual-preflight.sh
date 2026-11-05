#!/usr/bin/env bash
# =============================================================================
# deployment/_shared/scripts/manual-preflight.sh
#
# Manual-mode secrets preflight runner for /release {platform} {target}.
#
# Contract: framework snippet `layers/secrets/snippets/manual-preflight.md` AC80.
# Authority: RULE-SECRETS-VAULT-001 (manual-mode arm), RULE-DEPLOYMENT-MANIFEST-001 DM-CAP.
# PLAN: plan-layer/project-plans/.../fastlane-modernization/11-release-dispatcher-update.md
#
# For each `manual_inputs[]` row in `deployment/<platform>/<target>/secrets-needs.yaml`:
#   1. Exists           — test -f canonical_path
#   2. Non-empty        — [ -s canonical_path ]
#   3. Non-placeholder  — first 16 bytes != "CLAUDE-PLHLD-v1" (binary)
#                         AND first line != "# CLAUDE-PLACEHOLDER" (text)
#                         AND not byte-identical to declared placeholder ref
#
# Exit 0 iff every row passes all three checks. Exit 1 otherwise with PASS/FAIL
# table including source_hint and gha_secret_var per failing row.
#
# Usage:
#   bash deployment/_shared/scripts/manual-preflight.sh <target>
#   bash deployment/_shared/scripts/manual-preflight.sh --target-dir <path>
# =============================================================================

set -uo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
PROJECT_ROOT="$(cd "$PROJECT_ROOT/.." && pwd)"

# ---- arg parsing ----------------------------------------------------------
TARGET=""
TARGET_DIR=""
QUIET=0
while [ $# -gt 0 ]; do
    case "$1" in
        --target-dir) shift; TARGET_DIR="${1:-}" ;;
        --quiet)      QUIET=1 ;;
        -h|--help)
            sed -n '1,30p' "${BASH_SOURCE[0]}"
            exit 0
            ;;
        --*)
            echo "Unknown flag: $1" >&2
            exit 2
            ;;
        *)
            if [ -z "$TARGET" ]; then
                TARGET="$1"
            else
                echo "Unexpected arg: $1" >&2
                exit 2
            fi
            ;;
    esac
    shift
done

if [ -z "$TARGET" ] && [ -z "$TARGET_DIR" ]; then
    echo "Usage: manual-preflight.sh <target> | --target-dir <path>" >&2
    exit 2
fi

# ---- resolve target dir ---------------------------------------------------
if [ -z "$TARGET_DIR" ]; then
    # Locate <target> under deployment/<platform>/<target>/ by scanning.
    MANIFEST="$PROJECT_ROOT/deployment/DEPLOYMENT_MANIFEST.yaml"
    if [ ! -f "$MANIFEST" ]; then
        echo "[ERROR] DEPLOYMENT_MANIFEST.yaml not found at $MANIFEST" >&2
        exit 1
    fi
    # Find by canonical_name (yq required)
    if ! command -v yq >/dev/null 2>&1; then
        echo "[ERROR] yq is required for target resolution" >&2
        exit 1
    fi
    TARGET_PATH=$(yq -r ".enabled_targets[] | select(.canonical_name == \"$TARGET\" or .canonical_name == \"android-$TARGET\" or .canonical_name == \"ios-$TARGET\" or .canonical_name == \"desktop-$TARGET\" or .canonical_name == \"web-$TARGET\") | .path" "$MANIFEST" 2>/dev/null | head -n1)
    if [ -z "$TARGET_PATH" ] || [ "$TARGET_PATH" = "null" ]; then
        echo "[ERROR] Target '$TARGET' not found in DEPLOYMENT_MANIFEST.yaml#enabled_targets[]" >&2
        # List valid
        echo "Valid targets:" >&2
        yq -r '.enabled_targets[].canonical_name' "$MANIFEST" | sed 's/^/  - /' >&2
        exit 1
    fi
    TARGET_DIR="$PROJECT_ROOT/$TARGET_PATH"
fi

# ---- load secrets-needs.yaml ---------------------------------------------
NEEDS_FILE="$TARGET_DIR/secrets-needs.yaml"
if [ ! -f "$NEEDS_FILE" ]; then
    echo "[ERROR] secrets-needs.yaml not found at $NEEDS_FILE" >&2
    exit 1
fi

if ! command -v yq >/dev/null 2>&1; then
    echo "[ERROR] yq is required for YAML parsing" >&2
    exit 1
fi

# Count manual_inputs rows
TOTAL=$(yq -r '.manual_inputs | length' "$NEEDS_FILE" 2>/dev/null)
if [ -z "$TOTAL" ] || [ "$TOTAL" = "null" ] || [ "$TOTAL" = "0" ]; then
    [ "$QUIET" = "1" ] || echo "ℹ no manual_inputs[] declared in $NEEDS_FILE — preflight is a no-op (vault-only target?)"
    exit 0
fi

# ---- per-row check --------------------------------------------------------
PASS=0
FAIL=0
declare -a FAIL_ROWS=()

# True when $1 looks like text, per file(1).
# Used to decide WHICH placeholder probe applies: a text secret carries a
# `# CLAUDE-PLACEHOLDER` first line, a binary one a magic prefix.
is_text_file() {
    # Returns 0 if file looks like text (ASCII / UTF-8 / regular text file)
    local f="$1"
    [ ! -f "$f" ] && return 1
    file -b "$f" 2>/dev/null | grep -qiE 'text|ASCII|UTF-8|JSON|XML|YAML|empty'
}

# True when text file $1 is still the shipped placeholder rather than a real secret.
check_placeholder_text() {
    local f="$1"
    local first_line
    first_line=$(head -n1 "$f" 2>/dev/null || true)
    [ "$first_line" = "# CLAUDE-PLACEHOLDER" ] && return 0 || return 1
}

# True when binary file $1 still carries the placeholder magic prefix.
# Checks the first 16 bytes only, so it is safe on a large keystore.
check_placeholder_binary() {
    local f="$1"
    local first16
    first16=$(head -c16 "$f" 2>/dev/null || true)
    [ "$first16" = "CLAUDE-PLHLD-v1" ] && return 0 || return 1
}

for i in $(seq 0 $((TOTAL - 1))); do
    CANON=$(yq -r ".manual_inputs[$i].canonical" "$NEEDS_FILE")
    HINT=$(yq -r ".manual_inputs[$i].source_hint // \"\"" "$NEEDS_FILE")
    PLACEHOLDER=$(yq -r ".manual_inputs[$i].placeholder // \"\"" "$NEEDS_FILE")
    GHA_VAR=$(yq -r ".manual_inputs[$i].gha_secret_var // \"\"" "$NEEDS_FILE")
    CANON_PATH="$PROJECT_ROOT/$CANON"
    PLACEHOLDER_PATH="$PROJECT_ROOT/$PLACEHOLDER"

    REASON=""
    if [ ! -f "$CANON_PATH" ]; then
        REASON="MISSING"
    elif [ ! -s "$CANON_PATH" ]; then
        REASON="EMPTY"
    else
        # Non-placeholder checks
        if is_text_file "$CANON_PATH"; then
            if check_placeholder_text "$CANON_PATH"; then
                REASON="PLACEHOLDER"
            fi
        else
            if check_placeholder_binary "$CANON_PATH"; then
                REASON="PLACEHOLDER"
            fi
        fi
        # Demo-copy soft check (only if placeholder ref exists + still no reason set)
        if [ -z "$REASON" ] && [ -n "$PLACEHOLDER" ] && [ -f "$PLACEHOLDER_PATH" ]; then
            if cmp -s "$CANON_PATH" "$PLACEHOLDER_PATH"; then
                REASON="DEMO-COPY"
            fi
        fi
    fi

    if [ -z "$REASON" ]; then
        [ "$QUIET" = "1" ] || echo "  OK         $CANON"
        PASS=$((PASS + 1))
    else
        [ "$QUIET" = "1" ] || echo "  $REASON $CANON"
        FAIL_ROWS+=("$CANON|$HINT|$PLACEHOLDER|$GHA_VAR|$REASON")
        FAIL=$((FAIL + 1))
    fi
done

# ---- failure detail -------------------------------------------------------
if [ "$FAIL" -gt 0 ]; then
    echo
    echo "❌ manual-preflight: FAIL"
    echo
    for row in ${FAIL_ROWS[@]+"${FAIL_ROWS[@]}"}; do
        IFS='|' read -r r_canon r_hint r_placeholder r_gha r_reason <<< "$row"
        echo "[FAIL] needs[].canonical=$r_canon"
        echo "       source_hint:    $r_hint"
        echo "       placeholder:    $r_placeholder"
        echo "       gha_secret_var: $r_gha"
        echo "       reason:         $r_reason"
        echo
    done
fi

# ---- summary --------------------------------------------------------------
echo "manual-preflight: PASS=$PASS  FAIL=$FAIL  TOTAL=$TOTAL"

# ---- auto-init detector (G45 / T19) ---------------------------------------
# If EVERY row reports MISSING or secrets/ doesn't exist, AND we're in a TTY,
# offer to bootstrap from secrets/sample/.
if [ "$FAIL" -eq "$TOTAL" ] || [ ! -d "$PROJECT_ROOT/secrets" ]; then
    if [ -t 0 ] && [ "${SKIP_AUTO_INIT:-0}" != "1" ]; then
        echo
        echo "[INFO] secrets/ appears uninitialized. Run secrets-init-from-demo.sh now?" >&2
        printf "[I] Init now (recommended) / [M] I'll handle / [C] Cancel " >&2
        read -r CHOICE
        case "${CHOICE:-I}" in
            I|i)
                if [ -f "$SCRIPT_DIR/secrets-init-from-demo.sh" ]; then
                    bash "$SCRIPT_DIR/secrets-init-from-demo.sh"
                    SKIP_AUTO_INIT=1 exec "$0" "$@"
                else
                    echo "[WARN] secrets-init-from-demo.sh not found; cannot auto-init." >&2
                fi
                ;;
            M|m) ;;     # fall through to standard error report
            C|c) exit 0 ;;
        esac
    fi
fi

[ "$FAIL" -eq 0 ] && exit 0 || exit 1
