# `legal/`

> **Kind:** Generated legal documents  
> **Measured:** 6 tracked files — 4× `.md`, 2× `.sh`

## Shape

| Path | Files |
|---|---:|
| `legal/output/` | 2 |
| `legal/_shared/` | 1 |
| (files at the root) | 1 |
| `legal/privacy-policy/` | 1 |
| `legal/terms/` | 1 |

## Docs in the tree

_Authored beside the code, where the module's own consumers read them._

| Path | |
|---|---|
| `legal/output/privacy-policy.md` | Privacy Policy — Mifos Pay |
| `legal/output/terms.md` | Terms of Service — Mifos Pay |
| `legal/privacy-policy/template.md` | Privacy Policy — ${APP_NAME} |
| `legal/terms/template.md` | Terms of Service — ${APP_NAME} |

<!-- tree-scaffold:end -->
## Significance

The privacy policy and terms, **generated** from `app-profile` by `legal/generate.sh` rather than
written per fork. A store rejects a listing whose policy names no legal entity, and a hand-written
policy drifts from the app the moment a data-collection answer changes.

`render()` substitutes only the variables in `LEGAL_VARS`. A bare `envsubst` would also expand
anything else in the legal text that happens to look like a shell variable — which in a document full
of `$` and braces is a real risk, not a theoretical one.
