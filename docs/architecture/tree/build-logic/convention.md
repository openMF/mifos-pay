# `build-logic/convention/`

> Part of [`build-logic/`](../build-logic.md)  
> **Measured:** 41 tracked files — 39× `.kt`, 1× `.kts`, 1× `.gitkeep`

41 tracked files.

<!-- tree-scaffold:end -->
## Significance

Every convention plugin, compiled before the main build configures — which is what lets "a KMP library
module" or "an Android application" be defined once instead of in 46 build files.

Two carry the most weight. `SyncForkConfigPlugin` is the projection from `app-profile/` onto every
generated surface, and caching is disabled on it deliberately: it reads a gitignored `fork.properties`
and writes outside the build directory, so a cached result would be wrong on any machine whose fork
config differs. `FeatureAggregateConventionPlugin` scans each feature's `di` and `navigation` packages
and generates the Koin, destination and tab aggregates — the reason adding a feature needs no registry
edit, and the reason a feature cannot be half-registered.

`local/LocalFlavors.kt` ships EMPTY with a worked example in comments. It is `owner: fork`, so a sync
never overwrites it — which is exactly why the template cannot put anything real there.
