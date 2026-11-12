/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.profile.fork

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.designsystem.fork.component.BasicDialogState
import kpt.core.designsystem.fork.component.ForkBasicDialog
import kpt.core.designsystem.fork.component.ForkButton
import kpt.core.designsystem.fork.component.ForkLoadingDialog
import kpt.core.designsystem.fork.component.ForkScaffold
import kpt.core.designsystem.fork.component.LoadingDialogState
import kpt.core.designsystem.fork.icon.ForkIcons
import kpt.core.model.client.Client
import kpt.core.ui.fork.utils.EventsEffect
import kpt.feature.profile.ProfileAction
import kpt.feature.profile.ProfileEvent
import kpt.feature.profile.ProfileState
import kpt.feature.profile.ProfileViewModel
import kpt.feature.profile.components.ProfileDetailsCard
import kpt.feature.profile.components.ProfileImage
import kpt.feature.profile.generated.resources.Res
import kpt.feature.profile.generated.resources.feature_profile
import kpt.feature.profile.generated.resources.feature_profile_personal_qr_code
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
@Composable
internal fun ProfileScreen(
    navigateBack: () -> Unit,
    onEditProfile: () -> Unit,
    onLinkBackAccount: () -> Unit,
    showQrCode: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val clientState by viewModel.clientState.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
            ProfileEvent.OnEditProfile -> onEditProfile.invoke()
            ProfileEvent.OnLinkBankAccount -> onLinkBackAccount.invoke()
            ProfileEvent.ShowQRCode -> showQrCode.invoke()
            is ProfileEvent.OnNavigateBack -> navigateBack.invoke()
        }
    }

    ProfileDialogs(
        dialogState = state.dialogState,
        onDismissRequest = remember(viewModel) {
            { viewModel.trySendAction(ProfileAction.DismissErrorDialog) }
        },
    )

    ProfileScreenContent(
        state = state,
        clientState = clientState,
        onAction = remember(viewModel) {
            { action -> viewModel.trySendAction(action) }
        },
        onRetry = remember(viewModel) {
            { viewModel.retry() }
        },
        modifier = modifier.fillMaxSize(),
    )
}

@Composable
internal fun ProfileScreenContent(
    state: ProfileState,
    clientState: ScreenState<Client>,
    onAction: (ProfileAction) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ForkScaffold(
        modifier = modifier,
        topBarTitle = stringResource(Res.string.feature_profile),
        backPress = {
            onAction(ProfileAction.NavigateBack)
        },
        containerColor = KptTheme.colorScheme.background,
    ) { paddingValues ->
        // Template idiom: `ScreenContent` (core-base/ui) owns every render
        // branch — loading / empty / no-network / unauthenticated / error+retry —
        // driven by the stream's pre-decided `ScreenState`. Only the Content
        // body (the profile detail region) is authored here.
        ScreenContent(
            state = clientState,
            onRetry = onRetry,
            modifier = Modifier.fillMaxSize().padding(paddingValues),
        ) { client, _ ->
            ProfileScreenContent(
                client = client,
                clientImage = state.clientImage,
                onAction = onAction,
                modifier = Modifier,
            )
        }
    }
}

@Composable
private fun ProfileScreenContent(
    client: Client,
    clientImage: String?,
    modifier: Modifier = Modifier,
    onAction: (ProfileAction) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = KptTheme.spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
    ) {
        ProfileImage(bitmap = clientImage)

        ProfileDetailsCard(
            client = client,
            modifier = Modifier.fillMaxWidth(),
        )

        ForkButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            text = {
                Text(
                    text = stringResource(Res.string.feature_profile_personal_qr_code),
                )
            },
            onClick = {
                onAction(ProfileAction.ShowPersonalQRCode)
            },
            leadingIcon = {
                Icon(
                    imageVector = ForkIcons.QrCode,
                    contentDescription = stringResource(Res.string.feature_profile_personal_qr_code),
                )
            },
        )

//        TODO uncomment this after migrating to self api to link savings account
//        ForkButton(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(55.dp),
//            text = {
//                Text(
//                    text = stringResource(Res.string.feature_profile_link_bank_account),
//                )
//            },
//            onClick = {
//                onAction(ProfileAction.NavigateToLinkBankAccount)
//            },
//            leadingIcon = {
//                Icon(imageVector = ForkIcons.AttachMoney, contentDescription = "")
//            },
//        )

        Spacer(modifier = Modifier.height(1.dp))
    }
}

@Composable
private fun ProfileDialogs(
    dialogState: ProfileState.DialogState?,
    onDismissRequest: () -> Unit,
) {
    when (dialogState) {
        is ProfileState.DialogState.Error -> ForkBasicDialog(
            visibilityState = BasicDialogState.Shown(
                message = stringResource(dialogState.message),
            ),
            onDismissRequest = onDismissRequest,
        )

        is ProfileState.DialogState.Loading -> ForkLoadingDialog(
            visibilityState = LoadingDialogState.Shown,
        )

        null -> Unit
    }
}
