#!/bin/bash
#
# Firebase Project Setup Script
# Creates Firebase project and registers Android/iOS apps
#

# Colors and formatting
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m' # No Color

# Emoji indicators
CHECK_MARK="✅"
WARNING="⚠️"
ROCKET="🚀"
FIRE="🔥"

# Exit on any error
set -e

# Print section header
print_section() {
    echo
    echo -e "${BLUE}╔════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║${NC} ${BOLD}$1${NC}"
    echo -e "${BLUE}╚════════════════════════════════════════════╝${NC}"
    echo
}

# Print success message
print_success() {
    echo -e "${GREEN}${CHECK_MARK} $1${NC}"
}

# Print warning message
print_warning() {
    echo -e "${YELLOW}${WARNING} $1${NC}"
}

# Print info message
print_info() {
    echo -e "${CYAN}$1${NC}"
}

# Print error message
print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Function to read package name from libs.versions.toml
get_package_from_version_catalog() {
    local toml_file="gradle/libs.versions.toml"
    
    if [ ! -f "$toml_file" ]; then
        print_error "libs.versions.toml not found at $toml_file"
        exit 1
    fi
    
    # Extract androidPackageNamespace value
    local package_name=$(grep "^androidPackageNamespace" "$toml_file" | sed 's/androidPackageNamespace = "\(.*\)"/\1/' | tr -d ' ')
    
    if [ -z "$package_name" ]; then
        print_error "Could not find androidPackageNamespace in libs.versions.toml"
        exit 1
    fi
    
    echo "$package_name"
}

