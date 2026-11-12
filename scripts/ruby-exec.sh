#!/usr/bin/env bash
# ruby-exec.sh — the ONE way this repo reaches Ruby/Bundler. Source it; do not reimplement it.
#
# THE TRAP THIS CLOSES
# rbenv works by PATH interception: it puts shims ahead of the system ruby, and each shim reads
# `.ruby-version` to pick a version. If `~/.rbenv/shims` is NOT on PATH — a shell without
# `eval "$(rbenv init -)"`, a cron job, a GUI-launched terminal, a CI step that reset PATH — then
# nothing intercepts, `ruby` falls through to macOS's /usr/bin/ruby 2.6.10, and the pinned interpreter
# is simply not used. rbenv is installed and inert.
#
# The failure that produces is actively misleading. The Gemfile's gems were installed for the pinned
# ruby, so `bundle exec` under 2.6.10 dies inside rubygems' `activate_bin_path` with an error about
# GEM ACTIVATION. Every instinct says "the gems are wrong"; the gems are fine and the interpreter is
# wrong. RT-8 in ruby-toolchain-coherence.sh exists to name that, and this file exists so scripts do
# not hit it in the first place.
#
# Resolution order — first hit wins, most-specific first:
#   1. RUBY_EXEC_BIN            explicit override (CI, or a human debugging)
#   2. framework provisioner    core/scripts/ruby-toolchain-ensure.sh, when this fork lives inside
#                               the framework tree — it INSTALLS the pinned ruby + bundler if absent
#   3. rbenv version dir        $(rbenv root)/versions/<pinned>/bin/ruby — used DIRECTLY, so it works
#                               whether or not shims are on PATH. This is the case that fixes the trap.
#   4. provision                pinned version absent → framework helper installs it, or
#                               RUBY_EXEC_AUTO_INSTALL=1 runs `rbenv install`. Otherwise FAIL LOUDLY
#                               with the exact command — a wrong interpreter is worse than a stop.
#   5. PATH ruby                only when there is no rbenv at all; warns if it misses the pin
#
# The pinned version is read from `.ruby-version` — the SoT that RT-1 keeps in step with
# Gemfile.lock's `RUBY VERSION`. It is deliberately NOT a literal in this file: a hardcoded default
# silently stops tracking the pin the moment someone bumps `.ruby-version`, which is how
# doctor.sh's `RBENV_VERSION="${RBENV_VERSION:-3.3.6}"` could have gone stale.
#
# Usage:
#   . "$REPO_ROOT/scripts/ruby-exec.sh"
#   ruby_exec  <script.rb> [args...]              # run a ruby script under the pinned interpreter
#   ruby_bundle <app-root> -- <cmd> [args...]     # run `bundle <cmd>` in an app root, pinned
#   ruby_exec_report                              # one line: which interpreter was chosen and why

# Guard against double-sourcing — this file defines functions only, it runs nothing on load.
[ -n "${RUBY_EXEC_SH_LOADED:-}" ] && return 0 2>/dev/null || true
RUBY_EXEC_SH_LOADED=1

_ruby_exec_repo_root() {
  # scripts/ruby-exec.sh → repo root is one level up from scripts/.
  #
  # BASH_SOURCE is UNSET when this file is sourced from zsh — the macOS default shell, and exactly
  # what scripts/CLAUDE.md tells a human to do interactively (`. scripts/ruby-exec.sh && ruby_exec_report`).
  # `dirname ""` is `.`, so the root resolved to the PARENT OF THE CWD, `.ruby-version` was not found,
  # and ruby_exec_report printed `pinned=` empty — a diagnostic quietly reporting no pin on a repo
  # that has one. Never guess a root: derive it if we can, then VERIFY it by marker, then search.
  local d=""
  # shellcheck disable=SC2128
  if [ -n "${BASH_SOURCE:-}" ]; then d="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." 2>/dev/null && pwd)"; fi
  [ -n "$d" ] || { [ -n "${ZSH_VERSION:-}" ] && [ -n "${0:-}" ] && d="$(cd "$(dirname "$0")/.." 2>/dev/null && pwd)"; }
  # A derived root only counts if it actually looks like this repo — otherwise fall through.
  if [ -n "$d" ] && { [ -f "$d/.ruby-version" ] || [ -f "$d/Gemfile" ]; }; then printf '%s' "$d"; return 0; fi
  # Last resort: walk up from the CWD for the marker. Works from any subdirectory and any shell.
  d="$PWD"; local i=0
  while [ "$d" != "/" ] && [ $i -lt 8 ]; do
    if [ -f "$d/.ruby-version" ] || [ -f "$d/Gemfile" ]; then printf '%s' "$d"; return 0; fi
    d="$(dirname "$d")"; i=$((i+1))
  done
  printf '%s' "$PWD"
}

