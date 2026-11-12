# `feature/onboarding/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 41 tracked files — 21× `.xml`, 10× `.png`, 9× `.kt`, 1× `.kts`

**9 Kotlin files** — 1 screen(s), 1 ViewModel(s).

Screens: `OnboardingScreen`

ViewModels: `OnboardingViewModel`

**Consumes:** `core/store/user`

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

First-run onboarding, and the place an app asks for analytics and crash-report consent before collecting
anything. Three intro pages and a privacy page, shown once on a fresh install.

**It is off by default.** `RootNavViewModel` routes to it only when `UserData.firstTimeUser` is true, and a fresh
install's value comes from `app-profile/app.yaml#security.first_run_onboarding` (generated into
`AppSecurityConfig.FIRST_RUN_ONBOARDING`, default `false`). An app that turns it on gets the screen on first launch;
every other app is unaffected. `RootNavScreen` installs it on the root graph — it is not a `@FeatureDestination`,
because it runs before the authenticated graph exists.

**The consent behaviour is the part to keep when you rebrand it.** Both switches start OFF. Nothing is saved while
the user browses or flips switches — only a finishing button writes. "Allow and get started" is the one-tap
affirmative consent (both on); "Continue without sharing" / "Continue with my choices" saves the switches exactly as
shown, and is the same size and prominence. Pre-ticked switches, or "turn it off later in Settings", are not valid
consent under GDPR/ePrivacy, UK GDPR/PECR or India's DPDP Act. The two choices are written FIRST and
`setFirstTimeState(false)` LAST, so RootNav only moves on once consent is stored; `PrivacyConsentApplier` then
applies it on every platform. A failed write keeps the user on the privacy page and does not mark first run done.

**What a fork changes:** the three intro pages (copy, page art — this starter uses design-system icons, a fork
drops in its own illustrations and brand mark), and the agreement wording. The Terms link reads
`AppOrgLinks["terms"]` (`app-profile/app.yaml#org.links.terms`) and the policy link
`AppPrivacyConfig.PRIVACY_POLICY_URL`, both opened in-app. An app with a server-setup screen passes it to
`onboardingDestination(onOpenServerConfig = …)` to show a "Set up your server" link; the template passes nothing and
the link is hidden.

**Layout:** the page dots and actions sit in a fixed bottom bar, only the content above scrolls, and pages move with
the template shared-axis motion (`KptSharedAxis`). Wide windows get a two-pane layout. System back and horizontal
swipes move between pages; back on the first page leaves without saving.

Like the other `feature/` starters, this module is fork-owned once forked: a template sync never overwrites it.

<!-- api-docs:begin module=feature/onboarding sha=fd3898a9c2c309d5f95e67e295b10ee522ecbf5d -->
## API reference

