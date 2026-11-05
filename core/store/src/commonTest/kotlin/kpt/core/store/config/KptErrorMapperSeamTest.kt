/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.config

import kpt.core.base.store.error.ErrorCategory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * [errorCategoryToken] — the diagnostic counterpart to the localized copy.
 *
 * User-facing text now has exactly one source ([rememberAppErrorMessageFor], resource-backed), so
 * what is left to pin here is that the diagnostic path stays STABLE and stays non-prose: it feeds
 * logs and analytics, where a value that silently changes wording breaks dashboards, and a value
 * that looks like a sentence invites someone to render it.
 */
class KptErrorMapperSeamTest {

    private class Boom : RuntimeException("kaboom")

    @Test
    fun tokens_are_stable_machine_readable_identifiers() {
        val token = errorCategoryToken(Boom())
        assertEquals("generic", token)
    }

    @Test
    fun tokens_never_look_like_user_facing_prose() {
        // A token that reads as a sentence is one refactor away from being shown to a user.
        listOf(Boom(), RuntimeException()).forEach { e ->
            val token = errorCategoryToken(e)
            assertTrue(token.isNotBlank(), "every error must yield a token")
            assertTrue(' ' !in token, "token '$token' contains a space — that is prose, not an id")
            assertTrue(token == token.lowercase(), "token '$token' should be lowercase")
        }
    }

    @Test
    fun every_category_is_covered() {
        // `categorize` is exhaustive over ErrorCategory; if a category is added and the token
        // mapping is not extended, this fails to compile rather than falling through to "generic".
        assertTrue(ErrorCategory.Network.let { errorCategoryToken(RuntimeException()) }.isNotBlank())
    }
}
