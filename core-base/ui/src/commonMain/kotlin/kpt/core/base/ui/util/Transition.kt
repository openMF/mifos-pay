/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.ui.util

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavBackStackEntry
import kpt.core.base.designsystem.theme.Motion
import kpt.core.base.ui.motion.KptFadeThrough
import kpt.core.base.ui.motion.KptSharedAxis
import kotlin.jvm.JvmSuppressWildcards

/**
 * Function type for providing nullable enter transitions in navigation.
 * Used with [AnimatedContentTransitionScope] to define how a destination enters the screen.
 */
typealias EnterTransitionProvider =
    (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)

/**
 * Function type for providing nullable exit transitions in navigation.
 * Used with [AnimatedContentTransitionScope] to define how a destination exits the screen.
 */
typealias ExitTransitionProvider =
    (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)

/**
 * Function type for providing non-null enter transitions in navigation.
 * Used with [AnimatedContentTransitionScope] to define how a destination enters the screen.
 */
typealias NonNullEnterTransitionProvider =
    (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition)

/**
 * Function type for providing non-null exit transitions in navigation.
 * Used with [AnimatedContentTransitionScope] to define how a destination exits the screen.
 */
typealias NonNullExitTransitionProvider =
    (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition)

/**
 * The default transition time (in milliseconds) for all fade transitions in the
 * [TransitionProviders].
 */
const val DEFAULT_FADE_TRANSITION_TIME_MS: Int = 300

/**
 * The default transition time (in milliseconds) for all slide transitions in the
 * [TransitionProviders].
 */
const val DEFAULT_SLIDE_TRANSITION_TIME_MS: Int = 450

/**
 * The default transition time (in milliseconds) for all slide transitions in the
 * [TransitionProviders].
 */
const val DEFAULT_PUSH_TRANSITION_TIME_MS: Int = 350

/**
 * The default transition time (in milliseconds) for all "stay"/no-op transitions in the
 * [TransitionProviders].
 *
 * This should be at least as large as any other transition that might also be happening during a
 * navigation.
 */
val DEFAULT_STAY_TRANSITION_TIME_MS: Int =
    maxOf(
        DEFAULT_FADE_TRANSITION_TIME_MS,
        DEFAULT_SLIDE_TRANSITION_TIME_MS,
        DEFAULT_PUSH_TRANSITION_TIME_MS,
    )

/**
 * Checks if the parent of the destination before and after the navigation is the same. This is
 * useful to ignore certain enter/exit transitions when navigating between distinct, nested flows.
 */
val AnimatedContentTransitionScope<NavBackStackEntry>.isSameGraphNavigation: Boolean
    get() = initialState.destination.parent == targetState.destination.parent

/**
 * Contains standard "transition providers" that may be used to specify the [EnterTransition] and
 * [ExitTransition] used when building a typical composable destination. These may return `null`
 * values in order to allow transitions between nested navigation graphs to be specified by
 * components higher up in the graph.
 */
object TransitionProviders {
    /**
     * The standard set of "enter" transition providers.
     */
    object Enter {
        /**
         * Fades the new screen in.
         *
         * Note that this represents a `null` transition when navigating between different nested
         * navigation graphs.
         */
        val fadeIn: EnterTransitionProvider = {
            RootTransitionProviders.Enter
                .fadeIn(this)
                .takeIf { isSameGraphNavigation }
        }

        /**
         * Slides the new screen in from the left of the screen.
         */
        val pushLeft: EnterTransitionProvider = {
            RootTransitionProviders
                .Enter
                .pushLeft(this)
                .takeIf { isSameGraphNavigation }
        }

        /**
         * Slides the new screen in from the right of the screen.
         */
        val pushRight: EnterTransitionProvider = {
            RootTransitionProviders
                .Enter
                .pushRight(this)
                .takeIf { isSameGraphNavigation }
        }

        /**
         * Slides the new screen in from the bottom of the screen.
         *
         * Note that this represents a `null` transition when navigating between different nested
         * navigation graphs.
         */
        val slideUp: EnterTransitionProvider = {
            RootTransitionProviders
                .Enter
                .slideUp(this)
                .takeIf { isSameGraphNavigation }
        }

