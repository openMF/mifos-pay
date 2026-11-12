/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.store.screen

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Test double for [ScreenDataStream]. Emits [ScreenState] programmatically.
 *
 * Use in ViewModel unit tests to control state sequences without a real Store or network:
 * ```kotlin
 * val fakeStream = FakeScreenDataStream<ClientDetail>()
 * val vm = EditClientViewModel(stream = fakeStream.asStream, ...)
 *
 * fakeStream.emit(ScreenState.Loading)
 * fakeStream.emit(ScreenState.Content(client))
 * ```
 *
 * **Combined-VM fixture pattern** for ScreenDataStream + SubmitHandler tests:
 * 1. Submit blocked while Loading: emit Loading, call vm.onSave() → SubmitState stays Idle.
 * 2. Background refresh during Submitting: emit Content, trigger submit, emit second Content —
 *    SubmitState should stay Submitting (submit wins the race).
 * 3. `submitWhenContent` fires only after Content arrives: emit Loading, then Content —
 *    verify submit executes only once Content is present.
 */
class FakeScreenDataStream<T> {

    private val emittedStates = MutableSharedFlow<ScreenState<T>>(
        replay = 1,
        extraBufferCapacity = 16,
    )
    private val refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /**
     * The underlying [ScreenDataStream] to pass to ViewModels under test.
     * Its [ScreenDataStream.state] is backed by this fake's emission channel.
     */
    val asStream: ScreenDataStream<T> = ScreenDataStream(
        state = emittedStates.asSharedFlow(),
        refreshTrigger = refreshTrigger,
    )

    /** Convenience accessor for [asStream].state. */
    val state: Flow<ScreenState<T>> get() = asStream.state

    /** Emits [state] to all active collectors. Suspends if buffer is full. */
    suspend fun emit(state: ScreenState<T>) {
        emittedStates.emit(state)
    }

    /** Emits [state] without suspending. Returns false if the buffer is full. */
    fun tryEmit(state: ScreenState<T>): Boolean = emittedStates.tryEmit(state)
}
