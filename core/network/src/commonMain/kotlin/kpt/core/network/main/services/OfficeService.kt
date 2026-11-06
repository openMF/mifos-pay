/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.main.services

import de.jensklingenberg.ktorfit.http.GET
import kotlinx.coroutines.flow.Flow
import kpt.core.model.office.Office
import kpt.core.network.main.utils.ApiEndPoints
interface OfficeService {
    @GET(ApiEndPoints.OFFICES)
    fun getOffices(): Flow<List<Office>>
}