        /**
         * A "no-op" transition: this changes nothing about the screen but "lasts" as long as
         * other standard transitions in order to leave the screen in place such that it does not
         * immediately appear while the other screen transitions away.
         *
         * Note that this represents a `null` transition when navigating between different nested
         * navigation graphs.
         */
        val stay: EnterTransitionProvider = {
            RootTransitionProviders
                .Enter
                .stay(this)
                .takeIf { isSameGraphNavigation }
        }
    }

    /**
     * The standard set of "exit" transition providers.
     */
    object Exit {
        /**
         * Fades the current screen out.
         *
         * Note that this represents a `null` transition when navigating between different nested
         * navigation graphs.
         */
        val fadeOut: ExitTransitionProvider = {
            RootTransitionProviders
                .Exit
                .fadeOut(this)
                .takeIf { isSameGraphNavigation }
        }

        /**
         * Slides the current screen out to the left of the screen.
         */
        val pushLeft: ExitTransitionProvider = {
            RootTransitionProviders
                .Exit
                .pushLeft(this)
                .takeIf { isSameGraphNavigation }
        }

        /**
         * Slides the current screen out to the right of the screen.
         */
        val pushRight: ExitTransitionProvider = {
            RootTransitionProviders
                .Exit
                .pushRight(this)
                .takeIf { isSameGraphNavigation }
        }

        /**
         * Slides the current screen down to the bottom of the screen.
         *
         * Note that this represents a `null` transition when navigating between different nested
         * navigation graphs.
         */
        val slideDown: ExitTransitionProvider = {
            RootTransitionProviders
                .Exit
                .slideDown(this)
                .takeIf { isSameGraphNavigation }
        }

        /**
         * A "no-op" transition: this changes nothing about the screen but "lasts" as long as
         * other standard transitions in order to leave the screen in place such that it does not
         * immediately disappear while the other screen transitions into place.
         *
         * Note that this represents a `null` transition when navigating between different nested
         * navigation graphs.
         */
        val stay: ExitTransitionProvider = {
            RootTransitionProviders
                .Exit
                .stay(this)
                .takeIf { isSameGraphNavigation }
        }
    }

    /**
     * Kpt transition vocabulary aligned with the M3 motion spec
     * (https://m3.material.io/styles/motion/transitions/transition-patterns). These are
     * **semantic aliases** of the existing [Enter] / [Exit] providers — same animation specs,
     * but named after the motion pattern they implement rather than the direction of travel.
     *
     *  - [sharedAxisForward] — pushing a new screen onto the stack.
     *  - [sharedAxisBack] — popping back to the previous screen.
     *  - [fadeThrough] — sibling navigation (bottom-nav tab switch, paged sub-section switch).
     *
     * Like the parent providers, these return `null` for cross-graph navigation so the outer
     * navigator's transition can take over (see [isSameGraphNavigation]).
     *
     * **Theme-token injection (follow-up):** these still capture hardcoded durations from
     * [DEFAULT_FADE_TRANSITION_TIME_MS] / [DEFAULT_PUSH_TRANSITION_TIME_MS]. Wiring
     * `MaterialTheme.motion` through them requires moving the `Motion` data class from
     * `core/designsystem/theme/Motion.kt` down to `core-base/designsystem` (today blocked by
     * dependency direction) and lifting the providers from `val` to `fun(motion: Motion)`.
     */
    object Kpt {
        /** Enter transitions for a destination arriving on screen. */
        object Enter {
            /** Hardcoded aliases — kept for callers that don't have a `Motion` in scope. */
            val sharedAxisForward: EnterTransitionProvider = TransitionProviders.Enter.pushLeft

            /** Back shared-axis — the mirror of the forward move, for returning. */
            val sharedAxisBack: EnterTransitionProvider = TransitionProviders.Enter.pushRight

            /** Fade-through — a lateral move between peers with no spatial relationship. */
            val fadeThrough: EnterTransitionProvider = TransitionProviders.Enter.fadeIn

            /** Slide-up — a modal-style arrival. */
            val slideUp: EnterTransitionProvider = TransitionProviders.Enter.slideUp

