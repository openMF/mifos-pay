/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.lwmswhoxvvoagzkqxiyd.config

import kotlinx.coroutines.test.runTest
import kpt.core.base.network.AccessPointKind
import kpt.core.base.network.AccessPointRegistry
import kpt.core.base.network.SupabaseClientFactory
import kpt.core.network.config.AppAccessPoints
import kpt.core.network.lwmswhoxvvoagzkqxiyd.appserverconfig.api.ServerInstanceApi
import kpt.core.network.lwmswhoxvvoagzkqxiyd.appserverconfig.api.impl.ServerInstanceApiImpl
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The Supabase access-point path, end to end on the NEUTRAL template.
 *
 * Lives under the ENDPOINT's own package (`kpt.core.network.lwmswhoxvvoagzkqxiyd`), not `config`, because it
 * depends on that endpoint's API facade: `remove-demo.sh` deletes an `owner: template` access point's
 * package, and a test sitting outside it would survive holding an import of a class that no longer
 * exists — an unresolved reference in :core:network:commonTest for every cleaned fork.
 *
 * The tests run the declared points with their publishable key STRIPPED (see [unconfiguredPoints]) — the
 * state every fork is in before it configures Supabase, and the one worth testing: the whole path
 * (registry -> factory -> generated binding -> API facade) has to be constructible and callable
 * without a backend. If it were not, the generated `supabaseApi(...)` binding would blow up Koin
 * graph construction on a fresh clone. Stripping the key rather than relying on its absence keeps the
 * suite off the network on a machine where `/secrets pull` HAS materialized one.
 */
class SupabaseAccessPointTest {

    private val unconfiguredPoints = AppAccessPoints.points.map { p ->
        p.copy(supabase = p.supabase?.copy(publishableKey = ""))
    }

    private fun factory() = SupabaseClientFactory(
        registry = AccessPointRegistry(unconfiguredPoints),
    )

    @Test
    fun `every declared supabase point carries its generated spec`() {
        val registry = AccessPointRegistry(AppAccessPoints.points)
        val supabase = registry.supabasePoints()
        assertTrue(supabase.isNotEmpty(), "template declares at least one Supabase access point")
        // The factory reads the key from here alone; a point without it would be a keyless client.
        supabase.forEach { assertNotNull(it.supabase, "${it.id} has no SupabaseSpec — run syncForkConfig") }
    }

    @Test
    fun `unconfigured supabase point yields an inert client rather than throwing`() {
        val point = AccessPointRegistry(AppAccessPoints.points).supabasePoints().first()
        val client = factory().requireClientFor(point.id)
        // Empty key -> isConfigured false. Constructing the client must still succeed:
        // SupabaseConfigClient.client is `by lazy`, so nothing touches the network.
        assertFalse(client.isConfigured, "a point with no publishable key must read as unconfigured")
    }

    @Test
    fun `requireClientFor names the declared points when the id is wrong`() {
        val declared = AccessPointRegistry(AppAccessPoints.points).supabasePoints().map { it.id }
        val error = assertFailsWith<IllegalStateException> { factory().requireClientFor("no-such-point") }
        val message = error.message.orEmpty()
        assertTrue("no-such-point" in message, "error names the id that failed: $message")
        declared.forEach {
            assertTrue(it in message, "error lists declared point '$it' so a typo is self-diagnosing: $message")
        }
    }

    @Test
    fun `a REST id is not resolvable as a supabase point`() {
        val rest = AppAccessPoints.points.first { it.kind == AccessPointKind.REST }
        // clientFor returns null for a probe; requireClientFor (what the DI binding uses) must throw,
        // so wiring supabaseApi("<a rest id>") fails loudly at graph construction, not at first call.
        assertNull(factory().clientFor(rest.id))
        assertFailsWith<IllegalStateException> { factory().requireClientFor(rest.id) }
    }

    @Test
    fun `the generated ServerInstanceApi returns empty instead of failing when unconfigured`() = runTest {
        val point = AccessPointRegistry(unconfiguredPoints).supabasePoints().first()
        // Typed as the INTERFACE, constructed as the IMPL — the same shape the generated binding uses
        // (`supabaseApi<ServerInstanceApi>("<id>") { ServerInstanceApiImpl(it) }`), so this asserts against
        // the contract a consumer injects rather than against the implementation.
        val api: ServerInstanceApi = ServerInstanceApiImpl(factory().requireClientFor(point.id))
        assertFalse(api.isConfigured)
        // The guard matters: without it this would reach `client`, build a Supabase client on an empty
        // key, and fail at app start-up on every unconfigured fork.
        assertTrue(api.fetchInstances("field-officer").isEmpty(), "an unconfigured client reads nothing")
    }
}
