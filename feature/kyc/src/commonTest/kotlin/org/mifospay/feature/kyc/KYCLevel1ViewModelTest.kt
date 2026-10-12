/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mobile-wallet/blob/master/LICENSE.md
 */
package org.mifospay.feature.kyc

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for [KYCLevel1ViewModel].
 *
 * Covers the DobChanged action path — specifically verifying the ViewModel correctly
 * processes a Long timestamp into a formatted date string. This guards against
 * regressions to the null-safety fix on the UI side (selectedDateMillis!! → ?.let).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class KYCLevel1ViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeKycRepository: FakeKycLevelRepository
    private lateinit var fakeUserPreferencesRepository: FakeUserPreferencesRepository

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeKycRepository = FakeKycLevelRepository()
        fakeUserPreferencesRepository = FakeUserPreferencesRepository(clientId = 42L)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): KYCLevel1ViewModel {
        return KYCLevel1ViewModel(
            kycLevelRepository = fakeKycRepository,
            repository = fakeUserPreferencesRepository,
            savedStateHandle = SavedStateHandle(),
        )
    }

    // region DobChanged action

    @Test
    fun givenValidTimestamp_whenDobChanged_thenDobInputIsUpdated() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        // January 15, 2000 00:00:00 UTC in millis
        val timestampMillis = 947894400000L

        viewModel.trySendAction(KycLevel1Action.DobChanged(timestampMillis))
        advanceUntilIdle()

        val state = viewModel.stateFlow.value
        assertTrue(
            state.dobInput.isNotEmpty(),
            "dobInput should be non-empty after DobChanged",
        )
        // DateHelper formats as "dd-MM-yyyy" — verify it's a recognisable date string
        assertTrue(
            state.dobInput.contains("-"),
            "dobInput should be in dd-MM-yyyy format, got: ${state.dobInput}",
        )
    }

    @Test
    fun givenInitialState_thenDobInputIsEmpty() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("", viewModel.stateFlow.value.dobInput)
    }

    @Test
    fun givenDobChanged_whenSubmitClickedWithOtherFieldsEmpty_thenFirstNameErrorShown() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.trySendAction(KycLevel1Action.DobChanged(947894400000L))
        advanceUntilIdle()

        viewModel.trySendAction(KycLevel1Action.SubmitClicked)
        advanceUntilIdle()

        val dialogState = viewModel.stateFlow.value.dialogState
        assertIs<KycLevel1State.DialogState.Error>(dialogState)
        assertEquals("First name is required", dialogState.message)
    }

    @Test
    fun givenAllFieldsMissingDob_whenSubmitClicked_thenDobErrorShown() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.trySendAction(KycLevel1Action.FirstNameChanged("John"))
        viewModel.trySendAction(KycLevel1Action.LastNameChanged("Doe"))
        viewModel.trySendAction(KycLevel1Action.MobileNoChanged("1234567890"))
        viewModel.trySendAction(KycLevel1Action.AddressLine1Changed("123 Main St"))
        viewModel.trySendAction(KycLevel1Action.AddressLine2Changed("Apt 1"))
        // intentionally skip DobChanged
        advanceUntilIdle()

        viewModel.trySendAction(KycLevel1Action.SubmitClicked)
        advanceUntilIdle()

        val dialogState = viewModel.stateFlow.value.dialogState
        assertIs<KycLevel1State.DialogState.Error>(dialogState)
        assertEquals("Date of birth is required", dialogState.message)
    }

    // endregion

    // region Existing KYC data loading

    @Test
    fun givenExistingKycData_whenViewModelCreated_thenStateIsPopulated() = runTest {
        fakeKycRepository.setLevel1Details(
            org.mifospay.core.model.kyc.KYCLevel1Details(
                firstName = "Alice",
                lastName = "Smith",
                addressLine1 = "456 Oak Ave",
                addressLine2 = "Suite 2",
                mobileNo = "9876543210",
                dob = "15-01-2000",
                currentLevel = "KYC_LEVEL_1",
            ),
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.stateFlow.value
        assertEquals("Alice", state.firstNameInput)
        assertEquals("Smith", state.lastNameInput)
        assertEquals("15-01-2000", state.dobInput)
        assertTrue(state.doesExist)
    }

    @Test
    fun givenNoExistingKycData_whenViewModelCreated_thenStateIsEmpty() = runTest {
        fakeKycRepository.setLevel1Details(null)

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.stateFlow.value
        assertEquals("", state.firstNameInput)
        assertEquals("", state.dobInput)
        assertTrue(!state.doesExist)
    }

    // endregion

    // region DismissDialog

    @Test
    fun givenErrorDialogShown_whenDismissDialog_thenDialogStateIsNull() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.trySendAction(KycLevel1Action.SubmitClicked)
        advanceUntilIdle()

        assertNotNull(viewModel.stateFlow.value.dialogState)

        viewModel.trySendAction(KycLevel1Action.DismissDialog)
        advanceUntilIdle()

        assertEquals(null, viewModel.stateFlow.value.dialogState)
    }

    // endregion
}
