/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.designsystem.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Shared motion specs — durations, easings, and motion-pattern parameters.
 *
 * Values align with Material 3 motion guidance
 * (https://m3.material.io/styles/motion/easing-and-duration). All durations in milliseconds.
 *
 * Access from any Composable via [MaterialTheme.motion]. Framework transition factories under
 * `kpt.core.base.ui.motion` (`KptSharedAxis`, `KptFadeThrough`, etc.) and the
 * `RootTransitionProviders.Kpt` / `TransitionProviders.Kpt` provider namespaces consume
 * these values — changing a single token here propagates app-wide.
 *
 * Lives in `core-base/designsystem` so framework-shared transition code can read it without
 * depending on `core/designsystem` (forbidden dependency direction).
 */
@Immutable
data class Motion(
    // ── Durations ────────────────────────────────────────────────────────────
    /** 50ms — instant micro-interaction (chip press, icon toggle). */
    val durationShort1: Int = 50,
    /** 100ms — fast micro-interaction. */
    val durationShort2: Int = 100,
    /** 150ms — fast feedback (ripple, hover). */
    val durationShort3: Int = 150,
    /** 200ms — fast container-state change. */
    val durationShort4: Int = 200,
    /** 250ms — standard fade / cross-fade. */
    val durationMedium1: Int = 250,
    /** 300ms — standard container morph. */
    val durationMedium2: Int = 300,
    /** 350ms — standard slide. */
    val durationMedium3: Int = 350,
    /** 400ms — slow container morph. */
    val durationMedium4: Int = 400,
    /** 450ms — full-screen nav transition (forward push). */
    val durationLong1: Int = 450,
    /** 500ms — full-screen nav transition. */
    val durationLong2: Int = 500,
    /** 550ms — emphasized full-screen. */
    val durationLong3: Int = 550,
    /** 600ms — extra-long full-screen. */
    val durationLong4: Int = 600,

    // ── Easings ──────────────────────────────────────────────────────────────
    /** Standard easing — most UI elements. */
    val easingStandard: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f),
    /** Emphasized easing — full-screen transitions, hero content. */
    val easingEmphasized: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f),
    /** Decelerated — incoming elements (enter). */
    val easingDecelerated: Easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f),
    /** Accelerated — outgoing elements (exit). */
    val easingAccelerated: Easing = CubicBezierEasing(0.3f, 0.0f, 1.0f, 1.0f),

    // ── Pattern parameters ───────────────────────────────────────────────────
    /** Slide distance for shared-axis-X transitions. */
    val sharedAxisSlideDistance: Dp = 30.dp,
    /** Duration of one full breath of the "refreshing now" pulse (consumed by `Modifier.kptRefreshingPulse`). */
    val refreshingPulseDurationMs: Int = 1200,
    /** Stagger delay per item when `LazyColumn` items use `Modifier.kptListItemEnter`. */
    val listItemEnterStaggerMs: Int = 30,
    /** Cap on list-item stagger — items past this index snap into place. */
    val listItemEnterMaxAnimated: Int = 20,
)

/**
 * CompositionLocal carrying the app's motion scale — the shared durations and easings.
 *
 * Read this rather than hardcoding a duration: symmetric enter/exit timings are what make
 * transitions feel like one system instead of per-screen choices.
 */
val LocalMotion = staticCompositionLocalOf { Motion() }

/** Resolve the active [Motion] specs from composition. */
val MaterialTheme.motion: Motion
    @Composable
    @ReadOnlyComposable
    get() = LocalMotion.current.also { MotionSnapshot.current = it }

/**
 * Last-read snapshot of the active [Motion]. Updated as a side effect whenever any
 * `@Composable` site reads [MaterialTheme.motion]. Used by non-`@Composable` code
 * paths — chiefly the `composableWith*Transitions` helpers in `core-base/ui` — that
 * need motion tokens from outside a Composable scope (`NavGraphBuilder` lambdas are
 * non-`@Composable`).
 *
 * **Reactivity caveat**: the snapshot updates **on `@Composable` read**. If a fork
 * swaps the Motion provider mid-app, non-`@Composable` consumers won't observe the
 * change until at least one `@Composable` site reads `MaterialTheme.motion` again.
 * In practice every screen using motion-aware widgets (FreshnessIndicator, charts,
 * list-item-enter modifier, etc.) triggers a read on composition, so transitions on
 * subsequent pushes get the updated value.
 *
 * **Initial value**: defaults to `Motion()` — same as `LocalMotion`'s default — so
 * the very first transition is well-defined even before any `@Composable` read.
 */
object MotionSnapshot {
    /**
     * The motion values last published by the theme.
     *
     * A snapshot outside composition, for the transition builders that must run where `LocalMotion` cannot be read.
     * `@Volatile` because it is written on the composition thread and read from animation callbacks; defaults to
     * `Motion()` so the first transition is defined before any read.
     */
    @kotlin.concurrent.Volatile
    var current: Motion = Motion()
        internal set
}
