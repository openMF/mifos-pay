# `feature/settings/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 51 tracked files — 27× `.kt`, 16× `.xml`, 4× `.png`, 1× `.gitignore`, 1× `.md`

**27 Kotlin files** — 4 screen(s), 2 ViewModel(s).

Screens: `LanguageScreen`, `NotificationScreen`, `SettingsScreen`, `SyncAndDraftsScreen`

ViewModels: `SettingsViewModel`, `SyncAndDraftsViewModel`

**Consumes:** `core/store/repository`, `core/store/user`

### Docs in the tree

| Path | |
|---|---|
| `feature/settings/README.md` | :feature:settings |

<!-- tree-scaffold:end -->
## Significance

The largest feature, and the one that touches the most cross-cutting state: theme, language, dynamic
colour, screen capture, app lock, the conflict inbox and the sync-and-drafts view.

Two of those are worth calling out. The **conflict inbox** is where a MUTABLE store's divergences
surface for a human to resolve — without it, a server-wins resolution silently discards a user's edit.
The **sync-and-drafts** screen makes the offline write queue visible, which is the difference between
"the app lost my change" and "the app is holding my change until it can send it".

`UserEditableSettings` is deliberately narrower than `UserData`, so a settings screen cannot write a
session or lock field it has no business touching.

<!-- api-docs:begin module=feature/settings sha=1bb3558ab4a02e5c8f1f37b438d5a909c9a18ebd -->
## API reference

