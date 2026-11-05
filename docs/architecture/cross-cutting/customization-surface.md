# Customization Surface — the fork-ownership contract

`customization-surface.yaml` (repo root) is the **single declared source of truth
for who owns each path** when a fork syncs template updates. It replaces ownership
knowledge that was previously implicit and scattered across `scripts/white-label/sync-dirs.sh`
(`SYNC_DIRS` + `EXCLUSIONS`), `scripts/white-label/fork-init.sh`, and the `syncForkConfig` copy map.

## Why it exists

A fork runs `scripts/white-label/sync-dirs.sh` to pull template updates while keeping its own branding
+ features. The hard question is *"which files may the sync overwrite?"* When that
answer lives only in an ad-hoc `EXCLUSIONS` list, a path with no exclusion gets
**blind-overwritten** — and that silently drops fork edits. The motivating case:

> `cmp-android/src/main/AndroidManifest.xml` is template-shipped **and**
> fork-extended (a fork adds `RECORD_AUDIO`, `FOREGROUND_SERVICE_MICROPHONE`, …).
> The `EXCLUSIONS` map preserved only `src/main/res`, so a full sync overwrote the
> manifest and **dropped the fork's permissions** — breaking the app.

The contract makes ownership **explicit, single-source, and machine-checkable**, so
that whole class of silent loss becomes impossible.

## The three ownership modes

| owner | meaning | sync behaviour |
|---|---|---|
| `template` | template is the sole author | **overwrite** the fork's copy. A fork edit here is drift — a genuine fix belongs **upstream** as a PR to `openMF/kmp-project-template`. |
| `fork` | the fork is the sole author | **never touch** it (branding, demo, the `core/store` seam, generated `Config.xcconfig`, local flavors, icons, store listings, fork identity). |
| `merge` | both author | **3-way merge**, never a blind copy (`AndroidManifest.xml`, `strings.xml`, `libs.versions.toml`, `settings.gradle.kts`, nav host). |

### Repository community files — `owner: fork`, `strategy: repo-files`

`CODE_OF_CONDUCT.md`, `CONTRIBUTING.md` (or `Contributing.md`), `LICENSE` (or `LICENCE`, `LICENSE.md`)
and `SECURITY.md` belong to the **repository**: the org's devops and security teams set them up per
repository (a CLA bot or an org-wide `SECURITY.md` commit lands straight in each repo). They are not
the template's, and not this repo's tooling's. A sync never writes them.
They were `owner: template` until 2026-11-03, and the field-officer sync replaced that app's own
`Contributing.md` and `LICENCE` with the template's.

Each file declares who may **edit** it, per file. The repo's `app-profile/app.yaml#repo_files.files`
holds the default; the framework workspace's `_org/company.yaml#repo_files.<file>` overrides it, file by
file, and the framework's edit guard enforces the result.

```yaml
# app-profile/app.yaml — the template ships every file locked
repo_files:
  managed_by: org
  files:
    CODE_OF_CONDUCT.md: { access: read }
    CONTRIBUTING.md:    { access: read }
    LICENSE:            { access: read }
    SECURITY.md:        { access: read }
    .github/workflows/cla-check.yml: { access: read }   # the org's CLA bot workflow, keyed by path

# workspaces/<ws>/_org/company.yaml — the org's override, per file
repo_files:
  CONTRIBUTING.md: { access: write, writers: [some-login] }   # only these GitHub logins
  LICENSE:         { access: write }                          # anyone in the workspace
```

| access | meaning |
|---|---|
| `read` (default) | locked. Also applies when a file, or the whole block, is absent. |
| `write` | editable; with `writers`, only those GitHub logins (an unknown login is refused). |

Other spellings share an entry: `Contributing.md` → `CONTRIBUTING.md`; `LICENCE` / `LICENSE.md` →
`LICENSE`. A file outside the repo root is keyed by its path: `.github/workflows/cla-check.yml`, the CLA
contributor check the org's automation pushes into every repo, sits in this class too (its rule precedes
the `.github/**` template rule, since the first match wins). `scripts/customization-surface.sh resolve
LICENSE` prints `fork  (strategy: repo-files)`.

