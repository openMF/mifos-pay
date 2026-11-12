#!/usr/bin/env bash
# org-file-names.sh — no file below the repository root borrows an org community file's name.
#
# WHY THIS EXISTS
# The organisation's repository ruleset asks a separate team to review any changed file named like a
# community file (SECURITY.md, LICENSE, CODE_OF_CONDUCT.md …), and it matches those names at ANY
# depth. `docs/architecture/tree/core-base/security.md` — only the guide for the `core-base/security`
# module — therefore made every template sync look like a change to the org's security policy, and a
# fork PR was held for a review of a file it never touched. Module guides for such modules are named
# `<module>-module.md` (scripts/docs/reserved-page-names.txt); this check keeps every other path clear.
#
#   OFN-1 no tracked file outside the repository root is named after an org community file
#
# Exit 0 = PASS · 1 = FAIL (blocks). Pure bash + git.
set -uo pipefail
: "${HEALTH_ROOT:?org-file-names: HEALTH_ROOT not set (run via product-health.sh)}"
cd "$HEALTH_ROOT" || exit 1

# The names the org ruleset matches (SECURITY.md, CODE_OF_CONDUCT.md, LICENSE[.md|.txt], COPYING,
# renovate.json). `.github/` is GitHub's own home for community files, so it is not a false match.
hits="$(git ls-files 2>/dev/null | grep '/' | grep -vE '^\.github/|^scripts/product-health/tests/' \
  | grep -iE '(^|/)(security\.md|code_of_conduct\.md|license(\.md|\.txt)?|copying|renovate\.json)$')"
if [ -n "$hits" ]; then
  echo "❌ OFN-1 file(s) below the root named like an org community file — the org ruleset asks the"
  echo "     scaffold reviewers to approve any change to them:"
  printf '%s\n' "$hits" | sed 's/^/     /'
  echo "     → rename (a module guide becomes <module>-module.md via scripts/docs/reserved-page-names.txt)"
  exit 1
fi
echo "no file below the root borrows an org community file's name"
exit 0
