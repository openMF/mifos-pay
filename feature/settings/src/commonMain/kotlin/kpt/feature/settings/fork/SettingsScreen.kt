/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.settings.fork

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.component.BasicDialogState
import kpt.core.designsystem.component.LoadingDialogState
import kpt.core.designsystem.component.MifosBasicDialog
import kpt.core.designsystem.component.MifosLoadingDialog
import kpt.core.designsystem.component.MifosScaffold
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.ui.utils.EventsEffect
import kpt.feature.settings.di.MifosSettingsViewModelQualifier
import kpt.feature.settings.generated.resources.Res
import kpt.feature.settings.generated.resources.feature_settings_change_language_text
import kpt.feature.settings.generated.resources.feature_settings_change_passcode
import kpt.feature.settings.generated.resources.feature_settings_change_password
import kpt.feature.settings.generated.resources.feature_settings_disable_account
import kpt.feature.settings.generated.resources.feature_settings_disable_biometrics
import kpt.feature.settings.generated.resources.feature_settings_enable_biometrics
import kpt.feature.settings.generated.resources.feature_settings_faq
import kpt.feature.settings.generated.resources.feature_settings_log_out
import kpt.feature.settings.generated.resources.feature_settings_profile
import kpt.feature.settings.generated.resources.feature_settings_settings
import kpt.feature.settings.generated.resources.outline_logout
import kpt.feature.settings.generated.resources.outline_password
import kpt.feature.settings.generated.resources.outline_pin
import kpt.feature.settings.language.LanguageDialog
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.mifos.authenticator.biometrics.platformAuthenticationProvider
import org.mifos.authenticator.biometrics.platformAuthenticator.PlatformAuthenticatorStatus
import org.mifos.feature.passcode.rememberBiometricErrorMessages
import org.mifos.feature.passcode.rememberBiometricPromptStrings
/**
 * Settings screen route. Owns the passcode/biometrics integration glue:
 *
 *  - Reads `authProvider.isRegistered` from the composition local
 *    [platformAuthenticationProvider] and threads it down as the source of
 *    truth for the biometrics-toggle label and the disable-vs-enable branch.
 *  - Observes [entryStateHandle] for the disable-biometrics round-trip
 *    boolean (key: [DISABLE_BIOMETRICS_VERIFICATION_KEY]) written by the
 *    internal passcode screen, and re-dispatches it as
 *    [SettingsAction.DisableBiometricsResult] so the VM can call
 *    `authProvider.unregister()`.
 *  - Routes [SettingsEvent.NavigateToPasscodeScreen] to
 *    [navigateToPasscodeScreen] with the verification-key set to
 *    [DISABLE_BIOMETRICS_VERIFICATION_KEY] when the user is currently
 *    registered (disable flow), and `null` otherwise (change-passcode flow,
 *    no round-trip channel needed).
 *
 * @param navigateToPasscodeScreen `(verificationKey?) -> Unit` — caller
 *        binds this to `navController::navigateToInternalMifosPasscodeScreen`.
 *        The verification-key is written/read on the **previous** back-stack
 *        entry's saved-state-handle, which is this screen's [entryStateHandle].
 * @param entryStateHandle The settings destination's own
 *        `SavedStateHandle`, hoisted from the nav-graph builder so the
 *        round-trip channel works.
 */
