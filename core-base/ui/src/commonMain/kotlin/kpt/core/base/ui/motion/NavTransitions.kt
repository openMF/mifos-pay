/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.ui.motion

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

/**
 * `NavGraphBuilder.composable<T>(...)` wrapper that pre-wires the app's shared-axis
 * transitions for **forward/back navigation** in a single stack.
 *
 * Reads `MaterialTheme.motion` at graph-build time (`@Composable` extension) so the
 * pre-resolved transitions honor the active theme tokens.
 *
 * Usage:
 * ```
 * fun NavGraphBuilder.loansGraph(nav: NavController) {
 *     sharedAxisComposable<LoanListRoute> { LoanListScreen(...) }
 *     sharedAxisComposable<LoanDetailRoute> { LoanDetailScreen(...) }
 * }
 * ```
 *
 * For broader-stroke adoption, the legacy `composableWithPushTransitions<T>()` in
 * `kpt.core.base.ui.nav.NavGraphBuilderExtensions.kt` is also theme-aware now —
 * either helper works.
 */
@Composable
inline fun <reified T : Any> NavGraphBuilder.sharedAxisComposable(
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    // Snapshot @Composable-resolved transitions at graph-build time so the non-Composable
    // enterTransition lambdas can capture them as plain data.
    val enterFwd = KptSharedAxis.enterForward()
    val exitFwd = KptSharedAxis.exitForward()
    val enterBack = KptSharedAxis.enterBack()
    val exitBack = KptSharedAxis.exitBack()
    composable<T>(
        enterTransition = { enterFwd },
        exitTransition = { exitFwd },
        popEnterTransition = { enterBack },
        popExitTransition = { exitBack },
        content = content,
    )
}

/**
 * `NavGraphBuilder.composable<T>(...)` wrapper for **sibling navigation** —
 * bottom-nav tabs, paged sub-sections. Outgoing fades to nothing before
 * incoming fades in (per [KptFadeThrough]).
 */
@Composable
inline fun <reified T : Any> NavGraphBuilder.fadeThroughComposable(
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    val enter = KptFadeThrough.enter()
    val exit = KptFadeThrough.exit()
    composable<T>(
        enterTransition = { enter },
        exitTransition = { exit },
        popEnterTransition = { enter },
        popExitTransition = { exit },
        content = content,
    )
}
