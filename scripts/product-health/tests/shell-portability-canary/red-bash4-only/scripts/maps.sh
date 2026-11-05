#!/bin/bash
# Fixture: the keystore-manager.sh / sync-dirs.sh shape. On bash 3.2 `declare -A` is rejected and
# the initializer then runs as an INDEXED assignment — every key evaluates to 0, the entries
# collide, and the map silently holds one wrong value instead of N right ones.
MAP_KEYS=()
declare -A EXCLUSIONS=(
    ["cmp-android"]="src/main/res:dir"
    ["cmp-ios"]="iosApp/Assets.xcassets:dir"
)
mapfile -t lines < <(printf 'a\nb\n')
echo "${EXCLUSIONS[cmp-android]} ${#lines[@]}"
