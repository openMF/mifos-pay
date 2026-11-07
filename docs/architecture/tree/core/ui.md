# `core/ui`

> **Layer:** core — fork-owned; a codegen target
> **Corpus surface:** `CORE_UI.md`
> **Measured:** 70 Kotlin files, 0 test files

**Defines annotations:** `@DevicePreviews`, `@FeatureTab`

## Principal types

`AuthenticationUtils`, `BackgroundEvent`, `BaseViewModel`, `DevicePreviews`, `DialogState`, `DialogType`, `ErrorMessageProviderImpl`, `ErrorType`, `FeatureTab`, `FloatingActionButtonContent`, `InfoType`, `KptPullToRefreshState`, `LottieConstants`, `MimeType`  …and 17 more

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core/ui sha=fe10c2aaf5602cb12041e7a759af4f9442ddbd95 -->
## API reference

_Generated from `core/ui` at tree `fe10c2aaf560` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

### `core/ui/src/commonMain/kotlin/kpt/core/ui/AmountEditText.kt`

```kotlin
fun AmountEditText(
```
AmountEditText - Generic amount input component with validation

<details><summary>Used in the template — <code>feature/transfer-interbank/src/commonMain/kotlin/kpt/feature/transfer/interbank/screens/TransferDetailsScreen.kt:176</code></summary>

```kotlin
                        color = KptTheme.colorScheme.onSurfaceVariant,
                    )
                    AmountEditText(
                        value = amountLocal,
                        onValueChange = { doubleAmount ->
                            onAmountChanged.invoke(doubleAmount.toString())
                            amountLocal = doubleAmount.toString()
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/AvatarBox.kt`

```kotlin
fun AvatarBox(
```
Circular avatar showing the initials of `name`.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:373</code></summary>

```kotlin
                },
                leadingContent = {
                    AvatarBox(
                        icon = MifosIcons.Bank,
                        backgroundColor = KptTheme.colorScheme.secondaryContainer,
                    )
                },
```

</details>

```kotlin
fun AvatarBox(
```
Circular avatar showing an `icon`.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:373</code></summary>

```kotlin
                },
                leadingContent = {
                    AvatarBox(
                        icon = MifosIcons.Bank,
                        backgroundColor = KptTheme.colorScheme.secondaryContainer,
                    )
                },
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/DevicePreviews.kt`

```kotlin
annotation class DevicePreviews
```
Multipreview annotation that represents various device sizes. Add this annotation to a composable to render various devices.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/EmptyContentScreen.kt:243</code></summary>

```kotlin

/** Preview of the empty-state screen with a drawable image. */
@DevicePreviews
@Composable
fun EmptyContentScreenDrawableImagePreview() {
    MifosTheme {
        EmptyContentScreen(
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/DropdownBox.kt`

```kotlin
fun DropdownBox(
```
Prefer to use when text field is not read only

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AddBillScreen.kt:137</code></summary>

```kotlin
                )

                DropdownBox(
                    expanded = showBillerDropdown,
                    label = "Select Biller *",
                    value = state.formData.billerName ?: "Select a biller",
                    readOnly = true,
```

</details>

```kotlin
fun ExposedDropdownBox(
```
Read-only text field that opens a dropdown menu of `content` items.

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/signup/SignupScreen.kt:324</code></summary>

```kotlin

                // Country Dropdown
                ExposedDropdownBox(
                    expanded = isCountryDropdownExpanded,
                    label = stringResource(Res.string.feature_auth_country),
                    value = state.countryInput,
                    onExpandChange = { isCountryDropdownExpanded = it },
```

</details>

```kotlin
fun DropdownBoxItem(
```
Single selectable `text` row inside a dropdown menu.

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/signup/SignupScreen.kt:332</code></summary>

```kotlin
                ) {
                    state.countriesWithStates.keys.forEach { country ->
                        DropdownBoxItem(
                            text = country,
                            onClick = {
                                onAction(SignUpAction.CountryInputChange(country))
                                isCountryDropdownExpanded = false
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/EmptyContentScreen.kt`

```kotlin
fun EmptyContentScreen(
```
Empty-state screen with title, subtitle and a caller-provided image.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:194</code></summary>

```kotlin

                is AccountState.ViewState.Error -> {
                    EmptyContentScreen(
                        title = stringResource(Res.string.feature_accounts_error_oops),
                        subTitle = stringResource(Res.string.feature_accounts_unexpected_error_subtitle),
                        modifier = Modifier,
                        iconTint = KptTheme.colorScheme.error,
```

</details>

```kotlin
fun EmptyContentScreen(
```
Empty-state screen with title, subtitle, an image and an action button with icon.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:194</code></summary>

```kotlin

                is AccountState.ViewState.Error -> {
                    EmptyContentScreen(
                        title = stringResource(Res.string.feature_accounts_error_oops),
                        subTitle = stringResource(Res.string.feature_accounts_unexpected_error_subtitle),
                        modifier = Modifier,
                        iconTint = KptTheme.colorScheme.error,
```

</details>

```kotlin
fun EmptyContentScreen(
```
Empty-state screen with title, subtitle and a drawable-resource image.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:194</code></summary>

```kotlin

                is AccountState.ViewState.Error -> {
                    EmptyContentScreen(
                        title = stringResource(Res.string.feature_accounts_error_oops),
                        subTitle = stringResource(Res.string.feature_accounts_unexpected_error_subtitle),
                        modifier = Modifier,
                        iconTint = KptTheme.colorScheme.error,
```

</details>

```kotlin
fun EmptyContentScreen(
```
Empty-state screen with title, subtitle and a default artwork drawable.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:194</code></summary>

```kotlin

                is AccountState.ViewState.Error -> {
                    EmptyContentScreen(
                        title = stringResource(Res.string.feature_accounts_error_oops),
                        subTitle = stringResource(Res.string.feature_accounts_unexpected_error_subtitle),
                        modifier = Modifier,
                        iconTint = KptTheme.colorScheme.error,
```

</details>

```kotlin
fun EmptyContentScreen(
```
Empty-state screen with title, subtitle, an image-vector icon and an action button.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:194</code></summary>

```kotlin

                is AccountState.ViewState.Error -> {
                    EmptyContentScreen(
                        title = stringResource(Res.string.feature_accounts_error_oops),
                        subTitle = stringResource(Res.string.feature_accounts_unexpected_error_subtitle),
                        modifier = Modifier,
                        iconTint = KptTheme.colorScheme.error,
```

</details>

```kotlin
fun EmptyContentScreenDrawableImagePreview()
```
Preview of the empty-state screen with a drawable image.

```kotlin
fun EmptyContentScreenImageVectorPreview()
```
Preview of the empty-state screen with an image-vector icon.

### `core/ui/src/commonMain/kotlin/kpt/core/ui/ErrorBottomSheet.kt`

```kotlin
enum class ErrorType
```
Type of error to display in the bottom sheet. Determines the icon and color scheme used.

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/confirm/TransferConfirmScreen.kt:249</code></summary>

```kotlin
                title = stringResource(Res.string.feature_make_transfer_oops_title),
                message = stringResource(dialogState.message),
                errorType = ErrorType.VALIDATION,
                onDismiss = onDismissRequest,
                dismissButtonText = stringResource(Res.string.feature_transfer_cancel),
            )
        }
```

</details>

```kotlin
fun ErrorBottomSheet(
```
A professional error bottom sheet that displays error information with appropriate icons, colors, and action buttons.

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/confirm/TransferConfirmScreen.kt:246</code></summary>

```kotlin
        }
        is TransferConfirmState.DialogState.Error.ValidationError -> {
            ErrorBottomSheet(
                title = stringResource(Res.string.feature_make_transfer_oops_title),
                message = stringResource(dialogState.message),
                errorType = ErrorType.VALIDATION,
                onDismiss = onDismissRequest,
```

