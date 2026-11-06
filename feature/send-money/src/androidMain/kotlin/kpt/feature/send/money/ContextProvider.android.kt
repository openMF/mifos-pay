/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.send.money

import android.content.Context

/**
 * Android-specific context provider for sharing functionality
 */
object ContextProvider {
    private var applicationContext: Context? = null

    /**
     * Initialize the context provider with the application context
     * This should be called from the Application class or MainActivity
     */
    fun init(context: Context) {
        applicationContext = context.applicationContext
    }

    /**
     * Get the application context
     */
    fun getContext(): Context? = applicationContext
}
