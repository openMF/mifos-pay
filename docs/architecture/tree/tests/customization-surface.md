# `tests/customization-surface/`

> Part of [`tests/`](../tests.md)  
> **Measured:** 19 tracked files — 19× `.sh`

19 tracked files.

<!-- tree-scaffold:end -->
## Significance

Functional tests for the template-sync ownership engine. Each runs the REAL functions — extracted from
`scripts/customization-surface.sh` and `scripts/white-label/sync-dirs.sh` — against a throwaway git
repository, so a passing test proves the shipped code, not a copy of it.

| Test | Proves |
|---|---|
| `merge-3way-test.sh` | `cs_merge_3way` keeps a fork's added `<uses-permission>` when the template updates the manifest |
| `mirror-test.sh` | `--mirror` removes template-owned files the fork added, leaves fork-owned and held files alone, and never touches a file the template ships; a held template file is also kept from the overlay |
| `pinned-branch-test.sh` | a sync pinned to a caller's branch (`SYNC_PREBUILT_BRANCH`) reads the fork's state from that branch, so a held file that exists only there is kept with its content |
| `yaml-schema-merge-test.sh` | the app-profile merge is built on the fork's file: fork block scalars, maps, fork-only keys and comments survive, the template's new keys and untouched defaults arrive, a key the fork deleted stays deleted |
| `template-ref-test.sh` | dev mode: `--template-ref <commit>` syncs from an unmerged template commit fetched by hash, refuses an unreachable one, and leaves prod mode (merged default branch) unchanged |
| `sync-roots-test.sh` | a path outside every sync root resolves `fork`, so a fork's own top-level directory is never claimed by the template |

`mirror-test.sh` and `pinned-branch-test.sh` need bash 4: the engine's `EXCLUSIONS` map is an associative array.
