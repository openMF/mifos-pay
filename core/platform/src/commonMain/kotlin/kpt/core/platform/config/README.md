# `core/platform/config` — the fork-extension seam

**Template-owned.** `/kmp-project-template-sync` full-copies this package, so a fork keeps
receiving upstream fixes here. Do not edit these files in a fork — extend them through the
`Fork*` seam each one declares.

## Read this first — how `core/platform` differs from its siblings

This module is a **re-export boundary** over `core-base/platform` (`G-CORE-BASE-ENCAP`) and the
home for `expect`/`actual` platform bridges. It **ships zero Kotlin source by design** — it stays a
pass-through until a fork or feature needs a bridge `core-base/platform` does not already provide.
See `core/platform/CONSUMPTION.md`.

That makes `config/` narrower here than elsewhere. A platform capability is not configured from
`commonMain`; it is IMPLEMENTED per target:

- the **contract** is an `expect` declaration in `commonMain`, in that capability's own package
  (`permissions/`, `biometrics/`, `sharing/`, …) — **not** here
- the **implementations** are the `actual`s under `src/androidMain`, `src/iosMain`,
  `src/desktopMain`, `src/wasmJsMain` / `src/jsMain`, `src/nativeMain`
- a value that differs per target (notification-channel importance, permission rationale copy)
  belongs with the `actual` that uses it, or with the feature that owns the UX — putting it in
  `commonMain` would force one value onto every platform

Capability packages are **fork-owned**: `kmp-platform-gen` writes them, they are undeclared, and
the module catch-all resolves them `fork` so a sync never overwrites a fork's bridges.

## What belongs here

Only what is genuinely common AND genuinely template-owned across every target:

- policy the boundary itself enforces — DI wiring defaults for the re-exported `platformModule`,
  a shared capability-availability contract, cross-target fallback behaviour
- an `App*` default + `Fork*` override pair, where a fork should retune the boundary's
  behaviour rather than fork a bridge

If a candidate has a different right answer on Android than on iOS, it is not config — it is an
`actual`, and it belongs in the capability package.

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
`core/platform/module-packages.yaml` — both already in place.

Adding the first file needs no contract change. Putting it anywhere else in this module does:
the module catch-all resolves `fork`, so it would silently stop syncing.
