/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.tools.storeksp

import kotlin.test.Test
import kotlin.test.assertEquals

class StoreDependencyArgTest {

    @Test
    fun anUnqualifiedDependencyResolvesByType() {
        assertEquals("dao = get()", storeDependencyArg("dao", qualifier = null))
    }

    @Test
    fun aFromQualifierDependencyResolvesByName() {
        // A store fetching from the self-service endpoint of an API served by several access points
        // (`@ApiBinding("main", "selfservice")`) must get THAT binding, not the default one.
        assertEquals(
            "clientService = get(named(\"selfservice\"))",
            storeDependencyArg("clientService", qualifier = "selfservice"),
        )
    }
}
