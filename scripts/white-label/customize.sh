#!/bin/bash
#
# DEPRECATED — renamed to fork-init.sh on 2026-09-16.
#
# "Customizer" was ambiguous about what it actually does (write fork identity, propagate it via
# syncForkConfig, strip the demo), and the ambiguity was not harmless: the framework accumulated
# 28 references to a `customizer.sh` that never existed alongside 9 to `customize.sh`, and
# /project-add's identity step carried a "skip if absent" guard — so it silently skipped on every
# run. The new name matches the vocabulary it already lives in: gradle/fork.properties,
# ./gradlew syncForkConfig, `owner: fork`.
#
# This shim exists because forks that already synced carry `customize.sh` in their trees and in
# their own docs/CI. It forwards every argument unchanged. It will be removed in a later template
# release; move call sites to fork-init.sh.
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
echo "⚠️  customize.sh is DEPRECATED — use fork-init.sh (forwarding unchanged)." >&2
exec bash "$HERE/fork-init.sh" "$@"
