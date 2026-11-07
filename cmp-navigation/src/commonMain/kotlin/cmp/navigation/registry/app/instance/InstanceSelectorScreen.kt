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

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cmp.navigation.generated.resources.Res
import cmp.navigation.generated.resources.default
import cmp.navigation.generated.resources.error_message
import cmp.navigation.generated.resources.interbank_server
import cmp.navigation.generated.resources.no_interbank_servers
import cmp.navigation.generated.resources.select_instance
import cmp.navigation.generated.resources.tenant_value
import cmp.navigation.generated.resources.update
import kpt.core.base.designsystem.KptTheme
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.fork.component.ForkBottomSheet
import kpt.core.designsystem.fork.component.ForkButton
import kpt.core.designsystem.fork.component.ForkCard
import kpt.core.designsystem.fork.component.ForkScaffold
import kpt.core.designsystem.fork.icon.ForkIcons
import kpt.core.designsystem.theme.fork.ForkTheme
import kpt.core.model.instance.InterbankServer
import kpt.core.model.instance.ServerInstance
import kpt.core.ui.ForkProgressIndicator
import kpt.core.ui.fork.utils.EventsEffect
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
/** Bottom-sheet screen for choosing the main and inter-bank server instances; [onDismiss] closes it. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstanceSelectorScreen(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InstanceSelectorViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    // Handle events
    EventsEffect(viewModel = viewModel) { event ->
        when (event) {
            is InstanceSelectorEvent.DismissSheet -> onDismiss()
        }
    }

    ForkBottomSheet(
        onDismiss = onDismiss,
        modifier = modifier,
    ) {
        ForkScaffold(
            modifier = modifier,
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(KptTheme.spacing.md),
                    verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
                ) {
                    ForkButton(
                        onClick = {
                            viewModel.trySendAction(InstanceSelectorAction.UpdateInstances)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.isUpdateEnabled,
                    ) {
                        Text(stringResource(Res.string.update))
                    }
                }
            },
        ) { contentPadding ->
            when {
                state.isLoading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        ForkProgressIndicator()
                    }
                }

                state.error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = stringResource(Res.string.error_message, state.error ?: ""),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }

                else -> {
                    InstancesList(
                        modifier = Modifier.padding(contentPadding),
                        instances = state.instances,
                        selectedMainInstance = state.tempSelectedMainInstance,
                        selectedInterbankInstance = state.tempSelectedInterbankInstance,
                        onMainInstanceSelected = { instance ->
                            viewModel.trySendAction(
                                InstanceSelectorAction.SelectMainInstance(instance),
                            )
                        },
                        onInterbankInstanceSelected = { instance ->
                            viewModel.trySendAction(
                                InstanceSelectorAction.SelectInterbankInstance(instance),
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun InstancesList(
    instances: List<ServerInstance>,
    selectedMainInstance: ServerInstance?,
    selectedInterbankInstance: InterbankServer?,
    onMainInstanceSelected: (ServerInstance) -> Unit,
    onInterbankInstanceSelected: (InterbankServer) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            // Header with drag handle
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(Res.string.select_instance),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
            }
        }

        items(instances) { instance ->
            val isMainSelected = instance == selectedMainInstance

            MainInstanceItem(
                instance = instance,
                isSelected = isMainSelected,
                selectedInterbankInstance = selectedInterbankInstance,
                onMainInstanceClick = { onMainInstanceSelected(instance) },
                onInterbankInstanceClick = { interbankServer ->
                    onInterbankInstanceSelected(interbankServer)
                },
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MainInstanceItem(
    instance: ServerInstance,
    isSelected: Boolean,
    selectedInterbankInstance: InterbankServer?,
    onMainInstanceClick: () -> Unit,
    onInterbankInstanceClick: (InterbankServer) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ForkCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onMainInstanceClick),
            colors = CardDefaults.cardColors(
                containerColor = KptTheme.colorScheme.surface,
            ),
            shape = KptTheme.shapes.medium,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = onMainInstanceClick,
                    modifier = Modifier.padding(0.dp),
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    // Instance Label
                    Text(
                        text = instance.label,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    // Badges Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (instance.isDefault) {
                            Text(
                                text = stringResource(Res.string.default),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                ),
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.tertiaryContainer,
                                        shape = RoundedCornerShape(4.dp),
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }

                    // Endpoint URL
                    Text(
                        text = instance.endpoint,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    // Tenant ID
                    Text(
                        text = stringResource(Res.string.tenant_value, instance.platformTenantId),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        // Show interbank servers when main instance is selected
        if (isSelected) {
            InterbankServersList(
                interbankServers = instance.interbankServers,
                selectedInterbankInstance = selectedInterbankInstance,
                onInterbankInstanceClick = onInterbankInstanceClick,
            )
        }
    }
}

@Composable
private fun InterbankServersList(
    interbankServers: List<InterbankServer>,
    selectedInterbankInstance: InterbankServer?,
    onInterbankInstanceClick: (InterbankServer) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(Res.string.interbank_server),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (interbankServers.isEmpty()) {
            Text(
                text = stringResource(Res.string.no_interbank_servers),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 8.dp),
            )
        } else {
            interbankServers.forEach { interbankServer ->
                InterbankServerItem(
                    interbankServer = interbankServer,
                    isSelected = interbankServer == selectedInterbankInstance,
                    onClick = { onInterbankInstanceClick(interbankServer) },
                )
            }
        }
    }
}

@Composable
private fun InterbankServerItem(
    interbankServer: InterbankServer,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val animatedContainerColor = animateColorAsState(
        targetValue = if (isSelected) {
            KptTheme.colorScheme.secondaryContainer
        } else {
            KptTheme.colorScheme.surfaceContainerLow
        },
        animationSpec = tween(durationMillis = 200),
        label = "containerColor",
    )

    val animatedBorderColor = animateColorAsState(
        targetValue = if (isSelected) {
            KptTheme.colorScheme.primary
        } else {
            KptTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        },
        animationSpec = tween(durationMillis = 200),
        label = "borderColor",
    )

    val animatedIconBackground = animateColorAsState(
        targetValue = if (isSelected) {
            KptTheme.colorScheme.primary
        } else {
            KptTheme.colorScheme.surfaceContainerHighest
        },
        animationSpec = tween(durationMillis = 200),
        label = "iconBackground",
    )

    val animatedIconTint = animateColorAsState(
        targetValue = if (isSelected) {
            KptTheme.colorScheme.onPrimary
        } else {
            KptTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(durationMillis = 200),
        label = "iconTint",
    )

    ForkCard(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = animatedBorderColor.value,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = animatedContainerColor.value,
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Icon with animated background
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = animatedIconBackground.value,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = ForkIcons.Bank,
                    contentDescription = null,
                    tint = animatedIconTint.value,
                    modifier = Modifier.size(20.dp),
                )
            }

            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = interbankServer.label,
                        style = KptTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = if (isSelected) {
                            KptTheme.colorScheme.onSecondaryContainer
                        } else {
                            KptTheme.colorScheme.onSurface
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )

                    if (interbankServer.isDefault) {
                        Text(
                            text = stringResource(Res.string.default),
                            style = KptTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = KptTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier
                                .background(
                                    color = KptTheme.colorScheme.tertiaryContainer,
                                    shape = RoundedCornerShape(6.dp),
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }

                Text(
                    text = interbankServer.endpoint,
                    style = KptTheme.typography.labelMedium,
                    color = if (isSelected) {
                        KptTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    } else {
                        KptTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Radio button
            RadioButton(
                selected = isSelected,
                onClick = onClick,
            )
        }
    }
}

@Preview
@Composable
private fun MainInstanceItemPreview() {
    KptTheme {
        MainInstanceItem(
            instance = ServerInstance(
                endpoint = "mifos-bank-2.mifos.community",
                protocol = "https://",
                path = "/fineract-provider/api/v1/",
                platformTenantId = "mifos-bank-2",
                label = "Mifos Bank 2 Instance",
                isDefault = true,
                interbankServers = listOf(
                    InterbankServer(
                        endpoint = "apis.flexcore.mx",
                        protocol = "https://",
                        path = "/v1.0/vnext2/",
                        label = "Mifos Bank 2 Interbank",
                        isDefault = true,
                    ),
                ),
            ),
            isSelected = true,
            selectedInterbankInstance = InterbankServer(
                endpoint = "apis.flexcore.mx",
                protocol = "https://",
                path = "/v1.0/vnext2/",
                label = "Mifos Bank 2 Interbank",
                isDefault = true,
            ),
            onMainInstanceClick = {},
            onInterbankInstanceClick = {},
        )
    }
}

@Preview
@Composable
private fun MainInstanceItemUnselectedPreview() {
    ForkTheme {
        MainInstanceItem(
            instance = ServerInstance(
                endpoint = "venus.mifos.community",
                protocol = "https://",
                path = "/fineract-provider/api/v1/",
                platformTenantId = "venus",
                label = "Venus Instance",
                isDefault = false,
                interbankServers = emptyList(),
            ),
            isSelected = false,
            selectedInterbankInstance = null,
            onMainInstanceClick = {},
            onInterbankInstanceClick = {},
        )
    }
}

@Preview
@Composable
private fun InterbankServerItemSelectedPreview() {
    KptTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            InterbankServerItem(
                interbankServer = InterbankServer(
                    endpoint = "apis.flexcore.mx",
                    protocol = "https://",
                    path = "/v1.0/vnext2/",
                    label = "Mifos Bank 2 Interbank",
                    isDefault = true,
                ),
                isSelected = true,
                onClick = {},
            )
            InterbankServerItem(
                interbankServer = InterbankServer(
                    endpoint = "apis.flexcore.mx",
                    protocol = "https://",
                    path = "/v1.0/vnext1/",
                    label = "Mifos Bank 1 Interbank",
                    isDefault = false,
                ),
                isSelected = false,
                onClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun InstancesListPreview() {
    ForkTheme {
        InstancesList(
            instances = listOf(
                ServerInstance(
                    endpoint = "mifos-bank-2.mifos.community",
                    protocol = "https://",
                    path = "/fineract-provider/api/v1/",
                    platformTenantId = "mifos-bank-2",
                    label = "Mifos Bank 2 Instance",
                    isDefault = true,
                    interbankServers = listOf(
                        InterbankServer(
                            endpoint = "apis.flexcore.mx",
                            protocol = "https://",
                            path = "/v1.0/vnext2/",
                            label = "Mifos Bank 2 Interbank",
                            isDefault = true,
                        ),
                    ),
                ),
                ServerInstance(
                    endpoint = "mifos-bank-1.mifos.community",
                    protocol = "https://",
                    path = "/fineract-provider/api/v1/",
                    platformTenantId = "mifos-bank-1",
                    label = "Mifos Bank 1 Instance",
                    isDefault = false,
                    interbankServers = listOf(
                        InterbankServer(
                            endpoint = "apis.flexcore.mx",
                            protocol = "https://",
                            path = "/v1.0/vnext1/",
                            label = "Mifos Bank 1 Interbank",
                            isDefault = true,
                        ),
                    ),
                ),
                ServerInstance(
                    endpoint = "venus.mifos.community",
                    protocol = "https://",
                    path = "/fineract-provider/api/v1/",
                    platformTenantId = "venus",
                    label = "Venus Instance",
                    isDefault = false,
                    interbankServers = emptyList(),
                ),
            ),
            selectedMainInstance = ServerInstance(
                endpoint = "mifos-bank-2.mifos.community",
                protocol = "https://",
                path = "/fineract-provider/api/v1/",
                platformTenantId = "mifos-bank-2",
                label = "Mifos Bank 2 Instance",
                isDefault = true,
                interbankServers = listOf(
                    InterbankServer(
                        endpoint = "apis.flexcore.mx",
                        protocol = "https://",
                        path = "/v1.0/vnext2/",
                        label = "Mifos Bank 2 Interbank",
                        isDefault = true,
                    ),
                ),
            ),
            selectedInterbankInstance = InterbankServer(
                endpoint = "apis.flexcore.mx",
                protocol = "https://",
                path = "/v1.0/vnext2/",
                label = "Mifos Bank 2 Interbank",
                isDefault = true,
            ),
            onMainInstanceSelected = {},
            onInterbankInstanceSelected = {},
        )
    }
}
