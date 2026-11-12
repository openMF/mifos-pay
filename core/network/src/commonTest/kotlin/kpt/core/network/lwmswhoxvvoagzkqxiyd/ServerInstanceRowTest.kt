/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.lwmswhoxvvoagzkqxiyd

import kotlinx.serialization.json.Json
import kpt.core.network.lwmswhoxvvoagzkqxiyd.appserverconfig.dto.RemoteServerInstanceDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The DTO against the LIVE `app_server_config` row shape (columns captured 2026-10-30 with the publishable
 * key alone — public server config; values trimmed, demo account invented).
 */
class ServerInstanceRowTest {

    private val liveRows = """
        [{"id":"c1cddd98-fb0c-4f9d-b692-281d872655eb","label":"Mifos sandbox","protocol":"https://",
          "endpoint":"sandbox.mifos.community","path":"/fineract-provider/api/v1/","port":null,
          "platform_tenant_id":"default","is_default":false,"supported_apps":["field-officer","mifos-pay"],
          "demo_accounts":[{"role":"officer","username":"demo","password":"demo"}],"interbank_servers":[],
          "sort_order":3,"enabled":true,"created_at":"2026-10-25T12:00:00+00:00","updated_at":"2026-10-25T12:00:00+00:00"}]
    """

    @Test
    fun theLiveRowShapeDecodes() {
        // ignoreUnknownKeys mirrors supabase-kt's own decoder: id / sort_order / enabled / timestamps are
        // server-side bookkeeping the client does not model.
        val row = Json { ignoreUnknownKeys = true }.decodeFromString<List<RemoteServerInstanceDto>>(liveRows).single()
        assertEquals("sandbox.mifos.community", row.endpoint)
        assertEquals("default", row.platformTenantId)
        assertNull(row.port)
        assertTrue("field-officer" in row.supportedApps)
        assertEquals("officer", row.demoAccounts.single().role)
    }
}
