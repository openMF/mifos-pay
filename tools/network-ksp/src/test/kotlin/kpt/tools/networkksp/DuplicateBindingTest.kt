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
            listOf(
                ApiBindingKey("mifos", "a.ClientApi", qualified = false),
                ApiBindingKey("mifos", "a.LoanApi", qualified = false),
                ApiBindingKey("mifos", "a.SavingsApi", qualified = false),
            ),
        )
        assertTrue(errors.isEmpty(), "different types on one point must not conflict: $errors")
    }

    @Test
    fun sameTypeTwiceOnOnePointIsAnError() {
        val errors = duplicateBindingErrors(
            listOf(
                ApiBindingKey("mifos", "a.ClientApi", qualified = false),
                ApiBindingKey("mifos", "a.ClientApi", qualified = false),
            ),
        )
        assertEquals(1, errors.size)
        assertTrue("a.ClientApi" in errors.single() && "2 times" in errors.single(), errors.single())
    }

    @Test
    fun sameTypeUnqualifiedOnTwoPointsIsAnError() {
        // Two separate annotations each claiming the default (unqualified) binding: one key, two definitions.
        val errors = duplicateBindingErrors(
            listOf(
                ApiBindingKey("rest", "a.ClientApi", qualified = false),
                ApiBindingKey("backup", "a.ClientApi", qualified = false),
            ),
        )
        assertEquals(1, errors.size)
        assertTrue("rest" in errors.single() && "backup" in errors.single(), errors.single())
    }

    @Test
    fun oneTypeOnSeveralPointsViaOneMultiPointBindingIsValid() {
        // @ApiBinding("main", "selfservice"): the first point is the default, the others are qualified by id.
        val errors = duplicateBindingErrors(
            listOf(
                ApiBindingKey("main", "a.ClientApi", qualified = false),
                ApiBindingKey("selfservice", "a.ClientApi", qualified = true),
            ),
        )
        assertTrue(errors.isEmpty(), "a qualified extra point is its own Koin key: $errors")
    }

    @Test
    fun oneBindingPerTypeIsValid() {
        assertTrue(
            duplicateBindingErrors(
                listOf(ApiBindingKey("a", "x.A", qualified = false), ApiBindingKey("b", "x.B", qualified = false)),
            ).isEmpty(),
        )
    }
}
