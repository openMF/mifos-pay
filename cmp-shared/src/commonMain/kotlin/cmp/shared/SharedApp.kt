/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cmp.navigation.ComposeApp
import coil3.compose.LocalPlatformContext
import kpt.core.base.platform.LocalManagerProvider
import kpt.core.base.platform.context.LocalContext
import kpt.core.base.ui.util.LocalImageLoaderProvider
import kpt.core.base.ui.util.getDefaultImageLoader

/** The shared app root every platform entry point renders. */
@Composable
fun SharedApp(
    updateScreenCapture: (isScreenCaptureAllowed: Boolean) -> Unit,
    handleRecreate: () -> Unit,
    handleThemeMode: (osValue: Int) -> Unit,
    handleAppLocale: (locale: String?) -> Unit,
    modifier: Modifier = Modifier,
    onSplashScreenRemoved: () -> Unit,
) {
    LocalManagerProvider(LocalContext.current) {
        LocalImageLoaderProvider(getDefaultImageLoader(LocalPlatformContext.current)) {
            ComposeApp(
                updateScreenCapture = updateScreenCapture,
                handleRecreate = handleRecreate,
                handleThemeMode = handleThemeMode,
                handleAppLocale = handleAppLocale,
                onSplashScreenRemoved = onSplashScreenRemoved,
                modifier = modifier,
            )
        }
    }
}
