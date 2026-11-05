/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.tools.networkksp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DuplicateBindingTest {

    @Test
    fun manyDifferentTypesOnOneAccessPointAreValid() {
        // A monolith backend: one server, one point, many resource APIs.
        val errors = duplicateBindingErrors(
            listOf("mifos" to "a.ClientApi", "mifos" to "a.LoanApi", "mifos" to "a.SavingsApi"),
        )
        assertTrue(errors.isEmpty(), "different types on one point must not conflict: $errors")
    }

    @Test
    fun sameTypeTwiceOnOnePointIsAnError() {
        val errors = duplicateBindingErrors(listOf("mifos" to "a.ClientApi", "mifos" to "a.ClientApi"))
        assertEquals(1, errors.size)
        assertTrue("a.ClientApi" in errors.single() && "2 times" in errors.single(), errors.single())
    }

    @Test
    fun sameTypeOnTwoDifferentPointsIsAnError() {
        // Keying on (point, type) would miss this: the binding is an unqualified single<T>.
        val errors = duplicateBindingErrors(listOf("rest" to "a.ClientApi", "backup" to "a.ClientApi"))
        assertEquals(1, errors.size)
        assertTrue("rest" in errors.single() && "backup" in errors.single(), errors.single())
    }

    @Test
    fun oneBindingPerTypeIsValid() {
        assertTrue(duplicateBindingErrors(listOf("a" to "x.A", "b" to "x.B")).isEmpty())
    }
}
