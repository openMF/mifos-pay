#!/usr/bin/env bash
# Fixture: the two spellings SP-4 must accept — the `[@]+` idiom, and a real count guard above.
set -uo pipefail
successful=()
failed=()
for t in ${successful[@]+"${successful[@]}"}; do echo "  ok $t"; done
if [ "${#failed[@]}" -gt 0 ]; then
    for t in "${failed[@]}"; do echo "  fail $t"; done
fi
