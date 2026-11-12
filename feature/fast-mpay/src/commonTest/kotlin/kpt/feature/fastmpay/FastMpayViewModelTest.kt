/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.fastmpay

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import io.ktor.util.encodeBase64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.model.instance.ServerInstance
import kpt.feature.fastmpay.model.QrProcessResult
import kpt.feature.fastmpay.navigation.QR_DATA_ARG
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for [FastMpayViewModel].
 *
 * Tests cover:
 * - QR data decoding and processing on init
 * - Loading state management
 * - Error handling for missing or invalid QR data
 * - Result clearing functionality
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FastMpayViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeSelfServiceRepository: FakeSelfServiceRepository
    private lateinit var fakeUserPreferencesRepository: FakeUserPreferencesRepository
    private lateinit var fakeOfficeRepository: FakeOfficeRepository
    private lateinit var processor: FastMpayProcessor

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeSelfServiceRepository = FakeSelfServiceRepository()
        fakeUserPreferencesRepository = FakeUserPreferencesRepository()
        // Non-null clientId so the store-backed beneficiary read path is
        // exercised (see FastMpayProcessor.processIntraBankQr — a null
        // clientId short-circuits to AddBeneficiary).
        fakeUserPreferencesRepository.setClientId(1L)
        fakeOfficeRepository = FakeOfficeRepository()
        processor = FastMpayProcessor(
            selfServiceRepository = fakeSelfServiceRepository,
            userPreferencesRepository = fakeUserPreferencesRepository,
            officeRepository = fakeOfficeRepository,
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // region Initialization Tests

    @Test
    fun givenValidQrData_whenViewModelCreated_thenProcessesAndEmitsResult() = runTest {
        // Given
        fakeUserPreferencesRepository.setSelectedInstance(
            ServerInstance(
                endpoint = "test.com",
                protocol = "https://",
                path = "/api/v1",
                platformTenantId = "mifos-bank",
                label = "Test Bank",
            ),
        )
        fakeSelfServiceRepository.setBeneficiaryList(emptyList())

        val qrString = "mpay://pay?qt=0&fsp=mifos-bank&ci=123&am=100&cn=John&an=ACC001&ai=456&cu=USD&oi=1&pi=2"
        val encodedQr = qrString.encodeBase64()
        val savedStateHandle = SavedStateHandle(mapOf(QR_DATA_ARG to encodedQr))

        // When
        val viewModel = FastMpayViewModel(savedStateHandle, processor)
        advanceUntilIdle()

        // Then
        viewModel.resultFlow.test {
            val result = awaitItem()
            assertIs<QrProcessResult.NavigateToAddBeneficiary>(result)
        }
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun givenMissingQrData_whenViewModelCreated_thenEmitsError() = runTest {
        // Given - No QR data in SavedStateHandle
        val savedStateHandle = SavedStateHandle()

        // When
        val viewModel = FastMpayViewModel(savedStateHandle, processor)
        advanceUntilIdle()

        // Then
        viewModel.errorFlow.test {
            val error = awaitItem()
            assertEquals("QR data is missing", error)
        }
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun givenInvalidQrData_whenViewModelCreated_thenEmitsError() = runTest {
        // Given - Invalid QR data
        val savedStateHandle = SavedStateHandle(mapOf(QR_DATA_ARG to "invalid-qr-data"))

        // When
        val viewModel = FastMpayViewModel(savedStateHandle, processor)
        advanceUntilIdle()

        // Then
        viewModel.errorFlow.test {
            val error = awaitItem()
            assertNotNull(error)
        }
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun givenInitialState_whenViewModelCreated_thenIsLoadingTrue() = runTest {
        // Given
        val savedStateHandle = SavedStateHandle()

        // When
        val viewModel = FastMpayViewModel(savedStateHandle, processor)

        // Then - Initially loading is true
        assertTrue(viewModel.isLoading.value)

        advanceUntilIdle()
        // After processing completes, loading should be false
        assertFalse(viewModel.isLoading.value)
    }

    // endregion

    // region Clear Result Tests

    @Test
    fun givenResultEmitted_whenClearResult_thenResultIsNull() = runTest {
        // Given
        fakeUserPreferencesRepository.setSelectedInstance(
            ServerInstance(
                endpoint = "test.com",
                protocol = "https://",
                path = "/api/v1",
                platformTenantId = "mifos-bank",
                label = "Test Bank",
            ),
        )
        fakeSelfServiceRepository.setBeneficiaryList(emptyList())

        val qrString = "mpay://pay?qt=0&fsp=mifos-bank&ci=123&am=100&cn=John&an=ACC001&ai=456&cu=USD&oi=1&pi=2"
        val encodedQr = qrString.encodeBase64()
        val savedStateHandle = SavedStateHandle(mapOf(QR_DATA_ARG to encodedQr))

        val viewModel = FastMpayViewModel(savedStateHandle, processor)
        advanceUntilIdle()

        // When
        viewModel.clearResult()

        // Then
        assertNull(viewModel.resultFlow.value)
    }

    @Test
    fun givenErrorEmitted_whenClearError_thenErrorIsNull() = runTest {
        // Given - No QR data to trigger error
        val savedStateHandle = SavedStateHandle()

        val viewModel = FastMpayViewModel(savedStateHandle, processor)
        advanceUntilIdle()

        assertEquals("QR data is missing", viewModel.errorFlow.value)

        // When
        viewModel.clearError()

        // Then
        assertNull(viewModel.errorFlow.value)
    }

    // endregion

    // region Integration Tests

    @Test
    fun givenIntraBankQrWithBankMismatch_whenViewModelCreated_thenEmitsBankMismatchResult() =
        runTest {
            // Given
            fakeUserPreferencesRepository.setSelectedInstance(
                ServerInstance(
                    endpoint = "test.com",
                    protocol = "https://",
                    path = "/api/v1",
                    platformTenantId = "my-bank",
                    label = "My Bank",
                ),
            )

            // QR from different bank
            val qrString = "mpay://pay?qt=0&fsp=other-bank&ci=123&am=100&cn=John&an=ACC001&ai=456&cu=USD&oi=1&pi=2"
            val encodedQr = qrString.encodeBase64()
            val savedStateHandle = SavedStateHandle(mapOf(QR_DATA_ARG to encodedQr))

            // When
            val viewModel = FastMpayViewModel(savedStateHandle, processor)
            advanceUntilIdle()

            // Then
            viewModel.resultFlow.test {
                val result = awaitItem()
                assertIs<QrProcessResult.BankMismatch>(result)
                assertEquals("my-bank", result.currentBankId)
                assertEquals("other-bank", result.qrBankId)
            }
        }

    @Test
    fun givenInterBankQr_whenViewModelCreated_thenEmitsInterbankResult() = runTest {
        // Given
        val qrString = "mpay://pay?qt=1&fsp=other-bank&ae=EXT123&cn=John Doe&am=100&cu=USD"
        val encodedQr = qrString.encodeBase64()
        val savedStateHandle = SavedStateHandle(mapOf(QR_DATA_ARG to encodedQr))

        // When
        val viewModel = FastMpayViewModel(savedStateHandle, processor)
        advanceUntilIdle()

        // Then
        viewModel.resultFlow.test {
            val result = awaitItem()
            assertIs<QrProcessResult.NavigateToInterbankTransfer>(result)
            assertEquals("EXT123", result.accountExternalId)
            assertEquals("John Doe", result.recipientName)
            assertEquals("100", result.amount)
        }
    }

    @Test
    fun givenMerchantQr_whenViewModelCreated_thenEmitsMerchantPaymentResult() = runTest {
        // Given
        val qrString = "mpay://pay?qt=3&fsp=mifos-bank&ci=123&cn=Coffee Shop&an=MERCH001&ai=456&cu=USD&am=5.50&oi=1&pi=2"
        val encodedQr = qrString.encodeBase64()
        val savedStateHandle = SavedStateHandle(mapOf(QR_DATA_ARG to encodedQr))

        // When
        val viewModel = FastMpayViewModel(savedStateHandle, processor)
        advanceUntilIdle()

        // Then
        viewModel.resultFlow.test {
            val result = awaitItem()
            assertIs<QrProcessResult.NavigateToMerchantPayment>(result)
            assertEquals("Coffee Shop", result.qrData.clientName)
        }
    }

    // endregion

    private fun assertNotNull(value: Any?) {
        assertTrue(value != null, "Expected non-null value")
    }
}
