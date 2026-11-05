#!/usr/bin/env bash
PERMS=$(stat -f "%A" "$KEY_FILE")
