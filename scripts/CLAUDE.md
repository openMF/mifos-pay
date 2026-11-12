# Bash Scripts — Automation & Setup

**Last Updated:** 2026-09-10
**Tracked scripts:** 34 (excluding `scripts/product-health/`, which documents itself)

[← Back to Main](../CLAUDE.md)

> **This file is a map of what EXISTS.** It was rewritten 2026-09-10 after an audit found it
> describing a layout the tree had left behind: 8 scripts documented as current no longer existed
> (`deploy_firebase.sh`, `deploy_testflight.sh`, `deploy_appstore.sh`, `check_environment.sh`,
> `check_file_env_keys.sh`, `check-commit-signing.sh`, `ensure_base64.sh`,
> `fix-detekt-permissions.sh`), 22 that do exist were never mentioned, and every path was written
> flat (`fork-init.sh`) when the scripts had long since moved into subdirectories
> (`scripts/white-label/fork-init.sh`). A doc that names a script which is not there costs more than
> no doc — it sends a reader looking for something that was deliberately removed.
>
> The deploy scripts are gone on purpose: deployment runs through **Fastlane lanes**, not shell
> wrappers. See [`deployment/BOOTSTRAP.md`](../deployment/BOOTSTRAP.md).

---

## Layout

```
scripts/
├── ruby-exec.sh                    the ONE way this repo reaches Ruby/Bundler
├── customization-surface.sh        reader + validator for customization-surface.yaml
├── remove-demo.sh                  strip the demo showcase, leaving a clean fork
├── deployment-manifest-validate.sh validate the deploy-target ownership split
├── configure-release-environments.sh  create + protect the GitHub Environments releases bind to
├── verify-demo-convention.sh       static gate for the demo⇄framework separation convention
├── verify-2-4-0.sh                 per-target main+test compile matrix for Kotlin 2.4.0
│
├── white-label/    the white-label lifecycle (see white-label/README.md)
├── ios/            iOS setup + verification
├── ci/             pre-commit / pre-push / CI-parity gates
├── secrets/        platform-wise secrets toolkit
├── store/          store listing + screenshots
├── _shared/        shared bash helpers
└── product-health/ the 30-check health suite (self-documenting)
```

---

## `ruby-exec.sh` — read this before writing any script that shells out to Ruby

**Every** bundler call goes through this. It is not a convenience wrapper; it closes a trap that
produces a misleading error.

rbenv works by PATH interception. In a shell without `eval "$(rbenv init -)"` — a cron job, a
GUI-launched terminal, a CI step that reset PATH — nothing intercepts, `ruby` falls through to
macOS's `/usr/bin/ruby` 2.6.10, and the pinned interpreter is never used. The gems were installed
for the pinned version, so `bundle exec` then dies inside rubygems' `activate_bin_path` with an
error about **gem activation**. The gems are fine; the interpreter is wrong.

```bash
. "$REPO_ROOT/scripts/ruby-exec.sh"
ruby_exec   script.rb [args...]              # run a ruby script under the pinned interpreter
ruby_bundle <app-root> -- exec fastlane …    # run bundler in an app root, pinned
ruby_exec_report                             # which interpreter was chosen, and why
```

It resolves `$(rbenv root)/versions/<pin>/bin/ruby` **directly**, so it works whether or not shims
are on PATH. The pin comes from `.ruby-version` — never a literal, so bumping the pin cannot leave a
caller asking for a stale version. If the pinned version is not installed it provisions it (framework
helper, or `RUBY_EXEC_AUTO_INSTALL=1`) and otherwise **fails loudly with the exact command** rather
than silently running the wrong ruby.

**Enforced:** `RT-9` in `product-health/checks/ruby-toolchain-coherence.sh` fails the build if any
tracked `*.sh` invokes `bundle` directly. The match is anchored to command position — start of line,
or after `;` `&` `|` `(` `&&` `||` — and a shell comment is exempt. **A quoted string is not**: the
matcher sees text, so `echo "(cd deployment && bundle exec …)"` trips it just as a real invocation
would. That is the right outcome rather than a false positive, because an instruction telling a human
to type a bare `bundle exec` teaches the very failure this file opens with. Print the `ruby_bundle`
form instead, as `scripts/ios/setup_ios_complete.sh` does.

