# `feature/profile/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 21 tracked files — 13× `.kt`, 3× `.xml`, 1× `.gitignore`, 1× `.md`, 1× `.kts`

**13 Kotlin files** — 2 screen(s), 2 ViewModel(s).

Screens: `EditProfileScreen`, `ProfileScreen`

ViewModels: `EditProfileViewModel`, `ProfileViewModel`

**Consumes:** `core/store/repository`

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

<!-- api-docs:begin module=feature/profile sha=bd42c8d051f984742f2727d1cdbe315805f71feb -->
## API reference

_Generated from `feature/profile` at tree `bd42c8d051f9` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/profile/src/commonMain/kotlin/kpt/feature/profile/ProfileRoute.kt`

```kotlin
data object ProfileRoute
```
Type-safe navigation route of the profile screen.

```kotlin
fun NavController.navigateToProfile(navOptions: NavOptions? = null) = navigate(ProfileRoute, navOptions)
```
Navigates to the profile screen.

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

### `feature/profile/src/commonMain/kotlin/kpt/feature/profile/components/ProfileCard.kt`

```kotlin
fun ProfileDetailsCard(
```
Card listing the client profile details such as username, email and mobile number.

<details><summary>Used in the template — <code>feature/profile/src/commonMain/kotlin/kpt/feature/profile/fork/ProfileScreen.kt:146</code></summary>

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
A labeled profile field showing a label above its value.

### `feature/profile/src/commonMain/kotlin/kpt/feature/profile/components/ProfileImage.kt`

```kotlin
fun ProfileImage(
```
Displays the client profile image decoded from a base64 string.

<details><summary>Used in the template — <code>feature/profile/src/commonMain/kotlin/kpt/feature/profile/fork/ProfileScreen.kt:144</code></summary>

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
Profile image that can be tapped to pick a new picture.

<details><summary>Used in the template — <code>feature/profile/src/commonMain/kotlin/kpt/feature/profile/edit/EditProfileScreen.kt:119</code></summary>

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

### `feature/profile/src/commonMain/kotlin/kpt/feature/profile/di/ProfileModule.kt`

```kotlin
val ProfileModule = module
```
Koin module providing the profile and edit-profile ViewModels.

### `feature/profile/src/commonMain/kotlin/kpt/feature/profile/edit/EditProfileViewModel.kt`

```kotlin
sealed interface EditProfileEvent
```
One-shot events emitted by the edit-profile ViewModel.

<details><summary>Used in the template — <code>feature/profile/src/commonMain/kotlin/kpt/feature/profile/edit/EditProfileScreen.kt:67</code></summary>

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
Actions dispatched to the edit-profile ViewModel.

<details><summary>Used in the template — <code>feature/profile/src/commonMain/kotlin/kpt/feature/profile/edit/EditProfileScreen.kt:89</code></summary>

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

### `feature/profile/src/commonMain/kotlin/kpt/feature/profile/navigation/EditProfileNavigation.kt`

```kotlin
fun NavController.navigateToEditProfile(navOptions: NavOptions? = null) =
```
Navigates to the edit-profile screen.

### `feature/profile/src/commonMain/kotlin/kpt/feature/profile/navigation/ProfileNavigation.kt`

```kotlin
fun NavController.navigateToProfile(navOptions: NavOptions? = null)
```
Navigates to the profile screen.

```kotlin
fun NavGraphBuilder.profileScreen(
```
Registers the profile screen destination.

```kotlin
fun NavGraphBuilder.profileNavGraph(
```
Registers the profile nested graph containing the profile and edit-profile screens.

---

_4 type(s), 12 function(s)/property(ies); 16 carry KDoc at source; 0 authored example(s); 6 live call site(s)._
<!-- api-docs:end -->