# The pinned version, from the SoT. Empty if .ruby-version is absent (a fork that stripped it).
ruby_exec_pinned_version() {
  local root; root="$(_ruby_exec_repo_root)"
  local vf="$root/.ruby-version"
  [ -f "$vf" ] || return 0
  tr -d '[:space:]' < "$vf"
}

RUBY_EXEC_RESOLVED=""   # absolute interpreter path
RUBY_EXEC_SOURCE=""     # how it was found — surfaced by ruby_exec_report

_ruby_exec_resolve() {
  [ -n "$RUBY_EXEC_RESOLVED" ] && return 0
  local want; want="$(ruby_exec_pinned_version)"

  # 1 — explicit override
  if [ -n "${RUBY_EXEC_BIN:-}" ] && [ -x "${RUBY_EXEC_BIN}" ]; then
    RUBY_EXEC_RESOLVED="$RUBY_EXEC_BIN"; RUBY_EXEC_SOURCE="RUBY_EXEC_BIN override"; return 0
  fi

  # 3 — rbenv version dir, used DIRECTLY. Ahead of the shim on purpose: this path does not care
  #     whether shims are on PATH, which is the entire failure this file exists for.
  if [ -n "$want" ] && command -v rbenv >/dev/null 2>&1; then
    local rr; rr="$(rbenv root 2>/dev/null)"
    if [ -n "$rr" ] && [ -x "$rr/versions/$want/bin/ruby" ]; then
      RUBY_EXEC_RESOLVED="$rr/versions/$want/bin/ruby"
      RUBY_EXEC_SOURCE="rbenv versions/$want (direct — shims not required)"; return 0
    fi
    # The pinned version is NOT installed. Try to provision it before degrading — and NEVER degrade
    # silently, which is what the shim fallback used to do: with an uninstalled pin it reported
    # "rbenv shim" as though resolved, and the shim then failed at RUN time with rbenv's own
    # "version not installed" error, far from the code that chose it.
    if [ -n "$want" ]; then
      local fw; fw="$(_ruby_exec_framework_helper || true)"
      if [ -n "$fw" ]; then
        # The framework provisioner is CHECK-FIRST and idempotent: it runs `rbenv install -s` only
        # when the version is genuinely absent, so calling it here is cheap on a warm machine.
        RBENV_VERSION="$want" bash "$fw" "$(_ruby_exec_repo_root)" >/dev/null 2>&1 || true
        if [ -x "$rr/versions/$want/bin/ruby" ]; then
          RUBY_EXEC_RESOLVED="$rr/versions/$want/bin/ruby"
          RUBY_EXEC_SOURCE="rbenv versions/$want (provisioned by framework helper)"; return 0
        fi
      fi
      if [ "${RUBY_EXEC_AUTO_INSTALL:-0}" = "1" ]; then
        echo "ruby-exec: installing ruby $want (RUBY_EXEC_AUTO_INSTALL=1) — this compiles, expect minutes…" >&2
        rbenv install -s "$want" >&2 || true
        if [ -x "$rr/versions/$want/bin/ruby" ]; then
          RUBY_EXEC_RESOLVED="$rr/versions/$want/bin/ruby"
          RUBY_EXEC_SOURCE="rbenv versions/$want (auto-installed)"; return 0
        fi
      fi
      # Loud, actionable failure beats a wrong interpreter. Auto-installing by default would compile
      # Ruby inside an unrelated script for minutes with no warning, so it is opt-in.
      echo "❌ ruby-exec: .ruby-version pins $want but it is not installed." >&2
      echo "    Install it:   rbenv install $want" >&2
      echo "    Or re-run with RUBY_EXEC_AUTO_INSTALL=1 to install it here (compiles, takes minutes)." >&2
      return 1
    fi
  fi

  # 5 — whatever PATH gives, with a warning when it does not match the pin
  local p; p="$(command -v ruby 2>/dev/null)"
  if [ -n "$p" ]; then
    RUBY_EXEC_RESOLVED="$p"
    local have; have="$("$p" -e 'print RUBY_VERSION' 2>/dev/null)"
    if [ -n "$want" ] && [ "$have" != "$want" ]; then
      RUBY_EXEC_SOURCE="PATH ruby $have — DOES NOT MATCH .ruby-version $want"
      echo "⚠️  ruby-exec: using $p ($have) but .ruby-version pins $want." >&2
      echo "    Install it (rbenv install $want) or fix the shell (eval \"\$(rbenv init -)\")." >&2
    else
      RUBY_EXEC_SOURCE="PATH ruby $have"
    fi
    return 0
  fi
  echo "❌ ruby-exec: no ruby interpreter found at all." >&2
  return 1
}