_Generated from `feature/settings` at tree `1bb3558ab4a0` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/settings/src/commonMain/kotlin/kpt/feature/settings/DevMenuEntry.kt`

```kotlin
data class DevMenuEntry(
```
DevMenuEntry — a generic row in the Settings screen's "Developer" section.

### `feature/settings/src/commonMain/kotlin/kpt/feature/settings/LanguageDialog.kt`

```kotlin
fun LanguageDialog(onDismiss: () -> Unit, viewModel: SettingsViewModel = koinViewModel())
```
Language chooser dialog bound to `SettingsViewModel`, dismissed through `onDismiss`.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/fork/SettingsScreen.kt:345</code></summary>

```kotlin

        DialogState.LanguageSelection -> {
            LanguageDialog(
                onDismiss = onDismissRequest,
            )
        }
```

</details>

```kotlin
fun LanguageDialog(
```
Stateless language chooser dialog rendering `settingsUiState` and reporting the choice via `onChangeLanguage`.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/fork/SettingsScreen.kt:345</code></summary>

```kotlin

        DialogState.LanguageSelection -> {
            LanguageDialog(
                onDismiss = onDismissRequest,
            )
        }
```

</details>

```kotlin
fun LanguageChooserRow(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier)
```
Selectable row showing one language option.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/language/LanguageScreen.kt:111</code></summary>

```kotlin
    Column(Modifier.selectableGroup()) {
        LanguageConfig.entries.forEach { language ->
            LanguageChooserRow(
                text = language.text,
                selected = currentLanguage.localeName == language.toLanguage().localeName,
                onClick = { onChangeLanguage(language) },
            )
```

</details>

### `feature/settings/src/commonMain/kotlin/kpt/feature/settings/Platform.kt`

```kotlin
expect fun getPlatform(): Platform
```
Returns the `Platform` the app is running on.

```kotlin
enum class Platform
```
Platforms the settings feature distinguishes between.

<details><summary>Used in the template — <code>feature/settings/src/androidMain/kotlin/kpt/feature/settings/Platform.android.kt:16</code></summary>

```kotlin

/** Android actual of [getPlatform]. */
actual fun getPlatform(): Platform {
    return Platform.Android
}

/** Android actual; dynamic (Material You) color is supported only on Android 12 and above. */
```

</details>

```kotlin
expect fun supportsDynamicTheming(): Boolean
```
Whether the current platform supports dynamic (Material You) theming.

### `feature/settings/src/commonMain/kotlin/kpt/feature/settings/SettingsDialog.kt`

```kotlin
fun SettingsDialog(onDismiss: () -> Unit, viewModel: SettingsViewModel = koinViewModel())
```
Settings dialog for theme brand, dynamic color and dark theme bound to `SettingsViewModel`.

```kotlin
fun SettingsDialog(
```
Stateless settings dialog rendering `settingsUiState` and reporting preference changes through the callbacks.

```kotlin
fun SettingsDialogThemeChooserRow(
```
Selectable row for one theme option in the settings dialog.

### `feature/settings/src/commonMain/kotlin/kpt/feature/settings/SettingsModule.kt`

```kotlin
val SettingsModule = module
```
Koin module providing the template settings and sync-and-drafts view models.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/di/SettingsModule.kt:23</code></summary>

```kotlin

/** Koin module providing the fork settings view model under its qualifier. */
val SettingsModule = module {
    viewModelOf(::SettingsViewModel) { qualifier = MifosSettingsViewModelQualifier }
}
```

</details>

### `feature/settings/src/commonMain/kotlin/kpt/feature/settings/SettingsRoute.kt`

```kotlin
data object SettingsRoute
```
Navigation route of the settings screen.

```kotlin
data object NotificationRoute
```
Navigation route of the notification screen.

```kotlin
data object SyncAndDraftsRoute
```
Navigation route of the sync-and-drafts screen.

```kotlin
fun NavController.navigateToSettings(navOptions: NavOptions? = null) = navigate(SettingsRoute, navOptions)
```
Navigates to the settings screen.

```kotlin
fun NavController.navigateToNotification(navOptions: NavOptions? = null) = navigate(NotificationRoute, navOptions)
```
Navigates to the notification screen.

```kotlin
fun NavController.navigateToSyncAndDrafts(navOptions: NavOptions? = null) = navigate(SyncAndDraftsRoute, navOptions)
```
Navigates to the sync-and-drafts screen.

```kotlin
fun NavGraphBuilder.settingsDestination(settingsBody: @Composable () -> Unit = {})
```
The settings backbone destination.

```kotlin
fun NavGraphBuilder.notificationDestination(onBackClick: () -> Unit)
```
Registers the notification destination.

```kotlin
fun NavGraphBuilder.syncAndDraftsDestination(onBackClick: () -> Unit)
```
Registers the sync-and-drafts destination.

### `feature/settings/src/commonMain/kotlin/kpt/feature/settings/SettingsViewModel.kt`

```kotlin
class SettingsViewModel(
```
Settings ViewModel — the toolkit's `local_only_prefs` demo, on the MVI `BaseViewModel` idiom (state + typed actions), consistent with every other feature.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/LanguageDialog.kt:46</code></summary>

```kotlin
/** Language chooser dialog bound to [SettingsViewModel], dismissed through [onDismiss]. */
@Composable
fun LanguageDialog(onDismiss: () -> Unit, viewModel: SettingsViewModel = koinViewModel()) {
    val settingsUiState by viewModel.settingsUiState.collectAsStateWithLifecycle()
    LanguageDialog(
        onDismiss = onDismiss,
        settingsUiState = settingsUiState,
```

</details>

```kotlin
sealed interface SettingsAction
```
Preference-mutation intents accepted by `SettingsViewModel`.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/fork/SettingsScreen.kt:115</code></summary>

```kotlin
        disableBiometricsResult?.let { result ->
            viewmodel.trySendAction(
                SettingsAction.DisableBiometricsResult(
                    success = result,
                    systemAuthProvider = authProvider,
                ),
            )
```

</details>

```kotlin
data class UserEditableSettings(
```
User-editable appearance and language preferences.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/SettingsDialog.kt:131</code></summary>

```kotlin
@Composable
private fun ColumnScope.SettingsPanel(
    settings: UserEditableSettings,
    supportDynamicColor: Boolean,
    onChangeThemeBrand: (themeBrand: ThemeBrand) -> Unit,
    onChangeDynamicColorPreference: (useDynamicColor: Boolean) -> Unit,
    onChangeDarkThemeConfig: (darkThemeConfig: DarkThemeConfig) -> Unit,
```

</details>

```kotlin
sealed interface SettingsUiState
```
UI state of the settings dialogs.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/LanguageDialog.kt:58</code></summary>

```kotlin
@Composable
fun LanguageDialog(
    settingsUiState: SettingsUiState,
    onDismiss: () -> Unit,
    onChangeLanguage: (language: LanguageConfig) -> Unit,
    modifier: Modifier = Modifier,
) {
```

</details>

### `feature/settings/src/commonMain/kotlin/kpt/feature/settings/SyncAndDraftsViewModel.kt`

```kotlin
class SyncAndDraftsViewModel(
```
ViewModel for the template-level **Sync & Drafts** screen (Settings).

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/SettingsModule.kt:18</code></summary>

```kotlin
val SettingsModule = module {
    viewModelOf(::SettingsViewModel)
    viewModelOf(::SyncAndDraftsViewModel)
}
```

</details>

```kotlin
sealed interface SyncAndDraftsAction
```
Cross-form draft actions accepted by `SyncAndDraftsViewModel`.

```kotlin
sealed interface SyncAndDraftsUiState
```
UI state for the Sync & Drafts screen.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/SyncAndDraftsPreview.kt:34</code></summary>

```kotlin
    KptTheme {
        SyncAndDraftsScreenContent(
            uiState = SyncAndDraftsUiState.Success(
                drafts = listOf(
                    DraftRecord(3, "bill", "electricity", SubmitOutboxStatus.PENDING, 0, 0, null),
                    DraftRecord(4, "loan", "home-loan", SubmitOutboxStatus.PENDING, 0, 0, null),
                ),
```

</details>

### `feature/settings/src/commonMain/kotlin/kpt/feature/settings/TestTags.kt`

```kotlin
object TestTags
```
Append-only test-tag registry for the settings feature. Consumed by Compose UI tests in `feature/settings/src/commonTest/`. APPEND-ONLY contract (RULE-KMP-COMPOSE-UITEST-001 CU-5).

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/HomeScreen.kt:54</code></summary>

```kotlin
) {
    Scaffold(
        modifier = Modifier.testTag(TestTags.Home.SCREEN),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
```

</details>

- `const val SCREEN: String = "settings_screen"` — Root scaffold — always rendered regardless of dialog state.
- `const val SCREEN: String = "notification_screen"` — Root scaffold — always rendered regardless of content state.
- `const val SCREEN: String = "sync_and_drafts_screen"` — Root scaffold — always rendered regardless of content state.
- `const val PRUNE: String = "sync_and_drafts_prune"` — The manual "Prune expired" button (rendered only when there is content).

### `feature/settings/src/commonMain/kotlin/kpt/feature/settings/di/SettingsModule.kt`

```kotlin
val MifosSettingsViewModelQualifier = named("kpt.feature.settings.fork.SettingsViewModel")
```
Koin qualifier for this feature's `SettingsViewModel`. On JS / Wasm Koin keys a binding by the type's simple name, and the template's `kpt.feature.settings.SettingsViewModel` is bound too.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/fork/SettingsScreen.kt:102</code></summary>

```kotlin
    entryStateHandle: SavedStateHandle,
    modifier: Modifier = Modifier,
    viewmodel: SettingsViewModel = koinViewModel(qualifier = MifosSettingsViewModelQualifier),
) {
    val state by viewmodel.stateFlow.collectAsStateWithLifecycle()
    val authProvider = platformAuthenticationProvider.current
    val isRegistered by authProvider.isRegistered.collectAsStateWithLifecycle()
```

</details>

```kotlin
val SettingsModule = module
```
Koin module providing the fork settings view model under its qualifier.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/SettingsModule.kt:16</code></summary>

```kotlin

/** Koin module providing the template settings and sync-and-drafts view models. */
val SettingsModule = module {
    viewModelOf(::SettingsViewModel)
    viewModelOf(::SyncAndDraftsViewModel)
}
```

</details>

### `feature/settings/src/commonMain/kotlin/kpt/feature/settings/fork/SettingsViewModel.kt`

```kotlin
class SettingsViewModel(
```
ViewModel for `SettingsScreen`.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/LanguageDialog.kt:46</code></summary>

```kotlin
/** Language chooser dialog bound to [SettingsViewModel], dismissed through [onDismiss]. */
@Composable
fun LanguageDialog(onDismiss: () -> Unit, viewModel: SettingsViewModel = koinViewModel()) {
    val settingsUiState by viewModel.settingsUiState.collectAsStateWithLifecycle()
    LanguageDialog(
        onDismiss = onDismiss,
        settingsUiState = settingsUiState,
```

</details>

```kotlin
data class SettingsState(
```
UI state of the settings screen.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/language/LanguageScreen.kt:65</code></summary>

```kotlin
@Composable
fun LanguageDialogContent(
    settingsUiState: SettingsState,
    onDismiss: () -> Unit,
    onChangeLanguage: (language: LanguageConfig) -> Unit,
    onUpdateAppLocale: () -> Unit,
    modifier: Modifier = Modifier,
```

</details>

```kotlin
sealed interface DialogState
```
Dialogs the settings screen can show.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountViewModel.kt:141</code></summary>

```kotlin
                    is SubmitState.Submitting -> {
                        mutableStateFlow.update {
                            it.copy(dialogState = AccountState.DialogState.Loading)
                        }
                    }

                    is SubmitState.Submitted -> {
```

</details>

```kotlin
sealed interface SettingsEvent
```
One-shot events emitted by the settings view model.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/fork/SettingsScreen.kt:126</code></summary>

```kotlin
    EventsEffect(viewmodel) { event ->
        when (event) {
            SettingsEvent.OnNavigateBack -> backPress.invoke()
            SettingsEvent.NavigateToPasscodeScreen -> {
                navigateToPasscodeScreen(
                    if (isRegistered) DISABLE_BIOMETRICS_VERIFICATION_KEY else null,
                )
```

</details>

```kotlin
sealed interface SettingsAction
```
Actions handled by the settings view model.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/SettingsViewModel.kt:33</code></summary>

```kotlin
    private val settingsRepository: UserDataRepository,
    private val analyticsHelper: AnalyticsHelper,
) : BaseViewModel<SettingsUiState, Nothing, SettingsAction>(SettingsUiState.Loading) {

    /** Backward-compat alias for the inherited [stateFlow] — the dialogs read this. */
    val settingsUiState: StateFlow<SettingsUiState> get() = stateFlow
```

</details>

### `feature/settings/src/commonMain/kotlin/kpt/feature/settings/language/LanguageScreen.kt`

```kotlin
fun LanguageDialog(
```
Language chooser dialog bound to the fork `SettingsViewModel`, dismissed through `onDismiss`.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/LanguageDialog.kt:46</code></summary>

```kotlin
/** Language chooser dialog bound to [SettingsViewModel], dismissed through [onDismiss]. */
@Composable
fun LanguageDialog(onDismiss: () -> Unit, viewModel: SettingsViewModel = koinViewModel()) {
    val settingsUiState by viewModel.settingsUiState.collectAsStateWithLifecycle()
    LanguageDialog(
        onDismiss = onDismiss,
        settingsUiState = settingsUiState,
```

</details>

```kotlin
fun LanguageDialogContent(
```
Stateless language chooser dialog content rendering `settingsUiState` and reporting the choice.

```kotlin
fun LanguageChooserRow(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier)
```
Selectable row showing one language option.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/LanguageDialog.kt:111</code></summary>

```kotlin
    Column(Modifier.selectableGroup()) {
        LanguageConfig.entries.forEach { language ->
            LanguageChooserRow(
                text = language.text,
                selected = currentLanguage == language,
                onClick = { onChangeLanguage(language) },
            )
```

</details>

### `feature/settings/src/commonMain/kotlin/kpt/feature/settings/navigation/SettingsNavigation.kt`

```kotlin
fun NavController.navigateToSettings(navOptions: NavOptions? = null)
```
Pushes `SETTINGS_ROUTE`.

```kotlin
fun NavGraphBuilder.settingsScreen(
```
Registers the settings destination. Hoists the destination's own `androidx.lifecycle.SavedStateHandle` into `SettingsScreenRoute` so the disable-biometrics round-trip channel (`DISABLE_BIOMETRICS_VERIFICATION_KEY`) works.

---

_18 type(s), 26 function(s)/property(ies); 44 carry KDoc at source; 0 authored example(s); 21 live call site(s)._
<!-- api-docs:end -->
