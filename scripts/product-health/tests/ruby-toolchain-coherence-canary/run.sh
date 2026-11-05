#!/usr/bin/env bash
# run.sh — RED/GREEN canary for checks/ruby-toolchain-coherence.sh (RT-1…RT-8).
#
# The gate exists because the Ruby toolchain drifts in ways that produce MISLEADING signals rather
# than clean failures: an unshimmed PATH silently yields /usr/bin/ruby 2.6 and `bundle exec` dies
# inside rubygems; a second bundler app root quietly materializes its own 135M copy of the same gems
# and falls a fastlane version behind. A gate that cannot FAIL is decoration, so this proves each
# direction against a fixture tree.
#
# Why the fixtures are copied into a throwaway git repo: RT-5/RT-6 assert the two .bundle/config
# files are TRACKED (untracked, they fix only the machine that wrote them), and RT-4 uses `git grep`.
# Run in place, `git -C <fixture>` resolves to THIS repository and answers about the wrong tree. A
# `git init` + `git add -A` per leg makes the tracked-state assertions mean what they say.
#
# Why the green leg is stamped with the live Ruby: RT-8 compares the running interpreter against
# .ruby-version. A static 3.3.6 fixture would warn (exit 2) on any machine or runner whose Ruby
# differs — the canary would report a fixture mismatch as a gate failure. Stamping keeps the green
# leg about coherence, which is what it exists to prove. The red legs need no stamping: they exit 1
# on their own rule, and a hard failure outranks RT-8's warn.
set -uo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CHECK="$(cd "$HERE/../../checks" && pwd)/ruby-toolchain-coherence.sh"
rc_ok=0
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

stage() { # <fixture> → prints the staged tree path
  local fx="$1" dst="$TMP/$fx"
  cp -R "$HERE/$fx" "$dst"
  git -C "$dst" init -q 2>/dev/null
  git -C "$dst" add -A 2>/dev/null
  printf '%s' "$dst"
}

# Align the green fixture with whatever Ruby is actually running (see header).
stamp_live_ruby() {
  local d="$1" live series
  command -v ruby >/dev/null 2>&1 || return 0
  live="$(ruby -e 'print RUBY_VERSION' 2>/dev/null)" || return 0
  [ -n "$live" ] || return 0
  series="${live%.*}"
  printf '%s\n' "$live" > "$d/.ruby-version"
  sed -i.bak -E "s/^ruby '~> .*'$/ruby '~> ${series}'/" "$d/Gemfile" && rm -f "$d/Gemfile.bak"
  sed -i.bak -E "s/^   ruby .*$/   ruby ${live}/"        "$d/Gemfile.lock" && rm -f "$d/Gemfile.lock.bak"
  git -C "$d" add -A 2>/dev/null
}

# $4 (optional) = a string the output must contain. Exit code alone cannot distinguish "the rule
# fired" from "the check broke for another reason" — and a check whose scan silently yields nothing
# would still let a RED fixture exit non-zero on some other rule (CI-1 of self-test-canaries.sh).
cell() { # <fixture> <expected-exit> <label> [expected-substring]
  local fx="$1" exp="$2" lbl="$3" want="${4:-}" dir out rc
  dir="$(stage "$fx")"
  [ "$fx" = "green" ] && stamp_live_ruby "$dir"
  out="$(HEALTH_ROOT="$dir" bash "$CHECK" 2>&1)"; rc=$?
  if [ "$rc" = "$exp" ] && { [ -z "$want" ] || printf '%s' "$out" | grep -q "$want"; }; then
    echo "   ✅ $lbl → exit $rc${want:+ (found: $want)}"
  else
    echo "   ❌ $lbl → exit $rc (expected $exp)"; printf '%s\n' "$out" | sed 's/^/        /'; rc_ok=1
  fi
}

echo "── ruby toolchain coherence (ruby-toolchain-coherence.sh) ──"
cell red-version-drift 1 "deployment/.ruby-version drifted → FAIL" "RT-2"
cell red-root-path     1 "root BUNDLE_PATH off the shared tree → FAIL" "RT-6"
cell red-cwd           1 "release workflow fastlane_cwd not deployment → FAIL" "RT-7"
cell red-cwd-missing   1 "a SECOND caller omits fastlane_cwd → FAIL" "passes no"
cell green             0 "one version, both app roots, CI agree → PASS" "coherent"
exit "$rc_ok"
