/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.mpay.qr.scan

import app.cash.turbine.test
import com.russhwolf.settings.MapSettings
import io.ktor.util.encodeBase64
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.common.manager.DispatcherManager
import kpt.core.data.util.QrTransferRouter
import kpt.core.datastore.prefs.ForkPreferencesRepository
import kpt.core.datastore.prefs.UserPreferencesRepository
import kpt.core.datastore.prefs.UserPreferencesRepositoryImpl
import kpt.core.model.account.DefaultAccount
import kpt.core.model.client.Client
import kpt.core.model.client.UpdatedClient
import kpt.core.model.instance.InterbankServer
import kpt.core.model.instance.ServerInstance
import kpt.core.model.user.Language
import kpt.core.model.user.UserInfo
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for [ScanQrViewModel].
 *
 * Tests cover:
 * - Torch toggle functionality
 * - Help dialog visibility
 * - Image processing states
 * - QR scan routing for intra-bank and inter-bank transfers
 * - Error handling for invalid QR codes
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ScanQrViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeUserPreferencesRepository: FakeUserPreferencesRepository
    private lateinit var qrTransferRouter: QrTransferRouter
    private lateinit var viewModel: ScanQrViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeUserPreferencesRepository = FakeUserPreferencesRepository()
        qrTransferRouter = QrTransferRouter(fakeUserPreferencesRepository)
        viewModel = ScanQrViewModel(
            qrTransferRouter = qrTransferRouter,
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // region Torch Tests

    @Test
    fun givenInitialState_whenViewModelCreated_thenTorchIsDisabled() {
        assertFalse(viewModel.isTorchEnabled.value)
    }

    @Test
    fun givenTorchDisabled_whenToggleTorch_thenTorchIsEnabled() = runTest {
        viewModel.toggleTorch()
        advanceUntilIdle()
        assertTrue(viewModel.isTorchEnabled.value)
    }

    @Test
    fun givenTorchEnabled_whenToggleTorch_thenTorchIsDisabled() = runTest {
        viewModel.toggleTorch()
        advanceUntilIdle()
        assertTrue(viewModel.isTorchEnabled.value)

        viewModel.toggleTorch()
        advanceUntilIdle()
        assertFalse(viewModel.isTorchEnabled.value)
    }

    @Test
    fun givenAnyState_whenSetTorchEnabled_thenTorchStateIsUpdated() = runTest {
        viewModel.setTorchEnabled(true)
        advanceUntilIdle()
        assertTrue(viewModel.isTorchEnabled.value)

        viewModel.setTorchEnabled(false)
        advanceUntilIdle()
        assertFalse(viewModel.isTorchEnabled.value)
    }

    // endregion

    // region Help Dialog Tests

    @Test
    fun givenInitialState_whenViewModelCreated_thenHelpDialogIsHidden() {
        assertFalse(viewModel.showHelpDialog.value)
    }

    @Test
    fun givenHelpDialogHidden_whenShowHelpDialog_thenHelpDialogIsVisible() = runTest {
        viewModel.showHelpDialog()
        advanceUntilIdle()
        assertTrue(viewModel.showHelpDialog.value)
    }

    @Test
    fun givenHelpDialogVisible_whenHideHelpDialog_thenHelpDialogIsHidden() = runTest {
        viewModel.showHelpDialog()
        advanceUntilIdle()
        assertTrue(viewModel.showHelpDialog.value)

        viewModel.hideHelpDialog()
        advanceUntilIdle()
        assertFalse(viewModel.showHelpDialog.value)
    }

    // endregion

    // region Image Processing Tests

    @Test
    fun givenInitialState_whenViewModelCreated_thenNotProcessingImage() {
        assertFalse(viewModel.isProcessingImage.value)
    }

    @Test
    fun givenNotProcessing_whenSetProcessingTrue_thenIsProcessing() = runTest {
        viewModel.setProcessingImage(true)
        advanceUntilIdle()
        assertTrue(viewModel.isProcessingImage.value)
    }

    @Test
    fun givenProcessing_whenSetProcessingFalse_thenNotProcessing() = runTest {
        viewModel.setProcessingImage(true)
        advanceUntilIdle()
        assertTrue(viewModel.isProcessingImage.value)

        viewModel.setProcessingImage(false)
        advanceUntilIdle()
        assertFalse(viewModel.isProcessingImage.value)
    }

    @Test
    fun givenInitialState_whenViewModelCreated_thenSelectedImageBytesIsNull() {
        assertNull(viewModel.selectedImageBytes.value)
    }

    @Test
    fun givenNoImage_whenSetSelectedImageBytes_thenImageBytesAreSet() = runTest {
        val imageBytes = byteArrayOf(1, 2, 3, 4)
        viewModel.setSelectedImageBytes(imageBytes)
        advanceUntilIdle()
        assertEquals(imageBytes, viewModel.selectedImageBytes.value)
    }

    @Test
    fun givenImageSelected_whenClearSelectedImage_thenImageBytesAreNullAndNotProcessing() = runTest {
        viewModel.setSelectedImageBytes(byteArrayOf(1, 2, 3))
        viewModel.setProcessingImage(true)
        advanceUntilIdle()

        viewModel.clearSelectedImage()
        advanceUntilIdle()

        assertNull(viewModel.selectedImageBytes.value)
        assertFalse(viewModel.isProcessingImage.value)
    }

    // endregion

    // region Image QR Scan Tests

    @Test
    fun givenImageSelected_whenOnImageQrScannedWithNull_thenNoQrFoundEventEmitted() = runTest {
        viewModel.setSelectedImageBytes(byteArrayOf(1, 2, 3))
        viewModel.setProcessingImage(true)
        advanceUntilIdle()

        viewModel.eventFlow.test {
            viewModel.onImageQrScanned(null)

            // Keep awaiting items until we find the expected event
            var foundEvent: ScanQrEvent? = null
            while (foundEvent == null) {
                val item = awaitItem()
                if (item is ScanQrEvent.OnNoQrFound) {
                    foundEvent = item
                }
            }
            assertIs<ScanQrEvent.OnNoQrFound>(foundEvent)
        }

        assertFalse(viewModel.isProcessingImage.value)
        assertNull(viewModel.selectedImageBytes.value)
    }

    // endregion

    // region QR Routing Tests

    @Test
    fun givenIntraBankQr_whenOnScanned_thenNavigateToIntraBankTransfer() = runTest {
        // Set up the fake repository to return matching fspId
        fakeUserPreferencesRepository.setSelectedInstance(
            ServerInstance(
                endpoint = "test.com",
                protocol = "https://",
                path = "/api/v1",
                platformTenantId = "mifos-bank",
                label = "Test Bank",
            ),
        )

        // Create a proper Base64 encoded QR code
        // Format: mpay://pay?qt=0&fsp=mifos-bank&ci=123&am=100&cn=John&an=ACC001&ai=456&cu=USD&oi=1&pi=2
        val qrString = "mpay://pay?qt=0&fsp=mifos-bank&ci=123&am=100&cn=John&an=ACC001&ai=456&cu=USD&oi=1&pi=2"
        val encodedQr = qrString.encodeBase64()

        viewModel.eventFlow.test {
            val result = viewModel.onScanned(encodedQr)

            assertTrue(result)
            // Find the navigation event (skip null and OnScanSuccess)
            var foundEvent: ScanQrEvent.OnNavigateToIntraBankTransfer? = null
            while (foundEvent == null) {
                val item = awaitItem()
                if (item is ScanQrEvent.OnNavigateToIntraBankTransfer) {
                    foundEvent = item
                }
            }
            assertEquals(123L, foundEvent.qrData.clientId)
            assertEquals(456L, foundEvent.qrData.accountId)
        }
    }

    @Test
    fun givenInterBankQr_whenOnScanned_thenNavigateToInterBankTransfer() = runTest {
        // Set up the fake repository with different fspId
        fakeUserPreferencesRepository.setSelectedInstance(
            ServerInstance(
                endpoint = "mybank.com",
                protocol = "https://",
                path = "/api/v1",
                platformTenantId = "my-bank",
                label = "My Bank",
            ),
        )

        // Create a proper Base64 encoded inter-bank QR code
        // Format: mpay://pay?qt=1&fsp=other-bank&ae=ACC123&cn=John Doe&am=100&cu=USD
        val qrString = "mpay://pay?qt=1&fsp=other-bank&ae=ACC123&cn=John Doe&am=100&cu=USD"
        val encodedQr = qrString.encodeBase64()

        viewModel.eventFlow.test {
            val result = viewModel.onScanned(encodedQr)

            assertTrue(result)
            // Find the navigation event (skip null and OnScanSuccess)
            var foundEvent: ScanQrEvent.OnNavigateToInterbankTransfer? = null
            while (foundEvent == null) {
                val item = awaitItem()
                if (item is ScanQrEvent.OnNavigateToInterbankTransfer) {
                    foundEvent = item
                }
            }
            assertEquals("ACC123", foundEvent.accountExternalId)
            assertEquals("John Doe", foundEvent.recipientName)
        }
    }

    @Test
    fun givenInvalidQrCode_whenOnScanned_thenShowErrorToast() = runTest {
        viewModel.eventFlow.test {
            val result = viewModel.onScanned("invalid-qr-data")

            assertFalse(result)
            // Find the ShowToast event (skip null)
            var foundEvent: ScanQrEvent.ShowToast? = null
            while (foundEvent == null) {
                val item = awaitItem()
                if (item is ScanQrEvent.ShowToast) {
                    foundEvent = item
                }
            }
            assertEquals("Scan a Valid QR Code", foundEvent.message)
        }
    }

    // endregion
}

