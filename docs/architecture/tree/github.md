# `.github/`

> **Kind:** GitHub Actions  
> **Measured:** 17 tracked files — 12× `.yml`, 2× `.md`, 2× `.yaml`, 1× `.properties`

## Shape

| Path | Files |
|---|---:|
| `.github/workflows/` | 14 |
| (files at the root) | 3 |

## Workflows

| | |
|---|---|
| `cache-cleanup.yaml` | Cleanup Cache |
| `cla-check.yml` | CLA contributor check |
| `deployment-status.yml` | Deployment Status |
| `docs-check.yml` | Docs · API reference |
| `docs-refresh.yml` | Docs · refresh and publish |
| `pr-check.yml` | PR Check |
| `quality-gate.yml` | Quality Gate |
| `release-android-only.yml` | Release · Android Only |
| `release-multi-platform-local.yml` | Release · Multi-Platform (Local Dev · self-hosted) |
| `release-multi-platform.yml` | Release · Multi-Platform |
| `rollback.yml` | Rollback |
| `sync-dirs.yaml` | Sync CMP Directories |
| `tag-monthly-release.yml` | Tag Monthly Release |
| `tag-weekly-release.yml` | Tag Weekly Release |

## Subsections

| Unit | Files | Page |
|---|---:|---|
| `.github/workflows/` | 14 | [workflows](github/workflows.md) |

## Docs in the tree

_Authored beside the code, where the module's own consumers read them._

| Path | |
|---|---|
| `.github/CLAUDE.md` | GitHub Actions - CI/CD Infrastructure |
| `.github/CONTRIBUTING.md` | Contribute to Mobile Wallet Android Application |

## Guides

_Authored in depth, living in this area's own directory._

- [GitHub Actions Deep Dive - CI/CD Architecture](github/actions-deep-dive.md)
- [Release Approval Gates — Admin Setup Guide](github/release-approval-gates.md)

<!-- tree-scaffold:end -->
## Significance

CI and release orchestration. The local workflows are thin wrappers: each maps its dispatch inputs
onto a reusable workflow in `openMF/mifos-x-actionhub` and passes `secrets: inherit`, so the
orchestration lives in one place across every fork.

**Promotion is a rung, not a separate workflow.** Each platform takes a `<platform>_rung` — the top
rung to reach — and every lower rung fires first: `internal → beta → production`. Picking
`production` runs the whole ladder in one dispatch.

**`quality-gate.yml` is deliberately local** rather than the reusable v2: that one bundles SBOM
generation unconditionally, which breaks on Gradle 9 with KMP. Keeping it local is the reason Spotless,
Detekt, Dependency Guard and Kover can run at all.

**The exact action pins live in the workflow files**, never in prose — `grep 'openMF/mifos-x-actionhub'
.github/workflows/*.yml` is authoritative, and a documented version number is out of date the first
time someone bumps one.