# The framework provisioner, when this fork lives inside the framework tree. Resolved by SEARCHING
# UPWARD rather than by a fixed number of `../` hops: doctor.sh hardcoded five levels, which resolves
# only at workspaces/{ws}/{proj}/source/{proj}/ and silently misses for a standalone clone — exactly
# the shape an OSS fork has.
_ruby_exec_framework_helper() {
  local d; d="$(_ruby_exec_repo_root)"
  local i=0
  while [ "$d" != "/" ] && [ $i -lt 8 ]; do
    if [ -x "$d/core/scripts/ruby-toolchain-ensure.sh" ]; then
      printf '%s' "$d/core/scripts/ruby-toolchain-ensure.sh"; return 0
    fi
    d="$(dirname "$d")"; i=$((i+1))
  done
  return 1
}

/usr/bin/true   # keep the file harmless if executed rather than sourced

# The resolved interpreter's OWN gem directory. RubyGems also searches the per-user dir
# ~/.gem/ruby/<ABI> — and every Ruby of one ABI shares it, so a native gem a DIFFERENT 3.3.x built there
# (Homebrew's, say) is loaded by the pinned rbenv 3.3.12 and dies with "linked to incompatible
# libruby.3.3.dylib" (json's parser.bundle, 2026-11-05). Pinning GEM_HOME/GEM_PATH to the interpreter's
# own dir — plus this repo's shared bundler tree (vendor/bundle, .bundle/config BUNDLE_PATH), which holds
# the gems built FOR the pinned interpreter (xcodeproj & co. for the pbxproj merger) — drops that user dir.
# A caller that set GEM_HOME/GEM_PATH itself keeps them, and RUBY_EXEC_KEEP_GEM_PATH=1 opts out entirely.
_ruby_exec_gem_dir() {
  [ -n "${RUBY_EXEC_GEM_DIR:-}" ] && return 0
  RUBY_EXEC_GEM_DIR="$("$RUBY_EXEC_RESOLVED" -e 'print Gem.default_dir' 2>/dev/null)"
  local abi vendor; abi="$("$RUBY_EXEC_RESOLVED" -e 'print Gem.ruby_api_version' 2>/dev/null)"
  vendor="$(_ruby_exec_repo_root)/vendor/bundle/ruby/$abi"
  RUBY_EXEC_GEM_PATH="$RUBY_EXEC_GEM_DIR"
  [ -n "$abi" ] && [ -d "$vendor" ] && RUBY_EXEC_GEM_PATH="$RUBY_EXEC_GEM_DIR:$vendor"
}