/**
 * Fake implementation of [ForkPreferencesRepository] for testing.
 */
private class FakeUserPreferencesRepository :
    ForkPreferencesRepository,
    UserPreferencesRepository by templatePreferences() {
    private val _selectedInstance = MutableStateFlow<ServerInstance?>(null)

    override val selectedInstance: StateFlow<ServerInstance?> = _selectedInstance

    fun setSelectedInstance(instance: ServerInstance?) {
        _selectedInstance.value = instance
    }

    // Minimal implementations for interface compliance
    override val userInfo: Flow<UserInfo> = flowOf(
        UserInfo(
            username = "",
            userId = 0L,
            base64EncodedAuthenticationKey = "",
            authenticated = false,
            officeId = 0,
            officeName = "",
            roles = emptyList(),
            permissions = emptyList(),
            clients = emptyList(),
            shouldRenewPassword = false,
            isTwoFactorAuthenticationRequired = false,
        ),
    )
    override val token: StateFlow<String?> = MutableStateFlow(null)
    override val client: StateFlow<Client?> = MutableStateFlow(null)
    override val clientId: StateFlow<Long?> = MutableStateFlow(null)
    override val authToken: String? = null
    override val defaultAccount: StateFlow<DefaultAccount?> = MutableStateFlow(null)
    override val defaultAccountId: StateFlow<Long?> = MutableStateFlow(null)
    override val selectedInterbankInstance: StateFlow<InterbankServer?> = MutableStateFlow(null)
    override val accountExternalIds: StateFlow<Map<Long, String>> = MutableStateFlow(emptyMap())
    override val language: StateFlow<Language> = MutableStateFlow(Language.DEFAULT)

    override suspend fun updateToken(token: String) {}
    override suspend fun updateUserInfo(user: UserInfo) {}
    override suspend fun setLanguage(language: Language) {}
    override suspend fun updateClientInfo(client: Client) {}
    override suspend fun updateClientProfile(client: UpdatedClient) {}
    override suspend fun updateDefaultAccount(account: DefaultAccount) {}
    override suspend fun updateSelectedInstance(instance: ServerInstance) {}
    override suspend fun updateSelectedInterbankInstance(instance: InterbankServer) {}
    override suspend fun updateAccountExternalIds(accountExternalIds: Map<Long, String>) {}
    override fun getAccountExternalId(accountId: Long): String? = null
    override suspend fun logOut() {}
}

/** The template's own preferences over in-memory settings — the fake's framework half. */
private fun templatePreferences(): UserPreferencesRepository = UserPreferencesRepositoryImpl(
    plainSettings = MapSettings(),
    secureSettings = MapSettings(),
    dispatcher = object : DispatcherManager {
        override val default = Dispatchers.Unconfined
        override val main = Dispatchers.Main
        override val io = Dispatchers.Unconfined
        override val unconfined = Dispatchers.Unconfined
        override val appScope = CoroutineScope(Dispatchers.Unconfined)
    },
)
