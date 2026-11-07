/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.settings.language

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.model.user.Language
import kpt.core.model.user.fork.LanguageConfig
import kpt.core.model.user.toLanguage
import kpt.feature.settings.di.MifosSettingsViewModelQualifier
import kpt.feature.settings.fork.SettingsState
import kpt.feature.settings.fork.SettingsViewModel
import kpt.feature.settings.generated.resources.Res
import kpt.feature.settings.generated.resources.feature_settings_change_language_placeholder_text
import kpt.feature.settings.generated.resources.feature_settings_change_language_text
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
/** Language chooser dialog bound to the fork [SettingsViewModel], dismissed through [onDismiss]. */
@Composable
fun LanguageDialog(
    onDismiss: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(qualifier = MifosSettingsViewModelQualifier),
) {
    val settingsUiState by viewModel.stateFlow.collectAsStateWithLifecycle()
    LanguageDialogContent(
        onDismiss = onDismiss,
        settingsUiState = settingsUiState,
        onChangeLanguage = viewModel::updateLanguage,
        onUpdateAppLocale = viewModel::updateAppLocale,
    )
}

/** Stateless language chooser dialog content rendering [settingsUiState] and reporting the choice. */
@Composable
fun LanguageDialogContent(
    settingsUiState: SettingsState,
    onDismiss: () -> Unit,
    onChangeLanguage: (language: LanguageConfig) -> Unit,
    onUpdateAppLocale: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = modifier.fillMaxWidth(0.8f),
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = stringResource(resource = Res.string.feature_settings_change_language_text),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            HorizontalDivider()
            Column(Modifier.verticalScroll(rememberScrollState())) {
                LanguagePanel(
                    currentLanguage = settingsUiState.pendingLanguage ?: Language.DEFAULT,
                    onChangeLanguage = onChangeLanguage,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onUpdateAppLocale()
                    onDismiss()
                },
                modifier = Modifier.padding(horizontal = 8.dp),
            ) {
                Text(
                    text = stringResource(resource = Res.string.feature_settings_change_language_placeholder_text),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
    )
}

@Composable
private fun LanguagePanel(currentLanguage: Language, onChangeLanguage: (language: LanguageConfig) -> Unit) {
    Column(Modifier.selectableGroup()) {
        LanguageConfig.entries.forEach { language ->
            LanguageChooserRow(
                text = language.text,
                selected = currentLanguage.localeName == language.toLanguage().localeName,
                onClick = { onChangeLanguage(language) },
            )
        }
    }
}

/** Selectable row showing one language option. */
@Composable
fun LanguageChooserRow(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
        )
        Spacer(Modifier.width(8.dp))
        Text(text)
    }
}