</details>

```kotlin
fun ErrorBottomSheet(
```
Overload for simple error messages without UiError.

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/confirm/TransferConfirmScreen.kt:246</code></summary>

```kotlin
        }
        is TransferConfirmState.DialogState.Error.ValidationError -> {
            ErrorBottomSheet(
                title = stringResource(Res.string.feature_make_transfer_oops_title),
                message = stringResource(dialogState.message),
                errorType = ErrorType.VALIDATION,
                onDismiss = onDismissRequest,
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/ErrorMessageProviderImpl.kt`

```kotlin
class ErrorMessageProviderImpl : ErrorMessageProvider
```
Default implementation of `ErrorMessageProvider` that uses Compose resources. This implementation provides localized error messages for HTTP status codes and network errors. It uses suspend functions to load string resources.

<details><summary>Example</summary>

```kotlin
val provider = ErrorMessageProviderImpl()
val uiError = UiError.from(exception, provider)
```

</details>

```kotlin
val DefaultErrorMessageProvider: ErrorMessageProvider = ErrorMessageProviderImpl()
```
Singleton instance of `ErrorMessageProviderImpl` for easy access.

<details><summary>Used in the template — <code>feature/editpassword/src/commonMain/kotlin/kpt/feature/editpassword/EditPasswordViewModel.kt:94</code></summary>

```kotlin

                    is SubmitState.Failed -> {
                        val uiError = submitState.error.toUiError(DefaultErrorMessageProvider)
                        mutableStateFlow.update {
                            it.copy(dialogState = EditPasswordDialog.ApiError(uiError))
                        }
                        submitPassword.reset()
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/ErrorScreenContent.kt`

```kotlin
fun ErrorScreenContent(
```
Error state with title, subtitle and a retry button wired to `onClickRetry`.

<details><summary>Used in the template — <code>feature/history/src/commonMain/kotlin/kpt/feature/history/transactions/SpecificTransactionsScreen.kt:104</code></summary>

```kotlin

                is STState.ViewState.Error -> {
                    ErrorScreenContent(
                        modifier = Modifier.align(Alignment.Center),
                        title = stringResource(Res.string.feature_history_error_oops),
                        subTitle = stringResource(Res.string.feature_history_error),
                    )
```

</details>

```kotlin
fun ErrorContentScreenDrawableImagePreview()
```
Preview of `ErrorScreenContent`.

### `core/ui/src/commonMain/kotlin/kpt/core/ui/ExpiryDateInput.kt`

```kotlin
fun ExpiryDateInput(
```
Four-digit card expiry (MM/YY) input rendered as individual character boxes.

<details><summary>Used in the template — <code>feature/upi-setup/src/commonMain/kotlin/kpt/feature/upi/setup/screens/DebitCardScreenContent.kt:83</code></summary>

```kotlin
        Spacer(modifier = Modifier.height(8.dp))

        ExpiryDateInput(
            date = expiryDate,
            onDateChange = { expiryDate = it },
            onDone = {
                onDone(cardNumber, "month, year", expiryDate)
```

</details>

```kotlin
fun FormattedDateView(
```
Single character box of the expiry input, with a slash separator after the second digit.

```kotlin
fun ExpiryDateInputPreview()
```
Preview of `ExpiryDateInput`.

### `core/ui/src/commonMain/kotlin/kpt/core/ui/HeadingTitle.kt`

```kotlin
fun VerifyStepHeader(
```
Step heading with a verified/unverified status icon next to `text`.

<details><summary>Used in the template — <code>feature/upi-setup/src/commonMain/kotlin/kpt/feature/upi/setup/screens/DebitCardScreen.kt:80</code></summary>

```kotlin
            ),
        ) {
            VerifyStepHeader("Debit Card Details ", verificationStatus)
            if (isContentVisible) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
```

</details>

```kotlin
fun VerifyStepHeaderVerifiedPreview()
```
Preview of `VerifyStepHeader` in the verified state.

```kotlin
fun VerifyStepHeaderUnverifiedPreview()
```
Preview of `VerifyStepHeader` in the unverified state.

### `core/ui/src/commonMain/kotlin/kpt/core/ui/InfoBottomSheet.kt`

```kotlin
enum class InfoType
```
Type of information to display in the bottom sheet. Determines the icon and color scheme used.

<details><summary>Used in the template — <code>feature/fast-mpay/src/commonMain/kotlin/kpt/feature/fastmpay/FastMpayScreen.kt:201</code></summary>

```kotlin
            title = stringResource(Res.string.core_ui_different_bank_title),
            message = stringResource(Res.string.core_ui_different_bank_message),
            infoType = InfoType.WARNING,
            onDismiss = {
                showBankMismatchSheet = false
                bankMismatchData = null
            },
```

</details>

```kotlin
fun InfoBottomSheet(
```
A professional info bottom sheet that displays informational content with appropriate icons, colors, and action buttons. Similar to `ErrorBottomSheet` but for informational/warning scenarios rather than errors.

<details><summary>Used in the template — <code>feature/fast-mpay/src/commonMain/kotlin/kpt/feature/fastmpay/FastMpayScreen.kt:198</code></summary>

```kotlin
    if (showBankMismatchSheet && bankMismatchData != null) {
        val mismatchData = bankMismatchData
        InfoBottomSheet(
            title = stringResource(Res.string.core_ui_different_bank_title),
            message = stringResource(Res.string.core_ui_different_bank_message),
            infoType = InfoType.WARNING,
            onDismiss = {
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/LocalTimeZone.kt`

```kotlin
val LocalTimeZone = compositionLocalOf { TimeZone.currentSystemDefault() }
```
TimeZone that can be provided with the TimeZoneMonitor. This way, it's not needed to pass every single composable the time zone to show in UI.

### `core/ui/src/commonMain/kotlin/kpt/core/ui/MifosDivider.kt`

```kotlin
fun MifosDivider(
```
Horizontal divider with configurable thickness and color.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/savingsaccount/AddEditSavingAccountScreen.kt:416</code></summary>

```kotlin

                            if (index != filteredLocalList.size - 1) {
                                MifosDivider(
                                    color = KptTheme.colorScheme.outlineVariant,
                                )
                            }
                        }
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/MifosPasswordField.kt`

```kotlin
fun MifosPasswordField(
```
Password field with a show/hide toggle whose visibility state is owned by the caller.

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/login/LoginScreen.kt:190</code></summary>

```kotlin
        )
        Spacer(modifier = Modifier.padding(horizontal = KptTheme.spacing.md))
        MifosPasswordField(
            label = stringResource(Res.string.feature_auth_password),
            value = state.password,
            onValueChange = {
                onAction(LoginAction.PasswordChanged(it))
```

</details>

```kotlin
fun MifosPasswordField(
```
Password field that manages its own show/hide toggle state.

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/login/LoginScreen.kt:190</code></summary>

```kotlin
        )
        Spacer(modifier = Modifier.padding(horizontal = KptTheme.spacing.md))
        MifosPasswordField(
            label = stringResource(Res.string.feature_auth_password),
            value = state.password,
            onValueChange = {
                onAction(LoginAction.PasswordChanged(it))
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/MifosProgressIndicator.kt`

```kotlin
fun MifosProgressIndicator(
```
Full-size Lottie loading animation.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:191</code></summary>

```kotlin
        ) {
            when (state) {
                is AccountState.ViewState.Loading -> MifosProgressIndicator()

                is AccountState.ViewState.Error -> {
                    EmptyContentScreen(
                        title = stringResource(Res.string.feature_accounts_error_oops),
```

