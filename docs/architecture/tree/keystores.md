# `keystores/`

> **Kind:** Project area  
> **Measured:** 2 tracked files — 2× `.keystore`

## Shape

| Path | Files |
|---|---:|
| (files at the root) | 2 |

<!-- tree-scaffold:end -->
## Significance

Android signing keystores. A release must be signed with the same key as every earlier release, or
the store rejects the update and installed users cannot upgrade. Signing material belongs in the
secrets vault rather than the repository: treat these files as legacy and keep new keys out.
