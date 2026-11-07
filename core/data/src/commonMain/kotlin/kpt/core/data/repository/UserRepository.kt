/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.repository

import kpt.core.common.ScreenStateStream
import kpt.core.model.network.CommonResponse
import kpt.core.model.network.GenericResponse
import kpt.core.model.network.entity.UserWithRole
import kpt.core.model.user.NewUser
/** Repository for users and their roles. */
interface UserRepository {
    // Phase-3 cutover — Flow-shaped surfaces on ScreenState.
    /** Streams all users with their roles. */
    suspend fun getUsers(): ScreenStateStream<List<UserWithRole>>

    /** Streams the signed-in user with role. */
    suspend fun getUser(): ScreenStateStream<UserWithRole>

    /** Updates a user. */
    suspend fun updateUser(userId: Int, updatedUser: NewUser): ScreenStateStream<GenericResponse>

    // Writes throw on failure (DataState-free).
    /** Creates a user and returns its id. */
    suspend fun createUser(newUser: NewUser): Int

    /** Updates a user's password. */
    suspend fun updateUserPassword(userId: Long, password: String)

    /** Deletes a user. */
    suspend fun deleteUser(userId: Int): CommonResponse

    /** Assigns a client to a user. */
    suspend fun assignClientToUser(userId: Int, clientId: Int)
}
