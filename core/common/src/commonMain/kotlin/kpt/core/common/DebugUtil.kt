/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.common

import co.touchlab.kermit.Logger

object DebugUtil {

    private val logger = Logger.withTag("QXZ")

    fun log(vararg objects: Any): Array<out Any> {
        val stringToPrint = objects.joinToString(", ")
        logger.d { stringToPrint }
        return objects
    }
}
