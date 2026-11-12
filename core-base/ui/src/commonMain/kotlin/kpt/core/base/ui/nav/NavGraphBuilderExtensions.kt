/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.ui.nav

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import kpt.core.base.designsystem.theme.Motion
import kpt.core.base.designsystem.theme.MotionSnapshot
import kpt.core.base.ui.util.RootTransitionProviders
import kotlin.jvm.JvmSuppressWildcards
import kotlin.reflect.KType

/**
 * A wrapper around [NavGraphBuilder.composable] that supplies slide up/down transitions.
 *
 * Theme-aware since 2026-05-26 — durations resolve from `motion.durationLong1`. Suitable for
 * modal-style destinations (bottom sheets, full-screen dialogs) where the new screen slides
 * up from the bottom on push and slides down on pop.
 *
 * Two overloads: the **no-arg** variant pulls motion from [MotionSnapshot.current] (updated
 * on every `MaterialTheme.motion` read upstream), so call sites stay clean. The
 * **explicit** variant takes a [Motion] parameter — useful for tests or when you need
 * deterministic transitions regardless of theme state.
 */
inline fun <reified T : Any> NavGraphBuilder.composableWithSlideTransitions(
    typeMap: Map<KType, @JvmSuppressWildcards NavType<*>> = emptyMap(),
    deepLinks: List<NavDeepLink> = emptyList(),
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) = composableWithSlideTransitions<T>(MotionSnapshot.current, typeMap, deepLinks, content)

/** Registers a destination with slide transitions — a modal-style arrival. */
inline fun <reified T : Any> NavGraphBuilder.composableWithSlideTransitions(
    motion: Motion,
    typeMap: Map<KType, @JvmSuppressWildcards NavType<*>> = emptyMap(),
    deepLinks: List<NavDeepLink> = emptyList(),
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    this.composable<T>(
        typeMap = typeMap,
        deepLinks = deepLinks,
        enterTransition = RootTransitionProviders.Kpt.Enter.slideUp(motion),
        exitTransition = RootTransitionProviders.Kpt.Exit.stay(motion),
        popEnterTransition = RootTransitionProviders.Kpt.Enter.stay(motion),
        popExitTransition = RootTransitionProviders.Kpt.Exit.slideDown(motion),
        sizeTransform = null,
        content = content,
    )
}

/**
 * A wrapper around [NavGraphBuilder.composable] that supplies "stay" transitions.
 *
 * Theme-aware since 2026-05-26 — holds the screen visible for `motion.durationLong1` while
 * the other side of the transition animates. Uses `0.99f` target alpha so Compose doesn't
 * optimize the held side away.
 *
 * Two overloads — see [composableWithSlideTransitions] for the snapshot vs. explicit
 * trade-off.
 */
inline fun <reified T : Any> NavGraphBuilder.composableWithStayTransitions(
    typeMap: Map<KType, @JvmSuppressWildcards NavType<*>> = emptyMap(),
    deepLinks: List<NavDeepLink> = emptyList(),
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) = composableWithStayTransitions<T>(MotionSnapshot.current, typeMap, deepLinks, content)

/** Registers a destination that HOLDS while its sibling animates, for a tab swap where only one side should move. */
inline fun <reified T : Any> NavGraphBuilder.composableWithStayTransitions(
    motion: Motion,
    typeMap: Map<KType, @JvmSuppressWildcards NavType<*>> = emptyMap(),
    deepLinks: List<NavDeepLink> = emptyList(),
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    this.composable<T>(
        typeMap = typeMap,
        deepLinks = deepLinks,
        // RootTransitionProviders (non-null) so the navbar container always holds at 0.99α
        // when a feature graph slides in over it — cross-graph isSameGraphNavigation guard
        // in TransitionProviders.Kpt returns null here, making the outer NavHost's
        // fade-through fire instead of stay.
        enterTransition = RootTransitionProviders.Kpt.Enter.stay(motion),
        exitTransition = RootTransitionProviders.Kpt.Exit.stay(motion),
        popEnterTransition = RootTransitionProviders.Kpt.Enter.stay(motion),
        popExitTransition = RootTransitionProviders.Kpt.Exit.stay(motion),
        sizeTransform = null,
        content = content,
    )
}