            /**
             * Hold this side still while the other animates. Holds 0.99 alpha so Compose cannot optimise the
             * transition away, and runs for the LONGEST concurrent duration so the held side does not blink while its
             * counterpart is still moving.
             */
            val stay: EnterTransitionProvider = TransitionProviders.Enter.stay

            /**
             * Theme-aware variants. Consume tokens from [Motion]. Caller resolves
             * `MaterialTheme.motion` at a `@Composable` site and passes it in — the
             * returned provider snapshots the values and is safe to pass into the
             * non-`@Composable` `enterTransition = ...` lambda.
             */
            fun sharedAxisForward(motion: Motion): EnterTransitionProvider = {
                KptSharedAxis.enterForward(motion).takeIf { isSameGraphNavigation }
            }

            /**
             * Back shared-axis — the mirror of the forward move, for returning. Theme-aware: takes its duration and
             * easing from [Motion].
             */
            fun sharedAxisBack(motion: Motion): EnterTransitionProvider = {
                KptSharedAxis.enterBack(motion).takeIf { isSameGraphNavigation }
            }

            /**
             * Fade-through — a lateral move between peers with no spatial relationship. Theme-aware: takes its
             * duration and easing from [Motion].
             */
            fun fadeThrough(motion: Motion): EnterTransitionProvider = {
                KptFadeThrough.enter(motion).takeIf { isSameGraphNavigation }
            }

            /** Slide-up — a modal-style arrival. Theme-aware: takes its duration and easing from [Motion]. */
            fun slideUp(motion: Motion): EnterTransitionProvider = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(motion.durationLong1, easing = motion.easingEmphasized),
                ).takeIf { isSameGraphNavigation }
            }
            // "stay" holds 0.99 alpha so Compose doesn't optimize it away — duration must
            // match the longest concurrent transition so the held side doesn't blink in/out
            // while the other side animates.
            /**
             * Hold this side still while the other animates. Holds 0.99 alpha so Compose cannot optimise the
             * transition away, and runs for the LONGEST concurrent duration so the held side does not blink while its
             * counterpart is still moving. Theme-aware: takes its duration and easing from [Motion].
             */
            fun stay(motion: Motion): EnterTransitionProvider = {
                fadeIn(
                    animationSpec = tween(motion.durationLong1),
                    initialAlpha = 1f,
                ).takeIf { isSameGraphNavigation }
            }
        }

        /** Exit transitions for a destination leaving the screen. */
        object Exit {
            /** Forward shared-axis — the motion for going DEEPER into a flow. */
            val sharedAxisForward: ExitTransitionProvider = TransitionProviders.Exit.pushLeft

            /** Back shared-axis — the mirror of the forward move, for returning. */
            val sharedAxisBack: ExitTransitionProvider = TransitionProviders.Exit.pushRight

            /** Fade-through — a lateral move between peers with no spatial relationship. */
            val fadeThrough: ExitTransitionProvider = TransitionProviders.Exit.fadeOut

            /** Slide-down — paired with the slide-up enter. */
            val slideDown: ExitTransitionProvider = TransitionProviders.Exit.slideDown

            /**
             * Hold this side still while the other animates. Holds 0.99 alpha so Compose cannot optimise the
             * transition away, and runs for the LONGEST concurrent duration so the held side does not blink while its
             * counterpart is still moving.
             */
            val stay: ExitTransitionProvider = TransitionProviders.Exit.stay

            /**
             * Forward shared-axis — the motion for going DEEPER into a flow. Theme-aware: takes its duration and
             * easing from [Motion].
             */
            fun sharedAxisForward(motion: Motion): ExitTransitionProvider = {
                KptSharedAxis.exitForward(motion).takeIf { isSameGraphNavigation }
            }

            /**
             * Back shared-axis — the mirror of the forward move, for returning. Theme-aware: takes its duration and
             * easing from [Motion].
             */
            fun sharedAxisBack(motion: Motion): ExitTransitionProvider = {
                KptSharedAxis.exitBack(motion).takeIf { isSameGraphNavigation }
            }

            /**
             * Fade-through — a lateral move between peers with no spatial relationship. Theme-aware: takes its
             * duration and easing from [Motion].
             */
            fun fadeThrough(motion: Motion): ExitTransitionProvider = {
                KptFadeThrough.exit(motion).takeIf { isSameGraphNavigation }
            }

            /**
             * Slide-down — paired with the slide-up enter. Theme-aware: takes its duration and easing from [Motion].
             */
            fun slideDown(motion: Motion): ExitTransitionProvider = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(motion.durationLong1, easing = motion.easingEmphasized),
                ).takeIf { isSameGraphNavigation }
            }

            /**
             * Hold this side still while the other animates. Holds 0.99 alpha so Compose cannot optimise the
             * transition away, and runs for the LONGEST concurrent duration so the held side does not blink while its
             * counterpart is still moving. Theme-aware: takes its duration and easing from [Motion].
             */
            fun stay(motion: Motion): ExitTransitionProvider = {
                fadeOut(
                    animationSpec = tween(motion.durationLong1),
                    targetAlpha = 0.99f,
                ).takeIf { isSameGraphNavigation }
            }
        }
    }
}

