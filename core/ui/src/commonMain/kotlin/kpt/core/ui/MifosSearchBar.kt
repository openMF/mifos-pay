/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.KptTheme
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.icon.MifosIcons
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MifosSearchBar(
    query: String,
    placeHolder: String,
    showClearButton: Boolean = true,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onClearQuery: () -> Unit = {},
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    SearchBar(
        modifier = modifier.fillMaxWidth(),
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = onSearch,
                expanded = false,
                onExpandedChange = {},
                enabled = enabled,
                placeholder = { Text(text = placeHolder) },
                leadingIcon = {
                    Icon(
                        imageVector = MifosIcons.Search,
                        contentDescription = null,
                    )
                },
                trailingIcon = {
                    if (showClearButton && query.isNotEmpty()) {
                        IconButton(onClick = onClearQuery) {
                            Icon(
                                imageVector = MifosIcons.Close,
                                contentDescription = null,
                            )
                        }
                    }
                },
                interactionSource = null,
            )
        },
        expanded = false,
        onExpandedChange = {},
        shape = SearchBarDefaults.inputFieldShape,
        colors = SearchBarDefaults.colors(),
        tonalElevation = SearchBarDefaults.TonalElevation,
        shadowElevation = SearchBarDefaults.ShadowElevation,
        windowInsets = SearchBarDefaults.windowInsets,
        content = {},
    )
}

@Composable
fun SimpleSearchBar(
    query: String,
    placeHolder: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    onClearQuery: () -> Unit = {},
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp).clickable {
                onClick()
            },
        placeholder = {
            Text(
                text = placeHolder,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = MifosIcons.Search,
                contentDescription = "Search",
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClearQuery) {
                    Icon(
                        imageVector = MifosIcons.Close,
                        contentDescription = "Clear",
                    )
                }
            }
        },
        shape = KptTheme.shapes.small,
        colors = TextFieldDefaults.colors(
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
            unfocusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        ),
        enabled = enabled,
        textStyle = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
@DevicePreviews
fun PreviewMifosSearchBar() {
    KptTheme {
        MifosSearchBar(
            query = "Hello",
            placeHolder = "Search...",
            onQueryChange = {},
            onSearch = {},
            onClearQuery = {},
        )
    }
}

@Composable
@DevicePreviews
fun PreviewSimpleSearchBar() {
    KptTheme {
        SimpleSearchBar(
            query = "",
            placeHolder = "Search here...",
            onQueryChange = {},
            onClearQuery = {},
        )
    }
}
