/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.autopay

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kpt.core.common.getSerialized
import kpt.core.common.setSerialized
import kpt.core.model.autopay.AutoPayGlobalSettings
import kpt.core.model.autopay.AutoPayRules
import kpt.core.model.autopay.NotificationSettings
import kpt.core.model.autopay.SecuritySettings
import kpt.core.ui.utils.BaseViewModel
class AutoPayPreferencesViewModel(
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<AutoPayPreferencesState, AutoPayPreferencesEvent, AutoPayPreferencesAction>(
    initialState = savedStateHandle.getSerialized(KEY_STATE) ?: AutoPayPreferencesState(),
) {

    companion object {
        private const val KEY_STATE = "autopay_preferences_state"
    }

    init {
        stateFlow
            .onEach { savedStateHandle.setSerialized(key = KEY_STATE, value = it) }
            .launchIn(viewModelScope)

        loadSettings()
    }

    override fun handleAction(action: AutoPayPreferencesAction) {
        when (action) {
            is AutoPayPreferencesAction.UpdateGlobalSettings -> {
                updateGlobalSettings(action.settings)
            }
            is AutoPayPreferencesAction.UpdateNotificationSettings -> {
                updateNotificationSettings(action.settings)
            }
            is AutoPayPreferencesAction.UpdateSecuritySettings -> {
                updateSecuritySettings(action.settings)
            }
            is AutoPayPreferencesAction.UpdateAutoPayRules -> {
                updateAutoPayRules(action.rules)
            }
            is AutoPayPreferencesAction.ToggleAutoPayEnabled -> {
                toggleAutoPayEnabled(action.enabled)
            }
            is AutoPayPreferencesAction.SaveSettings -> {
                saveSettings()
            }
            is AutoPayPreferencesAction.LoadSettings -> {
                loadSettings()
            }
        }
    }

    private fun loadSettings() {
        mutableStateFlow.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val defaultSettings = AutoPayGlobalSettings()

                mutableStateFlow.update {
                    it.copy(
                        isLoading = false,
                        globalSettings = defaultSettings,
                        hasUnsavedChanges = false,
                    )
                }
            } catch (e: Exception) {
                mutableStateFlow.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load settings",
                    )
                }
            }
        }
    }

    private fun updateGlobalSettings(settings: AutoPayGlobalSettings) {
        mutableStateFlow.update {
            it.copy(
                globalSettings = settings,
                hasUnsavedChanges = true,
            )
        }
    }

    private fun updateNotificationSettings(settings: NotificationSettings) {
        val currentSettings = stateFlow.value.globalSettings
        val updatedSettings = currentSettings.copy(notificationSettings = settings)

        mutableStateFlow.update {
            it.copy(
                globalSettings = updatedSettings,
                hasUnsavedChanges = true,
            )
        }
    }

    private fun updateSecuritySettings(settings: SecuritySettings) {
        val currentSettings = stateFlow.value.globalSettings
        val updatedSettings = currentSettings.copy(securitySettings = settings)

        mutableStateFlow.update {
            it.copy(
                globalSettings = updatedSettings,
                hasUnsavedChanges = true,
            )
        }
    }

    private fun updateAutoPayRules(rules: AutoPayRules) {
        val currentSettings = stateFlow.value.globalSettings
        val updatedSettings = currentSettings.copy(globalAutoPayRules = rules)

        mutableStateFlow.update {
            it.copy(
                globalSettings = updatedSettings,
                hasUnsavedChanges = true,
            )
        }
    }

    private fun toggleAutoPayEnabled(enabled: Boolean) {
        val currentSettings = stateFlow.value.globalSettings
        val updatedSettings = currentSettings.copy(isAutoPayEnabled = enabled)

        mutableStateFlow.update {
            it.copy(
                globalSettings = updatedSettings,
                hasUnsavedChanges = true,
            )
        }
    }

    private fun saveSettings() {
        mutableStateFlow.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            try {
                val settings = stateFlow.value.globalSettings

                mutableStateFlow.update {
                    it.copy(
                        isSaving = false,
                        hasUnsavedChanges = false,
                    )
                }

                sendEvent(AutoPayPreferencesEvent.SettingsSaved)
            } catch (e: Exception) {
                mutableStateFlow.update {
                    it.copy(
                        isSaving = false,
                        error = e.message ?: "Failed to save settings",
                    )
                }
            }
        }
    }
}

@Serializable
data class AutoPayPreferencesState(
    val globalSettings: AutoPayGlobalSettings = AutoPayGlobalSettings(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val error: String? = null,
)

sealed interface AutoPayPreferencesEvent {
    data object SettingsSaved : AutoPayPreferencesEvent
    data class ShowError(val message: String) : AutoPayPreferencesEvent
}

sealed interface AutoPayPreferencesAction {
    data class UpdateGlobalSettings(val settings: AutoPayGlobalSettings) : AutoPayPreferencesAction
    data class UpdateNotificationSettings(val settings: NotificationSettings) : AutoPayPreferencesAction
    data class UpdateSecuritySettings(val settings: SecuritySettings) : AutoPayPreferencesAction
    data class UpdateAutoPayRules(val rules: AutoPayRules) : AutoPayPreferencesAction
    data class ToggleAutoPayEnabled(val enabled: Boolean) : AutoPayPreferencesAction
    data object SaveSettings : AutoPayPreferencesAction
    data object LoadSettings : AutoPayPreferencesAction
}
