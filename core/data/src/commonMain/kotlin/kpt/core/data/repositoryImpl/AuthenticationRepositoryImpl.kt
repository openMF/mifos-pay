/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.repositoryImpl

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kpt.core.base.data.annotation.FromQualifier
import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.data.mapper.toUserInfo
import kpt.core.data.repository.AuthenticationRepository
import kpt.core.model.fork.user.UserInfo
import kpt.core.model.network.entity.authentication.AuthenticationPayload
import kpt.core.network.SelfServiceApiManager
/** Default [AuthenticationRepository] backed by the authentication API. */
@RepositoryBinding(binds = AuthenticationRepository::class)
class AuthenticationRepositoryImpl(
    private val apiManager: SelfServiceApiManager,
    @FromQualifier("IO") private val ioDispatcher: CoroutineDispatcher,
) : AuthenticationRepository {
    override suspend fun authenticate(username: String, password: String): UserInfo {
        val payload = AuthenticationPayload(username, password)

        return withContext(ioDispatcher) {
            apiManager.authenticationApi.authenticate(payload).toUserInfo()
        }
    }
}
