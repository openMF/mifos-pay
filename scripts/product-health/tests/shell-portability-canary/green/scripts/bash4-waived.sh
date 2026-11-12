#!/usr/bin/env bash
# bash4-required: associative array is load-bearing here; a version guard aborts on bash 3.
# Fixture: the sanctioned opt-out. SP-5 must stay silent when a script declares the requirement.
if [ -z "${BASH_VERSINFO:-}" ] || [ "${BASH_VERSINFO[0]}" -lt 4 ]; then
    echo "requires bash 4+" >&2; exit 1
fi
declare -A M=( ["a"]="1" )
echo "${M[a]}"
