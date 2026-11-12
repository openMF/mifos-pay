# `scripts/white-label/`

> Part of [`scripts/`](../scripts.md)  
> **Measured:** 11 tracked files — 7× `.sh`, 3× `.rb`, 1× `.md`

### Scripts

| Script | Purpose |
|---|---|
| `customize.sh` | renamed to fork-init.sh on 2026-09-16. |
| `doctor.sh` | the SINGLE white-label lifecycle command for a fork of this template. |
| `firebase.sh` | Firebase Project Setup Script |
| `fork-init.sh` | turn a template clone into a branded, demo-free consumer fork. |
| `keystore.sh` | DEPRECATED thin shim (AC87 of fastlane-modernization epic) |
| `sync-dirs.sh` | bash4-required: associative arrays (EXCLUSIONS, kept empty) and the contract reader; see the guard below. |
| `template-migrations.sh` | make a repo follow the renames the template made. |

### Docs in the tree

| Path | |
|---|---|
| `scripts/white-label/README.md` | `scripts/white-label/` — the white-label lifecycle |

### Guides

- [Sync Capabilities](white-label/template-sync.md)

<!-- tree-scaffold:end -->
## Significance

The fork lifecycle, and `doctor.sh` is the **one entry point** — setup, verify and sync behind a single
command, so there is no ordering for a contributor to remember.

`sync-dirs.sh` is the piece with the most consequence: it pulls upstream template updates without
clobbering fork work, and it decides ownership per path from `customization-surface.yaml` rather than by
directory. That contract defaults to template-first (`**` → template), so fork territory is claimed
EXPLICITLY — a new fork-owned directory needs a rule there before the next sync overwrites it.

`derive.rb` is the single projection from `app-profile/` to `gradle/fork.properties`. Nothing else may
write that file, and `fork-props-single-reader.sh` fails the build if any script reads it by hand
instead of through `_shared/fork-props.sh`.
