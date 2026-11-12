/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.history.transactions

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.update
import kpt.core.common.ScreenState
import kpt.core.data.repository.AccountRepository
import kpt.core.model.savingsaccount.Transaction
import kpt.core.model.savingsaccount.TransferDetail
import kpt.core.ui.fork.utils.BaseViewModel
import kpt.feature.history.transactions.STState.ViewState.Content
import kpt.feature.history.transactions.STState.ViewState.Error
internal class SpecificTransactionsViewModel(
    private val accountRepository: AccountRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<STState, STEvent, STAction>(
    initialState = STState(viewState = STState.ViewState.Loading),
) {

    companion object {
        private const val ACCOUNT_ID_KEY = "accountId"
        private const val TRANSACTION_ID_KEY = "transactionId"
    }

    init {
        savedStateHandle.get<Long>(ACCOUNT_ID_KEY)?.let { accountId ->
            savedStateHandle.get<Long>(TRANSACTION_ID_KEY)?.let { transactionId ->
                // Use the BaseViewModel `observeScreen` bridge to fold the
                // ScreenState stream directly. The internal
                // `TransactionReceive` action (which used to shuttle
                // DataState) is removed; the reducer folds inline.
                accountRepository.getTransaction(accountId, transactionId).observeScreen { screenState ->
                    when (screenState) {
                        is ScreenState.Loading -> {
                            mutableStateFlow.update {
                                it.copy(viewState = STState.ViewState.Loading)
                            }
                        }

                        is ScreenState.Empty -> {
                            mutableStateFlow.update {
                                it.copy(viewState = Error("Transaction not found."))
                            }
                        }

                        is ScreenState.Content -> {
                            handleTransferDetailReceive(screenState.data)
                        }

                        is ScreenState.Error -> {
                            mutableStateFlow.update {
                                it.copy(viewState = Error(screenState.error.message.toString()))
                            }
                        }

                        is ScreenState.NoNetwork -> {
                            mutableStateFlow.update {
                                it.copy(viewState = Error("No network. Please check your connection."))
                            }
                        }

                        is ScreenState.Unauthenticated -> {
                            mutableStateFlow.update {
                                it.copy(viewState = Error("Session expired. Please log in again."))
                            }
                        }
                    }
                }
            }
        }
    }

    override fun handleAction(action: STAction) {
        when (action) {
            is STAction.NavigateBack -> {
                sendEvent(STEvent.OnNavigateBack)
            }

            is STAction.ViewTransaction -> {
                sendEvent(STEvent.OnViewTransaction(action.transferId))
            }
        }
    }

    private fun handleTransferDetailReceive(transaction: Transaction) {
        mutableStateFlow.update {
            it.copy(viewState = Content(transaction, null))
        }
        // TODO: below api not there for Self So Commented it
//        transaction.transferId?.let { transferId ->
//            accountRepository.getAccountTransfer(transferId)
//                .onEach { result: DataState<TransferDetail> ->
//                    when (result) {
//                        is DataState.Error -> {
//                            mutableStateFlow.update {
//                                it.copy(viewState = Error(result.exception.message.toString()))
//                            }
//                        }
//
//                        is DataState.Loading -> {
//                            mutableStateFlow.update {
//                                it.copy(viewState = STState.ViewState.Loading)
//                            }
//                        }
//
//                        is DataState.Success -> {
//                            mutableStateFlow.update {
//                                it.copy(viewState = Content(transaction, result.data))
//                            }
//                        }
//                    }
//                }.launchIn(viewModelScope)
//        } ?: run {
//            mutableStateFlow.update {
//                it.copy(viewState = Content(transaction, null))
//            }
//        }
    }
}

internal data class STState(
    val viewState: ViewState,
) {
    sealed interface ViewState {
        data object Loading : ViewState
        data class Error(val message: String) : ViewState
        data class Content(
            val transaction: Transaction,
            val detail: TransferDetail?,
        ) : ViewState
    }
}

internal sealed interface STEvent {
    data object OnNavigateBack : STEvent
    data class OnViewTransaction(val transferId: Long) : STEvent
}

internal sealed interface STAction {
    data object NavigateBack : STAction
    data class ViewTransaction(val transferId: Long) : STAction
}
