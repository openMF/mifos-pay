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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import kpt.core.base.designsystem.component.KptShimmerLoadingBox
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.freshness.FreshnessSignal
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.captiveportal.rememberOpenCaptivePortalSignIn

/** Default size of the visual icon/illustration in the empty/error/no-network states. */
private val DefaultVisualSize = 64.dp

/**
 * Generic composable that renders any [ScreenState] with sensible defaults.
 * Animates between states with a fade transition. When [ScreenState.Content.isRefreshing] is true,
 * invokes the [refreshingIndicator] slot above the content area. Pass `null` to suppress
 * the in-flight progress banner entirely.
 *
 * Override precedence (highest first): per-call slot lambdas → [LocalScreenStateDefaults] →
 * library defaults. Pass slot lambdas to override at this call site, or wrap a subtree in
 * `CompositionLocalProvider(LocalScreenStateDefaults provides ...)` to override app-wide.
 *
 * Usage:
 * ```
 * @Composable
 * fun SavingsScreen(vm: SavingsViewModel) {
 *     val state by vm.uiState.collectAsStateWithLifecycle()
 *     ScreenContent(state = state, onRetry = vm::onRetry) { data, freshness ->
 *         SavingsList(data.accounts)
 *     }
 * }
 * ```
 */
@Composable
fun <T> ScreenContent(
    state: ScreenState<T>,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    refreshingIndicator: (@Composable () -> Unit)? = { DefaultRefreshingBanner() },
    onDismiss: (() -> Unit)? = null,
    loading: @Composable () -> Unit = { DefaultLoadingContent() },
    empty: @Composable () -> Unit = { DefaultEmptyContent() },
    noNetwork: @Composable (isCaptivePortal: Boolean) -> Unit = { captive ->
        DefaultNoNetworkContent(onRetry, isCaptivePortal = captive, onDismiss = onDismiss)
    },
    error: @Composable (Throwable) -> Unit = { DefaultErrorContent(it, onRetry, onDismiss = onDismiss) },
    content: @Composable (data: T, freshnessSignal: FreshnessSignal) -> Unit,
) {
    AnimatedContent(
        targetState = state,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        contentKey = { it.transitionKey() },
        label = "ScreenContent",
        modifier = modifier,
        // Default is TopStart — placing slot content at top-left when it doesn't
        // fill the AnimatedContent box. Default*Content composables already use
        // fillMaxSize + Arrangement.Center, but during fade transitions or with
        // certain parent constraints they could collapse. Explicit Center here
        // makes empty / error / no-network states predictably centered.
        contentAlignment = Alignment.Center,
    ) { current ->
        when (current) {
            is ScreenState.Loading -> loading()
            is ScreenState.Empty -> empty()
            is ScreenState.NoNetwork -> noNetwork(current.isCaptivePortal)
            is ScreenState.Error -> error(current.error)
            is ScreenState.Unauthenticated -> error(IllegalStateException("Unauthenticated"))
            is ScreenState.Content -> {
                // fillMaxSize so Content (refreshing indicator + caller's content)
                // gets the full available height — otherwise this Column wraps and
                // a LazyColumn inside content may not get its requested fill.
                Column(modifier = Modifier.fillMaxSize()) {
                    if (current.freshnessSignal.isRefreshing) {
                        refreshingIndicator?.invoke()
                    }
                    content(current.data, current.freshnessSignal)
                }
            }
        }
    }
}

/**
 * Direct [ScreenDataStream] overload — the ViewModel exposes the stream (built in the repository)
 * and Compose consumes it here: `stream.state` is collected lifecycle-aware and retry is wired to
 * `stream.retry()`. The ViewModel neither converts to a `StateFlow` nor sets any state — the stream
 * owns the whole state machine. Mirrors [kpt.core.base.ui.paging.PagingScreenContent].
 *
 * Same slot/override precedence as the base overload: per-call slot lambda → [LocalScreenStateDefaults]
 * (the app-wide defaults branded in `core/store` `KptScreenStateDefaults`) → library default. So the
 * state UIs can be set per-screen here AND globally in `core/store`.
 *
 * Best for PASSTHROUGH reads (the stream already decides Empty via its `isEmpty`). Features that
 * PROJECT/COMBINE the Content payload (totals, search filter, computed schedule, dashboards) keep the
 * `state:` overload and do the `mapContent`/`combineContent` in the ViewModel — never in Compose.
 *
 * ```
 * // ViewModel:  val alerts = repository.alertsStream(viewModelScope)   // just the stream
 * ScreenContent(stream = viewModel.alerts) { alerts, _ -> LazyColumn { … } }
 * ```
 */
