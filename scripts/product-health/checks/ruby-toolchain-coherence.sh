#!/usr/bin/env bash
# checks/ruby-toolchain-coherence.sh — ONE Ruby version, declared once, reachable everywhere.
#
# The trap this guards (hit 2026-09-09): `ruby -c deployment/_shared/project_config.rb` reported a
# syntax error that looked real but was not. The file uses Ruby 3.0+ endless-method syntax
# (`def self.x = VALUE`); the interpreter that parsed it was Apple's SYSTEM ruby at /usr/bin/ruby
# (2.6.10), because rbenv's shims were not on PATH in that shell. Under the project's own ruby the
# same file is "Syntax OK". Nothing in the repo was wrong — but the misleading signal cost real time,
# so the coherence that makes it diagnosable is now asserted.
#
#   RT-1  .ruby-version agrees with Gemfile.lock's RUBY VERSION.
#   RT-2  deployment/.ruby-version resolves to the same version as the root one (it is a symlink, so
#         it cannot drift — matching how deployment/Gemfile{,.lock} already symlink to the root).
#   RT-3  the Gemfile `ruby '~> X.Y'` constraint is satisfied by .ruby-version.
#   RT-5  deployment/ (the second bundler app root, needed because fastlane resolves lanes from a
#         `fastlane/` subdir of cwd) points BUNDLE_PATH at the ROOT vendor tree, and that config is
#         TRACKED. BUNDLE_PATH is relative to each app root, so without this each root materializes
#         its own 135M copy of the same gems — and they drift: a root-only `bundle install` leaves
#         deployment/ on the previous fastlane. A gitignored config would fix only one machine.
#   RT-4  no tracked Ruby script hardcodes a `#!/usr/bin/ruby` shebang — that bypasses rbenv/bundler
#         and re-introduces the system-2.6 interpreter. Use `#!/usr/bin/env ruby`.
#   RT-6  the ROOT bundler app root pins BUNDLE_PATH at vendor/bundle, via a TRACKED config — the
#         twin of RT-5. Without it a fresh clone gets no `path` at the root at all (proven: `git
#         archive HEAD` + `bundle config get path` → "You have not configured a value for `path`"),
#         so a root `bundle install` scatters gems into the global gem dir while deployment/ fills
#         ../vendor/bundle: the same two-copies drift, relocated. RT-5 alone does not catch it.
#   RT-7  the CI contract. ruby/setup-ruby with `bundler-cache: true` runs
#         `bundle config set --local path <cwd>/vendor/bundle` (bundler.js: `path.join(process.cwd(),
#         'vendor/bundle')`) and OVERWRITES whichever app-root config it runs in — so RT-5/RT-6's
#         values are ignored in CI by design. What actually matters there is that setup-ruby's
#         `working-directory` lands on the app root that owns the lanes: EVERY workflow calling a
#         publish-*-kmp / release-multi-platform reusable workflow must pass `fastlane_cwd:
#         deployment`. Left at its default '.', fastlane resolves against the ROOT Fastfile — whose
#         only lane is `ios build_ios` — and the job dies with "Could not find lane" after a full
#         build. Checked across ALL callers, because a repo accumulates them.
#   RT-9  no tracked script invokes bundler DIRECTLY — every caller goes through scripts/ruby-exec.sh.
#         RT-4 bans the `#!/usr/bin/ruby` shebang for bypassing rbenv; a bare `bundle exec` bypasses it
#         exactly the same way, just one level up: `bundle` on PATH belongs to whichever ruby owns it,
#         which on a shell without rbenv's shims is the system 2.6.10. ruby-exec.sh resolves the pinned
#         interpreter from .ruby-version DIRECTLY (via `$(rbenv root)/versions/<pin>/bin/ruby`), so it
#         works whether or not shims are on PATH — that is the whole point, and a caller that skips it
#         re-opens the trap for everyone.
#
#   RT-8  (WARN, non-blocking) THE REPO can reach its pinned interpreter — asked of the repo, not of
#         whatever shell happens to be invoking the suite.
#
#         It used to ask "is the ruby on PATH the pinned one?", which is a question about the
#         OPERATOR'S SHELL. Its own message conceded as much ("Nothing is wrong in the repo"), and it
#         warned on every terminal that had not run `eval "$(rbenv init -)"`, every CI runner, and
#         every fresh shell — a permanent, unactionable warning, and that is the kind that teaches
#         people to skim past the warn column.
#
#         Two things closed the gap it was standing in for. `scripts/ruby-exec.sh` resolves
#         $(rbenv root)/versions/<pin>/bin/ruby DIRECTLY, so the repo reaches its interpreter whether
#         or not shims are on PATH; and RT-9 (below) forbids any tracked *.sh from invoking bundler
#         directly, so no script in this repo can fall into the trap at all.
#
#         What survives is narrower and real: the deployment docs tell a HUMAN to run a bundler lane
#         by hand, and in a shim-less shell that still dies inside rubygems' activate_bin_path with
#         an error naming gems rather than the interpreter. So a PATH mismatch is now an
#         informational hint next to a PASS, and the WARN is reserved for what is genuinely the
#         repo's problem: the resolver cannot reach the pinned version at all (no rbenv, or the pin
#         is not installed).
#
# exit 0 = PASS · 1 = FAIL (blocks) · 2 = WARN. No .ruby-version → PASS (no Ruby toolchain declared).
set -uo pipefail
# shellcheck source=scripts/product-health/lib.sh
source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib.sh"
: "${HEALTH_ROOT:?ruby-toolchain-coherence: HEALTH_ROOT not set (run via product-health.sh)}"

