/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import kpt.core.base.ui.AppInfo
import kpt.feature.home.generated.resources.Res
import kpt.feature.home.generated.resources.screens_home_settings_cd
import kpt.feature.home.ui.TestTags
import org.jetbrains.compose.resources.stringResource

/**
 * The home tab shell — a framework-owned [Scaffold] with the app-title top bar and a
 * settings action. Always present in every fork, and it carries ZERO demo imports.
 *
 * The home body is supplied by the caller as an opaque [homeBody] composable — the fork owns
 * what renders there via `cmp-navigation`'s `ForkBackboneRegistry.homeBody` (default: the demo
 * Money-Toolkit dashboard). `customizer --clean` empties that seam, leaving this shell with an
 * empty body for the fork to fill — the top bar and settings entry point survive unchanged.
 * (S5 heal, epic pure-white-label-store5-network T7: the 12 hardcoded demo `navigateToX`
 * callbacks that used to thread through this shell now live in the fork-owned ForkBackboneRegistry.)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    onSettingsClick: () -> Unit,
    homeBody: @Composable () -> Unit = {},
) {
    Scaffold(
        modifier = Modifier.testTag(TestTags.Home.SCREEN),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        // App title from the common AppInfo.appDisplayName accessor (BuildKonfig →
                        // fork.properties#app.display.name), never a hardcoded brand string.
                        text = AppInfo.appDisplayName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(Res.string.screens_home_settings_cd),
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
                windowInsets = WindowInsets(0, 0, 0, 0),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Fork-owned home body (default: the demo dashboard) — supplied via ForkBackboneRegistry.homeBody.
            homeBody()
        }
    }
}
