#!/usr/bin/env bash
# A repository's own issue and pull-request forms are fork-owned. The template ships none, so while
# they resolved to `.github/**` (template) a sync deleted them from every fork that had them.
cd "$(dirname "$0")/../.." || exit 1
fail=0
for p in .github/ISSUE_TEMPLATE/DMP_2024.yml .github/ISSUE_TEMPLATE.md .github/PULL_REQUEST_TEMPLATE.md \
         .github/pull_request_template.md; do
  own="$(bash scripts/customization-surface.sh resolve "$p" | cut -f2 | awk '{print $1}')"
  [ "$own" = fork ] || { echo "  FAIL: $p resolves to '$own', expected fork"; fail=1; }
done
# The rule must stay narrow: the template's workflows are still the template's.
own="$(bash scripts/customization-surface.sh resolve .github/workflows/build.yml | cut -f2 | awk '{print $1}')"
[ "$own" = template ] || { echo "  FAIL: .github/workflows/build.yml resolves to '$own', expected template"; fail=1; }
[ "$fail" = 0 ] && echo "github-forms-test: PASS" || { echo "github-forms-test: FAIL"; exit 1; }
