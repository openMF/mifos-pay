/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.ui.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import kpt.core.base.store.error.ErrorCategory
import kpt.core.base.store.error.categorize

/**
 * App-level defaults for [ScreenContent] and [PagingScreenContent] slots.
 *
 * Provides three-tier overrides:
 * 1. **Per-call**: pass a slot lambda to `ScreenContent(empty = { ... })`.
 * 2. **App-wide**: `CompositionLocalProvider(LocalScreenStateDefaults provides MyDefaults) { ... }`.
 * 3. **Library default**: sensible Material 3 defaults out of the box.
 *
 * Override precedence (highest first): per-call → CompositionLocal → library default.
 *
 * Typical usage in an app theme:
 * ```kotlin
 * @Composable
 * fun KptTheme(content: @Composable () -> Unit) {
 *     val defaults = remember {
 *         ScreenStateDefaults(
 *             empty = ScreenStateEmpty(
 *                 visual = ScreenStateVisual.Vector(MyBrandIcons.EmptyBox),
 *                 title = "Nothing here yet",
 *                 message = "When you have data, it'll show up here.",
 *             ),
 *             error = ScreenStateError(
 *                 messageFor = ::mapErrorToUserMessage,
 *                 onShown = { error -> AppTelemetry.recordError("screen_state_error", error) },
 *             ),
 *         )
 *     }
 *     CompositionLocalProvider(LocalScreenStateDefaults provides defaults) {
 *         MaterialTheme { content() }
 *     }
 * }
 * ```
 */
@Immutable
data class ScreenStateDefaults(
    /** How a loading state renders — spinner, skeleton or a branded animation. */
    val loading: ScreenStateLoading = ScreenStateLoading.Spinner,
    /** How an empty state renders. Empty is a SUCCESSFUL read that returned nothing, never an error. */
    val empty: ScreenStateEmpty = ScreenStateEmpty(),
    /** How an offline state renders, including the captive-portal variant. */
    val noNetwork: ScreenStateNoNetwork = ScreenStateNoNetwork(),
    /** How a failure renders, including the retry affordance and the error→copy mapping. */
    val error: ScreenStateError = ScreenStateError(),
)

/** Configuration for the empty state. */
@Immutable
data class ScreenStateEmpty(
    /** Illustration shown above the empty copy. */
    val visual: ScreenStateVisual = ScreenStateVisual.Vector(Icons.Default.Inbox),
    /** Optional headline. Null renders [message] alone. */
    val title: String? = null,
    /** Body copy explaining that nothing is here yet — not that something failed. */
    val message: String = "No data available",
    /** Optional call to action, for an empty state the user can act on ("Add your first loan"). */
    val cta: ScreenStateCta? = null,
)

/** Configuration for the error state. */
@Immutable
data class ScreenStateError(
    /** Illustration shown above the error copy. */
    val visual: ScreenStateVisual = ScreenStateVisual.Vector(Icons.Default.Error),
    /** Optional headline. Null renders the mapped message alone. */
    val title: String? = null,
    /**
     * Translates a [Throwable] into a user-facing message. The default routes through
     * [categorize] to provide category-aware copy (network / auth / server / generic).
     * Override per app via `ScreenStateError(messageFor = ::myMapper)`.
     */
    val messageFor: (Throwable) -> String = DefaultErrorMessageFor,
    /** Label on the retry button. */
    val retryText: String = "Try again",
    /** Optional analytics callback fired exactly once per distinct error rendered. */
    val onShown: ((Throwable) -> Unit)? = null,
)

/**
 * Singleton reference used as the default for [ScreenStateError.messageFor].
 * Stored as a val so that two default-constructed [ScreenStateError] instances compare
 * equal on all platforms including Kotlin/JS (where `::fun` creates a new wrapper each time).
 */
val DefaultErrorMessageFor: (Throwable) -> String = ::defaultErrorMessage

/**
 * Library default for [ScreenStateError.messageFor]. Routes through [categorize] so apps
 * that don't supply a custom mapper still get sensible category-specific copy out of the
 * box. Apps that need branded messages should pass their own lambda — typically wired
 * once at the theme level via `LocalScreenStateDefaults`.
 */
fun defaultErrorMessage(error: Throwable): String = when (val cat = categorize(error)) {
    ErrorCategory.Network -> "Can't reach the server. Check your connection."
    ErrorCategory.Timeout.Connect -> "Connection timed out. Check your connection and try again."
    ErrorCategory.Timeout.Read -> "Server took too long to respond. Try again in a bit."
    ErrorCategory.Auth -> "Your session expired. Please sign in again."
    is ErrorCategory.Server -> "Our servers are having a moment. Try again in a bit."
    ErrorCategory.RateLimit -> "Too many requests. Please wait and try again."
    ErrorCategory.QuotaExceeded -> "You've reached your quota. Upgrade or wait to continue."
    is ErrorCategory.ClientError -> error.message ?: "Request failed (${cat.httpCode})."
    ErrorCategory.Generic -> error.message ?: "Something went wrong"
}

