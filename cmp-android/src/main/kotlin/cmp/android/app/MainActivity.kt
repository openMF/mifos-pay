/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.android.app

import android.content.res.Resources
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import cmp.shared.SharedApp
import io.github.mobilebytelabs.kmptoolkit.firebase.analytics.AnalyticsHelper
import io.github.mobilebytelabs.kmptoolkit.firebase.analytics.AppLifecycleTracker
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init
import kotlinx.coroutines.launch
import kpt.core.base.platform.update.AppUpdateManager
import kpt.core.base.ui.util.ShareUtils
import kpt.core.data.user.UserDataRepository
import org.koin.android.ext.android.inject
import java.util.Locale

/**
 * Main activity class. This class is used to set the content view of the
 * activity.
 *
 * @constructor Create empty Main activity
 * @see ComponentActivity
 */
@Suppress("UnusedPrivateProperty")
class MainActivity : AppCompatActivity() {

    // INJECTED, not constructed. `platformModule` already binds
    // `single<AppUpdateManager> { AppUpdateManagerImpl() }`, so building one here produced a SECOND
    // instance and left that binding dead on Android — anything resolving AppUpdateManager from Koin
    // (or reading LocalAppUpdateManager) talked to a different object than the one this activity
    // drives through onCreate/onResume.
    private val appUpdateManager: AppUpdateManager by inject()

    private val userPreferencesRepository: UserDataRepository by inject()

    private val analyticsHelper: AnalyticsHelper by inject()
    private val lifecycleTracker by lazy { AppLifecycleTracker(analyticsHelper) }

    override fun onCreate(savedInstanceState: Bundle?) {
        var shouldShowSplashScreen = true
        installSplashScreen().setKeepOnScreenCondition { shouldShowSplashScreen }

        super.onCreate(savedInstanceState)

        val darkThemeConfigFlow = userPreferencesRepository.observeDarkThemeConfig

        setupEdgeToEdge(darkThemeConfigFlow)

        ShareUtils.setActivityProvider { return@setActivityProvider this }
        FileKit.init(this)

        analyticsHelper.setUserId(deviceData)

        setContent {
            // The update CHECK moved to AppViewModel.init (commonMain) so every platform performs
            // it, not just Android. What stays here is `checkForResumeUpdateState()` in onResume —
            // genuinely an Android lifecycle concern, because Play's flexible update can be
            // interrupted by backgrounding and must be re-offered on return.
            lifecycleTracker.markAppLaunchComplete()

            // Expose every Compose testTag as an Android resource id, so Maestro `id:` selectors and UI
            // Automator can find the tags generators emit. Android-only, so it lives here, once, at the root.
            Box(Modifier.fillMaxSize().semantics { testTagsAsResourceId = true }) {
                SharedApp(
                    updateScreenCapture = ::updateScreenCapture,
                    handleRecreate = ::handleRecreate,
                    handleThemeMode = {
                        AppCompatDelegate.setDefaultNightMode(it)
                    },
                    handleAppLocale = { localeTag ->
                        val currentLocales = AppCompatDelegate.getApplicationLocales()
                        val newLocales = if (localeTag != null) {
                            LocaleListCompat.forLanguageTags(localeTag)
                        } else {
                            // System Default: clear app-specific locale
                            LocaleListCompat.getEmptyLocaleList()
                        }

                        // Only update if the locale has actually changed
                        if (currentLocales != newLocales) {
                            AppCompatDelegate.setApplicationLocales(newLocales)
                            // Update Locale.setDefault for non-UI formatting
                            if (localeTag != null) {
                                // Use forLanguageTag to properly parse locales like "en-GB", "pt-BR"
                                Locale.setDefault(Locale.forLanguageTag(localeTag))
                            } else {
                                // Reset to true system default locale from device configuration
                                // Use Resources.getSystem() to get device locale unaffected by app overrides
                                val systemLocale = Resources.getSystem().configuration.locales[0]
                                Locale.setDefault(systemLocale)
                            }
                        }
                    },
                    onSplashScreenRemoved = {
                        shouldShowSplashScreen = false
                    },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch { appUpdateManager.checkForResumeUpdateState() }
        lifecycleTracker.onEnterBackground()
    }

    override fun onStart() {
        super.onStart()
        lifecycleTracker.markAppLaunchStart()
    }

    private fun handleRecreate() {
        recreate()
    }

    private fun updateScreenCapture(isScreenCaptureAllowed: Boolean) {
        // Debug builds always allow screen capture so QA / device-test tooling (adb
        // screencap, screen recordings, Android Studio Profiler captures) work without
        // toggling the user-facing "Allow screen capture" preference. Release builds
        // honor the user preference. Its fresh-install default comes from
        // app-profile/app.yaml#security.screen_capture_allowed_by_default (false = FLAG_SECURE
        // on, the template's choice for a finance app showing balances and account names).
        val allow = isScreenCaptureAllowed || BuildConfig.DEBUG
        if (allow) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}
