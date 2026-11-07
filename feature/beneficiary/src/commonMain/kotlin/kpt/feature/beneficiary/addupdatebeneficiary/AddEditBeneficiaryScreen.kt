/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.beneficiary.addupdatebeneficiary

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import kpt.core.model.office.Office
import kpt.core.model.utils.Locale
import kpt.core.model.utils.filterLocales
import kpt.core.ui.MifosDivider
import kpt.core.ui.utils.EventsEffect
import kpt.feature.beneficiary.generated.resources.Res
import kpt.feature.beneficiary.generated.resources.feature_beneficiary_account_no
import kpt.feature.beneficiary.generated.resources.feature_beneficiary_account_type
import kpt.feature.beneficiary.generated.resources.feature_beneficiary_locale
import kpt.feature.beneficiary.generated.resources.feature_beneficiary_nickname
import kpt.feature.beneficiary.generated.resources.feature_beneficiary_office_name
import kpt.feature.beneficiary.generated.resources.feature_beneficiary_transfer_limit
import kpt.feature.beneficiary.generated.resources.scan_qr_code
import kpt.feature.beneficiary.generated.resources.skip_the_form
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
@Composable
internal fun AddEditBeneficiaryScreen(
    navigateBack: () -> Unit,
    navigateToQrReaderScreen: () -> Unit,
    navigateToIntraBankTransfer: (
        officeId: Int,
        clientId: Long,
        accountTypeId: Int,
        accountId: Int,
        amount: Int,
        accountName: String,
        accountNo: String,
    ) -> Unit,
    navigateToInterbankTransfer: (accountNumber: String, recipientName: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddEditBeneficiaryViewModel = koinViewModel(),
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val localeList by viewModel.filteredLocalList.collectAsStateWithLifecycle()
    val officeList by viewModel.officeList.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
            is AEBEvent.NavigateBack -> navigateBack.invoke()

            is AEBEvent.NavigateToQr -> navigateToQrReaderScreen.invoke()

            is AEBEvent.ShowToast -> {
                scope.launch {
                    snackbarHostState.showSnackbar(getString(event.message))
                }
            }

            is AEBEvent.NavigateToIntraBankTransfer -> {
                navigateToIntraBankTransfer(
                    event.officeId,
                    event.clientId,
                    event.accountTypeId,
                    event.accountId,
                    event.amount,
                    event.accountName,
                    event.accountNo,
                )
            }

            is AEBEvent.NavigateToInterbankTransfer -> {
                navigateToInterbankTransfer(event.accountNumber, event.recipientName)
            }
        }
    }

    BeneficiaryDialogs(
        dialogState = state.dialogState,
        onDismissRequest = remember(viewModel) {
            { viewModel.trySendAction(AEBAction.DismissDialog) }
        },
    )

    AddEditBeneficiaryScreenContent(
        state = state,
        localeList = localeList,
        officeList = officeList,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
        onAction = remember(viewModel) {
            { viewModel.trySendAction(it) }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddEditBeneficiaryScreenContent(
    state: AEBState,
    localeList: List<Locale>,
    officeList: List<Office>,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    onAction: (AEBAction) -> Unit,
) {
    MifosScaffold(
        topBarTitle = stringResource(state.title),
        backPress = { onAction(AEBAction.NavigateBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(KptTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                MifosTextField(
                    label = stringResource(Res.string.feature_beneficiary_nickname),
                    value = state.name,
                    onValueChange = {
                        onAction(AEBAction.ChangeName(it))
                    },
                )
            }

            item {
                MifosTextField(
                    label = stringResource(Res.string.feature_beneficiary_account_no),
                    value = state.accountNumber,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                    ),
                    onValueChange = {
                        onAction(AEBAction.ChangeAccountNumber(it))
                    },
                )
            }

            item {
                MifosTextField(
                    label = stringResource(Res.string.feature_beneficiary_transfer_limit),
                    value = state.transferLimit.toString(),
                    onValueChange = {
                        onAction(AEBAction.ChangeTransferLimit(it))
                    },
                    onClickClearIcon = {
                        onAction(AEBAction.ChangeTransferLimit(""))
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                    ),
                )
            }

            item {
                val filteredLocalList by remember(localeList, state.locale) {
                    derivedStateOf {
                        localeList.filterLocales(state.locale)
                    }
                }

                var textFieldSize by remember { mutableStateOf(Size.Zero) }
                var localeToggled by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = localeToggled && localeList.isNotEmpty(),
                    onExpandedChange = {
                        localeToggled = !localeToggled
                    },
                ) {
                    MifosTextField(
                        label = stringResource(Res.string.feature_beneficiary_locale),
                        value = state.locale,
                        onValueChange = {
                            localeToggled = true
                            onAction(AEBAction.ChangeLocale(it))
                        },
                        onClickClearIcon = {
                            onAction(AEBAction.ChangeLocale(""))
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = localeToggled,
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                textFieldSize = coordinates.size.toSize()
                            }
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    )

                    DropdownMenu(
                        expanded = localeToggled,
                        onDismissRequest = {
                            localeToggled = false
                        },
                        properties = PopupProperties(
                            focusable = false,
                            dismissOnBackPress = true,
                            dismissOnClickOutside = true,
                            clippingEnabled = true,
                        ),
                        modifier = Modifier
                            .width(with(LocalDensity.current) { textFieldSize.width.toDp() })
                            .heightIn(max = 200.dp),
                    ) {
                        filteredLocalList.forEachIndexed { index, locale ->
                            DropdownMenuItem(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                onClick = {
                                    onAction(AEBAction.ChangeLocale(locale.localName))
                                    localeToggled = false
                                },
                                text = {
                                    Text(
                                        text = locale.countryName,
                                    )
                                },
                            )

                            if (index != filteredLocalList.size - 1) {
                                MifosDivider()
                            }
                        }
                    }
                }
            }

            item {
                val filteredOfficeList by remember(officeList, state.officeName) {
                    derivedStateOf {
                        officeList.filter { office ->
                            office.name.contains(state.officeName, ignoreCase = true)
                        }
                    }
                }

                MifosDropdownMenu(
                    label = stringResource(Res.string.feature_beneficiary_office_name),
                    selectedValue = state.officeName,
                    items = filteredOfficeList,
                    onItemSelected = { office ->
                        onAction(AEBAction.ChangeOffice(office))
                    },
                    onValueChange = { value ->
                        onAction(AEBAction.ChangeOfficeName(value))
                    },
                    onClearClick = {
                        onAction(AEBAction.ChangeOfficeName(""))
                    },
                    itemToString = { office -> office.name },
                )
            }

            item {
                MifosTextField(
                    label = stringResource(Res.string.feature_beneficiary_account_type),
                    value = stringResource(state.accountTypeName),
                    readOnly = true,
                    showClearIcon = false,
                    onValueChange = {
                        onAction(AEBAction.ChangeAccountType(it.toInt()))
                    },
                )
            }

            item {
                MifosButton(
                    text = {
                        Text(text = stringResource(state.btnText))
                    },
                    onClick = {
                        onAction(AEBAction.SaveBeneficiary)
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(KptTheme.spacing.md))

                OutlinedCard(
                    onClick = { onAction(AEBAction.OnQrScanClicked) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = KptTheme.colorScheme.surfaceVariant,
                    ),
                    border = BorderStroke(1.dp, KptTheme.colorScheme.primary),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(KptTheme.spacing.md),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = MifosIcons.Scan,
                            contentDescription = null,
                            tint = KptTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.width(KptTheme.spacing.sm))
                        Column {
                            Text(
                                text = stringResource(Res.string.scan_qr_code),
                                style = KptTheme.typography.titleMedium,
                                color = KptTheme.colorScheme.primary,
                            )
                            Text(
                                text = stringResource(Res.string.skip_the_form),
                                style = KptTheme.typography.bodySmall,
                                color = KptTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BeneficiaryDialogs(
    dialogState: AEBState.DialogState?,
    onDismissRequest: () -> Unit,
) {
    when (dialogState) {
        is AEBState.DialogState.Error.StringMessage -> MifosBasicDialog(
            visibilityState = BasicDialogState.Shown(
                message = dialogState.message,
            ),
            onDismissRequest = onDismissRequest,
        )

        is AEBState.DialogState.Loading -> MifosLoadingDialog(
            visibilityState = LoadingDialogState.Shown,
        )

        is AEBState.DialogState.Error.ResourceMessage -> MifosBasicDialog(
            visibilityState = BasicDialogState.Shown(
                message = stringResource(dialogState.message),
            ),
            onDismissRequest = onDismissRequest,
        )

        null -> Unit
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun <T> MifosDropdownMenu(
    label: String,
    selectedValue: String,
    items: List<T>,
    onItemSelected: (T) -> Unit,
    onValueChange: (String) -> Unit,
    onClearClick: () -> Unit,
    itemToString: (T) -> String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var textFieldSize by remember { mutableStateOf(Size.Zero) }
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded && items.isNotEmpty(),
        onExpandedChange = {
            if (enabled) {
                expanded = !expanded
            }
        },
        modifier = modifier,
    ) {
        MifosTextField(
            label = label,
            value = selectedValue,
            onValueChange = {
                expanded = true
                onValueChange(it)
            },
            onClickClearIcon = onClearClick,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded,
                )
            },
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    textFieldSize = coordinates.size.toSize()
                }
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )

        DropdownMenu(
            expanded = expanded && items.isNotEmpty(),
            onDismissRequest = {
                expanded = false
            },
            properties = PopupProperties(
                focusable = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                clippingEnabled = true,
            ),
            modifier = Modifier
                .width(with(LocalDensity.current) { textFieldSize.width.toDp() })
                .heightIn(max = 200.dp),
        ) {
            items.forEachIndexed { index, item ->
                DropdownMenuItem(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    },
                    text = {
                        Text(text = itemToString(item))
                    },
                )

                if (index != items.size - 1) {
                    MifosDivider()
                }
            }
        }
    }
}
