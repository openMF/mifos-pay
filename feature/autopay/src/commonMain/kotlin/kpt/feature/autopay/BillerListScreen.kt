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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.designsystem.component.MifosButton
import kpt.core.designsystem.component.MifosScaffold
import kpt.core.designsystem.component.MifosTopBar
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.model.autopay.Biller
import kpt.core.ui.utils.EventsEffect
import org.koin.compose.viewmodel.koinViewModel
/** Screen listing saved billers with search, add, edit and delete actions. */
@Composable
fun BillerListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddBiller: () -> Unit,
    onNavigateToEditBiller: (String) -> Unit,
    viewModel: BillerListViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var showSearchBar by remember { mutableStateOf(false) }

    EventsEffect(viewModel) { event ->
        when (event) {
            is BillerListEvent.NavigateToAddBiller -> onNavigateToAddBiller()
            is BillerListEvent.NavigateToEditBiller -> onNavigateToEditBiller(event.billerId)
            is BillerListEvent.BillerDeleted -> {
                // Biller deleted successfully, no action needed
            }
        }
    }

    MifosScaffold(
        topBar = {
            MifosTopBar(
                topBarTitle = "My Billers",
                backPress = onNavigateBack,
                actions = {
                    IconButton(
                        onClick = { showSearchBar = !showSearchBar },
                    ) {
                        Icon(
                            imageVector = MifosIcons.Search,
                            contentDescription = "Search billers",
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            ) {
                if (showSearchBar) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            viewModel.trySendAction(BillerListAction.SearchBillers(it))
                        },
                        placeholder = { Text("Search billers...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (state.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (state.billers.isEmpty()) {
                    EmptyStateCard(
                        title = "No Billers Found",
                        description = "You don't have any billers yet. Add your first biller to get started!",
                        icon = MifosIcons.Person,
                        onAddBiller = { viewModel.trySendAction(BillerListAction.AddNewBiller) },
                    )
                } else {
                    BillerList(
                        billers = state.billers,
                        onEditBiller = { billerId ->
                            viewModel.trySendAction(BillerListAction.EditBiller(billerId))
                        },
                        onDeleteBiller = { billerId ->
                            viewModel.trySendAction(BillerListAction.DeleteBiller(billerId))
                        },
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Spacer(modifier = Modifier.weight(1f))

                    FloatingActionButton(
                        onClick = { viewModel.trySendAction(BillerListAction.AddNewBiller) },
                        modifier = Modifier.padding(bottom = 16.dp),
                    ) {
                        Icon(
                            imageVector = MifosIcons.Add,
                            contentDescription = "Add biller",
                        )
                    }
                }

                MifosButton(
                    text = { Text("Back to AutoPay") },
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun EmptyStateCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onAddBiller: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onAddBiller,
            ) {
                Icon(
                    imageVector = MifosIcons.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Biller")
            }
        }
    }
}

@Composable
private fun BillerList(
    billers: List<Biller>,
    onEditBiller: (String) -> Unit,
    onDeleteBiller: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 100.dp),
    ) {
        items(
            items = billers,
            key = { it.id ?: it.name },
        ) { biller ->
            BillerCard(
                biller = biller,
                onEdit = { onEditBiller(biller.id ?: "") },
                onDelete = { onDeleteBiller(biller.id ?: "") },
            )
        }
    }
}

@Composable
private fun BillerCard(
    biller: Biller,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = biller.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Account: ${biller.accountNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = biller.category.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Box {
                IconButton(
                    onClick = { showMenu = true },
                ) {
                    Icon(
                        imageVector = MifosIcons.MoreVert,
                        contentDescription = "More options",
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        leadingIcon = {
                            Icon(
                                imageVector = MifosIcons.Edit,
                                contentDescription = null,
                            )
                        },
                        onClick = {
                            showMenu = false
                            onEdit()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        leadingIcon = {
                            Icon(
                                imageVector = MifosIcons.Delete,
                                contentDescription = null,
                            )
                        },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                    )
                }
            }
        }
    }
}
