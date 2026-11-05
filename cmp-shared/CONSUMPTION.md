# Consuming `cmp-shared` in a fork

`cmp-shared` is the aggregator every platform entry point calls into: `initKoin()` for DI startup
and `SharedApp` for the root UI. It is where per-platform app classes (`AndroidApp`, `main.kt`,
`Application.kt`/`Main.kt`, `ViewController.kt`) converge into one commonMain call each.

## What's here

- **`SharedApp`** (`SharedApp.kt`) — the `@Composable` every platform renders. Wraps
  `cmp-navigation`'s `ComposeApp` in `LocalManagerProvider` + `LocalImageLoaderProvider`. Takes the
  platform callbacks (`updateScreenCapture`, `handleRecreate`, `handleThemeMode`,
  `handleAppLocale`, `onSplashScreenRemoved`) and forwards them.
- **`initKoin()`** (`utils/KoinExt.kt`) — the single per-platform DI entrypoint. Starts Koin with
  `KoinModules.allModules`, then runs `WorkerKmpAuto.install()` (worker-kmp codegen shim —
  wires every `@WorkerKmpWorkers` declaration on every platform from this one commonMain call),
  `initSyncNotifier()` (KMPNotifier local-notification setup), and
  `ForkInitializers.runAll()` (the fork's registered startup hooks).
- **`WorkerDeclarations.kt`** — template-owned (`owner: template`) `@WorkerKmpWorkers` site listing
  the base workers (`DataSyncWorker`, `NotificationWorker`).
- **`ForkWorkerDeclarations.kt`** — the fork-owned (`owner: fork`) sibling seam; the KSP processor
  scans commonMain for every `@WorkerKmpWorkers` site and aggregates both files. Ships as a no-op
  anchor (`forkWorkerDeclarations()`) — annotate it to register your own background workers without
  touching `WorkerDeclarations.kt`.
- **`nativeMain/ViewController.kt`** — the iOS export point `cmp-ios/ContentView.swift` calls
  (`ViewControllerKt.viewController()`).

## What a fork touches

- **Background workers** — annotate `forkWorkerDeclarations()` in `ForkWorkerDeclarations.kt` with
  `@WorkerKmpWorkers(workers = [MyWorker::class])`.
- **Startup hooks, features, tabs, dashboard** — these are NOT wired here; they go through
  `cmp-navigation`'s registries (`ForkInitializers`, `ForkFeatureRegistry`, `ForkBackboneRegistry`,
  `ForkTabRegistry`), which `initKoin()`/`ComposeApp` read from.

## What to leave to sync

`SharedApp`, `initKoin()`, and `WorkerDeclarations.kt` are template infrastructure — a template
sync full-copies them while `ForkWorkerDeclarations.kt` survives untouched.

Symbols: SharedApp, initKoin, koinConfiguration, workerDeclarations, forkWorkerDeclarations
