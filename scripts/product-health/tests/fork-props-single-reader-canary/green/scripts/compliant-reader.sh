#!/usr/bin/env bash
# Fixture: the compliant shape — reads gradle/fork.properties through the shared bash reader.
. "$(dirname "${BASH_SOURCE[0]}")/../../../../_shared/fork-props.sh"
FORK_PROPERTIES="gradle/fork.properties"
echo "$(fp_get app.id)"
