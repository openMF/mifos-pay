/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.domain

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kpt.core.common.utils.OpenForMokkery
import kpt.core.data.repository.AuthenticationRepository
import kpt.core.data.repository.ClientRepository
import kpt.core.datastore.prefs.ForkPreferencesRepository
import kpt.core.model.user.UserInfo
@OpenForMokkery
class LoginUseCase(
    private val repository: AuthenticationRepository,
    private val clientRepository: ClientRepository,
    private val userPreferencesRepository: ForkPreferencesRepository,
    private val ioDispatcher: CoroutineDispatcher,
) {
    /**
     * Authenticates the user, persists the session token + resolved client/user
     * info into preferences, and returns the [UserInfo] on success. Throws on any
     * failure — the caller (LoginViewModel's SubmitHandler) maps the thrown
     * exception to its Failed state and surfaces a feature StringResource. The
     * exception messages below are diagnostic (not surfaced verbatim to the user).
     */
    suspend operator fun invoke(username: String, password: String): UserInfo {
        val userInfo = try {
            withContext(ioDispatcher) {
                repository.authenticate(username, password)
            }
        } catch (e: Exception) {
            throw IllegalStateException("Invalid credentials", e)
        }

        if (userInfo.clients.isEmpty()) {
            error("No clients found")
        }

        return persistSession(userInfo)
    }

    private suspend fun persistSession(userInfo: UserInfo): UserInfo {
        withContext(ioDispatcher) {
            userPreferencesRepository.updateToken(userInfo.base64EncodedAuthenticationKey)
        }

        val client = try {
            withContext(ioDispatcher) {
                clientRepository.getClient(userInfo.clients.first())
            }
        } catch (e: Exception) {
            userPreferencesRepository.logOut()
            throw IllegalStateException("No client found", e)
        }

        withContext(ioDispatcher) {
            userPreferencesRepository.updateClientInfo(client)
            userPreferencesRepository.updateUserInfo(userInfo)
        }

        return userInfo
    }
}
