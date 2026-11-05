#!/bin/bash

# ==============================================================================
# APN Setup Verification Script
# ==============================================================================
# This script verifies that your APN configuration is correct
# ==============================================================================

set -e  # Exit on any error

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
cd "$PROJECT_ROOT"

# The ONE bash reader of gradle/fork.properties (scripts/_shared/fork-props.sh). Replaces an
# inline `grep | cut -d= | tr` that had neither head -1 nor inline-`#` stripping. Latent, not
# live: today's bridge has neither, but an inline comment would have made this Team ID
# `ABCD123456   # PLACEHOLDER — …` and a duplicated key would have concatenated values.
# shellcheck source=../_shared/fork-props.sh
. "$PROJECT_ROOT/scripts/_shared/fork-props.sh"

# Print functions
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_info() {
    echo -e "${CYAN}ℹ $1${NC}"
}

print_section() {
    echo
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo
}

# Print banner
print_section "🔍 APN Configuration Verification"

ERRORS=0
WARNINGS=0

# Check if APN secret files exist
print_info "Checking APN configuration files..."

APN_KEY_ID_FILE="secrets/live/apple/apn/key_id"
APN_TEAM_ID_FILE="secrets/live/apple/apn/team_id"
APN_KEY_FILE_PATH="secrets/live/apple/apn/APNAuthKey.p8"

if [ ! -f "$APN_KEY_ID_FILE" ]; then
    print_error "APN key_id file not found: $APN_KEY_ID_FILE"
    print_info "Run: bash scripts/ios/setup_apn_key.sh"
    exit 1
fi
print_success "APN key_id file exists: $APN_KEY_ID_FILE"

if [ ! -f "$APN_TEAM_ID_FILE" ]; then
    print_error "APN team_id file not found: $APN_TEAM_ID_FILE"
    print_info "Run: bash scripts/ios/setup_apn_key.sh"
    exit 1
fi
print_success "APN team_id file exists: $APN_TEAM_ID_FILE"

# Read APN values from per-value files
APN_KEY_ID=$(cat "$APN_KEY_ID_FILE" 2>/dev/null | tr -d '\n\r')
APN_TEAM_ID=$(cat "$APN_TEAM_ID_FILE" 2>/dev/null | tr -d '\n\r')
APN_KEY_PATH="$APN_KEY_FILE_PATH"

# Read TEAM_ID from fork.properties for cross-check
FORK_PROPERTIES="$PROJECT_ROOT/gradle/fork.properties"
TEAM_ID="$(fp_get apple.team.id)"

# Check APN configuration
print_section "📋 Checking APN Configuration"

if [ -z "$APN_KEY_ID" ]; then
    print_error "APN key_id is empty: $APN_KEY_ID_FILE"
    print_info "Run: bash scripts/ios/setup_apn_key.sh"
    ((ERRORS++))
else
    # Validate format (10 characters)
    if [[ ! $APN_KEY_ID =~ ^[A-Z0-9]{10}$ ]]; then
        print_error "APN_KEY_ID has invalid format: (see $APN_KEY_ID_FILE)"
        print_info "Must be 10 characters (letters and numbers)"
        ((ERRORS++))
    else
        print_success "APN_KEY_ID format valid"
    fi
fi

if [ -z "$APN_TEAM_ID" ]; then
    print_error "APN team_id is empty: $APN_TEAM_ID_FILE"
    ((ERRORS++))
else
    print_success "APN_TEAM_ID set (from $APN_TEAM_ID_FILE)"

    # Check if it matches apple.team.id in fork.properties
    if [ -n "$TEAM_ID" ] && [ "$APN_TEAM_ID" != "$TEAM_ID" ]; then
        print_warning "APN team_id differs from apple.team.id in gradle/fork.properties"
        print_info "They should usually be the same"
        ((WARNINGS++))
    fi
fi

if [ ! -f "$APN_KEY_PATH" ]; then
    print_error "APN key file not found: $APN_KEY_PATH"
    ((ERRORS++))
else
    print_success "APN_KEY_PATH exists: $APN_KEY_PATH"
fi

# Check APN key file
print_section "🔑 Checking APN Key File"

APN_KEY_FILE="secrets/live/apple/apn/APNAuthKey.p8"

