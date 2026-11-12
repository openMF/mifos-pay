/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.designsystem.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.theme.KptTheme

/**
 * The sidebar panel itself. Shared by the docked and overlay paths so the two cannot drift apart.
 */
@Composable
private fun SidebarPanel(
    configuration: SidebarConfiguration,
    modifier: Modifier = Modifier,
    shadowElevation: Dp = 0.dp,
    content: @Composable () -> Unit,
) = Surface(
    modifier = modifier.width(configuration.width),
    color = configuration.backgroundColor ?: KptTheme.colorScheme.surface,
    shadowElevation = shadowElevation,
    content = content,
)

/**
 * Persistent sidebar beside content, for wide windows.
 *
 * [sidebarVisible] is hoisted, so this never toggles itself: it reports a user-initiated dismissal
 * through [onSidebarVisibilityChange] and leaves the decision to the caller. The only such gesture
 * is a tap on the scrim of an `overlay` sidebar, and only when the configuration says it is
 * [SidebarConfiguration.collapsible] — a pinned rail has nothing to dismiss.
 */
@Composable
fun KptSidebarLayout(
    sidebarContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    configuration: SidebarConfiguration = SidebarConfiguration(),
    sidebarVisible: Boolean = true,
    onSidebarVisibilityChange: (Boolean) -> Unit = {},
    content: @Composable () -> Unit,
) {
    // Docked and overlay are mutually exclusive presentations of the same panel. Gating the in-Row
    // panel on !overlay is what stops an overlay sidebar being drawn twice — once inset beside the
    // content and once floating over it.
    val docked = sidebarVisible && !configuration.overlay
    val dividerColor = configuration.dividerColor ?: KptTheme.colorScheme.outline

    Row(
        modifier = modifier
            .fillMaxSize()
            .testTag("KptSidebarLayout"),
    ) {
        if (docked && configuration.position == SidebarPosition.Start) {
            SidebarPanel(configuration, content = sidebarContent)
            VerticalDivider(color = dividerColor)
        }

        Box(
            modifier = Modifier.weight(1f),
        ) {
            content()

            if (configuration.overlay && sidebarVisible) {
                if (configuration.collapsible) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(KptTheme.colorScheme.scrim.copy(alpha = 0.32f))
                            .testTag("KptSidebarScrim")
                            // No indication: a ripple spreading across a full-screen scrim reads as
                            // a rendering fault rather than a press.
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { onSidebarVisibilityChange(false) },
                    )
                }

                SidebarPanel(
                    configuration = configuration,
                    modifier = Modifier
                        .fillMaxHeight()
                        .align(
                            if (configuration.position == SidebarPosition.Start) {
                                Alignment.CenterStart
                            } else {
                                Alignment.CenterEnd
                            },
                        ),
                    shadowElevation = 8.dp,
                    content = sidebarContent,
                )
            }
        }

        if (docked && configuration.position == SidebarPosition.End) {
            VerticalDivider(color = dividerColor)
            SidebarPanel(configuration, content = sidebarContent)
        }
    }
}

/**
 * Width and behaviour of a [KptSidebarLayout] sidebar.
 */
@Immutable
data class SidebarConfiguration(
    /** Sidebar width when expanded. 300.dp is the Material navigation-drawer width. */
    val width: Dp = 300.dp,
    /** Which edge the sidebar occupies. `Start`, so it mirrors automatically in an RTL locale. */
    val position: SidebarPosition = SidebarPosition.Start,
    /**
     * Whether the user can collapse it. False pins it open — right for a desktop tool, wrong for a window that can get
     * narrow.
     */
    val collapsible: Boolean = true,
    /**
     * True draws the sidebar OVER the content; false insets the content beside it. Overlay suits a temporary drawer,
     * inset a permanent rail.
     */
    val overlay: Boolean = false,
    /** Sidebar background, or null to inherit the theme surface. */
    val backgroundColor: Color? = null,
    /** Colour of the edge between sidebar and content, or null for the theme's outline-variant. */
    val dividerColor: Color? = null,
)

/**
 * Which edge the sidebar occupies.
 */
enum class SidebarPosition {
    /** The reading-start edge — left in LTR, right in RTL. The usual choice, since it mirrors. */
    Start,

    /** The reading-end edge. Use only when the sidebar is genuinely secondary to the content. */
    End,
}