/**
 * A wrapper around [NavGraphBuilder.composable] that supplies push transitions.
 *
 * Theme-aware since 2026-05-26 — push semantics preserved (only one side moves at a time:
 * entering screen slides; exiting screen stays; popping screen slides back; revealed
 * screen stays). Forks that override the `Motion` data class (slide distance, durations,
 * easing) see those changes flow through every push-transitioning screen automatically.
 *
 * Two overloads — see [composableWithSlideTransitions] for the snapshot vs. explicit
 * trade-off.
 *
 * This is suitable for screens deeper within a hierarchy that uses push transitions; the root
 * screen of such a hierarchy should use [composableWithRootPushTransitions].
 */
inline fun <reified T : Any> NavGraphBuilder.composableWithPushTransitions(
    typeMap: Map<KType, @JvmSuppressWildcards NavType<*>> = emptyMap(),
    deepLinks: List<NavDeepLink> = emptyList(),
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) = composableWithPushTransitions<T>(MotionSnapshot.current, typeMap, deepLinks, content)

/** Registers a destination with push transitions — the default for moving deeper into a flow. */
inline fun <reified T : Any> NavGraphBuilder.composableWithPushTransitions(
    motion: Motion,
    typeMap: Map<KType, @JvmSuppressWildcards NavType<*>> = emptyMap(),
    deepLinks: List<NavDeepLink> = emptyList(),
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    this.composable<T>(
        typeMap = typeMap,
        deepLinks = deepLinks,
        // All four slots use RootTransitionProviders (non-null) so push animations always
        // apply regardless of graph depth. TransitionProviders.Kpt returns null for
        // cross-graph navigation (isSameGraphNavigation guard), which makes the outer
        // NavHost's fade-through default fire instead of the intended slide — producing the
        // "blink" on any feature whose nav is wrapped in a nested navigation<XxxGraphRoute>.
        enterTransition = RootTransitionProviders.Kpt.Enter.sharedAxisForward(motion),
        exitTransition = RootTransitionProviders.Kpt.Exit.stay(motion),
        popEnterTransition = RootTransitionProviders.Kpt.Enter.stay(motion),
        popExitTransition = RootTransitionProviders.Kpt.Exit.sharedAxisBack(motion),
        sizeTransform = null,
        content = content,
    )
}

/**
 * A wrapper around [NavGraphBuilder.composable] that supplies push transitions to the root screen
 * in a nested graph that uses push transitions.
 *
 * Theme-aware since 2026-05-26 — see [composableWithPushTransitions].
 */
inline fun <reified T : Any> NavGraphBuilder.composableWithRootPushTransitions(
    typeMap: Map<KType, @JvmSuppressWildcards NavType<*>> = emptyMap(),
    deepLinks: List<NavDeepLink> = emptyList(),
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) = composableWithRootPushTransitions<T>(MotionSnapshot.current, typeMap, deepLinks, content)

/** Registers a top-level destination with push transitions, for a root of the graph rather than a step inside one. */
inline fun <reified T : Any> NavGraphBuilder.composableWithRootPushTransitions(
    motion: Motion,
    typeMap: Map<KType, @JvmSuppressWildcards NavType<*>> = emptyMap(),
    deepLinks: List<NavDeepLink> = emptyList(),
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    this.composable<T>(
        typeMap = typeMap,
        deepLinks = deepLinks,
        enterTransition = RootTransitionProviders.Kpt.Enter.stay(motion),
        exitTransition = RootTransitionProviders.Kpt.Exit.sharedAxisForward(motion),
        popEnterTransition = RootTransitionProviders.Kpt.Enter.sharedAxisBack(motion),
        popExitTransition = RootTransitionProviders.Kpt.Exit.fadeThrough(motion),
        sizeTransform = null,
        content = content,
    )
}