RV="$HEALTH_ROOT/.ruby-version"
[ -f "$RV" ] || { echo "no .ruby-version — project declares no Ruby toolchain (ok)"; exit 0; }

fail=0
want="$(tr -d '[:space:]' < "$RV")"

# RT-1 — .ruby-version vs Gemfile.lock RUBY VERSION.
LOCK="$HEALTH_ROOT/Gemfile.lock"
if [ -f "$LOCK" ]; then
  locked="$(grep -A1 '^RUBY VERSION' "$LOCK" | tail -1 | tr -d '[:space:]' | sed 's/^ruby//')"
  if [ -n "$locked" ] && [ "$locked" != "$want" ]; then
    echo "${C_RED}✗ RT-1${C_RST}: .ruby-version ($want) != Gemfile.lock RUBY VERSION ($locked)"
    fail=1
  fi
fi

# RT-2 — deployment/.ruby-version must resolve to the same version.
DRV="$HEALTH_ROOT/deployment/.ruby-version"
if [ -e "$DRV" ]; then
  got="$(tr -d '[:space:]' < "$DRV")"
  if [ "$got" != "$want" ]; then
    echo "${C_RED}✗ RT-2${C_RST}: deployment/.ruby-version ($got) != root .ruby-version ($want)"
    echo "       Make it a symlink so it cannot drift:  ln -sf ../.ruby-version deployment/.ruby-version"
    fail=1
  fi
fi

# RT-3 — Gemfile constraint must admit .ruby-version.
GF="$HEALTH_ROOT/Gemfile"
if [ -f "$GF" ]; then
  con="$(grep -oE "^ruby +'[^']+'" "$GF" | head -1 | sed -E "s/^ruby +'(.*)'$/\1/")"
  if [ -n "$con" ]; then
    series="$(printf '%s' "$con" | sed -E 's/^~> *//' )"
    case "$want" in
      "$series"|"$series".*) : ;;
      *) echo "${C_RED}✗ RT-3${C_RST}: Gemfile requires ruby '$con' but .ruby-version is $want"; fail=1 ;;
    esac
  fi
fi

# RT-4 — no hardcoded system-ruby shebang in tracked scripts (the /usr/bin/ruby 2.6 trap).
bad="$(git -C "$HEALTH_ROOT" grep -l '^#!/usr/bin/ruby' -- '*.rb' 2>/dev/null || true)"
if [ -n "$bad" ]; then
  echo "${C_RED}✗ RT-4${C_RST}: script(s) hardcode '#!/usr/bin/ruby' (bypasses rbenv → system 2.6):"
  printf '%s\n' "$bad" | sed 's/^/       /'
  echo "       Use '#!/usr/bin/env ruby' so the rbenv shim / bundler context wins."
  fail=1
fi

# RT-5 — deployment bundler app root must share the repo-root vendor tree, via a TRACKED config.
DBC="$HEALTH_ROOT/deployment/.bundle/config"
if [ -d "$HEALTH_ROOT/deployment/fastlane" ]; then
  if [ ! -f "$DBC" ]; then
    echo "${C_RED}✗ RT-5${C_RST}: deployment/ is a bundler app root but has no .bundle/config —"
    echo "       it will materialize its own vendor/bundle (duplicate gems that drift). Add:"
    echo "         BUNDLE_PATH: \"../vendor/bundle\""
    fail=1
  elif ! grep -qE '^BUNDLE_PATH:.*\.\./vendor/bundle' "$DBC"; then
    echo "${C_RED}✗ RT-5${C_RST}: deployment/.bundle/config does not point BUNDLE_PATH at ../vendor/bundle:"
    grep -E '^BUNDLE_PATH:' "$DBC" | sed 's/^/       /'
    fail=1
  elif [ "$(git -C "$HEALTH_ROOT" ls-files deployment/.bundle/config | wc -l | tr -d ' ')" -eq 0 ]; then
    echo "${C_RED}✗ RT-5${C_RST}: deployment/.bundle/config is UNTRACKED — it would fix only this machine."
    fail=1
  fi
