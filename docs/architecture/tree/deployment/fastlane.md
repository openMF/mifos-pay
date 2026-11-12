# `deployment/fastlane/`

> Part of [`deployment/`](../deployment.md)  
> **Measured:** 3 tracked files — 1× `.md`

3 tracked files.

### Docs in the tree

| Path | |
|---|---|
| `deployment/fastlane/README.md` | Installation |

<!-- tree-scaffold:end -->
## Significance

The **canonical Fastlane directory** — this is where the `Fastfile` lives, and every invocation is
`(cd deployment && bundle exec fastlane <platform> <lane>)`. The `fastlane/` shim at the repository root
exists only to point a reader here.

`Pluginfile` pins the plugin set (`firebase_app_distribution`, `increment_build_number`), which matters
because a lane that silently resolves a different plugin version can change upload behaviour without any
change to the lane itself.

Everything under `deployment/<platform>/` is loaded from here, which is why a platform directory with no
lanes — `linux/`, `windows/` — is still a valid part of the layout: it contributes metadata, not lanes.