@Composable
internal fun SettingsScreenRoute(
    backPress: () -> Unit,
    onEditPassword: () -> Unit,
    onLogout: () -> Unit,
    handleAppLocale: (String) -> Unit,
    navigateToPasscodeScreen: (verificationKey: String?) -> Unit,
    navigateToFaqScreen: () -> Unit,
    navigateToNotificationScreen: () -> Unit,
    navigateToProfile: () -> Unit,
    entryStateHandle: SavedStateHandle,
    modifier: Modifier = Modifier,
    viewmodel: SettingsViewModel = koinViewModel(qualifier = MifosSettingsViewModelQualifier),
) {
    val state by viewmodel.stateFlow.collectAsStateWithLifecycle()
    val authProvider = platformAuthenticationProvider.current
    val isRegistered by authProvider.isRegistered.collectAsStateWithLifecycle()

    val disableBiometricsResult by entryStateHandle
        .getStateFlow<Boolean?>(DISABLE_BIOMETRICS_VERIFICATION_KEY, null)
        .collectAsStateWithLifecycle()

    LaunchedEffect(disableBiometricsResult) {
        disableBiometricsResult?.let { result ->
            viewmodel.trySendAction(
                SettingsAction.DisableBiometricsResult(
                    success = result,
                    systemAuthProvider = authProvider,
                ),
            )
            entryStateHandle.remove<Boolean>(DISABLE_BIOMETRICS_VERIFICATION_KEY)
        }
    }

    EventsEffect(viewmodel) { event ->
        when (event) {
            SettingsEvent.OnNavigateBack -> backPress.invoke()
            SettingsEvent.NavigateToPasscodeScreen -> {
                navigateToPasscodeScreen(
                    if (isRegistered) DISABLE_BIOMETRICS_VERIFICATION_KEY else null,
                )
            }
            is SettingsEvent.ChangeLocale -> handleAppLocale(event.locale)
            SettingsEvent.OnNavigateToEditPasswordScreen -> onEditPassword.invoke()
            SettingsEvent.OnNavigateToFaqScreen -> navigateToFaqScreen.invoke()
            SettingsEvent.OnNavigateToLogout -> onLogout.invoke()
            SettingsEvent.OnNavigateToProfile -> navigateToProfile.invoke()
            SettingsEvent.OnNavigateToNotificationScreen -> navigateToNotificationScreen.invoke()
        }
    }

    Box(modifier) {
        SettingsDialogs(
            dialogState = state.dialogState,
            onDismissRequest = remember(viewmodel) {
                { viewmodel.trySendAction(SettingsAction.DismissDialog) }
            },
        )

        SettingsScreenContent(
            isSystemAuthenticationEnabled = isRegistered,
            onAction = viewmodel::trySendAction,
        )
    }
}

