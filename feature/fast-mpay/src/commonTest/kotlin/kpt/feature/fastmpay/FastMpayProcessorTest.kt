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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import kpt.core.model.beneficiary.Beneficiary
import kpt.core.model.instance.ServerInstance
import kpt.core.model.office.Office
import kpt.core.model.utils.QrCodeData
import kpt.core.model.utils.QrCodeType
import kpt.feature.fastmpay.model.QrProcessResult
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

/**
 * Unit tests for [FastMpayProcessor].
 *
 * Tests cover:
 * - Intra-bank QR processing with existing beneficiary
 * - Intra-bank QR processing without existing beneficiary
 * - Bank mismatch detection when FSP IDs differ
 * - Inter-bank QR processing
 * - Beneficiary QR processing
 * - Merchant QR processing
 * - Error handling
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FastMpayProcessorTest {

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

    // region Intra-bank QR Tests

    @Test
    fun givenIntraBankQrWithExistingBeneficiary_whenProcess_thenNavigateToMakeTransfer() = runTest {
        // Given
        val existingBeneficiary = Beneficiary(
            name = "John Doe",
            clientName = "John Doe",
            accountNumber = "ACC001",
            accountType = Beneficiary.AccountType(id = 2, code = "savings", value = "Savings"),
            officeName = "Head Office",
            transferLimit = 0,
        )
        fakeSelfServiceRepository.setBeneficiaryList(listOf(existingBeneficiary))
        fakeUserPreferencesRepository.setSelectedInstance(
            ServerInstance(
                endpoint = "test.com",
                protocol = "https://",
                path = "/api/v1",
                platformTenantId = "mifos-bank",
                label = "Test Bank",
            ),
        )

        val qrData = QrCodeData(
            type = QrCodeType.INTRA_BANK,
            fspId = "mifos-bank",
            clientId = 123L,
            clientName = "John Doe",
            accountNo = "ACC001",
            accountId = 456L,
            officeId = 1,
            accountTypeId = 2,
            currency = "USD",
            amount = "100",
        )

        // When
        val result = processor.processQrCode(qrData, this)

        // Then
        assertIs<QrProcessResult.NavigateToMakeTransfer>(result)
        assertEquals("John Doe", result.beneficiaryName)
        assertEquals(qrData, result.qrData)
    }

    @Test
    fun givenIntraBankQrWithoutExistingBeneficiary_whenProcess_thenNavigateToAddBeneficiary() =
        runTest {
            // Given
            fakeSelfServiceRepository.setBeneficiaryList(emptyList())
            fakeUserPreferencesRepository.setSelectedInstance(
                ServerInstance(
                    endpoint = "test.com",
                    protocol = "https://",
                    path = "/api/v1",
                    platformTenantId = "mifos-bank",
                    label = "Test Bank",
                ),
            )

            val qrData = QrCodeData(
                type = QrCodeType.INTRA_BANK,
                fspId = "mifos-bank",
                clientId = 123L,
                clientName = "Jane Doe",
                accountNo = "ACC002",
                accountId = 456L,
                officeId = 1,
                accountTypeId = 2,
                currency = "USD",
                amount = "",
            )

            // When
            val result = processor.processQrCode(qrData, this)

            // Then
            assertIs<QrProcessResult.NavigateToAddBeneficiary>(result)
            assertNotNull(result.beneficiaryData)
            assertEquals(QrCodeType.INTRA_BANK, result.sourceQrType)
        }

    @Test
    fun givenIntraBankQrFromDifferentBank_whenProcess_thenReturnBankMismatch() = runTest {
        // Given
        fakeUserPreferencesRepository.setSelectedInstance(
            ServerInstance(
                endpoint = "test.com",
                protocol = "https://",
                path = "/api/v1",
                platformTenantId = "mifos-bank-1",
                label = "My Bank",
            ),
        )

        val qrData = QrCodeData(
            type = QrCodeType.INTRA_BANK,
            fspId = "mifos-bank-2",
            clientId = 123L,
            clientName = "John Doe",
            accountNo = "ACC001",
            accountId = 456L,
            officeId = 1,
            accountTypeId = 2,
            currency = "USD",
            amount = "100",
        )

        // When
        val result = processor.processQrCode(qrData, this)

        // Then
        assertIs<QrProcessResult.BankMismatch>(result)
        assertEquals("mifos-bank-1", result.currentBankId)
        assertEquals("mifos-bank-2", result.qrBankId)
        assertEquals(qrData, result.qrData)
    }

    @Test
    fun givenIntraBankQrWithSameFspIdCaseInsensitive_whenProcess_thenNotBankMismatch() = runTest {
        // Given - FSP IDs match but different case
        fakeSelfServiceRepository.setBeneficiaryList(emptyList())
        fakeUserPreferencesRepository.setSelectedInstance(
            ServerInstance(
                endpoint = "test.com",
                protocol = "https://",
                path = "/api/v1",
                platformTenantId = "MIFOS-BANK",
                label = "My Bank",
            ),
        )

        val qrData = QrCodeData(
            type = QrCodeType.INTRA_BANK,
            fspId = "mifos-bank",
            clientId = 123L,
            clientName = "John Doe",
            accountNo = "ACC001",
            accountId = 456L,
            officeId = 1,
            accountTypeId = 2,
            currency = "USD",
            amount = "",
        )

        // When
        val result = processor.processQrCode(qrData, this)

        // Then - Should not be BankMismatch since FSP IDs match (case-insensitive)
        assertIs<QrProcessResult.NavigateToAddBeneficiary>(result)
        assertEquals(QrCodeType.INTRA_BANK, result.sourceQrType)
    }

    @Test
    fun givenIntraBankQrWithNullFspId_whenProcess_thenNotBankMismatch() = runTest {
        // Given - QR has no FSP ID
        fakeSelfServiceRepository.setBeneficiaryList(emptyList())
        fakeUserPreferencesRepository.setSelectedInstance(
            ServerInstance(
                endpoint = "test.com",
                protocol = "https://",
                path = "/api/v1",
                platformTenantId = "mifos-bank",
                label = "My Bank",
            ),
        )

        val qrData = QrCodeData(
            type = QrCodeType.INTRA_BANK,
            fspId = null,
            clientId = 123L,
            clientName = "John Doe",
            accountNo = "ACC001",
            accountId = 456L,
            officeId = 1,
            accountTypeId = 2,
            currency = "USD",
            amount = "",
        )

        // When
        val result = processor.processQrCode(qrData, this)

        // Then - Should not be BankMismatch when QR has no FSP ID
        assertIs<QrProcessResult.NavigateToAddBeneficiary>(result)
        assertEquals(QrCodeType.INTRA_BANK, result.sourceQrType)
    }

    @Test
    fun givenBeneficiaryFetchError_whenProcess_thenFallbackToAddBeneficiary() = runTest {
        // Given
        fakeSelfServiceRepository.setShouldReturnError(true)
        fakeUserPreferencesRepository.setSelectedInstance(
            ServerInstance(
                endpoint = "test.com",
                protocol = "https://",
                path = "/api/v1",
                platformTenantId = "mifos-bank",
                label = "Test Bank",
            ),
        )

        val qrData = QrCodeData(
            type = QrCodeType.INTRA_BANK,
            fspId = "mifos-bank",
            clientId = 123L,
            clientName = "John Doe",
            accountNo = "ACC001",
            accountId = 456L,
            officeId = 1,
            accountTypeId = 2,
            currency = "USD",
            amount = "",
        )

        // When
        val result = processor.processQrCode(qrData, this)

        // Then - On error, should fallback to add beneficiary
        assertIs<QrProcessResult.NavigateToAddBeneficiary>(result)
        assertEquals(QrCodeType.INTRA_BANK, result.sourceQrType)
    }

    // endregion

    // region Inter-bank QR Tests

    @Test
    fun givenInterBankQrWithAccountExternalId_whenProcess_thenNavigateToInterbankTransfer() =
        runTest {
            // Given
            val qrData = QrCodeData(
                type = QrCodeType.INTER_BANK,
                fspId = "other-bank",
                clientId = 0L,
                clientName = "John Doe",
                accountNo = "",
                accountId = 0L,
                accountExternalId = "EXT123456",
                currency = "USD",
                amount = "500",
            )

            // When
            val result = processor.processQrCode(qrData, this)

            // Then
            assertIs<QrProcessResult.NavigateToInterbankTransfer>(result)
            assertEquals("EXT123456", result.accountExternalId)
            assertEquals("John Doe", result.recipientName)
            assertEquals("500", result.amount)
        }

    @Test
    fun givenInterBankQrWithBlankAccountExternalId_whenProcess_thenReturnError() = runTest {
        // Given
        val qrData = QrCodeData(
            type = QrCodeType.INTER_BANK,
            fspId = "other-bank",
            clientId = 0L,
            clientName = "John Doe",
            accountNo = "",
            accountId = 0L,
            accountExternalId = "",
            currency = "USD",
            amount = "500",
        )

        // When
        val result = processor.processQrCode(qrData, this)

        // Then
        assertIs<QrProcessResult.Error>(result)
        assertEquals("Inter-bank QR missing account external ID", result.message)
    }

    @Test
    fun givenInterBankQrWithNullAccountExternalId_whenProcess_thenReturnError() = runTest {
        // Given
        val qrData = QrCodeData(
            type = QrCodeType.INTER_BANK,
            fspId = "other-bank",
            clientId = 0L,
            clientName = "John Doe",
            accountNo = "",
            accountId = 0L,
            accountExternalId = null,
            currency = "USD",
            amount = "500",
        )

        // When
        val result = processor.processQrCode(qrData, this)

        // Then
        assertIs<QrProcessResult.Error>(result)
        assertEquals("Inter-bank QR missing account external ID", result.message)
    }

    @Test
    fun givenInterBankQrWithOptionalFieldsBlank_whenProcess_thenOptionalFieldsAreNull() = runTest {
        // Given
        val qrData = QrCodeData(
            type = QrCodeType.INTER_BANK,
            fspId = "other-bank",
            clientId = 0L,
            clientName = "",
            accountNo = "",
            accountId = 0L,
            accountExternalId = "EXT123",
            currency = "USD",
            amount = "",
        )

        // When
        val result = processor.processQrCode(qrData, this)

        // Then
        assertIs<QrProcessResult.NavigateToInterbankTransfer>(result)
        assertEquals("EXT123", result.accountExternalId)
        assertEquals(null, result.recipientName)
        assertEquals(null, result.amount)
    }

    // endregion

    // region Beneficiary QR Tests

    @Test
    fun givenBeneficiaryQr_whenProcess_thenNavigateToAddBeneficiary() = runTest {
        // Given
        val qrData = QrCodeData(
            type = QrCodeType.BENEFICIARY,
            fspId = "mifos-bank",
            clientId = 123L,
            clientName = "John Doe",
            accountNo = "ACC001",
            accountId = 456L,
            officeId = 1,
            accountTypeId = 2,
            currency = "USD",
            amount = "",
        )

        // When
        val result = processor.processQrCode(qrData, this)

        // Then
        assertIs<QrProcessResult.NavigateToAddBeneficiary>(result)
        assertNotNull(result.beneficiaryData)
        assertEquals(QrCodeType.BENEFICIARY, result.sourceQrType)
    }

    // endregion

    // region Office Name Resolution Tests

    @Test
    fun givenIntraBankQrWithOfficeId_whenProcess_thenResolvesOfficeName() = runTest {
        // Given
        fakeSelfServiceRepository.setBeneficiaryList(emptyList())
        fakeOfficeRepository.setOfficeList(
            listOf(
                Office(id = 1, name = "Head Office"),
                Office(id = 5, name = "Lagos Branch"),
            ),
        )
        fakeUserPreferencesRepository.setSelectedInstance(
            ServerInstance(
                endpoint = "test.com",
                protocol = "https://",
                path = "/api/v1",
                platformTenantId = "mifos-bank",
                label = "Test Bank",
            ),
        )

        val qrData = QrCodeData(
            type = QrCodeType.INTRA_BANK,
            fspId = "mifos-bank",
            clientId = 123L,
            clientName = "John Doe",
            accountNo = "ACC001",
            accountId = 456L,
            // Lagos Branch
            officeId = 5,
            accountTypeId = 2,
            currency = "USD",
            amount = "",
        )

        // When
        val result = processor.processQrCode(qrData, this)

        // Then
        assertIs<QrProcessResult.NavigateToAddBeneficiary>(result)
        val beneficiary = Json.decodeFromString<Beneficiary>(result.beneficiaryData)
        assertEquals("Lagos Branch", beneficiary.officeName)
        assertEquals(QrCodeType.INTRA_BANK, result.sourceQrType)
    }

    @Test
    fun givenOfficeApiError_whenProcess_thenFallbackToDefaultOfficeName() = runTest {
        // Given
        fakeSelfServiceRepository.setBeneficiaryList(emptyList())
        fakeOfficeRepository.setShouldReturnError(true)
        fakeUserPreferencesRepository.setSelectedInstance(
            ServerInstance(
                endpoint = "test.com",
                protocol = "https://",
                path = "/api/v1",
                platformTenantId = "mifos-bank",
                label = "Test Bank",
            ),
        )

        val qrData = QrCodeData(
            type = QrCodeType.INTRA_BANK,
            fspId = "mifos-bank",
            clientId = 123L,
            clientName = "John Doe",
            accountNo = "ACC001",
            accountId = 456L,
            officeId = 5,
            accountTypeId = 2,
            currency = "USD",
            amount = "",
        )

        // When
        val result = processor.processQrCode(qrData, this)

        // Then
        assertIs<QrProcessResult.NavigateToAddBeneficiary>(result)
        val beneficiary = Json.decodeFromString<Beneficiary>(result.beneficiaryData)
        assertEquals("Head Office", beneficiary.officeName)
        assertEquals(QrCodeType.INTRA_BANK, result.sourceQrType)
    }

    @Test
    fun givenOfficeIdNotFound_whenProcess_thenFallbackToDefaultOfficeName() = runTest {
        // Given
        fakeSelfServiceRepository.setBeneficiaryList(emptyList())
        fakeOfficeRepository.setOfficeList(
            listOf(Office(id = 1, name = "Head Office")),
        )
        fakeUserPreferencesRepository.setSelectedInstance(
            ServerInstance(
                endpoint = "test.com",
                protocol = "https://",
                path = "/api/v1",
                platformTenantId = "mifos-bank",
                label = "Test Bank",
            ),
        )

        val qrData = QrCodeData(
            type = QrCodeType.INTRA_BANK,
            fspId = "mifos-bank",
            clientId = 123L,
            clientName = "John Doe",
            accountNo = "ACC001",
            accountId = 456L,
            // Non-existent office ID
            officeId = 999,
            accountTypeId = 2,
            currency = "USD",
            amount = "",
        )

        // When
        val result = processor.processQrCode(qrData, this)

        // Then
        assertIs<QrProcessResult.NavigateToAddBeneficiary>(result)
        val beneficiary = Json.decodeFromString<Beneficiary>(result.beneficiaryData)
        assertEquals("Head Office", beneficiary.officeName)
        assertEquals(QrCodeType.INTRA_BANK, result.sourceQrType)
    }

    // endregion

    // region Merchant QR Tests

    @Test
    fun givenMerchantQr_whenProcess_thenNavigateToMerchantPayment() = runTest {
        // Given
        val qrData = QrCodeData(
            type = QrCodeType.MERCHANT,
            fspId = "mifos-bank",
            clientId = 123L,
            clientName = "Coffee Shop",
            accountNo = "MERCH001",
            accountId = 456L,
            officeId = 1,
            accountTypeId = 2,
            currency = "USD",
            amount = "5.50",
        )

        // When
        val result = processor.processQrCode(qrData, this)

        // Then
        assertIs<QrProcessResult.NavigateToMerchantPayment>(result)
        assertEquals(qrData, result.qrData)
    }

    // endregion
}
