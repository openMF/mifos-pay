/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.registry.app

import cmp.navigation.registry.app.navigation.ForkNavGraph.LOGIN_GRAPH
import kpt.core.model.fork.user.UserInfo
import kpt.feature.passcode.ROOT_PASSCODE_ROUTE
import kotlin.test.Test
import kotlin.test.assertEquals

class StartDestinationTest {
    private fun user(authenticated: Boolean) = UserState.Authenticated(
        UserInfo(
            username = "andromeda",
            userId = 1,
            base64EncodedAuthenticationKey = "key",
            authenticated = authenticated,
            officeId = 1,
            officeName = "Head Office",
            roles = emptyList(),
            permissions = emptyList(),
            clients = listOf(2),
            shouldRenewPassword = false,
            isTwoFactorAuthenticationRequired = false,
        ),
    )

    @Test
    fun signedInWithPasscodeStartsAtPasscode() =
        assertEquals(ROOT_PASSCODE_ROUTE, startDestination(user(true), isPasscodeNotCreated = false))

    @Test
    fun signedInWithoutPasscodeStartsAtLogin() =
        assertEquals(LOGIN_GRAPH, startDestination(user(true), isPasscodeNotCreated = true))

    @Test
    fun signedOutStartsAtLogin() =
        assertEquals(LOGIN_GRAPH, startDestination(user(false), isPasscodeNotCreated = false))

    @Test
    fun notYetLoadedStartsAtLogin() =
        assertEquals(LOGIN_GRAPH, startDestination(UserState.UnAuthenticated, isPasscodeNotCreated = false))
}
