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
import kpt.core.data.repository.RegistrationRepository
import kpt.core.model.network.entity.register.RegisterPayload
import kpt.core.model.network.entity.register.UserVerify
import kpt.core.network.FineractApiManager
/** Default [RegistrationRepository] backed by the registration API. */
@RepositoryBinding(binds = RegistrationRepository::class)
class RegistrationRepositoryImpl(
    private val apiManager: FineractApiManager,
    @FromQualifier("IO") private val ioDispatcher: CoroutineDispatcher,
) : RegistrationRepository {
    override suspend fun registerUser(registerPayload: RegisterPayload) {
        withContext(ioDispatcher) {
            apiManager.registrationAPi.registerUser(registerPayload)
        }
    }

    override suspend fun verifyUser(userVerify: UserVerify) {
        withContext(ioDispatcher) {
            apiManager.registrationAPi.verifyUser(userVerify)
        }
    }
}
