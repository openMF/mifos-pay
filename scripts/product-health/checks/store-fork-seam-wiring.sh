#!/usr/bin/env bash
# store-fork-seam-wiring.sh — the template actually CONSULTS its fork seams.
#
# WHY THIS EXISTS
# `ForkErrorMapper` and `ForkScreenStateDefaults` are the only places a fork customizes error
# copy and screen-state visuals without editing a template-owned file. That contract holds only if
# the template files call them. Delete `projectCopy(error) ?:` or `.applyProjectOverrides()` and
# NOTHING fails: the template's own behaviour is unchanged, every unit test still passes, and the
# loss surfaces only in a fork whose branding silently stops applying.
#
# Both call sites sit inside `@Composable` functions and `core/store` does not carry the Compose
# ui-test dependency, so a unit test cannot reach them. `KptSeamContractTest` covers the seams' DEFAULTS
# (null / identity); this covers the WIRING.
#
# CONTRACT
#   FS-1 config/KptErrorMapper consults projectErrorMessage BEFORE its own categorised branches
#   FS-2 config/KptScreenStateDefaults applies applyProjectOverrides to what it returns
#   FS-3 the seams are NEUTRAL on the template (bare `object X : Overrides`, no override) — a
#        template that customized itself would make every fork inherit its choices
#   FS-4 the CONTRACT interfaces stay TEMPLATE-owned and keep their DEFAULT bodies, so a new hook
#        ships compatibly and no fork breaks on the sync that adds it
set -uo pipefail
cd "$(dirname "$0")/../../.." || exit 2
B="core/store/src/commonMain/kotlin/kpt/core/store"
fails=0

# Assert file $1 contains regex $2. $3 is the check id, $4 the message on failure.
# A missing file is a failure, not a skip — the seam is required, so its absence is the defect.
need() { # need <file> <regex> <id> <why>
  if [ ! -f "$1" ]; then echo "  ❌ $3 missing file: $1"; fails=$((fails + 1)); return; fi
  if ! grep -qE "$2" "$1"; then
    echo "  ❌ $3 $4"
    echo "       expected /$2/ in ${1#./}"
    fails=$((fails + 1))
  fi
}

# Assert file $1 does NOT contain regex $2 — the inverse of `need`, for a pattern that must
# have been removed.
deny() { # deny <file> <regex> <id> <why>
  if [ ! -f "$1" ]; then echo "  ❌ $3 missing file: $1"; fails=$((fails + 1)); return; fi
  if grep -qE "$2" "$1"; then
    echo "  ❌ $3 $4"
    echo "       forbidden /$2/ found in ${1#./}"
    fails=$((fails + 1))
  fi
}

# FS-1 — the ?: ordering is the contract: fork first, framework as fallback.
need "$B/config/KptErrorMapper.kt" 'projectCopy\(error\)[[:space:]]*\?:' "FS-1" \
  "KptErrorMapper no longer consults the fork seam — a fork's error copy would never appear"
need "$B/config/KptErrorMapper.kt" 'ForkErrorMapper::message' "FS-1" \
  "KptErrorMapper does not reference the ForkErrorMapper seam at all"

# FS-2 — applied to the RETURNED value, not computed and discarded.
need "$B/config/KptScreenStateDefaults.kt" '\)\.let\(ForkScreenStateDefaults::customize\)' "FS-2" \
  "KptScreenStateDefaults no longer applies the fork overrides — a fork's branding would be dropped"

# FS-3 — template neutrality. ONLY on the template itself: in a fork, overriding these seams is the
# whole point of them (a fork's own error copy and branding), so asserting neutrality there would
# forbid exactly what the seam exists for.
if [ "${TEMPLATE_SELF_BUILD:-}" = "1" ]; then
need "$B/config/ForkErrorMapper.kt" '^object ForkErrorMapper : ErrorMessageOverrides$' "FS-3" \
  "the template's ForkErrorMapper is not the bare neutral implementation"
deny "$B/config/ForkErrorMapper.kt" '^[[:space:]]*override fun message' "FS-3" \
  "the template OVERRIDES message — it would shadow the framework's categorised copy in every fork"
need "$B/config/ForkScreenStateDefaults.kt" '^object ForkScreenStateDefaults : ScreenStateOverrides$' "FS-3" \
  "the template's ForkScreenStateDefaults is not the bare neutral implementation"
deny "$B/config/ForkScreenStateDefaults.kt" '^[[:space:]]*override fun customize' "FS-3" \
  "the template OVERRIDES customize — every fork would inherit the template's own branding"
fi

# FS-4 — the CONTRACT stays template-owned. If the interfaces ever migrate into the fork-owned
# files, the defaults migrate with them and the whole reason for the interface disappears: a new
# hook could no longer ship with a default body, and the next sync would break every fork.
need "$B/config/KptErrorMapper.kt" '^interface ErrorMessageOverrides \{' "FS-4" \
  "ErrorMessageOverrides is not declared in the template-owned KptErrorMapper.kt"
need "$B/config/KptScreenStateDefaults.kt" '^interface ScreenStateOverrides \{' "FS-4" \
  "ScreenStateOverrides is not declared in the template-owned KptScreenStateDefaults.kt"
need "$B/config/KptErrorMapper.kt" 'fun message\(error: Throwable\): String\? = null' "FS-4" \
  "ErrorMessageOverrides.message lost its null default — a fork adding no override would break the fallback"
need "$B/config/KptScreenStateDefaults.kt" 'fun customize\(defaults: ScreenStateDefaults\): ScreenStateDefaults = defaults' "FS-4" \
  "ScreenStateOverrides.customize lost its identity default — a new hook could not ship compatibly"

[ "$fails" -eq 0 ] && { echo "✅ store-fork-seam-wiring: FS-1..FS-4 pass"; exit 0; }
echo "❌ store-fork-seam-wiring: $fails failure(s)"; exit 1
