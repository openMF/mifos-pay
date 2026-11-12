/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.component.AppCard
import kpt.core.base.ui.AppInfo
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.spacing
import kpt.core.ui.scaffold.KptScaffold
import kpt.feature.settings.generated.resources.Res
import kpt.feature.settings.generated.resources.feature_settings_change_language_placeholder_text
import kpt.feature.settings.generated.resources.feature_settings_change_language_text
import kpt.feature.settings.generated.resources.feature_settings_change_theme_placeholder_text
import kpt.feature.settings.generated.resources.feature_settings_change_theme_text
import kpt.feature.settings.generated.resources.feature_settings_sync_drafts_row
import kpt.feature.settings.generated.resources.feature_settings_sync_drafts_row_description
import org.jetbrains.compose.resources.stringResource

/**
 * The settings screen CONTENT — the template-owned Settings [KptScaffold] with the theme / language /
 * sync-and-drafts cards + version footer. This presentational composable is the reference `@Preview`
 * render target (see `SettingsScreenPreview.kt` → the desktop Roborazzi golden), so it takes plain
 * callbacks and no state.
 *
 * The stateful shell/seam split (WS01 base-feature seam, epic AC7): the dialog state + dev-menu now live
 * in the fork-owned [kpt.feature.settings.demo.SettingsDemoBody] (rendered via `cmp-navigation`'s
 * `ForkBackboneRegistry.settingsBody`), which wires those callbacks to THIS content. A template sync
 * full-copies this content shell while the fork's `settingsBody` seam survives.
 */
@Composable
internal fun SettingsScreenContent(
    onBackClick: () -> Unit,
    onThemeCardClick: () -> Unit,
    onLanguageCardClick: () -> Unit,
    onSyncAndDraftsClick: () -> Unit,
    modifier: Modifier = Modifier,
    onFooterLongClick: (() -> Unit)? = null,
) {
    val sp = MaterialTheme.spacing
    KptScaffold(
        title = "Settings",
        onNavigationIconClick = onBackClick,
        modifier = modifier.testTag(TestTags.Settings.SCREEN),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = sp.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(sp.md),
        ) {
            ThemeCard(onClick = onThemeCardClick)
            LanguageCard(onClick = onLanguageCardClick)
            SyncAndDraftsCard(onClick = onSyncAndDraftsClick)
            Spacer(modifier = Modifier.fillMaxWidth().padding(sp.sm))
            VersionLabel(onLongClick = onFooterLongClick)
        }
    }
}

@Composable
internal fun SyncAndDraftsCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    SettingsRowCard(
        icon = AppIcons.Payment,
        title = stringResource(Res.string.feature_settings_sync_drafts_row),
        contentDescription = stringResource(Res.string.feature_settings_sync_drafts_row_description),
        accentColor = MaterialTheme.colorScheme.secondary,
        onClick = onClick,
        modifier = modifier,
    )
}

/**
 * Static version footer label. When the caller supplies a non-empty dev-menu list (via
 * [kpt.feature.settings.demo.SettingsDemoBody]), long-pressing opens the Dev tools menu. When the list
 * is empty (release builds / neutralized forks) the long-press handler is `null`, so the label behaves
 * as a plain text footer.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun VersionLabel(onLongClick: (() -> Unit)?, modifier: Modifier = Modifier) {
    // App-name footer rendered from the common AppInfo.appDisplayName accessor (BuildKonfig →
    // gradle/fork.properties#app.display.name), not a hardcoded string resource — so a fork rebrands
    // in app-profile in one place (S9/T10).
    val rowModifier = if (onLongClick != null) {
        modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { /* no-op short-tap */ },
                onLongClick = onLongClick,
            )
    } else {
        modifier.fillMaxWidth()
    }
    Text(
        text = AppInfo.appDisplayName,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = rowModifier,
    )
}

@Composable
internal fun ThemeCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    SettingsRowCard(
        icon = AppIcons.Sun,
        title = stringResource(Res.string.feature_settings_change_theme_text),
        contentDescription = stringResource(Res.string.feature_settings_change_theme_placeholder_text),
        accentColor = MaterialTheme.colorScheme.tertiary,
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
internal fun LanguageCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    SettingsRowCard(
        icon = AppIcons.Language,
        title = stringResource(Res.string.feature_settings_change_language_text),
        contentDescription = stringResource(Res.string.feature_settings_change_language_placeholder_text),
        accentColor = MaterialTheme.colorScheme.primary,
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
private fun SettingsRowCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    contentDescription: String,
    accentColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sp = MaterialTheme.spacing
    AppCard(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        accentColor = accentColor,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(sp.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(sp.md),
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = accentColor)
            Text(text = title, modifier = Modifier.weight(1f))
            IconButton(onClick = onClick) {
                Icon(imageVector = AppIcons.ArrowRight, contentDescription = contentDescription)
            }
        }
    }
}
