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

import com.russhwolf.settings.Settings
import kpt.core.base.common.manager.DispatcherManager

/**
 * Fork implementation of [ForkPreferencesRepository]. `owner: fork` — never synced.
 *
 * `by delegate` forwards every framework preference to the template's own implementation, so this
 * class starts out complete and STAYS complete when the template ADDS a preference: the new member
 * arrives on the supertype and the delegate already satisfies it. No edit here, no compile break.
 *
 * It also receives the same storage handles the template's impl gets — [plainSettings],
 * [secureSettings] and [dispatcher] — because delegation alone gives you READ access to framework
 * preferences and nowhere to put your OWN. A fork needs somewhere to write.
 *
 * They are public `val`s, mirroring [UserPreferencesRepositoryImpl]: the whole point of this class is
 * that a fork fills it in, so the handles it is handed should be reachable rather than sealed off in
 * a class the fork owns anyway. It also means the compiler does not warn about unused private
 * members while the body is still empty.
 *
 * ## Add a preference
 * Declare it on [ForkPreferencesRepository], then implement it here against the settings:
 * ```
 * override val observeMyFlag: Flow<Boolean> =
 *     MutableStateFlow(plainSettings.getBoolean(KEY_MY_FLAG, false))
 *
 * override suspend fun setMyFlag(enabled: Boolean) = withContext(dispatcher.io) {
 *     plainSettings.putBoolean(KEY_MY_FLAG, enabled)
 * }
 * ```
 * Use [secureSettings] for anything sensitive — it is encrypted at rest on every platform that can
 * be (Android EncryptedSharedPreferences, iOS/native Keychain, desktop AES-GCM). Web is browser
 * storage and is NOT encrypted; do not put a credential there.
 *
 * Namespace your keys (e.g. a `project.` prefix) so a future framework preference cannot collide
 * with yours on the shared `Settings` instances.
 *
 * ## Alter a framework preference
 * `override` it — the explicit member wins over the delegated one, and `delegate.…` still reaches
 * the framework behaviour underneath:
 * ```
 * override suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
 *     analytics.log("theme_changed")
 *     delegate.setDarkThemeConfig(darkThemeConfig)
 * }
 * ```
 */
class ForkPreferencesRepositoryImpl(
    /**
     * The framework-shared implementation every call forwards to. Exposed so a fork can reach a base member it has not
     * overridden.
     */
    val delegate: UserPreferencesRepository,
    /** Unencrypted preferences — theme, language, onboarding. Cheap to read on every composition. */
    val plainSettings: Settings,
    /**
     * Encrypted preferences — passcode and auth token. A separate store because its reads are expensive and its
     * backing key is platform-held.
     */
    val secureSettings: Settings,
    /**
     * Dispatcher provider, so a write never blocks the caller's thread and tests can run it on a scheduler they
     * control.
     */
    val dispatcher: DispatcherManager,
) : ForkPreferencesRepository, UserPreferencesRepository by delegate