fi

# RT-6 — the ROOT bundler app root must pin the shared vendor tree, via a TRACKED config.
RBC="$HEALTH_ROOT/.bundle/config"
if [ -f "$HEALTH_ROOT/Gemfile" ]; then
  if [ ! -f "$RBC" ]; then
    echo "${C_RED}✗ RT-6${C_RST}: repo root is a bundler app root but has no .bundle/config —"
    echo "       a fresh clone resolves no 'path' at all and installs to the global gem dir. Add:"
    echo "         BUNDLE_PATH: \"vendor/bundle\""
    fail=1
  elif ! grep -qE '^BUNDLE_PATH:[[:space:]]*"?vendor/bundle"?[[:space:]]*$' "$RBC"; then
    echo "${C_RED}✗ RT-6${C_RST}: root .bundle/config does not point BUNDLE_PATH at vendor/bundle:"
    grep -E '^BUNDLE_PATH:' "$RBC" | sed 's/^/       /'
    fail=1
  elif [ "$(git -C "$HEALTH_ROOT" ls-files .bundle/config | wc -l | tr -d ' ')" -eq 0 ]; then
    echo "${C_RED}✗ RT-6${C_RST}: root .bundle/config is UNTRACKED — it would fix only this machine."
    echo "       .gitignore needs the dir un-excluded first:  !/.bundle/ ; /.bundle/* ; !/.bundle/config"
    fail=1
  fi
fi

# RT-7 — CI contract, checked across EVERY caller (not one hand-picked file).
#
# setup-ruby's `working-directory` is driven by whatever the caller passes as fastlane_cwd, so the
# contract lives in the CALLERS — and a repo accumulates them (an orchestrator, a local variant, a
# single-platform bypass). Checking one file only ever finds the one you were already looking at:
# release-android-only.yml sat here passing no fastlane_cwd at all, which resolves
# `fastlane android deployInternal` against the ROOT Fastfile — whose only lane is `ios build_ios`.
# It has never been dispatched, so nothing ever reported it.
#
# Scope: callers of the fastlane-invoking reusable workflows (publish-*-kmp, release-multi-platform).
# Deliberately NOT every actionhub caller — pr-check / tag-* / status / cache-cleanup run no
# fastlane, and rollback-v2 accepts no fastlane_cwd input at all (tracked upstream instead).
if [ -d "$HEALTH_ROOT/deployment/fastlane" ] && [ -d "$HEALTH_ROOT/.github/workflows" ]; then
  for wf in "$HEALTH_ROOT"/.github/workflows/*.yml "$HEALTH_ROOT"/.github/workflows/*.yaml; do
    [ -f "$wf" ] || continue
    grep -qE 'uses:.*(publish-(android|apple|desktop|web)-kmp|release-multi-platform)' "$wf" || continue
    cwd_val="$(grep -oE '^[[:space:]]*fastlane_cwd:[[:space:]]*[^[:space:]#]+' "$wf" | head -1 \
               | sed -E 's/.*fastlane_cwd:[[:space:]]*//; s/^["'"'"']//; s/["'"'"']$//')"
    if [ -z "$cwd_val" ]; then
      echo "${C_RED}✗ RT-7${C_RST}: $(basename "$wf") calls a fastlane publish workflow but passes no"
      echo "       fastlane_cwd — it defaults to '.', where the only lane is 'ios build_ios'."
      echo "       Add:  fastlane_cwd: deployment"
      fail=1
    elif [ "$cwd_val" != "deployment" ]; then
      echo "${C_RED}✗ RT-7${C_RST}: $(basename "$wf") passes fastlane_cwd: $cwd_val, but the lanes live"
      echo "       in deployment/fastlane. Expected 'deployment'."
      fail=1
    fi
  done
fi

