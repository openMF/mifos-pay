#!/usr/bin/env bash
# Fixture: the CORRECT shape — explanation above the command, continuation unbroken.
# Exclude the WHOLE workerKmpAppCodegen family, never a subset.
set -euo pipefail
"$GRADLEW" -p "$REPO_ROOT" ":cmp-shared:linkDebugFrameworkIosArm64" \
  -x :cmp-shared:workerKmpAppCodegenAll \
  -x :sync:workerKmpAppCodegenWeb
