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
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kpt.core.common.ScreenStateStream
import kpt.core.common.asScreenStateFlow
import kpt.core.data.mapper.toEntity
import kpt.core.data.repository.UserRepository
import kpt.core.model.network.CommonResponse
import kpt.core.model.network.GenericResponse
import kpt.core.model.network.entity.UserWithRole
import kpt.core.model.network.entity.user.UpdateUserEntityPassword
import kpt.core.model.user.NewUser
import kpt.core.network.SelfServiceApiManager
/** Default [UserRepository] backed by the user API. */
class UserRepositoryImpl(
    private val selfServiceApiManager: SelfServiceApiManager,
    private val ioDispatcher: CoroutineDispatcher,
) : UserRepository {
    override suspend fun getUsers(): ScreenStateStream<List<UserWithRole>> {
        return selfServiceApiManager.userApi.users()
            .asScreenStateFlow(isEmpty = { it.isEmpty() })
            .flowOn(ioDispatcher)
    }

    override suspend fun getUser(): ScreenStateStream<UserWithRole> {
        return selfServiceApiManager.userApi.getUser()
            .asScreenStateFlow()
            .flowOn(ioDispatcher)
    }

    override suspend fun createUser(newUser: NewUser): Int {
        return withContext(ioDispatcher) {
            selfServiceApiManager.userApi.createUser(newUser.toEntity()).resourceId
        }
    }

    override suspend fun updateUser(
        userId: Int,
        updatedUser: NewUser,
    ): ScreenStateStream<GenericResponse> {
        return selfServiceApiManager.userApi
            .updateUser(updatedUser.toEntity())
            .asScreenStateFlow()
            .flowOn(ioDispatcher)
    }

    override suspend fun updateUserPassword(
        userId: Long,
        password: String,
    ) {
        withContext(ioDispatcher) {
            selfServiceApiManager.userApi.updateUserPassword(
                updateUserEntity = UpdateUserEntityPassword(
                    password,
                    password,
                ),
            )
        }
    }

    override suspend fun deleteUser(userId: Int): CommonResponse {
        return withContext(ioDispatcher) {
            selfServiceApiManager.userApi.deleteUser(userId)
        }
    }

    override suspend fun assignClientToUser(userId: Int, clientId: Int) {
        withContext(ioDispatcher) {
            selfServiceApiManager.userApi.assignClientToUser(userId, mapOf("clients" to listOf(clientId)))
        }
    }
}