@Composable
fun <T> ScreenContent(
    stream: ScreenDataStream<T>,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit = stream::retry,
    refreshingIndicator: (@Composable () -> Unit)? = { DefaultRefreshingBanner() },
    onDismiss: (() -> Unit)? = null,
    loading: @Composable () -> Unit = { DefaultLoadingContent() },
    empty: @Composable () -> Unit = { DefaultEmptyContent() },
    noNetwork: @Composable (isCaptivePortal: Boolean) -> Unit = { captive ->
        DefaultNoNetworkContent(onRetry, isCaptivePortal = captive, onDismiss = onDismiss)
    },
    error: @Composable (Throwable) -> Unit = { DefaultErrorContent(it, onRetry, onDismiss = onDismiss) },
    content: @Composable (data: T, freshnessSignal: FreshnessSignal) -> Unit,
) {
    val state by stream.state.collectAsStateWithLifecycle(ScreenState.Loading)
    ScreenContent(
        state = state,
        onRetry = onRetry,
        modifier = modifier,
        refreshingIndicator = refreshingIndicator,
        onDismiss = onDismiss,
        loading = loading,
        empty = empty,
        noNetwork = noNetwork,
        error = error,
        content = content,
    )
}

/**
 * Stable transition key used by [AnimatedContent] to detect "same kind of state" without
 * comparing data payloads. Prevents jitter when only the [ScreenState.Content.data] changes.
 */
private fun ScreenState<*>.transitionKey(): String = when (this) {
    is ScreenState.Loading -> "loading"
    is ScreenState.Empty -> "empty"
    is ScreenState.NoNetwork -> if (isCaptivePortal) "no_network_captive" else "no_network"
    is ScreenState.Error -> "error"
    is ScreenState.Unauthenticated -> "unauthenticated"
    is ScreenState.Content -> "content"
}

// ── Default state composables ─────────────────────────────────────────────
//
// Each Default*Content reads its config from LocalScreenStateDefaults.current by default.
// Pass an explicit `config` argument to override per-call without going through
// CompositionLocal.

/**
 * The framework's loading rendering — spinner, skeleton or branded animation, per [config].
 *
 * A fork brands every screen's loading state at once by supplying its own `ScreenStateDefaults`
 * through `LocalScreenStateDefaults`; pass [config] only to override a single call site.
 */
@Composable
fun DefaultLoadingContent(
    modifier: Modifier = Modifier,
    config: ScreenStateLoading = LocalScreenStateDefaults.current.loading,
) {
    when (config) {
        is ScreenStateLoading.Spinner -> Box(
            modifier = modifier
                .fillMaxSize()
                .semantics {
                    contentDescription = "Loading"
                    liveRegion = LiveRegionMode.Polite
                },
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        is ScreenStateLoading.Skeleton -> SkeletonLoadingContent(
            rowCount = config.rowCount,
            modifier = modifier,
        )
        is ScreenStateLoading.Custom -> config.content()
    }
}

@Composable
private fun SkeletonLoadingContent(
    rowCount: Int,
    modifier: Modifier = Modifier,
) {
    val spacing = KptTheme.spacing
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(spacing.md)
            .semantics {
                contentDescription = "Loading"
                liveRegion = LiveRegionMode.Polite
            },
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        repeat(rowCount) {
            KptShimmerLoadingBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
            )
        }
    }
}

/**
 * The framework's empty rendering — for a successful read that returned nothing.
 *
 * Distinct from error on purpose: "no results" is a valid outcome, and showing a retry affordance
 * for it invites the user to retry something that already worked.
 */
@Composable
fun DefaultEmptyContent(
    modifier: Modifier = Modifier,
    config: ScreenStateEmpty = LocalScreenStateDefaults.current.empty,
) {
    val spacing = KptTheme.spacing
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(spacing.lg)
            .semantics {
                contentDescription = config.title?.let { "$it. ${config.message}" } ?: config.message
                liveRegion = LiveRegionMode.Polite
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        ScreenStateVisualRenderer(config.visual)
        Spacer(Modifier.height(spacing.md))
        config.title?.let { title ->
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(spacing.xs))
        }
        Text(
            text = config.message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        config.cta?.let { cta ->
            Spacer(Modifier.height(spacing.lg))
            OutlinedButton(onClick = cta.onClick) {
                Text(cta.label)
            }
        }
    }
}

/**
 * The framework's offline rendering, shown when the failure was connectivity rather than the request.
 *
 * Separate from [DefaultErrorContent] because the remedy differs: the user reconnects, rather than
 * being told something went wrong with a retry that cannot succeed yet.
 */
