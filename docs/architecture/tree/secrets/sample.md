# `secrets/sample/`

> Part of [`secrets/`](../secrets.md)  
> **Measured:** 53 tracked files — 14× `.md`, 3× `.json`, 2× `.p8`, 2× `.p12`, 1× `.gitkeep`

53 tracked files.

### Docs in the tree

| Path | |
|---|---|
| `secrets/sample/README.md` | secrets/sample/ — OSS-safe schema-as-code |
| `secrets/sample/SETUP_CHECKLIST.md` | Secrets Setup Checklist |

<!-- tree-scaffold:end -->
## Significance

The committed SHAPE of every secret, with no real value in it. A contributor copies `sample/` to
`live/` and fills it in, so the layout is discoverable without anything sensitive being present.

Every file carries a `CLAUDE-PLACEHOLDER` marker — text ones in a first-line comment, binary ones as a
magic prefix — and `manual-preflight.sh` probes for it. That is how a build detects "you copied the
sample but never filled it in", which otherwise surfaces as a signing failure with a misleading message.

`.placeholders-manifest.yaml` declares each sample's real peer, its kind and its GitHub-secret name, so
the mapping from a local file to a CI secret is data rather than tribal knowledge.
