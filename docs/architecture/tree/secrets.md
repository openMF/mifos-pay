# `secrets/`

> **Kind:** Secret material (gitignored live values)  
> **Measured:** 56 tracked files — 14× `.md`, 3× `.yaml`, 3× `.json`, 2× `.gitkeep`, 2× `.p8`

## Shape

| Path | Files |
|---|---:|
| `secrets/sample/` | 53 |
| (files at the root) | 3 |

## Subsections

| Unit | Files | Page |
|---|---:|---|
| `secrets/sample/` | 53 | [sample](secrets/sample.md) |

## Docs in the tree

_Authored beside the code, where the module's own consumers read them._

| Path | |
|---|---|
| `secrets/sample/README.md` | secrets/sample/ — OSS-safe schema-as-code |
| `secrets/sample/SETUP_CHECKLIST.md` | Secrets Setup Checklist |

## Guides

_Authored in depth, living in this area's own directory._

- [Contributor Secrets Bootstrap](secrets/contributor-onboarding.md)
- [Secrets Management - Complete Guide](secrets/management.md)
- [Secrets Manager](secrets/manager.md)

<!-- tree-scaffold:end -->
## Significance

Secret material. `secrets/live/` is **gitignored and fork-owned**; `secrets/sample/` is the committed
shape a contributor copies from, so the layout is discoverable without any real value being present.

**Two intake paths, both deliberate.** An OSS fork copies `sample/` and fills it in. A
framework-managed project runs `/secrets pull`, which materialises from a SOPS+age vault to the
canonical paths declared in `secrets/LAYOUT.yaml`. Every consumer — Fastlane, the workflows, the dev
build — resolves through that layout rather than hardcoding a path, so moving a secret is one edit.

**A value must never reach a transcript or a log.** Metadata always suffices to prove materialisation:
`test -f`, `wc -c`, `shasum`. If a check appears to need the value itself, the check is wrong.
