/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.auth.signup

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.logical.or
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verifySuspend
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.common.ScreenState
import kpt.core.common.StringProvider
import kpt.core.data.repository.AssetRepository
import kpt.core.data.repository.ClientRepository
import kpt.core.data.repository.SearchRepository
import kpt.core.data.repository.UserRepository
import kpt.core.model.search.SearchResult
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SignUpViewModelTest {

    private val testDispatcher: CoroutineDispatcher = StandardTestDispatcher()

    private val mockUserRepository: UserRepository = mock()
    private val mockSearchRepository: SearchRepository = mock()
    private val mockClientRepository: ClientRepository = mock()
    private val mockAssetRepository: AssetRepository = mock()

    // Stub StringProvider.get(...) for the two arities SignupViewModel actually calls — resource
    // only (0 format args) and resource + one label (1 format arg) — using plain `any()` element
    // matchers, avoiding Mokkery 3.4.2's deprecated generic vararg matchers (varargsAll/anyVarargs).
    private val mockStringProvider: StringProvider = mock<StringProvider> {
        everySuspend { get(any()) } returns "Mock String"
        everySuspend { get(any(), any()) } returns "Mock String"
    }

    private lateinit var viewModel: SignupViewModel

    private val fakeSearchResult = SearchResult(
        entityId = 1,
        entityAccountNo = "123",
        entityName = "SameUserName",
        entityType = "savings",
        parentId = 1,
        parentName = "smith",
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        every { mockAssetRepository.getCountriesWithStates() } returns flowOf(
            ScreenState.Content(
                mapOf(
                    "Afghanistan" to listOf(
                        "Badakhshān", "Baghlān", "Balkh", "Bādghīs", "Bāmyān",
                    ),
                    "Albania" to listOf(
                        "Berat", "Dibër", "Durrës", "Elbasan", "Fier",
                    ),
                ),
            ),
        )

        viewModel = SignupViewModel(
            userRepository = mockUserRepository,
            searchRepository = mockSearchRepository,
            clientRepository = mockClientRepository,
            assetRepository = mockAssetRepository,
            stringProvider = mockStringProvider,
            savedStateHandle = SavedStateHandle(),
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Ensures the first name is updated in the state when user inputs it.
     */
    @Test
    fun givenFirstName_whenInputChanged_thenFirstNameIsUpdated() = runTest {
        val firstName = "John"
        viewModel.trySendAction(SignUpAction.FirstNameInputChange(firstName))
        advanceUntilIdle()
        assertEquals(firstName, viewModel.stateFlow.value.firstNameInput)
    }

    /**
     * Ensures the last name is updated in the state when user inputs it.
     */
    @Test
    fun givenLastName_whenInputChanged_thenLastNameIsUpdated() = runTest {
        val lastName = "Doe"
        viewModel.trySendAction(SignUpAction.LastNameInputChange(lastName))
        advanceUntilIdle()
        assertEquals(lastName, viewModel.stateFlow.value.lastNameInput)
    }

    /**
     * Ensures the username is updated in the state.
     */
    @Test
    fun givenUsername_whenInputChanged_thenUsernameIsUpdated() = runTest {
        val username = "john_doe"
        viewModel.trySendAction(SignUpAction.UserNameInputChange(username))
        advanceUntilIdle()
        assertEquals(username, viewModel.stateFlow.value.userNameInput)
    }

    /**
     * Verifies the email input updates the corresponding state.
     */
    @Test
    fun givenEmail_whenInputChanged_thenEmailIsUpdated() = runTest {
        val email = "john@example.com"
        viewModel.trySendAction(SignUpAction.EmailInputChange(email))
        advanceUntilIdle()
        assertEquals(email, viewModel.stateFlow.value.emailInput)
    }

    /**
     * Confirms the mobile number field updates properly in the state.
     */
    @Test
    fun givenMobileNumber_whenInputChanged_thenMobileNumberIsUpdated() = runTest {
        val mobile = "9876543210"
        viewModel.trySendAction(SignUpAction.MobileNumberInputChange(mobile))
        advanceUntilIdle()
        assertEquals(mobile, viewModel.stateFlow.value.mobileNumberInput)
    }

    /**
     * Ensures password field gets updated on input.
     */
    @Test
    fun givenPassword_whenInputChanged_thenPasswordIsUpdated() = runTest {
        val password = "Test@1234"
        viewModel.trySendAction(SignUpAction.PasswordInputChange(password))
        advanceUntilIdle()
        assertEquals(password, viewModel.stateFlow.value.passwordInput)
    }

    /**
     * Ensures confirm password field gets updated on input.
     */
    @Test
    fun givenConfirmPassword_whenInputChanged_thenConfirmPasswordIsUpdated() =
        runTest {
            val confirmPassword = "Test@1234"
            viewModel.trySendAction(SignUpAction.ConfirmPasswordInputChange(confirmPassword))
            advanceUntilIdle()
            assertEquals(confirmPassword, viewModel.stateFlow.value.confirmPasswordInput)
        }

    /**
     * Verifies Address Line 1 updates correctly.
     */
    @Test
    fun givenAddressLine1_whenInputChanged_thenAddressLine1IsUpdated() = runTest {
        val address1 = "123 Main Street"
        viewModel.trySendAction(SignUpAction.AddressLine1InputChange(address1))
        advanceUntilIdle()
        assertEquals(address1, viewModel.stateFlow.value.addressLine1Input)
    }

    /**
     * Verifies Address Line 2 updates correctly.
     */
    @Test
    fun givenAddressLine2_whenInputChanged_thenAddressLine2IsUpdated() = runTest {
        val address2 = "Suite 456"
        viewModel.trySendAction(SignUpAction.AddressLine2InputChange(address2))
        advanceUntilIdle()
        assertEquals(address2, viewModel.stateFlow.value.addressLine2Input)
    }

    /**
     * Checks the pincode input updates its field correctly.
     */
    @Test
    fun givenPinCode_whenInputChanged_thenPinCodeIsUpdated() = runTest {
        val pincode = "56000"
        viewModel.trySendAction(SignUpAction.PinCodeInputChange(pincode))
        advanceUntilIdle()
        assertEquals(pincode, viewModel.stateFlow.value.pinCodeInput)
    }

    /**
     * Ensures country change updates the country field and resets the state field.
     */
    @Test
    fun givenCountry_whenInputChanged_thenCountryIsUpdatedAndStateReset() = runTest {
        val country = "USA"
        viewModel.trySendAction(SignUpAction.CountryInputChange(country))
        advanceUntilIdle()
        assertEquals(country, viewModel.stateFlow.value.countryInput)
        assertEquals("", viewModel.stateFlow.value.stateInput)
    }

    /**
     * Ensures the selected state updates correctly in the ViewModel state.
     */
    @Test
    fun givenState_whenInputChanged_thenStateIsUpdated() = runTest {
        val state = "KA"
        viewModel.trySendAction(SignUpAction.StateInputChange(state))
        advanceUntilIdle()
        assertEquals(state, viewModel.stateFlow.value.stateInput)
    }

    /**
     * Verifies the correct list of states is shown for the selected country.
     *
     * Includes verification of call to [mockAssetRepository.getCountriesWithStates]
     */
    @Test
    fun givenCountrySelected_whenFetchingStates_thenReturnExpectedStates() = runTest {
        viewModel.trySendAction(SignUpAction.CountryInputChange("Afghanistan"))
        advanceUntilIdle()

        // Verifies that getCountriesWithStates() was called exactly once during the process
        verify { mockAssetRepository.getCountriesWithStates() }

        assertEquals(
            listOf("Badakhshān", "Baghlān", "Balkh", "Bādghīs", "Bāmyān"),
            viewModel.stateFlow.value.statesForSelectedCountry,
        )
    }

    /**
     * Tests a successful end-to-end sign-up flow.
     *
     * This includes:
     * - Verifying that the user and client are created.
     * - Verifying that the client is assigned to the user.
     * - Ensuring no error dialog is shown at the end.
     */
    @Test
    fun givenValidInputs_whenSubmitClicked_thenUserAndClientAreCreatedSuccessfully() = runTest {
        /* Mock the SearchRepository to simulate that both the entered username and mobile number are available.
         * This means the backend did not find any existing user or client with the provided credentials,
         * so it returns an empty list, indicating no conflicts.
         * This sets up the scenario for a successful registration flow.
         */
        everySuspend {
            mockSearchRepository.searchResources(
                "john_doe",
                any(),
                any(),
            )
        } returns emptyList()
        everySuspend {
            mockSearchRepository.searchResources(
                "9876543210",
                any(),
                any(),
            )
        } returns emptyList()

        // Mock user creation to return a user ID
        everySuspend { mockUserRepository.createUser(any()) } returns 123

        // Mock client creation to return a client ID
        everySuspend { mockClientRepository.createClient(any()) } returns 456

        // Mock assigning client to user to return success
        everySuspend {
            mockUserRepository.assignClientToUser(
                any(),
                any(),
            )
        } returns Unit

        enterAllFields()

        viewModel.trySendAction(SignUpAction.SubmitClick)

        advanceUntilIdle()

        // Verify that all critical suspend calls were made as expected
        verifySuspend {
            mockSearchRepository.searchResources("john_doe", any(), any())
            mockSearchRepository.searchResources("9876543210", any(), any())
            mockUserRepository.createUser(any())
            mockClientRepository.createClient(any())
            mockUserRepository.assignClientToUser(123, 456)
        }

        assertNull(viewModel.stateFlow.value.dialogState)
    }

    /**
     * Tests a complete and successful signup process:
     *
     * - Verifies that both username and mobile number pass uniqueness checks.
     * - Ensures user and client are created successfully.
     * - Confirms the client is assigned to the user.
     * - Validates the emitted events:
     *   - A toast message confirming signup success.
     *   - A navigation event redirecting the user to the login screen.
     */
    @Test
    fun givenValidInputs_whenSignUpSucceeds_thenShowToastAndNavigateToLogin() = runTest {
        /* Mock the SearchRepository to simulate that both the entered username and mobile number are available.
         * This means the backend did not find any existing user or client with the provided credentials,
         * so it returns an empty list, indicating no conflicts.
         * This sets up the scenario for a successful registration flow.
         */
        everySuspend {
            mockSearchRepository.searchResources(
                or("john_doe", "9876543210"),
                any(),
                any(),
            )
        } returns emptyList()

        // Mock user creation
        everySuspend { mockUserRepository.createUser(any()) } returns 101

        // Mock client creation
        everySuspend { mockClientRepository.createClient(any()) } returns 202

        // Mock client-user assignment
        everySuspend { mockUserRepository.assignClientToUser(101, 202) } returns Unit

        enterAllFields()

        viewModel.trySendAction(SignUpAction.SubmitClick)
        advanceUntilIdle()

        // Verify that all critical suspend calls were made as expected
        verifySuspend {
            mockSearchRepository.searchResources("john_doe", any(), any())
            mockSearchRepository.searchResources("9876543210", any(), any())
            mockUserRepository.createUser(any())
            mockClientRepository.createClient(any())
            mockUserRepository.assignClientToUser(any(), any())
        }

        viewModel.eventFlow.test {
            // Assert: Show toast
            val toastEvent = awaitItem()
            assertTrue(toastEvent is SignUpEvent.ShowToast)
            assertEquals("Mock String", toastEvent.message)

            // Assert: Navigate to login
            val navigationEvent = awaitItem()
            assertTrue(navigationEvent is SignUpEvent.NavigateToLogin)
            assertEquals("john_doe", navigationEvent.username)
        }
    }

    /**
     * Tests that when the user clicks the close button during signup,
     * the ViewModel emits a [SignUpEvent.NavigateBack] event to trigger back navigation.
     */
    @Test
    fun whenCloseClicked_thenNavigateBackEventEmitted() = runTest {
        viewModel.eventFlow.test {
            // Act: simulate clicking the close/back button
            viewModel.trySendAction(SignUpAction.CloseClick)

            // Assert: the viewModel emits a NavigateBack event
            val event = awaitItem()
            assertTrue(event is SignUpEvent.NavigateBack)
        }
    }

    /**
     * Tests that if the entered username already exists, the ViewModel shows an error dialog.
     *
     * This ensures the UI properly handles the case where the user tries to register with
     * a username that is already taken.
     */
    @Test
    fun givenExistingUsername_whenSubmitClicked_thenErrorDialogShown() = runTest {
        /* Mocks searchResources to return a result indicating the username or mobile already exists.
         * This simulates the backend identifying a duplicate username during the signup process.
         */
        everySuspend {
            mockSearchRepository.searchResources(
                or("SameUserName", "9876543210"), any(), any(),
            )
        } returns listOf(
            fakeSearchResult,
        )

        enterAllFields()

        viewModel.trySendAction(SignUpAction.UserNameInputChange("SameUserName"))

        viewModel.trySendAction(SignUpAction.SubmitClick)

        advanceUntilIdle()

        /*
         * Verifies that search was performed for both the username and the mobile number.
         * Confirms the repository method was invoked with the expected parameters.
         */
        verifySuspend {
            mockSearchRepository.searchResources("SameUserName", any(), any())
            mockSearchRepository.searchResources(
                query = "9876543210",
                resources = any(),
                exactMatch = any(),
            )
        }

        assertTrue(viewModel.stateFlow.value.dialogState is SignUpState.DialogState.Error)
    }

    /**
     * Tests that if the entered mobile number already exists, the ViewModel shows an error dialog.
     *
     * This ensures the UI prevents duplicate registrations using an already registered mobile number.
     */
    @Test
    fun givenExistingMobileNumber_whenSubmitClicked_thenErrorDialogShown() = runTest {
        /*
         * Mocks searchResources to return a result indicating the username or mobile already exists.
         * Specifically simulates a conflict on the mobile number while keeping the username valid.
         */
        everySuspend {
            mockSearchRepository.searchResources(
                query = or("john_doe", "1234567890"),
                resources = any(),
                exactMatch = any(),
            )
        } returns listOf(
            fakeSearchResult,
        )

        enterAllFields()

        viewModel.trySendAction(SignUpAction.MobileNumberInputChange("1234567890"))

        viewModel.trySendAction(SignUpAction.SubmitClick)

        advanceUntilIdle()

        /*
         * Verifies that the repository was queried with both username and mobile number
         * to check for existing users before submission.
         * */
        verifySuspend {
            mockSearchRepository.searchResources(
                query = "john_doe",
                resources = any(),
                exactMatch = any(),
            )
            mockSearchRepository.searchResources(
                query = "1234567890",
                resources = any(),
                exactMatch = any(),
            )
        }

        assertTrue(viewModel.stateFlow.value.dialogState is SignUpState.DialogState.Error)
    }

    /**
     * Tests that if both the username and mobile number already exist, the ViewModel shows a combined error dialog.
     *
     * This validates the system's ability to detect and handle multiple field conflicts in one go.
     */
    @Test
    fun givenExistingUsernameAndMobileNumber_whenSubmitClicked_thenErrorDialogShown() =
        runTest {
            /* Mocks searchResources to return a result showing both username and mobile are taken.
             * This covers the scenario where a new user attempts to register with fully duplicated credentials.
             */
            everySuspend {
                mockSearchRepository.searchResources(
                    query = or("SameUserName", "1234567890"),
                    any(),
                    any(),
                )
            } returns listOf(
                fakeSearchResult,
            )

            enterAllFields()

            viewModel.trySendAction(SignUpAction.UserNameInputChange("SameUserName"))
            viewModel.trySendAction(SignUpAction.MobileNumberInputChange("1234567890"))

            viewModel.trySendAction(SignUpAction.SubmitClick)

            advanceUntilIdle()

            // Verifies that both conflicting queries (username and mobile number) were made to the repository.
            verifySuspend {
                mockSearchRepository.searchResources("SameUserName", any(), any())
                mockSearchRepository.searchResources(
                    query = "1234567890",
                    resources = any(),
                    exactMatch = any(),
                )
            }

            assertTrue(viewModel.stateFlow.value.dialogState is SignUpState.DialogState.Error)
        }

    /**
     * Tests that when the first name is missing and the form is submitted,
     * the ViewModel responds with a validation error dialog.
     */
    @Test
    fun givenMissingFirstName_whenSubmitClicked_thenErrorDialogShown() = runTest {
        viewModel.trySendAction(SignUpAction.SubmitClick)
        advanceUntilIdle()
        assertTrue(viewModel.stateFlow.value.dialogState is SignUpState.DialogState.Error)
    }

    /**
     * Tests that a missing last name triggers a required field error dialog.
     */
    @Test
    fun givenMissingLastName_whenSubmitClicked_thenErrorDialogShown() = runTest {
        enterFirstName()
        viewModel.trySendAction(SignUpAction.SubmitClick)
        advanceUntilIdle()
        assertTrue(viewModel.stateFlow.value.dialogState is SignUpState.DialogState.Error)
    }

    /**
     * Tests that a missing username triggers a required field error dialog.
     */
    @Test
    fun givenMissingUsername_whenSubmitClicked_thenErrorDialogShown() = runTest {
        enterLastName()
        viewModel.trySendAction(SignUpAction.SubmitClick)
        advanceUntilIdle()
        assertTrue(viewModel.stateFlow.value.dialogState is SignUpState.DialogState.Error)
    }

    /**
     * Tests that a missing email triggers a required field error dialog.
     */
    @Test
    fun givenMissingEmail_whenSubmitClicked_thenErrorDialogShown() = runTest {
        enterUserName()
        viewModel.trySendAction(SignUpAction.SubmitClick)
        advanceUntilIdle()
        assertTrue(viewModel.stateFlow.value.dialogState is SignUpState.DialogState.Error)
    }

    /**
     * Tests that an invalid email format leads to a validation error.
     */
    @Test
    fun givenInvalidEmailFormat_whenSubmitClicked_thenErrorDialogShown() = runTest {
        enterUserName()
        viewModel.trySendAction(SignUpAction.EmailInputChange("not-an-email"))

        viewModel.trySendAction(SignUpAction.SubmitClick)
        advanceUntilIdle()

        assertTrue(viewModel.stateFlow.value.dialogState is SignUpState.DialogState.Error)
    }

    /**
     * Tests that an empty mobile number field shows a required field error.
     */
    @Test
    fun givenMissingMobileNumber_whenSubmitClicked_thenErrorDialogShown() =
        runTest(testDispatcher) {
            enterEmail()
            viewModel.trySendAction(SignUpAction.SubmitClick)
            advanceUntilIdle()
            assertTrue(viewModel.stateFlow.value.dialogState is SignUpState.DialogState.Error)
        }

    /**
     * Tests that entering a short (invalid) mobile number triggers an error.
     */
    @Test
    fun givenShortMobileNumber_whenSubmitClicked_thenErrorDialogShown() = runTest {
        enterEmail()
        viewModel.trySendAction(SignUpAction.MobileNumberInputChange("12345"))

        viewModel.trySendAction(SignUpAction.SubmitClick)
        advanceUntilIdle()

        assertTrue(viewModel.stateFlow.value.dialogState is SignUpState.DialogState.Error)
    }

    /**
     * Tests that a missing password results in a required field error dialog.
     */
    @Test
    fun givenMissingPassword_whenSubmitClicked_thenErrorDialogShown() = runTest {
        enterMobileNumber()
        viewModel.trySendAction(SignUpAction.SubmitClick)
        advanceUntilIdle()
        assertTrue(viewModel.stateFlow.value.dialogState is SignUpState.DialogState.Error)
    }

    /**
     * Tests that if confirm password is not entered, an error dialog is shown.
     */
    @Test
    fun givenMissingConfirmPassword_whenSubmitClicked_thenErrorDialogShown() =
        runTest {
            enterPassword()
            viewModel.trySendAction(SignUpAction.SubmitClick)
            advanceUntilIdle()
            assertTrue(viewModel.stateFlow.value.dialogState is SignUpState.DialogState.Error)
        }

    /**
     * Tests that a mismatch between password and confirm password triggers an error.
     */
    @Test
    fun givenPasswordMismatch_whenSubmitClicked_thenErrorDialogShown() = runTest {
        enterPassword()
        viewModel.trySendAction(SignUpAction.ConfirmPasswordInputChange("DifferentPassword"))

        viewModel.trySendAction(SignUpAction.SubmitClick)
        advanceUntilIdle()
        assertTrue(viewModel.stateFlow.value.dialogState is SignUpState.DialogState.Error)
    }

    /**
     * Tests that missing Address Line 1 results in a validation error dialog.
     */
    @Test
    fun givenMissingAddressLine1_whenSubmitClicked_thenErrorDialogShown() =
        runTest {
            enterConfirmPassword()
            viewModel.trySendAction(SignUpAction.SubmitClick)
            advanceUntilIdle()
            assertTrue(viewModel.stateFlow.value.dialogState is SignUpState.DialogState.Error)
        }

    /**
     * Tests that missing Address Line 2 triggers a required field error.
     */
    @Test
    fun givenMissingAddressLine2_whenSubmitClicked_thenErrorDialogShown() =
        runTest {
            enterAddressLine1()
            viewModel.trySendAction(SignUpAction.SubmitClick)
            advanceUntilIdle()
            assertTrue(viewModel.stateFlow.value.dialogState is SignUpState.DialogState.Error)
        }

    /**
     * Tests that missing pincode results in a validation error message.
     */
    @Test
    fun givenMissingPinCode_whenSubmitClicked_thenErrorDialogShown() = runTest {
        enterAddressLine2()
        viewModel.trySendAction(SignUpAction.SubmitClick)
        advanceUntilIdle()
        assertTrue(viewModel.stateFlow.value.dialogState is SignUpState.DialogState.Error)
    }

    /**
     * Tests that not selecting a country during sign-up shows a required field error.
     */
    @Test
    fun givenMissingCountry_whenSubmitClicked_thenErrorDialogShown() = runTest {
        enterPinCode()
        viewModel.trySendAction(SignUpAction.SubmitClick)
        advanceUntilIdle()
        assertTrue(viewModel.stateFlow.value.dialogState is SignUpState.DialogState.Error)
    }

    /**
     * Tests that not selecting a state results in an error dialog.
     */
    @Test
    fun givenMissingState_whenSubmitClicked_thenErrorDialogShown() = runTest {
        enterCountry()
        viewModel.trySendAction(SignUpAction.SubmitClick)
        advanceUntilIdle()
        assertTrue(viewModel.stateFlow.value.dialogState is SignUpState.DialogState.Error)
    }

    /**
     * Fills in savings account number, business name, and first name.
     * Acts as the initial field setup for form input sequences.
     */
    private fun enterFirstName() {
        viewModel.trySendAction(SignUpAction.SavingsAccountNoInputChange(1))
        viewModel.trySendAction(SignUpAction.BusinessNameInputChange("Acme Corp"))
        viewModel.trySendAction(SignUpAction.FirstNameInputChange("John"))
    }

    /**
     * Fills in the last name after first name has been entered.
     */
    private fun enterLastName() {
        enterFirstName()
        viewModel.trySendAction(SignUpAction.LastNameInputChange("Doe"))
    }

    /**
     * Fills in the username after last name has been entered.
     */
    private fun enterUserName() {
        enterLastName()
        viewModel.trySendAction(SignUpAction.UserNameInputChange("john_doe"))
    }

    /**
     * Fills in the email address after username has been entered.
     */
    private fun enterEmail() {
        enterUserName()
        viewModel.trySendAction(SignUpAction.EmailInputChange("john@example.com"))
    }

    /**
     * Fills in the mobile number after email has been entered.
     */
    private fun enterMobileNumber() {
        enterEmail()
        viewModel.trySendAction(SignUpAction.MobileNumberInputChange("9876543210"))
    }

    /**
     * Fills in the password after mobile number has been entered.
     */
    private fun enterPassword() {
        enterMobileNumber()
        viewModel.trySendAction(SignUpAction.PasswordInputChange("Strongkey@123"))
    }

    /**
     * Fills in the confirm password field after the password has been entered.
     */
    private fun enterConfirmPassword() {
        enterPassword()
        viewModel.trySendAction(SignUpAction.ConfirmPasswordInputChange("Strongkey@123"))
    }

    /**
     * Fills in address line 1 after confirm password has been entered.
     */
    private fun enterAddressLine1() {
        enterConfirmPassword()
        viewModel.trySendAction(SignUpAction.AddressLine1InputChange("123 Main St"))
    }

    /**
     * Fills in address line 2 after address line 1 has been entered.
     */
    private fun enterAddressLine2() {
        enterAddressLine1()
        viewModel.trySendAction(SignUpAction.AddressLine2InputChange("Apt 456"))
    }

    /**
     * Fills in the pin code after address line 2 has been entered.
     */
    private fun enterPinCode() {
        enterAddressLine2()
        viewModel.trySendAction(SignUpAction.PinCodeInputChange("560001"))
    }

    /**
     * Fills in the country after pin code has been entered.
     */
    private fun enterCountry() {
        enterPinCode()
        viewModel.trySendAction(SignUpAction.CountryInputChange("IN"))
    }

    /**
     * Fills in the state after country has been entered.
     */
    private fun enterState() {
        enterCountry()
        viewModel.trySendAction(SignUpAction.StateInputChange("KA"))
    }

    /**
     * Convenience method to fill in all required fields in the correct order.
     * This is used for tests that need the form to be fully populated.
     */
    private fun enterAllFields() {
        enterState()
    }
}
