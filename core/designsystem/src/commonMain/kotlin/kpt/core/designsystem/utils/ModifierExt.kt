/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.designsystem.utils

import androidx.compose.foundation.Indication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.LayoutDirection

/**
 * Horizontally mirrors content when the layout direction is RTL.
 *
 * For glyphs with a direction (a back chevron, a progress arrow). NOT for text or logos, which
 * must not be flipped.
 */
@Stable
@Composable
@Suppress("ModifierComposable")
fun Modifier.mirrorIfRtl() = composed {
    if (LocalLayoutDirection.current == LayoutDirection.Rtl) {
        scale(scaleX = -1f, scaleY = 1f)
    } else {
        this
    }
}

/**
 * Makes the element reachable and actionable by keyboard tab — desktop and web, where a tap target
 * alone leaves it unusable.
 */
@Stable
@Composable
@Suppress("ModifierComposable")
fun Modifier.tabNavigation() = composed {
    val focusManager = LocalFocusManager.current
    onPreviewKeyEvent { keyEvent ->
        if (keyEvent.key == Key.Tab && keyEvent.type == KeyEventType.KeyDown) {
            focusManager.moveFocus(
                if (keyEvent.isShiftPressed) {
                    FocusDirection.Previous
                } else {
                    FocusDirection.Next
                },
            )
            true
        } else {
            false
        }
    }
}

/**
 * Click handling with the app's ripple and accessibility semantics already attached, so a clickable
 * element is announced correctly without each call site remembering to say so.
 */
fun Modifier.onClick(
    indication: Indication? = null,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: () -> Unit,
) = this.composed(
    inspectorInfo = debugInspectorInfo {
        name = "onClickModifier"
        value = enabled
    },
) {
    val interactionSource = remember { MutableInteractionSource() }
    clickable(
        indication = indication,
        interactionSource = interactionSource,
        enabled = enabled,
        onClickLabel = onClickLabel,
        role = role,
    ) {
        onClick.invoke()
    }
}
