/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.datastore.prefs

import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlinx.coroutines.test.runTest
import kpt.core.base.common.manager.DispatcherManager
import kpt.core.model.user.DarkThemeConfig
import kpt.core.model.user.LanguageConfig
import kpt.core.model.user.ThemeBrand
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

/**
 * Locks the sign-out contract: `clearUserData()` discards what belongs to the PERSON and keeps what
 * belongs to the DEVICE.
 *
 * Nothing covered this before, and the implementation only flipped `isAuthenticated` — so
 * `activeUserId` and `passcode` stayed readable on disk, in BOTH stores, and the next person on a
 * shared device inherited them. A test that asserts only "isAuthenticated == false" would have
 * passed against that bug, which is why every assertion here reads the value back.
 */
class UserPreferencesClearUserDataTest {

    private val plain = MapSettings()
    private val secure = MapSettings()

    private val dispatchers = object : DispatcherManager {
        override val default: CoroutineDispatcher = Dispatchers.Unconfined
        override val main: MainCoroutineDispatcher = Dispatchers.Main
        override val io: CoroutineDispatcher = Dispatchers.Unconfined
        override val unconfined: CoroutineDispatcher = Dispatchers.Unconfined
        override val appScope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined)
    }

    private fun repo() = UserPreferencesRepositoryImpl(plain, secure, dispatchers)

    @Test
    fun clearUserDataRemovesIdentityFromBothStores() = runTest {
        val repo = repo()
        repo.setPasscode("9182")
        repo.setIsAuthenticated(true)
        repo.setAuthToken("secret-token")

        repo.clearUserData()

        // Read through a FRESH instance: the contract is about what survives on disk, not about the
        // in-memory StateFlow of the instance that did the clearing.
        val reloaded = repo()
        val data = reloaded.userData.value
        assertNotEquals("9182", data.passcode, "passcode must not survive sign-out")
        assertEquals("", data.activeUserId, "activeUserId must not survive sign-out")
        assertFalse(data.isAuthenticated, "sign-out must leave the app unauthenticated")
        assertFalse(data.isUnlocked, "sign-out must leave the app locked")
        assertNull(reloaded.authToken, "auth token must not survive sign-out")
    }

    @Test
    fun clearUserDataKeepsDevicePreferences() = runTest {
        val repo = repo()
        repo.setThemeBrand(ThemeBrand.ANDROID)
        repo.setDarkThemeConfig(DarkThemeConfig.DARK)
        repo.setLanguage(LanguageConfig.ENGLISH)
        repo.setShowOnboarding(false)

        repo.clearUserData()

        val data = repo().userData.value
        assertEquals(ThemeBrand.ANDROID, data.themeBrand, "theme is a device preference")
        assertEquals(DarkThemeConfig.DARK, data.darkThemeConfig, "dark-mode is a device preference")
        assertEquals(LanguageConfig.ENGLISH, data.appLanguage, "language is a device preference")
        assertFalse(data.showOnboarding, "onboarding-seen must not reset on sign-out")
    }
}