</details>

```kotlin
fun MifosProgressIndicatorMini(
```
Small Lottie loading animation for inline use.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/fork/HomeScreen.kt:928</code></summary>

```kotlin

            if (transactionsLoading) {
                MifosProgressIndicatorMini()
            } else {
                if (transactions != null && transactions.isEmpty()) {
                    Text(
                        text = stringResource(Res.string.home_no_transactions_found),
```

</details>

```kotlin
fun MifosProgressIndicatorOverlay(
```
Lottie loading animation drawn over existing content.

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/login/LoginScreen.kt:105</code></summary>

```kotlin

    if (state.dialogState is LoginState.DialogState.Loading) {
        MifosProgressIndicatorOverlay()
    }
}

@Composable
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/MifosSearchBar.kt`

```kotlin
fun MifosSearchBar(
```
Search bar with query text, placeholder and optional clear button.

<details><summary>Used in the template — <code>feature/merchants/src/commonMain/kotlin/kpt/feature/merchants/ui/MerchantScreen.kt:207</code></summary>

```kotlin
    modifier: Modifier = Modifier,
) {
    MifosSearchBar(
        query = query,
        placeHolder = stringResource(Res.string.feature_merchants_search),
        onQueryChange = onQueryChange,
        onSearch = onSearch,
```

</details>

```kotlin
fun SimpleSearchBar(
```
Compact search field with query text and placeholder.

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/hub/IntraBankHubScreen.kt:154</code></summary>

```kotlin
                Spacer(Modifier.height(KptTheme.spacing.sm))

                SimpleSearchBar(
                    query = "",
                    placeHolder = stringResource(Res.string.feature_select_account_placeholder),
                    onQueryChange = {},
                    onClick = {
```

</details>

```kotlin
fun PreviewMifosSearchBar()
```
Preview of `MifosSearchBar`.

```kotlin
fun PreviewSimpleSearchBar()
```
Preview of `SimpleSearchBar`.

### `core/ui/src/commonMain/kotlin/kpt/core/ui/MifosSmallChip.kt`

```kotlin
fun MifosSmallChip(
```
Small non-interactive chip showing a `label`.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:501</code></summary>

```kotlin
@Composable
private fun StatusChip(label: String, color: Color) {
    MifosSmallChip(
        label = label,
        containerColor = color,
        contentColor = KptTheme.colorScheme.scrim,
    )
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/MifosUserImage.kt`

```kotlin
fun MifosUserImage(
```
Circular user picture from `bitmap`, falling back to the initials of `username` or a default icon.

### `core/ui/src/commonMain/kotlin/kpt/core/ui/NavGraphBuilderExtensions.kt`

```kotlin
fun NavGraphBuilder.composableWithSlideTransitions(
```
Registers a composable destination for `route` using the slide enter/exit transitions.

```kotlin
fun NavGraphBuilder.composableWithStayTransitions(
```
Registers a composable destination for `route` using the stay enter/exit transitions.

```kotlin
fun NavGraphBuilder.composableWithPushTransitions(
```
Registers a composable destination for `route` using the push enter/exit transitions.

```kotlin
fun NavGraphBuilder.composableWithRootPushTransitions(
```
Registers a composable destination for `route` using the root-push enter/exit transitions.

### `core/ui/src/commonMain/kotlin/kpt/core/ui/NavigationItem.kt`

```kotlin
interface NavigationItem
```
Represents a user-interactable item to navigate a user via the bottom app bar or navigation rail.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/bottombar/KptBottomBar.kt:30</code></summary>

```kotlin
@Composable
fun KptBottomBar(
    navigationItems: List<NavigationItem>,
    selectedItem: NavigationItem?,
    onClick: (NavigationItem) -> Unit,
    modifier: Modifier = Modifier,
    /** Show each tab's name under its icon. See `KptNavigationBarItem.labelRes`. */
```

</details>

- `val selectedIcon: ImageVector` — The resource ID for the icon representing the tab when it is selected.
- `val icon: ImageVector` — Resource id for the icon representing the tab.
- `val labelRes: StringResource` — Resource id for the label describing the tab.
- `val contentDescriptionRes: StringResource` — Resource id for the content description describing the tab.
- `val graphRoute: String` — Route of the tab's graph.
- `val startDestinationRoute: String` — Route of the tab's start destination.
- `val testTag: String` — The test tag of the tab.

### `core/ui/src/commonMain/kotlin/kpt/core/ui/NonLetterColorVisualTransformation.kt`

```kotlin
fun nonLetterColorVisualTransformation(): VisualTransformation
```
Remembers a visual transformation that colors digits and special characters differently from letters.

### `core/ui/src/commonMain/kotlin/kpt/core/ui/OtpTextField.kt`