if [ ! -f "$APN_KEY_FILE" ]; then
    print_error "APN key file not found: $APN_KEY_FILE"
    print_info "Run: bash scripts/ios/setup_apn_key.sh"
    ((ERRORS++))
else
    print_success "APN key file exists: $APN_KEY_FILE"

    # Check file permissions
    PERMS=$(stat -f "%A" "$APN_KEY_FILE" 2>/dev/null || stat -c "%a" "$APN_KEY_FILE" 2>/dev/null)
    if [ "$PERMS" != "600" ]; then
        print_warning "APN key file permissions: $PERMS (should be 600)"
        print_info "Fix with: chmod 600 $APN_KEY_FILE"
        ((WARNINGS++))
    else
        print_success "File permissions correct: 600"
    fi

    # Validate it's a valid P8 key
    if ! grep -q "BEGIN PRIVATE KEY" "$APN_KEY_FILE"; then
        print_error "File doesn't appear to be a valid .p8 key"
        ((ERRORS++))
    else
        print_success "Valid P8 key format detected"
    fi

    # Check file size (should be around 200-300 bytes for a valid key)
    FILE_SIZE=$(wc -c < "$APN_KEY_FILE" | tr -d ' ')
    if [ "$FILE_SIZE" -lt 100 ] || [ "$FILE_SIZE" -gt 500 ]; then
        print_warning "APN key file size unusual: $FILE_SIZE bytes"
        print_info "Valid P8 keys are typically 200-300 bytes"
        ((WARNINGS++))
    else
        print_success "File size looks reasonable: $FILE_SIZE bytes"
    fi
fi

# Check if key filename matches Key ID
if [ -f "$APN_KEY_FILE" ] && [ -n "$APN_KEY_ID" ]; then
    EXPECTED_NAME="AuthKey_$APN_KEY_ID.p8"
    if [ "$(basename $APN_KEY_FILE)" != "$EXPECTED_NAME" ]; then
        print_warning "APN key filename: $(basename $APN_KEY_FILE)"
        print_info "Expected filename based on Key ID: $EXPECTED_NAME"
        print_info "This is usually OK, but verify the Key ID is correct"
        ((WARNINGS++))
    fi
fi

# Firebase Console Check
print_section "🔥 Firebase Console Integration"

print_info "Manual verification needed in Firebase Console:"
echo "  1. Go to: https://console.firebase.google.com/"
echo "  2. Select your project"
echo "  3. Settings → Cloud Messaging tab"
echo "  4. Check 'APNs authentication key' section"
echo "  5. Verify:"
if [ -n "$APN_KEY_ID" ]; then
    echo "     - Key ID matches: $APN_KEY_ID"
fi
if [ -n "$APN_TEAM_ID" ]; then
    echo "     - Team ID matches: $APN_TEAM_ID"
fi
echo

read -p "Have you verified the APN key in Firebase Console? [y/N]: " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    print_warning "Please verify APN configuration in Firebase Console"
    ((WARNINGS++))
else
    print_success "Firebase Console verification confirmed"
fi

# Summary
print_section "📊 Verification Summary"

if [ $ERRORS -eq 0 ] && [ $WARNINGS -eq 0 ]; then
    print_success "✅ All checks passed!"
    echo
    print_info "Your APN configuration is correct and ready to use"
    echo
    print_info "Next Steps:"
    echo "  1. Test push notifications in your app"
    echo "  2. Check Firebase Console → Cloud Messaging → Send test message"
    echo "  3. Monitor delivery reports in Firebase Console"
elif [ $ERRORS -eq 0 ]; then
    print_warning "⚠️  Configuration OK with $WARNINGS warning(s)"
    echo
    print_info "Your APN configuration should work, but review the warnings above"
else
    print_error "❌ Found $ERRORS error(s) and $WARNINGS warning(s)"
    echo
    print_info "Please fix the errors above before using APN"
    print_info "Run: bash scripts/ios/setup_apn_key.sh"
    exit 1
fi

print_info "Configuration Details:"
if [ -n "$APN_KEY_ID" ]; then
    echo "  • APN Key ID: $APN_KEY_ID"
fi
if [ -n "$APN_TEAM_ID" ]; then
    echo "  • APN Team ID: $APN_TEAM_ID"
fi
if [ -f "$APN_KEY_FILE" ]; then
    echo "  • APN Key File: $APN_KEY_FILE"
fi
echo

print_success "Verification complete! 🔔"
