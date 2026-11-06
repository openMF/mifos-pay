/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.repository

import kpt.core.common.ScreenStateStream
import kpt.core.model.network.twofactor.AccessToken
import kpt.core.model.network.twofactor.DeliveryMethod
interface TwoFactorAuthRepository {
    // Phase-3 cutover — Flow-shaped 2FA reads/submissions on ScreenState.
    suspend fun deliveryMethods(): ScreenStateStream<List<DeliveryMethod>>

    suspend fun requestOTP(deliveryMethod: String): ScreenStateStream<String>

    suspend fun validateToken(token: String): ScreenStateStream<AccessToken>
}