/**
 * Contains standard "transition providers" that may be used to specify the [EnterTransition] and
 * [ExitTransition] used when building a root [NavHost], which requires a non-null value.
 */
object RootTransitionProviders {
    /**
     * The standard set of "enter" transition providers.
     */
    object Enter {
        /**
         * Fades the new screen in.
         */
        val fadeIn: NonNullEnterTransitionProvider = {
            fadeIn(tween(DEFAULT_FADE_TRANSITION_TIME_MS))
        }

        /**
         * There is no transition for the entering screen.
         */
        val none: NonNullEnterTransitionProvider = {
            EnterTransition.None
        }

        /**
         * Slides the new screen in from the left of the screen.
         */
        val pushLeft: NonNullEnterTransitionProvider = {
            slideInHorizontally(
                animationSpec = tween(
                    durationMillis = TransitionPushSpec.enterSlideDurationMs(),
                    delayMillis = TransitionPushSpec.enterSlideDelayMs(),
                ),
                initialOffsetX = { fullWidth -> fullWidth / 2 },
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = TransitionPushSpec.enterFadeDurationMs(),
                    delayMillis = TransitionPushSpec.enterFadeDelayMs(),
                ),
            )
        }

        /**
         * Slides the new screen in from the right of the screen.
         */
        val pushRight: NonNullEnterTransitionProvider = {
            slideInHorizontally(
                animationSpec = tween(
                    durationMillis = TransitionPushSpec.enterSlideDurationMs(),
                    delayMillis = TransitionPushSpec.enterSlideDelayMs(),
                ),
                initialOffsetX = { fullWidth -> -fullWidth / 2 },
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = TransitionPushSpec.enterFadeDurationMs(),
                    delayMillis = TransitionPushSpec.enterFadeDelayMs(),
                ),
            )
        }

        /**
         * Slides the new screen in from the bottom of the screen.
         */
        val slideUp: NonNullEnterTransitionProvider = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Up,
                animationSpec = tween(DEFAULT_SLIDE_TRANSITION_TIME_MS),
            )
        }

        /**
         * A "no-op" transition: this changes nothing about the screen but "lasts" as long as
         * other standard transitions in order to leave the screen in place such that it does not
         * immediately appear while the other screen transitions away.
         */
        val stay: NonNullEnterTransitionProvider = {
            fadeIn(
                animationSpec = tween(DEFAULT_STAY_TRANSITION_TIME_MS),
                initialAlpha = 1f,
            )
        }
    }

    /**
     * The standard set of "exit" transition providers.
     */
    object Exit {
        /**
         * Fades the current screen out.
         */
        val fadeOut: NonNullExitTransitionProvider = {
            fadeOut(tween(DEFAULT_FADE_TRANSITION_TIME_MS))
        }

        /**
         * There is no transition for the exiting screen.
         *
         * Unlike the [stay] transition, this will immediately remove the outgoing screen even if
         * there is an ongoing enter transition happening for the new screen.
         */
        val none: NonNullExitTransitionProvider = {
            ExitTransition.None
        }

        /**
         * Slides the current screen out to the left of the screen.
         */
        val pushLeft: NonNullExitTransitionProvider = {
            slideOutHorizontally(
                animationSpec = tween(
                    durationMillis = TransitionPushSpec.exitSlideDurationMs(),
                    delayMillis = TransitionPushSpec.exitSlideDelayMs(),
                ),
                targetOffsetX = { fullWidth -> -fullWidth / 2 },
            ) + fadeOut(
                animationSpec = tween(
                    durationMillis = TransitionPushSpec.exitFadeDurationMs(),
                    delayMillis = TransitionPushSpec.exitFadeDelayMs(),
                ),
            )
        }

        /**
         * Slides the current screen out to the right of the screen.
         */
        val pushRight: NonNullExitTransitionProvider = {
            slideOutHorizontally(
                animationSpec = tween(
                    durationMillis = TransitionPushSpec.exitSlideDurationMs(),
                    delayMillis = TransitionPushSpec.exitSlideDelayMs(),
                ),
                targetOffsetX = { fullWidth -> fullWidth / 2 },
            ) + fadeOut(
                animationSpec = tween(
                    durationMillis = TransitionPushSpec.exitFadeDurationMs(),
                    delayMillis = TransitionPushSpec.exitFadeDelayMs(),
                ),
            )
        }

        /**
         * Slides the current screen down to the bottom of the screen.
         */
        val slideDown: NonNullExitTransitionProvider = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Down,
                animationSpec = tween(DEFAULT_SLIDE_TRANSITION_TIME_MS),
            )
        }

        /**
         * A "no-op" transition: this changes nothing about the screen but "lasts" as long as
         * other standard transitions in order to leave the screen in place such that it does not
         * immediately disappear while the other screen transitions into place.
         */
        val stay: NonNullExitTransitionProvider = {
            fadeOut(
                animationSpec = tween(DEFAULT_STAY_TRANSITION_TIME_MS),
                targetAlpha = 0.99f,
            )
        }
    }

    /**
     * Kpt transition vocabulary for **root [NavHost]** wiring (non-null providers).
     *
     * Companion to [TransitionProviders.Kpt] — same motion patterns, but always non-null since
     * the root [NavHost] requires every transition slot filled.
     *
     *  - [sharedAxisForward] — pushing a new screen onto the stack.
     *  - [sharedAxisBack] — popping back.
     *  - [fadeThrough] — sibling navigation (used by `cmp-navigation/authenticatednavbar` for
     *    bottom-nav tab switches).
     *  - [none] — instant, no animation (splash → first authenticated screen handoff).
     *  - [stay] — no-op holding the screen visible while the other side transitions.
     *
     * See [TransitionProviders.Kpt] for the wider rationale + the theme-token follow-up note.
     */
    object Kpt {
        /** Enter transitions for a ROOT-level destination — non-null, so the graph always has one. */
        object Enter {
            /** Hardcoded aliases — kept for callers that don't have a `Motion` in scope. */
            val sharedAxisForward: NonNullEnterTransitionProvider = RootTransitionProviders.Enter.pushLeft

            /** Back shared-axis — the mirror of the forward move, for returning. */
            val sharedAxisBack: NonNullEnterTransitionProvider = RootTransitionProviders.Enter.pushRight

            /** Fade-through — a lateral move between peers with no spatial relationship. */
            val fadeThrough: NonNullEnterTransitionProvider = RootTransitionProviders.Enter.fadeIn

            /** Slide-up — a modal-style arrival. */
            val slideUp: NonNullEnterTransitionProvider = RootTransitionProviders.Enter.slideUp

            /** No transition at all — an instant cut. */
            val none: NonNullEnterTransitionProvider = RootTransitionProviders.Enter.none

            /**
             * Hold this side still while the other animates. Holds 0.99 alpha so Compose cannot optimise the
             * transition away, and runs for the LONGEST concurrent duration so the held side does not blink while its
             * counterpart is still moving.
             */
            val stay: NonNullEnterTransitionProvider = RootTransitionProviders.Enter.stay

            /**
             * Theme-aware variants. Caller resolves `MaterialTheme.motion` at a
             * `@Composable` site and passes it; the returned provider snapshots the
             * values. Use these whenever theme tokens should drive the transition.
             */
            fun sharedAxisForward(motion: Motion): NonNullEnterTransitionProvider = {
                KptSharedAxis.enterForward(motion)
            }

            /**
             * Back shared-axis — the mirror of the forward move, for returning. Theme-aware: takes its duration and
             * easing from [Motion].
             */
            fun sharedAxisBack(motion: Motion): NonNullEnterTransitionProvider = {
                KptSharedAxis.enterBack(motion)
            }

            /**
             * Fade-through — a lateral move between peers with no spatial relationship. Theme-aware: takes its
             * duration and easing from [Motion].
             */
            fun fadeThrough(motion: Motion): NonNullEnterTransitionProvider = {
                KptFadeThrough.enter(motion)
            }

            /** Slide-up — a modal-style arrival. Theme-aware: takes its duration and easing from [Motion]. */
            fun slideUp(motion: Motion): NonNullEnterTransitionProvider = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(motion.durationLong1, easing = motion.easingEmphasized),
                )
            }

            /**
             * Hold this side still while the other animates. Holds 0.99 alpha so Compose cannot optimise the
             * transition away, and runs for the LONGEST concurrent duration so the held side does not blink while its
             * counterpart is still moving. Theme-aware: takes its duration and easing from [Motion].
             */
            fun stay(motion: Motion): NonNullEnterTransitionProvider = {
                fadeIn(
                    animationSpec = tween(motion.durationLong1),
                    initialAlpha = 1f,
                )
            }
        }

        /** Exit transitions for a ROOT-level destination — non-null, so the graph always has one. */
        object Exit {
            /** Forward shared-axis — the motion for going DEEPER into a flow. */
            val sharedAxisForward: NonNullExitTransitionProvider = RootTransitionProviders.Exit.pushLeft

            /** Back shared-axis — the mirror of the forward move, for returning. */
            val sharedAxisBack: NonNullExitTransitionProvider = RootTransitionProviders.Exit.pushRight

            /** Fade-through — a lateral move between peers with no spatial relationship. */
            val fadeThrough: NonNullExitTransitionProvider = RootTransitionProviders.Exit.fadeOut

            /** Slide-down — paired with the slide-up enter. */
            val slideDown: NonNullExitTransitionProvider = RootTransitionProviders.Exit.slideDown

            /** No transition at all — an instant cut. */
            val none: NonNullExitTransitionProvider = RootTransitionProviders.Exit.none

            /**
             * Hold this side still while the other animates. Holds 0.99 alpha so Compose cannot optimise the
             * transition away, and runs for the LONGEST concurrent duration so the held side does not blink while its
             * counterpart is still moving.
             */
            val stay: NonNullExitTransitionProvider = RootTransitionProviders.Exit.stay

            /**
             * Forward shared-axis — the motion for going DEEPER into a flow. Theme-aware: takes its duration and
             * easing from [Motion].
             */
            fun sharedAxisForward(motion: Motion): NonNullExitTransitionProvider = {
                KptSharedAxis.exitForward(motion)
            }

            /**
             * Back shared-axis — the mirror of the forward move, for returning. Theme-aware: takes its duration and
             * easing from [Motion].
             */
            fun sharedAxisBack(motion: Motion): NonNullExitTransitionProvider = {
                KptSharedAxis.exitBack(motion)
            }

            /**
             * Fade-through — a lateral move between peers with no spatial relationship. Theme-aware: takes its
             * duration and easing from [Motion].
             */
            fun fadeThrough(motion: Motion): NonNullExitTransitionProvider = {
                KptFadeThrough.exit(motion)
            }

            /**
             * Slide-down — paired with the slide-up enter. Theme-aware: takes its duration and easing from [Motion].
             */
            fun slideDown(motion: Motion): NonNullExitTransitionProvider = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(motion.durationLong1, easing = motion.easingEmphasized),
                )
            }

            /**
             * Hold this side still while the other animates. Holds 0.99 alpha so Compose cannot optimise the
             * transition away, and runs for the LONGEST concurrent duration so the held side does not blink while its
             * counterpart is still moving. Theme-aware: takes its duration and easing from [Motion].
             */
            fun stay(motion: Motion): NonNullExitTransitionProvider = {
                fadeOut(
                    animationSpec = tween(motion.durationLong1),
                    targetAlpha = 0.99f,
                )
            }
        }
    }
}