**`RT-8`** in the same file asks whether *this repo* can reach the pinned interpreter — it runs the
resolver and checks the answer. It does **not** ask whether the `ruby` on your PATH happens to be the
pinned one: that is a property of your shell, not the repo, and every script goes through the
resolver anyway. So `product-health` passes on a shell that never ran `rbenv init`, and prints a note
(visible when you run the check directly) reminding you that a **hand-typed** lane is the one path
that still needs the shell fixed.

---

## `white-label/` — the fork lifecycle

| Script | Purpose |
|---|---|
| `doctor.sh` | **the ONE entry**: setup + verify + sync (RULE-WHITE-LABEL-DOCTOR-001) |
| `derive.rb` | `app-profile/` → `gradle/fork.properties` (the single SoT projection) |
| `fork-init.sh` | package-name / namespace customization |
| `firebase.sh` | Firebase project + app registration |
| `keystore.sh` | keystore generation and secrets management |
| `sync-dirs.sh` | the template sync engine (`owner: template`, self-propagating) |

`setup-project.sh` at the repo root is a thin redirect to `doctor.sh` (`--legacy` for the old flow).

Full detail: [`white-label/README.md`](white-label/README.md).

---

## `ios/` — setup and verification

| Script | Purpose |
|---|---|
| `setup_ios_complete.sh` | complete iOS deployment setup wizard (Team ID, App Store Connect API, Match SSH key) |
| `setup_apn_key.sh` | Apple Push Notification key configuration |
| `verify_ios_deployment.sh` | 70+ checks across prerequisites, Match, signing, Firebase, security |
| `verify_apn_setup.sh` | APN key presence, permissions, format |
| `check_ios_version.sh` | explains Gradle → Firebase → App Store version sanitization |

iOS uses **SwiftPM / XCFramework** (`cmp-ios/Package.swift` + the Xcode Run-Script phase). There is
no CocoaPods toolchain.

---

## `ci/` — gates

