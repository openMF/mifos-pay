/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.config

import kpt.core.base.network.HttpLogDetail
import kpt.core.base.network.NetworkPolicy
import kpt.core.base.network.maskQueryParams
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The network privacy policy: what a build may log, and that credentials never reach a log line. */
class NetworkPolicyTest {

    @Test
    fun releaseBuildsLogNothingByDefault() {
        assertEquals(HttpLogDetail.NONE, NetworkPolicy().logDetail(debugBuild = false))
    }

    @Test
    fun releaseBuildsAreClampedBelowBodiesEvenIfConfigured() {
        // The generator refuses `release: body`; the runtime clamps too, so a hand-built policy can't leak.
        val policy = NetworkPolicy(releaseLogging = HttpLogDetail.BODY)
        assertEquals(HttpLogDetail.HEADERS, policy.logDetail(debugBuild = false))
        assertEquals(HttpLogDetail.BODY, policy.logDetail(debugBuild = true))
    }

    @Test
    fun noCorsRelayUnlessDeclared() {
        assertNull(NetworkPolicy().webCorsProxy)
    }

    @Test
    fun theGeneratedTemplatePolicyIsReleaseSafe() {
        assertTrue(AppAccessPoints.policy.logDetail(debugBuild = false) <= HttpLogDetail.HEADERS)
    }

    @Test
    fun credentialQueryParamsAreMaskedInLoggedUrls() {
        val params = NetworkPolicy(redactQueryParams = listOf("tenantKey")).allRedactedQueryParams
        val line = "REQUEST: https://api.stlouisfed.org/fred/series?series_id=FEDFUNDS&api_key=abc123&file_type=json" +
            " https://x.test/a?tenantKey=t-9&page=2 https://x.test/b?API_KEY=Zz"
        val masked = maskQueryParams(line, params)
        assertFalse("abc123" in masked || "t-9" in masked || "Zz" in masked, masked)
        assertTrue("series_id=FEDFUNDS" in masked && "page=2" in masked, "non-secret params keep their values: $masked")
        assertTrue("api_key=***" in masked && "tenantKey=***" in masked, masked)
    }
}
