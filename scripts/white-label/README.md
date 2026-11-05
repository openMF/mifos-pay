# `scripts/white-label/` — the white-label lifecycle

Everything a fork of `kmp-project-template` needs to **set up, verify, and stay current** with the
white-label template lives here. There is **one entry** — `doctor.sh` — that drives and verifies every
stage; the other scripts are the stages it orchestrates.

## The single source of truth

`app-profile/{app.yaml, platforms/**}` is the ONE fork source of truth (identity, org, legal, signing,
firebase, store). Everything else is **derived** — `gradle/fork.properties` is generated from it and is
never hand-authored. Edit app-profile, run `doctor.sh`.

## The one entry

```bash
scripts/white-label/doctor.sh            # heal idempotent stages (derive + syncForkConfig) + diagnose all
scripts/white-label/doctor.sh --check    # diagnose only — full end-to-end sanity, mutates nothing (CI)
scripts/white-label/doctor.sh --fix      # heal everything it safely can, then diagnose
scripts/white-label/doctor.sh --sync     # adopt the LATEST template end-to-end: sync-dirs → --fix → verify
```

`../../setup-project.sh` is a thin redirect to `doctor.sh --fix` (legacy interactive prompts behind
`setup-project.sh --legacy`).

## The stages (each DRIVEN when incomplete + VERIFIED on every run)

| Stage | Script | Role |
|---|---|---|
| 1 customize | `fork-init.sh` | rename package/appId to the fork's (fresh clone only) |
| 2 identity | `derive.rb` | app-profile → `gradle/fork.properties` (derived, headered, zero-drift via config.rb MAP) |
| 3 derive | `../../gradlew syncForkConfig` | Config.xcconfig · deployment metadata · icons · catalog appId |
| 4 template | `sync-dirs.sh` | pull the latest template (fork files preserved via `../../customization-surface.yaml`) |
| 5 materialize | `firebase.sh` · `keystore.sh` · `../ios/setup_ios_complete.sh` · `/secrets pull` | google-services / keystore / iOS match / vault secrets |
| 6 health | `../product-health/product-health.sh` | fork-identity · appid-consistency · deployment-whitelabel B1–B10 · ios-pbxproj-identity · white-label-derived |

## Files

| File | Was | Notes |
|---|---|---|
| `doctor.sh` | `scripts/white-label-doctor.sh` | the one entry (RULE-WHITE-LABEL-DOCTOR-001) |
| `derive.rb` | `scripts/white-label-derive.rb` | app-profile → fork.properties |
| `fork-init.sh` | `customizer.sh` (root) | package rename |
| `firebase.sh` | `firebase-setup.sh` (root) | firebase registration |
| `keystore.sh` | `keystore-manager.sh` (root) | keystore/secrets |
| `sync-dirs.sh` | `sync-dirs.sh` (root) | template sync engine — `owner: template`, self-propagating |

## Framework (workspace forks)

With the Claude Product Cycle framework, workspace forks use `/kmp-project-template-sync` (which drives
this same `sync-dirs.sh` + `doctor.sh` adopt-and-verify tail) and `/kmp-project-template-sync
--all-consumers` to adopt the template into every workspace fork at once. A fork that forked at an OLD
pre-white-label version is migrated intelligently — see the sync command's whitelabel-adopt phase.
