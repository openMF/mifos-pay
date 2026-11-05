# `tools/data-ksp/`

> Part of [`tools/`](../tools.md)  
> **Measured:** 3 tracked files — 1× `.kts`, 1× `.kt`, 1× `.SymbolProcessorProvider`

**Processes:** `@RepositoryBinding` (`kpt.core.base.data.annotation.RepositoryBinding`)

<!-- tree-scaffold:end -->
## Significance

Processes `@RepositoryBinding` and generates the repository → interface Koin bindings, so a repository
is registered by annotating it where it is declared rather than by editing a DI module.

It also generates `AppStoreRegistry`, which is what lets `@FromStore(AppStoreIds.Loans)` resolve a store
by a compile-time constant instead of a raw string.
