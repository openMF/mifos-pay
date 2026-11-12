# `fastlane/`

> **Kind:** Release + deployment automation  
> **Measured:** 4 tracked files — 1× `.md`

## Shape

| Path | Files |
|---|---:|
| (files at the root) | 4 |

## Fastlane lanes

| | |
|---|---|
| `fastlane … build_ios` | Build unsigned debug iOS app for PR checks (no signing, no archive) |

## Docs in the tree

_Authored beside the code, where the module's own consumers read them._

| Path | |
|---|---|
| `fastlane/README.md` | Installation |

<!-- tree-scaffold:end -->
## Significance

The root Fastlane shim. The real lanes and configuration live in `deployment/`, which is the canonical
directory — every invocation is `(cd deployment && bundle exec fastlane <platform> <lane>)`.

This exists because Fastlane looks for a `fastlane/` directory beside the working directory, and a
reader who runs it from the repo root should be pointed at `deployment/` rather than meeting a
confusing failure.
