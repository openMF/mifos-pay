#!/usr/bin/env bash
# scripts/_shared/fork-props.sh — the ONE bash reader of gradle/fork.properties.
#
# `app-profile` (app.yaml + platforms) is the fork SoT; `scripts/white-label/derive.rb` materializes
# it into the derived build-bridge `gradle/fork.properties`; everything downstream READS that bridge.
# This file is the bash end of that contract. Its Gradle counterpart is
# `build-logic/convention/src/main/kotlin/org/convention/KptAppProfileProperties.kt`, and its Ruby counterpart
# is `FORK[...]` in `deployment/_shared/project_config.rb`.
#
# WHY one reader: independent parsers are how resolution silently diverges. The 2026-09-06 signing
# split — `deployment/Appfile` preferring the FILE while `_shared/project_config.rb` preferred the
# ENV for the same `apple.team.id` — was that failure at the Ruby layer. `fp_get` already enforced
# the rule inside `scripts/product-health/` ("exactly one parser and one SoT — a check never
# re-implements the read"); this promotes it out of that subsystem so every bash caller can share it.
#
# PARSING SEMANTICS (the reason a shared reader is worth more than a shared regex):
#   · head -1          — first match wins, so a duplicated key yields ONE value, not a concatenation.
#   · inline `#` strip — `key=value   # note` reads as `value`. NOTE: the shipped .template carries
#                        226 FULL-LINE comments but no inline ones, so this is a LATENT guard, not a
#                        live fix — it covers a hand-authored or future bridge, not today's.
#   · trailing-space strip.
# The ad-hoc idiom this replaced (`grep … | cut -d= -f2- | tr -d '\n\r'`) does none of these. On a
# bridge with an inline comment it yields `ABCD123456   # PLACEHOLDER — …` as an Apple Team ID; on a
# duplicated key it concatenates (`Acme LtdAcme Holdings Ltd`) into a keystore DN. Both are latent
# today — agreement by luck, not by contract, which is why the parse lives in one place.
#
# Usage:
#   source "$(git rev-parse --show-toplevel)/scripts/_shared/fork-props.sh"
#   FORK_PROPERTIES="$(fp_file "$ROOT")" || exit 1
#   team="$(fp_get apple.team.id)"
#
# NOTE: like the Gradle reader, this does NOT consult the environment. Env-vs-file precedence is a
# DEPLOY-layer decision owned by deployment/_shared/project_config.rb (env wins) — a reader of the
# already-derived bridge must not re-litigate it, or the layers disagree again.

# fp_file <repo_root> — echo the path to the bridge, or return 1 when there is none.
# Order: caller override ($FORK_PROPERTIES, lets a test point at a fixture) → the derived bridge →
# the committed schema. The .template fallback is why a fresh CI clone still resolves SOMETHING;
# callers that need real values must check for placeholders themselves.
fp_file() {
  local root="${1:-.}"
  if [ -n "${FORK_PROPERTIES:-}" ] && [ -f "$FORK_PROPERTIES" ]; then echo "$FORK_PROPERTIES"; return 0; fi
  if [ -f "$root/gradle/fork.properties" ]; then echo "$root/gradle/fork.properties"; return 0; fi
  if [ -f "$root/gradle/fork.properties.template" ]; then echo "$root/gradle/fork.properties.template"; return 0; fi
  return 1
}

# fp_get <key> — read one key from $FORK_PROPERTIES, trimming inline `# comment` + whitespace.
# Returns 1 (and echoes nothing) when the file is unset/absent, so `$(fp_get k)` is safely empty.
fp_get() {
  [ -f "${FORK_PROPERTIES:-}" ] || return 1
  grep -E "^$1=" "$FORK_PROPERTIES" 2>/dev/null | head -1 | cut -d= -f2- | sed 's/[[:space:]]*#.*$//; s/[[:space:]]*$//'
}
