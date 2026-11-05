# `tools/`

> **Kind:** KSP symbol processors  
> **Measured:** 13 tracked files — 5× `.kt`, 4× `.kts`, 4× `.SymbolProcessorProvider`

## Shape

| Path | Files |
|---|---:|
| `tools/network-ksp/` | 4 |
| `tools/data-ksp/` | 3 |
| `tools/database-ksp/` | 3 |
| `tools/store-ksp/` | 3 |

## Processors

| | |
|---|---|
| `tools/data-ksp` |  |
| `tools/database-ksp` |  |
| `tools/network-ksp` |  |
| `tools/store-ksp` |  |

## Subsections

| Unit | Files | Page |
|---|---:|---|
| `tools/data-ksp/` | 3 | [data-ksp](tools/data-ksp.md) |
| `tools/database-ksp/` | 3 | [database-ksp](tools/database-ksp.md) |
| `tools/network-ksp/` | 4 | [network-ksp](tools/network-ksp.md) |
| `tools/store-ksp/` | 3 | [store-ksp](tools/store-ksp.md) |

<!-- tree-scaffold:end -->
## Significance

Four KSP symbol processors, and the reason registration is declarative. Each turns an annotation into
generated wiring:

| Annotation | Processor | Generates |
|---|---|---|
| `@StoreProvider` | `store-ksp` | store registry, ids, cache keys, Koin binding, logout purge |
| `@RepositoryBinding` | `data-ksp` | repository → interface Koin bindings |
| `@ApiBinding` | `network-ksp` | Ktorfit API bindings per access point |
| `@DbEntity` | `database-ksp` | the Room `AppDatabase` entity list |

**Why this matters more than it looks.** Before `store-ksp` generated `AppStoreIds`, the same store id
was typed twice — `@StoreProvider(id = "loans")` in `core/store` and `@FromStore("loans")` in
`core/data` — in two modules with nothing linking them. A typo surfaced as an unresolved symbol inside
generated code, far from the mistake. And `database-ksp` is why there is no hand-maintained
`AppDatabase.kt`: Room needs one compile-time `entities = [...]` literal, which a fork cannot extend
from a separate file, so the list is generated from the annotations instead.
