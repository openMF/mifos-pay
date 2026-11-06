# `core/model`

> **Layer:** core — fork-owned; a codegen target
> **Corpus surface:** `CORE_MODEL.md`
> **Measured:** 154 Kotlin files, 2 test files

**Consumes contracts:** `@IgnoredOnParcel`, `@Parcelize`

## Principal types

`AccessToken`, `Account`, `AccountContent`, `AccountOption`, `AccountOptionsTemplate`, `AccountResult`, `AccountStatus`, `AccountTransferPayload`, `AccountType`, `AccountTypeOption`, `AccountsWithTransactions`, `Address`, `Amount`, `AppOrgLinks`  …and 170 more

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core/model sha=65d97333a8c2a05755941678f300467648ce7e37 -->
## API reference

_Generated from `core/model` at tree `65d97333a8c2` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

### `core/model/src/commonMain/kotlin/kpt/core/model/account/Account.kt`

```kotlin
data class Account(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountViewModel.kt:51</code></summary>

```kotlin
    initialState = run {
        val clientId = userRepository.clientId.value
        val defaultAccount = userRepository.defaultAccountId.value

        AccountState(
            clientId = clientId,
            defaultAccountId = defaultAccount,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/account/AccountContent.kt`

```kotlin
data class AccountContent(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountViewModel.kt:75</code></summary>

```kotlin
            } else {
                flowOf(
                    ScreenState.Error(IllegalStateException("Client ID not available")) as ScreenState<kpt.core.model.account.AccountContent>,
                )
            }
        }
        .mapLatest { screenState ->
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/account/AccountTransferPayload.kt`

```kotlin
data class AccountTransferPayload(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/mpay-qr/src/commonTest/kotlin/kpt/feature/mpay/qr/MpayQrViewModelTest.kt:509</code></summary>

```kotlin
        screenDataStreamForTesting(state = flowOf(StoreScreenState.Empty))

    override suspend fun makeTransfer(payload: AccountTransferPayload) {}
}

private class FakePocketRepository : PocketRepository {
    @OptIn(ExperimentalScreenDataStreamTestingApi::class)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/account/AccountsWithTransactions.kt`

```kotlin
data class AccountsWithTransactions(
```
_No KDoc at source._

### `core/model/src/commonMain/kotlin/kpt/core/model/account/DefaultAccount.kt`

```kotlin
data class DefaultAccount(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountViewModel.kt:221</code></summary>

```kotlin
    private fun handleSetDefaultAccount(action: AccountAction.SetDefaultAccount) {
        launchIO {
            userRepository.updateDefaultAccount(
                DefaultAccount(
                    accountId = action.accountId,
                    accountNo = action.accountNo,
                ),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/account/NewAccount.kt`

```kotlin
data class NewAccount(
```
_No KDoc at source._

### `core/model/src/commonMain/kotlin/kpt/core/model/account/RecentPayee.kt`

```kotlin
data class RecentPayee(
```
Represents a recently paid beneficiary/payee. Derived from transaction history and transfer details.

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/hub/IntraBankHubScreen.kt:344</code></summary>

```kotlin
private fun PreviewIntraBankHubScreenWithRecents() {
    val mockPayees = listOf(
        RecentPayee(
            clientId = 1,
            clientName = "John Doe",
            accountId = 101,
            accountNo = "ACC0017543",
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/autopay/AutoPay.kt`

```kotlin
data class AutoPay(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AddBillScreen.kt:241</code></summary>

```kotlin
                // AutoPay Section
                AutoPaySection(
                    enableAutoPay = state.formData.enableAutoPay,
                    paymentMethod = state.formData.autoPayPaymentMethod,
                    sourceAccount = state.formData.autoPaySourceAccount,
                    maxAmount = state.formData.autoPayMaxAmount,
                    paymentMethodError = state.validationResult.autoPayPaymentMethodError,
```

</details>

```kotlin
data class AutoPayTemplate(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repository/AutoPayRepository.kt:26</code></summary>

```kotlin
     * Get AutoPay template for creating new AutoPay schedules
     */
    fun getAutoPayTemplate(
        clientId: Long,
        sourceAccountId: Long,
    ): ScreenStateStream<AutoPayTemplate>
```

</details>

```kotlin
data class FrequencyOption(
```
_No KDoc at source._

```kotlin
data class PaymentMethod(
```
_No KDoc at source._

```kotlin
data class CurrencyOption(
```
_No KDoc at source._

```kotlin
data class AccountType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/beneficiary/src/commonMain/kotlin/kpt/feature/beneficiary/list/BeneficiaryListScreen.kt:320</code></summary>

```kotlin
        officeName = "Head Office",
        clientName = "Client $it",
        accountType = Beneficiary.AccountType(
            id = 1,
            code = "savings",
            value = "Savings",
        ),
```

</details>

```kotlin
enum class AutoPayStatus
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPayScheduleDetailsScreen.kt:103</code></summary>

```kotlin
                modifier = Modifier.padding(paddingValues),
                onPauseResume = {
                    if (state.schedule!!.status == AutoPayStatus.ACTIVE) {
                        viewModel.trySendAction(AutoPayScheduleDetailsAction.PauseSchedule)
                    } else {
                        viewModel.trySendAction(AutoPayScheduleDetailsAction.ResumeSchedule)
                    }
```

</details>

```kotlin
enum class PaymentStatus
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPayHistoryScreen.kt:145</code></summary>

```kotlin
    val statusColor = when (currentStatus) {
        // Green color for success
        PaymentStatus.COMPLETED -> Color(0xFF4CAF50)
        PaymentStatus.FAILED -> MaterialTheme.colorScheme.error
        PaymentStatus.PROCESSING -> MaterialTheme.colorScheme.tertiary
        PaymentStatus.PENDING -> MaterialTheme.colorScheme.tertiary
        PaymentStatus.UPCOMING -> MaterialTheme.colorScheme.primary
```

</details>

```kotlin
data class AutoPayGlobalSettings(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPaySettingsScreen.kt:138</code></summary>

```kotlin
@Composable
private fun GeneralSettingsSection(
    settings: kpt.core.model.autopay.AutoPayGlobalSettings,
    onToggleAutoPay: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsCard(
```

</details>

```kotlin
data class NotificationSettings(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPaySettingsScreen.kt:158</code></summary>

```kotlin
@Composable
private fun NotificationSettingsSection(
    settings: kpt.core.model.autopay.NotificationSettings,
    onSettingsChanged: (kpt.core.model.autopay.NotificationSettings) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsCard(
```

</details>

```kotlin
data class SecuritySettings(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPaySettingsScreen.kt:220</code></summary>

```kotlin
@Composable
private fun SecuritySettingsSection(
    settings: kpt.core.model.autopay.SecuritySettings,
    onSettingsChanged: (kpt.core.model.autopay.SecuritySettings) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsCard(
```

</details>

```kotlin
data class AutoPayRules(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPaySettingsScreen.kt:107</code></summary>

```kotlin

                AutoPayRulesSection(
                    rules = state.globalSettings.globalAutoPayRules,
                    onRulesChanged = { rules ->
                        viewModel.trySendAction(AutoPayPreferencesAction.UpdateAutoPayRules(rules))
                    },
                )
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/autopay/AutoPayPayload.kt`

```kotlin
data class AutoPayPayload(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repository/AutoPayRepository.kt:49</code></summary>

```kotlin
     */
    suspend fun createAutoPaySchedule(
        payload: AutoPayPayload,
    )

    /**
     * Update an existing AutoPay schedule. Throws on failure.
```

</details>

```kotlin
data class AutoPayUpdatePayload(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repository/AutoPayRepository.kt:57</code></summary>

```kotlin
    suspend fun updateAutoPaySchedule(
        autoPayId: Long,
        payload: AutoPayUpdatePayload,
    )

    /**
     * Delete an AutoPay schedule. Throws on failure.
```

</details>

```kotlin
data class AutoPayHistory(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPayHistoryScreen.kt:138</code></summary>

```kotlin
@Composable
private fun HistoryItemCard(
    historyItem: AutoPayHistory,
    modifier: Modifier = Modifier,
) {
    val currentStatus = historyItem.status
```

</details>

```kotlin
data class UpcomingPayment(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPayScreen.kt:523</code></summary>

```kotlin
@Composable
private fun UpcomingPaymentCard(
    payment: UpcomingPayment,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/autopay/Bill.kt`

```kotlin
data class Bill
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AddBillScreen.kt:102</code></summary>

```kotlin
        topBar = {
            MifosTopBar(
                topBarTitle = "Add New Bill",
                backPress = onNavigateBack,
            )
        },
    ) { paddingValues ->
```

</details>

```kotlin
enum class RecurrencePattern(val displayName: String, val interval: Int)
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AddBillScreen.kt:218</code></summary>

```kotlin
                    onExpandChange = { showRecurrenceDropdown = it },
                ) {
                    RecurrencePattern.entries.forEach { pattern ->
                        DropdownBoxItem(
                            text = pattern.displayName,
                            onClick = {
                                viewModel.trySendAction(AddBillAction.UpdateRecurrencePattern(pattern))
```

</details>

```kotlin
enum class BillStatus
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPayScheduleManagementScreen.kt:116</code></summary>

```kotlin
                    ScheduleManagementHeader(
                        totalSchedules = state.billsWithAutoPay.size,
                        activeSchedules = state.billsWithAutoPay.count { it.status == BillStatus.ACTIVE },
                    )
                }

                if (state.billsWithAutoPay.isEmpty()) {
```

</details>

```kotlin
data class BillFormData(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AddBillViewModel.kt:148</code></summary>

```kotlin

    private fun updateBillName(name: String) {
        val nameError = BillValidator.validateBillFormData(
            BillFormData(name = name),
        ).nameError
        mutableStateFlow.update {
            it.copy(
```

</details>

```kotlin
data class BillValidationResult(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AddBillViewModel.kt:203</code></summary>

```kotlin
    }

    private fun validateForm(): BillValidationResult {
        val currentState = stateFlow.value
        val formData = currentState.formData

        val validationResult = BillValidator.validateBillFormData(formData)
```

</details>

```kotlin
data class NextPaymentDate(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AddBillViewModel.kt:230</code></summary>

```kotlin
        }

        val nextDates = mutableListOf<NextPaymentDate>()
        val currentTime = Clock.System.now().toEpochMilliseconds()
        var nextDate = formData.dueDate

        // Generate next 5 payment dates
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/autopay/Biller.kt`

```kotlin
data class Biller
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AddBillScreen.kt:138</code></summary>

```kotlin
                DropdownBox(
                    expanded = showBillerDropdown,
                    label = "Select Biller *",
                    value = state.formData.billerName ?: "Select a biller",
                    readOnly = true,
                    isError = state.validationResult.billerError != null,
                    errorText = state.validationResult.billerError,
```

</details>

```kotlin
enum class BillerCategory(val displayName: String)
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AddBillerScreen.kt:157</code></summary>

```kotlin
                    onExpandChange = { showCategoryDropdown = it },
                ) {
                    BillerCategory.entries.forEach { category ->
                        DropdownBoxItem(
                            text = category.displayName,
                            onClick = {
                                viewModel.trySendAction(AddBillerAction.UpdateCategory(category))
```

</details>

```kotlin
data class BillerFormData(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AddBillerViewModel.kt:237</code></summary>

```kotlin
@Serializable
data class AddBillerState(
    val formData: BillerFormData = BillerFormData(),
    val validationResult: BillerValidationResult = BillerValidationResult(isValid = false),
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
```

</details>

```kotlin
data class BillerValidationResult(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AddBillerViewModel.kt:178</code></summary>

```kotlin
    }

    private fun validateForm(): BillerValidationResult {
        val currentState = stateFlow.value
        val formData = currentState.formData

        val validationResult = BillerValidator.validateBillerForm(formData)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/bank/Bank.kt`

```kotlin
data class Bank(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:373</code></summary>

```kotlin
                leadingContent = {
                    AvatarBox(
                        icon = MifosIcons.Bank,
                        backgroundColor = KptTheme.colorScheme.secondaryContainer,
                    )
                },
                trailingContent = {
```

</details>

```kotlin
enum class BankType
```
_No KDoc at source._

### `core/model/src/commonMain/kotlin/kpt/core/model/bank/BankAccountDetails.kt`

```kotlin
data class BankAccountDetails(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/upi-setup/src/commonMain/kotlin/kpt/feature/upi/setup/navigation/SetupUpiPinNavigation.kt:56</code></summary>

```kotlin
            type = type,
            index = index,
            bankAccountDetails = BankAccountDetails(
                accountNo = accountNo,
                bankName = null,
                accountHolderName = null,
                branch = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/beneficiary/Beneficiary.kt`

```kotlin
data class Beneficiary(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountViewModel.kt:178</code></summary>

```kotlin
            is AccountAction.EditBeneficiary -> {
                launchIO {
                    val beneficiary = json.encodeToString<Beneficiary>(action.beneficiary)
                    sendEvent(
                        AccountEvent.OnAddOrEditTPTBeneficiary(
                            BeneficiaryAddEditType.EditItem(beneficiary),
                        ),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/beneficiary/BeneficiaryPayload.kt`

```kotlin
data class BeneficiaryPayload(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/beneficiary/src/commonMain/kotlin/kpt/feature/beneficiary/addupdatebeneficiary/AddEditBeneficiaryViewModel.kt:342</code></summary>

```kotlin
        when (val addEditType = state.addEditType) {
            is BeneficiaryAddEditType.AddItem -> {
                val payload = BeneficiaryPayload(
                    name = state.name,
                    accountNumber = state.accountNumber,
                    transferLimit = state.transferLimit,
                    locale = state.locale,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/beneficiary/BeneficiaryUpdatePayload.kt`

```kotlin
data class BeneficiaryUpdatePayload(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/beneficiary/src/commonMain/kotlin/kpt/feature/beneficiary/addupdatebeneficiary/AddEditBeneficiaryViewModel.kt:361</code></summary>

```kotlin
            is BeneficiaryAddEditType.EditItem -> {
                val beneficiaryId = state.beneficiaryId ?: return
                val payload = BeneficiaryUpdatePayload(
                    name = state.name,
                    transferLimit = state.transferLimit,
                )
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/client/Client.kt`

```kotlin
data class Client(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountViewModel.kt:75</code></summary>

```kotlin
            } else {
                flowOf(
                    ScreenState.Error(IllegalStateException("Client ID not available")) as ScreenState<kpt.core.model.account.AccountContent>,
                )
            }
        }
        .mapLatest { screenState ->
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/client/ClientAccount.kt`

```kotlin
data class ClientAccount(
```
_No KDoc at source._

### `core/model/src/commonMain/kotlin/kpt/core/model/client/ClientAddress.kt`

```kotlin
data class ClientAddress(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/signup/SignupViewModel.kt:476</code></summary>

```kotlin
            mobileNo = state.mobileNumberInput,
            savingsProductId = state.savingsProductId,
            address = ClientAddress(
                addressLine1 = state.addressLine1Input,
                addressLine2 = state.addressLine2Input,
                postalCode = state.pinCodeInput,
                stateProvinceId = state.stateInput,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/client/ClientStatus.kt`

```kotlin
data class ClientStatus(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/mapper/ClientDetailsMapper.kt:42</code></summary>

```kotlin
        officeName = officeName ?: "",
        savingsProductName = savingsProductName ?: "",
        status = status?.toModel() ?: ClientStatus(),
        timeline = timeline?.toModel() ?: ClientTimeline(),
        legalForm = legalForm?.toModel() ?: ClientStatus(),
    )
}
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/client/ClientTimeline.kt`

```kotlin
data class ClientTimeline(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/mapper/ClientDetailsMapper.kt:43</code></summary>

```kotlin
        savingsProductName = savingsProductName ?: "",
        status = status?.toModel() ?: ClientStatus(),
        timeline = timeline?.toModel() ?: ClientTimeline(),
        legalForm = legalForm?.toModel() ?: ClientStatus(),
    )
}
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/client/NewClient.kt`

```kotlin
data class NewClient(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/signup/SignupViewModel.kt:470</code></summary>

```kotlin

        // 3. Create the client (rollback: delete the user on failure).
        val newClient = NewClient(
            firstname = state.firstNameInput,
            lastname = state.lastNameInput,
            externalId = state.userNameInput.plus("_client"),
            mobileNo = state.mobileNumberInput,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/client/UpdatedClient.kt`

```kotlin
data class UpdatedClient(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/fast-mpay/src/commonTest/kotlin/kpt/feature/fastmpay/TestFakes.kt:255</code></summary>

```kotlin
    override suspend fun setLanguage(language: Language) {}
    override suspend fun updateClientInfo(client: Client) {}
    override suspend fun updateClientProfile(client: UpdatedClient) {}

    override suspend fun updateDefaultAccount(account: DefaultAccount) {}

    override suspend fun updateSelectedInstance(instance: ServerInstance) {}
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/config/AppOrgLinks.kt`

```kotlin
object AppOrgLinks
```
The fork's own named organisation links — a community chat, a product site, a status page — for whatever surface the fork builds (an About screen, a help sheet). **SoT: `app-profile/app.yaml#org.links`** (`name: url`).

<details><summary>Example</summary>

```kotlin
AppOrgLinks["community"]?.let { url -> LinkRow("Community", url) }
```

</details>

<details><summary>Used in the template — <code>feature/onboarding/src/commonMain/kotlin/kpt/feature/onboarding/ui/OnboardingScreen.kt:151</code></summary>

```kotlin
        onOpenServerConfig = onOpenServerConfig,
        onOpenPrivacyPolicy = { urlLauncher.launchInApp(AppPrivacyConfig.PRIVACY_POLICY_URL) },
        onOpenTerms = { AppOrgLinks["terms"]?.let(urlLauncher::launchInApp) },
        modifier = modifier,
    )
}
```

</details>

- `val all: Map<String, String> get() = links` — Every declared link, in app-profile order.

### `core/model/src/commonMain/kotlin/kpt/core/model/config/AppPrivacyConfig.kt`

```kotlin
object AppPrivacyConfig
```
Consent defaults a fresh install starts with — analytics and crash reporting. **SoT: `app-profile/app.yaml#privacy`.** The block below is GENERATED by `./gradlew syncForkConfig`; change the YAML, not this file.

<details><summary>Used in the template — <code>feature/onboarding/src/commonMain/kotlin/kpt/feature/onboarding/ui/OnboardingScreen.kt:150</code></summary>

```kotlin
        onAction = viewModel::trySendAction,
        onOpenServerConfig = onOpenServerConfig,
        onOpenPrivacyPolicy = { urlLauncher.launchInApp(AppPrivacyConfig.PRIVACY_POLICY_URL) },
        onOpenTerms = { AppOrgLinks["terms"]?.let(urlLauncher::launchInApp) },
        modifier = modifier,
    )
}
```

</details>

- `const val ANALYTICS_ENABLED_BY_DEFAULT: Boolean = false` — `privacy.analytics_enabled_by_default` — whether a fresh install shares usage analytics.
- `const val CRASH_REPORTING_ENABLED_BY_DEFAULT: Boolean = false` — `privacy.crash_reporting_enabled_by_default` — whether a fresh install sends crash reports.
- `const val PRIVACY_POLICY_URL: String =` — `org.privacy_url` — the privacy policy page, opened in the in-app browser; empty when not set.

### `core/model/src/commonMain/kotlin/kpt/core/model/config/AppSecurityConfig.kt`

```kotlin
object AppSecurityConfig
```
Security defaults a fresh install starts with. **SoT: `app-profile/app.yaml#security`.** The block below is GENERATED by `./gradlew syncForkConfig`; change the YAML, not this file.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/user/UserData.kt:81</code></summary>

```kotlin
            useDynamicColor = false,
            appLanguage = LanguageConfig.DEFAULT,
            isAuthenticated = AppSecurityConfig.SIGNED_IN_BY_DEFAULT,
            isUnlocked = true,
            isPasscodeEnabled = false,
            isBiometricsEnabled = false,
            showOnboarding = false,
```

</details>

- `const val SCREEN_CAPTURE_ALLOWED_BY_DEFAULT: Boolean = false` — `security.screen_capture_allowed_by_default` — whether a fresh install allows screenshots + recording.
- `const val SIGNED_IN_BY_DEFAULT: Boolean = true` — `security.signed_in_by_default` — whether a fresh install starts signed in (false = the app has a login).
- `const val FIRST_RUN_ONBOARDING: Boolean = false` — `security.first_run_onboarding` — whether a fresh install shows onboarding once (false = skip it).

### `core/model/src/commonMain/kotlin/kpt/core/model/datatables/invoice/Invoice.kt`

```kotlin
data class Invoice(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/invoices/src/commonMain/kotlin/kpt/feature/invoices/InvoiceItem.kt:32</code></summary>

```kotlin
@Composable
internal fun InvoiceItem(
    invoice: Invoice,
    onClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/datatables/invoice/InvoiceEntity.kt`

```kotlin
data class InvoiceEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repository/InvoiceRepository.kt:62</code></summary>

```kotlin
    // SubmitHandler maps success/exception to SubmitState. The user-facing success
    // message is a feature StringResource surfaced by the ViewModel, not repo copy.
    suspend fun createInvoice(clientId: Long, invoice: InvoiceEntity)

    suspend fun updateInvoice(
        clientId: Long,
        invoiceId: Long,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/enums/AccountType.kt`

```kotlin
enum class AccountType(val value: String)
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/beneficiary/src/commonMain/kotlin/kpt/feature/beneficiary/list/BeneficiaryListScreen.kt:320</code></summary>

```kotlin
        officeName = "Head Office",
        clientName = "Client $it",
        accountType = Beneficiary.AccountType(
            id = 1,
            code = "savings",
            value = "Savings",
        ),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/instance/InstancesConfig.kt`

```kotlin
data class InstancesConfig(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/config/InstanceConfigLoader.kt:16</code></summary>

```kotlin
import kpt.core.model.instance.InstancesConfig
interface InstanceConfigLoader {
    suspend fun fetchInstancesConfig(): InstancesConfig
    fun observeInstancesConfig(): Flow<ScreenState<InstancesConfig>>
}
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/instance/InterbankServer.kt`

```kotlin
data class InterbankServer(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/auth/src/commonTest/kotlin/kpt/feature/auth/login/LoginViewModelTest.kt:59</code></summary>

```kotlin
    private val userPreferencesRepository: ForkPreferencesRepository = mock {
        every { selectedInstance } returns MutableStateFlow<ServerInstance?>(null)
        every { selectedInterbankInstance } returns MutableStateFlow<InterbankServer?>(null)
    }

    private lateinit var loginUseCase: LoginUseCase
    private lateinit var viewModel: LoginViewModel
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/instance/ServerInstance.kt`

```kotlin
data class ServerInstance(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/auth/src/commonTest/kotlin/kpt/feature/auth/login/LoginViewModelTest.kt:58</code></summary>

```kotlin
    private val clientRepository: ClientRepository = mock()
    private val userPreferencesRepository: ForkPreferencesRepository = mock {
        every { selectedInstance } returns MutableStateFlow<ServerInstance?>(null)
        every { selectedInterbankInstance } returns MutableStateFlow<InterbankServer?>(null)
    }

    private lateinit var loginUseCase: LoginUseCase
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/interbank/InterBankParticipant.kt`

```kotlin
data class InterBankParticipantRequest(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/InterBankRepositoryImpl.kt:35</code></summary>

```kotlin
        currencyCode: String,
    ): InterBankParticipantResponse = withContext(ioDispatcher) {
        val request = InterBankParticipantRequest(
            partyId = partyId.trim(),
            currencyCode = currencyCode,
            partyIdType = "MSISDN",
        )
```

</details>

```kotlin
data class InterBankParticipantResponse(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repository/InterBankRepository.kt:21</code></summary>

```kotlin
        partyId: String,
        currencyCode: String,
    ): InterBankParticipantResponse

    suspend fun fetchPartyInfo(
        partyId: String,
        currencyCode: String,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/interbank/InterBankPartyInfo.kt`

```kotlin
data class InterBankPartyInfoRequest(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/InterBankRepositoryImpl.kt:48</code></summary>

```kotlin
        ownerFspId: String,
    ): InterBankPartyInfoResponse = withContext(ioDispatcher) {
        val request = InterBankPartyInfoRequest(
            partyId = partyId.trim(),
            currencyCode = currencyCode,
            ownerFspId = ownerFspId,
            partyIdType = "MSISDN",
```

</details>

```kotlin
data class InterBankPartyInfoResponse(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/transfer-interbank/src/commonMain/kotlin/kpt/feature/transfer/interbank/InterbankTransferViewModel.kt:247</code></summary>

```kotlin
    }

    private fun showTransferDetails(info: InterBankPartyInfoResponse) = mutableStateFlow.update {
        it.copy(currentStep = InterbankTransferState.Step.TransferDetails, selectedParticipantInfo = info)
    }

    private fun showPreview() = mutableStateFlow.update {
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/interbank/InterBankTransfer.kt`

```kotlin
data class InterBankTransferRequest(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/transfer-interbank/src/commonMain/kotlin/kpt/feature/transfer/interbank/InterbankTransferViewModel.kt:399</code></summary>

```kotlin

        // Build InterBank transfer request from participantInfo and selected account
        val transferRequest = InterBankTransferRequest(
            homeTransactionId = Uuid.random().toString(),
            from = Party(
                fspId = participantInfo.sourceFspId,
                idType = participantInfo.partyIdType,
```

</details>

```kotlin
data class Party(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/transfer-interbank/src/commonMain/kotlin/kpt/feature/transfer/interbank/InterbankTransferViewModel.kt:401</code></summary>

```kotlin
        val transferRequest = InterBankTransferRequest(
            homeTransactionId = Uuid.random().toString(),
            from = Party(
                fspId = participantInfo.sourceFspId,
                idType = participantInfo.partyIdType,
                idValue = state.selectedFromAccount?.externalId
                    ?: state.selectedFromAccount?.number ?: "",
```

</details>

```kotlin
data class Amount(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AddBillScreen.kt:164</code></summary>

```kotlin

                MifosOutlinedTextField(
                    label = "Amount *",
                    value = state.formData.amount,
                    onValueChange = { viewModel.trySendAction(AddBillAction.UpdateAmount(it)) },
                    isError = state.validationResult.amountError != null,
                    errorMessage = state.validationResult.amountError,
```

</details>

```kotlin
data class TransactionType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/history/src/commonMain/kotlin/kpt/feature/history/HistoryScreen.kt:135</code></summary>

```kotlin
                    HistoryScreenHeader(
                        accountNo = state.selectedAccount?.number ?: "",
                        selectedTransactionType = state.selectedTransactionType,
                        isExportingPdf = state.isExportingPdf,
                        onFilterClick = {
                            onAction(HistoryAction.OnFilterClick)
                        },
```

</details>

```kotlin
data class InterBankTransferResponse(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/transfer-interbank/src/commonMain/kotlin/kpt/feature/transfer/interbank/InterbankTransferViewModel.kt:87</code></summary>

```kotlin
    // the Submitting/Submitted/Failed lifecycle; we observe it to drive this flow's
    // existing processing/success/failed steps + events, so the Screens are unchanged.
    private val submitTransfer = viewModelScope.submitHandler<InterBankTransferResponse>()
    private val submitLink = viewModelScope.submitHandler<Unit>()

    init {
        launchIO {
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/kyc/KYCLevel1Details.kt`

```kotlin
data class KYCLevel1Details(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/kyc/src/commonMain/kotlin/kpt/feature/kyc/KYCDescriptionViewModel.kt:45</code></summary>

```kotlin

    @OptIn(ExperimentalCoroutinesApi::class)
    val kycState = kycLevelRepository.fetchKYCLevel1Details(state.clientId).mapLatest { screenState ->
        // Fold ScreenState → the existing 3-branch KYCDescriptionUiState.
        // Empty is treated as Content(currentLevel = null) — no KYC record yet
        // means the user hasn't started onboarding. NoNetwork / Unauthenticated
        // fold into Error until Phase-4 differentiates them.
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/ClientResponseEntity.kt`

```kotlin
data class ClientResponseEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/main/services/ClientService.kt:64</code></summary>

```kotlin

    @POST(ApiEndPoints.CLIENTS)
    suspend fun createClient(@Body newClient: NewClientEntity): ClientResponseEntity

    @DELETE(ApiEndPoints.CLIENTS + "/{clientId}")
    suspend fun deleteClient(@Path("clientId") clientId: Int): ClientResponseEntity
}
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/CommonResponse.kt`

```kotlin
data class CommonResponse(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repository/UserRepository.kt:30</code></summary>

```kotlin
    suspend fun updateUserPassword(userId: Long, password: String)

    suspend fun deleteUser(userId: Int): CommonResponse

    suspend fun assignClientToUser(userId: Int, clientId: Int)
}
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/GenericResponse.kt`

```kotlin
data class GenericResponse(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repository/UserRepository.kt:23</code></summary>

```kotlin
    suspend fun getUser(): ScreenStateStream<UserWithRole>

    suspend fun updateUser(userId: Int, updatedUser: NewUser): ScreenStateStream<GenericResponse>

    // Writes throw on failure (DataState-free).
    suspend fun createUser(newUser: NewUser): Int
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/Page.kt`

```kotlin
data class Page<T>(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPayHistoryViewModel.kt:175</code></summary>

```kotlin
                                it.copy(
                                    isLoading = false,
                                    historyPage = null,
                                    error = null,
                                )
                            }
                        }
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/Role.kt`

```kotlin
data class Role(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/mpay-qr-scan/src/commonMain/kotlin/kpt/feature/mpay/qr/scan/components/QrActionButton.kt:44</code></summary>

```kotlin
    Column(
        modifier = modifier.semantics {
            role = Role.Button
            contentDescription = label
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/SearchedEntity.kt`

```kotlin
data class SearchedEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/mapper/SearchedEntitiesMapper.kt:14</code></summary>

```kotlin
import kpt.core.model.network.entity.SearchedEntity
import kpt.core.model.search.SearchResult
fun SearchedEntity.toModel(): SearchResult {
    return SearchResult(
        entityId = entityId,
        entityAccountNo = entityAccountNo,
        entityName = entityName,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/TPTResponse.kt`

```kotlin
data class TPTResponse(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repository/ThirdPartyTransferRepository.kt:23</code></summary>

```kotlin
    // Underlying network call remains a POST (a semantic write) but the
    // read-facing envelope is aligned with the rest of the fork.
    fun makeTransfer(payload: TransferPayload): ScreenStateStream<TPTResponse>
}
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/Timeline.kt`

```kotlin
data class Timeline(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/merchants/src/commonMain/kotlin/kpt/feature/merchants/ui/MerchantScreen.kt:305</code></summary>

```kotlin
            matured = false,
        ),
        timeline = Timeline(
            submittedOnDate = listOf(),
            submittedByUsername = "Lemuel Solomon",
            submittedByFirstname = "Vivian Henson",
            submittedByLastname = "Amalia Booker",
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/UserEntity.kt`

```kotlin
data class UserEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/UserRepositoryImpl.kt:63</code></summary>

```kotlin
        withContext(ioDispatcher) {
            selfServiceApiManager.userApi.updateUserPassword(
                updateUserEntity = UpdateUserEntityPassword(
                    password,
                    password,
                ),
            )
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/UserWithRole.kt`

```kotlin
data class UserWithRole(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repository/UserRepository.kt:19</code></summary>

```kotlin
interface UserRepository {
    // Phase-3 cutover — Flow-shaped surfaces on ScreenState.
    suspend fun getUsers(): ScreenStateStream<List<UserWithRole>>

    suspend fun getUser(): ScreenStateStream<UserWithRole>

    suspend fun updateUser(userId: Int, updatedUser: NewUser): ScreenStateStream<GenericResponse>
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/authentication/AuthenticationPayload.kt`

```kotlin
data class AuthenticationPayload(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/fast-mpay/src/commonTest/kotlin/kpt/feature/fastmpay/TestFakes.kt:115</code></summary>

```kotlin
    }

    override suspend fun loginSelf(payload: AuthenticationPayload): User =
        throw UnsupportedOperationException("Not used in fast-mpay tests")

    override fun getSelfClientDetails(clientId: Long): Flow<ScreenState<Client>> =
        flowOf(ScreenState.Empty)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/client/ClientAccountsEntity.kt`

```kotlin
data class ClientAccountsEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/mapper/AccountMapper.kt:17</code></summary>

```kotlin
import kpt.core.model.savingsaccount.SavingAccountEntity
import kpt.core.model.network.entity.templates.account.AccountType as NetworkAccountType
fun ClientAccountsEntity.toAccount(): List<Account> {
    return this.savingsAccounts.toAccount()
}

fun List<SavingAccountEntity>.toAccount(): List<Account> {
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/client/ClientEntity.kt`

```kotlin
data class ClientEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/mapper/ClientDetailsMapper.kt:25</code></summary>

```kotlin
import kpt.core.model.network.entity.client.Status
import kpt.core.model.network.entity.client.UpdateClientEntity
fun ClientEntity.toModel(): Client {
    return Client(
        id = id ?: 0,
        accountNo = accountNo ?: "",
        externalId = externalId ?: "",
```

</details>

```kotlin
data class ClientTimelineEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/mapper/ClientDetailsMapper.kt:87</code></summary>

```kotlin
}

fun ClientTimelineEntity.toModel(): ClientTimeline {
    return ClientTimeline(
        submittedOnDate = submittedOnDate,
        activatedOnDate = activatedOnDate,
        activatedByUsername = activatedByUsername,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/client/Currency.kt`

```kotlin
data class Currency(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/fork/HomeScreen.kt:949</code></summary>

```kotlin
            balance = 1000.0,
            id = 1L,
            currency = Currency(
                code = "USD",
                name = "US Dollar",
                decimalPlaces = 2,
                displaySymbol = "$",
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/client/DepositTypeEntity.kt`

```kotlin
data class DepositTypeEntity(
```
_No KDoc at source._

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/client/GuarantorAccountEntity.kt`

```kotlin
data class GuarantorAccountEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/network/entity/client/ClientAccountsEntity.kt:22</code></summary>

```kotlin
    val shareAccounts: List<ShareAccountResponseDto> = emptyList(),
    val groupLoanIndividualMonitoringAccounts: List<String?> = emptyList(),
    val guarantorAccounts: List<GuarantorAccountEntity> = emptyList(),
) {

    fun withSavingsAccounts(savingsAccounts: List<SavingAccountEntity>): ClientAccountsEntity {
        this.savingsAccounts = savingsAccounts
```

</details>

```kotlin
data class LoanStatusEntity(
```
_No KDoc at source._

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/client/NewClientEntity.kt`

```kotlin
data class NewClientEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/mapper/ClientDetailsMapper.kt:57</code></summary>

```kotlin
}

fun NewClient.toEntity(): NewClientEntity {
    return NewClientEntity(
        firstname = firstname,
        lastname = lastname,
        externalId = externalId,
```

</details>

```kotlin
data class Address(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/auth/src/commonTest/kotlin/kpt/feature/auth/login/LoginViewModelTest.kt:284</code></summary>

```kotlin
        displayName = "John Doe",
        mobileNo = "+1234567890",
        emailAddress = "john@example.com",
        dateOfBirth = emptyList(),
        isStaff = false,
        officeId = 1,
        officeName = "Head Office",
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/client/Status.kt`

```kotlin
data class Status(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:480</code></summary>

```kotlin
@Composable
private fun SavingAccountStatusCard(
    status: Status,
    modifier: Modifier = Modifier,
) {
    val activeStatus = SavingAccountStatus.entries.filter { it.isActive(status) }
    FlowRow(
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/client/Type.kt`

```kotlin
data class Type(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/savingsaccount/AddEditSavingAccountScreen.kt:429</code></summary>

```kotlin
                    value = state.nominalAnnualInterestRate.toString(),
                    label = stringResource(Res.string.feature_accounts_add_nominal_annual),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    onValueChange = {
                        onAction(AESAction.NominalAnnualInterestRateChanged(it))
                    },
                    onClickClearIcon = {
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/client/UpdateClientEntity.kt`

```kotlin
data class UpdateClientEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/mapper/ClientDetailsMapper.kt:97</code></summary>

```kotlin
}

fun UpdatedClient.toEntity(): UpdateClientEntity {
    return UpdateClientEntity(
        firstname = firstname,
        lastname = lastname,
        externalId = externalId,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/common/CurrencyResponseDto.kt`

```kotlin
data class CurrencyResponseDto(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/network/entity/loanAccount/LoanAccountResponseDto.kt:51</code></summary>

```kotlin
    val amountPaid: Double? = null,

    val currency: CurrencyResponseDto? = null,

    val inArrears: Boolean? = null,

    val summary: LoanSummaryResponseDto? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/common/TypeResponseDto.kt`

```kotlin
data class TypeResponseDto(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/network/entity/loanAccount/LoanAccountResponseDto.kt:43</code></summary>

```kotlin
    val status: LoanStatusResponseDto? = null,

    val loanType: TypeResponseDto? = null,

    val loanCycle: Int? = null,

    val loanBalance: Double? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/loanAccount/LoanAccountResponseDto.kt`

```kotlin
data class LoanAccountResponseDto(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/network/entity/client/ClientAccountsEntity.kt:19</code></summary>

```kotlin
data class ClientAccountsEntity(
    var savingsAccounts: List<SavingAccountEntity> = emptyList(),
    val loanAccounts: List<LoanAccountResponseDto> = emptyList(),
    val shareAccounts: List<ShareAccountResponseDto> = emptyList(),
    val groupLoanIndividualMonitoringAccounts: List<String?> = emptyList(),
    val guarantorAccounts: List<GuarantorAccountEntity> = emptyList(),
) {
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/loanAccount/LoanStatusResponseDto.kt`

```kotlin
data class LoanStatusResponseDto(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/mapper/pocket/PocketAccountMapper.kt:34</code></summary>

```kotlin
)

fun kpt.core.model.network.entity.loanAccount.LoanStatusResponseDto.toAccountStatus(): kpt.core.model.pocket.AccountStatus =
    when {
        active == true -> kpt.core.model.pocket.AccountStatus.ACTIVE
        pendingApproval == true -> kpt.core.model.pocket.AccountStatus.PENDING
        waitingForDisbursal == true -> kpt.core.model.pocket.AccountStatus.APPROVED
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/loanAccount/LoanSummaryResponseDto.kt`

```kotlin
data class LoanSummaryResponseDto(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/network/entity/loanAccount/LoanAccountResponseDto.kt:55</code></summary>

```kotlin
    val inArrears: Boolean? = null,

    val summary: LoanSummaryResponseDto? = null,

    val loanPurposeName: String? = null,

    val timeline: LoanTimelineResponseDto? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/loanAccount/LoanTimelineResponseDto.kt`

```kotlin
data class LoanTimelineResponseDto(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/network/entity/loanAccount/LoanAccountResponseDto.kt:59</code></summary>

```kotlin
    val loanPurposeName: String? = null,

    val timeline: LoanTimelineResponseDto? = null,
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/mifoserror/MifosError.kt`

```kotlin
data class MifosError(
```
Represents the standard error response format from Mifos/Fineract server.

<details><summary>Example</summary>

```kotlin
{
  "developerMessage": "The resource with id 123 was not found.",
  "httpStatusCode": "404",
  "defaultUserMessage": "Account not found",
  "userMessageGlobalisationCode": "error.msg.account.not.found",
  "errors": [
    {
      "developerMessage": "Account ID is invalid",
      "defaultUserMessage": "The account does not exist",
      "userMessageGlobalisationCode": "error.msg.account.invalid",
      "parameterName": "accountId"
    }
  ]
}
```

</details>

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/SelfServiceRepositoryImpl.kt:128</code></summary>

```kotlin
            .getAccounts(clientId, Constants.SAVINGS)
            .map { it.toAccount() }
            .asScreenStateFlow(errorBodyParser = parseMifosError, isEmpty = { it.isEmpty() })
            .flowOn(dispatcher)
    }

    // Network-only aggregation lifted into ScreenState via `asScreenStateFlow`
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/mifoserror/MifosErrorDetail.kt`

```kotlin
data class MifosErrorDetail(
```
Represents a specific error detail in the Mifos/Fineract error response. This is typically used for validation errors where multiple fields may have issues.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/network/entity/mifoserror/MifosError.kt:41</code></summary>

```kotlin
    val defaultUserMessage: String? = null,
    val userMessageGlobalisationCode: String? = null,
    val errors: List<MifosErrorDetail> = emptyList(),
) {
    /**
     * Returns the most appropriate user-facing error message.
     * Priority: First error's defaultUserMessage > root defaultUserMessage > developerMessage > fallback
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/noncore/Document.kt`

```kotlin
data class Document(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/kyc/src/commonMain/kotlin/kpt/feature/kyc/KYCLevel2ViewModel.kt:158</code></summary>

```kotlin
        }

        else -> uploadDocument()
    }

    /**
     * API call to upload document fails with the following error:
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/payload/ClientPayload.kt`

```kotlin
data class ClientPayload(
```
_No KDoc at source._

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/payload/DataTablePayload.kt`

```kotlin
data class DataTablePayload(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/network/entity/payload/ClientPayload.kt:32</code></summary>

```kotlin
    val dateFormat: String? = "DD_MMMM_YYYY",
    val locale: String? = "en",
    val datatables: List<DataTablePayload> = ArrayList(),
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/payload/PayPayload.kt`

```kotlin
data class PayPayload(
```
_No KDoc at source._

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/payload/PayResponse.kt`

```kotlin
data class PayResponse(
```
_No KDoc at source._

```kotlin
data class Change(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/common/src/commonMain/kotlin/kpt/core/common/Constants.kt:154</code></summary>

```kotlin
    const val UPI_PIN_SETUP_COMPLETED_SUCCESSFULLY = "UPI PIN Setup Completed Successfully"
    const val FORGOT = "Forgot"
    const val CHANGE = "Change"
    const val OTP = "otp"
    const val SETUP_UPI_PIN = "Setup UPI PIN"
    const val SETTING_UP_UPI_PIN = "Setting up UPI PIN.."
    const val UPDATED_BANK_ACCOUNT = "Updated Bank Account"
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/payload/TransferPayload.kt`

```kotlin
data class TransferPayload(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/confirm/TransferConfirmViewModel.kt:578</code></summary>

```kotlin
        get() = description.trim().isNotEmpty()

    val transferPayload: TransferPayload
        get() = TransferPayload(
            fromOfficeId = selectedAccount?.officeId,
            fromClientId = selectedAccount?.clientId,
            fromAccountType = selectedAccount?.accountType?.id,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/payload/UpdateVpaPayload.kt`

```kotlin
data class UpdateVpaPayload(
```
_No KDoc at source._

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/pocket/PocketRequestDto.kt`

```kotlin
data class PocketLinkRequest(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/PocketRepositoryImp.kt:137</code></summary>

```kotlin
            try {
                // Try to push to network directly to avoid overhead of waiting for the Fetcher
                val request = PocketLinkRequest(
                    accountsDetail = explicitlyAddedAccounts.map {
                        PocketLinkRequest.AccountDetail(
                            accountId = it.pocket.accountId.toString(),
                            accountType = it.pocket.accountType.name,
```

</details>

```kotlin
data class PocketCommandResponse(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/main/services/PocketService.kt:29</code></summary>

```kotlin
        @Query("command") command: String = "linkAccounts",
        @Body request: PocketLinkRequest,
    ): PocketCommandResponse

    @POST(ApiEndPoints.POCKETS)
    suspend fun delinkAccounts(
        @Query("command") command: String = "delinkAccounts",
```

</details>

```kotlin
data class PocketDelinkRequest(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/PocketRepositoryImp.kt:190</code></summary>

```kotlin
                val serverIds = accountsToDelink.filter { it.id > 0 }.map { it.id }
                if (serverIds.isNotEmpty()) {
                    val request = PocketDelinkRequest(serverIds)
                    dataManager.pocketApi.delinkAccounts(request = request)
                }

                // If successful, we can just delete them from Room (or mark them as SYNCED
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/pocket/PocketResponseDto.kt`

```kotlin
data class PocketResponseDto(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/mapper/pocket/PocketAccountMapper.kt:16</code></summary>

```kotlin
import kpt.core.model.network.entity.pocket.PocketResponseDto
import kpt.core.model.pocket.PocketAccount
fun PocketResponseDto.toDomainList(): List<PocketAccount> {
    val all = mutableListOf<PocketAccount>()

    loanAccounts.forEach { all.add(it.toDomain(AccountType.LOAN)) }
    savingsAccounts.forEach { all.add(it.toDomain(AccountType.SAVINGS)) }
```

</details>

```kotlin
data class PocketAccountDto(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/mapper/pocket/PocketAccountMapper.kt:26</code></summary>

```kotlin
}

private fun PocketAccountDto.toDomain(type: AccountType) = PocketAccount(
    id = this.id ?: 0L,
    pocketId = this.pocketId ?: 0L,
    accountId = this.accountId ?: 0L,
    accountType = type,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/register/RegisterPayload.kt`

```kotlin
data class RegisterPayload(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repository/RegistrationRepository.kt:15</code></summary>

```kotlin
import kpt.core.model.network.entity.register.UserVerify
interface RegistrationRepository {
    suspend fun registerUser(registerPayload: RegisterPayload)

    suspend fun verifyUser(userVerify: UserVerify)
}
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/register/UserVerify.kt`

```kotlin
data class UserVerify(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repository/RegistrationRepository.kt:17</code></summary>

```kotlin
    suspend fun registerUser(registerPayload: RegisterPayload)

    suspend fun verifyUser(userVerify: UserVerify)
}
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/shareAccount/ShareAccountResponseDto.kt`

```kotlin
data class ShareAccountResponseDto(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/network/entity/client/ClientAccountsEntity.kt:20</code></summary>

```kotlin
    var savingsAccounts: List<SavingAccountEntity> = emptyList(),
    val loanAccounts: List<LoanAccountResponseDto> = emptyList(),
    val shareAccounts: List<ShareAccountResponseDto> = emptyList(),
    val groupLoanIndividualMonitoringAccounts: List<String?> = emptyList(),
    val guarantorAccounts: List<GuarantorAccountEntity> = emptyList(),
) {
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/shareAccount/ShareStatusResponseDto.kt`

```kotlin
data class ShareStatusResponseDto(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/mapper/pocket/PocketAccountMapper.kt:59</code></summary>

```kotlin
    }

fun kpt.core.model.network.entity.shareAccount.ShareStatusResponseDto.toAccountStatus(): kpt.core.model.pocket.AccountStatus =
    when {
        active == true -> kpt.core.model.pocket.AccountStatus.ACTIVE
        submittedAndPendingApproval == true -> kpt.core.model.pocket.AccountStatus.PENDING
        approved == true -> kpt.core.model.pocket.AccountStatus.APPROVED
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/shareAccount/ShareSummaryResponseDto.kt`

```kotlin
data class ShareSummaryResponseDto(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/network/entity/shareAccount/ShareWithAssociationsResponseDto.kt:27</code></summary>

```kotlin
    val currency: CurrencyResponseDto? = null,
    val timeline: ShareTimelineResponseDto? = null,
    val summary: ShareSummaryResponseDto? = null,

    val currentMarketPrice: Double? = null,
    val savingsAccountId: Long? = null,
    val savingsAccountNumber: String? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/shareAccount/ShareTimelineResponseDto.kt`

```kotlin
data class ShareTimelineResponseDto(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/network/entity/shareAccount/ShareAccountResponseDto.kt:35</code></summary>

```kotlin
    val currency: CurrencyResponseDto? = null,

    val timeline: ShareTimelineResponseDto? = null,

)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/shareAccount/ShareWithAssociationsResponseDto.kt`

```kotlin
data class ShareWithAssociationsResponseDto(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/main/services/ShareAccountService.kt:24</code></summary>

```kotlin
        @Path("accountId") accountId: Long,
        @Query("associations") associations: String = "all",
    ): Flow<ShareWithAssociationsResponseDto>
}
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/signup/City.kt`

```kotlin
data class City(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/SearchIfscViewModel.kt:239</code></summary>

```kotlin
    DummyBankBranch("Koregaon Park", "Maharashtra"),
    DummyBankBranch("Banjara Hills", "Telangana"),
    DummyBankBranch("Salt Lake City", "West Bengal"),
    DummyBankBranch("Satellite", "Gujarat"),
    DummyBankBranch("Malviya Nagar", "Rajasthan"),
    DummyBankBranch("Gomti Nagar", "Uttar Pradesh"),
    DummyBankBranch("Sector 17", "Chandigarh"),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/signup/Country.kt`

```kotlin
data class Country(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/auth/src/commonTest/kotlin/kpt/feature/auth/signup/SignUpViewModelTest.kt:706</code></summary>

```kotlin
    @Test
    fun givenMissingState_whenSubmitClicked_thenErrorDialogShown() = runTest {
        enterCountry()
        viewModel.trySendAction(SignUpAction.SubmitClick)
        advanceUntilIdle()
        assertTrue(viewModel.stateFlow.value.dialogState is SignUpState.DialogState.Error)
    }
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/signup/State.kt`

```kotlin
data class State(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountViewModel.kt:49</code></summary>

```kotlin
    private val json: Json,
) : BaseViewModel<AccountState, AccountEvent, AccountAction>(
    initialState = run {
        val clientId = userRepository.clientId.value
        val defaultAccount = userRepository.defaultAccountId.value

        AccountState(
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/templates/account/AccountOption.kt`

```kotlin
data class AccountOption(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/confirm/TransferConfirmScreen.kt:415</code></summary>

```kotlin
@Composable
private fun FromAccountCard(
    account: AccountOption,
    balance: String,
    isOpened: Boolean,
    onAction: (TransferConfirmAction) -> Unit,
    modifier: Modifier = Modifier,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/templates/account/AccountOptionsTemplate.kt`

```kotlin
data class AccountOptionsTemplate(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/hub/IntraBankHubViewModel.kt:72</code></summary>

```kotlin

            is IntraBankHubAction.OnPayBeneficiary -> {
                val matchedAccount = state.toAccountOptionsTemplate
                    ?.firstOrNull {
                        it.accountNo == action.beneficiary.accountNumber
                    }
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/templates/account/AccountType.kt`

```kotlin
data class AccountType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/beneficiary/src/commonMain/kotlin/kpt/feature/beneficiary/list/BeneficiaryListScreen.kt:320</code></summary>

```kotlin
        officeName = "Head Office",
        clientName = "Client $it",
        accountType = Beneficiary.AccountType(
            id = 1,
            code = "savings",
            value = "Savings",
        ),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/templates/beneficiary/AccountTypeOption.kt`

```kotlin
data class AccountTypeOption(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/network/entity/templates/beneficiary/BeneficiaryTemplate.kt:16</code></summary>

```kotlin
@Serializable
data class BeneficiaryTemplate(
    val accountTypeOptions: List<AccountTypeOption?>? = null,
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/templates/beneficiary/BeneficiaryTemplate.kt`

```kotlin
data class BeneficiaryTemplate(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repository/BeneficiaryRepository.kt:22</code></summary>

```kotlin
    suspend fun getBeneficiaryList(): Flow<ScreenState<List<Beneficiary>>>

    suspend fun getBeneficiaryTemplate(): Flow<ScreenState<BeneficiaryTemplate>>

    // Writes execute the network call and throw on failure (no offline write
    // stores yet). Callers surface errors via their own try/catch.
    suspend fun createBeneficiary(beneficiaryPayload: BeneficiaryPayload)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/user/NewUserEntity.kt`

```kotlin
class NewUserEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/mapper/UserMapper.kt:24</code></summary>

```kotlin
val NEW_USER_ROLE_IDS: ArrayList<Int> = arrayListOf(MOBILE_WALLET_ROLE_ID, SUPER_USER_ROLE_ID)

fun NewUser.toEntity(): NewUserEntity {
    return NewUserEntity(
        username = username,
        firstname = firstname,
        lastname = lastname,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/user/UpdateUserEntityClients.kt`

```kotlin
data class UpdateUserEntityClients(
```
_No KDoc at source._

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/user/UpdateUserEntityEmail.kt`

```kotlin
data class UpdateUserEntityEmail(
```
_No KDoc at source._

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/user/UpdateUserEntityPassword.kt`

```kotlin
data class UpdateUserEntityPassword(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/UserRepositoryImpl.kt:63</code></summary>

```kotlin
        withContext(ioDispatcher) {
            selfServiceApiManager.userApi.updateUserPassword(
                updateUserEntity = UpdateUserEntityPassword(
                    password,
                    password,
                ),
            )
```

</details>

```kotlin
data class UpdateUserPasswordResponse(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/main/services/UserService.kt:41</code></summary>

```kotlin
    suspend fun updateUserPassword(
        @Body updateUserEntity: UpdateUserEntityPassword,
    ): UpdateUserPasswordResponse

    @DELETE(ApiEndPoints.USER + "/{userId}")
    suspend fun deleteUser(
        @Path("userId") userId: Int,
```

</details>

```kotlin
data class Changes(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/infra/SyncTestSupport.kt:56</code></summary>

```kotlin
        MutableStateFlow(NetworkStatus.Available(NetworkInfo(type = NetworkType.WiFi, isMetered = false))).asStateFlow()
    override val isOnline: StateFlow<Boolean> = MutableStateFlow(true).asStateFlow()
    override val networkChanges: SharedFlow<NetworkChangeEvent> =
        MutableSharedFlow<NetworkChangeEvent>().asSharedFlow()
    override fun close() = Unit
}
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/entity/user/User.kt`

```kotlin
data class User(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/login/LoginViewModel.kt:123</code></summary>

```kotlin

            is LoginAction.LoginClicked -> {
                loginUser(state.username, state.password)
            }

            is LoginAction.SignupClicked -> {
                sendEvent(LoginEvent.NavigateToSignup)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/twofactor/AccessToken.kt`

```kotlin
data class AccessToken(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repository/TwoFactorAuthRepository.kt:21</code></summary>

```kotlin
    suspend fun requestOTP(deliveryMethod: String): ScreenStateStream<String>

    suspend fun validateToken(token: String): ScreenStateStream<AccessToken>
}
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/network/twofactor/DeliveryMethod.kt`

```kotlin
data class DeliveryMethod(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repository/TwoFactorAuthRepository.kt:17</code></summary>

```kotlin
interface TwoFactorAuthRepository {
    // Phase-3 cutover — Flow-shaped 2FA reads/submissions on ScreenState.
    suspend fun deliveryMethods(): ScreenStateStream<List<DeliveryMethod>>

    suspend fun requestOTP(deliveryMethod: String): ScreenStateStream<String>

    suspend fun validateToken(token: String): ScreenStateStream<AccessToken>
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/notification/Notification.kt`

```kotlin
data class Notification(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/notification/src/commonMain/kotlin/kpt/feature/notification/NotificationScreen.kt:63</code></summary>

```kotlin
@VisibleForTesting
internal fun NotificationScreen(
    state: ScreenState<List<Notification>>,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit,
) {
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/notification/NotificationPayload.kt`

```kotlin
data class NotificationPayload(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/main/services/NotificationService.kt:18</code></summary>

```kotlin
interface NotificationService {
    @GET("notifications/")
    fun fetchNotifications(@Query("isRead") isRead: Boolean): Flow<NotificationPayload>
}
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/office/Office.kt`

```kotlin
data class Office(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/auth/src/commonTest/kotlin/kpt/feature/auth/login/LoginViewModelTest.kt:266</code></summary>

```kotlin
        authenticated = true,
        officeId = 1,
        officeName = "Main Office",
        roles = emptyList(),
        permissions = emptyList(),
        clients = listOf(1),
        shouldRenewPassword = false,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/payload/PocketLinkPayload.kt`

```kotlin
data class PocketLinkPayload(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/pocket/src/commonTest/kotlin/kpt/feature/pocket/viewmodels/Fakes.kt:131</code></summary>

```kotlin
    var lastDetailedForceRefresh: Boolean? = null
    var lastAvailableClientId: Long? = null
    var lastLinkPayload: PocketLinkPayload? = null
    var lastExplicitlyAddedAccounts: List<DetailedPocketAccount> = emptyList()
    var lastLinkClientId: Long? = null
    var lastDelinkMappingIds: List<Long> = emptyList()
    var lastDelinkClientId: Long? = null
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/pocket/PocketAccount.kt`

```kotlin
data class PocketAccount(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/mpay-qr/src/commonMain/kotlin/kpt/feature/mpay/qr/MpayQrViewModel.kt:438</code></summary>

```kotlin
            }
            val detailedAccount = DetailedPocketAccount(
                pocket = PocketAccount(
                    pocketId = 0,
                    id = 0,
                    accountId = accountId,
                    accountType = AccountType.SAVINGS,
```

</details>

```kotlin
data class DetailedPocketAccount(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/mpay-qr/src/commonMain/kotlin/kpt/feature/mpay/qr/MpayQrViewModel.kt:437</code></summary>

```kotlin
                return
            }
            val detailedAccount = DetailedPocketAccount(
                pocket = PocketAccount(
                    pocketId = 0,
                    id = 0,
                    accountId = accountId,
```

</details>

```kotlin
data class LinkableAccount(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/mpay-qr/src/commonTest/kotlin/kpt/feature/mpay/qr/MpayQrViewModelTest.kt:533</code></summary>

```kotlin
        clientId: Long,
        scope: CoroutineScope,
    ): ScreenDataStream<List<LinkableAccount>> =
        screenDataStreamForTesting(state = flowOf(StoreScreenState.Empty))

    override suspend fun linkAccounts(
        explicitlyAddedAccounts: List<DetailedPocketAccount>,
```

</details>

```kotlin
enum class AccountStatus
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/mpay-qr/src/commonMain/kotlin/kpt/feature/mpay/qr/MpayQrViewModel.kt:451</code></summary>

```kotlin
                status = state.selectedAccount?.status?.let {
                    when {
                        it.active -> AccountStatus.ACTIVE
                        it.approved -> AccountStatus.APPROVED
                        it.rejected -> AccountStatus.REJECTED
                        it.closed -> AccountStatus.CLOSED
                        it.submittedAndPendingApproval -> AccountStatus.PENDING
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/savedcards/CardPayload.kt`

```kotlin
data class CardPayload(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/savedcards/src/commonMain/kotlin/kpt/feature/savedcards/createOrUpdate/AddEditCardViewModel.kt:349</code></summary>

```kotlin

    @Transient
    val cardPayload = CardPayload(
        firstName = firstName,
        lastName = lastName,
        cardNumber = cardNumber,
        cvv = cvv,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/savedcards/SavedCard.kt`

```kotlin
data class SavedCard(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/savedcards/src/commonMain/kotlin/kpt/feature/savedcards/CardsScreen.kt:149</code></summary>

```kotlin
@Composable
internal fun CardsScreen(
    state: ScreenState<List<SavedCard>>,
    snackbarHostState: SnackbarHostState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    onAction: (CardAction) -> Unit,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/savingsaccount/AccountType.kt`

```kotlin
data class AccountType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/beneficiary/src/commonMain/kotlin/kpt/feature/beneficiary/list/BeneficiaryListScreen.kt:320</code></summary>

```kotlin
        officeName = "Head Office",
        clientName = "Client $it",
        accountType = Beneficiary.AccountType(
            id = 1,
            code = "savings",
            value = "Savings",
        ),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/savingsaccount/BlockUnblockResponseEntity.kt`

```kotlin
data class BlockUnblockResponseEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/main/services/SavingsAccountsService.kt:51</code></summary>

```kotlin
        @Path("accountId") accountId: Long,
        @Query("command") command: String?,
    ): BlockUnblockResponseEntity

    @GET(
        ApiEndPoints.SAVINGS_ACCOUNTS + "/{accountId}/" + ApiEndPoints.TRANSACTIONS +
            "/{transactionId}",
```

</details>

```kotlin
data class ChangesEntity(
```
_No KDoc at source._

### `core/model/src/commonMain/kotlin/kpt/core/model/savingsaccount/CreateNewSavingEntity.kt`

```kotlin
data class CreateNewSavingEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/savingsaccount/AddEditSavingViewModel.kt:439</code></summary>

```kotlin
        ) : ViewState {

            val createSavingEntity: CreateNewSavingEntity
                get() = CreateNewSavingEntity(
                    clientId = clientId,
                    productId = productId,
                    nominalAnnualInterestRate = nominalAnnualInterestRate,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/savingsaccount/Currency.kt`

```kotlin
data class Currency(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/fork/HomeScreen.kt:949</code></summary>

```kotlin
            balance = 1000.0,
            id = 1L,
            currency = Currency(
                code = "USD",
                name = "US Dollar",
                decimalPlaces = 2,
                displaySymbol = "$",
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/savingsaccount/DepositType.kt`

```kotlin
data class DepositType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/merchants/src/commonMain/kotlin/kpt/feature/merchants/ui/MerchantScreen.kt:280</code></summary>

```kotlin
        id = 1L,
        accountNo = "123456789",
        depositType = DepositType(
            id = 9994,
            code = "iriure",
            value = "liber",
        ),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/savingsaccount/InterestPeriod.kt`

```kotlin
data class InterestPeriod(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/merchants/src/commonMain/kotlin/kpt/feature/merchants/ui/MerchantScreen.kt:365</code></summary>

```kotlin
            blockDebit = false,
        ),
        interestCompoundingPeriodType = InterestPeriod(),
        interestPostingPeriodType = InterestPeriod(),
        interestCalculationType = InterestPeriod(),
        interestCalculationDaysInYearType = InterestPeriod(),
        lienAllowed = false,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/savingsaccount/PaymentDetailData.kt`

```kotlin
data class PaymentDetailData(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/mapper/TransactionMapper.kt:49</code></summary>

```kotlin
        },
        paymentDetailData = this.paymentDetailData?.let {
            Transaction.PaymentDetailData(
                id = it.id,
                paymentType = it.paymentType?.toModalUi(),
                accountNumber = it.accountNumber ?: "",
                checkNumber = it.checkNumber ?: "",
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/savingsaccount/PaymentType.kt`

```kotlin
data class PaymentType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/mapper/TransactionMapper.kt:62</code></summary>

```kotlin
}

fun PaymentType.toModalUi(): Transaction.PaymentType {
    return Transaction.PaymentType(
        id = this.id ?: 0,
        name = this.name ?: "",
        isSystemDefined = this.isSystemDefined ?: false,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/savingsaccount/SavingAccount.kt`

```kotlin
data class SavingAccount(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/savingsaccount/AddEditSavingViewModel.kt:90</code></summary>

```kotlin
    // drive this screen's existing loading/error dialog + toast + back-navigation,
    // so the Screen is unchanged.
    private val submitSavingAccount = viewModelScope.submitHandler<Unit>()

    init {
        stateFlow
            .onEach { savedStateHandle.setSerialized(key = ADD_EDIT_SAVING_STATE_KEY, value = it) }
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/savingsaccount/SavingAccountDetail.kt`

```kotlin
data class SavingAccountDetail(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/savingsaccount/details/SavingAccountDetailScreen.kt:184</code></summary>

```kotlin
@Composable
private fun SavingAccountDetails(
    savingAccountDetail: SavingAccountDetail,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    onViewTransaction: (Long, Long) -> Unit,
) {
```

</details>

```kotlin
fun SavingAccountDetail.toAccount(): Account
```
_No KDoc at source._

### `core/model/src/commonMain/kotlin/kpt/core/model/savingsaccount/SavingAccountEntity.kt`

```kotlin
data class SavingAccountEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/confirm/TransferConfirmViewModel.kt:551</code></summary>

```kotlin
    val fromAccountOptions: List<AccountOption>? = emptyList(),
    val balanceMap: Map<String, Double> = emptyMap(),
    val savingsAccountsMap: Map<String, SavingAccountEntity> = emptyMap(),
    val pocketAccountIds: Set<Long> = emptySet(),
    val isProcessing: Boolean = false,
    /**
     * `true` between [TransferConfirmEvent.NavigateForPasscodeVerification]
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/savingsaccount/SavingAccountTemplate.kt`

```kotlin
data class SavingAccountTemplate(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/savingsaccount/AddEditSavingViewModel.kt:97</code></summary>

```kotlin
            .launchIn(viewModelScope)

        repository.getSavingAccountTemplate(state.clientId).onEach {
            sendAction(HandleSavingTemplateResult(it))
        }.launchIn(viewModelScope)

        submitSavingAccount.state
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/savingsaccount/SavingCharge.kt`

```kotlin
data class SavingCharge(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/savingsaccount/SavingAccountTemplate.kt:26</code></summary>

```kotlin
    val isDormancyTrackingActive: Boolean,
    val productOptions: List<SavingProductOption>,
    val chargeOptions: List<SavingCharge> = emptyList(),
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/savingsaccount/SavingProductOption.kt`

```kotlin
data class SavingProductOption(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/savingsaccount/SavingAccountTemplate.kt:25</code></summary>

```kotlin
    val withHoldTax: Boolean,
    val isDormancyTrackingActive: Boolean,
    val productOptions: List<SavingProductOption>,
    val chargeOptions: List<SavingCharge> = emptyList(),
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/savingsaccount/SavingsWithAssociationsEntity.kt`

```kotlin
data class SavingsWithAssociationsEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/merchants/src/commonMain/kotlin/kpt/feature/merchants/MerchantViewModel.kt:155</code></summary>

```kotlin
    data object Empty : MerchantUiState()
    data class Error(val message: String) : MerchantUiState()
    data class ShowMerchants(val merchants: List<SavingsWithAssociationsEntity>) : MerchantUiState()
}
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/savingsaccount/Status.kt`

```kotlin
data class Status(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:480</code></summary>

```kotlin
@Composable
private fun SavingAccountStatusCard(
    status: Status,
    modifier: Modifier = Modifier,
) {
    val activeStatus = SavingAccountStatus.entries.filter { it.isActive(status) }
    FlowRow(
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/savingsaccount/SubStatus.kt`

```kotlin
data class SubStatus(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/merchants/src/commonMain/kotlin/kpt/feature/merchants/ui/MerchantScreen.kt:353</code></summary>

```kotlin
        ),
        transactions = listOf(),
        subStatus = SubStatus(
            id = 2838,
            code = "nobis",
            value = "mi",
            none = false,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/savingsaccount/Summary.kt`

```kotlin
data class Summary(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/savingsaccount/details/SavingAccountDetailScreen.kt:222</code></summary>

```kotlin
@Composable
private fun SavingAccountSummaryCard(
    summary: Summary,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
```

</details>

```kotlin
fun Summary.formatAmount(amount: Double): String
```
_No KDoc at source._

### `core/model/src/commonMain/kotlin/kpt/core/model/savingsaccount/Timeline.kt`

```kotlin
data class Timeline(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/merchants/src/commonMain/kotlin/kpt/feature/merchants/ui/MerchantScreen.kt:305</code></summary>

```kotlin
            matured = false,
        ),
        timeline = Timeline(
            submittedOnDate = listOf(),
            submittedByUsername = "Lemuel Solomon",
            submittedByFirstname = "Vivian Henson",
            submittedByLastname = "Amalia Booker",
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/savingsaccount/Transaction.kt`

```kotlin
data class Transaction(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/fast-mpay/src/commonTest/kotlin/kpt/feature/fastmpay/TestFakes.kt:124</code></summary>

```kotlin
        flowOf(ScreenState.Empty)

    override fun getSelfAccountTransactions(accountId: Long): Flow<List<Transaction>> =
        flowOf(emptyList())

    override suspend fun getSelfAccountTransactionFromId(
        accountId: Long,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/savingsaccount/TransactionType.kt`

```kotlin
enum class TransactionType
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/history/src/commonMain/kotlin/kpt/feature/history/HistoryScreen.kt:135</code></summary>

```kotlin
                    HistoryScreenHeader(
                        accountNo = state.selectedAccount?.number ?: "",
                        selectedTransactionType = state.selectedTransactionType,
                        isExportingPdf = state.isExportingPdf,
                        onFilterClick = {
                            onAction(HistoryAction.OnFilterClick)
                        },
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/savingsaccount/TransactionsEntity.kt`

```kotlin
data class TransactionsEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/mapper/TransactionMapper.kt:22</code></summary>

```kotlin
}

fun TransactionsEntity.toModel(): Transaction {
    return Transaction(
        reversed = this.reversed,
        transactionId = this.id,
        amount = this.amount,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/savingsaccount/Transfer.kt`

```kotlin
data class Transfer(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/mpay-qr/src/commonTest/kotlin/kpt/feature/mpay/qr/MpayQrViewModelTest.kt:509</code></summary>

```kotlin
        screenDataStreamForTesting(state = flowOf(StoreScreenState.Empty))

    override suspend fun makeTransfer(payload: AccountTransferPayload) {}
}

private class FakePocketRepository : PocketRepository {
    @OptIn(ExperimentalScreenDataStreamTestingApi::class)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/savingsaccount/TransferDetail.kt`

```kotlin
data class TransferDetail(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/history/src/commonMain/kotlin/kpt/feature/history/HistoryScreen.kt:62</code></summary>

```kotlin
@Composable
fun HistoryScreen(
    viewTransferDetail: (Long, Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/savingsaccount/UpdateSavingAccountEntity.kt`

```kotlin
data class UpdateSavingAccountEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/savingsaccount/AddEditSavingViewModel.kt:456</code></summary>

```kotlin
                )

            val updateSavingEntity: UpdateSavingAccountEntity
                get() = UpdateSavingAccountEntity(
                    clientId = clientId,
                    productId = productId,
                )
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/search/AccountResult.kt`

```kotlin
data class AccountResult(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/mpay-qr/src/commonTest/kotlin/kpt/feature/mpay/qr/MpayQrViewModelTest.kt:496</code></summary>

```kotlin
        screenDataStreamForTesting(state = flowOf(StoreScreenState.Empty))

    override fun searchAccounts(query: String): Flow<ScreenState<List<AccountResult>>> =
        flowOf(ScreenState.Empty)

    override fun getSelfAccounts(clientId: Long): Flow<ScreenState<List<Account>>> =
        flowOf(ScreenState.Empty)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/search/SearchResult.kt`

```kotlin
data class SearchResult(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/auth/src/commonTest/kotlin/kpt/feature/auth/mobileVerify/MobileVerificationViewModelTest.kt:139</code></summary>

```kotlin
                )
            } returns listOf(
                SearchResult(
                    entityId = 1,
                    entityAccountNo = "123",
                    entityName = "SameUserName",
                    entityType = "savings",
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/standinginstruction/SITemplate.kt`

```kotlin
data class SITemplate(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/standing-instruction/src/commonMain/kotlin/kpt/feature/standing/instruction/createOrUpdate/AddEditSIViewModel.kt:612</code></summary>

```kotlin
        @Serializable
        data class Content(
            val template: SITemplate,
            val payload: StandingInstructionPayload,
        ) : ViewState {

            @Transient
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/standinginstruction/SIUpdatePayload.kt`

```kotlin
data class SIUpdatePayload(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repository/StandingInstructionRepository.kt:60</code></summary>

```kotlin
    suspend fun updateStandingInstruction(
        instructionId: Long,
        payload: SIUpdatePayload,
    )

    suspend fun deleteStandingInstruction(instructionId: Long)
}
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/standinginstruction/StandingInstruction.kt`

```kotlin
data class StandingInstruction(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/standing-instruction/src/commonMain/kotlin/kpt/feature/standing/instruction/StandingInstructionScreen.kt:146</code></summary>

```kotlin
@Composable
internal fun StandingInstructionScreen(
    state: ScreenState<List<StandingInstruction>>,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit,
    onAction: (SIAction) -> Unit,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/standinginstruction/StandingInstructionPayload.kt`

```kotlin
data class StandingInstructionPayload(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/standing-instruction/src/commonMain/kotlin/kpt/feature/standing/instruction/createOrUpdate/AddEditSIViewModel.kt:394</code></summary>

```kotlin
                            template = template,
                            payload = if (state.isAddMode) {
                                StandingInstructionPayload(
                                    fromClientId = template.fromClient?.id ?: 0L,
                                    fromOfficeId = template.fromOffice?.id ?: 0L,
                                    fromAccountType = template.fromAccountType ?: "",
                                    fromAccountId = template.fromAccountOptions?.firstOrNull()?.id ?: -1,
```

</details>

```kotlin
fun StandingInstructionPayload.toSIUploadPayload(): SIUpdatePayload
```
_No KDoc at source._

### `core/model/src/commonMain/kotlin/kpt/core/model/user/AuthState.kt`

```kotlin
sealed class AuthState
```
Models high level auth state for the application.

### `core/model/src/commonMain/kotlin/kpt/core/model/user/DarkThemeConfig.kt`

```kotlin
enum class DarkThemeConfig(val configName: String, val osValue: Int)
```
The user's dark-mode preference: follow the system, or force light/dark. `osValue` maps to the platform's own night-mode constant, so the choice can be handed straight to the OS rather than re-interpreted per platform.

<details><summary>Used in the template — <code>feature/onboarding/src/commonTest/kotlin/kpt/feature/onboarding/ui/OnboardingViewModelTest.kt:72</code></summary>

```kotlin
        override val passcode: String get() = unused()
        override val observeLanguage: Flow<LanguageConfig> get() = unused()
        override val observeDarkThemeConfig: Flow<DarkThemeConfig> get() = unused()
        override val observeDynamicColorPreference: Flow<Boolean> get() = unused()
        override val observeScreenCapturePreference: Flow<Boolean> get() = unused()
        override val observeAnalyticsConsent: Flow<Boolean> get() = unused()
        override val observeCrashReportingConsent: Flow<Boolean> get() = unused()
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/user/Language.kt`

```kotlin
data class Language(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/fast-mpay/src/commonTest/kotlin/kpt/feature/fastmpay/TestFakes.kt:249</code></summary>

```kotlin
    override val selectedInterbankInstance: StateFlow<InterbankServer?> = MutableStateFlow(null)
    override val accountExternalIds: StateFlow<Map<Long, String>> = MutableStateFlow(emptyMap())
    override val language: StateFlow<Language> = MutableStateFlow(Language.DEFAULT)

    override suspend fun updateToken(token: String) {}
    override suspend fun updateUserInfo(user: UserInfo) {}
    override suspend fun setLanguage(language: Language) {}
```

</details>

```kotlin
fun LanguageConfig.toLanguage(): Language
```
_No KDoc at source._

### `core/model/src/commonMain/kotlin/kpt/core/model/user/LanguageConfig.kt`

```kotlin
enum class LanguageConfig(
```
Every language the app can be switched to, in the user's OWN language. GENERATED from core/registries/LOCALE_REGISTRY.yaml by `core/scripts/language-picker-sync.sh --write` — DO NOT HAND-EDIT.

<details><summary>Used in the template — <code>feature/onboarding/src/commonTest/kotlin/kpt/feature/onboarding/ui/OnboardingViewModelTest.kt:71</code></summary>

```kotlin
        override val authToken: String? get() = unused()
        override val passcode: String get() = unused()
        override val observeLanguage: Flow<LanguageConfig> get() = unused()
        override val observeDarkThemeConfig: Flow<DarkThemeConfig> get() = unused()
        override val observeDynamicColorPreference: Flow<Boolean> get() = unused()
        override val observeScreenCapturePreference: Flow<Boolean> get() = unused()
        override val observeAnalyticsConsent: Flow<Boolean> get() = unused()
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/user/NewUser.kt`

```kotlin
class NewUser(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/signup/SignupViewModel.kt:460</code></summary>

```kotlin

        // 2. Create the user account.
        val newUser = NewUser(
            state.userNameInput,
            state.firstNameInput,
            state.lastNameInput,
            state.emailInput,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/user/RoleInfo.kt`

```kotlin
data class RoleInfo(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/mapper/UserMapper.kt:46</code></summary>

```kotlin
    officeId = officeId,
    officeName = officeName,
    roles = roles.map { it.toRoleInfo() },
    permissions = permissions,
    clients = clients,
    shouldRenewPassword = shouldRenewPassword,
    isTwoFactorAuthenticationRequired = isTwoFactorAuthenticationRequired,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/user/ThemeBrand.kt`

```kotlin
enum class ThemeBrand(val brandName: String)
```
The selected colour brand. A fork extends this to offer its own palettes.

<details><summary>Used in the template — <code>feature/onboarding/src/commonTest/kotlin/kpt/feature/onboarding/ui/OnboardingViewModelTest.kt:78</code></summary>

```kotlin
        override val observeCrashReportingConsent: Flow<Boolean> get() = unused()
        override suspend fun setLanguage(language: LanguageConfig) = unused()
        override suspend fun setThemeBrand(themeBrand: ThemeBrand) = unused()
        override suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) = unused()
        override suspend fun setDynamicColorPreference(useDynamicColor: Boolean) = unused()
        override suspend fun setIsAuthenticated(isAuthenticated: Boolean) = unused()
        override suspend fun setIsUnlocked(isUnlocked: Boolean) = unused()
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/user/UnlockType.kt`

```kotlin
enum class UnlockType
```
How the user unlocks the app — passcode or biometric.

### `core/model/src/commonMain/kotlin/kpt/core/model/user/UserData.kt`

```kotlin
data class UserData(
```
Everything the app persists about the current user — theme, language, onboarding progress and the lock state.

<details><summary>Used in the template — <code>feature/onboarding/src/commonTest/kotlin/kpt/feature/onboarding/ui/OnboardingViewModelTest.kt:67</code></summary>

```kotlin
        }

        override val userData: StateFlow<UserData> get() = unused()
        override fun userDataStream(scope: CoroutineScope): ScreenDataStream<UserData> = unused()
        override val authToken: String? get() = unused()
        override val passcode: String get() = unused()
        override val observeLanguage: Flow<LanguageConfig> get() = unused()
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/user/UserInfo.kt`

```kotlin
data class UserInfo(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/login/LoginViewModel.kt:47</code></summary>

```kotlin
    // Submitting/Submitted/Failed lifecycle; we observe it below to drive this screen's
    // existing loading/error dialog + passcode navigation, so the Screen is unchanged.
    private val submitLogin = viewModelScope.submitHandler<UserInfo>()

    init {
        submitLogin.state
            .onEach { submitState ->
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/user/fork/LanguageConfig.kt`

```kotlin
enum class LanguageConfig(
```
Represents the languages supported by the app.

<details><summary>Used in the template — <code>feature/onboarding/src/commonTest/kotlin/kpt/feature/onboarding/ui/OnboardingViewModelTest.kt:71</code></summary>

```kotlin
        override val authToken: String? get() = unused()
        override val passcode: String get() = unused()
        override val observeLanguage: Flow<LanguageConfig> get() = unused()
        override val observeDarkThemeConfig: Flow<DarkThemeConfig> get() = unused()
        override val observeDynamicColorPreference: Flow<Boolean> get() = unused()
        override val observeScreenCapturePreference: Flow<Boolean> get() = unused()
        override val observeAnalyticsConsent: Flow<Boolean> get() = unused()
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/user/fork/UserData.kt`

```kotlin
data class UserData(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/onboarding/src/commonTest/kotlin/kpt/feature/onboarding/ui/OnboardingViewModelTest.kt:67</code></summary>

```kotlin
        }

        override val userData: StateFlow<UserData> get() = unused()
        override fun userDataStream(scope: CoroutineScope): ScreenDataStream<UserData> = unused()
        override val authToken: String? get() = unused()
        override val passcode: String get() = unused()
        override val observeLanguage: Flow<LanguageConfig> get() = unused()
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/utils/Country.kt`

```kotlin
data class Country(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/auth/src/commonTest/kotlin/kpt/feature/auth/signup/SignUpViewModelTest.kt:706</code></summary>

```kotlin
    @Test
    fun givenMissingState_whenSubmitClicked_thenErrorDialogShown() = runTest {
        enterCountry()
        viewModel.trySendAction(SignUpAction.SubmitClick)
        advanceUntilIdle()
        assertTrue(viewModel.stateFlow.value.dialogState is SignUpState.DialogState.Error)
    }
```

</details>

```kotlin
data class State(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountViewModel.kt:49</code></summary>

```kotlin
    private val json: Json,
) : BaseViewModel<AccountState, AccountEvent, AccountAction>(
    initialState = run {
        val clientId = userRepository.clientId.value
        val defaultAccount = userRepository.defaultAccountId.value

        AccountState(
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/utils/CurrencyCode.kt`

```kotlin
data class CurrencyCode(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/mpay-qr/src/commonMain/kotlin/kpt/feature/mpay/qr/SetAmountDialog.kt:52</code></summary>

```kotlin
    currency: String,
    amount: String,
    currencyList: List<CurrencyCode>,
    modifier: Modifier = Modifier,
    onAction: (MpayQrAction) -> Unit,
) {
    MifosCustomDialog(
```

</details>

```kotlin
fun List<CurrencyCode>.filterList(text: String): List<CurrencyCode>
```
_No KDoc at source._

### `core/model/src/commonMain/kotlin/kpt/core/model/utils/Locale.kt`

```kotlin
data class Locale(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/savingsaccount/AddEditSavingAccountScreen.kt:160</code></summary>

```kotlin
internal fun AddEditSavingAccountScreenContent(
    state: AESState,
    localeList: List<Locale>,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    onAction: (AESAction) -> Unit,
) {
```

</details>

```kotlin
fun List<Locale>.filterLocales(filterText: String): List<Locale>
```
_No KDoc at source._

### `core/model/src/commonMain/kotlin/kpt/core/model/utils/PaymentQrData.kt`

```kotlin
data class PaymentQrData(
```
Parcelable data class representing the UPI payment request details

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/SendMoneyViewModel.kt:204</code></summary>

```kotlin
                    if (StandardUpiQrCodeProcessor.isValidUpiQrCode(action.requestData)) {
                        val standardData = StandardUpiQrCodeProcessor.parseUpiQrCode(action.requestData)
                        StandardUpiQrCodeProcessor.toPaymentQrData(standardData)
                    } else {
                        throw e
                    }
                }
```

</details>

```kotlin
fun PaymentQrData.toAccount(): AccountResult
```
_No KDoc at source._

### `core/model/src/commonMain/kotlin/kpt/core/model/utils/QrCodeData.kt`

```kotlin
data class QrCodeData(
```
Data class representing QR code data for MPay transactions. Supports multiple QR formats based on `type`: 1. INTRA_BANK: clientId > 0, accountId > 0 Contains internal Mifos IDs for direct transfer within same bank. 2.

<details><summary>Used in the template — <code>feature/beneficiary/src/commonMain/kotlin/kpt/feature/beneficiary/addupdatebeneficiary/AddEditBeneficiaryViewModel.kt:70</code></summary>

```kotlin
                val sourceQrData = addEditType.sourceQrData?.let { qrDataJson ->
                    try {
                        json.decodeFromString(QrCodeData.serializer(), qrDataJson)
                    } catch (e: Exception) {
                        null
                    }
                }
```

</details>

```kotlin
fun QrCodeData.toAccount(): AccountResult
```
_No KDoc at source._

### `core/model/src/commonMain/kotlin/kpt/core/model/utils/QrCodeType.kt`

```kotlin
enum class QrCodeType
```
Enum representing the type of QR code for MPay transactions.

<details><summary>Used in the template — <code>feature/beneficiary/src/commonMain/kotlin/kpt/feature/beneficiary/addupdatebeneficiary/AddEditBeneficiaryViewModel.kt:380</code></summary>

```kotlin
        val qrData = state.sourceQrData
        when (state.sourceQrType) {
            QrCodeType.INTRA_BANK -> {
                if (qrData != null) {
                    // Use full QR data for navigation to TransferConfirm
                    sendEvent(
                        AEBEvent.NavigateToIntraBankTransfer(
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/utils/StandardUpiQrData.kt`

```kotlin
data class StandardUpiQrData(
```
Data class representing standard UPI QR code data Based on UPI QR code specification

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/util/StandardUpiQrCodeProcessor.kt:35</code></summary>

```kotlin
     * @throws IllegalArgumentException if the QR code is invalid
     */
    fun parseUpiQrCode(qrData: String): StandardUpiQrData {
        if (!isValidUpiQrCode(qrData)) {
            throw IllegalArgumentException("Invalid UPI QR code format")
        }
```

</details>

---

_195 type(s), 15 function(s)/property(ies); 24 carry KDoc at source; 2 authored example(s); 177 live call site(s)._
<!-- api-docs:end -->
