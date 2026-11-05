# `scripts/`

> **Kind:** Kotlin Multiplatform module group  
> **Measured:** 478 tracked files — 137× `.sh`, 135× `.kt`, 56× `.md`, 50× `.yaml`, 12× `.gitkeep`

## Shape

| Path | Files |
|---|---:|
| `scripts/product-health/` | 416 |
| `scripts/docs/` | 18 |
| `scripts/white-label/` | 11 |
| (files at the root) | 9 |
| `scripts/ci/` | 9 |
| `scripts/ios/` | 5 |
| `scripts/secrets/` | 5 |
| `scripts/store/` | 4 |
| `scripts/_shared/` | 1 |

## Modules

| | |
|---|---|
| `scripts:product-health` | 134 Kotlin files — [guide](scripts/product-health.md) |

## Subsections

| Unit | Files | Page |
|---|---:|---|
| `scripts/ci/` | 9 | [ci](scripts/ci.md) |
| `scripts/docs/` | 18 | [docs](scripts/docs.md) |
| `scripts/ios/` | 5 | [ios](scripts/ios.md) |
| `scripts/product-health/` | 416 | [product-health](scripts/product-health.md) |
| `scripts/secrets/` | 5 | [secrets](scripts/secrets.md) |
| `scripts/store/` | 4 | [store](scripts/store.md) |
| `scripts/white-label/` | 11 | [white-label](scripts/white-label.md) |

## Docs in the tree

_Authored beside the code, where the module's own consumers read them._

| Path | |
|---|---|
| `scripts/CLAUDE.md` | Bash Scripts — Automation & Setup |
| `scripts/kotlin-2-4-0-NOTES.md` | Kotlin 2.4.0 dependency modernization — investigation + verification record |
| `scripts/secrets/README.md` | Platform-wise secrets toolkit |
| `scripts/white-label/README.md` | `scripts/white-label/` — the white-label lifecycle |

<!-- tree-scaffold:end -->
## Significance

The repo's automation, and the reason a fork can be stood up without a runbook. Four groups matter
most: `white-label/` is the fork lifecycle (`doctor.sh` is the one entry point for setup, verify and
sync), `product-health/` is a 33-check suite that fails the build on architectural drift rather than
on style, `ci/` holds the pre-commit and CI-parity gates, and `docs/` generates this documentation.

**`ruby-exec.sh` is not a convenience wrapper.** rbenv works by PATH interception, so in a shell that
never ran `rbenv init` — a cron job, a GUI terminal, a CI step that reset PATH — `ruby` falls through
to macOS's 2.6 and `bundle exec` dies inside rubygems talking about gem activation. The gems are
fine; the interpreter is wrong. Every Ruby call goes through the resolver, and `product-health`'s
`RT-9` fails the build if a tracked script invokes `bundle` directly.

**`product-health/tests/` is deliberately malformed.** Those 140 Kotlin files are RED fixtures — a
check that cannot be proven to FAIL on broken input has not been tested. They are excluded from
detekt for that reason, not overlooked.