@Composable
private fun SettingsScreenContent(
    isSystemAuthenticationEnabled: Boolean,
    modifier: Modifier = Modifier,
    onAction: (SettingsAction) -> Unit,
) {
    val authProvider = platformAuthenticationProvider.current
    val authenticatorStatus by authProvider.authenticatorStatus.collectAsStateWithLifecycle()
    val biometricErrorMessages = rememberBiometricErrorMessages()
    val biometricPromptStrings = rememberBiometricPromptStrings()

    MifosScaffold(
        modifier = modifier,
        topBarTitle = stringResource(Res.string.feature_settings_settings),
        backPress = {
            onAction(SettingsAction.NavigateBack)
        },
        containerColor = KptTheme.colorScheme.background,
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
        ) {
//            SettingsCardItem(
//                title = stringResource(Res.string.feature_settings_notification_settings),
//                icon = MifosIcons.OutlinedNotifications,
//                onClick = {
//                    onAction(SettingsAction.NavigateToNotificationSettings)
//                },
//            )

            SettingsCardItem(
                title = stringResource(Res.string.feature_settings_faq),
                icon = MifosIcons.OutlinedInfo,
                onClick = {
                    onAction(SettingsAction.NavigateToFaqScreen)
                },
            )

            SettingsCardItem(
                title = stringResource(Res.string.feature_settings_profile),
                icon = MifosIcons.Profile,
                onClick = {
                    onAction(SettingsAction.NavigateToProfile)
                },
            )

            SettingsCardItem(
                title = stringResource(Res.string.feature_settings_change_password),
                icon = vectorResource(Res.drawable.outline_password),
                onClick = {
                    onAction(SettingsAction.ChangePassword)
                },
            )

            SettingsCardItem(
                title = stringResource(Res.string.feature_settings_change_passcode),
                icon = vectorResource(Res.drawable.outline_pin),
                onClick = {
                    onAction(SettingsAction.ChangePasscode)
                },
            )

            SettingsCardItem(
                title = stringResource(Res.string.feature_settings_change_language_text),
                icon = MifosIcons.Language,
                onClick = {
                    onAction(SettingsAction.ShowLanguageSelection)
                },
            )

            if (!authenticatorStatus.contains(PlatformAuthenticatorStatus.BIOMETRICS_NOT_AVAILABLE)) {
                SettingsCardItem(
                    title = if (isSystemAuthenticationEnabled) {
                        stringResource(Res.string.feature_settings_disable_biometrics)
                    } else {
                        stringResource(Res.string.feature_settings_enable_biometrics)
                    },
                    icon = MifosIcons.Fingerprint,
                    onClick = {
                        if (authenticatorStatus.contains(PlatformAuthenticatorStatus.BIOMETRICS_SET)) {
                            onAction(
                                SettingsAction.ToggleSystemAuth(
                                    systemAuthProvider = authProvider,
                                    isCurrentlyRegistered = isSystemAuthenticationEnabled,
                                    errorMessages = biometricErrorMessages,
                                    promptStrings = biometricPromptStrings,
                                ),
                            )
                        } else {
                            onAction(SettingsAction.BiometricsNotAvailable)
                        }
                    },
                )
            }

            SettingsCardItem(
                title = stringResource(Res.string.feature_settings_log_out),
                icon = vectorResource(Res.drawable.outline_logout),
                onClick = {
                    onAction(SettingsAction.Logout)
                },
            )

            SettingsCardItem(
                title = stringResource(Res.string.feature_settings_disable_account),
                icon = MifosIcons.OutlinedLock,
                color = KptTheme.colorScheme.error,
                onClick = {
                    onAction(SettingsAction.DisableAccount)
                },
            )
        }
    }
}

@Composable
private fun SettingsCardItem(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    color: Color = KptTheme.colorScheme.onSurface,
    onClick: () -> Unit,
) {
    ListItem(
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = title,
            )
        },
        headlineContent = {
            Text(text = title)
        },
        trailingContent = {
            Icon(
                imageVector = MifosIcons.ChevronRight,
                contentDescription = null,
            )
        },
        modifier = modifier.clickable {
            onClick()
        },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
            headlineColor = color,
        ),
    )
}

@Composable
private fun SettingsDialogs(
    dialogState: DialogState?,
    onDismissRequest: () -> Unit,
) {
    when (dialogState) {
        is DialogState.Error -> MifosBasicDialog(
            visibilityState = BasicDialogState.Shown(
                message = dialogState.message,
            ),
            onDismissRequest = onDismissRequest,
        )

        is DialogState.Loading -> MifosLoadingDialog(
            visibilityState = LoadingDialogState.Shown,
        )

        is DialogState.DisableAccount -> MifosBasicDialog(
            visibilityState = BasicDialogState.Shown(
                title = stringResource(dialogState.title),
                message = stringResource(dialogState.message),
            ),
            onConfirm = dialogState.onConfirm,
            onDismissRequest = onDismissRequest,
        )

        is DialogState.Logout -> MifosBasicDialog(
            visibilityState = BasicDialogState.Shown(
                title = stringResource(dialogState.title),
                message = stringResource(dialogState.message),
            ),
            onConfirm = dialogState.onConfirm,
            onDismissRequest = onDismissRequest,
        )

        DialogState.LanguageSelection -> {
            LanguageDialog(
                onDismiss = onDismissRequest,
            )
        }

        null -> Unit
    }
}

@Preview
@Composable
private fun SettingsScreenPreview() {
    SettingsScreenContent(
        onAction = {},
        isSystemAuthenticationEnabled = true,
    )
}
