# `tests/`

> **Kind:** Test fixtures  
> **Measured:** 27 tracked files — 19× `.sh`, 3× `.pbxproj`, 2× `.ours`, 2× `.theirs`, 1× `.md`

## Shape

| Path | Files |
|---|---:|
| `tests/customization-surface/` | 19 |
| `tests/fixtures/` | 8 |

## Subsections

| Unit | Files | Page |
|---|---:|---|
| `tests/customization-surface/` | 19 | [customization-surface](tests/customization-surface.md) |

<!-- tree-scaffold:end -->
## Significance

Cross-cutting test fixtures — the ones that do not belong to any single module's `commonTest`.

Two other fixture trees exist and are easy to confuse with this one:
`scripts/product-health/tests/` holds deliberately-malformed RED fixtures for the health checks, and
`scripts/docs/tests/` holds deliberately-UNDOCUMENTED Kotlin for the doc scanner. Both are excluded
from detekt, because a fixture that exists to be wrong cannot also satisfy the linter — documenting a
red fixture deletes the evidence that the check works.
