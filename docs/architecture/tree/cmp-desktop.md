# `cmp-desktop/`

> **Kind:** Project area  
> **Measured:** 12 tracked files — 2× `.md`, 2× `.entitlements`, 1× `.gitignore`, 1× `.kts`, 1× `.pro`

## Shape

| Path | Files |
|---|---:|
| (files at the root) | 8 |
| `cmp-desktop/icons/` | 3 |
| `cmp-desktop/src/` | 1 |

## Docs in the tree

_Authored beside the code, where the module's own consumers read them._

| Path | |
|---|---|
| `cmp-desktop/CONSUMPTION.md` | Consuming `cmp-desktop` in a fork |
| `cmp-desktop/README.md` |  |

<!-- tree-scaffold:end -->
## Significance

The Compose Desktop JVM application, packaged by `packageReleaseDistributionForCurrentOS` into a
platform installer — EXE and MSI on Windows, DMG on macOS, DEB on Linux. One Gradle task, three
outputs, built on a matrix.

**Icons come from `app-profile/icons/`,** copied to `cmp-desktop/icons/` in the three formats each OS
needs (`.icns`, `.ico`, `.png`) by `syncForkConfig`. A missing source is a no-op, so a fork that has
not supplied icons keeps the template's rather than failing the build.

Desktop is also the **device-free render tier**: Roborazzi golden screenshots run on `desktopTest`,
which is why a Compose layout regression can be caught in CI without an emulator.
