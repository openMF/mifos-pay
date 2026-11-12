# `scripts/ci/`

> Part of [`scripts/`](../scripts.md)  
> **Measured:** 9 tracked files — 8× `.sh`, 1× `.py`

### Scripts

| Script | Purpose |
|---|---|
| `check-secrets-resolver.sh` | Phase-7 secrets-resolver guard. Enforces that the build-secrets resolver stays the |
| `ci-equivalent-check.sh` | Local CI parity for the kmp-project-template consumer. |
| `kotlin-checks.sh` | kotlin-checks.sh pre-commit|pre-push|files <paths…>|changed [<base>]|module <dir…>|all [--fix] |
| `kotlin-conform.sh` | make Kotlin and XML files Spotless-formatted and Detekt-clean, |
| `pre-commit.sh` | Installed as .git/hooks/pre-commit by `./gradlew installGitHooks`. The checks live in |
| `pre-push.sh` | Installed as .git/hooks/pre-push by `./gradlew installGitHooks`. The checks live in |
| `verify-dynamic-flavor-propagation.sh` | scripts/ci/verify-dynamic-flavor-propagation.sh |
| `verify-workflow-token-scope.sh` | scripts/ci/verify-workflow-token-scope.sh |

<!-- tree-scaffold:end -->
## Significance

The gates that run before code leaves a machine. `pre-commit.sh` and `pre-push.sh` carry the branch
guard plus Spotless, Detekt and Dependency Guard; `ci-equivalent-check.sh` reproduces CI locally so a
red build is discovered before the push rather than 45 minutes after it.

`verify-dynamic-flavor-propagation.sh` is a canary, not a check: it INJECTS a synthetic flavor, asserts
it auto-derives everywhere, and restores the file on a trap. An interrupted run must not leave a
synthetic flavor behind, which is why the restore is registered rather than called at the end.
