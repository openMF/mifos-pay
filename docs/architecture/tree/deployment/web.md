# `deployment/web/`

> Part of [`deployment/`](../deployment.md)  
> **Measured:** 27 tracked files — 8× `.yaml`, 5× `.png`, 4× `.md`, 4× `.yml`, 2× `.toml`

### Contents

`cloudflare-pages/`, `gh-pages/`, `netlify/`, `og-images/`, `vercel/`

### Docs in the tree

| Path | |
|---|---|
| `deployment/web/cloudflare-pages/README.md` | deployment/web/cloudflare-pages — Cloudflare Pages |
| `deployment/web/gh-pages/README.md` | deployment/web/gh-pages — GitHub Pages |
| `deployment/web/netlify/README.md` | deployment/web/netlify — Netlify |
| `deployment/web/vercel/README.md` | deployment/web/vercel — Vercel |

<!-- tree-scaffold:end -->
## Significance

GitHub Pages from the Kotlin/JS distribution. The simplest target: one Gradle task, one branch push,
no signing and no review queue.

Its significance is as the **fast feedback loop** — a web deploy proves the shared Compose code renders
and the app boots without waiting on a store. When a UI regression appears here, it is in the shared
code rather than in a platform shell.