> The most-edited `owner: fork` files are the **white-label extension seams** — the registries a fork
> uses to add features, tabs, the home body, and startup hooks without touching template infra. They are
> documented in
> [`cmp-navigation/.../registry/README.md`](../README.md).

## Ownership you can read from a file name

| prefix | owner | for a fork developer |
|---|---|---|
| `Kpt*` | `template` | template code — a sync replaces it; never edit it in a fork |
| `KptFork*` | `merge` | template code with fork pieces (`KptForkScaffold.kt`, `KptForkRootScaffold.kt`) — 3-way merged |
| `Fork*` | `fork` | yours — the seams (`ForkStoreModule`, `ForkErrorMapper`, `ForkScreenStateDefaults`, `ForkTheme.kt`, the `Fork*Registry` objects); a sync never touches them |

The rule runs one way: a prefixed file must resolve to the owner its prefix names
(`scripts/product-health/checks/ownership-naming.sh`, `ON-1`), while fork code at large needs no prefix.
Generated `App*` projections of `app-profile/` (`AppAccessPoints`, `AppOrgLinks`, …) keep their names.

**Demo packages are owned beside the module, not in this file.** A `- packages: module-packages` entry
marks where, in first-match order, each `core/<module>/module-packages.yaml` `demo:` id becomes a
`core/<module>/**/kpt/core/<module>/<id>/**` → `demo-showcase` rule. The sync engine holds no ownership
knowledge of its own: `is_excluded` keeps a path in the fork's state exactly when it is held
(`.template-sync-holds`) or resolves `fork` / `demo-showcase`.

**Renames reach forks through `template-migrations.yaml`.** When the template renames a file or an
identifier, the row goes there (`words: "Old -> New"` rewrites the identifier and renames `Old.kt`;
`files: "a -> b"` moves a path). The sync engine applies the ledger from the commit being synced
(`scripts/white-label/template-migrations.sh`), commits the result before any merge, and merges a
renamed `merge`-owned file against its base under the old path — so a fork's seams follow the names
the template calls and keep their content. Pinned by `tests/customization-surface/template-migrations-test.sh`.

## Precedence

Rules are ordered **most-specific → most-general**, and the **first matching rule
wins**. So the narrow `merge`/`fork` carve-outs are declared *before* the broad
`template` module globs and correctly win over them. The trailing catch-all `**`
rule (`owner: template`, `default: true`) is **template-first** — a directory the template adds
syncs without anyone remembering a rule — but it claims only what the template **ships**: a path
under no sync root (`scripts/white-label/sync-dirs.sh` `SYNC_DIRS` / `SYNC_FILES`, read by the
reader, never duplicated in the contract) resolves `fork`. A fork's own `libs/`, `branding/` or
`maestro/` is therefore fork territory without the template naming it. `--verify` still flags every
template path that reaches the default, so a human classifies it.

Glob syntax: `*` matches within one path segment; `**` matches across segments;
`**/x` matches `x` at any depth (including the repo root).

## The reader / validator — `scripts/customization-surface.sh`

Pure bash + awk (no `yq`/`jq`/`python` dependency — runs on any fork as-is).

```bash
# what owns this path?
scripts/customization-surface.sh resolve cmp-android/src/main/AndroidManifest.xml
#   → cmp-android/src/main/AndroidManifest.xml   merge  (strategy: manifest-union)

# owner of every tracked path
scripts/customization-surface.sh report

# CI canary — fail if any tracked path is unclassified (only matches the default)
scripts/customization-surface.sh verify
#   → ── customization-surface coverage: 1587/1587 classified ──
#   → ✅ every tracked path has an explicit owner
```

Source it as a library too:

