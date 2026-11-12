/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.auth.mobileVerify

import androidx.lifecycle.SavedStateHandle
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.data.repository.SearchRepository
import kpt.core.model.search.SearchResult
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MobileVerificationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val searchRepository: SearchRepository = mock()
    private lateinit var viewModel: MobileVerificationViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = MobileVerificationViewModel(
            searchRepository = searchRepository,
            savedStateHandle = SavedStateHandle(),
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Tests that the ViewModel updates the state when the user enters a new phone number.
     */
    @Test
    fun givenPhoneNumber_whenPhoneNumberChanged_thenStateIsUpdated() = runTest {
        val newPhone = "9876543210"
        viewModel.trySendAction(MobileVerificationAction.PhoneNoChanged(newPhone))
        advanceUntilIdle()
        val state = viewModel.stateFlow.value
        assertEquals(newPhone, (state as MobileVerificationState.VerifyPhoneState).phoneNo)
    }

    /**
     * Tests that a valid phone number not found in the system triggers a transition to OTP entry.
     */
    @Test
    fun givenValidPhoneNumberNotInSystem_whenVerifyPhoneClicked_thenTransitionToOtpState() =
        runTest {
            val validPhoneNumber = "9876543210"

            /*
             * Mocks searchResources to return an empty list to indicate the number doesn't exist.
             */
            everySuspend {
                searchRepository.searchResources(
                    validPhoneNumber,
                    any(),
                    any(),
                )
            } returns emptyList()

            viewModel.trySendAction(MobileVerificationAction.PhoneNoChanged(validPhoneNumber))
            viewModel.trySendAction(MobileVerificationAction.VerifyPhoneBtnClicked)

            advanceUntilIdle()

            /*
             * Verifies that searchResources was called with the correct phone number.
             */
            verifySuspend {
                searchRepository.searchResources(validPhoneNumber, any(), any())
            }

            assertEquals(
                expected = validPhoneNumber,
                actual = (viewModel.stateFlow.value as MobileVerificationState.VerifyOtpState).phoneNo,
            )
        }

    /**
     * Tests that entering an invalid phone number results in an error dialog.
     */
    @Test
    fun givenInvalidPhoneNumber_whenVerifyPhoneClicked_thenErrorDialogShown() =
        runTest {
            val invalidPhoneNo = "123"

            viewModel.trySendAction(MobileVerificationAction.PhoneNoChanged(invalidPhoneNo))
            viewModel.trySendAction(MobileVerificationAction.VerifyPhoneBtnClicked)

            advanceUntilIdle()

            val dialogState =
                (viewModel.stateFlow.value as MobileVerificationState.VerifyPhoneState).dialogState
            assertTrue(dialogState is MobileVerificationState.DialogState.Error)
        }

    /**
     * Tests that entering a phone number that already exists shows an error dialog.
     */
    @Test
    fun givenExistingPhoneNumber_whenVerifyPhoneClicked_thenErrorDialogShown() =
        runTest {
            val existingPhoneNumber = "1234567890"

            /*
             * Mocks searchResources to return a result indicating the phone number is already taken.
             */
            everySuspend {
                searchRepository.searchResources(
                    existingPhoneNumber,
                    any(),
                    any(),
                )
            } returns listOf(
                SearchResult(
                    entityId = 1,
                    entityAccountNo = "123",
                    entityName = "SameUserName",
                    entityType = "savings",
                    parentId = 1,
                    parentName = "smith",
                ),
            )

            viewModel.trySendAction(MobileVerificationAction.PhoneNoChanged(existingPhoneNumber))
            viewModel.trySendAction(MobileVerificationAction.VerifyPhoneBtnClicked)

            advanceUntilIdle()

            /*
             * Verifies that searchResources was called with the expected parameters.
             * Ensures the ViewModel triggered the repository call as expected.
             */
            verifySuspend {
                searchRepository.searchResources(existingPhoneNumber, any(), any())
            }

            val dialogState =
                (viewModel.stateFlow.value as MobileVerificationState.VerifyPhoneState).dialogState
            assertTrue(dialogState is MobileVerificationState.DialogState.Error)
        }
}
