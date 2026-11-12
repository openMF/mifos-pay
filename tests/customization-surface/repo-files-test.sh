#!/usr/bin/env bash
# Functional test: the repository community files (code of conduct, contributing guide, licence,
# security policy) resolve `owner: fork, strategy: repo-files` under every spelling a repo uses, so a
# sync never writes them, and every file app-profile/app.yaml#repo_files.files exposes is covered.
# (Who may EDIT them is the framework's _org/company.yaml call — tested by its edit-guard canary.)
# Runs the REAL contract and reader.
set -uo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$HERE/../.." && pwd)"
CS="$REPO/scripts/customization-surface.sh"
WORK="$(mktemp -d)"; trap 'rm -rf "$WORK"' EXIT
fail=0; pass() { echo "  ✅ $1"; }; bad() { echo "  ❌ $1"; fail=1; }

# 1. Every spelling resolves to the fork, never the template (a sync must not overwrite them).
for f in CODE_OF_CONDUCT.md CONTRIBUTING.md Contributing.md LICENSE LICENCE LICENSE.md SECURITY.md .github/workflows/cla-check.yml; do
  out="$(bash "$CS" resolve "$f")"
  case "$out" in
    *"fork  (strategy: repo-files)") pass "$f → fork, repo-files" ;;
    *) bad "$f → '$out' (expected fork, strategy: repo-files)" ;;
  esac
done

# 2. Neighbours that merely LOOK similar are not swept in.
for f in README.md .github/workflows/pr-check.yml; do
  case "$(bash "$CS" resolve "$f")" in
    *repo-files*) bad "$f picked up the repo-files strategy" ;;
    *) pass "$f is not a repo-files path" ;;
  esac
done

# 1b. Every file app.yaml EXPOSES in repo_files.files resolves repo-files (the two lists cannot drift).
listed=$(awk '/^repo_files:/{b=1;next} b&&/^[^[:space:]#]/{b=0} b&&/^[[:space:]]+files:/{f=1;next} b&&f&&/^[[:space:]]+[A-Za-z_.\/-]+:[[:space:]]*\{/{k=$1;sub(/:$/,"",k);printf "%s ",k}' "$REPO/app-profile/app.yaml")
[ -n "$listed" ] && pass "app.yaml exposes: $listed" || bad "app.yaml#repo_files.files is missing"
for f in $listed; do
  grep -qE "^[[:space:]]+$f:[[:space:]]*\{[^}]*access:[[:space:]]*read" "$REPO/app-profile/app.yaml" \
    && pass "the template ships $f access: read" || bad "$f is not access: read in the template's app.yaml"
  case "$(bash "$CS" resolve "$f")" in *"strategy: repo-files"*) pass "listed $f is a repo-files path" ;; *) bad "listed $f is not covered by a repo-files rule" ;; esac
done

[ "$fail" = 0 ] && echo "repo-files-test: PASS" || { echo "repo-files-test: FAIL"; exit 1; }
