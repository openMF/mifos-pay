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

import kpt.core.base.ui.screen.ScreenStateDefaults
import kpt.core.base.ui.screen.ScreenStateEmpty
import kpt.core.base.ui.screen.ScreenStateError
import kpt.core.base.ui.screen.ScreenStateLoading
import kpt.core.base.ui.screen.ScreenStateNoNetwork
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

/**
 * The two fork seams are NEUTRAL on the template.
 *
 * Both exist so a fork customizes without editing a template-owned file, which only works if the
 * un-customized template inherits the framework behaviour untouched. `ForkErrorMapper.message`
 * returning anything but null would shadow all nine categorised error messages;
 * `ForkScreenStateDefaults.customize` returning anything but its argument would silently replace
 * the framework's visuals for every fork that never asked for it.
 *
 * The neutral behaviour comes from the DEFAULT bodies on the template-owned
 * [ErrorMessageOverrides] / [ScreenStateOverrides] interfaces, which the fork-owned objects
 * inherit by declaring no override. That is what lets the template add a hook later without
 * breaking a fork on the sync that delivers it.
 *
 * NOTE what this does NOT cover: that `config/KptErrorMapper` and `config/KptScreenStateDefaults`
 * actually CALL these. Both call sites sit inside `@Composable` functions, and `core/store` does not
 * carry the Compose ui-test dependency (it comes from CMPFeatureConventionPlugin, which only the
 * feature modules apply). Deleting either call would leave every test here green, so the wiring is
 * guarded by `store-fork-seam-wiring.sh` (FS-1/FS-2) instead.
 */
class KptSeamContractTest {

    @Test
    fun project_error_message_declines_by_default() {
        // Declining is what lets the framework's categorised copy through.
        assertNull(ForkErrorMapper.message(RuntimeException("boom")))
        assertNull(ForkErrorMapper.message(IllegalStateException()))
    }

    @Test
    fun project_screen_state_overrides_are_identity_by_default() {
        val defaults = ScreenStateDefaults(
            loading = ScreenStateLoading.Skeleton(rowCount = 5),
            empty = ScreenStateEmpty(title = "t", message = "m"),
            error = ScreenStateError(title = "e", retryText = "r"),
            noNetwork = ScreenStateNoNetwork(message = "n", retryText = "r"),
        )
        // Same instance, not merely an equal copy: the template must not rebuild what it was handed.
        assertSame(defaults, ForkScreenStateDefaults.customize(defaults))
    }

    @Test
    fun a_fork_implementation_overrides_the_default() {
        // The extension contract itself: implement the template-owned interface, override one member,
        // inherit the other defaults. This is what a fork writes in its own copy of the seam files.
        val forkMapper = object : ErrorMessageOverrides {
            override fun message(error: Throwable): String? =
                if (error is IllegalArgumentException) "fork copy" else null
        }
        assertEquals("fork copy", forkMapper.message(IllegalArgumentException()))
        assertNull(forkMapper.message(RuntimeException()), "unhandled errors still fall through")

        val forkVisuals = object : ScreenStateOverrides {}
        val defaults = ScreenStateDefaults(
            loading = ScreenStateLoading.Skeleton(rowCount = 5),
            empty = ScreenStateEmpty(title = "t", message = "m"),
            error = ScreenStateError(title = "e", retryText = "r"),
            noNetwork = ScreenStateNoNetwork(message = "n", retryText = "r"),
        )
        // Declaring NO override inherits the identity default — the compile-compatibility guarantee.
        assertSame(defaults, forkVisuals.customize(defaults))
    }

    @Test
    fun overriding_one_field_inherits_the_rest() {
        // The inheritance contract a fork relies on — `copy` one thing, keep everything else.
        val defaults = ScreenStateDefaults(
            loading = ScreenStateLoading.Skeleton(rowCount = 5),
            empty = ScreenStateEmpty(title = "framework-empty", message = "m"),
            error = ScreenStateError(title = "framework-error", retryText = "r"),
            noNetwork = ScreenStateNoNetwork(message = "n", retryText = "r"),
        )
        val branded = defaults.copy(empty = defaults.empty.copy(title = "fork-empty"))

        assertEquals("fork-empty", branded.empty.title, "the fork's override must win")
        assertEquals("framework-error", branded.error.title, "everything else must be inherited")
        assertEquals("m", branded.empty.message, "untouched fields of an overridden block survive")
    }
}
