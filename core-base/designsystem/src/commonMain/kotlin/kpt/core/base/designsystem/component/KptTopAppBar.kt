/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(ExperimentalMaterial3Api::class)

package kpt.core.base.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import kpt.core.base.designsystem.core.KptTopAppBarConfiguration
import kpt.core.base.designsystem.core.TopAppBarAction
import kpt.core.base.designsystem.core.TopAppBarVariant
import kpt.core.base.designsystem.theme.KptTheme

/**
 * The navigation-icon slot of [KptTopAppBar].
 *
 * Extracted so its null-handling does not count against the bar's own complexity, and so the four
 * Material variants below share one definition rather than four.
 *
 * Emits nothing when the configuration declares no icon. The button is present-but-disabled when an
 * icon is declared without a click handler, so a decorative icon cannot look tappable.
 */
@Composable
private fun NavigationIconSlot(configuration: KptTopAppBarConfiguration) {
    val icon = configuration.navigationIcon ?: return
    IconButton(
        onClick = configuration.onNavigationIonClick ?: {},
        enabled = configuration.onNavigationIonClick != null,
    ) {
        Icon(imageVector = icon, contentDescription = "Navigation")
    }
}

/**
 * Top app bar built from a declarative [KptTopAppBarConfiguration].
 *
 * The DSL form — prefer it when a screen's bar is assembled from data or varies by state; the
 * parameter overloads below are shorthands over this.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KptTopAppBar(
    configuration: KptTopAppBarConfiguration,
    modifier: Modifier = Modifier,
) {
    // The caller's modifier is applied AFTER the configuration's, so a screen holding a config it
    // did not build can still place the bar without having to copy the config to change layout.
    val finalModifier = configuration.modifier
        .then(modifier)
        .testTag(configuration.testTag ?: "KptTopAppBar")
        .let { mod ->
            if (configuration.contentDescription != null) {
                mod.semantics { contentDescription = configuration.contentDescription }
            } else {
                mod
            }
        }

    val titleContent: @Composable () -> Unit = {
        Column {
            Text(
                text = configuration.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            configuration.subtitle?.let { subtitle ->
                Text(
                    text = subtitle,
                    style = KptTheme.typography.bodySmall,
                    color = KptTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }

    val actionsContent: @Composable RowScope.() -> Unit = {
        configuration.actions.forEach { action ->
            IconButton(
                onClick = action.onClick,
                enabled = action.enabled,
                modifier = action.testTag?.let { Modifier.testTag(it) } ?: Modifier,
            ) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = action.contentDescription,
                )
            }
        }
    }

    // Resolved once rather than inside each branch: four copies of the same fallback are four
    // places for them to drift apart.
    val windowInsets = configuration.windowInsets ?: TopAppBarDefaults.windowInsets
    val colors = configuration.colors ?: TopAppBarDefaults.topAppBarColors()

    when (configuration.variant) {
        TopAppBarVariant.Small -> TopAppBar(
            title = titleContent,
            modifier = finalModifier,
            navigationIcon = { NavigationIconSlot(configuration) },
            actions = actionsContent,
            windowInsets = windowInsets,
            colors = colors,
            scrollBehavior = configuration.scrollBehavior,
        )

        TopAppBarVariant.CenterAligned -> CenterAlignedTopAppBar(
            title = titleContent,
            modifier = finalModifier,
            navigationIcon = { NavigationIconSlot(configuration) },
            actions = actionsContent,
            windowInsets = windowInsets,
            colors = colors,
            scrollBehavior = configuration.scrollBehavior,
        )

        TopAppBarVariant.Medium -> MediumTopAppBar(
            title = titleContent,
            modifier = finalModifier,
            navigationIcon = { NavigationIconSlot(configuration) },
            actions = actionsContent,
            windowInsets = windowInsets,
            colors = colors,
            scrollBehavior = configuration.scrollBehavior,
        )

        TopAppBarVariant.Large -> LargeTopAppBar(
            title = titleContent,
            modifier = finalModifier,
            navigationIcon = { NavigationIconSlot(configuration) },
            actions = actionsContent,
            windowInsets = windowInsets,
            colors = colors,
            scrollBehavior = configuration.scrollBehavior,
        )
    }
}

/**
 * Title-only bar with no navigation icon — a tab root. [variant] selects the Material 3 size.
 */
@Composable
fun KptTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    variant: TopAppBarVariant = TopAppBarVariant.Small,
) {
    KptTopAppBar(
        KptTopAppBarConfiguration(
            title = title,
            modifier = modifier,
            variant = variant,
        ),
    )
}

/**
 * Title plus a back affordance, always shown — a pushed detail screen.
 */
@Composable
fun KptTopAppBar(
    title: String,
    onNavigationIconClick: () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowBack,
    variant: TopAppBarVariant = TopAppBarVariant.Small,
    actions: List<TopAppBarAction> = emptyList(),
) {
    KptTopAppBar(
        KptTopAppBarConfiguration(
            title = title,
            modifier = modifier,
            variant = variant,
            navigationIcon = navigationIcon,
            onNavigationIonClick = onNavigationIconClick,
            actions = actions,
        ),
    )
}

/**
 * Title with a CONDITIONAL back affordance, for a screen reachable both as a tab root and as a
 * pushed destination.
 */
@Composable
fun KptTopAppBar(
    title: String,
    showNavigationIcon: Boolean,
    onNavigationIconClick: () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowBack,
    variant: TopAppBarVariant = TopAppBarVariant.Small,
    actions: List<TopAppBarAction> = emptyList(),
) {
    KptTopAppBar(
        KptTopAppBarConfiguration(
            title = title,
            modifier = modifier,
            variant = variant,
            navigationIcon = if (showNavigationIcon) navigationIcon else null,
            onNavigationIonClick = onNavigationIconClick,
            actions = actions,
        ),
    )
}

