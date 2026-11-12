# `deployment/` — DEVELOPMENT

Internal-contributor guide to the release/deployment machinery — 18 targets across 5 platforms
(Android/iOS/macOS/Desktop/Web). For **using** an existing lane to ship a fork's build, see
[`BOOTSTRAP.md`](BOOTSTRAP.md) (Path A manual-mode / Path B vault-mode fork onboarding) and
[`docs/deployment/FASTLANE_CONFIGURATION.md`](../docs/architecture/tree/deployment/fastlane-configuration.md) —
this doc covers adding/modifying the machinery itself, not consuming it.

## Purpose

Every deploy target is a self-contained directory `deployment/<platform>/<target>/`. There is no
top-level per-platform Fastfile with inline lanes — `deployment/Fastfile` is a pure **import
delegator** (`Dir["./*/**/lane.rb"].each { |f| import f }`); all lane bodies live in the per-target
directory. `DEPLOYMENT_MANIFEST.yaml` is the template-owned catalog of every target that exists;
a fork's actual enable/tier/confirm choices live separately in `app-profile/deploy-targets.yaml`
(fork-owned — not in this directory) and override the manifest's defaults at resolve time.

## Local invocation

```bash
# Fastlane-based targets (cd into deployment/ — no --fastlane-dir flag exists in Fastlane 2.235.0):
(cd deployment && bundle exec fastlane android deployReleaseApkOnFirebase)
(cd deployment && bundle exec fastlane ios deploy_on_firebase)
(cd deployment && bundle exec fastlane mac desktop_testflight)

# Bash-script-based targets (Linux/Windows/Microsoft Store/GitHub Pages — no Fastlane involved):
FLAVOR=prod TAG=v2026.06.04 STAGE=stable bash deployment/desktop/linux-deb/script.sh
bash deployment/web/gh-pages/script.sh

# Manifest coherence check (pure bash, no Gradle/network/YAML lib):
bash scripts/deployment-manifest-validate.sh
```

Each target's `config.yaml#runners.local.command` is the authoritative invocation string — read it
before guessing at a lane/script name.

## Two lane mechanisms — know which one a target uses

| Mechanism | Used by | Entry file |
|---|---|---|
| **Fastlane lane** (Ruby) | Android (firebase/play-*), iOS (firebase/testflight/appstore), macOS (dmg-notarized/mac-app-store), desktop sync-listing | `lane.rb`, imported by `deployment/Fastfile` |
| **Bash script** | Desktop unsigned/signed (linux-deb/windows-exe/msi-signed/macos-dmg-unsigned), Microsoft Store, all of Web (gh-pages/cloudflare-pages/netlify/vercel) | `script.sh`, invoked directly, not through Fastlane |

Don't assume every target has a `lane.rb` — several intentionally bypass Fastlane entirely (no
signing/store-API surface to justify the dependency). Check which files actually exist in the
target directory before adding logic.

## Per-target directory contract

```
deployment/<platform>/<target>/
├── lane.rb | script.sh     ← the executable entry point (see table above)
├── config.yaml              ← target/platform/tier/output_path/runners/requires_confirm
├── secrets-needs.yaml       ← dual-mode (vault alias + manual placeholder) schema v1.0
├── README.md                ← operator-facing notes for this target
└── workflow-snippet.yml     ← the CI-side invocation snippet (most targets)
```

`config.yaml#runners` declares both the `local.command` and `ci.workflow_snippet` — the two must
stay in sync (same underlying lane/script). `secrets-needs.yaml#vault_aliases[]` lists the
canonical `secrets/live/**` paths the target reads and how they materialize (`format: file|env`,
`via: copy|export`); `manual_inputs[]` is the parallel dual-mode fallback for OSS forks without
vault access (`gha_secret_var` names the matching plain GitHub Actions secret).

## Shared infrastructure (`_shared/`)

- **`config.rb`** — `FastlaneConfig` module (signing config, Firebase config resolution,
  `get_android_signing_config`, `get_firebase_config`), `AppProfile`/`ForkIdentity` modules that
  read fork identity out of `app-profile/`. Imported first by `Fastfile` so every `lane.rb` can use
  its helpers (`sanitize_options`, `generateReleaseNote`, `buildAndSignApp`) without re-requiring it.
- **`variant_resolver.rb`** — `VariantResolver.resolve(flavor:, build_type:)` derives the Gradle
  task name + APK output path **by convention** from `cmp-shared/build/kmp-flavors/variants.json`
  — the mechanism that let `deployApkOnFirebase(flavor:, build_type:)` collapse what used to be
  two hardcoded per-flavor lanes into one parameterized lane (see `android/firebase/lane.rb`).
