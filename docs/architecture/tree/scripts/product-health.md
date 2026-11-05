# `scripts/product-health/`

> Part of [`scripts/`](../scripts.md)  
> **Measured:** 416 tracked files — 134× `.kt`, 90× `.sh`, 52× `.md`, 50× `.yaml`, 12× `.gitkeep`

### Scripts

| Script | Purpose |
|---|---|
| `appid-consistency.sh` | gradle/fork.properties#app.id is the AUTHORED single source of truth |
| `cache-key-call-sites.sh` | no production call site spells a cacheKey string literal. |
| `compose-strings-apostrophe.sh` | no Android-style `\'` escape in Compose Multiplatform strings. |
| `core-module-deps.sh` | every core/<mod> build file full-copies, and its fork deps live in a seam. |
| `core-package-ownership.sh` | declared-package modules stay consistent with the contract. |
| `demo-strip-coherence.sh` | the POST-`--clean` tree must be coherent, not just the template's. |
| `deployment-template-owned.sh` | syncForkConfig never writes fork identity into deployment/. |
| `deployment-whitelabel.sh` | the white-label boundary between the fork-owned deployment SoT |
| `fork-identity.sh` | DIRECTIONAL, placeholder-aware signing + org identity guard. |
| `fork-props-manifest-parity.sh` | the TWO writers of gradle/fork.properties must agree on |
| `fork-props-single-reader.sh` | gradle/fork.properties has ONE reader per language. |
| `generated-file-ownership.sh` | every WHOLE-FILE generated source is owner:fork. |
| `ios-pbxproj-identity.sh` | the iOS Xcode project MUST derive its bundle id + provisioning |
| `ios-swiftpm-linked-package.sh` | cmp-ios/KotlinMultiplatformLinkedPackage MUST stay |
| `kdoc-comment-balance.sh` | a glob written in a KDoc must not open or close a comment. |
| `keychain-cleanup.sh` | setup_ci's throwaway keychain must be GUARANTEED-cleaned so it never |
| `listing-sync.sh` | the store listing must sync to the store on EVERY store deploy state, |
| `migration-ledger.sh` | the Room migration ledger is append-only and internally coherent. |
| `network-access-points.sh` | app-profile is the ONLY place a network endpoint is declared, |
| `no-vacuous-assert.sh` | a test that cannot fail is worse than no test. |
| `ownership-naming.sh` | a source file's name prefix says who owns it, and the contract agrees. |
| `pbxproj-merge.sh` | the Xcode project survives a template sync intact. |
| `ruby-toolchain-coherence.sh` | ONE Ruby version, declared once, reachable everywhere. |
| `secrets-alias-namespace.sh` | DIRECTIONAL guard on the vault-facing secrets files |
| `secrets-needs-generated.sh` | deployment/**/secrets-needs.yaml agree with secrets/LAYOUT.yaml. |
| `secrets-no-clobber-tracked.sh` | `/secrets pull` (BuildSecrets#materialize!) must NEVER |
| `self-test-canaries.sh` | runs every product-health canary (tests/*/run.sh) so the RED/GREEN |
| `shell-portability.sh` | tracked shell scripts must run on the RUNNERS, not just on a Mac. |
| `store-archetype-coverage.sh` | the 8 Store5 archetypes each keep a real showcase. |
| `store-fork-seam-wiring.sh` | the template actually CONSULTS its fork seams. |

_…and 60 more._

<!-- tree-scaffold:end -->
## Significance

Thirty-three checks that fail the build on **architectural drift**, not on style. Spotless and Detekt
already police formatting; this suite polices the things a linter cannot see — that `appId` in the
version catalog still matches `app-profile`, that every Store5 archetype still has a showcase, that the
fork seams are wired, that the migration ledger was appended to rather than renumbered.

**Its 370 files are mostly RED FIXTURES,** and that is the point: a check that cannot be demonstrated
to FAIL on broken input has not been tested. `tests/*-canary/red*/` directories are deliberately
malformed, which is why the whole tree is excluded from detekt — asking a red fixture to satisfy the
linter deletes the evidence.

Run `TEMPLATE_SELF_BUILD=1` on the upstream template itself, where fork-identity checks would otherwise
misfire on placeholder values.
