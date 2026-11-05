# `tools/store-ksp/`

> Part of [`tools/`](../tools.md)  
> **Measured:** 3 tracked files — 1× `.kts`, 1× `.kt`, 1× `.SymbolProcessorProvider`

3 tracked files.

<!-- tree-scaffold:end -->
## Significance

Processes `@StoreProvider` and `@CacheKey`, and generates the most wiring of the four: the store
registry, the id constants, the cache keys, the Koin binding and the logout purge registration.

Before it existed, a store id was typed twice — `@StoreProvider(id = "loans")` in `core/store` and
`@FromStore("loans")` in `core/data` — in two modules with nothing linking them. A typo surfaced as an
unresolved symbol inside generated code, far from the mistake. The generated `AppStoreIds` makes the
compiler resolve the pair.

Cache keys are nested per store on purpose: keys are named by ROLE (`LIST`, `item`, `of`), unique within
a store but not across them, so a flat object would collide the moment two stores both declared `LIST`.
