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

<!-- api-docs:begin module=feature/settings sha=99e23ec474d28a5f24d4b2456b8b3b64d5422eba -->
## API reference

_Generated from `feature/settings` at tree `99e23ec474d2` by `scripts/docs/api-docs-gen.sh`._
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
_No KDoc at source._

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
_No KDoc at source._

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
_No KDoc at source._

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/language/LanguageScreen.kt:109</code></summary>

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
_No KDoc at source._

```kotlin
enum class Platform
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/settings/src/androidMain/kotlin/kpt/feature/settings/Platform.android.kt:15</code></summary>

```kotlin
import androidx.annotation.ChecksSdkIntAtLeast

actual fun getPlatform(): Platform {
    return Platform.Android
}

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
```

</details>

```kotlin
expect fun supportsDynamicTheming(): Boolean
```
_No KDoc at source._

### `feature/settings/src/commonMain/kotlin/kpt/feature/settings/SettingsDialog.kt`

```kotlin
fun SettingsDialog(onDismiss: () -> Unit, viewModel: SettingsViewModel = koinViewModel())
```
_No KDoc at source._

```kotlin
fun SettingsDialog(
```
_No KDoc at source._

```kotlin
fun SettingsDialogThemeChooserRow(
```
_No KDoc at source._

### `feature/settings/src/commonMain/kotlin/kpt/feature/settings/SettingsModule.kt`

```kotlin
val SettingsModule = module
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/di/SettingsModule.kt:22</code></summary>

```kotlin
val MifosSettingsViewModelQualifier = named("kpt.feature.settings.fork.SettingsViewModel")

val SettingsModule = module {
    viewModelOf(::SettingsViewModel) { qualifier = MifosSettingsViewModelQualifier }
}
```

</details>

### `feature/settings/src/commonMain/kotlin/kpt/feature/settings/SettingsRoute.kt`

```kotlin
data object SettingsRoute
```
_No KDoc at source._

```kotlin
data object NotificationRoute
```
_No KDoc at source._

```kotlin
data object SyncAndDraftsRoute
```
_No KDoc at source._

```kotlin
fun NavController.navigateToSettings(navOptions: NavOptions? = null) = navigate(SettingsRoute, navOptions)
```
_No KDoc at source._

```kotlin
fun NavController.navigateToNotification(navOptions: NavOptions? = null) = navigate(NotificationRoute, navOptions)
```
_No KDoc at source._

```kotlin
fun NavController.navigateToSyncAndDrafts(navOptions: NavOptions? = null) = navigate(SyncAndDraftsRoute, navOptions)
```
_No KDoc at source._

```kotlin
fun NavGraphBuilder.settingsDestination(settingsBody: @Composable () -> Unit = {})
```
The settings backbone destination.

```kotlin
fun NavGraphBuilder.notificationDestination(onBackClick: () -> Unit)
```
_No KDoc at source._

```kotlin
fun NavGraphBuilder.syncAndDraftsDestination(onBackClick: () -> Unit)
```
_No KDoc at source._

### `feature/settings/src/commonMain/kotlin/kpt/feature/settings/SettingsViewModel.kt`

```kotlin
class SettingsViewModel(
```
Settings ViewModel — the toolkit's `local_only_prefs` demo, on the MVI `BaseViewModel` idiom (state + typed actions), consistent with every other feature.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/LanguageDialog.kt:45</code></summary>

```kotlin

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
_No KDoc at source._

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/SettingsDialog.kt:129</code></summary>

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
_No KDoc at source._

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/LanguageDialog.kt:56</code></summary>

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

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/SettingsModule.kt:17</code></summary>

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
_No KDoc at source._

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/SettingsModule.kt:15</code></summary>

```kotlin
import org.koin.dsl.module

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

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/LanguageDialog.kt:45</code></summary>

```kotlin

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
_No KDoc at source._

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/language/LanguageScreen.kt:63</code></summary>

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
_No KDoc at source._

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountViewModel.kt:132</code></summary>

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
_No KDoc at source._

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
_No KDoc at source._

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
_No KDoc at source._

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/LanguageDialog.kt:45</code></summary>

```kotlin

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
_No KDoc at source._

```kotlin
fun LanguageChooserRow(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier)
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/LanguageDialog.kt:109</code></summary>

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

_18 type(s), 26 function(s)/property(ies); 16 carry KDoc at source; 0 authored example(s); 21 live call site(s)._
<!-- api-docs:end -->
