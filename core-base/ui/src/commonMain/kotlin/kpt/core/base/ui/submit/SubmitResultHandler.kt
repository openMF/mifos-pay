/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.ui.submit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kpt.core.base.store.error.ErrorCategory
import kpt.core.base.store.submit.SubmitState

/**
 * Side-effect composable that fires callbacks when [state] reaches a terminal state.
 *
 * - [onSubmitted] fires once when [SubmitState.Submitted] is observed.
 * - [onFailed] fires once when [SubmitState.Failed] is observed.
 *
 * Uses [LaunchedEffect] keyed on [state] — fires exactly once per terminal state transition.
 * This works correctly because [SubmitState.Submitted] and [SubmitState.Failed] are data classes
 * with distinct structural equality; repeated recompositions with the same state do not re-fire.
 *
 * **Important:** call [kpt.core.base.store.SubmitHandler.reset] inside the callback
 * to return to [SubmitState.Idle], otherwise the same terminal state persists and the handler
 * cannot be used again on the same screen instance.
 *
 * ```kotlin
 * SubmitResultHandler(
 *     state = submitState,
 *     onSubmitted = { clientId ->
 *         onNavigateToDetail(clientId)
 *         // no reset needed — navigating away destroys the ViewModel
 *     },
 *     onFailed = { error, category ->
 *         viewModel.onDismiss()          // calls submit.reset()
 *         when (category) {
 *             ErrorCategory.Network -> showNoNetworkSheet()
 *             ErrorCategory.Auth    -> onNavigateToLogin()
 *             else                  -> showErrorDialog(error.message)
 *         }
 *     },
 * )
 * ```
 *
 * @param state      The current [SubmitState] from [kpt.core.base.store.SubmitHandler.state].
 * @param onSubmitted Callback invoked with the result when submission succeeds.
 * @param onFailed    Optional callback invoked with the error + category when submission fails.
 *   If null, failures are silently ignored (useful when the screen only cares about success).
 */
@Composable
fun <R> SubmitResultHandler(
    state: SubmitState<R>,
    onSubmitted: (result: R) -> Unit,
    onFailed: ((error: Throwable, category: ErrorCategory) -> Unit)? = null,
) {
    LaunchedEffect(state) {
        when (state) {
            is SubmitState.Submitted -> onSubmitted(state.result)
            is SubmitState.Failed -> onFailed?.invoke(state.error, state.category)
            else -> Unit
        }
    }
}
