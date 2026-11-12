#!/usr/bin/env bash
# Functional test: ruby_exec runs the pinned interpreter on ITS OWN gem directory, never the per-user
# ~/.gem/ruby/<ABI> that every Ruby of one ABI shares. A native gem another 3.3.x built there made the
# pinned rbenv 3.3.12 die with "linked to incompatible libruby" before any script ran.
#
#   G-1 the user gem dir is NOT on the gem path ruby_exec gives the interpreter
#   G-2 RUBY_EXEC_KEEP_GEM_PATH=1 leaves the caller's gem path alone (opt-out works)
set -uo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$HERE/../.." && pwd)"
WORK="$(mktemp -d)"; trap 'rm -rf "$WORK"' EXIT
fail=0; pass() { echo "  ✅ $1"; }; bad() { echo "  ❌ $1"; fail=1; }

# A HOME whose user gem dir exists — that is what puts it on RubyGems' default path.
abi="$(bash -c ". '$REPO/scripts/ruby-exec.sh'; ruby_exec -e 'print RbConfig::CONFIG[\"ruby_version\"]'" 2>/dev/null)"
[ -n "$abi" ] || { echo "  ⊘ skip — no resolvable ruby"; exit 0; }
mkdir -p "$WORK/home/.gem/ruby/$abi"
# RUBY_EXEC_NATIVE_OK=1: the fake HOME must never reach the native-extension heal — a rebuild there links
# the shared vendor/bundle gem against a libruby under the temp HOME and breaks every later run.
paths() { HOME="$WORK/home" RUBY_EXEC_NATIVE_OK=1 bash -c ". '$REPO/scripts/ruby-exec.sh'; ruby_exec -e 'puts Gem.path'" 2>/dev/null; }

paths | grep -q "$WORK/home/.gem" && bad "G-1 user gem dir is on the pinned interpreter's gem path" \
                                  || pass "G-1 user gem dir excluded"
RUBY_EXEC_KEEP_GEM_PATH=1 paths | grep -q "$WORK/home/.gem" && pass "G-2 opt-out keeps the caller's gem path" \
                                  || bad "G-2 RUBY_EXEC_KEEP_GEM_PATH=1 did not keep the user gem dir"

# G-3 the heal rebuilds an extension another Ruby compiled (gem_make.out names it) in an owned tree, and
#     leaves one the pinned interpreter built alone. A stub interpreter records what it was asked to do.
T="$WORK/gems"; mkdir -p "$T/extensions/p/3.3.0/foo-1.2.3" "$T/extensions/p/3.3.0/ok-0.1"
printf '/other/3.3.6/bin/ruby extconf.rb\n' > "$T/extensions/p/3.3.0/foo-1.2.3/gem_make.out"
STUB="$WORK/bin/ruby"; mkdir -p "$WORK/bin"
printf '#!/bin/sh\necho "$*" >> "%s/calls"\n' "$WORK" > "$STUB"; chmod +x "$STUB"
printf '%s extconf.rb\n' "$STUB" > "$T/extensions/p/3.3.0/ok-0.1/gem_make.out"
bash -c ". '$REPO/scripts/ruby-exec.sh'; RUBY_EXEC_RESOLVED='$STUB'; RUBY_EXEC_GEM_PATH='$T'; _ruby_exec_heal_native" 2>/dev/null
grep -q -- '-S gem pristine foo -v 1.2.3 --extensions' "$WORK/calls" 2>/dev/null \
  && ! grep -q 'pristine ok' "$WORK/calls" \
  && pass "G-3 mismatched extension rebuilt, matching one left alone" \
  || bad "G-3 heal calls: $(cat "$WORK/calls" 2>/dev/null)"

[ "$fail" = 0 ] && echo "ruby-exec-gem-isolation-test: PASS" || echo "ruby-exec-gem-isolation-test: FAIL"
exit "$fail"