- **`before_all.rb`** — regenerates `deployment/**/metadata` (DERIVED, gitignored) from
  `app-profile/` via `./gradlew syncForkConfig` before any lane runs; also restores the small
  allow-list of tracked files (`cmp-ios/iosApp.xcodeproj/project.pbxproj`,
  `cmp-ios/iosApp/Info.plist`) that Fastlane's `match`/`gym`/
  version steps mutate during a build, so a deploy never leaves dirty tracked source behind for
  `git-session-commit` to sweep in.
- **`listing_sync.rb`** — drift-checked store-listing sync every store-deploy lane calls
  (content-hashes the derived `metadata_path`, pushes only on change; always syncs on CI where
  there's no per-machine cache). Fixes the historical gap where a Play *promotion* lane
  (`skip_upload_metadata: true`) shipped a stale listing after a text-only app-profile change.
- **`scripts/`** — `keystore-manager.sh` (Android keystore lifecycle check), `manual-preflight.sh`
  (Path A manual-mode secrets check), `materialize-{android,ios,mac}-secrets.sh`,
  `gh-release-stage.sh` (prerelease/beta/stable promotion-ladder flags on a GH Release,
  called by the desktop `script.sh` targets), `promotion-log-append.sh`.

## Generated vs hand-authored

- **Hand-authored, commit these**: `lane.rb`/`script.sh`, `config.yaml`, `secrets-needs.yaml`,
  per-target `README.md`, `workflow-snippet.yml`, `_shared/**`, `DEPLOYMENT_MANIFEST.yaml`.
- **Generated, gitignored, never hand-edit**: `deployment/**/metadata/**` (store listing text +
  screenshots — materialized from `app-profile/` by `./gradlew syncForkConfig`, refreshed by
  `before_all.rb` on every lane run), `deployment/fastlane/.listing_sync_state.json` (per-machine
  drift cache).
- **Fork-owned, lives outside this directory**: `app-profile/deploy-targets.yaml` (which catalog
  targets are enabled + at what tier), `secrets/live/**` (actual secret material).

## How to add a new deployment target

1. Add a `canonical_name` row under the right platform in `DEPLOYMENT_MANIFEST.yaml` (tier 1 =
   default-on unsigned/basic; tier 2 = signed/notarized/alt-store, default off).
2. Create `deployment/<platform>/<target>/` with `config.yaml` + `secrets-needs.yaml` +
   `README.md`, and either `lane.rb` (if it needs Fastlane's signing/store-upload actions —
   `firebase_app_distribution`, `upload_to_play_store`, `upload_to_testflight`, `deliver`) or
   `script.sh` (if it's a plain build-and-upload with no Fastlane action available, e.g. a
   GitHub Release upload via `gh`).
3. If `lane.rb`: it is auto-picked up by `deployment/Fastfile`'s `Dir["./*/**/lane.rb"]` glob —
   no import to add by hand. Reuse `FastlaneConfig`/`VariantResolver` helpers from `_shared/`
   rather than re-deriving signing config or output paths.
4. Add `workflow-snippet.yml` mirroring the local `runners.local.command`, and wire
   `config.yaml#runners.ci.workflow_snippet` to it.
5. Run `bash scripts/deployment-manifest-validate.sh` — it asserts a fork's
   `app-profile/deploy-targets.yaml` only references `canonical_name`s that exist in the catalog
   and that every enabled target declares `tier ∈ {1,2}`.

## Related

- [`docs/deployment/FASTLANE_CONFIGURATION.md`](../docs/architecture/tree/deployment/fastlane-configuration.md) — Fastlane config reference.
- [`docs/release/ONBOARDING_CHECKLIST.md`](../docs/architecture/tree/deployment/release-onboarding.md) — release-manager onboarding.
- [`docs/ios/IOS_DEPLOYMENT.md`](../docs/architecture/tree/deployment/ios/deployment.md), [`IOS_SETUP.md`](../docs/architecture/tree/deployment/ios/setup.md), [`IOS_DEPLOYMENT_CHECKLIST.md`](../docs/architecture/tree/deployment/ios/checklist.md) — iOS-specific signing/deploy detail.
- [`BOOTSTRAP.md`](BOOTSTRAP.md) §"iOS Certificate Lifecycle — `renewCerts`" — Fastlane Match cert rotation.
