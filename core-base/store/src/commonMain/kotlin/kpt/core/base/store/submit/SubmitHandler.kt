/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.store.submit

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kpt.core.base.store.error.categorize

/**
 * Reusable one-shot executor for a single form/action submission lifecycle.
 *
 * Unlike [ScreenDataStream] (continuous Flow wrapper), [SubmitHandler] is a single-execution
 * executor: each [submit] call performs one API call and transitions [state] to a terminal state.
 *
 * **ViewModel usage:**
 * ```kotlin
 * private val submit = viewModelScope.submitHandler<ClientId>()
 * val submitState    = submit.state
 *     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SubmitState.Idle)
 *
 * fun onSave(form: ClientForm) = submit.submit { repository.createClient(form) }
 * fun onRetry()               = submit.retry()
 * fun onDismiss()             = submit.reset()
 * ```
 *
 * **Screen usage:**
 * ```kotlin
 * Box(Modifier.fillMaxSize()) {
 *     FormContent(enabled = !submitState.isSubmitting, onSubmit = { viewModel.onSave(it) })
 *     SubmitProgressOverlay(visible = submitState.isSubmitting)
 *     SubmitResultHandler(
 *         state = submitState,
 *         onSubmitted = { onNavigateBack() },
 *         onFailed = { error, category -> viewModel.onDismiss(); showError(error, category) },
 *     )
 * }
 * ```
 *
 * Create via [submitHandler] extension on [CoroutineScope].
 */
class SubmitHandler<R> internal constructor(
    private val scope: CoroutineScope,
) {
    private val _state = MutableStateFlow<SubmitState<R>>(SubmitState.Idle)

    /** Observable state. Expose via `.stateIn()` in the ViewModel. */
    val state: StateFlow<SubmitState<R>> = _state.asStateFlow()

    private var lastBlock: (suspend () -> R)? = null
    private var activeJob: Job? = null

    /**
     * Per-mutation copy attached to the most recent [submit] / [retry] cycle. Carried into
     * the [SubmitState] transitions so consumers can surface action-specific messages.
     * Reset to null when [reset] is called.
     */
    private var currentMessages: SubmitMessages? = null

    /**
     * Execute [block] as a one-shot submission.
     *
     * - No-op if already [SubmitState.Submitting] (idempotent — safe on rapid button taps).
     * - Transitions: any non-Submitting state → Submitting → [SubmitState.Submitted] | [SubmitState.Failed].
     * - Cancels any prior active job before starting (safe for sequential calls).
     *
     * @param block Suspend lambda performing the actual API call. Should throw on failure.
     */
    fun submit(block: suspend () -> R) {
        if (_state.value is SubmitState.Submitting) return
        lastBlock = block
        currentMessages = null
        execute(block)
    }

    /**
     * Execute [block] as a one-shot submission with per-mutation copy overrides.
     *
     * Same transitions as the no-args [submit] overload, but the supplied [messages] are
     * carried into each subsequent [SubmitState] transition. Each field of [messages] is
     * optional — `null` falls back to the theme-level default copy in the renderer.
     *
     * @param messages Per-mutation copy. Pass `null` for theme defaults.
     * @param block    Suspend lambda performing the actual API call. Should throw on failure.
     */
    fun submit(messages: SubmitMessages?, block: suspend () -> R) {
        if (_state.value is SubmitState.Submitting) return
        lastBlock = block
        currentMessages = messages
        execute(block)
    }

    /**
     * Re-run the last submitted block.
     * No-op if no prior block was submitted or currently [SubmitState.Submitting].
     */
    fun retry() {
        val block = lastBlock ?: return
        if (_state.value is SubmitState.Submitting) return
        execute(block)
    }

    /**
     * Return to [SubmitState.Idle].
     *
     * Call after consuming [SubmitState.Submitted] or [SubmitState.Failed] — e.g. after
     * the user dismisses an error dialog, or after navigating away on success — so that
     * the handler can be used again on the same screen.
     */
    fun reset() {
        activeJob?.cancel()
        currentMessages = null
        _state.value = SubmitState.Idle
    }

    private fun execute(block: suspend () -> R) {
        activeJob?.cancel()
        val msgs = currentMessages
        _state.value = SubmitState.Submitting(message = msgs?.submitting)
        activeJob = scope.launch {
            _state.value = try {
                SubmitState.Submitted(block(), message = msgs?.submitted)
            } catch (e: CancellationException) {
                throw e // never swallow scope cancellation as a failure
            } catch (e: Exception) {
                SubmitState.Failed(
                    error = e,
                    category = categorize(e),
                    message = msgs?.failed,
                )
            }
        }
    }
}

/**
 * Creates a [SubmitHandler] bound to this [CoroutineScope] (typically `viewModelScope`).
 *
 * ```kotlin
 * private val submit = viewModelScope.submitHandler<Unit>()
 * ```
 */
fun <R> CoroutineScope.submitHandler(): SubmitHandler<R> = SubmitHandler(this)
