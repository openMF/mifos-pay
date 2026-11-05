# Architecture

The architecture SoT for this template lives in **[ARCHITECTURE.md](ARCHITECTURE.md)**.

- **[Overview](ARCHITECTURE.md)** — layers, ownership, and how a feature is assembled
- **[`core-base/` modules](modules/core-base/)** — framework-shared, read-only to generators
- **[`core/` modules](modules/core/)** — fork-owned
- **[cross-cutting](cross-cutting/)** · **[patterns](patterns/)**

Each module page carries an **API reference** generated from source by
`core/scripts/template-api-docs-gen.sh` and kept current by `G-TEMPLATE-API-DOCS`
(RULE-TEMPLATE-API-DOCS-CURRENT-001). Everything inside an `<!-- api-docs:begin … -->`
block is generated — edit the Kotlin, then re-run the generator; prose outside the block
is authored and preserved.

> This file also exists so docsify has a directory index here: with `loadSidebar` +
> `relativePath`, browsing a nested module page probes for `architecture/README.md`, and
> without it every page load logged a harmless 404.