/**
 * Two-line bar: title over a subtitle, for a screen whose context needs naming (an account, a date
 * range) without stealing the title.
 */
@Composable
fun KptTopAppBar(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onNavigationIconClick: (() -> Unit)? = null,
    navigationIcon: ImageVector? = if (onNavigationIconClick != null) Icons.AutoMirrored.Filled.ArrowBack else null,
    variant: TopAppBarVariant = TopAppBarVariant.Small,
) {
    KptTopAppBar(
        KptTopAppBarConfiguration(
            title = title,
            subtitle = subtitle,
            modifier = modifier,
            variant = variant,
            navigationIcon = navigationIcon,
            onNavigationIonClick = onNavigationIconClick,
        ),
    )
}

/**
 * Title plus ONE trailing action. For more than one, use the configuration overload rather than
 * growing the parameter list.
 */
@Composable
fun KptTopAppBar(
    title: String,
    actionIcon: ImageVector,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    actionContentDescription: String = "Action",
    onNavigationIconClick: (() -> Unit)? = null,
    navigationIcon: ImageVector? = if (onNavigationIconClick != null) Icons.AutoMirrored.Filled.ArrowBack else null,
    variant: TopAppBarVariant = TopAppBarVariant.Small,
) {
    KptTopAppBar(
        KptTopAppBarConfiguration(
            title = title,
            modifier = modifier,
            variant = variant,
            navigationIcon = navigationIcon,
            onNavigationIonClick = onNavigationIconClick,
            actions = listOf(
                TopAppBarAction(actionIcon, actionContentDescription, onActionClick),
            ),
        ),
    )
}

/**
 * Bar whose title area is a live search field, with the query hoisted to the caller.
 */
@Composable
fun KptSearchAppBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search...",
    onSearchClick: (() -> Unit)? = null,
) {
    TopAppBar(
        title = {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text(placeholder) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                } else {
                    null
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            onSearchClick?.let { onClick ->
                IconButton(onClick = onClick) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            }
        },
        modifier = modifier.testTag("KptSearchAppBar"),
    )
}

/**
 * Bar carrying a profile avatar as its trailing action.
 */
@Composable
fun KptProfileAppBar(
    title: String,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onNavigationIconClick: (() -> Unit)? = null,
) {
    KptTopAppBar(
        KptTopAppBarConfiguration(
            title = title,
            subtitle = subtitle,
            modifier = modifier,
            navigationIcon = if (onNavigationIconClick != null) Icons.AutoMirrored.Filled.ArrowBack else null,
            onNavigationIonClick = onNavigationIconClick,
            actions = listOf(
                TopAppBarAction(
                    icon = Icons.Default.AccountCircle,
                    contentDescription = "Profile",
                    onClick = onProfileClick,
                ),
            ),
        ),
    )
}

/**
 * Bar preset for settings screens — back affordance plus the settings title treatment.
 */
@Composable
fun KptSettingsAppBar(
    onNavigationIconClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Settings",
    onSearchClick: (() -> Unit)? = null,
    onMoreClick: (() -> Unit)? = null,
) {
    val actions = mutableListOf<TopAppBarAction>()

    onSearchClick?.let {
        actions.add(TopAppBarAction(Icons.Default.Search, "Search", it))
    }

    onMoreClick?.let {
        actions.add(TopAppBarAction(Icons.Default.MoreVert, "More options", it))
    }

    KptTopAppBar(
        KptTopAppBarConfiguration(
            title = title,
            modifier = modifier,
            navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
            onNavigationIonClick = onNavigationIconClick,
            actions = actions,
        ),
    )
}

/**
 * The shared body of the four variant shorthands below.
 *
 * They differ only in [variant], so routing them through one helper is what keeps them
 * behaviourally identical: [KptMediumTopAppBar] and [KptLargeTopAppBar] previously accepted an
 * `onNavigationIconClick` and dropped it on the floor, so a back arrow the caller asked for never
 * appeared on those two sizes while it worked on the other two.
 */
@Composable
private fun KptVariantTopAppBar(
    title: String,
    variant: TopAppBarVariant,
    modifier: Modifier = Modifier,
    onNavigationIconClick: (() -> Unit)? = null,
) = KptTopAppBar(
    KptTopAppBarConfiguration(
        title = title,
        modifier = modifier,
        variant = variant,
        navigationIcon = onNavigationIconClick?.let { Icons.AutoMirrored.Filled.ArrowBack },
        onNavigationIonClick = onNavigationIconClick,
    ),
)

/**
 * Shorthand for the Small Material 3 bar — the default height.
 */
@Composable
fun KptSmallTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    onNavigationIconClick: (() -> Unit)? = null,
) = KptVariantTopAppBar(title, TopAppBarVariant.Small, modifier, onNavigationIconClick)

/**
 * Shorthand for the centre-aligned bar.
 */
@Composable
fun KptCenterAlignedTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    onNavigationIconClick: (() -> Unit)? = null,
) = KptVariantTopAppBar(title, TopAppBarVariant.CenterAligned, modifier, onNavigationIconClick)

/**
 * Shorthand for the Medium (collapsing) bar.
 */
@Composable
fun KptMediumTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    onNavigationIconClick: (() -> Unit)? = null,
) = KptVariantTopAppBar(title, TopAppBarVariant.Medium, modifier, onNavigationIconClick)

/**
 * Shorthand for the Large (collapsing) bar.
 */
@Composable
fun KptLargeTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    onNavigationIconClick: (() -> Unit)? = null,
) = KptVariantTopAppBar(title, TopAppBarVariant.Large, modifier, onNavigationIconClick)
