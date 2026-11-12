/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.payments

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.Serializable
import kpt.core.common.getSerialized
import kpt.core.common.setSerialized
import kpt.core.datastore.prefs.ForkPreferencesRepository
import kpt.core.ui.fork.utils.BaseViewModel
/** ViewModel holding the client details used to send and request money. */
class TransferViewModel(
    private val repository: ForkPreferencesRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<TransferState, TransferEvent, TransferAction>(
    initialState = savedStateHandle.getSerialized(TRANSFER_STATE_KEY) ?: run {
        /** Currently signed-in client from preferences; required to seed the transfer state. */
        val client = requireNotNull(repository.client.value)

        TransferState(
            mobileNo = client.mobileNo,
            externalId = client.externalId,
        )
    },
) {

    /** Holds the saved-state key under which [TransferState] is persisted. */
    companion object {
        private const val TRANSFER_STATE_KEY = "TransferState"
    }

    init {
        stateFlow
            .onEach { savedStateHandle.setSerialized(key = TRANSFER_STATE_KEY, value = it) }
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: TransferAction) {
        when (action) {
            is TransferAction.ShowQR -> {
                sendEvent(TransferEvent.OnShowQR)
            }

            is TransferAction.CopyTextToClipboard -> {
                sendEvent(TransferEvent.OnCopyTextToClipboard(action.text))
            }
        }
    }
}

/**
 * Serializable UI state of the transfer screens, holding the client contact details.
 *
 * @property mobileNo Mobile number of the client.
 * @property externalId External id of the client.
 */
@Serializable
data class TransferState(
    val mobileNo: String,
    val externalId: String,
)

/** One-shot events emitted by [TransferViewModel]. */
sealed interface TransferEvent {
    /**
     * Event requesting text to be copied to the clipboard.
     *
     * @property text Text to copy to the clipboard.
     */
    data class OnCopyTextToClipboard(val text: String) : TransferEvent

    /** Event requesting the QR code to be shown. */
    data object OnShowQR : TransferEvent
}

/** Actions dispatched to [TransferViewModel]. */
sealed interface TransferAction {
    /**
     * Action requesting text to be copied to the clipboard.
     *
     * @property text Text to copy to the clipboard.
     */
    data class CopyTextToClipboard(val text: String) : TransferAction

    /** Action requesting the QR code to be shown. */
    data object ShowQR : TransferAction
}
