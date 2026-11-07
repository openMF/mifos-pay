/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.registry.app

import kotlinx.coroutines.runBlocking
import kpt.core.base.network.AuthScheme
import kpt.core.base.network.RuntimeHeaderStore
import kpt.core.datastore.prefs.ForkPreferencesRepository
import org.koin.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Signing in makes the very next self-service request authenticated.
 *
 * The wallet saves the session token through [ForkPreferencesRepository]; core-base's auth bridge turns it
 * into the `selfservice` access point's `Authorization` header. If the two read different stores, or the
 * bridge applies the token late, the first call after sign-in (`self/clients/{id}`) goes out without a
 * credential and Fineract answers 403.
 */
class SignInAuthHeaderTest {

    @Test
    fun savingTheTokenSetsTheSelfServiceAuthorizationAtOnce() = runBlocking {
        val app = koinApplication {
            allowOverride(true)
            modules(cmp.navigation.di.KoinModules.allModules)
        }
        val prefs = app.koin.get<ForkPreferencesRepository>()
        val headers = app.koin.get<RuntimeHeaderStore>()
        val key = AuthScheme.runtimeKeyFor("selfservice")

        prefs.updateToken("dXNlcjpwYXNz")
        assertEquals("Basic dXNlcjpwYXNz", headers[key], "the next self-service request must carry it")

        prefs.logOut()
        assertNull(headers[key], "signed out: no credential on the next request")
        app.close()
    }
}
