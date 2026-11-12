# `gradle/`

> **Kind:** Gradle wrapper + version catalog  
> **Measured:** 6 tracked files — 3× `.properties`, 1× `.template`, 1× `.toml`, 1× `.jar`

## Shape

| Path | Files |
|---|---:|
| (files at the root) | 4 |
| `gradle/wrapper/` | 2 |

## Guides

_Authored in depth, living in this area's own directory._

- [Version Handling - Understanding Version Formats](gradle/version-handling.md)

<!-- tree-scaffold:end -->
## Significance

The Gradle wrapper and `libs.versions.toml`, the single version catalog every module resolves through.

**`appId` in the catalog is generated,** written by `syncForkConfig` from
`app-profile/app.yaml#identity.app_id`. Editing the catalog line directly puts the build and the store
listing into disagreement, and `appid-consistency.sh` fails CI when they drift.

**Module namespaces are deliberately NOT here.** Every module's Android `namespace` derives from the
framework-owned constant `BASE_MODULE_NAMESPACE` (`kpt`) in `build-logic`. It matches the hardcoded
Kotlin package root, has no per-fork meaning, and is kept out of the catalog so it can never cause a
merge conflict during a template sync.
