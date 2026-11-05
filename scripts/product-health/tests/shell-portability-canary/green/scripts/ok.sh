#!/usr/bin/env bash
sed -i.bak -e 's/^version: .*/version: 1/' "$LEDGER"
rm -f "$LEDGER.bak"
PERMS=$(stat -f "%A" "$KEY_FILE" 2>/dev/null || stat -c "%a" "$KEY_FILE" 2>/dev/null)
