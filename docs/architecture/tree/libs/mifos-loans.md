# `libs/mifos-loans/`

> Part of [`libs/`](../libs.md)  
> **Measured:** 138 tracked files — 117× `.kt`, 18× `.xml`, 1× `.gitignore`, 1× `.kts`, 1× `.pro`

138 tracked files.

<!-- tree-scaffold:end -->
## Significance

The loan-application flow — loan products, apply, confirm details, provider web view — packaged as a
library with its own repository, network layer and Koin module (`LoanApplicationModule`). The app
shell registers that module and links to its screens; drop both together if loans are removed.
