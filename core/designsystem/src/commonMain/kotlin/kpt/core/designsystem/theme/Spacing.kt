/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Shared spacing scale — replaces raw `.dp` literals scattered across features with a
 * disciplined 4 / 8 / 12 / 16 / 24 / 32 / 48 progression.
 *
 * Access from any Composable via [MaterialTheme.spacing]. Sub-plans 02 (layouts) and 03
 * (finance widgets) consume these tokens for all internal padding decisions — a fork that
 * wants denser layouts overrides `LocalSpacing` instead of editing each widget.
 */
@Immutable
data class Spacing(
    /** 0dp — no spacing. */
    val none: Dp = 0.dp,
    /** 4dp — tight grouping (icon-to-label, chip internal padding, label-to-supporting). */
    val xs: Dp = 4.dp,
    /** 8dp — adjacent elements within the same group. */
    val sm: Dp = 8.dp,
    /** 12dp — section-internal grouping. */
    val md: Dp = 12.dp,
    /** 16dp — standard list-row horizontal padding, standard card content padding. */
    val lg: Dp = 16.dp,
    /** 24dp — between major groups within a screen. */
    val xl: Dp = 24.dp,
    /** 32dp — section separators on dashboards. */
    val xxl: Dp = 32.dp,
    /** 48dp — hero spacing (empty-state illustration to title). */
    val xxxl: Dp = 48.dp,

    /**
     * Minimum touch target — M3 + WCAG 2.5.5 mandate ≥48dp.
     * Use as `Modifier.heightIn(min = spacing.touchTargetMin)`.
     */
    val touchTargetMin: Dp = 48.dp,
)

/** Composition local providing the active [Spacing] scale. */
val LocalSpacing = staticCompositionLocalOf { Spacing() }

/** Resolve the active [Spacing] scale from composition. */
val MaterialTheme.spacing: Spacing
    @Composable
    @ReadOnlyComposable
    get() = LocalSpacing.current
