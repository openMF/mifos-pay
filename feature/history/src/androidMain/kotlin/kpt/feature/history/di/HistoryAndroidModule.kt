/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.history.di

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.mobilebytelabs.kmptoolkit.pdfgenerator.ExperimentalPdfGeneratorApi
import com.mobilebytelabs.kmptoolkit.pdfgenerator.PdfGenerator
import com.mobilebytelabs.kmptoolkit.pdfgenerator.createPdfGenerator
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * [PdfGenerator.generateAndSharePdf] goes through Android's `PrintManager`, which throws
 * `IllegalStateException: Can print only from an activity` if handed the Application context.
 * Track the foreground activity via lifecycle callbacks so the generator's context always
 * points at a live Activity, surviving activity recreation (rotation, etc).
 *
 * `createdAtStart = true` forces this singleton (and its callback registration) to be built
 * during `Application.onCreate()`, before any Activity exists — otherwise Koin would only
 * create it lazily on first use (e.g. when the History screen opens), missing the very first
 * `onActivityResumed` and leaving the generator stuck on the Application context.
 */
@OptIn(ExperimentalPdfGeneratorApi::class)
val HistoryPdfAndroidModule: Module = module {
    single<PdfGenerator>(createdAtStart = true) {
        val application = androidApplication()
        createPdfGenerator(application).also { generator ->
            application.registerActivityLifecycleCallbacks(
                object : Application.ActivityLifecycleCallbacks {
                    override fun onActivityResumed(activity: Activity) {
                        generator.setContext(activity)
                    }

                    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
                    override fun onActivityStarted(activity: Activity) = Unit
                    override fun onActivityPaused(activity: Activity) = Unit
                    override fun onActivityStopped(activity: Activity) = Unit
                    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
                    override fun onActivityDestroyed(activity: Activity) = Unit
                },
            )
        }
    }
}

@OptIn(ExperimentalPdfGeneratorApi::class)
actual val HistoryPdfModule: Module
    get() = HistoryPdfAndroidModule