# RT-8 — can THIS REPO reach its pinned interpreter? (not: is PATH's ruby the pinned one)
warn=0
# RT8_RESOLVER lets the canary point at an absent/broken resolver while the rest of the tree stays
# real. A synthetic HEALTH_ROOT cannot isolate this check — RT-6 needs a git repo to decide whether
# .bundle/config is tracked, so it fails in any scratch dir and masks RT-8's own exit code.
RESOLVER="${RT8_RESOLVER:-$HEALTH_ROOT/scripts/ruby-exec.sh}"
if [ -f "$RESOLVER" ]; then
  # Ask the resolver, in a subshell so sourcing cannot leak into this check.
  rt8_report="$(bash -c ". '$RESOLVER' >/dev/null 2>&1; ruby_exec_report" 2>/dev/null || true)"
  rt8_bin="$(printf '%s' "$rt8_report" | sed -n 's/^ruby-exec: \([^ ]*\).*/\1/p')"
  rt8_ver=""
  [ -n "$rt8_bin" ] && [ -x "$rt8_bin" ] && rt8_ver="$("$rt8_bin" -e 'print RUBY_VERSION' 2>/dev/null || true)"

  if [ -z "$rt8_ver" ] || [ "$rt8_ver" != "$want" ]; then
    # The repo's own resolver cannot produce the pinned interpreter. THIS is repo-scoped: every
    # script that goes through ruby-exec.sh is affected, on every machine in this state.
    echo "${C_YEL}⚠ RT-8${C_RST}: scripts/ruby-exec.sh cannot reach the pinned ruby $want."
    echo "       resolver said: ${rt8_report:-<no output>}"
    echo "       Install it:  rbenv install $want    (or set RUBY_EXEC_BIN to a $want interpreter)"
    warn=2
  else
    # Repo is healthy. A PATH mismatch is only a hint, because the deployment docs instruct a human
    # to run a bundler lane directly and that path does NOT go through the resolver.
    live=""
    command -v ruby >/dev/null 2>&1 && live="$(ruby -e 'print RUBY_VERSION' 2>/dev/null || true)"
    if [ -n "$live" ] && [ "$live" != "$want" ]; then
      echo "  note: this shell's ruby is $live ($(command -v ruby)); the repo resolves $want directly,"
      echo "        so every script is unaffected. Only a hand-typed bundler lane would break — run"
      echo "        eval \"\$(rbenv init -)\"  first if you intend to invoke one by hand."
    fi
  fi
elif command -v ruby >/dev/null 2>&1; then
  # No resolver in this tree (a fork that stripped it) — fall back to the old PATH question, the best
  # available signal when nothing shim-independent exists.
  live="$(ruby -e 'print RUBY_VERSION' 2>/dev/null || true)"
  if [ -n "$live" ] && [ "$live" != "$want" ]; then
    echo "${C_YEL}⚠ RT-8${C_RST}: live ruby is $live but .ruby-version declares $want ($(command -v ruby)),"
    echo "       and scripts/ruby-exec.sh is absent so nothing resolves the pin independently."
    echo "       Fix the shell:  eval \"\$(rbenv init -)\"   (or restore scripts/ruby-exec.sh)"
    warn=2
  fi
fi

# ── RT-9 — bundler is reached ONLY through scripts/ruby-exec.sh ────────────────────────────────
# Sibling of RT-4: that bans `#!/usr/bin/ruby` for bypassing rbenv, this bans a bare `bundle` for
# bypassing it one level up. `bundle` on PATH belongs to whichever ruby owns it, so on a shell
# without rbenv shims it is the system 2.6.10 and every gem was installed for the pinned version.
#
# Scope: tracked *.sh only, excluding the resolver itself (it must name `bundle` to call it), this
# checker (it must name the pattern to detect it), and the canary fixtures. ECHOED instructions are
# allowed — a script printing "run: bundle exec fastlane …" for a human is documentation, not an
# invocation, so the match is anchored to a command position.
rt9_offenders=""
while IFS= read -r f; do
  case "$f" in
    scripts/ruby-exec.sh|scripts/product-health/checks/ruby-toolchain-coherence.sh) continue ;;
    scripts/product-health/tests/*) continue ;;
  esac
  # command position: start of line, or after `&&`, `||`, `;`, `(`, or a pipe — never inside echo/printf
  if grep -nE '(^|[;&|(]|&&|\|\|)[[:space:]]*bundle[[:space:]]+(exec|install|update|lock)\b' "$f" 2>/dev/null \
       | grep -qvE '^[0-9]+:[[:space:]]*#'; then
    rt9_offenders="$rt9_offenders $f"
  fi
done < <(git ls-files '*.sh' 2>/dev/null)

if [ -n "$rt9_offenders" ]; then
  echo "${C_RED}✗ RT-9${C_RST}: script(s) invoke bundler directly instead of via scripts/ruby-exec.sh:"
  for f in $rt9_offenders; do
    echo "       $f"
    grep -nE '(^|[;&|(]|&&|\|\|)[[:space:]]*bundle[[:space:]]+(exec|install|update|lock)\b' "$f" \
      | grep -vE '^[0-9]+:[[:space:]]*#' | sed 's/^/         /'
  done
  echo "       A bare \`bundle\` runs under whatever ruby owns it on PATH — the system 2.6.10 on a shell"
  echo "       without rbenv shims — while the gems were installed for \$(cat .ruby-version). Use:"
  echo "         . \"\$REPO_ROOT/scripts/ruby-exec.sh\"  &&  ruby_bundle <app-root> -- exec <cmd>"
  fail=1
fi

if [ "$fail" -ne 0 ]; then exit "$fail"; fi
if [ "$warn" -ne 0 ]; then exit "$warn"; fi
echo "ruby toolchain coherent (single version $want; Gemfile.lock, root + deployment app roots, and CI agree)"
exit 0
