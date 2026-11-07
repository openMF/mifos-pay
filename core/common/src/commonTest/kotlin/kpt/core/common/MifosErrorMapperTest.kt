/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.common

import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

class MifosErrorMapperTest {
    @Test
    fun unauthorizedStatusSignsOut() =
        assertEquals(ScreenState.Unauthenticated, MifosErrorMapper.mapError<Unit>(HttpStatusException(401, "no")))

    @Test
    fun otherStatusIsAnErrorCarryingTheException() {
        val e = HttpStatusException(404, "missing")
        val state = assertIs<ScreenState.Error>(MifosErrorMapper.mapError<Unit>(e))
        assertSame(e, state.error)
    }

    @Test
    fun networkAndIoFailuresAreNoNetwork() {
        assertIs<ScreenState.NoNetwork>(MifosErrorMapper.mapError<Unit>(NetworkException("offline")))
        assertIs<ScreenState.NoNetwork>(MifosErrorMapper.mapError<Unit>(IOException("reset")))
    }

    @Test
    fun anythingElseIsAnError() {
        val e = IllegalStateException("boom")
        assertSame(e, assertIs<ScreenState.Error>(MifosErrorMapper.mapError<Unit>(e)).error)
    }
}
