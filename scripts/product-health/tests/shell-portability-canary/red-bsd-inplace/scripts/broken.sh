#!/usr/bin/env bash
# Verbatim shape of the 2026-09-09 defect: BSD-only in-place edit.
sed -i '' -e 's/^version: .*/version: 1/' "$LEDGER"
