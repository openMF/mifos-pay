#!/usr/bin/env bash
# kotlin-checks.sh pre-commit|pre-push|files <paths…>|changed [<base>]|module <dir…>|all [--fix]
# — the git hooks' checks, scoped to what the commit or push CHANGES, plus on-demand CI-parity runs.
# The hooks (pre-commit.sh / pre-push.sh) only call this, so a template sync updates the checks without
# reinstalling hooks. The style rules themselves live in kotlin-conform.sh.
#
# Why scoped and Gradle-free: the old hooks ran `./gradlew spotlessCheck detekt dependencyGuard` over
# the whole repo on every commit AND again on every push — 25+ min on a fork with ~40 feature modules,
# and a fork's pre-existing Detekt findings blocked every push no matter what it touched.
#   pre-commit  formats the staged .kt/.kts/.xml files (licence header + ktlint), re-stages them, and
#               fails on anything left — ktlint findings it cannot fix, Detekt findings
#   pre-push    checks (does not change) the .kt/.kts/.xml files the pushed commits change;
#               dependencyGuard runs only when a build file changed (warn-only: a local `.local=true`
#               lib-integrate makes it differ from the committed baselines; CI enforces it)
#   files       formats + checks exactly the given paths (the sync engine's 3-way merge results)
#
# On-demand — CI's `spotlessCheck detekt` in seconds instead of ~13 min (check-only; --fix applies):
#   changed [<base>]  what this branch changes vs <base> (default: origin/HEAD's branch, else origin/dev),
#                     committed or not — the files a PR's checks will see. ~4 s for a 30-file PR.
#   module <dir…>     every file under the given module directories (e.g. feature/onboarding)
#   all               the whole repo — ~35 s for 2,400 files. Scope mirrors Gradle (see kotlin-conform.sh)
# None of these touch Gradle; dependencyGuard is printed as a reminder when build files changed.
# A tree that passed is remembered (`.git/kpt-checked-trees`), so a push of commits the pre-commit hook
# already checked costs nothing.
set -uo pipefail
stage="${1:?usage: kotlin-checks.sh pre-commit|pre-push|files <paths…>|changed [<base>]|module <dir…>|all [--fix]}"; shift
cd "$(git rev-parse --show-toplevel)" || exit 1
CACHE="$(git rev-parse --git-path kpt-checked-trees)"
CONFORM="scripts/ci/kotlin-conform.sh"
ZERO=0000000000000000000000000000000000000000

# kotlin_files — the existing style-checked files (.kt/.kts/.xml) among stdin's paths, fixtures and build
# output excluded. kotlin-conform.sh then narrows to what Gradle's Spotless/Detekt actually cover.
kotlin_files() {
  grep -E '\.(kts?|xml)$' | grep -vE '(^|/)(build|tests)/' | while IFS= read -r f; do [ -f "$f" ] && echo "$f"; done
}

# On-demand CI-parity modes — no branch guard (they never commit), check-only unless --fix.
case "$stage" in changed|module|all)
  fix=0; args=(); for a in "$@"; do if [ "$a" = --fix ]; then fix=1; else args+=("$a"); fi; done
  case "$stage" in
    changed)
      base="${args[0]:-}"
      if [ -z "$base" ]; then
        base="$(git symbolic-ref -q --short refs/remotes/origin/HEAD 2>/dev/null || true)"
        [ -n "$base" ] || base=origin/dev
      fi
      git rev-parse -q --verify "$base^{commit}" >/dev/null || { echo "kotlin-checks: unknown base '$base'" >&2; exit 2; }
      mb="$(git merge-base "$base" HEAD)"
      list="$( { git diff --name-only --diff-filter=ACMR "$mb"; git ls-files --others --exclude-standard; } | sort -u)" ;;
    module)
      [ "${#args[@]}" -gt 0 ] || { echo "usage: kotlin-checks.sh module <dir…>" >&2; exit 2; }
      list="$(git ls-files --cached --others --exclude-standard -- "${args[@]}")" ;;
    all) list="$(git ls-files --cached --others --exclude-standard)" ;;
  esac
  files=(); while IFS= read -r f; do files+=("$f"); done < <(printf '%s\n' "$list" | kotlin_files)
  [ "${#files[@]}" -gt 0 ] || { echo "✓ nothing to check ($stage)"; exit 0; }
  start=$SECONDS
  echo "▶ Spotless + Detekt on ${#files[@]} file(s) ($stage$([ "$fix" = 1 ] && echo ', fixing'))"
  if [ "$fix" = 1 ]; then bash "$CONFORM" "${files[@]}"; else bash "$CONFORM" --check "${files[@]}"; fi
  rc=$?
  if [ "$stage" = changed ] && printf '%s\n' "$list" | grep -qE '\.kts$|^gradle/libs\.versions\.toml$|/dependencies/'; then
    echo "ℹ build files changed — CI also runs dependencyGuard: ./gradlew dependencyGuard (refresh: dependencyGuardBaseline)"
  fi
  [ "$rc" = 0 ] && echo "✓ clean in $((SECONDS - start)) s" || echo "✗ findings above ($((SECONDS - start)) s)$([ "$fix" = 0 ] && echo ' — rerun with --fix to apply the fixable ones')" >&2
  exit "$rc" ;;
