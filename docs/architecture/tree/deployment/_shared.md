# `deployment/_shared/`

> Part of [`deployment/`](../deployment.md)  
> **Measured:** 25 tracked files — 12× `.rb`, 9× `.sh`, 2× `.md`, 2× `.yaml`

### Contents

`lib/`, `scripts/`

### Docs in the tree

| Path | |
|---|---|
| `deployment/_shared/scripts/SECRETS_INVENTORY.md` | gha_secret_var Inventory — mifos-x/kmp-project-template |
| `deployment/_shared/scripts/_README.md` | deployment/_shared/scripts — script index |

<!-- tree-scaffold:end -->
## Significance

The code every lane depends on, and the reason 34 lanes do not each re-derive the build's identity.

`config.rb` resolves configuration in a fixed order — ENV first, then `app-profile`, then the generated
`fork.properties` bridge — so CI can inject a value without writing anything to disk and a stale bridge
can never beat the source of truth. `build_secrets.rb` resolves a secret ALIAS to a path through
`secrets/LAYOUT.yaml`, which is what lets a secret move without touching a single lane.

`before_all.rb` carries the two things easiest to get wrong: it locates the repo root by looking for
`app-profile/app.yaml` (bounded, so a lane run from the wrong directory fails fast), and it restores
every source file a lane rewrote in place. On CI the tree is disposable; on a developer's machine it is
their working copy.
