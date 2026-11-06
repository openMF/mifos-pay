/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.kyc

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ImageLoader
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import coil3.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.component.BasicDialogState
import kpt.core.designsystem.component.LoadingDialogState
import kpt.core.designsystem.component.MifosBasicDialog
import kpt.core.designsystem.component.MifosButton
import kpt.core.designsystem.component.MifosLoadingDialog
import kpt.core.designsystem.component.MifosScaffold
import kpt.core.designsystem.component.MifosTextField
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.ui.AvatarBox
import kpt.core.ui.MifosProgressIndicator
import kpt.core.ui.utils.EventsEffect
import mifos_pay.feature.kyc.generated.resources.Res
import mifos_pay.feature.kyc.generated.resources.feature_kyc_file_name
import mifos_pay.feature.kyc.generated.resources.feature_kyc_submit
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
@Composable
internal fun KYCLevel2Screen(
    navigateBack: () -> Unit,
    navigateToLevel3: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: KYCLevel2ViewModel = koinViewModel(),
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
            is KycLevel2Event.OnNavigateBack -> navigateBack.invoke()
            KycLevel2Event.OnNavigateToLevel3 -> navigateToLevel3.invoke()
            is KycLevel2Event.ShowToast -> {
                scope.launch {
                    snackbarHostState.showSnackbar(getString(event.message))
                }
            }
        }
    }

    KycLevel2Dialogs(
        dialogState = state.dialogState,
        onDismissRequest = remember(viewModel) {
            { viewModel.trySendAction(KycLevel2Action.DismissDialog) }
        },
    )

    KYCLevel2ScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        modifier = modifier.fillMaxSize(),
        onAction = remember(viewModel) {
            { viewModel.trySendAction(it) }
        },
    )
}

@Composable
internal fun KYCLevel2ScreenContent(
    state: KycLevel2State,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    onAction: (KycLevel2Action) -> Unit,
) {
    MifosScaffold(
        topBarTitle = "Upload Documents",
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
        backPress = {
            onAction(KycLevel2Action.NavigateBack)
        },
    ) { paddingValues ->
        KYCLevel2ScreenContent(
            state = state,
            onAction = onAction,
            modifier = Modifier.padding(paddingValues),
        )
    }
}

@Composable
private fun KYCLevel2ScreenContent(
    state: KycLevel2State,
    modifier: Modifier = Modifier,
    onAction: (KycLevel2Action) -> Unit,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(KptTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(
            KptTheme.spacing.md,
            Alignment.CenterVertically,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        DocumentPicker(
            uploadedFile = state.uploadedFile,
            onPickFile = {
                onAction(KycLevel2Action.PickFile)
            },
        )

        MifosTextField(
            value = state.name,
            label = stringResource(Res.string.feature_kyc_file_name),
            onValueChange = {
                onAction(KycLevel2Action.NameChanged(it))
            },
            onClickClearIcon = {
                onAction(KycLevel2Action.NameChanged(""))
            },
        )

        MifosTextField(
            value = state.description,
            label = "Description",
            onValueChange = {
                onAction(KycLevel2Action.DescriptionChanged(it))
            },
            onClickClearIcon = {
                onAction(KycLevel2Action.DescriptionChanged(""))
            },
        )

        MifosButton(
            onClick = {
                onAction(KycLevel2Action.SubmitClicked)
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(Res.string.feature_kyc_submit))
        }
    }
}

@Composable
private fun DocumentPicker(
    modifier: Modifier = Modifier,
    uploadedFile: ByteArray?,
    onPickFile: () -> Unit,
) {
    val context = LocalPlatformContext.current

    val painter = rememberAsyncImagePainter(
        model = uploadedFile,
        imageLoader = ImageLoader(context),
    )

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = KptTheme.shapes.small,
        border = CardDefaults.outlinedCardBorder(
            enabled = true,
        ).copy(
            width = 1.dp,
            brush = Brush.sweepGradient(
                colors = listOf(
                    KptTheme.colorScheme.primary,
                    KptTheme.colorScheme.secondary,
                ),
            ),
        ),
        onClick = onPickFile,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            SubcomposeAsyncImage(
                model = uploadedFile,
                imageLoader = ImageLoader(context),
                contentScale = ContentScale.None,
                contentDescription = "Uploaded Image",
                modifier = Modifier.align(Alignment.Center),
            ) {
                val painterState by painter.state.collectAsStateWithLifecycle()

                when (painterState) {
                    is AsyncImagePainter.State.Empty -> {
                        AvatarBox(
                            icon = MifosIcons.Add,
                            size = 120,
                            contentColor = KptTheme.colorScheme.secondary,
                        )
                    }

                    is AsyncImagePainter.State.Error -> {
                        AvatarBox(
                            icon = MifosIcons.Add,
                            size = 120,
                            contentColor = KptTheme.colorScheme.secondary,
                        )
                    }

                    is AsyncImagePainter.State.Loading -> MifosProgressIndicator()

                    is AsyncImagePainter.State.Success -> {
                        SubcomposeAsyncImageContent(
                            contentScale = ContentScale.Fit,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KycLevel2Dialogs(
    dialogState: KycLevel2State.DialogState?,
    onDismissRequest: () -> Unit,
) {
    when (dialogState) {
        is KycLevel2State.DialogState.Error -> MifosBasicDialog(
            visibilityState = BasicDialogState.Shown(
                message = dialogState.message,
            ),
            onDismissRequest = onDismissRequest,
        )

        is KycLevel2State.DialogState.Loading -> MifosLoadingDialog(
            visibilityState = LoadingDialogState.Shown,
        )

        null -> Unit
    }
}
