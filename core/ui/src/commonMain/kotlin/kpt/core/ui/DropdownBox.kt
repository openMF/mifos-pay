/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.ui

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.PopupProperties
import kpt.core.designsystem.component.MifosTextField
/**
 * Prefer to use when text field is not read only
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownBox(
    expanded: Boolean,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    showClearIcon: Boolean = false,
    isError: Boolean = false,
    errorText: String? = null,
    onValueChange: (String) -> Unit = {},
    onExpandChange: (Boolean) -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    var textFieldSize by remember { mutableStateOf(Size.Zero) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandChange,
        modifier = modifier,
    ) {
        MifosTextField(
            label = label,
            value = value,
            showClearIcon = showClearIcon,
            readOnly = readOnly,
            isError = isError,
            errorText = errorText,
            onValueChange = onValueChange,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded,
                )
            },
            modifier = modifier
                .onGloballyPositioned { coordinates ->
                    textFieldSize = coordinates.size.toSize()
                }
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                onExpandChange(false)
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
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExposedDropdownBox(
    expanded: Boolean,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    readOnly: Boolean = true,
    showClearIcon: Boolean = false,
    isError: Boolean = false,
    errorText: String? = null,
    onValueChange: (String) -> Unit = {},
    onExpandChange: (Boolean) -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandChange,
        modifier = modifier,
    ) {
        MifosTextField(
            label = label,
            value = value,
            showClearIcon = showClearIcon,
            readOnly = readOnly,
            isError = isError,
            errorText = errorText,
            onValueChange = onValueChange,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded,
                )
            },
            modifier = modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                onExpandChange(false)
            },
            modifier = Modifier
                .heightIn(max = 200.dp),
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownBoxItem(
    text: String,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = { Text(text) },
        onClick = onClick,
        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
    )
}
