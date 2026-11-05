# `.github/workflows/`

> Part of [`.github/`](../github.md)  
> **Measured:** 14 tracked files — 12× `.yml`, 2× `.yaml`

14 tracked files.

<!-- tree-scaffold:end -->
## Significance

Thin wrappers. Each maps its dispatch inputs onto a reusable workflow in `openMF/mifos-x-actionhub` and
passes `secrets: inherit`, so orchestration lives in one place across every fork rather than being
copied into each.

**Promotion is a rung, not a workflow.** Each platform takes the top rung to reach and every lower rung
fires first: `internal → beta → production`. Store-facing stages declare a GitHub Environment, and
GitHub pauses them behind an approval button **only if that environment has required reviewers** —
configured once per repo by `scripts/configure-release-environments.sh`. Without reviewers the stage runs
unprompted, so the gate is the reviewers, not the workflow.

`quality-gate.yml` is deliberately local rather than the reusable v2, which bundles SBOM generation
unconditionally and breaks on Gradle 9 with KMP. The action pins live in these files and nowhere else —
`grep 'openMF/mifos-x-actionhub' .github/workflows/*.yml` is authoritative, and a version written into
prose is stale the first time someone bumps one.
