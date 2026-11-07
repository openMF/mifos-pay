/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-pay/blob/master/LICENSE.md
 */
package org.mifos.corebase.network

import kotlin.test.Test
import kotlin.test.assertEquals

class WebRelayTest {
    @Test
    fun cloudflarePagesHostsUseTheirOwnRelay() {
        assertEquals(
            "https://mifos-pay.pages.dev",
            relayOriginFor("mifos-pay.pages.dev", "https://mifos-pay.pages.dev"),
        )
        assertEquals(
            "https://abc.mifos-pay.pages.dev",
            relayOriginFor("abc.mifos-pay.pages.dev", "https://abc.mifos-pay.pages.dev"),
        )
    }

    @Test
    fun otherHostsUseTheHostedRelay() {
        assertEquals(HOSTED_RELAY_ORIGIN, relayOriginFor("openmf.github.io", "https://openmf.github.io"))
        assertEquals(
            HOSTED_RELAY_ORIGIN,
            relayOriginFor("evilpages.dev.example.com", "https://evilpages.dev.example.com"),
        )
    }
}