| Script | Purpose |
|---|---|
| `pre-commit.sh` · `pre-push.sh` | thin git hooks (installed by `./gradlew installGitHooks`) — both run `kotlin-checks.sh` |
| `kotlin-checks.sh` | branch guard + the style checks on the CHANGED files only (pre-commit formats and re-stages; pre-push checks); dependencyGuard only when a build file changed; a tree that passed is not re-checked. **On demand, CI's `spotlessCheck detekt` without Gradle:** `changed [<base>]` (this branch vs `origin/dev`, ~4 s for a 30-file PR) · `module <dir…>` · `all` (~35–60 s for the whole repo, vs ~13 min in Gradle) — check-only, `--fix` applies |
| `kotlin-conform.sh` | the style rules without Gradle, in seconds: Kotlin + XML licence headers (incl. Spotless's template-code exemption) + ktlint at Spotless's pinned version + Detekt with `config/detekt/detekt.yml`. Scope mirrors Gradle — only modules whose build file applies a Spotless convention (so `tools/*-ksp` is skipped, as Gradle skips it). Used by the git hooks, the sync engine (on its 3-way merge results) and the framework's write-time hook — one implementation everywhere. `--check` reports instead of fixing. Canary: `product-health/tests/kotlin-checks-canary` |
| `ci-equivalent-check.sh` | local CI parity for the consumer |
| `check-secrets-resolver.sh` | Phase-7 guard: the build-secrets resolver stays the single reader |
| `verify-dynamic-flavor-propagation.sh` | AC-9 canary — a flavor added via `LocalFlavors` auto-derives everywhere |
| `verify-workflow-token-scope.sh` | pre-push gate on `.github/workflows/*` token scope (AC-11) |

---

## `secrets/` — platform-wise toolkit

| Script | Purpose |
|---|---|
| `setup-secrets.sh` | interactive, platform-wise secret setup |
| `secrets-status.sh` | what is set up vs missing, per platform |
| `sync-secrets-to-github.sh` | push secrets to GitHub repository secrets |
| `_lib.sh` | shared helpers |

Never hand-write a secret into `secrets/live/` — that path is fork-owned, gitignored, and excluded
from sync.

---

## `store/` — listing and screenshots

| Script | Purpose |
|---|---|
| `store-listing-preflight.sh` | CI mirror of `/release` STEP 1.7 — fails fast on missing fields or store character limits, so an upload is never rejected by the Play/App Store API |
| `sync-play-listing.sh` | upload the Play listing (title, description, screenshots, feature graphic) without shipping an APK/AAB |
| `sync-play-listing.rb` | its Ruby implementation |
| `generate-screenshots.sh` | render generated HTML screenshots to PNG at store-submission resolution (Android 1080×1920, iOS 6.9" 1320×2868) |

---

## Root-level

| Script | Purpose |
|---|---|
| `customization-surface.sh` | reader + validator for `customization-surface.yaml` — `resolve <path>` prints a path's owner; `--verify` fails on a rule matching zero real paths |
| `remove-demo.sh` | strip the demo showcase, leaving a clean fork |
| `deployment-manifest-validate.sh` | validate the deploy-target ownership split (E1 / D-1) |
| `configure-release-environments.sh` | create + protect the GitHub Environments the release pipeline binds to, so store stages pause for manual approval. Run once per dispatching repo |
| `verify-demo-convention.sh` | static gate for the demo⇄framework separation convention |
| `verify-2-4-0.sh` | per-target main+test compile matrix for the Kotlin 2.4.0 upgrade |
| `_shared/fork-props.sh` | the ONE bash reader of `gradle/fork.properties` |

---

## Deployment

Deployment is **Fastlane lanes**, invoked from `deployment/`:

```bash
(cd deployment && bundle exec fastlane android deployInternal)
(cd deployment && bundle exec fastlane ios beta)
(cd deployment && bundle exec fastlane mac desktop_testflight)
```

Those need rbenv initialized in your shell. If a lane dies inside `activate_bin_path`, the
`before_all` hook in `deployment/_shared/before_all.rb` will already have warned you that the running
interpreter does not match `.ruby-version` — that, not the gems, is the cause.

Canonical guide: [`deployment/BOOTSTRAP.md`](../deployment/BOOTSTRAP.md) — Path A (OSS fork, manual
secrets) and Path B (vault mode).

---

## Common tasks

```bash
# fork setup — the one entry
scripts/white-label/doctor.sh

# who owns a path (sync ownership)
scripts/customization-surface.sh resolve core/store/src/commonMain/kotlin/kpt/core/store/di/KptStoreModule.kt

# health suite (30 checks)
bash scripts/product-health/product-health.sh
# on the upstream template itself, where fork-identity checks would misfire:
TEMPLATE_SELF_BUILD=1 bash scripts/product-health/product-health.sh

# iOS readiness
scripts/ios/verify_ios_deployment.sh

# strip the demo showcase
scripts/remove-demo.sh --apply
```

---

## Troubleshooting

**`bundle exec` fails inside `activate_bin_path`** — the interpreter, not the gems. This only happens
when you invoke a lane **by hand**; anything going through `ruby-exec.sh` is immune. Run
`eval "$(rbenv init -)"`, or check with `. scripts/ruby-exec.sh && ruby_exec_report`. `product-health`
will still pass in this state, by design — see `RT-8` above.

**A script cannot find `gradle/fork.properties` values** — it is derived from `app-profile/`. Run
`./gradlew syncForkConfig`; never hand-edit the bridge.

**`customization-surface.sh resolve` disagrees with what a sync did** — the contract's default is
**template-first** (`**` → `template`) but only for paths under a sync root (`sync-dirs.sh`
`SYNC_DIRS` / `SYNC_FILES`); a path under no root resolves `fork`, because the template can only own
what it ships. A path resolving `template` unexpectedly means no rule claims it inside a synced root.

**`sync-dirs.sh --mirror`** turns template-owned roots into a strict mirror — fork-only files there
are removed unless listed in `.template-sync-holds`. Never run it bare on an unclassified fork;
`/kmp-project-template-sync` classifies the fork's extras (adopt upstream · move to a fork package ·
delete) first.

Deeper guides: [Troubleshooting](../docs/troubleshooting.md) ·
[Deployment Playbook](../docs/architecture/tree/deployment/playbook.md) ·
[Secrets Management](../docs/architecture/tree/secrets/management.md)

[← Back to Main](../CLAUDE.md)