@Composable
fun DefaultNoNetworkContent(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    isCaptivePortal: Boolean = false,
    onDismiss: (() -> Unit)? = null,
    config: ScreenStateNoNetwork = LocalScreenStateDefaults.current.noNetwork,
) {
    val spacing = KptTheme.spacing
    val visual = if (isCaptivePortal) config.captivePortalVisual else config.visual
    val message = if (isCaptivePortal) config.captivePortalMessage else config.message
    // Prefer an explicitly configured action; fall back to the library-provided launcher.
    val captivePortalAction = config.captivePortalAction
        ?: rememberOpenCaptivePortalSignIn()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(spacing.lg)
            .semantics {
                contentDescription = message
                liveRegion = LiveRegionMode.Polite
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        ScreenStateVisualRenderer(visual)
        Spacer(Modifier.height(spacing.md))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(spacing.lg))
        if (isCaptivePortal) {
            // Primary: open platform-native captive-portal sign-in.
            Button(onClick = captivePortalAction) {
                Text(config.captivePortalActionText)
            }
            Spacer(Modifier.height(spacing.sm))
            // Secondary: retry, in case the user already signed in via system UI.
            ActionRow(onRetry = onRetry, retryText = config.retryText, onDismiss = onDismiss, retryAsOutlined = true)
        } else {
            ActionRow(onRetry = onRetry, retryText = config.retryText, onDismiss = onDismiss)
        }
    }
}

/**
 * The framework's error rendering, with the retry affordance wired to the screen's stream.
 *
 * Copy comes from the mapped `ErrorCategory`, not the raw throwable, so a user never sees an
 * exception message and every screen phrases the same failure the same way.
 */
@Composable
fun DefaultErrorContent(
    error: Throwable,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null,
    config: ScreenStateError = LocalScreenStateDefaults.current.error,
) {
    LaunchedEffect(error) { config.onShown?.invoke(error) }
    val spacing = KptTheme.spacing
    val message = config.messageFor(error)
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(spacing.lg)
            .semantics {
                contentDescription = config.title?.let { "$it. $message" } ?: message
                liveRegion = LiveRegionMode.Assertive
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        ScreenStateVisualRenderer(config.visual)
        Spacer(Modifier.height(spacing.md))
        config.title?.let { title ->
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(spacing.xs))
        }
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(spacing.lg))
        ActionRow(onRetry = onRetry, retryText = config.retryText, onDismiss = onDismiss)
    }
}

/**
 * Internal helper rendering a retry button alongside an optional dismiss (X) icon.
 * Used by [DefaultErrorContent] and [DefaultNoNetworkContent] when consumed inside
 * a multi-card dashboard via [kpt.core.base.ui.dashboard.IndependentCardLayout],
 * where users can opt to dismiss a single failed card rather than retry.
 */
@Composable
private fun ActionRow(
    onRetry: () -> Unit,
    retryText: String,
    onDismiss: (() -> Unit)?,
    retryAsOutlined: Boolean = false,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (retryAsOutlined) {
            OutlinedButton(onClick = onRetry) { Text(retryText) }
        } else {
            Button(onClick = onRetry) { Text(retryText) }
        }
        if (onDismiss != null) {
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                )
            }
        }
    }
}

// ── Visual renderer ──────────────────────────────────────────────────────

/**
 * Renders a `ScreenStateVisual` — vector, painter or Lottie — at a uniform size.
 *
 * Exists so the four Default*Content composables share one visual path: without it each would
 * branch over the visual type itself and they would drift in sizing and tint.
 */
@Composable
internal fun ScreenStateVisualRenderer(
    visual: ScreenStateVisual,
    modifier: Modifier = Modifier.size(DefaultVisualSize),
) {
    when (visual) {
        is ScreenStateVisual.Vector -> Icon(
            imageVector = visual.image,
            // parent layout owns contentDescription via semantics
            contentDescription = null,
            modifier = modifier,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        is ScreenStateVisual.PainterRef -> Image(
            painter = visual.painter(),
            contentDescription = null,
            modifier = modifier,
        )
        is ScreenStateVisual.Lottie -> {
            val composition by rememberLottieComposition { visual.spec() }
            val painter = rememberLottiePainter(
                composition = composition,
                iterations = visual.iterations,
                speed = visual.speed,
            )
            Image(
                painter = painter,
                contentDescription = null,
                modifier = modifier,
            )
        }
        is ScreenStateVisual.Custom -> visual.content()
    }
}
