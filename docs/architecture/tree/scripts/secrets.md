# `scripts/secrets/`

> Part of [`scripts/`](../scripts.md)  
> **Measured:** 5 tracked files — 4× `.sh`, 1× `.md`

### Scripts

| Script | Purpose |
|---|---|
| `_lib.sh` | ============================================================================= |
| `secrets-status.sh` | ============================================================================= |
| `setup-secrets.sh` | ============================================================================= |
| `sync-secrets-to-github.sh` | scripts/secrets/sync-secrets-to-github.sh |

### Docs in the tree

| Path | |
|---|---|
| `scripts/secrets/README.md` | Platform-wise secrets toolkit |

<!-- tree-scaffold:end -->
## Significance

The platform-wise secret toolkit. `setup-secrets.sh` is the interactive intake, `secrets-status.sh`
reports what is present per platform, and `sync-secrets-to-github.sh` pushes to repository secrets.

Every path resolves through `secrets/LAYOUT.yaml` rather than being hardcoded, which is what lets a
secret move without touching a consumer. `_write_file` chmods AFTER writing so a secret is never briefly
world-readable, and nothing here ever prints a value — metadata always suffices to prove materialisation.
