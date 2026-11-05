# `build-logic/`

> **Kind:** Gradle convention plugins (a separate build)  
> **Measured:** 44 tracked files — 39× `.kt`, 2× `.kts`, 1× `.md`, 1× `.gitkeep`, 1× `.properties`

## Shape

| Path | Files |
|---|---:|
| `build-logic/convention/` | 41 |
| (files at the root) | 3 |

## Subsections

| Unit | Files | Page |
|---|---:|---|
| `build-logic/convention/` | 41 | [convention](build-logic/convention.md) |

## Docs in the tree

_Authored beside the code, where the module's own consumers read them._

| Path | |
|---|---|
| `build-logic/README.md` | Convention Plugins |

## Guides

_Authored in depth, living in this area's own directory._

- [Adoption record: `kmp-product-flavors` plugin in this template](build-logic/kmp-product-flavors-adoption.md)

## Deeper reading

_This page is the map; these are the depth. Both are authored, and linking them is what
stops two descriptions of one thing from drifting apart._

- [Flavor extension](../../architecture/cross-cutting/flavors-extension.md)
- [Consumer app migration](../../architecture/cross-cutting/consumer-app-migration-guide.md)

<!-- tree-scaffold:end -->
## Significance

A **separate Gradle build** that supplies every convention plugin the modules apply. Its separateness
is the point: it is compiled before the main build configures, so it can decide what "a KMP library
module" or "an Android application" means in one place rather than in 46 build files.

**`SyncForkConfigPlugin` is the largest piece** and the one a fork depends on most — it is the task
that projects `app-profile/` onto every generated surface. Caching is disabled on it deliberately: it
reads a gitignored `fork.properties` and writes outside the build directory, so a cached result would
be wrong on any machine whose fork config differs.

**`FeatureAggregateConventionPlugin` is why adding a feature needs no registry edit.** It scans each
feature module's `di` and `navigation` packages and generates the Koin, destination and tab
aggregates. The generated objects are the ONLY place those lists exist, so a feature cannot be half
registered.

**Being a separate build has a cost worth knowing:** `detekt` never scanned it, so 41 declarations
here were undocumented while the main build reported full coverage. The doc scanner covers it
explicitly for that reason.
