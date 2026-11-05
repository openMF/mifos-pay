# Consuming `core-base/ui`

> Framework-shared Compose primitives — `ScreenState`/`SubmitState` rendering, base ViewModels, and
> navigation/motion helpers. There's no sibling `core/ui` wrapper for these; a fork's
> `core/designsystem` supplies BRANDING (`KptScreenStateDefaults`) that these composables read via
> `CompositionLocal`, but the composables themselves are consumed directly from feature screens.

## Call sequence

1. **Detail / single-source screens** — render a `ScreenState<T>` (or a `ScreenDataStream<T>` directly)
   with `ScreenContent(state, onRetry) { data, freshness -> ... }`. It animates between
   Loading/Empty/NoNetwork/Error/Content and shows a `refreshingIndicator` slot during SWR
   revalidation. Override any slot per-call, or globally via `LocalScreenStateDefaults`.
2. **Infinite-scroll lists** — use `PagingScreenContent(pagingStream, onRetry) { items -> items(items)
   { ... } }`; it owns the `LazyColumn`, the `LoadMoreFooter`, and the load-more trigger, so you supply
   only per-item content.
3. **Forms / mutations** — wrap the ViewModel's `ScreenState` + `SubmitState` (or a single
   `MutationUiState<T, R>`) in `MutationScreenContent(state, onRetry, onSubmitted) { data, _ -> ... }`;
   it layers `SubmitProgressOverlay` + `SubmitResultHandler` for you. Pass `draftResumeState` +
   `onResumeClick` / `onDiscardClick` to also render `DraftResumeBanner` when the ViewModel's mode is
   `MutationMode.Draft`.
4. **ViewModel** — extend `BaseViewModel<State, Event, Action>` for plain MVI screens, or
   `BaseMutationViewModel<T, R>(mode)` for edit screens: pass `MutationMode.InSession` (fire-and-forget)
   or `MutationMode.Draft(outbox, formKey, uniqueKey)` (offline-resilient, resumable across restarts);
   override `performSubmit(payload)`.
5. **Navigation** — call `NavController.popBackStackSafely()` / `rememberSafeBackPress()` to guard
   against double-pop crashes on fast repeated back presses; the `motion` package (`KptSharedAxis`,
   `NavTransitions`) supplies the shared push/fade/stay transitions consumed by
   `NavGraphBuilderExtensions`.

## Notes

- `AppInfo.appDisplayName` is the SINGLE common-code accessor for the fork's display name (sourced from
  `app-profile/app.yaml` via BuildKonfig) — never hardcode an app-name string in a composable.
- `KptConnectivityBanner` and `DataFreshnessIndicator` / `DefaultRefreshingBanner` are the shipped
  connectivity/staleness UI; most screens get them for free through `ScreenContent`'s defaults rather
  than placing them manually.
- Safe to extend: the `Default*Content` composables read `LocalScreenStateDefaults` — brand them via
  `core/store`'s `KptScreenStateDefaults`, not by editing this module.
- Framework-owned, don't fork: `BaseViewModel`'s action/event channel plumbing and `ScreenContent`'s
  `AnimatedContent` transition-key logic — see the "Do NOT modify core-base/ui" note in the root
  `CLAUDE.md`.

Canonical example: `feature/loans` (`PagingScreenContent` over an `OFFLINE_LOCAL_ONLY` store), the
cloud-todo showcase (`MutationScreenContent` + `MutationMode.Draft`).

Symbols: ScreenContent, PagingScreenContent, MutationScreenContent, BaseViewModel, BaseMutationViewModel, AppInfo