esac

branch="$(git rev-parse --abbrev-ref HEAD)"
if [ "$branch" = master ] || [ "$branch" = dev ]; then
  echo "🛑 On '$branch' — commit and push from a feature branch." >&2
  exit 1
fi

# checked <tree> — this tree already passed (pre-commit or a previous push).
checked() { [ -f "$CACHE" ] && grep -qxF -- "$1" "$CACHE"; }
# remember <tree> — record a tree that passed.
remember() { echo "$1" >> "$CACHE"; }


if [ "$stage" = files ]; then
  files=(); while IFS= read -r f; do files+=("$f"); done < <(printf '%s\n' "$@" | kotlin_files)
  [ "${#files[@]}" -gt 0 ] || exit 0
  bash "$CONFORM" "${files[@]}"
  exit $?
fi

if [ "$stage" = pre-commit ]; then
  tree="$(git write-tree)"
  checked "$tree" && exit 0
  files=(); while IFS= read -r f; do files+=("$f"); done < <(git diff --cached --name-only --diff-filter=ACMR | kotlin_files)
  if [ "${#files[@]}" -gt 0 ]; then
    echo "▶ Spotless + Detekt on ${#files[@]} staged Kotlin file(s)"
    bash "$CONFORM" "${files[@]}"; rc=$?
    git add -- "${files[@]}"
    [ "$rc" = 0 ] || { echo "💥 Fix the findings above, then commit again." >&2; exit 1; }
  fi
  remember "$(git write-tree)"
  exit 0
fi

# pre-push: stdin carries `<local ref> <local sha> <remote ref> <remote sha>` per pushed ref.
rc=0
while read -r _lref lsha _rref rsha; do
  [ "$lsha" = "$ZERO" ] && continue                                   # deleting a remote ref
  tree="$(git rev-parse "$lsha^{tree}")"
  checked "$tree" && continue
  if [ "$rsha" != "$ZERO" ] && git cat-file -e "$rsha^{commit}" 2>/dev/null; then base="$rsha"
  else base="$(git merge-base "$lsha" refs/remotes/origin/HEAD 2>/dev/null || git rev-list --max-parents=0 "$lsha" | tail -1)"; fi
  changed="$(git diff --name-only --diff-filter=ACMR "$base" "$lsha")"
  files=(); while IFS= read -r f; do files+=("$f"); done < <(printf '%s\n' "$changed" | kotlin_files)
  if [ "${#files[@]}" -gt 0 ]; then
    echo "▶ Spotless + Detekt on ${#files[@]} pushed Kotlin file(s)"
    bash "$CONFORM" --check "${files[@]}" || { echo "💥 Fix the findings above (scripts/ci/kotlin-conform.sh <files> formats), then push again." >&2; rc=1; continue; }
  fi
  if printf '%s\n' "$changed" | grep -qE '\.kts$|^gradle/libs\.versions\.toml$|/dependencies/'; then
    ./gradlew dependencyGuard --daemon -q \
      || echo "⚠ dependencyGuard differs from the committed baselines — CI enforces it; refresh with ./gradlew dependencyGuardBaseline (lib-integrate .local=false) if intended." >&2
  fi
  remember "$tree"
done
exit "$rc"