# Native extensions are keyed by ABI (3.3.0), not by patch version, so vendor/bundle can hold gems
# another 3.3.x compiled ("linked to incompatible …/3.3.6/lib/libruby.3.3.dylib" — 9 of 10 on the machine
# this was written on). RubyGems records the compiling interpreter in each extension's gem_make.out, so
# once per shell rebuild every extension whose builder is not the pinned interpreter — but only inside a
# tree this interpreter owns (its own gem dir or the repo's vendor/bundle). Never in the shared ~/.gem user
# dir: rebuilding there just moves the breakage to the other Ruby.
_ruby_exec_heal_native() {
  [ -n "${RUBY_EXEC_NATIVE_OK:-}" ] && return 0
  RUBY_EXEC_NATIVE_OK=1
  local home out spec ver failed=""
  for home in ${RUBY_EXEC_GEM_PATH//:/ }; do
    for out in "$home"/extensions/*/*/*/gem_make.out; do
      [ -f "$out" ] || continue
      head -3 "$out" | grep -qF -- "$RUBY_EXEC_RESOLVED" && continue
      head -3 "$out" | grep -qE '/bin/ruby' || continue      # unknown format — leave it alone
      spec="$(basename "$(dirname "$out")")"; ver="${spec##*-}"
      echo "note: ruby-exec: rebuilding $spec's extension for the pinned interpreter (${home#"$(_ruby_exec_repo_root)/"})" >&2
      GEM_HOME="$home" GEM_PATH="$RUBY_EXEC_GEM_PATH" "$RUBY_EXEC_RESOLVED" -S gem pristine "${spec%-*}" \
        -v "$ver" --extensions >/dev/null 2>&1 || failed="$failed $spec"
    done
  done
  # Never a silent half-heal: name what could not be rebuilt and the exact repair.
  [ -z "$failed" ] || echo "⚠️  ruby-exec: could not rebuild$failed — run: (cd $(_ruby_exec_repo_root) && bundle pristine)" >&2
}

# Run "$@" with the gem path isolated to the resolved interpreter (see _ruby_exec_gem_dir).
_ruby_exec_isolated() {
  if [ "${RUBY_EXEC_KEEP_GEM_PATH:-0}" = "1" ] || [ -n "${GEM_HOME:-}${GEM_PATH:-}" ]; then "$@"; return; fi
  _ruby_exec_gem_dir
  if [ -n "$RUBY_EXEC_GEM_DIR" ]; then
    _ruby_exec_heal_native
    GEM_HOME="$RUBY_EXEC_GEM_DIR" GEM_PATH="$RUBY_EXEC_GEM_PATH" "$@"
  else
    "$@"
  fi
}

# Run a ruby script under the pinned interpreter.
ruby_exec() {
  _ruby_exec_resolve || return 1
  _ruby_exec_isolated "$RUBY_EXEC_RESOLVED" "$@"
}

# Run bundler in <app-root> under the pinned interpreter.
#   ruby_bundle <app-root> -- exec fastlane ios beta
ruby_bundle() {
  local app_root="$1"; shift
  [ "${1:-}" = "--" ] && shift
  [ -d "$app_root" ] || { echo "❌ ruby-exec: no such bundler app root: $app_root" >&2; return 1; }

  # 2 — prefer the framework provisioner: it INSTALLS the pinned ruby + bundler when absent, which
  #     plain resolution cannot do.
  local fw; fw="$(_ruby_exec_framework_helper || true)"
  if [ -n "$fw" ]; then
    RBENV_VERSION="${RBENV_VERSION:-$(ruby_exec_pinned_version)}" bash "$fw" "$app_root" -- bundle "$@"
    return $?
  fi

  _ruby_exec_resolve || return 1
  # Call bundle through the resolved ruby, not through PATH — `bundle` on PATH belongs to whichever
  # ruby owns it, which is the same trap one level up.
  local bundle_bin; bundle_bin="$(dirname "$RUBY_EXEC_RESOLVED")/bundle"
  if [ -x "$bundle_bin" ]; then
    ( cd "$app_root" && _ruby_exec_isolated "$RUBY_EXEC_RESOLVED" "$bundle_bin" "$@" )
  else
    ( cd "$app_root" && _ruby_exec_isolated bundle "$@" )
  fi
}

# Print which interpreter was chosen and why.
# stdout of the resolver is silenced but stderr is NOT: that is where the actionable
# message lives ("pins X but it is not installed — rbenv install X"), and swallowing it
# left the report saying `none (unresolved)` with no cause and no next step.
ruby_exec_report() {
  # stdout only is silenced. stderr carries the ACTIONABLE message ("pins X but it is not installed
  # — rbenv install X"), and swallowing it left the report saying `none (unresolved)` with no hint of
  # why or what to do — a diagnostic that diagnoses nothing.
  _ruby_exec_resolve >/dev/null || true
  printf 'ruby-exec: %s  (%s)  pinned=%s\n' \
    "${RUBY_EXEC_RESOLVED:-none}" "${RUBY_EXEC_SOURCE:-UNRESOLVED — see the error above}" \
    "$(ruby_exec_pinned_version)"
  [ -n "$RUBY_EXEC_RESOLVED" ]
}
