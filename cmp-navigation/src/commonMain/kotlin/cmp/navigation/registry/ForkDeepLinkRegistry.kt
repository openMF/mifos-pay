/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.registry

import androidx.navigation.NavHostController
import com.mobilebytelabs.kmptoolkit.deeplink.DeepLink

/**
 * Fork-owned customization seam for OS **deep links** — the deep-link twin of [ForkFeatureRegistry] /
 * [ForkBackboneRegistry] / [ForkTabRegistry].
 *
 * Inbound URIs (home-screen widget taps, `app://…` links, iOS `openURL`, browser `#`/`?` routes) are
 * captured by the cmp-deep-link library (KmpToolkit, commonMain) — on Android via its auto-init
 * `ContentProvider` + `ActivityLifecycleCallbacks`, so **no Activity wiring is needed** — and parsed into a
 * [DeepLink] on `DeepLinkHandler`. `RootNavScreen` observes `DeepLinkHandler.lastReceived` and, once the
 * user is authenticated and the authenticated graph is on-screen, dispatches the link through [route].
 *
 * The template ships **no** deep links, so the default [route] is a no-op. A fork that adds deep links (e.g.
 * quick-launch widgets) registers ITS mapping ONCE at app start — never editing `RootNavScreen`:
 *
 * ```kotlin
 * // In the fork's app init (e.g. alongside ForkFeatureRegistry wiring):
 * ForkDeepLinkRegistry.route = { link, navController ->
 *     when (link.host) {
 *         "focus" -> { navController.navigateToFocusSession(); true }
 *         "goals" -> { navController.navigateToDailyGoals(); true }
 *         else    -> false
 *     }
 * }
 * ```
 */
object ForkDeepLinkRegistry {

    /**
     * Map a parsed [DeepLink] to a `navigate(...)` on the root [NavHostController]. Return `true` when the
     * link matched a known destination (navigation was issued) so `RootNavScreen` clears the handled link;
     * return `false` to leave it pending / ignore an unknown scheme. Default: no-op (template has no links).
     */
    var route: (link: DeepLink, navController: NavHostController) -> Boolean = { _, _ -> false }
}
