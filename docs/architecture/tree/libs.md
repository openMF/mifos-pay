# `libs/`

> **Kind:** Kotlin Multiplatform module  
> **Measured:** 138 tracked files — 117× `.kt`, 18× `.xml`, 1× `.gitignore`, 1× `.kts`, 1× `.pro`

## Shape

| Path | Files |
|---|---:|
| `libs/mifos-loans/` | 138 |

## Modules

| | |
|---|---|
| `libs:mifos-loans` | 117 Kotlin files — [guide](libs/mifos-loans.md) |

## Subsections

| Unit | Files | Page |
|---|---:|---|
| `libs/mifos-loans/` | 138 | [mifos-loans](libs/mifos-loans.md) |

<!-- tree-scaffold:end -->
## Significance

In-repo libraries that are not features: self-contained units the app consumes like an external
dependency. Today that is `mifos-loans`, the loan-application flow. A library here can move to its own
repository without touching feature code.
