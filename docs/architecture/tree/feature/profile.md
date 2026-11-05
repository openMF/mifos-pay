# `feature/profile/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 21 tracked files — 13× `.kt`, 3× `.xml`, 1× `.gitignore`, 1× `.md`, 1× `.kts`

**13 Kotlin files** — 2 screen(s), 2 ViewModel(s).

Screens: `EditProfileScreen`, `ProfileScreen`

ViewModels: `EditProfileViewModel`, `ProfileViewModel`

### Docs in the tree

| Path | |
|---|---|
| `feature/profile/README.md` | :feature:profile |

<!-- tree-scaffold:end -->
## Significance

A signed-out placeholder, and a deliberate demonstration of where the seam is. `ProfileInfo` carries
only the app's display name, yet it is a real domain model rather than a raw `String` — because a fork
replaces the *fetcher*, not the screen. Swapping in a signed-in user means widening the model and
returning it from the store, with the ViewModel and composable untouched.

<!-- api-docs:begin module=feature/profile sha=78193e5e185dc9e445ffacf95bfe71bce7003998 -->
## API reference

_Generated from `feature/profile` at tree `78193e5e185d` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/profile/src/commonMain/kotlin/kpt/feature/profile/ProfileRoute.kt`

```kotlin
data object ProfileRoute
```
_No KDoc at source._

```kotlin
fun NavController.navigateToProfile(navOptions: NavOptions? = null) = navigate(ProfileRoute, navOptions)
```
_No KDoc at source._

```kotlin
fun NavGraphBuilder.profileDestination(profileBody: @Composable () -> Unit = {})
```
The profile backbone destination.

### `feature/profile/src/commonMain/kotlin/kpt/feature/profile/TestTags.kt`

```kotlin
object TestTags
```
Append-only test-tag registry for the profile feature. Consumed by Compose UI tests in `feature/profile/src/commonTest/`. APPEND-ONLY contract (RULE-KMP-COMPOSE-UITEST-001 CU-5).

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

- `const val SCREEN: String = "profile_screen"` — Root scaffold — always rendered regardless of content state.

### `feature/profile/src/commonMain/kotlin/org/mifospay/feature/profile/components/ProfileCard.kt`

```kotlin
fun ProfileDetailsCard(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/profile/src/commonMain/kotlin/org/mifospay/feature/profile/ProfileScreen.kt:143</code></summary>

```kotlin
        ProfileImage(bitmap = clientImage)

        ProfileDetailsCard(
            client = client,
            modifier = Modifier.fillMaxWidth(),
        )
```

</details>

```kotlin
fun ProfileItem(
```
_No KDoc at source._

### `feature/profile/src/commonMain/kotlin/org/mifospay/feature/profile/components/ProfileImage.kt`

```kotlin
fun ProfileImage(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/profile/src/commonMain/kotlin/org/mifospay/feature/profile/ProfileScreen.kt:141</code></summary>

```kotlin
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
    ) {
        ProfileImage(bitmap = clientImage)

        ProfileDetailsCard(
            client = client,
            modifier = Modifier.fillMaxWidth(),
```

</details>

```kotlin
fun EditableProfileImage(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/profile/src/commonMain/kotlin/org/mifospay/feature/profile/edit/EditProfileScreen.kt:120</code></summary>

```kotlin
        ) {
            item {
                EditableProfileImage(
                    profileImage = state.profileImage,
                    onPickImage = {
                        onAction(EditProfileAction.PickProfileImage)
                    },
```

</details>

### `feature/profile/src/commonMain/kotlin/org/mifospay/feature/profile/di/ProfileModule.kt`

```kotlin
val ProfileModule = module
```
_No KDoc at source._

### `feature/profile/src/commonMain/kotlin/org/mifospay/feature/profile/edit/EditProfileViewModel.kt`

```kotlin
sealed interface EditProfileEvent
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/profile/src/commonMain/kotlin/org/mifospay/feature/profile/edit/EditProfileScreen.kt:68</code></summary>

```kotlin
    EventsEffect(viewModel) { event ->
        when (event) {
            is EditProfileEvent.NavigateBack -> onBackClick.invoke()
            is EditProfileEvent.ShowToast -> {
                scope.launch {
                    val message = getString(event.message)
                    snackbarHostState.showSnackbar(message)
```

</details>

```kotlin
sealed interface EditProfileAction
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/profile/src/commonMain/kotlin/org/mifospay/feature/profile/edit/EditProfileScreen.kt:90</code></summary>

```kotlin
            dialogState = state.dialogState,
            onDismissRequest = remember(viewModel) {
                { viewModel.trySendAction(EditProfileAction.DismissErrorDialog) }
            },
        )
    }
}
```

</details>

### `feature/profile/src/commonMain/kotlin/org/mifospay/feature/profile/navigation/EditProfileNavigation.kt`

```kotlin
fun NavController.navigateToEditProfile(navOptions: NavOptions? = null) =
```
_No KDoc at source._

### `feature/profile/src/commonMain/kotlin/org/mifospay/feature/profile/navigation/ProfileNavigation.kt`

```kotlin
fun NavController.navigateToProfile(navOptions: NavOptions? = null)
```
_No KDoc at source._

```kotlin
fun NavGraphBuilder.profileScreen(
```
_No KDoc at source._

```kotlin
fun NavGraphBuilder.profileNavGraph(
```
_No KDoc at source._

---

_4 type(s), 12 function(s)/property(ies); 3 carry KDoc at source; 0 authored example(s); 6 live call site(s)._
<!-- api-docs:end -->
