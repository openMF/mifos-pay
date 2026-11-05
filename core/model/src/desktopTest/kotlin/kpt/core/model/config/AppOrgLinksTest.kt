/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Locks the lookup, not the links: [AppOrgLinks] is GENERATED from app-profile, so asserting a
 * particular set would fail on every fork that declares its own.
 */
class AppOrgLinksTest {

    @Test
    fun everyDeclaredLinkIsReachableByName() {
        AppOrgLinks.all.forEach { (name, url) -> assertEquals(url, AppOrgLinks[name]) }
    }

    @Test
    fun anUndeclaredNameIsNull() {
        assertNull(AppOrgLinks["__not-a-declared-link__"])
    }
}
