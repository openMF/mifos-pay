<!-- deployment/BOOTSTRAP.md — canonical fork-onboarding guide. -->
<!-- AC68 + AC84 + AC101 + AC108 + AC113 (fastlane-modernization sub-plan 14). -->

# deployment/BOOTSTRAP.md — Fork onboarding guide

Welcome. This is the **canonical** onboarding guide for getting a fresh fork of
`kmp-project-template` to a working deploy. Two paths are supported; pick the
one that matches your team's secret-management posture.

> ⚠️  **Seeing `/release: command not found`?** You're on the OSS-fork path
> (you don't have the `claude-product-cycle` framework cloned). That's
> expected — use `cd deployment && bundle exec fastlane <lane>`
> instead. See **Path A** below for the canonical OSS-fork commands.
>
> Optional convenience: `source deployment/_shared/scripts/release-fallback.sh`
> in your shell rc; future `/release` typos print this redirect.

---

## Which path should I pick? (decision matrix)

| Question                                          | Path A (manual) | Path B (vault) |
|---------------------------------------------------|:---------------:|:--------------:|
| Team size                                         | 1-3 contributors | 4+ contributors |
| Secret rotation cadence                           | Annual or less   | Quarterly or more |
| Multi-fork secret sharing                         | No               | Yes (org vault) |
| Already using framework `/secrets *`?             | No               | Yes |
| Compliance audit requirement (SOC2/ISO27001)      | Optional         | **Required** |
| Time to first release                             | ~15 min          | ~45 min |

Pick **Path A** for a personal fork or small team — minimum ceremony, secrets
materialize directly into `secrets/` from your local copies, no framework
clone required.

Pick **Path B** for any team or organisation — secrets live in a private
SOPS+age encrypted vault repo and propagate via `/secrets *`. Requires the
framework (`claude-product-cycle`) cloned to drive the pipeline.

---

## Path A — OSS developer (manual mode) — DEFAULT

Forks of `kmp-project-template` typically do NOT have the
`claude-product-cycle` framework cloned, so `/release` is **NOT** the entry
point for Path A. Use these primary invocations instead:

```bash
# Local deploy (developer's machine — runs Fastlane directly)
cd deployment && bundle exec fastlane android deployInternal
cd deployment && bundle exec fastlane ios release
bash deployment/web/cloudflare-pages/script.sh   # non-Fastlane targets ship script.sh

# CI deploy (GitHub Actions on push or workflow_dispatch)
gh workflow run release-android.yml -f target=playstore-internal
gh workflow run release-ios.yml     -f target=testflight
gh workflow run release-web.yml     -f target=cloudflare-pages
gh workflow run release-desktop.yml -f target=github-release
```

`/release` is a maintainer convenience wrapper documented in Path B; it adds
vault preflight + PROMOTION_LOG audit but is **NOT** required for Path A.

### Path A — 7 steps

1. **Fork + clone** the template.
2. **Set deploy mode**:

   ```yaml
   # PROJECT_CONFIG.yaml
   deployment:
     dual_mode_default: manual
   ```

3. **Generate keystores** (Android):

   ```bash
   ./keystore-manager.sh generate
   ```

4. **Populate `secrets/live/`** by copying `secrets/sample/` and filling in
   real values. Every file in `secrets/sample/` ships a magic-marker
   placeholder (`# CLAUDE-PLACEHOLDER` text-files, `CLAUDE-PLHLD-v1\0` binaries)
   so it's unambiguous what's safe to commit and what is not.

   The 8 baseline files most forks need:

   | secrets/sample/                                               | Purpose                                            |
   |---------------------------------------------------------------|----------------------------------------------------|
   | `firebaseAppDistributionServiceCredentialsFile.json`          | Firebase App Distribution (Android + iOS)          |
   | `playStorePublishServiceCredentialsFile.json`                 | Play Console upload                                |
   | `AuthKey.p8`                                                  | App Store Connect / TestFlight upload              |
   | `APNAuthKey.p8`                                               | Apple Push (if used)                               |
   | `match_ci_key` + `match_ci_key.pub`                           | Fastlane Match SSH (iOS code signing)              |
   | `gradle/fork.properties` (from `gradle/fork.properties.template`)  | Non-secret identity/metadata (`apple.team.id`, contacts, URLs) |
   | `cloudflare/CLOUDFLARE_API_TOKEN`                             | Cloudflare Pages deploy (web)                      |

5. **Wire CI** by pasting the same files into GitHub Actions repo secrets
   (base64-decoded per the per-target `workflow-snippet.yml` manual flavor).
   Each `deployment/<platform>/<target>/secrets-needs.yaml` lists the exact
   env-var names the lane expects.

6. **Open a PR** — confirm PR Checks 6/6 green (Build iOS App / Build Android
   App / Build Desktop / Build Web App / Static Analysis / Test Coverage).

7. **Run a deploy** — pick a target and invoke either `bundle exec fastlane`
   (local) or `gh workflow run` (CI). Confirm the build artefact lands at the
   target store.

### Path A — example: Android Firebase App Distribution

```bash
# Local
cd deployment && bundle exec fastlane android deployReleaseApkOnFirebase

# CI
gh workflow run release-android.yml -f target=firebase-app-distribution
```

Required secrets (from `deployment/android/firebase-app-distribution/secrets-needs.yaml`):
- `FIREBASE_APP_ID_ANDROID_PROD` (env var, pasted as repo secret)
- `firebaseAppDistributionServiceCredentialsFile.json` (file, materialized
  from base64 via the workflow-snippet manual flavor)

### Path A — example: iOS TestFlight

```bash
# Local (requires Xcode — SwiftPM/XCFramework)
cd deployment && bundle exec fastlane ios beta

# CI
gh workflow run release-ios.yml -f target=testflight
```

Required secrets (from `deployment/ios/testflight/secrets-needs.yaml`):
- `MATCH_GIT_PRIVATE_KEY` (env var, base64 of `match_ci_key`)
- `MATCH_PASSWORD` (env var, paste from your team's password manager)
- `AuthKey.p8` (file)
- `APP_STORE_CONNECT_API_KEY_ID` (env var)
- `APP_STORE_CONNECT_API_ISSUER_ID` (env var)

### iOS Certificate Lifecycle — `renewCerts`

Apple Distribution certificates expire after **1 year**. When one expires,
every iOS deploy lane fails with `"Your certificate '…' is not valid"`. The
`renewCerts` lane handles the full renewal cycle:

1. Revoke expired (or soonest-expiring) cert from Apple portal via ASC API.
2. Purge the old cert files from the Match git repo (`ios-provisioning-profile`).
3. Run `match adhoc` + `match appstore` to create a fresh cert and push it.

**Run it:**
```bash
# From repo root
(cd deployment && bundle exec fastlane ios renewCerts)
```

**When to run:**
- A deploy fails with `"not valid, please check end date"` → run immediately.
- Monthly CI schedule (`openMF/ios-provisioning-profile cron (cert-renewal.yml)`) handles the
  proactive case automatically on the 1st of every month.

#### Colleague setup (one-time per machine)

Everyone who needs to run `renewCerts` or any iOS lane locally needs:

| What | Where | How to get it |
|------|-------|---------------|
| SSH private key with write access to `openMF/ios-provisioning-profile` | `secrets/live/apple/match/match_ci_key` | Framework vault: `/secrets pull`; or request from team lead |
| `MATCH_PASSWORD` | `secrets/live/apple/match/.match_password` | Same vault / team lead |
| `KEYCHAIN_PASSWORD` | `secrets/live/apple/match/keychain_password` | Your macOS login keychain password |
| `CERTIFICATES_PASSWORD` | `secrets/live/apple/match/certificates_password` | Same as `MATCH_PASSWORD` in standard setups |
| ASC API key | `secrets/live/apple/appstore/AuthKey.p8` + `key_id` + `issuer_id` | Framework vault: `/secrets pull` |

Once secrets are in place, clone the Match repo once so subsequent runs
are fast (`git fetch + reset` instead of a full clone):

```bash
# From repo root — one-time setup per machine
GIT_SSH_COMMAND="ssh -i secrets/live/apple/match/match_ci_key -o StrictHostKeyChecking=no" \
  git clone --depth 1 git@github.com:openMF/ios-provisioning-profile.git \
  secrets/live/apple/match/ios-provisioning-profile
```

> The `secrets/` directory is gitignored — the clone never enters source control.
> The `renewCerts` lane auto-clones on first run if the directory is absent, so
> this manual step is optional but speeds up subsequent runs.

**GitHub Actions** — the `cert-renewal.yml (openMF/ios-provisioning-profile)` workflow sets up all secrets
and clones the Match repo automatically. Required repo secrets:
`MATCH_GIT_PRIVATE_KEY`, `MATCH_PASSWORD`, `CERTIFICATES_PASSWORD`,
`KEYCHAIN_PASSWORD`, `APPSTORE_KEY_ID`, `APPSTORE_ISSUER_ID`, `APPSTORE_AUTH_KEY`.

---

### Path A — example: Web GitHub Pages

```bash
# Local (publishes to gh-pages branch)
bash deployment/web/github-pages/script.sh

# CI
gh workflow run release-web.yml -f target=github-pages
```

Required secrets: none (uses `GITHUB_TOKEN` provided by GHA automatically).

---

## Path B — Framework maintainer (vault mode)

Path B assumes the `claude-product-cycle` framework is cloned and you have
SOPS + age installed locally. All secrets live in the org vault repo and
materialize via `/secrets pull` — never typed into chat, never committed to
the consumer repo.

### Path B — 5 steps

1. **Ensure `SOPS_AGE_KEY` available** in your shell (e.g. via macOS Keychain
   per `core/scripts/secrets-keychain-load.sh`, or 1Password CLI, or local
   `~/.config/sops/age/keys.txt`).

2. **Adopt the project into the vault** (one-time per fork):

   ```bash
   /context-start mifos-x/kmp-project-template
   /secrets adopt --apply
   ```

   This registers the aliases listed in `secrets-manifest.yaml` (repo root — GENERATED by
   `./gradlew syncForkConfig` from `deployment/**/secrets-needs.yaml`) in `core/registries/SECRETS_ALIAS_REGISTRY.yaml`
   (kpt-* namespace per RULE-SECRETS-ALIAS-REGISTRY-001).

3. **Pull secrets** — materializes encrypted vault rows into `secrets/` and
   `local.properties` at canonical filesystem locations:

   ```bash
   /secrets pull
   ```

4. **Verify the manifest** — confirms every alias has a matching vault row
   and every materialization target was written:

   ```bash
   /secrets verify --required-for android-firebase-app-distribution
   /secrets verify --required-for ios-testflight
   ```

5. **Deploy** via `/release`, which auto-loads the manifest, runs the
   capability preflight, dispatches the per-target lane, and appends a
   12-field row to `deployment/PROMOTION_LOG.yaml`:

   ```bash
   /release android firebase-app-distribution
   /release ios testflight
   /release web github-pages
   ```

### Step 5b (Path B only) — Enable vault-mode in CI

For maintainer-grade CI vault-mode (auto-decrypt secrets from the vault on
each runner), configure two repo secrets via `/secrets sync-to-ci`:

1. `SOPS_AGE_KEY` — your team's age private key (used by sops to decrypt)
2. `VAULT_DEPLOY_KEY` — SSH private key with read access to the vault repo
   (e.g. `mobilebytesensei/secrets-vault-mifos-x`)

The `workflow-snippet.yml` vault-mode flavor in every per-target dir detects
`secrets.SOPS_AGE_KEY` presence, clones the vault repo via
`webfactory/ssh-agent@v0.9.0` using `VAULT_DEPLOY_KEY`, then runs
`bash core/scripts/secrets-pull.sh --required-for <capability>` to materialize
secrets to `secrets/` paths on the runner.

OSS forks using Path A skip this step entirely — manual mode uses GitHub
Actions repo secrets (base64-decoded per the manual flavor) and never touches
the vault.

See also: `docs/guides/secrets/SECRETS_MANAGEMENT_GUIDE.md` for vault
topology + rotation policy, and `/secrets sync-to-ci` documentation for
propagation modes (`act-local` default, `workflow`, `script`).

### Path B — example: Android Firebase App Distribution

```bash
/release android firebase-app-distribution
```

What `/release` does:
- Reads `deployment/DEPLOYMENT_MANIFEST.yaml` → finds the
  `android-firebase-app-distribution` target row.
- Reads `deployment/android/firebase-app-distribution/secrets-needs.yaml` →
  computes the capability id (e.g. `android-firebase-app-distribution`).
- Runs `/secrets verify --required-for android-firebase-app-distribution`.
  PASS → proceeds; FAIL → halts with the missing alias list.
- Dispatches `cd deployment && bundle exec fastlane android
  deployReleaseApkOnFirebase`.
- On success, appends a row to `PROMOTION_LOG.yaml` with timestamp, target,
  mode (`vault`), git-sha, fastlane exit code, lane.rb sha, secrets-needs
  sha, capability id, and outcome.
- Emits `ACTIVITY_LOG` events `release:start` + `release:complete`.

### Path B — example: iOS TestFlight

```bash
/release ios testflight
```

Same flow as Android — preflight + dispatch + audit. Match repo secrets are
pulled from the vault (no `MATCH_GIT_PRIVATE_KEY` env var to paste manually).

### Path B — example: Web GitHub Pages

```bash
/release web github-pages
```

The web targets typically need fewer secrets (or none for GH Pages), but the
same preflight runs — `/secrets verify --required-for web-github-pages` is a
no-op when the target's `secrets-needs.yaml` is empty.

---

## Migration from legacy `fastlane/`

The legacy `fastlane/` and `fastlane-config/` directories at the project root
are **still present** at the close of `template_version: "2.6.0"`. The
fastlane-modernization epic is structurally complete (deployment/ tree fully
populated; /release dispatcher updated; secrets pipeline migrated) but the
legacy-tree **deletion** is gated on upstream signals:

| Signal | Required state | Tracked at |
|---|---|---|
| `openMF/mifos-x-actionhub` tagged `v2.0.0` | published | upstream actionhub repo |
| `mifos-x-actionhub-publish-{desktop-app,web-publish}-kmp` sub-actions tagged `v2.0.0` | published | upstream actionhub-publish-* repos |
| Consumer pin in `.github/workflows/pr-check.yml` bumped to `@v1.0.14` / `@v2.0.0` | green PR Checks | local pr-check workflow |

The 4 staged upstream PR bundles live under
`.claude-runtime/external-prs/fastlane-modernization-{12,13,14}-*/` for the
admin to push. Once the upstream tags land, a follow-up PR (the
"fastlane-deletion" PR) will:

1. **Uncomment** per-target `import_from_git`-style imports in
   `deployment/Fastfile` — currently the per-target Fastfiles are commented
   so the legacy `fastlane/Fastfile` still serves the dispatcher fallback.
2. **Delete** the entire `fastlane/` directory (`git rm -r fastlane/`).
3. **Delete** the entire `fastlane-config/` directory (`git rm -r
   fastlane-config/`).
4. **Update** `docs/setup/FORK_QUICKSTART.md` to remove any residual references to
   `fastlane/` paths.
5. **Update** `CLAUDE.md` `Architecture` block to drop the `fastlane/` row.
6. **Re-run** `./gradlew check spotlessCheck detekt dependencyGuard` and the
   6 PR Check jobs to confirm green on the post-deletion tree.

Until then, the per-target deployment dirs are **functionally complete** —
direct `cd deployment && bundle exec fastlane <lane>` invocations
work against the new tree right now. The legacy `fastlane/` block exists
only as a fallback for unmigrated callers.

### Why no `.env.local.example`?

Forks coming from an earlier `template_version` will notice `.env.local.example`
has been deleted at `2.6.0`. This is **intentional** per RULE-SECRETS-VAULT-001
SV18 (the no-`.env*`-in-vault-mode rule).

The previous `.env.local.example` carried two intents:
1. The **`FRED_API_KEY` placeholder** for the B7 Interest Rates feature.
2. **Documentation** that "fork operators paste secrets here".

Both intents now migrate:

- **FRED_API_KEY** → per-developer alias. Run
  `/secrets request mifos_x_fred_api_key` from a project-bound session;
  the framework will open a vault PR proposing the new alias row. The
  materialized value lands in `local.properties` (KMP ecosystem) on
  `/secrets pull` and `FredApiConfig` reads it via
  `BuildKonfig.FRED_API_KEY` (or `System.getenv("FRED_API_KEY")` as fallback
  in manual mode).
- **Documentation** → this file. The `secrets/sample/` → `secrets/live/` layout +
  Path A walkthrough above replaces the `.env.local.example` comment block.

For OSS forks running Path A who don't want vault ceremony:
- Non-secret identity/metadata (team ID, contact info, URLs) goes in `gradle/fork.properties`
  (copy from `gradle/fork.properties.template`, which is committed).
- Secret values (API keys, passwords, certificates) are dropped as per-value files under
  `secrets/live/<platform>/...` (all gitignored). The pre-commit secrets guard
  (`/secrets install-hook`) refuses any `.env*` file staged outside the vault
  repo, which closes the legacy-leak surface.

---

## Reference

- `deployment/DEPLOYMENT_MANIFEST.yaml` — every supported target row.
- `deployment/PROMOTION_LOG.yaml` — append-only deploy audit (12-field schema).
- `deployment/_shared/scripts/` — shared bootstrap + preflight scripts.
- `deployment/_shared/lib/` — shared Ruby helpers consumed by per-target
  `lane.rb` files.
- `deployment/**/secrets-needs.yaml` — per-target secrets, GENERATED from `secrets/LAYOUT.yaml` by
  `./gradlew syncForkConfig` (author only `needs:`).
- `secrets-manifest.yaml` — the vault manifest, GENERATED by `./gradlew syncForkConfig` from those
  files. Do not hand-edit it.
- `secrets/sample/` — Path A schema-as-code mirror with magic markers (copy to `secrets/live/` and fill real values; `build-secrets` resolves paths from `secrets/LAYOUT.yaml`).
- `docs/guides/secrets/SECRETS_MANAGEMENT_GUIDE.md` — vault topology.
- `RULE-DEPLOYMENT-MANIFEST-001` — manifest schema enforcement (DM1-DM8).
- `RULE-SECRETS-VAULT-001` — vault-mandatory policy (SV1-SV31).
- `RULE-SECRETS-ALIAS-REGISTRY-001` — unified-alias-namespace contract.
