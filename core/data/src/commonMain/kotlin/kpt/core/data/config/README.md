# `core/data/config` — the fork-extension seam

**Template-owned.** `/kmp-project-template-sync` full-copies this package, so a fork keeps
receiving upstream fixes here. Do not edit these files in a fork — extend them through the
`Fork*` seam each one declares.

## What belongs here

Repository-level POLICY a fork tunes — default cache TTLs, sync cadence, retry/backoff
bounds, conflict-resolution defaults. Repositories stay in their domain packages; the
numbers they read belong here so a fork retunes without touching repository code.

## The shape every `config/` package follows

| Prefix | Who owns it | What it is |
|---|---|---|
| `App*` | template | the shipped default a fork inherits |
| `Fork*` | fork | the override seam — empty on the template, filled by the fork |
| `Fork*` | fork | per-fork values the build reads |
| generated | build | projections of `app-profile/app.yaml`; never hand-edited |

Worked examples in this repo: `core/store/config` (`KptErrorMapper` + `ForkErrorMapper`,
`KptScreenStateDefaults` + `ForkScreenStateDefaults`), `core/network/config`
(`AppAccessPoints` / `AppUrlTypes`, projected by `syncForkConfig`),
`core/database/config` (`DatabaseConfig` generated + `ForkDatabaseConfig`), and
`core/firebase/config` (`analytics/` + `crashlytics/` cross-cutting halves).

## Currently empty

This package exists so the seam has a home BEFORE it has content: a later addition lands here
and syncs to every fork automatically, with no ownership rule or registry row to remember. It is
declared `template` in `customization-surface.yaml` and carries an `id: config` row in
`core/data/module-packages.yaml` — both already in place.

Adding the first file needs no contract change. Putting it anywhere else in this module does:
the module catch-all resolves `fork`, so it would silently stop syncing.
