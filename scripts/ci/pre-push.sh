#!/bin/sh
# Installed as .git/hooks/pre-push by `./gradlew installGitHooks`. The checks live in
# scripts/ci/kotlin-checks.sh, scoped to what the push changes, so a template sync updates them
# without reinstalling this hook.
exec bash scripts/ci/kotlin-checks.sh pre-push