```kotlin
fun OtpTextField(
```
Pure digit collector — it does NOT decide correctness itself. `onOtpEntered` fires once `otpCount` digits are typed (or on IME-done); the caller verifies the code against the real backend (e.g.

<details><summary>Used in the template — <code>feature/upi-setup/src/commonMain/kotlin/kpt/feature/upi/setup/screens/OtpScreen.kt:108</code></summary>

```kotlin

            is OtpRequestState.Sent, OtpRequestState.Idle -> {
                OtpTextField(
                    onOtpEntered = onOtpEntered,
                    modifier = Modifier.padding(top = KptTheme.spacing.lg),
                    isError = otpVerifyState is OtpVerifyState.Failed,
                    errorMessage = "Invalid OTP — please try again",
```

</details>

```kotlin
fun CharView(
```
Single box showing one OTP character at `index` of `text`.

```kotlin
fun PreviewOtpTextField()
```
Preview of the OTP text field.

### `core/ui/src/commonMain/kotlin/kpt/core/ui/PasswordStrengthIndicator.kt`

```kotlin
fun CombinedPasswordErrorCard(
```
Error card combining the strength bar, character counter and error text for a password.

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/signup/SignupScreen.kt:253</code></summary>

```kotlin
                )
                Spacer(modifier = Modifier.height(KptTheme.spacing.xs))
                CombinedPasswordErrorCard(
                    modifier = Modifier.fillMaxWidth(),
                    errors = state.passwordFeedback,
                    passwordStrengthState = state.passwordStrengthState,
                    currentCharacterCount = state.passwordInput.length,
```

</details>

```kotlin
fun PasswordStrengthIndicator(
```
Strength meter and hint text reflecting the given `PasswordStrengthState`.

<details><summary>Used in the template — <code>feature/editpassword/src/commonMain/kotlin/kpt/feature/editpassword/EditPasswordScreen.kt:143</code></summary>

```kotlin
                showPasswordChange = { showPassword = !showPassword },
            )
            PasswordStrengthIndicator(
                modifier = Modifier,
                state = state.passwordStrengthState,
                currentCharacterCount = state.newPasswordInput.length,
            )
```

</details>

```kotlin
enum class PasswordStrengthState
```
Password strength levels displayed by the indicator, from empty to very strong.

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/signup/SignupViewModel.kt:275</code></summary>

```kotlin
        if (action.password.isEmpty()) {
            mutableStateFlow.update {
                it.copy(passwordStrengthState = PasswordStrengthState.NONE)
            }
        } else {
            passwordStrengthJob = viewModelScope.launch {
                val result = PasswordChecker.getPasswordStrengthResult(action.password)
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/RevealSwipe.kt`

```kotlin
fun RevealSwipe(
```
Swipeable card that reveals a background action layer behind the foreground content.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:298</code></summary>

```kotlin
    )

    RevealSwipe(
        modifier = modifier,
        state = state,
        shape = KptTheme.shapes.small,
        backgroundCardStartColor = KptTheme.colorScheme.tertiary,
```

</details>

```kotlin
fun BaseRevealSwipe(
```
Lower-level reveal-swipe implementation that takes explicit shape and colors.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/input/RevealSwipe.kt:147</code></summary>

```kotlin
    var pressOffset by remember { mutableStateOf(DpOffset.Zero) }

    BaseRevealSwipe(
        shape = shape,
        backgroundCardStartColor = backgroundCardStartColor,
        backgroundCardEndColor = backgroundCardEndColor,
        card = card,
```

</details>

```kotlin
enum class RevealDirection
```
Directions in which a `RevealSwipe` can be swiped open.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:295</code></summary>

```kotlin
    val state = rememberRevealState(
        maxRevealDp = if (account.status.submittedAndPendingApproval) 105.dp else 75.dp,
        directions = setOf(RevealDirection.EndToStart),
    )

    RevealSwipe(
        modifier = modifier,
```

</details>

```kotlin
enum class RevealValue
```
Possible values of `RevealState`.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/input/RevealSwipe.kt:238</code></summary>

```kotlin
                        {
                            val isOpen =
                                state.anchoredDraggableState.targetValue != RevealValue.Default
                            // if open, just close. No click event.
                            if (isOpen) {
                                closeOnContentClickHandler()
                            } else {
```

</details>

```kotlin
fun rememberRevealState(
```
Create and `remember` a `RevealState` with the default animation clock.

```kotlin
data class RevealState(
```
Anchored-draggable state of a `RevealSwipe`.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:293</code></summary>

```kotlin
    onClickViewAccount: (Long) -> Unit,
) {
    val state = rememberRevealState(
        maxRevealDp = if (account.status.submittedAndPendingApproval) 105.dp else 75.dp,
        directions = setOf(RevealDirection.EndToStart),
    )
```

</details>

```kotlin
suspend fun RevealState.reset()
```
Reset the component to the default position, with an animation.

```kotlin
suspend fun RevealState.resetFast()
```
Reset the component to the default position, with an animation.

### `core/ui/src/commonMain/kotlin/kpt/core/ui/ScrollableTabRow.kt`

```kotlin
fun MifosScrollableTabRow(
```
Scrollable tab row linked to a pager, one tab per `TabContent`.

<details><summary>Used in the template — <code>feature/finance/src/commonMain/kotlin/kpt/feature/finance/FinanceScreen.kt:28</code></summary>

```kotlin

    Column(modifier = modifier.fillMaxSize()) {
        MifosScrollableTabRow(
            tabContents = tabContents,
            pagerState = pagerState,
        )
    }
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/TransactionFilterBottomSheet.kt`

```kotlin
fun TransactionFilterBottomSheet(
```
Bottom sheet for filtering transaction history by account and transaction type.

<details><summary>Used in the template — <code>feature/history/src/commonMain/kotlin/kpt/feature/history/HistoryScreen.kt:161</code></summary>

```kotlin
                }
                if (state.showFilter) {
                    TransactionFilterBottomSheet(
                        selectedAccount = state.selectedAccount,
                        accounts = state.accounts,
                        selectedTransactionType = state.selectedTransactionType,
                        onAccountSelected = {
```

</details>

```kotlin
fun TransactionTypeFilter(
```
Row of chips for selecting the transaction type filter.

```kotlin
fun FilterAccountDropDown(
```
Dropdown for selecting which account to filter transactions by.

### `core/ui/src/commonMain/kotlin/kpt/core/ui/TransactionHistoryCard.kt`

```kotlin
fun TransactionHistoryCard(
```
Card listing recent `Transaction`s with an optional "View all" action.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/savingsaccount/details/SavingAccountDetailScreen.kt:210</code></summary>

```kotlin

        item {
            TransactionHistoryCard(
                transactions = savingAccountDetail.transactions,
                onViewTransaction = onViewTransaction,
                showLeadingIcon = true,
                showViewAll = false,
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/TransactionItemCard.kt`

```kotlin
fun TransactionItemCard(
```
Card wrapping a single `Transaction` row, clickable to open its details.

```kotlin
fun TransactionItem(
```
Row showing a `Transaction` amount, type and date with an optional leading icon.

<details><summary>Used in the template — <code>feature/history/src/commonMain/kotlin/kpt/feature/history/components/TransactionList.kt:69</code></summary>

```kotlin
            key = { _, it -> it.transactionId },
        ) { i, items ->
            TransactionItem(
                transaction = items,
                modifier = Modifier,
                onClick = {
                    onAction(HistoryAction.ViewTransaction(it))
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/bottombar/KptBottomBar.kt`

```kotlin
fun KptBottomBar(
```
App-level bottom navigation bar, driven by the generated tab registry. Tabs register by `@FeatureTab` annotation — never by editing a list here, which would make adding a feature a two-place change and silently drop it if one were missed.

### `core/ui/src/commonMain/kotlin/kpt/core/ui/bottombar/KptNavigationBarItem.kt`

```kotlin
fun RowScope.KptNavigationBarItem(
```
One item in `KptBottomBar`: icon, label and selected state.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/bottombar/KptBottomBar.kt:44</code></summary>

```kotlin
    ) {
        navigationItems.forEach { navigationItem ->
            KptNavigationBarItem(
                contentDescriptionRes = navigationItem.contentDescriptionRes,
                selectedIcon = navigationItem.selectedIcon,
                unselectedIcon = navigationItem.icon,
                isSelected = selectedItem == navigationItem,
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/bottombar/KptNavigationRail.kt`

```kotlin
fun KptNavigationRail(
```
The side rail used instead of a bottom bar on wide windows — same tab source, different surface.

### `core/ui/src/commonMain/kotlin/kpt/core/ui/bottombar/KptNavigationRailItem.kt`

```kotlin
fun ColumnScope.KptNavigationRailItem(
```
One item in `KptNavigationRail`.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/bottombar/KptNavigationRail.kt:69</code></summary>

```kotlin
        ) {
            navigationItems.forEach { navigationItem ->
                KptNavigationRailItem(
                    contentDescriptionRes = navigationItem.contentDescriptionRes,
                    selectedIconRes = navigationItem.selectedIcon,
                    unselectedIconRes = navigationItem.icon,
                    isSelected = navigationItem == selectedItem,
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/input/PasswordStrengthIndicator.kt`

```kotlin
fun PasswordStrengthIndicator(
```
Live strength meter for a password field, from `PasswordChecker`'s verdict. Advisory only: it reflects the rules, it does not enforce them — validation belongs at submit.

<details><summary>Used in the template — <code>feature/editpassword/src/commonMain/kotlin/kpt/feature/editpassword/EditPasswordScreen.kt:143</code></summary>

```kotlin
                showPasswordChange = { showPassword = !showPassword },
            )
            PasswordStrengthIndicator(
                modifier = Modifier,
                state = state.passwordStrengthState,
                currentCharacterCount = state.newPasswordInput.length,
            )
```

</details>

```kotlin
enum class PasswordStrengthState
```
Rendered strength bands, from weakest to strongest.

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/signup/SignupViewModel.kt:275</code></summary>

```kotlin
        if (action.password.isEmpty()) {
            mutableStateFlow.update {
                it.copy(passwordStrengthState = PasswordStrengthState.NONE)
            }
        } else {
            passwordStrengthJob = viewModelScope.launch {
                val result = PasswordChecker.getPasswordStrengthResult(action.password)
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/input/RevealSwipe.kt`

```kotlin
fun RevealSwipe(
```
Swipe-to-reveal row: drag aside to expose actions (delete, archive) behind the content. The revealed actions must ALSO be reachable another way — a swipe is invisible to a screen reader and unavailable to keyboard or pointer users.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:298</code></summary>

```kotlin
    )

    RevealSwipe(
        modifier = modifier,
        state = state,
        shape = KptTheme.shapes.small,
        backgroundCardStartColor = KptTheme.colorScheme.tertiary,
```

</details>

```kotlin
fun BaseRevealSwipe(
```
Unstyled `RevealSwipe` core — gesture and offset handling with no chrome, for a caller that wants its own presentation.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/RevealSwipe.kt:141</code></summary>

```kotlin
    var pressOffset by remember { mutableStateOf(DpOffset.Zero) }

    BaseRevealSwipe(
        modifier = modifier.semantics {
            customActions = buildList {
                backgroundStartActionLabel?.let {
                    add(
```

</details>

```kotlin
enum class RevealDirection
```
Which side(s) a row may be dragged toward to reveal its actions.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:295</code></summary>

```kotlin
    val state = rememberRevealState(
        maxRevealDp = if (account.status.submittedAndPendingApproval) 105.dp else 75.dp,
        directions = setOf(RevealDirection.EndToStart),
    )

    RevealSwipe(
        modifier = modifier,
```

</details>

```kotlin
enum class RevealValue
```
Possible values of `RevealState`.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/RevealSwipe.kt:231</code></summary>

```kotlin
                        {
                            val isOpen =
                                state.anchoredDraggableState.targetValue != RevealValue.Default
                            // if open, just close. No click event.
                            if (isOpen) {
                                closeOnContentClickHandler()
                            } else {
```

</details>

```kotlin
fun rememberRevealState(
```
Create and `remember` a `RevealState` with the default animation clock.

```kotlin
data class RevealState(
```
Current offset and reveal state of one `RevealSwipe` row; hoist it to reset a row programmatically.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:293</code></summary>

```kotlin
    onClickViewAccount: (Long) -> Unit,
) {
    val state = rememberRevealState(
        maxRevealDp = if (account.status.submittedAndPendingApproval) 105.dp else 75.dp,
        directions = setOf(RevealDirection.EndToStart),
    )
```

</details>

```kotlin
suspend fun RevealState.reset()
```
Reset the component to the default position, with an animation.

```kotlin
suspend fun RevealState.resetFast()
```
Reset the component to the default position, with an animation.

### `core/ui/src/commonMain/kotlin/kpt/core/ui/navigation/FeatureTab.kt`

```kotlin
public annotation class FeatureTab
```
Marks a `NavigationItem` object as a bottom-navigation tab contributed by a feature.

### `core/ui/src/commonMain/kotlin/kpt/core/ui/navigation/NavigationItem.kt`

```kotlin
interface NavigationItem
```
Represents a user-interactable item to navigate a user via the bottom app bar or navigation rail.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/NavigationItem.kt:18</code></summary>

```kotlin
 * Represents a user-interactable item to navigate a user via the bottom app bar or navigation rail.
 */
interface NavigationItem {
    /**
     * The resource ID for the icon representing the tab when it is selected.
     */
    val selectedIcon: ImageVector
```

</details>

- `val selectedIcon: ImageVector` — The resource ID for the icon representing the tab when it is selected.
- `val icon: ImageVector` — Resource id for the icon representing the tab.
- `val labelRes: StringResource` — Resource id for the label describing the tab.
- `val contentDescriptionRes: StringResource` — Resource id for the content description describing the tab.
- `val graphRoute: String` — Route of the tab's graph.
- `val startDestinationRoute: String` — Route of the tab's start destination.
- `val testTag: String` — The test tag of the tab.
- `val inlineTab: Boolean get() = true` — Whether this tab renders INLINE inside the navbar scaffold — its content swaps within the inner NavHost so the bottom bar stays visible and the tab keeps its own back stack (exactly like the backbone Home/Profile tabs) — versus a FULL-SCREEN destination pushed on the outer authenticated graph (the bottom bar is hidden; e.g. an immersive focus-timer that declares `bottom_navigation_visible: false`). Default `true` (inline). A full-screen tab overrides to `false`. An inline extra tab (beyond Home/Profile) MUST also register its start destination via `ForkTabRegistry.extraInlineTabDestinations` so the inner NavHost can host it; otherwise the tab has nothing to render inline.

### `core/ui/src/commonMain/kotlin/kpt/core/ui/scaffold/KptForkScaffold.kt`

```kotlin
fun KptScaffold(
```
App screen scaffold for a screen with a BACK affordance — the nav icon is always shown and `onNavigationIconClick` is required.

<details><summary>Used in the template — <code>feature/profile/src/commonMain/kotlin/kpt/feature/profile/ProfileScreen.kt:30</code></summary>

```kotlin
    profileBody: @Composable () -> Unit = {},
) {
    KptScaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag(TestTags.Profile.SCREEN),
    ) {
```

</details>

```kotlin
fun KptScaffold(
```
App screen scaffold whose back affordance is CONDITIONAL — pass `showNavigationIcon` when the same screen is reachable both as a tab root and as a pushed destination.

<details><summary>Used in the template — <code>feature/profile/src/commonMain/kotlin/kpt/feature/profile/ProfileScreen.kt:30</code></summary>

```kotlin
    profileBody: @Composable () -> Unit = {},
) {
    KptScaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag(TestTags.Profile.SCREEN),
    ) {
```

</details>

```kotlin
fun KptScaffold(
```
Fully slot-based scaffold: the caller supplies `topBar`, `bottomBar` and the FAB composables.

<details><summary>Used in the template — <code>feature/profile/src/commonMain/kotlin/kpt/feature/profile/ProfileScreen.kt:30</code></summary>

```kotlin
    profileBody: @Composable () -> Unit = {},
) {
    KptScaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag(TestTags.Profile.SCREEN),
    ) {
```

</details>

```kotlin
data class FloatingActionButtonContent(
```
Declarative FAB spec — icon, description and click — so a caller configures the FAB without passing a composable slot.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/MifosScaffold.kt:53</code></summary>

```kotlin
    topBarTitle: String? = null,
    containerColor: Color = Color.Transparent,
    floatingActionButtonContent: FloatingActionButtonContent? = null,
    pullToRefreshState: MifosPullToRefreshState = rememberMifosPullToRefreshState(),
    snackbarHost: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit = {},
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/scaffold/KptPullToRefreshState.kt`

```kotlin
data class KptPullToRefreshState(
```
Data class representing the pull-to-refresh state and behavior.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/scaffold/KptForkScaffold.kt:57</code></summary>

```kotlin
    containerColor: Color = KptTheme.colorScheme.background,
    floatingActionButtonContent: FloatingActionButtonContent? = null,
    pullToRefreshState: KptPullToRefreshState = rememberKptPullToRefreshState(),
    contentWindowInsets: WindowInsets = ScaffoldDefaults
        .contentWindowInsets
//        .union(WindowInsets.displayCutout)
        .only(WindowInsetsSides.Horizontal),
```

</details>

```kotlin
fun rememberKptPullToRefreshState(
```
Remembers and returns a `KptPullToRefreshState` instance.

```kotlin
fun rememberKptPullToRefreshState(
```
Bridge: derive a `KptPullToRefreshState` from a `PagingScreenStream` so `KptScaffold` / `KptRootScaffold` can drive pull-to-refresh on paginated screens without the consumer wiring isRefreshing/onRefresh manually.

<details><summary>Example</summary>

```kotlin
KptScaffold(
    pullToRefreshState = rememberKptPullToRefreshState(viewModel.pagingStream),
    ...
) { ... }
// and disable PagingScreenContent's built-in pull-to-refresh:
PagingScreenContent(
    pagingStream = viewModel.pagingStream,
    onRetry = viewModel::onRetry,
    enablePullToRefresh = false,   // scaffold owns the gesture
) { items(coins) { ... } }
```

</details>

```kotlin
fun <T> rememberKptPullToRefreshState(
```
Bridge for non-paginated single-key screens driven by `ScreenDataStream`. Use with `KptScaffold` for detail pages that benefit from pull-to-refresh.

<details><summary>Example</summary>

```kotlin
KptScaffold(
    pullToRefreshState = rememberKptPullToRefreshState(
        stream = viewModel.stream,
        currentState = uiState,  // your collectAsStateWithLifecycle value
    ),
    ...
) { ... }
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/utility/AddBtnChip.kt`

```kotlin
fun AddCardChip(
```
Chip with text and an add button, used to prompt adding a card.

### `core/ui/src/commonMain/kotlin/kpt/core/ui/utility/DialogState.kt`

```kotlin
data class DialogState(
```
State of a confirmation dialog.

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

### `core/ui/src/commonMain/kotlin/kpt/core/ui/utility/DialogType.kt`

```kotlin
enum class DialogType
```
Kinds of confirmation dialog shown from profile and settings screens.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/utility/DialogState.kt:19</code></summary>

```kotlin
 */
data class DialogState(
    val type: DialogType = DialogType.NONE,
    val onConfirm: () -> Unit = {},
)
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/utility/TabContent.kt`

```kotlin
data class TabContent(
```
A tab together with its page content.

<details><summary>Used in the template — <code>feature/finance/src/commonMain/kotlin/kpt/feature/finance/FinanceScreen.kt:22</code></summary>

```kotlin
@Composable
internal fun FinanceRoute(
    tabContents: List<TabContent>,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { tabContents.size })
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/utils/AuthenticationUtils.kt`

```kotlin
object AuthenticationUtils
```
Default placeholder identity values used for the signed-in user.

<details><summary>Used in the template — <code>feature/passcode/src/commonMain/kotlin/kpt/feature/passcode/BiometricsSetupScreenViewmodel.kt:75</code></summary>

```kotlin
            val client = mutableStateFlow.value.client
            val result = platformAuthenticationProvider.registerUser(
                userName = client?.id?.run { toString() } ?: AuthenticationUtils.DEFAULT_USER_ID,
                emailId = client?.emailAddress ?: AuthenticationUtils.DEFAULT_USER_EMAIL,
                displayName = client?.displayName ?: AuthenticationUtils.DEFAULT_DISPLAY_NAME,
                title = promptStrings.title,
                subtitle = promptStrings.subtitle,
```

</details>

- `const val DEFAULT_USER_ID = "mifosUser"` — Default user id.
- `const val DEFAULT_USER_EMAIL = "mifospay@mifos.org"` — Default user email address.
- `const val DEFAULT_DISPLAY_NAME = "Mifos Pay User"` — Default user display name.

### `core/ui/src/commonMain/kotlin/kpt/core/ui/utils/BackgroundEvent.kt`

```kotlin
interface BackgroundEvent
```
Almost all the events in the app involve navigation or toasts. To prevent accidentally navigating to the same view twice, by default, events are ignored if the view is not currently resumed.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/SendMoneyOptionsViewModel.kt:93</code></summary>

```kotlin
     * @property data Raw scanned value.
     */
    data class QrCodeScanned(val data: String) : SendMoneyOptionsEvent, BackgroundEvent

    /**
     * Navigate to payee details for a scanned QR code.
     * @property qrCodeData Raw scanned QR value.
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/utils/BaseViewModel.kt`

```kotlin
abstract class BaseViewModel<S, E, A>(
```
A base `ViewModel` that helps enforce the unidirectional data flow pattern and associated responsibilities of a typical ViewModel: - Maintaining and emitting a current state (of type `S`) with the given `initialState`.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountViewModel.kt:52</code></summary>

```kotlin
    private val repository: SelfServiceRepository,
    private val json: Json,
) : BaseViewModel<AccountState, AccountEvent, AccountAction>(
    initialState = run {
        /** Client id read from user preferences when the view model is created. */
        val clientId = userRepository.clientId.value
```

</details>

- `val stateFlow: StateFlow<S> = mutableStateFlow.asStateFlow()` — A `StateFlow` representing state updates.
- `val eventFlow: Flow<E> = eventChannel.receiveAsFlow()` — A `Flow` of one-shot events. These may be received and consumed by only a single consumer. Any additional consumers will receive no events.
- `val actionChannel: SendChannel<A> = internalActionChannel` — A `SendChannel` for sending actions to the ViewModel for processing.
- `fun trySendAction(action: A)` — Convenience method for sending an action to the `actionChannel`.

### `core/ui/src/commonMain/kotlin/kpt/core/ui/utils/EventsEffect.kt`

```kotlin
fun <E> EventsEffect(
```
Convenience method for observing event flow from `BaseViewModel`. By default, events will only be consumed when the associated screen is resumed, to avoid bugs like duplicate navigation calls.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:111</code></summary>

```kotlin
    val accountState by viewModel.accountState.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
            is AccountEvent.OnAddEditSavingsAccount -> {
                onAddEditSavingsAccount(event.type)
            }
```

</details>

```kotlin
fun <E> EventsEffect(
```
`EventsEffect` over a bare event flow — pass `viewModel.eventFlow` rather than the view model, so a screen does not forward its view model into another composable.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:111</code></summary>

```kotlin
    val accountState by viewModel.accountState.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
            is AccountEvent.OnAddEditSavingsAccount -> {
                onAddEditSavingsAccount(event.type)
            }
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/utils/LifecycleEventEffect.kt`

```kotlin
fun LivecycleEventEffect(
```
Creates a side effect to observe lifecycle events.

<details><summary>Used in the template — <code>core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/effects/LifecycleEventEffect.kt:24</code></summary>

```kotlin
 */
@Composable
fun LivecycleEventEffect(
    onEvent: (owner: LifecycleOwner, event: Lifecycle.Event) -> Unit,
) {
    val eventHandler = rememberUpdatedState(onEvent)
    val lifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current)
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/utils/LottieConstants.kt`

```kotlin
object LottieConstants
```
Resource paths of bundled Lottie animations.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/MifosProgressIndicator.kt:42</code></summary>

```kotlin
    val composition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes(LottieConstants.LOADING_ANIMATION).decodeToString(),
        )
    }
    val progress by animateLottieCompositionAsState(composition)
```

</details>

- `const val LOADING_ANIMATION = "files/loading_animation.json"` — Path of the loading animation JSON file.

### `core/ui/src/commonMain/kotlin/kpt/core/ui/utils/PasswordChecker.kt`

```kotlin
object PasswordChecker
```
Scores a password against the app's rules and explains what is missing. Pure and platform-free, so the same verdict drives the indicator and any validation.

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/signup/SignupViewModel.kt:267</code></summary>

```kotlin
            it.copy(
                passwordInput = action.password,
                passwordFeedback = PasswordChecker.getPasswordFeedback(action.password)
                    .toPersistentList(),
            )
        }
        // Update password strength:
```

</details>

- `fun getPasswordStrengthResult(password: String): PasswordStrengthResult` — Validates then scores a password. Returns an error for empty or over-length input rather than a band, because neither has a meaningful strength.
- `val result = getPasswordStrength(password)`
- `fun getPasswordStrength(password: String): PasswordStrength` — Scores a password into a band from character-class variety, length and entropy.
- `val length = password.length`
- `val hasUpperCase = password.any { it.isUpperCase() }`
- `val hasLowerCase = password.any { it.isLowerCase() }`
- `val hasNumbers = password.any { it.isDigit() }`
- `val hasSymbols = password.any { !it.isLetterOrDigit() }`
- `val numTypesPresent =`
- `val entropyBits = calculateEntropy(password)`
- `val charPool = 26 + 26 + 10 + 33 // lowercase + uppercase + digits + symbols`
- `fun getPasswordFeedback(password: String): List<String>` — The unmet requirements, as user-facing sentences — what to fix, not just that it is weak. Empty when the password satisfies every rule.
- `val feedback = mutableListOf<String>()`

```kotlin
sealed class PasswordStrengthResult
```
The verdict: a strength band plus the unmet requirements, so the UI can say WHAT to fix rather than only that it is weak.

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/signup/SignupViewModel.kt:254</code></summary>

```kotlin
            }

            is ReceivePasswordStrengthResult -> handlePasswordStrengthResult(action)

            is SignUpAction.SubmitClick -> handleSubmitClick()

            is SignUpAction.LoadCountries -> loadCountriesFromJson()
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/utils/PasswordStrength.kt`

```kotlin
enum class PasswordStrength
```
Password strength bands used by `PasswordChecker`.

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/signup/SignupViewModel.kt:289</code></summary>

```kotlin
            is PasswordStrengthResult.Success -> {
                val updatedState = when (result.passwordStrength) {
                    PasswordStrength.LEVEL_0 -> PasswordStrengthState.WEAK_1
                    PasswordStrength.LEVEL_1 -> PasswordStrengthState.WEAK_2
                    PasswordStrength.LEVEL_2 -> PasswordStrengthState.WEAK_3
                    PasswordStrength.LEVEL_3 -> PasswordStrengthState.GOOD
                    PasswordStrength.LEVEL_4 -> PasswordStrengthState.STRONG
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/utils/PasswordStrengthExtensions.kt`

```kotlin
fun Int.toPasswordStrengthOrNull(): PasswordStrength? = when (this)
```
Converts the given `Int` to a `PasswordStrength`. A `null` value is returned if this value is not in the [0, 4] range.

```kotlin
fun PasswordStrength.toInt(): Int = when (this)
```
Converts the given `PasswordStrength` to an `Int`.

### `core/ui/src/commonMain/kotlin/kpt/core/ui/utils/ScreenshotUtils.kt`

```kotlin
expect object ScreenshotUtils
```
Platform-specific utilities for taking screenshots of Compose screens. This expect declaration should be implemented for each platform to handle the specifics of screenshot functionality.

<details><summary>Used in the template — <code>core/ui/src/androidMain/kotlin/kpt/core/ui/utils/ScreenshotUtils.android.kt:34</code></summary>

```kotlin
 * including the ability to exclude specific UI elements and generate context-aware filenames.
 */
actual object ScreenshotUtils {

    /**
     * Provider function to retrieve the current [Activity].
     * This must be set before using screenshot functionality.
```

</details>

- `suspend fun takeScreenshotAndShare(` — Takes a screenshot and immediately shares it using the platform's sharing mechanism. This overload allows passing context-specific details for better file naming.

```kotlin
data class ScreenshotContextDetails(
```
Context details for screenshots to enable better file naming.

<details><summary>Used in the template — <code>feature/send-money/src/androidMain/kotlin/kpt/feature/send/money/PaymentDetailsScreen.android.kt:58</code></summary>

```kotlin

                    val state = viewModel.stateFlow.value
                    val contextDetails = ScreenshotContextDetails(
                        screenType = "Payment Details",
                        amount = state.formattedAmount,
                        recipientName = state.payeeName,
                        timestamp = state.transactionDate,
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/utils/ShareUtils.kt`

```kotlin
expect object ShareUtils
```
Platform-specific utilities for sharing content such as text and files. This expect declaration should be implemented for each platform (e.g., Android, iOS) to handle the specifics of sharing functionality.

<details><summary>Used in the template — <code>feature/mpay-qr/src/commonMain/kotlin/kpt/feature/mpay/qr/MpayQrViewModel.kt:310</code></summary>

```kotlin
    private fun shareQrCode(data: ByteArray) {
        viewModelScope.launch {
            ShareUtils.shareFile(
                file = ShareFileModel(
                    fileName = "qr_code.png",
                    bytes = data,
                    mime = MimeType.IMAGE,
```

</details>

- `suspend fun shareText(text: String)` — Shares plain text content using the platform's native sharing mechanism.
- `suspend fun shareFile(file: ShareFileModel)` — Shares a file using the platform's native sharing mechanism. This is a suspend function, allowing for asynchronous operations such as file preparation or permission handling if needed.

```kotlin
enum class MimeType
```
Represents supported MIME types for file sharing.

<details><summary>Used in the template — <code>feature/mpay-qr/src/commonMain/kotlin/kpt/feature/mpay/qr/MpayQrViewModel.kt:314</code></summary>

```kotlin
                    fileName = "qr_code.png",
                    bytes = data,
                    mime = MimeType.IMAGE,
                ),
            )
        }
    }
```

</details>

```kotlin
data class ShareFileModel(
```
Model representing a file to be shared.

<details><summary>Used in the template — <code>feature/mpay-qr/src/commonMain/kotlin/kpt/feature/mpay/qr/MpayQrViewModel.kt:311</code></summary>

```kotlin
        viewModelScope.launch {
            ShareUtils.shareFile(
                file = ShareFileModel(
                    fileName = "qr_code.png",
                    bytes = data,
                    mime = MimeType.IMAGE,
                ),
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/utils/Transition.kt`

```kotlin
typealias EnterTransitionProvider =
```
_No KDoc at source._

<details><summary>Used in the template — <code>core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/util/Transition.kt:30</code></summary>

```kotlin
 * Used with [AnimatedContentTransitionScope] to define how a destination enters the screen.
 */
typealias EnterTransitionProvider =
    (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)

/**
 * Function type for providing nullable exit transitions in navigation.
```

</details>

```kotlin
typealias ExitTransitionProvider =
```
_No KDoc at source._

<details><summary>Used in the template — <code>core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/util/Transition.kt:37</code></summary>

```kotlin
 * Used with [AnimatedContentTransitionScope] to define how a destination exits the screen.
 */
typealias ExitTransitionProvider =
    (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)

/**
 * Function type for providing non-null enter transitions in navigation.
```

</details>

```kotlin
typealias NonNullEnterTransitionProvider =
```
_No KDoc at source._

<details><summary>Used in the template — <code>core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/util/Transition.kt:44</code></summary>

```kotlin
 * Used with [AnimatedContentTransitionScope] to define how a destination enters the screen.
 */
typealias NonNullEnterTransitionProvider =
    (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition)

/**
 * Function type for providing non-null exit transitions in navigation.
```

</details>

```kotlin
typealias NonNullExitTransitionProvider =
```
_No KDoc at source._

<details><summary>Used in the template — <code>core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/util/Transition.kt:51</code></summary>

```kotlin
 * Used with [AnimatedContentTransitionScope] to define how a destination exits the screen.
 */
typealias NonNullExitTransitionProvider =
    (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition)

/**
 * The default transition time (in milliseconds) for all fade transitions in the
```

</details>

```kotlin
val DEFAULT_STAY_TRANSITION_TIME_MS: Int =
```
The default transition time (in milliseconds) for all "stay"/no-op transitions in the `TransitionProviders`. This should be at least as large as any other transition that might also be happening during a navigation.

<details><summary>Used in the template — <code>core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/util/Transition.kt:79</code></summary>

```kotlin
 * navigation.
 */
val DEFAULT_STAY_TRANSITION_TIME_MS: Int =
    maxOf(
        DEFAULT_FADE_TRANSITION_TIME_MS,
        DEFAULT_SLIDE_TRANSITION_TIME_MS,
        DEFAULT_PUSH_TRANSITION_TIME_MS,
```

</details>

```kotlin
val AnimatedContentTransitionScope<NavBackStackEntry>.isSameGraphNavigation: Boolean
```
Checks if the parent of the destination before and after the navigation is the same. This is useful to ignore certain enter/exit transitions when navigating between distinct, nested flows.

```kotlin
object TransitionProviders
```
Contains standard "transition providers" that may be used to specify the `EnterTransition` and `ExitTransition` used when building a typical composable destination.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/NavGraphBuilderExtensions.kt:31</code></summary>

```kotlin
        arguments = arguments,
        deepLinks = deepLinks,
        enterTransition = TransitionProviders.Enter.slideUp,
        exitTransition = TransitionProviders.Exit.stay,
        popEnterTransition = TransitionProviders.Enter.stay,
        popExitTransition = TransitionProviders.Exit.slideDown,
        content = content,
```

</details>

- `val fadeIn: EnterTransitionProvider =` — Fades the new screen in. Note that this represents a `null` transition when navigating between different nested navigation graphs.
- `val pushLeft: EnterTransitionProvider =` — Slides the new screen in from the left of the screen.
- `val pushRight: EnterTransitionProvider =` — Slides the new screen in from the right of the screen.
- `val slideUp: EnterTransitionProvider =` — Slides the new screen in from the bottom of the screen. Note that this represents a `null` transition when navigating between different nested navigation graphs.
- `val stay: EnterTransitionProvider =` — A "no-op" transition: this changes nothing about the screen but "lasts" as long as other standard transitions in order to leave the screen in place such that it does not immediately appear while the other screen transitions away.
- `val fadeOut: ExitTransitionProvider =` — Fades the current screen out. Note that this represents a `null` transition when navigating between different nested navigation graphs.
- `val pushLeft: ExitTransitionProvider =` — Slides the current screen out to the left of the screen.
- `val pushRight: ExitTransitionProvider =` — Slides the current screen out to the right of the screen.
- `val slideDown: ExitTransitionProvider =` — Slides the current screen down to the bottom of the screen. Note that this represents a `null` transition when navigating between different nested navigation graphs.
- `val stay: ExitTransitionProvider =` — A "no-op" transition: this changes nothing about the screen but "lasts" as long as other standard transitions in order to leave the screen in place such that it does not immediately disappear while the other screen transitions into place.

```kotlin
object RootTransitionProviders
```
Contains standard "transition providers" that may be used to specify the `EnterTransition` and `ExitTransition` used when building a root `NavHost`, which requires a non-null value.

<details><summary>Used in the template — <code>core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/nav/NavGraphBuilderExtensions.kt:53</code></summary>

```kotlin
        typeMap = typeMap,
        deepLinks = deepLinks,
        enterTransition = RootTransitionProviders.Kpt.Enter.slideUp(motion),
        exitTransition = RootTransitionProviders.Kpt.Exit.stay(motion),
        popEnterTransition = RootTransitionProviders.Kpt.Enter.stay(motion),
        popExitTransition = RootTransitionProviders.Kpt.Exit.slideDown(motion),
        sizeTransform = null,
```

</details>

- `val fadeIn: NonNullEnterTransitionProvider =` — Fades the new screen in.
- `val none: NonNullEnterTransitionProvider =` — There is no transition for the entering screen.
- `val pushLeft: NonNullEnterTransitionProvider =` — Slides the new screen in from the left of the screen.
- `val totalTransitionDurationMs = DEFAULT_PUSH_TRANSITION_TIME_MS`
- `val pushRight: NonNullEnterTransitionProvider =` — Slides the new screen in from the right of the screen.
- `val totalTransitionDurationMs = DEFAULT_PUSH_TRANSITION_TIME_MS`
- `val slideUp: NonNullEnterTransitionProvider =` — Slides the new screen in from the bottom of the screen.
- `val stay: NonNullEnterTransitionProvider =` — A "no-op" transition: this changes nothing about the screen but "lasts" as long as other standard transitions in order to leave the screen in place such that it does not immediately appear while the other screen transitions away.
- `val fadeOut: NonNullExitTransitionProvider =` — Fades the current screen out.
- `val none: NonNullExitTransitionProvider =` — There is no transition for the exiting screen. Unlike the `stay` transition, this will immediately remove the outgoing screen even if there is an ongoing enter transition happening for the new screen.
- `val pushLeft: NonNullExitTransitionProvider =` — Slides the current screen out to the left of the screen.
- `val totalTransitionDurationMs = DEFAULT_PUSH_TRANSITION_TIME_MS`
- `val delayMs = totalTransitionDurationMs / 7`
- `val slideWithoutDelayMs = totalTransitionDurationMs - delayMs`
  _…more members; read the file._

### `core/ui/src/commonMain/kotlin/kpt/core/ui/utils/fork/PasswordChecker.kt`

```kotlin
object PasswordChecker
```
Evaluates password strength and produces improvement feedback.

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/signup/SignupViewModel.kt:267</code></summary>

```kotlin
            it.copy(
                passwordInput = action.password,
                passwordFeedback = PasswordChecker.getPasswordFeedback(action.password)
                    .toPersistentList(),
            )
        }
        // Update password strength:
```

</details>

- `fun getPasswordStrengthResult(password: String): PasswordStrengthResult` — Computes the `PasswordStrength` of `password` and wraps it in a `PasswordStrengthResult`.
- `val length = password.length`
- `val hasUpperCase = password.any { it.isUpperCase() }`
- `val hasLowerCase = password.any { it.isLowerCase() }`
- `val hasNumbers = password.any { it.isDigit() }`
- `val hasSymbols = password.any { !it.isLetterOrDigit() }`
- `val numTypesPresent =`
- `val entropyBits = calculateEntropy(password)`
- `val charPool = 26 + 26 + 10 + 33 // lowercase + uppercase + digits + symbols`
- `fun getPasswordFeedback(password: String): List<String>` — Returns human-readable hints on how to make `password` stronger.
- `val feedback = mutableListOf<String>()`

```kotlin
sealed class PasswordStrengthResult
```
Result of a password strength evaluation.

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/signup/SignupViewModel.kt:254</code></summary>

```kotlin
            }

            is ReceivePasswordStrengthResult -> handlePasswordStrengthResult(action)

            is SignUpAction.SubmitClick -> handleSubmitClick()

            is SignUpAction.LoadCountries -> loadCountriesFromJson()
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/utils/fork/PasswordStrength.kt`

```kotlin
enum class PasswordStrength
```
Password strength from weakest (level 0) to strongest (level 5).

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/signup/SignupViewModel.kt:289</code></summary>

```kotlin
            is PasswordStrengthResult.Success -> {
                val updatedState = when (result.passwordStrength) {
                    PasswordStrength.LEVEL_0 -> PasswordStrengthState.WEAK_1
                    PasswordStrength.LEVEL_1 -> PasswordStrengthState.WEAK_2
                    PasswordStrength.LEVEL_2 -> PasswordStrengthState.WEAK_3
                    PasswordStrength.LEVEL_3 -> PasswordStrengthState.GOOD
                    PasswordStrength.LEVEL_4 -> PasswordStrengthState.STRONG
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/utils/fork/PasswordStrengthExtensions.kt`

```kotlin
fun Int.toPasswordStrengthOrNull(): PasswordStrength? =
```
Converts the given `Int` to a `PasswordStrength`. A `null` value is returned if this value is not in the [0, 4] range.

```kotlin
fun PasswordStrength.toInt(): Int =
```
Converts the given `PasswordStrength` to an `Int`.

---

_41 type(s), 160 function(s)/property(ies); 173 carry KDoc at source; 3 authored example(s); 87 live call site(s)._
<!-- api-docs:end -->
