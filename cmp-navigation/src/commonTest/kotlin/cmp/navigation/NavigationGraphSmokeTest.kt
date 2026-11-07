/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation

import cmp.navigation.authenticated.AuthenticatedGraphRoute
import cmp.navigation.authenticatednavbar.AuthenticatedNavbarRoute
import cmp.navigation.splash.SplashRoute
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.serializer
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * End-to-end smoke test for the outer app-shell navigation graph.
 *
 * Catches the bug class where someone deletes / renames / mis-wires a route in
 * `:cmp-navigation` without a compile-time signal. The test exercises the
 * template-shell outer routes only — fork feature destinations live in
 * `:cmp-shared/.../ForkNavHost.kt` and are covered by fork-side tests.
 *
 * Phase 2 nav-chunk (skeleton): the template-demo route universe
 * (`kpt.feature.{bills,calculators,crypto,currencyrates,emicalculator,loans,
 * macro,rates}.*`) that the original template smoke-test iterated over was
 * stripped — those modules don't exist in the fork's classpath. The test now
 * covers the three template outer routes that survived the demo-strip. As
 * per-feature typed-route conversion progresses (see TODOs in
 * `authenticated/AuthenticatedNavigation.kt`), each converted route gets added
 * to the [parameterlessRoutes] list below.
 */
class NavigationGraphSmokeTest {

    /**
     * Parameterless routes (data object) that every consumer references by
     * type. Adding a new top-level typed route → add it here.
     */
    private val parameterlessRoutes: List<Any> = listOf(
        SplashRoute,
        AuthenticatedGraphRoute,
        AuthenticatedNavbarRoute,
    )

    @OptIn(InternalSerializationApi::class)
    @Test
    fun `every parameterless route is @Serializable`() {
        parameterlessRoutes.forEach { route ->
            // Fails fast at runtime if @Serializable is missing on any route.
            assertNotNull(
                route::class.serializer(),
                "Route ${route::class.simpleName} is missing @Serializable",
            )
        }
    }
}