/**
 * Configuration for the no-network state, including captive-portal variant.
 *
 * When the rendered state is `isCaptivePortal == true`, [DefaultNoNetworkContent] surfaces
 * a primary CTA button labelled [captivePortalActionText]. If [captivePortalAction] is `null`
 * (the default), [DefaultNoNetworkContent] automatically wires the button to
 * `rememberOpenCaptivePortalSignIn()` (backed by cmp-intent-launcher). Provide a non-null
 * [captivePortalAction] only to override the default behaviour for this screen.
 */
@Immutable
data class ScreenStateNoNetwork(
    /** Illustration for a plain offline state. */
    val visual: ScreenStateVisual = ScreenStateVisual.Vector(Icons.Default.WifiOff),
    /** Illustration for the captive-portal case, which is a different problem than being offline. */
    val captivePortalVisual: ScreenStateVisual = ScreenStateVisual.Vector(Icons.Default.CloudOff),
    /** Copy for a plain offline state. */
    val message: String = "You're offline",
    /** Copy for the captive-portal case — the network is reachable but wants a sign-in. */
    val captivePortalMessage: String = "Sign in to your WiFi network",
    /** Label on the captive-portal CTA button. */
    val captivePortalActionText: String = "Open sign-in page",
    /**
     * Optional override for the captive-portal CTA. When `null` (the default),
     * [DefaultNoNetworkContent] provides the action via `rememberOpenCaptivePortalSignIn()`.
     */
    val captivePortalAction: (() -> Unit)? = null,
    /** Label on the retry button. */
    val retryText: String = "Try again",
)

/** A primary call-to-action shown alongside an empty/error state. */
@Immutable
data class ScreenStateCta(
    /**
     * The button's label. Fork-supplied so an empty or error state can say "Add your first loan" rather than a generic
     * "Retry".
     */
    val label: String,
    /** Invoked when the CTA is tapped. */
    val onClick: () -> Unit,
)

/** Loading-state strategy. Library ships [Spinner] and [Skeleton]; consumers can supply [Custom]. */
sealed interface ScreenStateLoading {
    /** Centered Material 3 [androidx.compose.material3.CircularProgressIndicator]. */
    data object Spinner : ScreenStateLoading

    /**
     * Shimmer skeleton with [rowCount] placeholder rows. Implementation lands in Phase 2.
     *
     * @property rowCount placeholder rows to shimmer; match the real list so the layout does not
     *   jump when content arrives.
     */
    data class Skeleton(val rowCount: Int = 3) : ScreenStateLoading

    /**
     * Fully custom loading content.
     *
     * @property content rendered in place of the library's loading state.
     */
    data class Custom(val content: @Composable () -> Unit) : ScreenStateLoading
}

/**
 * Visual element for empty/error/no-network states. Sealed: extend by adding a new variant
 * here and a branch in [ScreenStateVisualRenderer].
 */
sealed interface ScreenStateVisual {
    /**
     * Material icon or any [ImageVector].
     *
     * @property image the vector to draw.
     */
    data class Vector(val image: ImageVector) : ScreenStateVisual

    /**
     * Arbitrary [Painter] (e.g., from `composeResources`). Lambda form so the painter
     * can be created with `painterResource(...)` inside a Composable scope.
     *
     * @property painter suspendable factory for the painter, invoked inside composition.
     */
    data class PainterRef(val painter: @Composable () -> Painter) : ScreenStateVisual

    /**
     * Lottie animation rendered via Compottie. The [spec] lambda is a `suspend` loader
     * (matches `rememberLottieComposition`'s signature) so it can read assets from
     * `composeResources` via `Res.readBytes(...)` or fetch from any other suspend source.
     *
     * @property spec Suspend factory for the [LottieCompositionSpec] (composeResources, JSON, etc.).
     * @property iterations Loop count; defaults to [Compottie.IterateForever].
     * @property speed Playback speed multiplier; 1f is real-time.
     */
    data class Lottie(
        val spec: suspend () -> LottieCompositionSpec,
        val iterations: Int = Compottie.IterateForever,
        val speed: Float = 1f,
    ) : ScreenStateVisual

    /**
     * Fully custom Composable — escape hatch for anything else.
     *
     * @property content the composable rendered in place of a built-in visual.
     */
    data class Custom(val content: @Composable () -> Unit) : ScreenStateVisual
}

/**
 * App-wide ScreenState defaults. Override via:
 * ```
 * CompositionLocalProvider(LocalScreenStateDefaults provides myDefaults) { App() }
 * ```
 */
val LocalScreenStateDefaults = compositionLocalOf { ScreenStateDefaults() }
