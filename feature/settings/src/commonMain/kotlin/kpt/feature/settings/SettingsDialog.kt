/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import kpt.core.model.user.DarkThemeConfig
import kpt.core.model.user.ThemeBrand
import kpt.feature.settings.generated.resources.Res
import kpt.feature.settings.generated.resources.feature_settings_brand_android
import kpt.feature.settings.generated.resources.feature_settings_brand_default
import kpt.feature.settings.generated.resources.feature_settings_dark_mode_config_dark
import kpt.feature.settings.generated.resources.feature_settings_dark_mode_config_light
import kpt.feature.settings.generated.resources.feature_settings_dark_mode_config_system_default
import kpt.feature.settings.generated.resources.feature_settings_dark_mode_preference
import kpt.feature.settings.generated.resources.feature_settings_dismiss_dialog_button_text
import kpt.feature.settings.generated.resources.feature_settings_dynamic_color_no
import kpt.feature.settings.generated.resources.feature_settings_dynamic_color_preference
import kpt.feature.settings.generated.resources.feature_settings_dynamic_color_yes
import kpt.feature.settings.generated.resources.feature_settings_loading
import kpt.feature.settings.generated.resources.feature_settings_theme
import kpt.feature.settings.generated.resources.feature_settings_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Settings dialog for theme brand, dynamic color and dark theme bound to [SettingsViewModel]. */
@Composable
fun SettingsDialog(onDismiss: () -> Unit, viewModel: SettingsViewModel = koinViewModel()) {
    val settingsUiState by viewModel.settingsUiState.collectAsStateWithLifecycle()
    SettingsDialog(
        onDismiss = onDismiss,
        settingsUiState = settingsUiState,
        onChangeThemeBrand = viewModel::updateThemeBrand,
        onChangeDynamicColorPreference = viewModel::updateDynamicColorPreference,
        onChangeDarkThemeConfig = viewModel::updateDarkThemeConfig,
    )
}

/** Stateless settings dialog rendering [settingsUiState] and reporting preference changes through the callbacks. */
@Composable
fun SettingsDialog(
    settingsUiState: SettingsUiState,
    onDismiss: () -> Unit,
    onChangeThemeBrand: (themeBrand: ThemeBrand) -> Unit,
    onChangeDynamicColorPreference: (useDynamicColor: Boolean) -> Unit,
    onChangeDarkThemeConfig: (darkThemeConfig: DarkThemeConfig) -> Unit,
    modifier: Modifier = Modifier,
    supportDynamicColor: Boolean = supportsDynamicTheming(),
) {
    AlertDialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = modifier.fillMaxWidth(0.8f),
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = stringResource(resource = Res.string.feature_settings_title),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            HorizontalDivider()
            Column(Modifier.verticalScroll(rememberScrollState())) {
                when (settingsUiState) {
                    SettingsUiState.Loading -> {
                        Text(
                            text = stringResource(resource = Res.string.feature_settings_loading),
                            modifier = Modifier.padding(vertical = 16.dp),
                        )
                    }

                    is SettingsUiState.Success -> {
                        SettingsPanel(
                            settings = settingsUiState.settings,
                            supportDynamicColor = supportDynamicColor,
                            onChangeThemeBrand = onChangeThemeBrand,
                            onChangeDynamicColorPreference = onChangeDynamicColorPreference,
                            onChangeDarkThemeConfig = onChangeDarkThemeConfig,
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.padding(horizontal = 8.dp),
            ) {
                Text(
                    text = stringResource(resource = Res.string.feature_settings_dismiss_dialog_button_text),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
    )
}

// [ColumnScope] is used for using the [ColumnScope.AnimatedVisibility] extension overload composable.
@Composable
private fun ColumnScope.SettingsPanel(
    settings: UserEditableSettings,
    supportDynamicColor: Boolean,
    onChangeThemeBrand: (themeBrand: ThemeBrand) -> Unit,
    onChangeDynamicColorPreference: (useDynamicColor: Boolean) -> Unit,
    onChangeDarkThemeConfig: (darkThemeConfig: DarkThemeConfig) -> Unit,
) {
    val platform = getPlatform()
    if (platform == Platform.Android) {
        SettingsDialogSectionTitle(text = stringResource(resource = Res.string.feature_settings_theme))
        Column(Modifier.selectableGroup()) {
            SettingsDialogThemeChooserRow(
                text = stringResource(resource = Res.string.feature_settings_brand_default),
                selected = settings.brand == ThemeBrand.DEFAULT,
                onClick = { onChangeThemeBrand(ThemeBrand.DEFAULT) },
            )
            SettingsDialogThemeChooserRow(
                text = stringResource(resource = Res.string.feature_settings_brand_android),
                selected = settings.brand == ThemeBrand.ANDROID,
                onClick = { onChangeThemeBrand(ThemeBrand.ANDROID) },
            )
        }
        AnimatedVisibility(visible = settings.brand == ThemeBrand.DEFAULT && supportDynamicColor) {
            Column {
                SettingsDialogSectionTitle(
                    text = stringResource(
                        resource = Res.string.feature_settings_dynamic_color_preference,
                    ),
                )
                Column(Modifier.selectableGroup()) {
                    SettingsDialogThemeChooserRow(
                        text = stringResource(resource = Res.string.feature_settings_dynamic_color_yes),
                        selected = settings.useDynamicColor,
                        onClick = { onChangeDynamicColorPreference(true) },
                    )
                    SettingsDialogThemeChooserRow(
                        text = stringResource(resource = Res.string.feature_settings_dynamic_color_no),
                        selected = !settings.useDynamicColor,
                        onClick = { onChangeDynamicColorPreference(false) },
                    )
                }
            }
        }
    }
    SettingsDialogSectionTitle(text = stringResource(resource = Res.string.feature_settings_dark_mode_preference))
    Column(Modifier.selectableGroup()) {
        SettingsDialogThemeChooserRow(
            text = stringResource(resource = Res.string.feature_settings_dark_mode_config_system_default),
            selected = settings.darkThemeConfig == DarkThemeConfig.FOLLOW_SYSTEM,
            onClick = { onChangeDarkThemeConfig(DarkThemeConfig.FOLLOW_SYSTEM) },
        )
        SettingsDialogThemeChooserRow(
            text = stringResource(resource = Res.string.feature_settings_dark_mode_config_light),
            selected = settings.darkThemeConfig == DarkThemeConfig.LIGHT,
            onClick = { onChangeDarkThemeConfig(DarkThemeConfig.LIGHT) },
        )
        SettingsDialogThemeChooserRow(
            text = stringResource(resource = Res.string.feature_settings_dark_mode_config_dark),
            selected = settings.darkThemeConfig == DarkThemeConfig.DARK,
            onClick = { onChangeDarkThemeConfig(DarkThemeConfig.DARK) },
        )
    }
}

@Composable
private fun SettingsDialogSectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier.padding(top = 16.dp, bottom = 8.dp),
    )
}

/** Selectable row for one theme option in the settings dialog. */
@Composable
fun SettingsDialogThemeChooserRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
