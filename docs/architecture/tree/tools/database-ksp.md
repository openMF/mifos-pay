# `tools/database-ksp/`

> Part of [`tools/`](../tools.md)  
> **Measured:** 3 tracked files — 1× `.kts`, 1× `.kt`, 1× `.SymbolProcessorProvider`

**Processes:** `@annotation` (`kpt.core.base.database.annotation`)

<!-- tree-scaffold:end -->
## Significance

Processes `@DbEntity` and generates the Room `AppDatabase` entity list.

This one exists because of a hard Room constraint: the database needs ONE compile-time
`entities = [...]` array literal, which means a fork's tables cannot live in a separate file. Generating
the list from annotations is the only way a fork can add a table without editing a template-owned file —
and it is why there is no hand-maintained `AppDatabase.kt` in the tree at all.
