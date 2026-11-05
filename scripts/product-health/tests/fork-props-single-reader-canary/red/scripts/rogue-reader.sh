#!/usr/bin/env bash
# Fixture: a NEW hand-rolled parser of gradle/fork.properties — exactly what the gate must catch.
# Note it lacks head -1 and inline-# stripping, so it diverges from fp_get on a duplicated key or a
# comment-annotated bridge (the state setup-project.sh produces by copying .template).
APP_ID=$(grep -E "^app\.id=" gradle/fork.properties 2>/dev/null | cut -d= -f2-)
echo "$APP_ID"
