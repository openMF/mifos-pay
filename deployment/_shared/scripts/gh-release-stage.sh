#!/usr/bin/env bash
# deployment/_shared/scripts/gh-release-stage.sh
#
# Apply GitHub Release `prerelease` + `latest` flags per the direct-distribution
# promotion ladder (Stage 1 RC → Stage 2 Beta → Stage 3 Production). Shared by
# every desktop direct-distribution script that uploads to GH Releases:
#   - deployment/desktop/macos-dmg-unsigned/script.sh   (kept for legacy parity)
#   - deployment/desktop/windows-exe/script.sh
#   - deployment/desktop/msi-signed/script.sh
#   - deployment/desktop/linux-deb/script.sh
# (The Ruby Fastlane equivalent lives inline in dmg-notarized/lane.rb as
# `_set_release_stage_flags`.)
#
# Usage:
#   bash deployment/_shared/scripts/gh-release-stage.sh <tag> <stage>
# Where <stage> ∈ {prerelease, beta, stable}.
#
# Mapping:
#   prerelease → prerelease: true,  latest: false   (Stage 1)
#   beta       → prerelease: false, latest: false   (Stage 2)
#   stable     → prerelease: false, latest: true    (Stage 3)
#
# No-op without GH_TOKEN — `gh release edit` requires auth.
set -euo pipefail

TAG="${1:?tag required}"
STAGE="${2:-stable}"

case "$STAGE" in
  prerelease) PRERELEASE=true;  LATEST=false ;;
  beta)       PRERELEASE=false; LATEST=false ;;
  stable)     PRERELEASE=false; LATEST=true  ;;
  *)
    echo "Invalid STAGE: $STAGE (expected: prerelease | beta | stable)" >&2
    exit 2
    ;;
esac

REPO_ARG=()
[[ -n "${GITHUB_REPOSITORY:-}" ]] && REPO_ARG=(--repo "$GITHUB_REPOSITORY")

gh release edit "$TAG" --prerelease="$PRERELEASE" --latest="$LATEST" ${REPO_ARG[@]+"${REPO_ARG[@]}"} >/dev/null
echo "🏷  Release $TAG → prerelease=$PRERELEASE, latest=$LATEST"