```bash
source scripts/customization-surface.sh
cs_resolve_owner    "gradle/libs.versions.toml"   # → merge
cs_resolve_strategy "gradle/libs.versions.toml"   # → catalog-3way
```

## How `scripts/white-label/sync-dirs.sh` uses it

1. **Advisory report** — before the sync, `scripts/white-label/sync-dirs.sh` sources the reader and
   prints the `merge`-owned paths in the sync surface (fully guarded, cannot abort).
2. **3-way merge of `merge`-owned files** — after checking out the upstream copy of
   a directory, for every file that changed between the fork base and upstream and
   resolves to `owner: merge`, `scripts/white-label/sync-dirs.sh` runs `cs_merge <strategy> ours base
   theirs` instead of taking the blind upstream copy:
   - `ours` = fork's current file (`BASE_BRANCH`)
   - `theirs` = upstream file (`temp_branch`)
   - `base` = `resolve_merge_base BASE_BRANCH temp_branch` — **not** `git merge-base` alone

   **The base is the whole promise.** Two fork topologies exist and only one gives git a common
   ancestor: a **forked** repo (clone / `gh repo fork`) shares history, while a **copied** one — the
   template tree dropped into a fresh repo, the common case in practice — has different root commits
   and **zero** shared commits, so `git merge-base` returns empty. With an empty base the engine used
   `ours`, and `git merge-file` then reads every template difference as a clean addition onto an
   untouched base and takes **all of theirs** — reproducing exactly the full-copy that `merge` exists
   to prevent, while printing *"Merged (…) — fork edits preserved"* over the top of the loss.

   `resolve_merge_base()` therefore falls back to **`.template-version#template_sha`**, the commit the
   tree last synced FROM — precisely the ancestor git cannot compute across unrelated histories, and
   what that file's own header always claimed it was for. With neither available (a copied fork's
   first-ever sync) the engine still falls back to `ours` but **warns per file** instead of reporting
   a merge it did not perform. Pinned by `MB-1`–`MB-3` in
   `scripts/product-health/checks/sync-merge-base.sh`.

   `pbxproj-3way` (`**/*.xcodeproj/project.pbxproj`) delegates to
   `scripts/white-label/merge-pbxproj.rb`, because a line merge on an Xcode project is not merely
   worse — it is unusable. A pbxproj is a serialized object graph keyed by 24-hex UUIDs, so adding a
   target rewrites UUID arrays three levels from the object it adds, and `git merge-file` reads
   adjacent array insertions as a conflict. A conflict marker inside a pbxproj makes the project
   **unopenable**, so a human cannot even use Xcode to resolve it. It is tractable because the UUIDs
   are stable across the fork boundary (measured: 33 of 36 objects shared), which turns the text
   merge into a keyed 3-way: UUID arrays union, `buildSettings` merge per key, and the declared
   `FORK_IDENTITY_KEYS` (`DEVELOPMENT_TEAM`, `PRODUCT_BUNDLE_IDENTIFIER`, `CODE_SIGN_*`, …) go to the
   fork when both sides moved. Anything it cannot decide FAILS the merge rather than guessing, and
   the engine's fallback when ruby is unavailable is **keep-ours**, never take-theirs — losing a
   sync is recoverable, losing the fork's targets is not. Uses the `xcodeproj` gem, already resolved
   in `Gemfile.lock` as a fastlane transitive. Pinned by `scripts/product-health/checks/pbxproj-merge.sh`
   against a real captured fixture in `tests/fixtures/pbxproj-3way-canary/`.

   `manifest-union` runs a **semantic** union (union `<uses-permission>` /
   `<uses-feature>` by `android:name`, keeping the template's structural update) so a
   fork's `RECORD_AUDIO` survives a template manifest change. The other strategies
   (`catalog-3way`, `include-union`, `kotlin-3way`, `strings-union`) run `git
   merge-file`, which cleanly unions non-overlapping edits and emits conflict markers
   **only on a true overlap** — surfaced with a `CONFLICT` warning for review, never
   silently shipped.

### The shells are `template`, except the files that carry fork wiring

`build-logic/**` and the application shells — `cmp-android`, `cmp-ios`, `cmp-desktop`, `cmp-web`,
`cmp-shared`, `cmp-navigation` — are template code and resolve `template`: a sync replaces each file in
full. A fork edit to one of them is overwritten; fork build hooks go in `build-logic/**/kotlin/local/**`
(fork). The exception is the handful of shell files that carry fork-specific **platform wiring**, which
has nowhere else to live. `core/<mod>/module-deps.gradle.kts` gives a fork a seam
for dependencies; there is no equivalent seam for an Xcode target, an entitlement, or a plist key.

Measured on a real consumer (2026-09-10), before these files had rows of their own, a full-copy would
have destroyed every one of them silently:

| Path | Fork content a full-copy deletes |
|---|---|
| `cmp-ios/iosApp/Info.plist` | `CFBundleURLTypes` for the Google Sign-In OAuth redirect — no build failure; OAuth just never returns |
| `cmp-ios/iosApp/iosApp.entitlements` | the WidgetKit app-group. The template half (`keychain-access-groups`, required by `core-base/datastore`) is **also** needed — neither side may win, so it must be a union |
| `cmp-ios/iosApp.xcodeproj/project.pbxproj` | an entire app-extension target the template has no concept of |
| `cmp-ios/iosApp/iOSApp.swift` | scene-phase `AppBackground` / `AppForeground` hooks |

Those files — and every `cmp-*/build.gradle.kts`, `MainActivity.kt`, `AndroidManifest.xml`, the
cmp-navigation seams (`*Navigation*`, `RootNav*`, `di/`, `*TabItem*`, `ui/`) and its `strings.xml` —
resolve `merge`, by name. A 3-way takes the template's evolution — the SwiftPM migration landed 5 `XCLocalSwiftPackageReference`
entries plus the `KotlinMultiplatformLinkedPackage` tree this way — while the fork's additions survive,
and a genuine overlap surfaces as a conflict marker rather than a silent loss.

Rows without a `strategy:` use `cs_merge`'s default `git merge-file`; structure-aware strategies sit on
the rows that name one filetype (`plist-union`, `pbxproj-3way`, `manifest-union`, `kotlin-3way`). Binaries under these
directories are carved out `fork` (`res/**`, `Assets.xcassets/**`, `cmp-desktop/icons/**`, the
`cmp-web` resources), and the engine additionally refuses to line-merge a binary.

Every merge goes through one dispatcher, `merge_by_strategy`, and a file the template did not change
since the base (`BASE == THEIRS`) comes back as the fork's copy, untouched, whatever the strategy. A
re-serializing merger would otherwise rebuild it around the template's comments and report the
fork's own lines as dropped. Proof: `tests/customization-surface/template-unchanged-test.sh`.

Four root files are merged but never checked out: `settings.gradle.kts`, `gradle/libs.versions.toml`,
`gradle.properties` and `lib-integrate.properties` (`CONTRACT_ROOT_MERGES` in `sync-dirs.sh`). A full
checkout would clobber the fork's half first. `tests/customization-surface/sync-reach-test.sh` fails
when a `template`/`merge` path is reachable by none of `SYNC_DIRS`, `SYNC_FILES` or
`CONTRACT_ROOT_MERGES`. Feature modules are not sync roots, so their manifests and strings resolve
`fork` like the rest of the module.

**New fork-owned seams are seeded.** A fork-owned file absent at the last-synced template commit is one
the fork never had, so the template's copy lands as its seed, even in a directory that is new too
(`cmp/android/app/fork/ForkAndroidHooks.kt`, which `AndroidApp.onCreate` calls). It is dropped only
when the fork deleted the package it sits in, the nearest directory the template already shipped.
Room schemas and demo code are never seeded. Proof: `tests/customization-surface/seam-seed-test.sh`.

The fork-owned seams inside the shells are `ForkWorkerDeclarations.kt` (cmp-shared) and `registry/**`
(cmp-navigation).

The `template`/`fork` mechanical behaviour (`SYNC_DIRS` + `EXCLUSIONS`) is unchanged;
the contract adds the `merge` path handling that previously didn't exist. Guarded so
forks that don't ship the reader are unaffected.

Proof: `tests/customization-surface/merge-3way-test.sh` — a fork's `RECORD_AUDIO` +
`FOREGROUND_SERVICE_MICROPHONE` survive a template update that adds
`POST_NOTIFICATIONS`, clean, no markers; a true same-line conflict returns rc=1 with
markers.

### Overlay vs strict mirror (`--mirror`)

`git checkout <template> -- <dir>` is an **overlay**: it writes every file the template has and never
removes a file only the fork has. `propagate_template_deletions` removes files the template itself
deleted since the last sync (when the fork never touched them); nothing removed files the template
NEVER shipped. A 2026-10-31 audit of 10 forks found fork code living permanently inside
template-owned roots — components dropped into `kpt/core/designsystem/component/`, whole packages in
`core-base/` — that a sync never reported and never reconciled.

`sync-dirs.sh --mirror` makes template-owned roots a strict mirror: after the copy lands, every file
the fork has under the root, the template does not, and the contract resolves **exactly** `template`
is removed. Never touched: `is_excluded` paths (fork-owned, `**/demo/**`, the `core/store` seam,
`schemas/`), `merge`-owned and `demo-showcase` paths, and anything listed in the fork-owned
**`.template-sync-holds`** (`path<TAB>reason` — an extra proposed upstream waits there until the
template adopts it). `--mirror` is opt-in: `/kmp-project-template-sync` first classifies each extra —
adopt upstream, move to a fork package, or delete — so nothing is removed unreviewed. Proof:
`tests/customization-surface/mirror-test.sh`.

### `.template-version` advances only on a COMPLETE sync

The marker is the 3-way base for the next sync, so it must state what the tree fully holds. A failed
`sync_directory` / `sync_file` used to be ignored and the marker stamped anyway; two audited forks
recorded the latest base while hundreds of their `core-base` files matched OLDER template versions.
Every failure is now recorded, and if any occurred the engine names them, leaves the previous marker,
and exits before committing.

### A conflicted merge never reaches a commit

A `merge`-owned file whose 3-way merge left markers used to be reported ("resolve markers before
committing the sync") and then committed by the same run. A dry run on a real fork produced a sync
commit with `<<<<<<<` in 19 files — build-logic plugins, the navigation shell, an Xcode file list.
Conflicts are now recorded; every path still synced, so `.template-version` is written, but the engine
exits **3** before committing and lists the files. Resolve them on the sync branch, then commit.

## Recommended CI wiring

Add a coverage gate so a new template file can never land unclassified (and thus
never silently clobber a fork later):

```yaml
- name: Verify customization-surface coverage
  run: bash scripts/customization-surface.sh verify
```

## Roadmap (follow-ups)

1. ~~Merge engine adoption~~ — **done**: `scripts/white-label/sync-dirs.sh` 3-way merges `merge`-owned
   files (`manifest-union` semantic + `git merge-file` for the rest). Remaining:
   fold the `fork`/`template` decisions into the same contract lookup and retire the
   hand-maintained `EXCLUSIONS` map (the reader can already answer `fork` → skip /
   `template` → copy; `EXCLUSIONS` becomes redundant).
2. **`syncForkConfig` alignment** — the plugin reads the same contract to know
   which generated files it owns vs must not clobber.

## Editing the contract

- A new **template-owned** module → add a `template` rule (and, if forks extend a
  specific file inside it, a narrower `merge`/`fork` rule *above* it).
- A new **fork-branded** path → add a `fork` rule.
- A file both sides edit → add a `merge` rule with a `strategy:`.
- Always run `scripts/customization-surface.sh verify` after editing.
