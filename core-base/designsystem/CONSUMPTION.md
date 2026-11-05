# Consuming `core-base/designsystem` in a fork

> The framework-shared Material 3 primitives — theme provider, base components, responsive layout,
> chart geometry — that `core/designsystem` wraps in the app's brand. `core/designsystem`'s
> `KptTheme` is what features actually use; this module is what THAT wrapper is built on.

## Call sequence

1. **`core/designsystem`'s `KptTheme` wraps `KptMaterialTheme`** (this module) — you rarely call
   `KptMaterialTheme` directly from a feature. If you're extending the theme layer itself (i.e.
   working inside `core/designsystem`), build a `KptThemeProvider` (via the `kptTheme { colors {
   ... }; typography { ... }; spacing { ... } }` DSL or `KptThemeProviderImpl()`) and pass it to
   `KptMaterialTheme(theme = ...) { }` — it provides both the `KptTheme.*` CompositionLocals and a
   derived Material3 `ColorScheme`/`Typography`/`Shapes` in one composable.
2. **Reach for a base component before hand-rolling one** — `KptTopAppBar` (+ `kptTopAppBar { }`
   builder), `KptButton`, `AppCard`, `HeroCard`, `KptToastHost`, `KptShimmerLoadingBox`,
   `KptProgress`/`KptProgressRenderer` (+ `ProgressSizeSpec`), `BounceAnimation`, `SlideTransition`.
   These are brand-agnostic — `core/designsystem` composes them, it doesn't reimplement them.
3. **Use the responsive layout primitives** for adaptive UI instead of hand-rolled breakpoints —
   `KptResponsiveLayout` (compact/medium/expanded slots), `KptGrid`, `KptMasonryGrid`,
   `KptFlowRow`/`KptFlowColumn`, `KptSplitPane`, `KptSidebarLayout`, `KptStack`, and the
   `Adaptive*Scaffold` family (`AdaptiveListDetailPaneScaffold`,
   `AdaptiveNavigableListDetailScaffold`, `AdaptiveNavigableSupportingPaneScaffold`,
   `AdaptiveNavigationSuiteScaffold`).
4. **Compute chart geometry, don't hand-roll trig** — `BarGeometry`, `DonutGeometry`,
   `SparklineGeometry` produce pure layout math (bar rects, arc segments, polyline points); `core
   /designsystem`'s `chart/` composables (`KptBarChart`, `KptDonutChart`, `KptSparkline`) consume
   them and add the brand palette (`ChartTokens`).
5. **Implement `KptComponent`/`Clickable`/`Styleable`/`Themeable`** (`core/KptComponent.kt`) when
   authoring a new base-tier component so it composes with the existing config-builder pattern; use
   `ComponentStateHolder` for simple observable component-local state.

## Notes

- `core-base/designsystem`'s own `KptTheme` composable is a lighter-weight, Material3-agnostic
  token provider (colors/typography/shapes/spacing/elevation only) — `KptMaterialTheme` is the one
  that also wires Material3's `MaterialTheme`, and is what `core/designsystem` actually builds on.
- Don't add brand/finance-domain tokens or composables here (`FinanceColors`, `MoneyText`,
  `StatusChip`, …) — those belong in `core/designsystem`, which is the fork-brandable layer above
  this one.
- Framework-shared (E2/T3) — upgrades cleanly across template versions; push fork pressure to
  `core/designsystem`.

Canonical example: `core/designsystem`'s `KptTheme.kt` (wraps `KptMaterialTheme` + adds
`FinanceColors`/`Spacing`/`Elevation`); `core/designsystem`'s `chart/` composables (consume
`core-base/designsystem`'s geometry + `ChartTokens`).

Symbols: KptMaterialTheme, KptTheme, KptThemeProvider, kptTheme, KptComponent, ComponentStateHolder, KptResponsiveLayout, KptTopAppBar, BarGeometry, DonutGeometry, SparklineGeometry
