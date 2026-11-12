#!/usr/bin/env bash
# Fixture: the ci-prepush.sh shape that died on 2026-09-02. `successful` is empty whenever every
# task fails, and the loop below has no count guard — so on bash 3.2 the summary aborts here and
# the "Failed" section underneath never prints.
set -uo pipefail
successful=()
failed=("detekt")
echo "Successful: ${#successful[@]}"
for t in "${successful[@]}"; do echo "  ok $t"; done
if [ "${#failed[@]}" -gt 0 ]; then
    for t in "${failed[@]}"; do echo "  fail $t"; done
fi
