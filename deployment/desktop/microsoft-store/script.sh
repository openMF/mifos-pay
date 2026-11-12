#!/usr/bin/env bash
# deployment/desktop/microsoft-store/script.sh
# Tier-2 Microsoft Store MSIX → Partner Center submission.
# Runs under Git Bash on windows-latest. Assumes MakeAppx.exe is on PATH or
# its absolute path is provided via MAKEAPPX env var.
set -euo pipefail

FLAVOR="${FLAVOR:-prod}"
MAKEAPPX="${MAKEAPPX:?MAKEAPPX env var required (path to MakeAppx.exe)}"
MS_PARTNER_CENTER_TENANT_ID="${MS_PARTNER_CENTER_TENANT_ID:?ms_partner_center_tenant_id required}"
MS_PARTNER_CENTER_CLIENT_ID="${MS_PARTNER_CENTER_CLIENT_ID:?ms_partner_center_client_id required}"
MS_PARTNER_CENTER_CLIENT_SECRET="${MS_PARTNER_CENTER_CLIENT_SECRET:?ms_partner_center_client_secret required}"
MS_APP_ID="${MS_APP_ID:?ms_app_id required}"

# 1. Build app payload (createReleaseDistributable produces /app/<App>.app).
./gradlew :cmp-desktop:createReleaseDistributable -PflavorDimension="$FLAVOR"

# 2. Stage MSIX layout: app payload + this fork's AppxManifest.xml. The committed Package.appxmanifest
#    is a template with neutral identity values; fork-identity.sh renders the fork's (from
#    gradle/fork.properties) into the stage, so nothing under deployment/ is ever rewritten.
ID="$(dirname "$0")/../../_shared/scripts/fork-identity.sh"
STAGE="cmp-desktop/build/compose/binaries/main-release/msix-stage"
APP_LAYOUT="cmp-desktop/build/compose/binaries/main-release/app"
MANIFEST="$(dirname "$0")/Package.appxmanifest"
# One name for the package, shared with the StoreBroker config's packagePath (it was mifos.msix here
# while StoreBroker pointed at <project>.msix).
OUTPUT="cmp-desktop/build/compose/binaries/main-release/msix/$(bash "$ID" msix-name)"
mkdir -p "$STAGE" "$(dirname "$OUTPUT")"
cp -R "$APP_LAYOUT/." "$STAGE/"
bash "$ID" appx "$MANIFEST" "$STAGE/AppxManifest.xml"

# 3. Pack via MakeAppx.exe.
"$MAKEAPPX" pack /d "$STAGE" /p "$OUTPUT" /overwrite

# 4. Submit to Partner Center. The StoreBroker config is rendered for this fork, beside the package.
bash "$ID" storebroker cmp-desktop/build/ms-store/StoreBroker.config.json
# In CI we invoke microsoft/StoreBroker-Action; locally invoke `Submit-StoreBrokerSubmission`
# via PowerShell. This bash path documents the contract; PS variant is preferred locally.
echo "Submission step is wired through GH Action (CI) or PowerShell StoreBroker module (local)."
echo "Package built at: $OUTPUT"
