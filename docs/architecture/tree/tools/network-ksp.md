# `tools/network-ksp/`

> Part of [`tools/`](../tools.md)  
> **Measured:** 4 tracked files — 2× `.kt`, 1× `.kts`, 1× `.SymbolProcessorProvider`

**Processes:** `@ApiBinding` (`kpt.core.base.network.annotation.ApiBinding`)

<!-- tree-scaffold:end -->
## Significance

Processes `@ApiBinding` and generates the Ktorfit bindings per access point. An API interface is declared
once and bound by its access-point id, so the base URL, the loggable host and the header provider all
come from `app-profile#network.access_points` rather than from the call site.

That is what makes an endpoint change a config edit rather than a code change.

## The binding rule

**One access point, any number of APIs. One type, bound once.** Every binding expands to an
unqualified `single<T>` (`restApi` / `supabaseApi` in `core-base/network/NetworkDsl.kt`), so Koin keys
it by type alone:

- Different API types on the same access point are independent definitions. A monolith backend (one
  server, one base URL, one credential) binds all its resource APIs to a single point.
- The same type bound twice — on one point or on two — is one key defined twice; the later binding
  would silently replace the first for every injection site. The processor fails the build and names
  the type and every point it was bound on.

Earlier versions rejected the first case (grouping by access point) and missed the second. The check is
`duplicateBindingErrors` in `ApiBindingProcessor.kt`, pinned by `tools/network-ksp/src/test/kotlin/kpt/tools/networkksp/DuplicateBindingTest.kt`.
