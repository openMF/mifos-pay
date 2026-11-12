#!/usr/bin/env bash
# Fixture: the real cmp-ios/scripts/embed-xcframework.sh shape that failed on 2026-09-17.
# The trailing backslash splices the comment onto the command, so the command ends at the `#`,
# every -x below is dropped, and the orphaned block runs as a command (exit 127 under set -e).
set -euo pipefail
"$GRADLEW" -p "$REPO_ROOT" ":cmp-shared:linkDebugFrameworkIosArm64" \
  # Exclude the WHOLE workerKmpAppCodegen family, never a subset.
  -x :cmp-shared:workerKmpAppCodegenAll \
  -x :sync:workerKmpAppCodegenWeb