# Validate arguments
if [[ $# -lt 2 ]]; then
    echo -e "${RED}${WARNING} Invalid arguments${NC}"
    echo -e "${CYAN}Usage: bash scripts/white-label/firebase.sh <firebase-project-id> <ios-bundle-id>${NC}"
    echo -e "${CYAN}Example: bash scripts/white-label/firebase.sh my-app-12345 com.example.myapp${NC}"
    echo
    echo -e "${YELLOW}Note: Android package name will be read from gradle/libs.versions.toml${NC}"
    exit 1
fi

FIREBASE_PROJECT_ID=$1
IOS_BUNDLE_ID=$2

# Read Android package from version catalog
print_section "Reading Configuration"
ANDROID_PACKAGE=$(get_package_from_version_catalog)
print_info "Android Package (from libs.versions.toml): $ANDROID_PACKAGE"
print_info "iOS Bundle ID: $IOS_BUNDLE_ID"
print_info "Firebase Project ID: $FIREBASE_PROJECT_ID"

# Check if Firebase CLI is installed
check_firebase_cli() {
    print_section "Checking Firebase CLI"
    
    if ! command -v firebase &> /dev/null; then
        print_error "Firebase CLI is not installed"
        echo -e "${YELLOW}Install with: npm install -g firebase-tools${NC}"
        exit 1
    fi
    
    print_success "Firebase CLI is installed"
}

# Check if user is logged in to Firebase
check_firebase_auth() {
    if ! firebase projects:list &> /dev/null; then
        print_error "Not logged in to Firebase"
        echo -e "${YELLOW}Run: firebase login${NC}"
        exit 1
    fi
    
    print_success "Firebase authentication verified"
}

# Setup Firebase project
setup_firebase_project() {
    print_section "Setting Up Firebase Project"
    
    # Check if project exists
    if firebase projects:list | grep -q "$FIREBASE_PROJECT_ID"; then
        print_warning "Firebase project '$FIREBASE_PROJECT_ID' already exists. Using existing project."
    else
        print_info "Creating new Firebase project: $FIREBASE_PROJECT_ID"
        print_warning "If creation fails, create it manually at: https://console.firebase.google.com"
        
        firebase projects:create "$FIREBASE_PROJECT_ID" --display-name "$FIREBASE_PROJECT_ID" || {
            print_warning "Could not create via CLI. Create manually in Firebase Console."
            read -p "Press Enter after creating the project..."
        }
    fi
    
    firebase use "$FIREBASE_PROJECT_ID"
    print_success "Using Firebase project: $FIREBASE_PROJECT_ID"
}

# Register Android app
register_android_app() {
    local package_name=$1
    local app_nickname=$2
    
    print_info "Registering: $app_nickname ($package_name)"
    
    # Check if app already exists
    local app_list=$(firebase apps:list ANDROID --project="$FIREBASE_PROJECT_ID" 2>/dev/null || echo "")
    
    if echo "$app_list" | grep -q "$package_name"; then
        print_warning "Already registered: $package_name"
        return 0
    fi
    
    firebase apps:create ANDROID "$package_name" \
        --display-name="$app_nickname" \
        --project="$FIREBASE_PROJECT_ID" || {
        print_error "Failed to register: $package_name"
        return 1
    }
    
    print_success "Registered: $app_nickname"
}

# Register iOS app
register_ios_app() {
    local bundle_id=$1
    local app_nickname=$2
    
    print_info "Registering: $app_nickname ($bundle_id)"
    
    local app_list=$(firebase apps:list IOS --project="$FIREBASE_PROJECT_ID" 2>/dev/null || echo "")
    
    if echo "$app_list" | grep -q "$bundle_id"; then
        print_warning "Already registered: $bundle_id"
        return 0
    fi
    
    firebase apps:create IOS "$bundle_id" \
        --display-name="$app_nickname" \
        --project="$FIREBASE_PROJECT_ID" || {
        print_error "Failed to register: $bundle_id"
        return 1
    }
    
    print_success "Registered: $app_nickname"
}

# Get Firebase App ID
get_android_app_id() {
    local package_name=$1
    firebase apps:list ANDROID --project="$FIREBASE_PROJECT_ID" 2>/dev/null | \
        grep "$package_name" | awk '{print $4}' | head -1
}

# Print the Firebase app id for iOS bundle id $1, or nothing when it is not registered.
# Empty output is the "not registered yet" signal the caller branches on, which is why
# firebase's stderr is discarded rather than surfaced.
get_ios_app_id() {
    local bundle_id=$1
    firebase apps:list IOS --project="$FIREBASE_PROJECT_ID" 2>/dev/null | \
        grep "$bundle_id" | awk '{print $4}' | head -1
}

# Create google-services.json with all 4 variants
create_google_services_json() {
    print_section "Creating google-services.json with All Variants"
    
    local google_services_file="cmp-android/google-services.json"
    
    print_info "Downloading configs from Firebase..."
    
    # Get both app IDs
    local release_app_id=$(get_android_app_id "$ANDROID_PACKAGE")
    local demo_app_id=$(get_android_app_id "${ANDROID_PACKAGE}.demo")
    
    if [ -z "$release_app_id" ] || [ -z "$demo_app_id" ]; then
        print_error "Could not retrieve Firebase App IDs"
        print_info "Release App ID: $release_app_id"
        print_info "Demo App ID: $demo_app_id"
        return 1
    fi
    
    # Download configs for both variants
    print_info "Downloading config for release variant..."
    firebase apps:sdkconfig ANDROID "$release_app_id" \
        --project="$FIREBASE_PROJECT_ID" \
        --out="$google_services_file.release" || {
        print_error "Failed to download release config"
        return 1
    }
    
    print_info "Downloading config for demo variant..."
    firebase apps:sdkconfig ANDROID "$demo_app_id" \
        --project="$FIREBASE_PROJECT_ID" \
        --out="$google_services_file.demo" || {
        print_error "Failed to download demo config"
        return 1
    }
    
    # Create google-services.json with all 4 variants
    if command -v python3 &> /dev/null; then
        print_info "Creating google-services.json with 4 app variants..."
        python3 << EOF
import json

# Read both Firebase configs
with open("$google_services_file.release", 'r') as f:
    release_config = json.load(f)
    
with open("$google_services_file.demo", 'r') as f:
    demo_config = json.load(f)

# Get the client configs
release_client = release_config['client'][0] if 'client' in release_config and len(release_config['client']) > 0 else None
demo_client = demo_config['client'][0] if 'client' in demo_config and len(demo_config['client']) > 0 else None

if not release_client or not demo_client:
    print("Error: Could not extract client configs")
    exit(1)

# Create debug variant by duplicating release config with different package name
debug_client = json.loads(json.dumps(release_client))
debug_client['client_info']['android_client_info']['package_name'] = "$ANDROID_PACKAGE.debug"

# Create demo.debug variant by duplicating demo config with different package name
demo_debug_client = json.loads(json.dumps(demo_client))
demo_debug_client['client_info']['android_client_info']['package_name'] = "$ANDROID_PACKAGE.demo.debug"

# Build final config with all 4 variants
final_config = release_config.copy()
final_config['client'] = [
    release_client,      # 1. Release: $ANDROID_PACKAGE
    debug_client,        # 2. Debug: $ANDROID_PACKAGE.debug
    demo_client,         # 3. Demo: $ANDROID_PACKAGE.demo
    demo_debug_client    # 4. Demo Debug: $ANDROID_PACKAGE.demo.debug
]

# Write the merged config
with open("$google_services_file", 'w') as f:
    json.dump(final_config, f, indent=2)
    
print("Successfully created google-services.json with 4 app variants")
EOF
        
        if [ $? -eq 0 ]; then
            # Clean up temporary files
            rm -f "$google_services_file.release" "$google_services_file.demo"
            print_success "Created google-services.json with all 4 app variants"
            echo
            print_info "App variants in google-services.json:"
            print_info "  1. Release:     $ANDROID_PACKAGE"
            print_info "  2. Debug:       $ANDROID_PACKAGE.debug"
            print_info "  3. Demo:        $ANDROID_PACKAGE.demo"
            print_info "  4. Demo Debug:  $ANDROID_PACKAGE.demo.debug"
            echo
            print_warning "Note: Debug variants share Firebase credentials with their release counterparts"
        else
            print_error "Failed to create google-services.json"
            return 1
        fi
    else
        print_error "Python3 is required to create google-services.json with multiple variants"
        print_info "Install Python3 and run this script again"
        print_info "Using release config as fallback..."
        mv "$google_services_file.release" "$google_services_file"
        rm -f "$google_services_file.demo"
        print_warning "Only release variant added. Manually add other variants in Firebase Console"
        return 1
    fi
}

# Download GoogleService-Info.plist
download_google_service_info() {
    local bundle_id=$1
    local output_path="cmp-ios/iosApp/GoogleService-Info.plist"
    
    print_info "Downloading GoogleService-Info.plist"
    
    local app_id=$(get_ios_app_id "$bundle_id")
    
    if [ -z "$app_id" ]; then
        print_error "Could not find iOS app ID"
        return 1
    fi
    
    mkdir -p "$(dirname "$output_path")"
    
    firebase apps:sdkconfig IOS "$app_id" \
        --project="$FIREBASE_PROJECT_ID" \
        --out="$output_path" || {
        print_error "Failed to download GoogleService-Info.plist"
        return 1
    }
    
    print_success "Downloaded GoogleService-Info.plist"
}

# Update project_config.rb with Firebase App IDs
update_project_config() {
    print_section "Updating Fastlane Configuration"
    
    local config_file="fastlane-config/project_config.rb"
    
    if [ ! -f "$config_file" ]; then
        print_error "Configuration file not found: $config_file"
        return 1
    fi
    
    print_info "Fetching Firebase App IDs..."
    
    local android_release_id=$(get_android_app_id "$ANDROID_PACKAGE")
    local android_demo_id=$(get_android_app_id "${ANDROID_PACKAGE}.demo")
    local ios_app_id=$(get_ios_app_id "$IOS_BUNDLE_ID")
    
    # Update Android Firebase config - Release variant
    if [ -n "$android_release_id" ]; then
        sed -i.bak "0,/firebase_prod_app_id:.*/ s|firebase_prod_app_id:.*|firebase_prod_app_id: \"$android_release_id\",|" "$config_file"
        print_success "Updated Android release Firebase App ID"
    fi
    
    # Update Android Firebase config - Demo variant
    if [ -n "$android_demo_id" ]; then
        sed -i.bak "s|firebase_demo_app_id:.*|firebase_demo_app_id: \"$android_demo_id\",|g" "$config_file"
        print_success "Updated Android demo Firebase App ID"
    fi
    
    # Update iOS Firebase config
    if [ -n "$ios_app_id" ]; then
        sed -i.bak "/firebase: {/,/}/ s|app_id: \"[^\"]*\"|app_id: \"$ios_app_id\"|" "$config_file"
        print_success "Updated iOS Firebase App ID"
    fi
    
    rm -f "$config_file.bak"
}

# Copy google-services.json to secrets folder
copy_to_secrets() {
    local secrets_dir="secrets"
    mkdir -p "$secrets_dir"
    
    if [ -f "cmp-android/google-services.json" ]; then
        cp "cmp-android/google-services.json" "$secrets_dir/google-services.json"
        print_success "Copied google-services.json to secrets/"
    fi
}

# Main execution
main() {
    echo -e "${BLUE}"
    echo "╔══════════════════════════════════════════════════════════════╗"
    echo "║           ${FIRE} Firebase Project Setup ${FIRE}                      ║"
    echo "╚══════════════════════════════════════════════════════════════╝"
    echo -e "${NC}"
    
    print_info "Firebase Project ID: $FIREBASE_PROJECT_ID"
    print_info "Android Package: $ANDROID_PACKAGE (from version catalog)"
    print_info "iOS Bundle ID: $IOS_BUNDLE_ID"
    echo
    
    # Check prerequisites
    check_firebase_cli
    check_firebase_auth
    
    # Setup Firebase project
    setup_firebase_project
    
    # Register only 2 Android apps in Firebase
    print_section "Registering Android Apps in Firebase"
    print_info "Registering 2 Firebase apps:"
    print_info "  1. Release: $ANDROID_PACKAGE"
    print_info "  2. Demo: ${ANDROID_PACKAGE}.demo"
    echo
    print_warning "Debug variants will share credentials with release variants"
    echo
    
    register_android_app "$ANDROID_PACKAGE" "Release"
    register_android_app "${ANDROID_PACKAGE}.demo" "Demo"
    
    # Register iOS app
    print_section "Registering iOS App"
    register_ios_app "$IOS_BUNDLE_ID" "iOS"
    
    # Wait for Firebase to process
    print_info "Waiting for Firebase to process registrations..."
    sleep 3
    
    # Create google-services.json with all 4 variants
    create_google_services_json
    
    # Download iOS configuration
    print_section "Downloading iOS Configuration"
    download_google_service_info "$IOS_BUNDLE_ID"
    
    # Update project configuration
    update_project_config
    
    # Copy files to secrets
    copy_to_secrets
    
    # Summary
    print_section "Setup Complete"
    echo -e "${GREEN}${CHECK_MARK} Firebase project configured successfully!${NC}"
    echo
    echo -e "${CYAN}Project: ${BOLD}$FIREBASE_PROJECT_ID${NC}"
    echo -e "${CYAN}Console: ${BOLD}https://console.firebase.google.com/project/$FIREBASE_PROJECT_ID${NC}"
    echo
    echo -e "${CYAN}Firebase Apps Registered (2 apps):${NC}"
    echo -e "  ${BOLD}1. Android Release:${NC} $ANDROID_PACKAGE"
    echo -e "  ${BOLD}2. Android Demo:${NC} ${ANDROID_PACKAGE}.demo"
    echo -e "  ${BOLD}3. iOS:${NC} $IOS_BUNDLE_ID"
    echo
    echo -e "${CYAN}google-services.json Variants (4 variants):${NC}"
    echo -e "  ${BOLD}1. Release:${NC}     $ANDROID_PACKAGE"
    echo -e "  ${BOLD}2. Debug:${NC}       $ANDROID_PACKAGE.debug ${YELLOW}(shares Release credentials)${NC}"
    echo -e "  ${BOLD}3. Demo:${NC}        ${ANDROID_PACKAGE}.demo"
    echo -e "  ${BOLD}4. Demo Debug:${NC}  ${ANDROID_PACKAGE}.demo.debug ${YELLOW}(shares Demo credentials)${NC}"
    echo
    echo -e "${YELLOW}Important Notes:${NC}"
    echo -e "  • Only 2 apps created in Firebase Console (saves quota)"
    echo -e "  • Debug variants use same Firebase credentials as their release counterparts"
    echo -e "  • All 4 variants configured in google-services.json for Gradle build variants"
    echo
    echo -e "${YELLOW}Next Steps:${NC}"
    echo -e "  1. Enable required Firebase services in console"
    echo -e "  2. Set up App Distribution groups"
    echo -e "  3. Download service account key → secrets/"
    echo
}

# Execute main function
main
