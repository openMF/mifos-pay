/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.registry.app.instance

import androidx.lifecycle.viewModelScope
import cmp.navigation.generated.resources.Res
import cmp.navigation.generated.resources.failed_to_load_instances
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kpt.core.common.ScreenState
import kpt.core.datastore.prefs.ForkPreferencesRepository
import kpt.core.model.instance.InstancesConfig
import kpt.core.model.instance.InterbankServer
import kpt.core.model.instance.ServerInstance
import kpt.core.network.main.config.InstanceConfigLoader
import kpt.core.ui.fork.utils.BaseViewModel
import org.jetbrains.compose.resources.getString
/** Loads the available server instances and applies the user's main and inter-bank selection to preferences. */
class InstanceSelectorViewModel(
    instanceConfigLoader: InstanceConfigLoader,
    private val userPreferencesRepository: ForkPreferencesRepository,
) : BaseViewModel<InstanceSelectorState, InstanceSelectorEvent, InstanceSelectorAction>(
    initialState = InstanceSelectorState(),
) {

    private val tempSelectedMainInstance = MutableStateFlow<ServerInstance?>(null)
    private val tempSelectedInterbankInstance = MutableStateFlow<InterbankServer?>(null)

    init {
        // Combine all sources and update state
        combine(
            instanceConfigLoader.observeInstancesConfig(),
            userPreferencesRepository.selectedInstance,
            userPreferencesRepository.selectedInterbankInstance,
            tempSelectedMainInstance,
            tempSelectedInterbankInstance,
        ) { remoteConfig, selectedMainInstance, selectedInterbankInstance, tempMainInstance, tempInterbankInstance ->
            when (remoteConfig) {
                is ScreenState.Content -> {
                    val config = remoteConfig.data
                    val defaultInstance = config.getDefaultInstance()

                    // Determine the effective main instance
                    val effectiveMainInstance = tempMainInstance
                        ?: selectedMainInstance
                        ?: defaultInstance

                    // Determine the effective interbank instance
                    val effectiveInterbankInstance = tempInterbankInstance
                        ?: selectedInterbankInstance
                        ?: effectiveMainInstance?.getDefaultInterbankServer()

                    mutableStateFlow.update {
                        it.copy(
                            instances = config.instances,
                            selectedMainInstance = selectedMainInstance ?: defaultInstance,
                            selectedInterbankInstance = selectedInterbankInstance
                                ?: defaultInstance?.getDefaultInterbankServer(),
                            tempSelectedMainInstance = effectiveMainInstance,
                            tempSelectedInterbankInstance = effectiveInterbankInstance,
                            isLoading = false,
                            error = null,
                        )
                    }
                }

                is ScreenState.Error -> {
                    val message = remoteConfig.error.message
                        ?: getString(Res.string.failed_to_load_instances)
                    mutableStateFlow.update {
                        it.copy(
                            isLoading = false,
                            error = message,
                        )
                    }
                }

                ScreenState.Loading -> {
                    mutableStateFlow.update {
                        it.copy(isLoading = true)
                    }
                }

                ScreenState.Empty -> {
                    mutableStateFlow.update {
                        it.copy(
                            instances = emptyList(),
                            isLoading = false,
                            error = null,
                        )
                    }
                }

                is ScreenState.NoNetwork,
                ScreenState.Unauthenticated,
                -> {
                    val message = getString(Res.string.failed_to_load_instances)
                    mutableStateFlow.update {
                        it.copy(
                            isLoading = false,
                            error = message,
                        )
                    }
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        ).launchIn(viewModelScope)
    }

    override fun handleAction(action: InstanceSelectorAction) {
        when (action) {
            is InstanceSelectorAction.SelectMainInstance -> {
                tempSelectedMainInstance.value = action.instance
                // Auto-select the default interbank server for the new main instance
                tempSelectedInterbankInstance.value = action.instance.getDefaultInterbankServer()
            }

            is InstanceSelectorAction.SelectInterbankInstance -> {
                tempSelectedInterbankInstance.value = action.instance
            }

            is InstanceSelectorAction.UpdateInstances -> {
                updateInstances()
            }

            is InstanceSelectorAction.ConfigLoaded -> {
                // Handled by init block
            }
        }
    }

    private fun updateInstances() {
        viewModelScope.launch {
            val currentState = state
            val mainInstanceChanged =
                tempSelectedMainInstance.value != currentState.selectedMainInstance
            val interbankInstanceChanged =
                tempSelectedInterbankInstance.value != currentState.selectedInterbankInstance

            if (mainInstanceChanged && tempSelectedMainInstance.value != null) {
                userPreferencesRepository.updateSelectedInstance(tempSelectedMainInstance.value!!)
            }

            if (interbankInstanceChanged && tempSelectedInterbankInstance.value != null) {
                userPreferencesRepository.updateSelectedInterbankInstance(
                    tempSelectedInterbankInstance.value!!,
                )
            }

            // Reset temp selections after update
            tempSelectedMainInstance.value = null
            tempSelectedInterbankInstance.value = null

            sendEvent(InstanceSelectorEvent.DismissSheet)
        }
    }
}

// State
/**
 * UI state of the instance selector.
 *
 * @property instances Server instances available to choose from.
 * @property selectedMainInstance Currently persisted main instance.
 * @property selectedInterbankInstance Currently persisted inter-bank server.
 * @property tempSelectedMainInstance Main instance picked in the sheet but not yet applied.
 * @property tempSelectedInterbankInstance Inter-bank server picked in the sheet but not yet applied.
 * @property isLoading True while the instance configuration is loading.
 * @property error Error message when loading failed, or null.
 */
data class InstanceSelectorState(
    val instances: List<ServerInstance> = emptyList(),
    val selectedMainInstance: ServerInstance? = null,
    val selectedInterbankInstance: InterbankServer? = null,
    val tempSelectedMainInstance: ServerInstance? = null,
    val tempSelectedInterbankInstance: InterbankServer? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
) {
    /** True when a pending selection differs from the persisted one. */
    val isUpdateEnabled: Boolean
        get() = tempSelectedMainInstance != selectedMainInstance ||
            tempSelectedInterbankInstance != selectedInterbankInstance
}

// Events
/** One-off events emitted by the instance selector. */
sealed interface InstanceSelectorEvent {
    /** Asks the UI to dismiss the sheet. */
    data object DismissSheet : InstanceSelectorEvent
}

// Actions
/** User and internal actions handled by the instance selector. */
sealed interface InstanceSelectorAction {
    /**
     * Picks a main server instance (not applied until update).
     *
     * @property instance The chosen main instance.
     */
    data class SelectMainInstance(val instance: ServerInstance) : InstanceSelectorAction

    /**
     * Picks an inter-bank server (not applied until update).
     *
     * @property instance The chosen inter-bank server.
     */
    data class SelectInterbankInstance(val instance: InterbankServer) : InstanceSelectorAction

    /** Applies the pending selections and dismisses the sheet. */
    data object UpdateInstances : InstanceSelectorAction

    /**
     * Delivers the loaded instances configuration to the view model.
     *
     * @property config Loading, success or error state of the configuration.
     */
    data class ConfigLoaded(val config: ScreenState<InstancesConfig>) : InstanceSelectorAction
}
