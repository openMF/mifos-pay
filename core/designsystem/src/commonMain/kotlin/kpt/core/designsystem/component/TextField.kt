/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.designsystem.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.icon.MifosIcons
@Composable
fun MifosOutlinedTextField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
    singleLine: Boolean = false,
    showClearIcon: Boolean = true,
    readOnly: Boolean = false,
    clearIcon: ImageVector = MifosIcons.Close,
    onClickClearIcon: () -> Unit = { onValueChange("") },
    onKeyboardActions: (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val isFocused by interactionSource.collectIsFocusedAsState()
    val showIcon by rememberUpdatedState(value.isNotEmpty())

    MifosCustomTextField(
        modifier = modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        label = label,
        isError = isError,
        readOnly = readOnly,
        supportingText = errorMessage?.let {
            {
                Text(text = errorMessage)
            }
        },
        singleLine = singleLine,
        leadingIcon = leadingIcon,
        trailingIcon = @Composable {
            AnimatedContent(
                targetState = showClearIcon && isFocused && showIcon,
            ) {
                if (it) {
                    ClearIconButton(
                        showClearIcon = true,
                        clearIcon = clearIcon,
                        onClickClearIcon = onClickClearIcon,
                    )
                } else {
                    trailingIcon?.invoke()
                }
            }
        },
        keyboardActions = KeyboardActions {
            onKeyboardActions?.invoke()
        },
        keyboardOptions = keyboardOptions,
        interactionSource = interactionSource,
        textStyle = LocalDensity.current.run {
            TextStyle(color = KptTheme.colorScheme.onSurface)
        },
        placeholder = placeholder,
    )
}

