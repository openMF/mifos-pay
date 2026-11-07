/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.repository

import kpt.core.model.fork.user.UserInfo
/** Repository for user authentication. */
interface AuthenticationRepository {
    /** Authenticates with the given credentials and returns the signed-in [UserInfo]. */
    suspend fun authenticate(username: String, password: String): UserInfo
}
