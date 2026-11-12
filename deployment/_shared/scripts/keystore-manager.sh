#!/bin/bash

# Android Keystore Generator and GitHub Secrets Management Script
# This script generates Android keystores and manages GitHub secrets

set -e  # Exit on any error

# The ONE bash reader of gradle/fork.properties. Self-locating because this script is invoked both
# from the repo root and from deployment/, so a relative source path is not safe.
# shellcheck source=../../../scripts/_shared/fork-props.sh
_KM_REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)"
. "$_KM_REPO_ROOT/scripts/_shared/fork-props.sh"

# Colors for better readability
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m' # No Color

# Print helper functions
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

# Print a red failure line.
print_error() {
    echo -e "${RED}✗ $1${NC}"
}

# Print a yellow warning line — the run continues.
print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

# Print a cyan informational line.
print_info() {
    echo -e "${CYAN}ℹ $1${NC}"
}

# Default environment file paths (secrets/ is preferred, root is legacy)
SECRETS_DIR_ENV_FILE="secrets/shared/secrets.env"
ROOT_ENV_FILE="secrets.env"
ENV_FILE=""  # Will be resolved after argument parsing

# Default values
COMMAND="generate"
REPO=""
ENV=""
SECRET_NAME=""
ENV_FILE_OVERRIDE=""  # User-specified --env-file path

# Keys that should not be sent to GitHub
EXCLUDED_GITHUB_KEYS=(
    "COMPANY_NAME"
    "DEPARTMENT"
    "ORGANIZATION"
    "CITY"
    "STATE"
    "COUNTRY"
    "VALIDITY"
    "KEYALG"
    "KEYSIZE"
    "OVERWRITE"
    "UPLOAD_KEYSTORE_NAME"
    "CN"
    "OU"
    "O"
    "L"
    "ST"
    "C"
)

# Global associative array for iOS string secrets
declare -g -A IOS_STRING_SECRETS

# Function to strip quotes from values
strip_quotes() {
    local value="$1"
    # Remove surrounding double quotes if present
    value="${value#\"}"
    value="${value%\"}"
    # Remove surrounding single quotes if present
    value="${value#\'}"
    value="${value%\'}"
    echo "$value"
}

# Resolve which secrets.env file to use
# Priority: --env-file override > secrets/shared/secrets.env > root secrets.env
resolve_env_file() {
    # If user specified --env-file, use that
    if [[ -n "$ENV_FILE_OVERRIDE" ]]; then
        ENV_FILE="$ENV_FILE_OVERRIDE"
        return 0
    fi

    local secrets_dir_exists=false
    local root_exists=false

    [[ -f "$SECRETS_DIR_ENV_FILE" ]] && secrets_dir_exists=true
    [[ -f "$ROOT_ENV_FILE" ]] && root_exists=true

    # Both exist - ask user to choose
    if [[ "$secrets_dir_exists" = true ]] && [[ "$root_exists" = true ]]; then
        echo -e "${YELLOW}Found secrets.env in two locations:${NC}"
        echo -e "  ${CYAN}[1]${NC} secrets/shared/secrets.env  (recommended)"
        echo -e "  ${CYAN}[2]${NC} secrets.env          (legacy/root)"
        echo ""
        read -r -p "Which file should be used? [1/2] (default: 1): " choice
        case "$choice" in
            2)
                ENV_FILE="$ROOT_ENV_FILE"
                print_info "Using root: $ROOT_ENV_FILE"
                ;;
            *)
                ENV_FILE="$SECRETS_DIR_ENV_FILE"
                print_info "Using secrets dir: $SECRETS_DIR_ENV_FILE"
                ;;
        esac
    elif [[ "$secrets_dir_exists" = true ]]; then
        ENV_FILE="$SECRETS_DIR_ENV_FILE"
    elif [[ "$root_exists" = true ]]; then
        ENV_FILE="$ROOT_ENV_FILE"
    else
        # Neither exists - default to secrets/ (will be created by generate/sync)
        ENV_FILE="$SECRETS_DIR_ENV_FILE"
    fi
}