@Composable
fun MifosTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showClearIcon: Boolean = true,
    readOnly: Boolean = false,
    clearIcon: ImageVector = MifosIcons.Close,
    isError: Boolean = false,
    errorText: String? = null,
    onClickClearIcon: () -> Unit = { onValueChange("") },
    visualTransformation: VisualTransformation = VisualTransformation.None,
    textStyle: TextStyle? = null,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.Companion.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    placeholder: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
    trailingIcon: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    onFocusChanged: (Boolean) -> Unit = {},
) {
    val isFocused by interactionSource.collectIsFocusedAsState()
    val showIcon by rememberUpdatedState(value.isNotEmpty())

    LaunchedEffect(isFocused) {
        onFocusChanged(isFocused)
    }

    MifosCustomTextField(
        value = value,
        label = label,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        readOnly = readOnly,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        interactionSource = interactionSource,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        leadingIcon = leadingIcon,
        isError = isError,
        trailingIcon = @Composable {
            AnimatedContent(
                targetState = showClearIcon && isFocused && showIcon,
            ) {
                if (it) {
                    ClearIconButton(
                        showClearIcon = true,
                        clearIcon = clearIcon,
                        onClickClearIcon = onClickClearIcon,
                    )
                } else {
                    trailingIcon?.invoke()
                }
            }
        },
        placeholder = placeholder,
        supportingText = errorText?.let {
            {
                Text(
                    text = it,
                    color = KptTheme.colorScheme.error,
                )
            }
        },
        textStyle = (textStyle ?: TextStyle()).copy(
            color = KptTheme.colorScheme.onSurface,
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MifosCustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = KptTheme.spacing.lg),
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    isError: Boolean = false,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
    placeholder: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
) {
    val colors = TextFieldDefaults.colors(
        focusedLabelColor = KptTheme.colorScheme.primary,
        unfocusedLabelColor = KptTheme.colorScheme.primary,
        cursorColor = KptTheme.colorScheme.onSurface,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        errorContainerColor = Color.Transparent,
        focusedIndicatorColor = KptTheme.colorScheme.primary,
        unfocusedIndicatorColor = KptTheme.colorScheme.onSurface.copy(alpha = 0.05f),
        focusedTrailingIconColor = KptTheme.colorScheme.onSurface.copy(0.15f),
        unfocusedTrailingIconColor = KptTheme.colorScheme.onSurface.copy(0.15f),
    )
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        interactionSource = interactionSource,
        enabled = enabled,
        singleLine = singleLine,
        readOnly = readOnly,
        textStyle = textStyle,
        visualTransformation = visualTransformation,
        keyboardActions = keyboardActions,
        maxLines = maxLines,
        minLines = minLines,
        keyboardOptions = keyboardOptions,
        cursorBrush = SolidColor(KptTheme.colorScheme.primary),
    ) {
        TextFieldDefaults.DecorationBox(
            value = value,
            visualTransformation = VisualTransformation.None,
            innerTextField = it,
            singleLine = singleLine,
            enabled = enabled,
            interactionSource = interactionSource,
            label = {
                Text(
                    text = label,
                    style = KptTheme.typography.labelLarge,
                    modifier = Modifier.padding(bottom = KptTheme.spacing.md),
                )
            },
            placeholder = placeholder,
            trailingIcon = trailingIcon,
            leadingIcon = leadingIcon,
            supportingText = supportingText,
            colors = colors,
            isError = isError,
            contentPadding = PaddingValues(bottom = KptTheme.spacing.md),
            container = {
                TextFieldDefaults.Container(
                    enabled = enabled,
                    isError = isError,
                    colors = colors,
                    interactionSource = interactionSource,
                    shape = RectangleShape,
                    focusedIndicatorLineThickness = 1.dp,
                    unfocusedIndicatorLineThickness = 1.dp,
                )
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MifosCustomTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    label: String,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = KptTheme.spacing.lg),
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    isError: Boolean = false,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
    placeholder: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
) {
    val colors = TextFieldDefaults.colors().copy(
        cursorColor = KptTheme.colorScheme.primary,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        errorContainerColor = Color.Transparent,
        focusedIndicatorColor = KptTheme.colorScheme.primary,
        unfocusedIndicatorColor = KptTheme.colorScheme.onSurface.copy(alpha = 0.05f),
        focusedTrailingIconColor = KptTheme.colorScheme.onSurface.copy(0.15f),
        unfocusedTrailingIconColor = KptTheme.colorScheme.onSurface.copy(0.15f),
    )
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        interactionSource = interactionSource,
        enabled = enabled,
        singleLine = singleLine,
        readOnly = readOnly,
        textStyle = textStyle,
        visualTransformation = visualTransformation,
        keyboardActions = keyboardActions,
        maxLines = maxLines,
        minLines = minLines,
        keyboardOptions = keyboardOptions,
        cursorBrush = SolidColor(KptTheme.colorScheme.primary),
    ) {
        TextFieldDefaults.DecorationBox(
            value = value.text,
            visualTransformation = VisualTransformation.None,
            innerTextField = it,
            singleLine = singleLine,
            enabled = enabled,
            interactionSource = interactionSource,
            label = {
                Text(
                    text = label,
                    color = KptTheme.colorScheme.primary,
                    style = KptTheme.typography.labelLarge,
                    modifier = Modifier.padding(bottom = KptTheme.spacing.md),
                )
            },
            trailingIcon = trailingIcon,
            leadingIcon = leadingIcon,
            supportingText = supportingText,
            colors = colors,
            isError = isError,
            placeholder = placeholder,
            contentPadding = PaddingValues(bottom = KptTheme.spacing.md),
            container = {
                TextFieldDefaults.Container(
                    enabled = enabled,
                    isError = isError,
                    colors = colors,
                    interactionSource = interactionSource,
                    shape = RectangleShape,
                    focusedIndicatorLineThickness = 1.dp,
                    unfocusedIndicatorLineThickness = 1.dp,
                )
            },
        )
    }
}

@Composable
private fun ClearIconButton(
    showClearIcon: Boolean,
    clearIcon: ImageVector,
    onClickClearIcon: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Unspecified,
    contentColor: Color = KptTheme.colorScheme.primary,
) {
    AnimatedVisibility(
        visible = showClearIcon,
        modifier = modifier,
    ) {
        IconButton(
            onClick = onClickClearIcon,
            modifier = Modifier.semantics {
                contentDescription = "clearIcon"
            },
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = containerColor,
                contentColor = contentColor,
            ),
        ) {
            Icon(
                imageVector = clearIcon,
                contentDescription = "trailingIcon",
            )
        }
    }
}