_Generated from `feature/onboarding` at tree `fd3898a9c2c3` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/onboarding/src/commonMain/kotlin/kpt/feature/onboarding/OnboardingRoute.kt`

```kotlin
data object OnboardingRoute
```
First-run onboarding — three intro pages and the privacy consent page.

```kotlin
fun NavController.navigateToOnboarding(navOptions: NavOptions? = null) = navigate(OnboardingRoute, navOptions)
```
Opens onboarding.

```kotlin
fun NavGraphBuilder.onboardingDestination(onOpenServerConfig: (() -> Unit)? = null)
```
Onboarding is NOT annotated `@FeatureDestination`: that annotation registers onto the AUTHENTICATED graph, and onboarding runs before it.

### `feature/onboarding/src/commonMain/kotlin/kpt/feature/onboarding/di/OnboardingModule.kt`

```kotlin
val OnboardingModule = module
```
Koin bindings for the onboarding feature's view model.

### `feature/onboarding/src/commonMain/kotlin/kpt/feature/onboarding/ui/OnboardingTestTags.kt`

```kotlin
object OnboardingTestTags
```
Stable handles for the onboarding screen — the Preview, the Compose UI test and the Maestro flow (idea-layer/screens/onboarding/maestro) all read these, never a hand-typed string.

<details><summary>Used in the template — <code>feature/onboarding/src/commonMain/kotlin/kpt/feature/onboarding/ui/OnboardingScreen.kt:172</code></summary>

```kotlin
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .testTag(OnboardingTestTags.SCREEN)
            .pointerInput(state.page) {
                // A horizontal swipe is the same as Next / back and never saves.
                var dragged = 0f
                detectHorizontalDragGestures(
```

</details>

- `const val SCREEN = "onboarding_pager"` — The screen root.
- `const val SKIP = "onboarding_skip"` — Skip, on the intro pages.
- `const val TITLE = "onboarding_title"` — The intro page's title.
- `const val BODY = "onboarding_body"` — The intro page's body.
- `const val PAGE_INDICATOR = "onboarding_page_indicator"` — The page dots.
- `const val NEXT = "onboarding_next"` — Next, on the intro pages.
- `const val PRIVACY_TITLE = "onboarding_privacy_title"` — The privacy page's title.
- `const val ANALYTICS_SWITCH = "onboarding_analytics_switch"` — The "Share usage analytics" switch row.
- `const val CRASH_REPORTS_SWITCH = "onboarding_crash_reports_switch"` — The "Send crash reports" switch row.
- `const val AGREEMENT = "onboarding_agreement"` — "By using our service you accept our Terms & Conditions and Privacy Policy." — one localized sentence whose two links are inline `LinkAnnotation`s (so each locale keeps its own word order).
- `const val CONNECT_INSTANCE = "onboarding_connect_instance"` — "Set up your server" — shown only when the app passes a server-setup destination.
- `const val ALLOW_GET_STARTED = "onboarding_allow_get_started"` — "Allow and get started" — the primary, one-tap consent.
- `const val CONTINUE_CHOICES = "onboarding_continue_choices"` — "Continue without sharing" / "Continue with my choices".
- `const val SAVE_FAILED = "onboarding_save_failed"` — The save-failed message.

### `feature/onboarding/src/commonMain/kotlin/kpt/feature/onboarding/ui/OnboardingViewModel.kt`

```kotlin
class OnboardingViewModel(
```
First-run onboarding: three intro pages and the privacy page. The page is STATE and the screen renders a `when` over it: a pager kept in sync both ways can disagree mid-swipe.

<details><summary>Used in the template — <code>feature/onboarding/src/commonMain/kotlin/kpt/feature/onboarding/di/OnboardingModule.kt:18</code></summary>

```kotlin
/** Koin bindings for the onboarding feature's view model. */
val OnboardingModule = module {
    viewModelOf(::OnboardingViewModel)
}
```

</details>

```kotlin
enum class OnboardingPage
```
The four onboarding pages, in order.

<details><summary>Used in the template — <code>feature/onboarding/src/commonMain/kotlin/kpt/feature/onboarding/ui/OnboardingScreen.kt:142</code></summary>

```kotlin
    // System back steps to the previous page; on the first page it falls through to the system, which leaves
    // without saving — both choices stay off and onboarding shows again next launch.
    BackHandler(enabled = state.page != OnboardingPage.Overview) {
        viewModel.trySendAction(OnboardingAction.Previous)
    }

    OnboardingScreenContent(
```

</details>

```kotlin
data class OnboardingState(
```
What the onboarding screen renders.

<details><summary>Used in the template — <code>feature/onboarding/src/commonMain/kotlin/kpt/feature/onboarding/ui/OnboardingScreen.kt:159</code></summary>

```kotlin
@Composable
internal fun OnboardingScreenContent(
    state: OnboardingState,
    onAction: (OnboardingAction) -> Unit,
    onOpenServerConfig: (() -> Unit)?,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenTerms: () -> Unit,
```

</details>

```kotlin
sealed interface OnboardingAction
```
Intents accepted by `OnboardingViewModel`.

<details><summary>Used in the template — <code>feature/onboarding/src/commonMain/kotlin/kpt/feature/onboarding/ui/OnboardingScreen.kt:143</code></summary>

```kotlin
    // without saving — both choices stay off and onboarding shows again next launch.
    BackHandler(enabled = state.page != OnboardingPage.Overview) {
        viewModel.trySendAction(OnboardingAction.Previous)
    }

    OnboardingScreenContent(
        state = state,
```

</details>

---

_6 type(s), 17 function(s)/property(ies); 23 carry KDoc at source; 0 authored example(s); 5 live call site(s)._
<!-- api-docs:end -->