# Load variables from secrets.env if it exists (simple variables only)
load_env_vars() {
    local env_file="$1"
    local show_message="$2"

    if [ -f "$env_file" ]; then
        if [ "$show_message" = "true" ]; then
            echo -e "${BLUE}Loading configuration from $env_file${NC}"
        fi

        # Only load simple variables (KEY=VALUE format), ignore multiline blocks
        local in_multiline=false
        local multiline_end=""

        while IFS= read -r line; do
            # Skip comments and blank lines
            if [ "$in_multiline" = false ] && [[ -z "$line" || "$line" == \#* ]]; then
                continue
            fi

            # Check if we're entering a multiline block
            if [ "$in_multiline" = false ] && [[ "$line" == *"<<"* ]]; then
                multiline_end=$(echo "$line" | sed 's/.*<<\(.*\)/\1/')
                in_multiline=true
                continue
            fi

            # Check if we're exiting a multiline block
            if [ "$in_multiline" = true ] && [[ "$line" == "$multiline_end" ]]; then
                in_multiline=false
                continue
            fi

            # Skip lines inside multiline blocks
            if [ "$in_multiline" = true ]; then
                continue
            fi

            # Process regular KEY=VALUE pairs
            if [[ "$line" == *"="* ]]; then
                # Extract the variable name
                local key=$(echo "$line" | cut -d '=' -f1 | xargs)
                # Extract the value (anything after the first =)
                local value=$(echo "$line" | cut -d '=' -f2-)
                # Export the variable
                export "$key"="$value"
            fi
        done < "$env_file"
    fi
}

# Function to display help
show_help() {
    echo -e "${BLUE}Android Keystore Generator and GitHub Secrets Management Script${NC}"
    echo ""
    echo "Usage:"
    echo "  ./keystore-manager.sh [COMMAND] [OPTIONS]"
    echo ""
    echo "Commands:"
    echo "  generate - Generate Android keystores; reads DN from gradle/fork.properties,"
    echo "             reads passwords from secrets/live/android/keystores/ files (default)"
    echo "  encode-secrets - [DEPRECATED] Encode files from secrets/ directory and update"
    echo "             secrets/shared/secrets.env. Use scripts/secrets/sync-secrets-to-github.sh"
    echo "             (the modern replacement) to push secrets from secrets/ to GitHub."
    echo "  sync     - Validate secrets.env format and completeness"
    echo "  view     - View all secrets in the secrets.env file as a formatted table"
    echo "  add      - [DEPRECATED] Add secrets to a GitHub repository from secrets.env."
    echo "             Use scripts/secrets/sync-secrets-to-github.sh instead."
    echo "  list     - List all secrets in a GitHub repository"
    echo "  delete   - Delete a secret from a GitHub repository"
    echo "  delete-all - [DEPRECATED] Delete all secrets from a GitHub repository that are"
    echo "             in secrets.env. Use scripts/secrets/sync-secrets-to-github.sh instead."
    echo "               Use --include-excluded flag to also delete excluded secrets"
    echo "  help     - Show this help message"
    echo ""
    echo "Options:"
    echo "  --repo=username/repo      - GitHub repository name"
    echo "  --env=environment         - GitHub environment name"
    echo "  --name=SECRET_NAME        - Secret name (for delete command)"
    echo "  --env-file=path           - Override secrets.env file path"
    echo "                              (default: secrets/shared/secrets.env, fallback: secrets.env)"
    echo ""
    echo "Examples:"
    echo "  ./keystore-manager.sh generate"
    echo "  ./keystore-manager.sh encode-secrets"
    echo "  ./keystore-manager.sh sync"
    echo "  ./keystore-manager.sh view"
    echo "  ./keystore-manager.sh add --repo=username/repo"
    echo "  ./keystore-manager.sh list --repo=username/repo"
    echo "  ./keystore-manager.sh delete --repo=username/repo --name=SECRET_NAME"
    echo " ./keystore-manager.sh delete-all --repo=username/repo [--env=environment]"
    echo " ./keystore-manager.sh delete-all --repo=username/repo [--env=environment] --include-excluded"

}

# Function to view secrets from secrets.env in a table
view_secrets() {
    if [ ! -f "$ENV_FILE" ]; then
        echo -e "${RED}Error: $ENV_FILE file not found.${NC}"
        exit 1
    fi

    echo -e "${BLUE}Loading configuration from $ENV_FILE${NC}"
    echo -e "${BLUE}Viewing secrets from $ENV_FILE${NC}"
    echo ""

    # Calculate column widths
    KEY_WIDTH=30
    VALUE_WIDTH=50
    TOTAL_WIDTH=$((KEY_WIDTH + VALUE_WIDTH + 5))  # 5 for borders and spacing

    # Function to print horizontal border
    print_border() {
        local char=$1
        local width=$2
        printf "${CYAN}%*s${NC}\n" "$width" | tr " " "$char"
    }

    # Print table header
    print_border "═" $TOTAL_WIDTH
    printf "${CYAN}║${BOLD} %-${KEY_WIDTH}s ${CYAN}║${BOLD} %-${VALUE_WIDTH}s ${CYAN}║${NC}\n" "SECRET KEY" "VALUE"
    print_border "═" $TOTAL_WIDTH

    # Process the file line by line with support for multiline values
    local multiline_mode=false
    local multiline_end=""

    while IFS= read -r line || [ -n "$line" ]; do
        # Skip empty lines and comments when not in multiline mode
        if [ "$multiline_mode" = false ] && [[ -z "$line" || "$line" == \#* ]]; then
            continue
        fi

        # Check if we're exiting a multiline block
        if [ "$multiline_mode" = true ] && [[ "$line" == "$multiline_end" ]]; then
            multiline_mode=false
            continue
        fi

        # Skip content lines inside multiline blocks
        if [ "$multiline_mode" = true ]; then
            continue
        fi

        # Check if this is the start of a multiline value
        if [[ "$line" == *"<<"* ]]; then
            # Extract the key (part before <<)
            local key=$(echo "$line" | cut -d '<' -f1 | xargs)
            # Extract the delimiter (part after <<)
            multiline_end=$(echo "$line" | sed 's/.*<<\(.*\)/\1/')
            multiline_mode=true

            # Print the multiline value immediately
            printf "${CYAN}║${NC} ${YELLOW}%-${KEY_WIDTH}s${NC} ${CYAN}║${NC} ${GREEN}%-${VALUE_WIDTH}s${NC} ${CYAN}║${NC}\n" "$key" "[MULTILINE VALUE]"
        elif [[ "$line" == *"="* ]]; then
            # This is a regular key=value line
            local key=$(echo "$line" | cut -d '=' -f1 | xargs)
            local value=$(echo "$line" | cut -d '=' -f2-)

            # Strip quotes for display
            value=$(strip_quotes "$value")

            # Truncate value if too long
            local display_value=""
            if [ ${#value} -gt $VALUE_WIDTH ]; then
                display_value="${value:0:$((VALUE_WIDTH-5))}..."
            else
                display_value="$value"
            fi

            # Print the regular key-value pair
            printf "${CYAN}║${NC} ${YELLOW}%-${KEY_WIDTH}s${NC} ${CYAN}║${NC} ${GREEN}%-${VALUE_WIDTH}s${NC} ${CYAN}║${NC}\n" "$key" "$display_value"
        fi
    done < "$ENV_FILE"

    # Print table footer
    print_border "═" $TOTAL_WIDTH

    # Help message for multiline values
    echo -e "${BLUE}Note: For multiline values, the content is displayed as [MULTILINE VALUE]${NC}"
}

# Function to check if keytool is available
check_keytool() {
    if ! command -v keytool &> /dev/null; then
        echo -e "${RED}Error: keytool command not found.${NC}"
        echo -e "Please ensure you have Java Development Kit (JDK) installed and that keytool is in your PATH."
        exit 1
    fi
}

# Function to check if gh CLI is available
check_gh_cli() {
    if ! command -v gh &> /dev/null; then
        echo -e "${RED}GitHub CLI (gh) is not installed. Please install it first:${NC}"
        echo -e "https://cli.github.com/manual/installation"
        exit 1
    fi

    # Check if user is authenticated
    if ! gh auth status &> /dev/null; then
        echo -e "${RED}You are not logged in to GitHub CLI. Please run:${NC}"
        echo -e "${BLUE}gh auth login${NC}"
        exit 1
    fi
}

# Function to create keystores directory
create_keystores_dir() {
    if [ ! -d "keystores" ]; then
        echo -e "${BLUE}Creating 'keystores' directory...${NC}"
        mkdir -p keystores
        if [ $? -ne 0 ]; then
            echo -e "${RED}Error: Failed to create 'keystores' directory.${NC}"
            exit 1
        fi
    fi
}

# Function to encode file to base64
encode_base64() {
    local file_path=$1
    if [ -f "$file_path" ]; then
        if [[ "$OSTYPE" == "darwin"* ]]; then
            # macOS requires -i flag for input file
            base64 -i "$file_path"
        else
            # Linux accepts positional argument and -w 0 for no wrapping
            base64 -w 0 "$file_path"
        fi
    else
        echo -e "${RED}Error: File not found: $file_path${NC}"
        return 1
    fi
}

# Function to create secrets directory if it doesn't exist
create_secrets_dir() {
    if [ ! -d "secrets" ]; then
        echo -e "${BLUE}Creating 'secrets' directory...${NC}"
        mkdir -p secrets
        if [ $? -ne 0 ]; then
            echo -e "${RED}Error: Failed to create 'secrets' directory.${NC}"
            exit 1
        fi
    fi
}

# Parse iOS string secrets from the new two-source model:
#   - Non-secret identity/metadata → gradle/fork.properties
#   - Secret values → per-value files under secrets/live/apple/...
#
# DEPRECATED: this function previously read secrets/shared/shared_keys.env.
# That file is being retired. All callers continue to work through this
# function — only the read sources have changed. Do not remove this function;
# it may still be called by callers outside this file.
parse_shared_keys_env() {
    local FORK_PROPS="gradle/fork.properties"

    # Skip if neither source is present (Android-only setup)
    if [[ ! -f "$FORK_PROPS" ]] && [[ ! -f "secrets/live/apple/appstore/key_id" ]]; then
        print_info "iOS configuration not found - skipping iOS secrets (Android-only project)"
        return 0
    fi

    print_info "Parsing iOS secrets from fork.properties + secrets/ files..."

    # Read MATCH_PASSWORD from .match_password file if it exists
    local MATCH_PWD=""
    if [[ -f "secrets/live/apple/match/.match_password" ]]; then
        MATCH_PWD=$(head -n1 secrets/live/apple/match/.match_password 2>/dev/null | tr -d '\n\r')
        print_success "Loaded MATCH_PASSWORD from .match_password file"
    else
        print_warning "secrets/live/apple/match/.match_password not found - MATCH_PASSWORD will be empty"
    fi

    # Extract non-secret identity from fork.properties
    local NOTARIZATION_TEAM_ID=$(_read_fork_prop "$FORK_PROPS" apple.team.id)

    # Extract secret values from per-value files
    local APPSTORE_KEY_ID=$(cat "secrets/live/apple/appstore/key_id" 2>/dev/null | tr -d '\n\r')
    local APPSTORE_ISSUER_ID=$(cat "secrets/live/apple/appstore/issuer_id" 2>/dev/null | tr -d '\n\r')
    local NOTARIZATION_APPLE_ID=""   # not yet migrated to a file; leave empty
    local NOTARIZATION_PASSWORD=""   # not yet migrated to a file; leave empty

    # Validate critical values
    if [[ -z "$APPSTORE_KEY_ID" ]]; then
        print_warning "APPSTORE_KEY_ID is empty - App Store Connect API key may not be configured"
    fi
    if [[ -z "$APPSTORE_ISSUER_ID" ]]; then
        print_warning "APPSTORE_ISSUER_ID is empty - App Store Connect API issuer may not be configured"
    fi

    # Populate global associative array (declared at top of script)
    IOS_STRING_SECRETS["APPSTORE_KEY_ID"]="$APPSTORE_KEY_ID"
    IOS_STRING_SECRETS["APPSTORE_ISSUER_ID"]="$APPSTORE_ISSUER_ID"
    IOS_STRING_SECRETS["MATCH_PASSWORD"]="$MATCH_PWD"
    IOS_STRING_SECRETS["NOTARIZATION_APPLE_ID"]="$NOTARIZATION_APPLE_ID"
    IOS_STRING_SECRETS["NOTARIZATION_PASSWORD"]="$NOTARIZATION_PASSWORD"
    IOS_STRING_SECRETS["NOTARIZATION_TEAM_ID"]="$NOTARIZATION_TEAM_ID"

    # Print summary
    local count=0
    for key in "${!IOS_STRING_SECRETS[@]}"; do
        if [[ -n "${IOS_STRING_SECRETS[$key]}" ]]; then
            count=$((count + 1))
        fi
    done

    print_success "Found $count of 6 iOS string secrets"
}

# Global associative array for macOS password secrets
declare -g -A MACOS_PASSWORD_SECRETS

# Parse macOS password secrets from dotfiles in secrets/
parse_macos_password_files() {
    print_info "Parsing macOS password files from secrets/..."

    local count=0

    # Read KEYCHAIN_PASSWORD from .keychain_password file
    if [[ -f "secrets/live/apple/match/.keychain_password" ]]; then
        local val
        val=$(head -n1 secrets/live/apple/match/.keychain_password 2>/dev/null | tr -d '\n\r')
        if [[ -n "$val" ]]; then
            MACOS_PASSWORD_SECRETS["KEYCHAIN_PASSWORD"]="$val"
            count=$((count + 1))
            print_success "Loaded KEYCHAIN_PASSWORD from .keychain_password file"
        else
            print_warning ".keychain_password file is empty"
        fi
    else
        print_info "secrets/live/apple/match/.keychain_password not found - KEYCHAIN_PASSWORD will remain as-is"
    fi

    # Read CERTIFICATES_PASSWORD from .certificates_password file
    if [[ -f "secrets/live/apple/match/.certificates_password" ]]; then
        local val
        val=$(head -n1 secrets/live/apple/match/.certificates_password 2>/dev/null | tr -d '\n\r')
        if [[ -n "$val" ]]; then
            MACOS_PASSWORD_SECRETS["CERTIFICATES_PASSWORD"]="$val"
            count=$((count + 1))
            print_success "Loaded CERTIFICATES_PASSWORD from .certificates_password file"
        else
            print_warning ".certificates_password file is empty"
        fi
    else
        print_info "secrets/live/apple/match/.certificates_password not found - CERTIFICATES_PASSWORD will remain as-is"
    fi

    print_success "Found $count of 2 macOS password secrets"
}

# Update macOS password secrets in secrets.env
# Always ensures KEYCHAIN_PASSWORD and CERTIFICATES_PASSWORD exist in the file.
# Populates from password files if available, otherwise adds empty placeholders.
update_macos_password_secrets() {
    local SECRETS_FILE="$ENV_FILE"

    if [[ ! -f "$SECRETS_FILE" ]]; then
        print_info "No secrets file to update macOS passwords in"
        return 0
    fi

    print_info "Updating macOS password secrets in $SECRETS_FILE..."

    # Keys we must ensure exist
    local required_keys=("KEYCHAIN_PASSWORD" "CERTIFICATES_PASSWORD")

    for key in "${required_keys[@]}"; do
        # Get value from parsed password files (may be empty)
        local value="${MACOS_PASSWORD_SECRETS[$key]:-}"

        if grep -q "^${key}=" "$SECRETS_FILE" 2>/dev/null; then
            # Key exists - update only if we have a non-empty value
            if [[ -n "$value" ]]; then
                local escaped_value
                escaped_value=$(printf '%s\n' "$value" | sed 's/[&/\]/\\&/g')
                sed -i.bak "s|^${key}=.*|${key}=\"${escaped_value}\"|" "$SECRETS_FILE"
                print_success "Updated $key"
            else
                print_info "Preserving existing $key (no password file found)"
            fi
        else
            # Key doesn't exist - add after macOS App Store section header (or end of file)
            local escaped_value=""
            [[ -n "$value" ]] && escaped_value=$(printf '%s\n' "$value" | sed 's/[&/\]/\\&/g')

            local section_line
            section_line=$(grep -n "^# macOS App Store" "$SECRETS_FILE" 2>/dev/null | head -1 | cut -d: -f1)
            if [[ -n "$section_line" ]]; then
                # Find end of comments block after section header
                local insert_line=$((section_line + 1))
                local total_lines
                total_lines=$(wc -l < "$SECRETS_FILE")
                # Skip past comment lines to find insertion point
                while [[ $insert_line -le $total_lines ]]; do
                    local line_content
                    line_content=$(sed -n "${insert_line}p" "$SECRETS_FILE")
                    if [[ "$line_content" != \#* ]] && [[ -n "$line_content" ]]; then
                        break
                    fi
                    insert_line=$((insert_line + 1))
                done
                {
                    head -n $((insert_line - 1)) "$SECRETS_FILE"
                    echo "${key}=\"${escaped_value}\""
                    tail -n +${insert_line} "$SECRETS_FILE"
                } > "${SECRETS_FILE}.tmp" && mv "${SECRETS_FILE}.tmp" "$SECRETS_FILE"
                print_success "Added $key to macOS App Store section"
            else
                echo "${key}=\"${escaped_value}\"" >> "$SECRETS_FILE"
                print_success "Appended $key to end of file"
            fi
        fi
    done

    rm -f "${SECRETS_FILE}.bak"
}

# Function to encode secrets directory files and update secrets.env
encode_secrets_directory_files() {
    echo -e "${BLUE}==================================================================${NC}"
    echo -e "${BLUE}Encoding files from secrets/ directory${NC}"
    echo -e "${BLUE}==================================================================${NC}"

    # Mapping of file names to secret names, as "file|secret" pairs.
    #
    # An associative array (`declare -A`) would read better, but this file's shebang is
    # `#!/bin/bash` — which on macOS is bash 3.2, where `declare -A` is rejected outright. bash then
    # treats the name as an INDEXED array and evaluates each subscript arithmetically, so
    # `MAP["google-services.json"]=X` fails with "invalid arithmetic operator" and the map stays
    # EMPTY. Nothing aborts (the errors do not trip `set -e`), so the scan below simply found no
    # secrets and the script reported success having encoded nothing. A "|" separator is safe here:
    # neither a secret file name nor a secret env-var name contains one.
    local FILE_TO_SECRET_PAIRS=(
        "firebaseAppDistributionServiceCredentialsFile.json|FIREBASECREDS"
        "google-services.json|GOOGLESERVICES"
        "playStorePublishServiceCredentialsFile.json|PLAYSTORECREDS"
        "AuthKey.p8|APPSTORE_AUTH_KEY"
        "match_ci_key|MATCH_GIT_PRIVATE_KEY"
        # macOS App Store certificates and provisioning profiles
        "mac_app_distribution.p12|MAC_APP_DISTRIBUTION_CERTIFICATE_B64"
        "mac_installer_distribution.p12|MAC_INSTALLER_DISTRIBUTION_CERTIFICATE_B64"
        "mac_embedded.provisionprofile|MAC_EMBEDDED_PROVISION_B64"
        "mac_runtime.provisionprofile|MAC_RUNTIME_PROVISION_B64"
    )

    local secrets_found=0
    local secrets_encoded=0
    # Index-matched parallel arrays stand in for the associative map (see the bash 3.2 note above).
    # `local` keeps the dynamic scope the child function relies on, exactly as `declare -A` did.
    local ENCODED_NAMES=()
    local ENCODED_VALUES=()

    # Check if secrets directory exists
    if [ ! -d "secrets" ]; then
        echo -e "${YELLOW}No 'secrets' directory found. Skipping secrets encoding.${NC}"
        return 0
    fi

    # Scan secrets directory for known files
    for pair in ${FILE_TO_SECRET_PAIRS[@]+"${FILE_TO_SECRET_PAIRS[@]}"}; do
        local file_name="${pair%%|*}"
        local secret_name="${pair#*|}"
        local file_path="secrets/$file_name"

        if [ -f "$file_path" ]; then
            secrets_found=$((secrets_found + 1))
            echo -e "${BLUE}Found: $file_name${NC}"
            echo -e "${BLUE}Encoding as: $secret_name${NC}"

            local encoded=$(encode_base64 "$file_path")
            if [ $? -eq 0 ]; then
                ENCODED_NAMES+=("$secret_name")
                ENCODED_VALUES+=("$encoded")
                secrets_encoded=$((secrets_encoded + 1))
                echo -e "${GREEN}✓ Successfully encoded $file_name${NC}"
            else
                echo -e "${RED}✗ Failed to encode $file_name${NC}"
            fi
        fi
    done

    if [ $secrets_found -eq 0 ]; then
        echo -e "${YELLOW}No known secret files found in secrets/ directory${NC}"
        echo -e "${YELLOW}Looking for: firebaseAppDistributionServiceCredentialsFile.json, google-services.json, playStorePublishServiceCredentialsFile.json, AuthKey.p8, match_ci_key${NC}"
        return 0
    fi

    if [ $secrets_encoded -eq 0 ]; then
        echo -e "${RED}Failed to encode any secret files${NC}"
        return 1
    fi

    # Update secrets.env file
    echo -e "${BLUE}Updating $ENV_FILE with encoded files...${NC}"
    update_secrets_env_with_files

    echo -e "${GREEN}Encoded $secrets_encoded out of $secrets_found secret files${NC}"
    return 0
}

# Function to update secrets.env with encoded secret files
update_secrets_env_with_files() {
    if [ ! -f "$ENV_FILE" ]; then
        echo -e "${YELLOW}$ENV_FILE not found. Secret files will not be added.${NC}"
        return 0
    fi

    # Access the ENCODED_SECRETS array from parent scope

    local temp_file="${ENV_FILE}.tmp"
    local in_multiline=false
    local multiline_end=""
    local current_key=""

    # Read existing secrets.env and track which sections exist. A "|"-delimited string stands in for
    # the set (bash 3.2 has no associative arrays — see the note in the encode function). Both ends
    # carry a sentinel "|" so a `*"|key|"*` match can never hit a partial key.
    local existing_sections="|"

    while IFS= read -r line || [ -n "$line" ]; do
        if [ "$in_multiline" = false ] && [[ "$line" == *"<<EOF" ]]; then
            current_key=$(echo "$line" | cut -d '<' -f1 | xargs)
            existing_sections="${existing_sections}${current_key}|"
            multiline_end="EOF"
            in_multiline=true
        elif [ "$in_multiline" = true ] && [[ "$line" == "$multiline_end" ]]; then
            in_multiline=false
        fi
    done < "$ENV_FILE"

    # Copy existing file and update/append sections
    cp "$ENV_FILE" "$temp_file"
    in_multiline=false

    # For each encoded secret, update or append
    local _i=0
    while [ "$_i" -lt "${#ENCODED_NAMES[@]}" ]; do
        local secret_name="${ENCODED_NAMES[$_i]}"
        local encoded_value="${ENCODED_VALUES[$_i]}"
        _i=$((_i + 1))

        if case "$existing_sections" in *"|${secret_name}|"*) true ;; *) false ;; esac; then
            # Update existing section
            echo -e "${BLUE}Updating existing section: $secret_name${NC}"
            local temp_file2="${temp_file}.2"
            local in_target_section=false

            while IFS= read -r line || [ -n "$line" ]; do
                if [[ "$line" == "${secret_name}<<EOF" ]]; then
                    in_target_section=true
                    echo "$line" >> "$temp_file2"
                    echo "$encoded_value" >> "$temp_file2"
                    continue
                fi

                if [ "$in_target_section" = true ] && [[ "$line" == "EOF" ]]; then
                    in_target_section=false
                    echo "$line" >> "$temp_file2"
                    continue
                fi

                if [ "$in_target_section" = false ]; then
                    echo "$line" >> "$temp_file2"
                fi
            done < "$temp_file"

            mv "$temp_file2" "$temp_file"
        else
            # Append new section
            echo -e "${BLUE}Adding new section: $secret_name${NC}"
            echo "" >> "$temp_file"
            echo "${secret_name}<<EOF" >> "$temp_file"
            echo "$encoded_value" >> "$temp_file"
            echo "EOF" >> "$temp_file"
        fi
    done

    # Replace original file
    mv "$temp_file" "$ENV_FILE"
    echo -e "${GREEN}$ENV_FILE updated successfully${NC}"
}

# Update iOS string secrets in secrets.env
update_ios_string_secrets() {
    local SECRETS_FILE="$ENV_FILE"

    # Check if secrets.env exists
    if [[ ! -f "$SECRETS_FILE" ]]; then
        print_warning "$SECRETS_FILE not found. Creating new file..."
        mkdir -p "$(dirname "$SECRETS_FILE")"
        touch "$SECRETS_FILE"
    fi

    # Skip if no iOS secrets extracted
    if [[ ${#IOS_STRING_SECRETS[@]} -eq 0 ]]; then
        print_info "No iOS secrets to update"
        return 0
    fi

    print_info "Updating iOS string secrets in $SECRETS_FILE..."

    # Check if iOS Configuration section exists
    if grep -q "^# iOS Configuration" "$SECRETS_FILE" 2>/dev/null; then
        print_info "iOS section exists - updating individual keys..."
        update_ios_section
    else
        print_info "iOS section doesn't exist - appending new section..."
        append_ios_section
    fi
}

# Helper function to update existing iOS section
update_ios_section() {
    local SECRETS_FILE="$ENV_FILE"

    for key in "${!IOS_STRING_SECRETS[@]}"; do
        local value="${IOS_STRING_SECRETS[$key]}"

        # Check if key exists in file
        if grep -q "^${key}=" "$SECRETS_FILE"; then
            # Update existing key
            if [[ -n "$value" ]]; then
                # Replace with new value (escape special characters)
                local escaped_value=$(printf '%s\n' "$value" | sed 's/[&/\]/\\&/g')
                sed -i.bak "s|^${key}=.*|${key}=\"${escaped_value}\"|" "$SECRETS_FILE"
                print_success "Updated $key"
            else
                # Keep existing value if new value is empty
                print_info "Preserving existing $key (new value empty)"
            fi
        else
            # Key doesn't exist - add it after iOS section header
            local section_line=$(grep -n "^# iOS Configuration" "$SECRETS_FILE" | cut -d: -f1)
            if [[ -n "$section_line" ]]; then
                # Insert after the separator line following the header (portable approach)
                local insert_line=$((section_line + 2))
                local escaped_value=$(printf '%s\n' "$value" | sed 's/[&/\]/\\&/g')
                {
                    head -n $((insert_line - 1)) "$SECRETS_FILE"
                    echo "${key}=\"${escaped_value}\""
                    tail -n +${insert_line} "$SECRETS_FILE"
                } > "${SECRETS_FILE}.tmp" && mv "${SECRETS_FILE}.tmp" "$SECRETS_FILE"
                print_success "Added $key to iOS section"
            fi
        fi
    done

    # Remove backup file
    rm -f "${SECRETS_FILE}.bak"
}

# Helper function to append new iOS section
append_ios_section() {
    local SECRETS_FILE="$ENV_FILE"

    # Append new iOS section
    cat >> "$SECRETS_FILE" << EOF

# ==============================================================================
# iOS Configuration
# ==============================================================================

# App Store Connect API Keys
APPSTORE_KEY_ID="${IOS_STRING_SECRETS[APPSTORE_KEY_ID]}"
APPSTORE_ISSUER_ID="${IOS_STRING_SECRETS[APPSTORE_ISSUER_ID]}"

# Fastlane Match
MATCH_PASSWORD="${IOS_STRING_SECRETS[MATCH_PASSWORD]}"

# macOS Notarization (for Desktop app distribution)
NOTARIZATION_APPLE_ID="${IOS_STRING_SECRETS[NOTARIZATION_APPLE_ID]}"
NOTARIZATION_PASSWORD="${IOS_STRING_SECRETS[NOTARIZATION_PASSWORD]}"
NOTARIZATION_TEAM_ID="${IOS_STRING_SECRETS[NOTARIZATION_TEAM_ID]}"
EOF

    print_success "Appended iOS Configuration section"
}

# Add Desktop signing placeholders to secrets.env
add_desktop_placeholders() {
    local SECRETS_FILE="$ENV_FILE"

    # Check if file exists
    if [[ ! -f "$SECRETS_FILE" ]]; then
        print_error "File $SECRETS_FILE does not exist"
        return 1
    fi

    # Check if Desktop Signing section exists
    if grep -q "^# Desktop Signing" "$SECRETS_FILE" 2>/dev/null; then
        print_info "Desktop Signing section already exists - skipping"
    else
        print_info "Adding Desktop Signing placeholder section..."

        # Append Desktop section
        if ! cat >> "$SECRETS_FILE" << 'EOF'

# ==============================================================================
# Desktop Signing (Optional)
# ==============================================================================
# These are optional for Desktop app distribution outside app stores.
# Populate when setting up code signing for Windows/macOS/Linux desktop apps.

# Windows Signing
WINDOWS_SIGNING_KEY=""
WINDOWS_SIGNING_PASSWORD=""
WINDOWS_SIGNING_CERTIFICATE=""

# macOS Signing (Desktop app, not iOS)
MACOS_SIGNING_KEY=""
MACOS_SIGNING_PASSWORD=""
MACOS_SIGNING_CERTIFICATE=""

# Linux Signing
LINUX_SIGNING_KEY=""
LINUX_SIGNING_PASSWORD=""
LINUX_SIGNING_CERTIFICATE=""
EOF
        then
            print_error "Failed to append Desktop Signing section"
            return 1
        fi

        print_success "Added Desktop Signing placeholder section"
    fi

    # Add macOS App Store section if not present
    if grep -q "^# macOS App Store" "$SECRETS_FILE" 2>/dev/null; then
        print_info "macOS App Store section already exists - skipping"
    else
        print_info "Adding macOS App Store placeholder section..."

        if ! cat >> "$SECRETS_FILE" << 'EOF'

# ==============================================================================
# macOS App Store (Required for macOS TestFlight & App Store deployment)
# ==============================================================================
# Keychain and certificate passwords for CI code signing.
# Place .p12 and .provisionprofile files in secrets/ directory, then run sync.
#
# Password files (read automatically by sync):
#   secrets/live/apple/match/.keychain_password              → KEYCHAIN_PASSWORD
#   secrets/live/apple/match/.certificates_password          → CERTIFICATES_PASSWORD
#
# Certificate/profile files (base64 encoded by sync):
#   secrets/live/desktop/macos/app_distribution.p12        → MAC_APP_DISTRIBUTION_CERTIFICATE_B64
#   secrets/live/desktop/macos/installer_distribution.p12  → MAC_INSTALLER_DISTRIBUTION_CERTIFICATE_B64
#   secrets/live/desktop/macos/embedded.provisionprofile   → MAC_EMBEDDED_PROVISION_B64
#   secrets/live/desktop/macos/runtime.provisionprofile    → MAC_RUNTIME_PROVISION_B64
EOF
        then
            print_error "Failed to append macOS App Store section"
            return 1
        fi

        print_success "Added macOS App Store placeholder section"
    fi
}

# Validate secrets.env format and completeness
validate_sync_result() {
    local SECRETS_FILE="$ENV_FILE"
    local exit_code=0

    print_info "Validating $SECRETS_FILE..."

    # Check if file exists
    if [[ ! -f "$SECRETS_FILE" ]]; then
        print_error "File $SECRETS_FILE does not exist"
        return 1
    fi

    # Track validation issues
    local format_errors=()
    local missing_secrets=()
    local invalid_base64=()

    # ============================================================================
    # 1. Check file format
    # ============================================================================

    print_info "Checking file format..."

    # Check for GitHub Secrets Environment File header (generated by update_secrets_env)
    if ! grep -q "^# GitHub Secrets Environment File" "$SECRETS_FILE"; then
        format_errors+=("Missing GitHub Secrets Environment File header")
    fi

    # Check for iOS Configuration section header (if iOS project)
    if [[ -f "secrets/live/apple/appstore/key_id" ]] || grep -qE "^apple\.team\.id=" gradle/fork.properties 2>/dev/null; then
        if ! grep -q "^# iOS Configuration" "$SECRETS_FILE"; then
            format_errors+=("Missing iOS configuration section header (iOS project detected)")
        fi
    fi

    # Validate heredoc blocks are properly formatted
    local in_heredoc=false
    local heredoc_key=""
    local heredoc_delimiter=""
    local line_num=0

    while IFS= read -r line; do
        line_num=$((line_num + 1))

        # Check for heredoc start
        if [[ "$line" =~ ^([A-Z_]+)\<\<([A-Z]+)$ ]]; then
            if [[ "$in_heredoc" = true ]]; then
                format_errors+=("Line $line_num: Nested heredoc detected (unclosed $heredoc_key)")
            fi
            heredoc_key="${BASH_REMATCH[1]}"
            heredoc_delimiter="${BASH_REMATCH[2]}"
            in_heredoc=true
        # Check for heredoc end
        elif [[ "$in_heredoc" = true ]] && [[ "$line" == "$heredoc_delimiter" ]]; then
            in_heredoc=false
            heredoc_key=""
            heredoc_delimiter=""
        fi
    done < "$SECRETS_FILE"

    # Check if any heredoc was left unclosed
    if [[ "$in_heredoc" = true ]]; then
        format_errors+=("Unclosed heredoc block: $heredoc_key (missing $heredoc_delimiter)")
    fi

    # Check for duplicate keys using process substitution
    local duplicates
    duplicates=$(while IFS= read -r line; do
        # Extract keys from both regular and heredoc formats
        if [[ "$line" =~ ^([A-Z_]+)= ]] || [[ "$line" =~ ^([A-Z_]+)\<\< ]]; then
            echo "${BASH_REMATCH[1]}"
        fi
    done < "$SECRETS_FILE" | sort | uniq -d)

    if [[ -n "$duplicates" ]]; then
        while IFS= read -r dup_key; do
            if [[ -n "$dup_key" ]]; then
                format_errors+=("Duplicate key found: $dup_key")
            fi
        done <<< "$duplicates"
    fi

    # Report format errors
    if [[ ${#format_errors[@]} -gt 0 ]]; then
        print_error "Format validation failed:"
        for error in "${format_errors[@]}"; do
            echo -e "  ${RED}- $error${NC}"
        done
        if [[ $exit_code -eq 0 ]]; then exit_code=1; fi
    else
        print_success "File format is valid"
    fi

    # ============================================================================
    # 2. Check required secrets
    # ============================================================================

    print_info "Checking required secrets..."

    # Define required Android secrets
    local required_android=(
        "KEYSTORE_PASSWORD"
        "KEYALIAS"
        "KEY_PASSWORD"
        "GOOGLESERVICES"
        "PLAYSTORECREDS"
        "FIREBASECREDS"
    )

    # Map alternative key names used in this project (Play App Signing single-keystore)
    # Alias lookup as a `case` (bash 3.2 has no associative arrays — see the encode function).
    # Returns the empty string when a secret has no alternative name.
    _key_alias_for() {
        case "$1" in
            KEYSTORE_PASSWORD) echo "UPLOAD_KEYSTORE_FILE_PASSWORD" ;;
            KEYALIAS)          echo "UPLOAD_KEYSTORE_ALIAS" ;;
            KEY_PASSWORD)      echo "UPLOAD_KEYSTORE_ALIAS_PASSWORD" ;;
            *)                 echo "" ;;
        esac
    }

    # Check Android secrets
    for secret in "${required_android[@]}"; do
        local found=false

        # Check direct key name
        if grep -q "^${secret}=" "$SECRETS_FILE" || grep -q "^${secret}<<" "$SECRETS_FILE"; then
            found=true
        # Check alternative names
        elif [ -n "$(_key_alias_for "$secret")" ]; then
            IFS='|' read -ra alternatives <<< "$(_key_alias_for "$secret")"
            for alt in "${alternatives[@]}"; do
                if grep -q "^${alt}=" "$SECRETS_FILE" || grep -q "^${alt}<<" "$SECRETS_FILE"; then
                    found=true
                    break
                fi
            done
        fi

        if [[ "$found" = false ]]; then
            missing_secrets+=("Android: $secret")
        fi
    done

    # Check iOS secrets if iOS project detected
    if [[ -f "secrets/live/apple/appstore/key_id" ]] || grep -qE "^apple\.team\.id=" gradle/fork.properties 2>/dev/null; then
        local required_ios=(
            "APPSTORE_KEY_ID"
            "APPSTORE_ISSUER_ID"
            "APPSTORE_AUTH_KEY"
            "MATCH_PASSWORD"
            "MATCH_GIT_PRIVATE_KEY"
        )

        for secret in "${required_ios[@]}"; do
            if ! grep -q "^${secret}=" "$SECRETS_FILE" && ! grep -q "^${secret}<<" "$SECRETS_FILE"; then
                missing_secrets+=("iOS: $secret")
            fi
        done
    fi

    # Report missing secrets
    if [[ ${#missing_secrets[@]} -gt 0 ]]; then
        print_error "Missing required secrets:"
        for secret in "${missing_secrets[@]}"; do
            echo -e "  ${RED}- $secret${NC}"
        done
        if [[ $exit_code -eq 0 ]]; then exit_code=2; fi
    else
        print_success "All required secrets are present"
    fi

    # ============================================================================
    # 3. Validate base64 encoding
    # ============================================================================

    print_info "Validating base64 encoding for file secrets..."

    # Define file secrets that should be base64 encoded
    local file_secrets=(
        "GOOGLESERVICES"
        "PLAYSTORECREDS"
        "FIREBASECREDS"
        "APPSTORE_AUTH_KEY"
        "MATCH_GIT_PRIVATE_KEY"
        "UPLOAD_KEYSTORE_FILE"
        "MAC_APP_DISTRIBUTION_CERTIFICATE_B64"
        "MAC_INSTALLER_DISTRIBUTION_CERTIFICATE_B64"
        "MAC_EMBEDDED_PROVISION_B64"
        "MAC_RUNTIME_PROVISION_B64"
    )

    # Extract and validate base64 values
    for secret in "${file_secrets[@]}"; do
        # Check if secret exists in file
        if ! grep -q "^${secret}<<" "$SECRETS_FILE"; then
            # Skip validation if secret doesn't exist (will be caught by required secrets check)
            continue
        fi

        # Extract the base64 value between heredoc markers
        local value=""
        local in_block=false
        local block_delimiter=""

        while IFS= read -r line; do
            if [[ "$line" =~ ^${secret}\<\<([A-Z]+)$ ]]; then
                in_block=true
                block_delimiter="${BASH_REMATCH[1]}"
                value=""
            elif [[ "$in_block" = true ]] && [[ "$line" == "$block_delimiter" ]]; then
                break
            elif [[ "$in_block" = true ]]; then
                value+="$line"
            fi
        done < "$SECRETS_FILE"

        # Validate base64 encoding
        if [[ -n "$value" ]]; then
            # Try to decode the base64 value (handle macOS vs Linux)
            if [[ "$OSTYPE" == "darwin"* ]]; then
                if ! printf '%s' "$value" | base64 -D > /dev/null 2>&1; then
                    invalid_base64+=("$secret")
                fi
            else
                if ! printf '%s' "$value" | base64 -d > /dev/null 2>&1; then
                    invalid_base64+=("$secret")
                fi
            fi
        fi
    done

    # Report invalid base64
    if [[ ${#invalid_base64[@]} -gt 0 ]]; then
        print_error "Invalid base64 encoding detected:"
        for secret in "${invalid_base64[@]}"; do
            echo -e "  ${RED}- $secret${NC}"
        done
        if [[ $exit_code -eq 0 ]]; then exit_code=3; fi
    else
        print_success "All file secrets have valid base64 encoding"
    fi

    # ============================================================================
    # Final summary
    # ============================================================================

    echo ""
    if [[ $exit_code -eq 0 ]]; then
        print_success "All validations passed"
    else
        local error_summary=""
        if [[ ${#format_errors[@]} -gt 0 ]]; then
            error_summary+="Format errors"
        fi
        if [[ ${#missing_secrets[@]} -gt 0 ]]; then
            [[ -n "$error_summary" ]] && error_summary+=", "
            error_summary+="Missing required secrets"
        fi
        if [[ ${#invalid_base64[@]} -gt 0 ]]; then
            [[ -n "$error_summary" ]] && error_summary+=", "
            error_summary+="Invalid base64 encoding"
        fi
        print_error "Validation failed: $error_summary"
    fi

    return $exit_code
}

# Function to create/update secrets.env file
update_secrets_env() {
    # Play App Signing model — single UPLOAD keystore. Google holds the app signing key.
    local upload_keystore=$1
    local upload_b64=$(encode_base64 "keystores/$upload_keystore")

    if [ -f "$ENV_FILE" ]; then
        echo -e "${BLUE}Updating existing $ENV_FILE${NC}"
        local temp_file="${ENV_FILE}.tmp"

        local in_upload_block=false
        local in_legacy_original_block=false
        local upload_found=false

        while IFS= read -r line || [ -n "$line" ]; do
            # Replace UPLOAD_KEYSTORE_FILE block with current content
            if [[ "$line" == "UPLOAD_KEYSTORE_FILE<<EOF" ]]; then
                in_upload_block=true
                upload_found=true
                echo "$line" >> "$temp_file"
                echo "$upload_b64" >> "$temp_file"
                continue
            fi

            # Drop legacy ORIGINAL_KEYSTORE_FILE block + its scalar siblings (silent purge)
            if [[ "$line" == "ORIGINAL_KEYSTORE_FILE<<EOF" ]]; then
                in_legacy_original_block=true
                continue
            fi
            if [[ "$line" == ORIGINAL_KEYSTORE_* ]]; then
                continue   # drops ORIGINAL_KEYSTORE_FILE_PASSWORD / _ALIAS / _ALIAS_PASSWORD scalars
            fi

            # EOF marker — exit the matching block
            if [ "$in_upload_block" = true ] && [[ "$line" == "EOF" ]]; then
                in_upload_block=false
                echo "$line" >> "$temp_file"
                continue
            fi
            if [ "$in_legacy_original_block" = true ] && [[ "$line" == "EOF" ]]; then
                in_legacy_original_block=false
                continue
            fi

            # Skip content inside blocks
            if [ "$in_upload_block" = true ] || [ "$in_legacy_original_block" = true ]; then
                continue
            fi

            echo "$line" >> "$temp_file"
        done < "$ENV_FILE"

        if [ "$upload_found" = false ]; then
            echo "" >> "$temp_file"
            echo "UPLOAD_KEYSTORE_FILE<<EOF" >> "$temp_file"
            echo "$upload_b64" >> "$temp_file"
            echo "EOF" >> "$temp_file"
        fi

        mv "$temp_file" "$ENV_FILE"
    else
        echo -e "${BLUE}Creating new $ENV_FILE${NC}"
        mkdir -p "$(dirname "$ENV_FILE")"

        cat > "$ENV_FILE" <<EOL
# GitHub Secrets Environment File (Play App Signing single-keystore model)
# Format: KEY=VALUE
# Use <<EOF and EOF to denote multiline values
# Run this command to format these secrets: dos2unix $ENV_FILE

UPLOAD_KEYSTORE_FILE_PASSWORD=${UPLOAD_KEYSTORE_FILE_PASSWORD}
UPLOAD_KEYSTORE_ALIAS=${UPLOAD_KEYSTORE_ALIAS}
UPLOAD_KEYSTORE_ALIAS_PASSWORD=${UPLOAD_KEYSTORE_ALIAS_PASSWORD}
UPLOAD_KEYSTORE_FILE<<EOF
$upload_b64
EOF
EOL
    fi

    echo -e "${GREEN}$ENV_FILE has been updated with base64 encoded upload keystore${NC}"
}

# Function to update fastlane-config/project_config.rb with keystore information
update_fastlane_config() {
    local keystore_name=$1
    local keystore_password=$2
    local key_alias=$3
    local key_password=$4

    # Path to the fastlane config file
    local config_dir="fastlane-config"
    local config_file="$config_dir/project_config.rb"

    echo -e "${BLUE}Updating fastlane configuration with keystore information...${NC}"

    # Create the fastlane-config directory if it doesn't exist
    if [ ! -d "$config_dir" ]; then
        echo -e "${BLUE}Creating '$config_dir' directory...${NC}"
        mkdir -p "$config_dir"
    fi

    # Check if the config file exists
    if [ -f "$config_file" ]; then
        echo -e "${BLUE}Updating existing $config_file${NC}"

        # Create a temporary file for the updated content
        local temp_file=$(mktemp)

        # Use awk for cross-platform compatibility (works on both macOS and Linux)
        # This handles the nested keystore config structure in project_config.rb
        awk -v ks_file="$keystore_name" -v ks_pass="$keystore_password" -v k_alias="$key_alias" -v k_pass="$key_password" '
        /keystore:.*\{/,/\}/ {
            if (/file:/) {
                gsub(/file: "[^"]*"/, "file: \"" ks_file "\"")
            }
            if (/password:/ && !/key_password/) {
                gsub(/password: "[^"]*"/, "password: \"" ks_pass "\"")
            }
            if (/key_alias:/) {
                gsub(/key_alias: "[^"]*"/, "key_alias: \"" k_alias "\"")
            }
            if (/key_password:/) {
                gsub(/key_password: "[^"]*"/, "key_password: \"" k_pass "\"")
            }
        }
        { print }
        ' "$config_file" > "$temp_file"

        # Replace the original file with the updated one
        mv "$temp_file" "$config_file"
    else
        # File doesn't exist, create it with a complete structure matching project_config.rb format
        echo -e "${BLUE}Creating new $config_file${NC}"

        mkdir -p "$config_dir"

        # Create the file with the complete structure
        cat > "$config_file" << EOL
# ==============================================================================
# Project Configuration - Update these values when setting up a new project
# ==============================================================================

module FastlaneConfig
  module ProjectConfig
    PROJECT_NAME = "kmp-project-template"
    ORGANIZATION_NAME = "Devikon Inc."

    ANDROID = {
      package_name: "cmp.android.app",
      play_store_json_key: "secrets/live/android/playStorePublishServiceCredentialsFile.json",
      apk_paths: {
        prod: "cmp-android/build/outputs/apk/prod/release/cmp-android-prod-release.apk",
        demo: "cmp-android/build/outputs/apk/demo/release/cmp-android-demo-release.apk"
      },
      aab_path: "cmp-android/build/outputs/bundle/prodRelease/cmp-android-prod-release.aab",
      keystore: {
        file: "$keystore_name",
        password: "$keystore_password",
        key_alias: "$key_alias",
        key_password: "$key_password"
      },
      firebase: {
        prod_app_id: "1:728434912738:android:REPLACE_ME",
        demo_app_id: "1:728434912738:android:REPLACE_ME",
        groups: "cmp-app-testers"
      }
    }

    SHARED = {
      firebase_service_credentials: "secrets/live/android/firebaseAppDistributionServiceCredentialsFile.json"
    }
  end
end
EOL
    fi

    echo -e "${GREEN}Fastlane configuration updated successfully${NC}"
}

# Function to update cmp-android/build.gradle.kts with keystore information
#
# DISABLED — this wrote PLAINTEXT PASSWORDS into a TRACKED SOURCE FILE.
#
# It rewrote the `?:` fallbacks in cmp-android/build.gradle.kts:
#
#     storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "<real password inlined here>"
#
# so every generated password landed in a git-tracked build script — one `git add` away from
# being committed and pushed, and printed by any routine `git diff` or code review.
#
# It also produced malformed Kotlin: the awk replacement does not escape the value, so a
# password containing `"`, `$` or `\` breaks the string literal and the module stops compiling.
# Since strong generated passwords routinely contain those characters, this failed exactly when
# the password was good.
#
# The inlining was never necessary. cmp-android/build.gradle.kts already resolves all four
# values from the environment via System.getenv(); CI and local release builds export them
# before invoking Gradle, and the checked-in `?:` fallbacks are deliberately non-secret
# template defaults. Credentials belong in the environment and in 0600 files under
# secrets/live/ — never in tracked source.
#
# Kept as a no-op stub (rather than deleted) so existing callers keep working.
update_gradle_config() {
    local keystore_name=$1
    local key_alias=$3   # $2/$4 are the passwords — deliberately unused
    echo -e "${BLUE}Skipping Gradle build-file mutation (credentials stay out of tracked source).${NC}"
    echo -e "${BLUE}  keystore: ${keystore_name}   alias: ${key_alias}${NC}"
    echo -e "${BLUE}  Passwords resolve at build time from KEYSTORE_PASSWORD /${NC}"
    echo -e "${BLUE}  KEYSTORE_ALIAS_PASSWORD in the environment.${NC}"
    return 0
}


# Read a single key from a Java-style properties file (key=value format).
# Usage: _read_fork_prop "gradle/fork.properties" "keystore.dn.city"
_read_fork_prop() {
    local props_file="$1"
    local key="$2"
    # Delegates to the ONE bash reader (scripts/_shared/fork-props.sh). The previous inline
    # `grep | cut -d= | tr` had neither head -1 nor inline-`#` stripping, so a duplicated key or an
    # inline comment would have fed a malformed value into a keystore DN. Latent rather than live —
    # today's bridge has neither. Signature is unchanged, so callers are untouched.
    FORK_PROPERTIES="$props_file" fp_get "$key"
}

# Read a secret from a per-value file under secrets/live/android/keystores/.
# Returns empty string (not an error) when the file is absent so the caller
# can fall back to a default.
_read_keystore_secret() {
    local secret_name="$1"   # keystore_password | keystore_alias | keystore_alias_password

    # ONE layout: the KEY=VALUE properties file. This is the only keystore-credential layout the
    # project declares (secrets/LAYOUT.yaml), the one Gradle can load directly, and the one the
    # signing config reads via System.getenv() after it is exported.
    #
    # An earlier revision of this reader looked for one file per value
    # (secrets/live/android/keystores/keystore_password, .../keystore_alias, ...). That layout was
    # never declared anywhere, so on a real project every lookup returned "" and generation
    # silently fell back to the placeholder defaults below ("Keystore_password" / "Keystore_Alias")
    # — producing a keystore whose passwords match nothing the project holds, a failure invisible
    # until an upload is rejected at signing time. Supporting both shapes only moved the confusion
    # around, so the undeclared one is gone: one layout, one place to look.
    # Path from secrets/LAYOUT.yaml via build_secrets — the SoT every consumer reads.
    local props
    props="$(ruby deployment/_shared/lib/build_secrets.rb path upload_keystore_properties 2>/dev/null || true)"
    # NO FALLBACK — same reason as the keystore path above. A hardcoded default here would silently
    # diverge from LAYOUT the moment LAYOUT changed.
    [[ -n "$props" ]] || { echo "" ; return; }
    [[ -f "$props" ]] || { echo ""; return; }

    # LAYOUT-declared property NAMES. secrets/LAYOUT.yaml#upload_keystore_properties.keys[].name is
    # the key the file is written under (storePassword / keyAlias / keyPassword); `source_env:` is
    # only where the VALUE is sourced from. Reading KEYSTORE_* here returns empty once the LAYOUT
    # pass owns the file, and generation then silently falls back to the placeholder defaults —
    # producing a keystore whose passwords match nothing in the vault.
    local key=""
    case "$secret_name" in
        keystore_password)       key="storePassword" ;;
        keystore_alias)          key="keyAlias" ;;
        keystore_alias_password) key="keyPassword" ;;
        *)                       echo ""; return ;;
    esac

    # First match only; strip the KEY= prefix and any trailing CR/LF. Never echoed by callers.
    grep -m1 "^${key}=" "$props" 2>/dev/null | sed "s/^${key}=//" | tr -d '\n\r'
}


# Function to generate keystore
# Reads:
#   - DN components from gradle/fork.properties (keystore.dn.* + legal.company.name)
#   - Passwords    from secrets/live/android/keystores/{keystore_password,keystore_alias,
#                  keystore_alias_password}
# Generation constants (VALIDITY=25, KEYALG=RSA, KEYSIZE=2048, OVERWRITE=false)
# are hard-coded defaults here — they are not per-fork configuration.
generate_keystore() {
    local env=$1
    local keystore_name=$2
    local key_alias=$3
    local keystore_password=$4
    local key_password=$5

    # Hard-coded generation constants (not per-fork config)
    # PKCS12 (keytool's default store type) has ONE password: the store password also protects the
    # key. `-keypass` is accepted and silently ignored when it differs, producing a keystore whose
    # key no consumer can open ("Get Key failed: Given final block not properly padded") — a failure
    # that reads like corruption and hides three layers from its cause. secrets/LAYOUT.yaml resolves
    # BOTH storePassword and keyPassword from the SAME vault alias, so they cannot diverge upstream;
    # this keeps the invariant local too.
    key_password="$keystore_password"

    local validity=1000  # ~1000 years — upload key must outlive the app
    local keyalg="RSA"
    local keysize=4096
    # Honour an explicit request. Hardcoded `false` with no flag meant the ONLY way to regenerate was
    # to delete the file first — and the skip path returned 0, so `generate` printed SUCCESS while
    # leaving the OLD keystore in place, byte-identical (caught only by hashing).
    local overwrite="${KEYSTORE_OVERWRITE:-false}"

    # Path to save the keystore
    # Output path from secrets/LAYOUT.yaml via build_secrets, NOT a hardcoded dir. Hardcoding
    # `keystores/` wrote where no consumer looks — cmp-android/build.gradle.kts and the fastlane
    # lanes both resolve secrets/live/android/keystores/ — so a freshly generated keystore appeared
    # not to exist and the old one kept being used.
    local keystore_path
    keystore_path="$(ruby deployment/_shared/lib/build_secrets.rb path upload_keystore 2>/dev/null || true)"
    if [ -z "$keystore_path" ]; then
        # NO FALLBACK. The old hardcoded `keystores/` is precisely the defect this replaced: it wrote
        # where no consumer looks, so a freshly generated keystore appeared not to exist and the old
        # one kept being used. Falling back to it would recreate that silently. If LAYOUT cannot
        # resolve the path there is no correct place to write, so stop and say why.
        echo -e "${RED}✖ secrets/LAYOUT.yaml could not resolve 'upload_keystore'.${NC}" >&2
        echo -e "${RED}  LAYOUT is the single source of truth for where the keystore lives; without it${NC}" >&2
        echo -e "${RED}  there is no path every consumer agrees on. Fix the LAYOUT entry, then re-run.${NC}" >&2
        return 1
    fi
    KEYSTORE_OUT_DIR="$(dirname "$keystore_path")"
    mkdir -p "$KEYSTORE_OUT_DIR"

    echo -e "${BLUE}==================================================================${NC}"
    echo -e "${BLUE}Generating $env keystore${NC}"
    echo -e "${BLUE}==================================================================${NC}"

    echo -e "Generating keystore with the following parameters:"
    echo -e "- Environment: $env"
    echo -e "- Keystore Name: $keystore_path"
    echo -e "- Key Alias: $key_alias"
    echo -e "- Validity: $validity years"
    echo -e "- Key Algorithm: $keyalg"
    echo -e "- Key Size: $keysize"

    # Check if the keystore file already exists
    if [ -f "$keystore_path" ]; then
        if [ "$overwrite" = "true" ]; then
            # ACTUALLY remove it. Printing "Overwriting" without deleting made keytool fail with
            # `Key pair not generated, alias <...> already exists` (keytool ADDS to an existing
            # keystore, it never truncates), leaving the OLD keystore while the log claimed success.
            echo -e "${BLUE}Overwriting existing keystore file '$keystore_path' (removing it first).${NC}"
            rm -f "$keystore_path"
        else
            echo -e "${YELLOW}⏭  SKIPPED — '$keystore_path' already exists; NOTHING was generated.${NC}"
            echo -e "${YELLOW}    To replace it deliberately:  KEYSTORE_OVERWRITE=true ./keystore-manager.sh generate${NC}"
            echo -e "${YELLOW}    (Play permanently binds the signing key on FIRST upload — replacing a"
            echo -e "     keystore after publication makes every future update unpublishable.)${NC}"
            return 9   # distinct from 0: the caller must not report SUCCESS for a skip
        fi
    fi

    # ── Build Distinguished Name from gradle/fork.properties ──────────────────
    # Non-secret cert identity lives in fork.properties (new home); passwords come
    # from secrets/live/android/keystores/ per-value files (new home).
    # Legacy: previously these values came from secrets.env (retired).
    local FORK_PROPS="gradle/fork.properties"
    DN_PARTS=()

    # CN — legal.company.name (primary) or keystore.dn.cn (override if present)
    local cn_val=$(_read_fork_prop "$FORK_PROPS" "keystore.dn.cn")
    if [[ -z "$cn_val" ]]; then
        cn_val=$(_read_fork_prop "$FORK_PROPS" "legal.company.name")
    fi
    if [[ -n "$cn_val" ]]; then
        echo -e "- Company Name (CN): $cn_val"
        DN_PARTS+=("CN=$cn_val")
    fi

    # OU — keystore.dn.org_unit
    local ou_val=$(_read_fork_prop "$FORK_PROPS" "keystore.dn.org_unit")
    if [[ -n "$ou_val" ]]; then
        echo -e "- Org Unit (OU): $ou_val"
        DN_PARTS+=("OU=$ou_val")
    fi

    # O — legal.company.name (same as CN by convention)
    local o_val=$(_read_fork_prop "$FORK_PROPS" "legal.company.name")
    if [[ -n "$o_val" ]]; then
        echo -e "- Organization (O): $o_val"
        DN_PARTS+=("O=$o_val")
    fi

    # L — keystore.dn.city
    local l_val=$(_read_fork_prop "$FORK_PROPS" "keystore.dn.city")
    if [[ -n "$l_val" ]]; then
        echo -e "- City (L): $l_val"
        DN_PARTS+=("L=$l_val")
    fi

    # ST — keystore.dn.state
    local st_val=$(_read_fork_prop "$FORK_PROPS" "keystore.dn.state")
    if [[ -n "$st_val" ]]; then
        echo -e "- State (ST): $st_val"
        DN_PARTS+=("ST=$st_val")
    fi

    # C — keystore.dn.country (default US)
    local c_val=$(_read_fork_prop "$FORK_PROPS" "keystore.dn.country")
    c_val="${c_val:-US}"
    echo -e "- Country (C): $c_val"
    DN_PARTS+=("C=$c_val")

    # ── Invoke keytool ─────────────────────────────────────────────────────────
    if [ ${#DN_PARTS[@]} -gt 0 ]; then
        local DN
        DN=$(IFS=,; echo "${DN_PARTS[*]}")
        keytool -genkey -v \
            -keystore "$keystore_path" \
            -storetype PKCS12 \
              -alias "$key_alias" \
            -keyalg "$keyalg" \
            -keysize "$keysize" \
            -sigalg SHA256withRSA \
              -validity $((validity*365)) \
            -storepass "$keystore_password" \
            -keypass "$keystore_password" \
            -dname "$DN"
    else
        # gradle/fork.properties not present — fall back to interactive DN entry
        echo -e "${BLUE}gradle/fork.properties not found. Using interactive mode for certificate DN.${NC}"
        keytool -genkey -v \
            -keystore "$keystore_path" \
            -storetype PKCS12 \
              -alias "$key_alias" \
            -keyalg "$keyalg" \
            -keysize "$keysize" \
            -sigalg SHA256withRSA \
              -validity $((validity*365)) \
            -storepass "$keystore_password" \
            -keypass "$keystore_password"  # PKCS12: key pw == store pw
    fi

    # Check if keystore was successfully created
    if [ $? -eq 0 ] && [ -f "$keystore_path" ]; then
        echo ""
        echo -e "${GREEN}===== $env Keystore created successfully! =====${NC}"
        echo -e "Keystore location: $(realpath "$keystore_path")"
        echo -e "Keystore alias: $key_alias"
        echo ""
        return 0
    else
        echo ""
        echo -e "${RED}Error: Failed to create $env keystore. Please check the error messages above.${NC}"
        return 1
    fi
}

# Generate the single UPLOAD keystore. Play App Signing model — Google holds the app
# signing key in KMS, developer only holds the upload key.
# Per https://support.google.com/googleplay/android-developer/answer/9842756
#
# Password sources (new model — secrets.env is RETIRED):
#   secrets/live/android/keystores/keystore_password       → UPLOAD_KEYSTORE_FILE_PASSWORD
#   secrets/live/android/keystores/keystore_alias          → UPLOAD_KEYSTORE_ALIAS
#   secrets/live/android/keystores/keystore_alias_password → UPLOAD_KEYSTORE_ALIAS_PASSWORD
# If a file is absent the script falls back to a safe default so first-run works.
# Run `scripts/secrets/setup-secrets.sh android` to populate those files.
#
# DN source (new model):
#   gradle/fork.properties keys: legal.company.name, keystore.dn.{org_unit,city,state,country}
#
# DEPRECATED: update_secrets_env / encode-secrets / add-to-github gh-secret-set flow.
#   Modern replacement: scripts/secrets/sync-secrets-to-github.sh
generate_keystores() {
    check_keytool
    create_keystores_dir

    # Generation constants — hard-coded, not per-fork config
    local UPLOAD_KEYSTORE_NAME="upload_keystore.keystore"

    # Read passwords from per-value secret files (new model).
    # Falls back to safe placeholder defaults when files are absent so keytool
    # can complete; the caller is expected to populate the files before CI use.
    local UPLOAD_KEYSTORE_FILE_PASSWORD
    UPLOAD_KEYSTORE_FILE_PASSWORD=$(_read_keystore_secret "keystore_password")

    local UPLOAD_KEYSTORE_ALIAS
    UPLOAD_KEYSTORE_ALIAS=$(_read_keystore_secret "keystore_alias")

    # The key password is NOT an independent credential. A PKCS12 keystore has no separate
    # key password — keytool discards `-keypass` at creation ("Different store and key
    # passwords not supported for PKCS12 KeyStores") — so it always equals the store
    # password. Read it if present (older forks still carry the key), otherwise derive it.
    # Requiring it as its own value is what made this refuse to run against a correctly
    # provisioned single-password keystore.
    local UPLOAD_KEYSTORE_ALIAS_PASSWORD
    UPLOAD_KEYSTORE_ALIAS_PASSWORD=$(_read_keystore_secret "keystore_alias_password")
    [[ -z "$UPLOAD_KEYSTORE_ALIAS_PASSWORD" ]] && UPLOAD_KEYSTORE_ALIAS_PASSWORD="$UPLOAD_KEYSTORE_FILE_PASSWORD"

    # No placeholder fallback. These used to default to "Keystore_password" / "Keystore_Alias" /
    # "Alias_password" when the properties file was absent or unreadable — which generated a REAL
    # keystore protected by a well-known literal, recorded nowhere, matching nothing in the vault.
    # Nothing failed at generation time; the app signed locally and the mismatch only surfaced when
    # Play rejected the upload, by which point the keystore may already be the app's identity.
    # The credential source is the single source of truth: if it is not there, stop.
    # (The key password is excluded from this check — it is derived above, never independent.)
    if [[ -z "$UPLOAD_KEYSTORE_FILE_PASSWORD" || -z "$UPLOAD_KEYSTORE_ALIAS" ]]; then
        echo -e "${RED}Keystore credentials not available.${NC}" >&2
        echo -e "${RED}  expected: secrets/live/android/keystores/upload_keystore.properties${NC}" >&2
        echo -e "${RED}  with keys: KEYSTORE_PASSWORD, KEYSTORE_ALIAS${NC}" >&2
        echo -e "${RED}Materialize them from your secrets store first, then re-run.${NC}" >&2
        echo -e "${RED}Refusing to generate a keystore with placeholder credentials.${NC}" >&2
        return 1
    fi

    echo -e "${BLUE}🔑 Play App Signing mode: generating UPLOAD keystore${NC}"
    print_info "Password source: secrets/live/android/keystores/ (new model)"
    print_info "DN source: gradle/fork.properties (new model)"
    generate_keystore "UPLOAD" "$UPLOAD_KEYSTORE_NAME" "$UPLOAD_KEYSTORE_ALIAS" "$UPLOAD_KEYSTORE_FILE_PASSWORD" "$UPLOAD_KEYSTORE_ALIAS_PASSWORD"
    UPLOAD_RESULT=$?

    if [ $UPLOAD_RESULT -eq 0 ]; then
        # DEPRECATED: update_secrets_env wrote passwords + base64 keystore into
        # secrets/shared/secrets.env (legacy bundle). That file is retired.
        # Passwords now live permanently in secrets/live/android/keystores/ per-value files.
        # The keystore file itself (keystores/upload_keystore.keystore) is the artifact.
        # To push all secrets to GitHub use: scripts/secrets/sync-secrets-to-github.sh
        print_info "Skipping legacy secrets.env update (retired). Secrets live in secrets/live/android/keystores/."

        # Update fastlane-config/project_config.rb with UPLOAD keystore information
        update_fastlane_config "$UPLOAD_KEYSTORE_NAME" "$UPLOAD_KEYSTORE_FILE_PASSWORD" "$UPLOAD_KEYSTORE_ALIAS" "$UPLOAD_KEYSTORE_ALIAS_PASSWORD"

        # Update cmp-android/build.gradle.kts with UPLOAD keystore information
        update_gradle_config "$UPLOAD_KEYSTORE_NAME" "$UPLOAD_KEYSTORE_FILE_PASSWORD" "$UPLOAD_KEYSTORE_ALIAS" "$UPLOAD_KEYSTORE_ALIAS_PASSWORD"

        # Encode and add files from secrets/ directory
        echo ""
        encode_secrets_directory_files
    fi

    # Summary
    echo ""
    echo -e "${BLUE}==================================================================${NC}"
    echo -e "${BLUE}                          SUMMARY                                  ${NC}"
    echo -e "${BLUE}==================================================================${NC}"

    if [ $UPLOAD_RESULT -eq 0 ]; then
        echo -e "${GREEN}UPLOAD keystore: SUCCESS - $(realpath "$KEYSTORE_OUT_DIR/$UPLOAD_KEYSTORE_NAME")${NC}"
    elif [ $UPLOAD_RESULT -eq 9 ]; then
        # A skip is NOT success. Reporting it as success once produced a "regenerated" keystore that
        # was byte-identical to the old one; the failure only surfaced later, at signing.
        echo -e "${YELLOW}UPLOAD keystore: SKIPPED (already exists — nothing generated)${NC}"
    else
        echo -e "${RED}UPLOAD keystore: FAILED${NC}"
    fi

    echo ""
    echo -e "${BLUE}IMPORTANT: back up your UPLOAD keystore. Lose it and you must request an${NC}"
    echo -e "${BLUE}upload key reset from Play Console (1-2 business days). Google holds the${NC}"
    echo -e "${BLUE}app signing key in KMS — you never need a local copy.${NC}"

    if [ $UPLOAD_RESULT -eq 0 ]; then
        echo -e "${GREEN}Keystore written to: keystores/$UPLOAD_KEYSTORE_NAME${NC}"
        echo -e "${BLUE}Build files NOT modified — signing config reads the credentials at${NC}"
        echo -e "${BLUE}build time from KEYSTORE_* (env, or the properties file).${NC}"
        echo -e "${CYAN}Next step: run 'scripts/secrets/sync-secrets-to-github.sh' to push secrets to GitHub${NC}"
        return 0
    else
        return 1
    fi
}

# Function to check if key should be excluded from GitHub
should_exclude_key() {
    local key=$1
    for excluded_key in "${EXCLUDED_GITHUB_KEYS[@]}"; do
        if [ "$key" = "$excluded_key" ]; then
            return 0  # True, should exclude
        fi
    done
    return 1  # False, should not exclude
}

# Function to add secrets from secrets.env to GitHub
add_secrets_to_github() {
    local repo=$1
    local env=$2

    check_gh_cli

    echo -e "${BLUE}Adding secrets to ${repo} from $ENV_FILE${NC}"
    if [ -n "$env" ]; then
        echo -e "${BLUE}Environment: ${env}${NC}"
    fi

    # Check if secrets.env exists
    if [ ! -f "$ENV_FILE" ]; then
        echo -e "${RED}Error: $ENV_FILE not found. Please run the 'generate' or 'sync' command first.${NC}"
        exit 1
    fi

    # Process the secrets.env file
    process_secrets_file "$repo" "$env"

    echo -e "${GREEN}All secrets have been added to GitHub successfully!${NC}"
}

# Function to process secrets from file
process_secrets_file() {
    local repo=$1
    local env=$2

    echo -e "${BLUE}Processing secrets from $ENV_FILE${NC}"

    # Process the file line by line with support for multiline values
    local current_key=""
    local current_value=""
    local multiline_mode=false
    local multiline_end=""

    while IFS= read -r line || [ -n "$line" ]; do
        # Skip empty lines and comments when not in multiline mode
        if [ "$multiline_mode" = false ] && [[ -z "$line" || "$line" == \#* ]]; then
            continue
        fi

        # Check if we're in multiline mode
        if [ "$multiline_mode" = true ]; then
            # Check if this line is the end marker for multiline
            if [[ "$line" == "$multiline_end" ]]; then
                multiline_mode=false

                # Add secret only if it's not in the excluded list
                if ! should_exclude_key "$current_key"; then
                    echo -e "${BLUE}Adding multiline secret: $current_key${NC}"

                    if [ -n "$env" ]; then
                        echo -n "$current_value" | gh secret set "$current_key" --repo="$repo" --env="$env"
                    else
                        echo -n "$current_value" | gh secret set "$current_key" --repo="$repo"
                    fi
                else
                    echo -e "${YELLOW}Skipping excluded key: $current_key (not sent to GitHub)${NC}"
                fi

                current_key=""
                current_value=""
            else
                # Append this line to the multiline value
                if [ -n "$current_value" ]; then
                    current_value+=$'\n'
                fi
                current_value+="$line"
            fi
        else
            # Check if this is the start of a multiline value using pattern matching
            if echo "$line" | grep -q "<<"; then
                # Extract the key (part before <<)
                current_key=$(echo "$line" | cut -d '<' -f1 | xargs)
                # Extract the delimiter (part after <<)
                multiline_end=$(echo "$line" | sed 's/.*<<\(.*\)/\1/')
                multiline_mode=true
                current_value=""
            elif echo "$line" | grep -q "="; then
                # This is a regular key=value line
                key=$(echo "$line" | cut -d '=' -f1 | xargs)
                value=$(echo "$line" | cut -d '=' -f2-)

                # Strip quotes for the actual value
                value=$(strip_quotes "$value")

                # Add secret only if it's not in the excluded list
                if ! should_exclude_key "$key"; then
                    echo -e "${BLUE}Adding secret: $key${NC}"

                    if [ -n "$env" ]; then
                        echo -n "$value" | gh secret set "$key" --repo="$repo" --env="$env"
                    else
                        echo -n "$value" | gh secret set "$key" --repo="$repo"
                    fi
                else
                    echo -e "${YELLOW}Skipping excluded key: $key (not sent to GitHub)${NC}"
                fi
            fi
        fi
    done < "$ENV_FILE"

    # Check if we're still in multiline mode at the end of the file
    if [ "$multiline_mode" = true ]; then
        echo -e "${RED}Error: Unterminated multiline secret. Missing closing delimiter: $multiline_end${NC}"
        return 1
    fi

    return 0
}

# Function to list secrets
list_secrets() {
    local repo=$1
    local env=$2

    check_gh_cli

    echo -e "${BLUE}Listing secrets for ${repo}${NC}"

    if [ -n "$env" ]; then
        echo -e "${BLUE}Environment: ${env}${NC}"
        gh secret list --repo="$repo" --env="$env"
    else
        gh secret list --repo="$repo"
    fi
}

# Function to delete a secret
delete_secret() {
    local repo=$1
    local name=$2
    local env=$3

    check_gh_cli

    echo -e "${BLUE}Deleting secret ${name} from ${repo}${NC}"

    if [ -n "$env" ]; then
        echo -e "${BLUE}Environment: ${env}${NC}"
        gh secret delete "$name" --repo="$repo" --env="$env"
    else
        gh secret delete "$name" --repo="$repo"
    fi

    echo -e "${GREEN}Secret deleted successfully!${NC}"
}

# Function to delete all secrets in env file from GitHub repository
delete_all_repo_secrets() {
    local repo=$1
    local env=$2
    local include_excluded=${3:-false}  # Default to false if not provided

    check_gh_cli

    echo -e "${BLUE}Deleting all secrets from ${repo} that are in $ENV_FILE${NC}"
    if [ -n "$env" ]; then
        echo -e "${BLUE}Environment: ${env}${NC}"
    fi

    if [ "$include_excluded" = "true" ]; then
        echo -e "${YELLOW}Warning: Including excluded secrets in deletion${NC}"
    fi

    # Check if secrets.env exists
    if [ ! -f "$ENV_FILE" ]; then
        echo -e "${RED}Error: $ENV_FILE file not found.${NC}"
        exit 1
    fi

    # First, get a list of all secrets in the repo
    echo -e "${BLUE}Fetching current secrets from GitHub...${NC}"

    local temp_secrets_list=$(mktemp)
    if [ -n "$env" ]; then
        gh secret list --repo="$repo" --env="$env" > "$temp_secrets_list"
    else
        gh secret list --repo="$repo" > "$temp_secrets_list"
    fi

    # Variables to track progress
    local deleted_count=0
    local skipped_count=0
    local excluded_count=0
    local deleted_secrets=()
    local skipped_secrets=()
    local excluded_secrets=()

    # Process the file line by line to find secrets
    echo -e "${BLUE}Processing secrets from $ENV_FILE...${NC}"
    local multiline_mode=false
    local multiline_end=""

    while IFS= read -r line || [ -n "$line" ]; do
        # Skip empty lines and comments when not in multiline mode
        if [ "$multiline_mode" = false ] && [[ -z "$line" || "$line" == \#* ]]; then
            continue
        fi

        # Check if we're exiting a multiline block
        if [ "$multiline_mode" = true ] && [[ "$line" == "$multiline_end" ]]; then
            multiline_mode=false
            continue
        fi

        # Skip content lines inside multiline blocks
        if [ "$multiline_mode" = true ]; then
            continue
        fi

        # Extract key from regular lines or multiline start
        local key=""
        if [[ "$line" == *"<<"* ]]; then
            # Extract the key (part before <<)
            key=$(echo "$line" | cut -d '<' -f1 | xargs)
            # Extract the delimiter (part after <<)
            multiline_end=$(echo "$line" | sed 's/.*<<\(.*\)/\1/')
            multiline_mode=true
        elif [[ "$line" == *"="* ]]; then
            # This is a regular key=value line
            key=$(echo "$line" | cut -d '=' -f1 | xargs)
        else
            continue
        fi

        # Skip empty keys
        if [ -z "$key" ]; then
            continue
        fi

        # Check if key should be excluded
        local is_excluded=false
        if should_exclude_key "$key"; then
            is_excluded=true
            if [ "$include_excluded" != "true" ]; then
                echo -e "${YELLOW}Skipping excluded key: $key${NC}"
                excluded_count=$((excluded_count + 1))
                excluded_secrets+=("$key")
                continue
            else
                echo -e "${YELLOW}Including excluded key (due to flag): $key${NC}"
            fi
        fi

        # Check if the key exists in the repo
        if grep -q "$key" "$temp_secrets_list"; then
            if [ "$is_excluded" = true ]; then
                echo -e "${YELLOW}Deleting excluded secret: $key${NC}"
            else
                echo -e "${BLUE}Deleting secret: $key${NC}"
            fi

            if [ -n "$env" ]; then
                gh secret delete "$key" --repo="$repo" --env="$env"
            else
                gh secret delete "$key" --repo="$repo"
            fi

            if [ $? -eq 0 ]; then
                if [ "$is_excluded" = true ]; then
                    excluded_count=$((excluded_count + 1))
                    excluded_secrets+=("$key (deleted)")
                else
                    deleted_count=$((deleted_count + 1))
                    deleted_secrets+=("$key")
                fi
            else
                echo -e "${RED}Failed to delete secret: $key${NC}"
                skipped_count=$((skipped_count + 1))
                skipped_secrets+=("$key (error)")
            fi
        else
            echo -e "${YELLOW}Secret not found in repo: $key${NC}"
            skipped_count=$((skipped_count + 1))
            skipped_secrets+=("$key (not found)")
        fi
    done < "$ENV_FILE"

    # Clean up
    rm -f "$temp_secrets_list"

    # Summary
    echo ""
    echo -e "${BLUE}==================================================================${NC}"
    echo -e "${BLUE}                          SUMMARY                                  ${NC}"
    echo -e "${BLUE}==================================================================${NC}"
    echo -e "${GREEN}Deleted $deleted_count secrets${NC}"
    echo -e "${YELLOW}Skipped $skipped_count secrets (not found in repo or errors)${NC}"
    echo -e "${YELLOW}Excluded $excluded_count secrets${NC}"

    if [ ${#deleted_secrets[@]} -gt 0 ]; then
        echo ""
        echo -e "${GREEN}Deleted secrets:${NC}"
        for secret in "${deleted_secrets[@]}"; do
            echo -e "  - $secret"
        done
    fi

    if [ ${#excluded_secrets[@]} -gt 0 ]; then
        echo ""
        echo -e "${YELLOW}Excluded secrets:${NC}"
        for secret in "${excluded_secrets[@]}"; do
            echo -e "  - $secret"
        done
    fi

    echo ""
    echo -e "${GREEN}Secret deletion process completed${NC}"
}

INCLUDE_EXCLUDED="false"  # Default value

# Parse command line arguments
if [ "$1" != "" ]; then
    COMMAND=$1
    shift
fi

for i in "$@"; do
    case $i in
        --repo=*)
        REPO="${i#*=}"
        shift
        ;;
        --env=*)
        ENV="${i#*=}"
        shift
        ;;
        --include-excluded)
        INCLUDE_EXCLUDED="true"
        shift
        ;;
        --name=*)
        SECRET_NAME="${i#*=}"
        shift
        ;;
        --env-file=*)
        ENV_FILE_OVERRIDE="${i#*=}"
        shift
        ;;
        *)
        # Unknown option
        ;;
    esac
done

# Resolve which env file to use
resolve_env_file

# Load variables safely from secrets.env if it exists
# Only show the loading message for the view command
show_message="false"
if [ "$COMMAND" = "view" ]; then
    show_message="true"
fi

if [ -f "$ENV_FILE" ]; then
    load_env_vars "$ENV_FILE" "$show_message"
fi

# Execute the appropriate command
case $COMMAND in
    generate)
        generate_keystores
        ;;
    encode-secrets)
        # DEPRECATED: the encode-secrets / secrets.env bundle flow is retired.
        # Modern replacement: scripts/secrets/sync-secrets-to-github.sh
        # That script reads secrets/ directly and pushes to GitHub without an
        # intermediate secrets.env file. This code path is preserved for
        # backward compatibility only and will be removed in a future release.
        print_warning "encode-secrets is deprecated. Use scripts/secrets/sync-secrets-to-github.sh instead."
        create_secrets_dir
        encode_secrets_directory_files
        ;;
    sync)
        echo -e "${BLUE}==================================================================${NC}"
        echo -e "${BLUE}              Synchronizing Secrets to $ENV_FILE                 ${NC}"
        echo -e "${BLUE}==================================================================${NC}"
        echo

        # Ensure secrets directory exists
        mkdir -p "$(dirname "$ENV_FILE")"

        # Create backup
        if [[ -f "$ENV_FILE" ]]; then
            cp "$ENV_FILE" "${ENV_FILE}.backup"
            print_info "Created backup: ${ENV_FILE}.backup"
        fi

        # Step 1: Parse iOS string secrets from fork.properties + secrets/ files
        echo
        print_info "[1/7] Parsing iOS secrets from fork.properties + secrets/ files..."
        parse_shared_keys_env

        # Step 2: Parse macOS password files
        echo
        print_info "[2/7] Parsing macOS password files..."
        parse_macos_password_files

        # Step 3: Encode file-based secrets from secrets/ directory
        echo
        print_info "[3/7] Encoding file-based secrets to base64..."
        encode_secrets_directory_files

        # Step 4: Update secrets.env with iOS string secrets
        echo
        print_info "[4/7] Updating $ENV_FILE with iOS string secrets..."
        update_ios_string_secrets

        # Step 5: Add Desktop & macOS App Store placeholders (before populating passwords)
        echo
        print_info "[5/7] Adding Desktop & macOS App Store placeholders..."
        add_desktop_placeholders

        # Step 6: Update macOS password secrets (after placeholders ensure keys exist)
        echo
        print_info "[6/7] Updating $ENV_FILE with macOS password secrets..."
        update_macos_password_secrets

        # Step 7: Validate result
        echo
        print_info "[7/7] Validating $ENV_FILE..."
        echo
        if validate_sync_result; then
            echo
            print_success "Secrets synchronized successfully to $ENV_FILE"

            # Show summary
            echo
            print_info "Summary:"
            total_string=$(grep -cE "^[A-Z_]+=" "$ENV_FILE" 2>/dev/null || echo "0")
            total_file=$(grep -c "<<EOF" "$ENV_FILE" 2>/dev/null || echo "0")
            echo "  Total string secrets: $total_string"
            echo "  Total file secrets:   $total_file"
            echo
            print_info "Next steps:"
            echo "  1. Review $ENV_FILE"
            echo "  2. Upload to GitHub: ./keystore-manager.sh add --repo=owner/repo"
        else
            echo
            print_error "Validation failed - check errors above"
            print_info "Backup available at: ${ENV_FILE}.backup"
            exit 1
        fi
        ;;
    view)
        view_secrets
        ;;
    add)
        # DEPRECATED: this subcommand reads secrets.env and calls gh secret set directly.
        # Modern replacement: scripts/secrets/sync-secrets-to-github.sh
        # That script reads secrets/ per-value files and does NOT require secrets.env.
        print_warning "The 'add' subcommand is deprecated. Use scripts/secrets/sync-secrets-to-github.sh instead."
        if [ -z "$REPO" ]; then
            echo -e "${RED}Error: Repository is required.${NC}"
            echo -e "Usage: ./keystore-manager.sh add --repo=username/repo [--env=environment]"
            exit 1
        fi
        add_secrets_to_github "$REPO" "$ENV"
        ;;
    list)
        if [ -z "$REPO" ]; then
            echo -e "${RED}Error: Repository is required.${NC}"
            echo -e "Usage: ./keystore-manager.sh list --repo=username/repo [--env=environment]"
            exit 1
        fi
        list_secrets "$REPO" "$ENV"
        ;;
    delete)
        if [ -z "$REPO" ] || [ -z "$SECRET_NAME" ]; then
            echo -e "${RED}Error: Repository and secret name are required.${NC}"
            echo -e "Usage: ./keystore-manager.sh delete --repo=username/repo --name=SECRET_NAME [--env=environment]"
            exit 1
        fi
        delete_secret "$REPO" "$SECRET_NAME" "$ENV"
        ;;
    delete-all)
        if [ -z "$REPO" ]; then
            echo -e "${RED}Error: Repository is required.${NC}"
            echo -e "Usage: ./keystore-manager.sh delete-all --repo=username/repo [--env=environment] [--include-excluded]"
            exit 1
        fi
        delete_all_repo_secrets "$REPO" "$ENV" "$INCLUDE_EXCLUDED"
        ;;
    help)
        show_help
        ;;
    *)
        echo -e "${RED}Unknown command: $COMMAND${NC}"
        show_help
        exit 1
        ;;
esac
