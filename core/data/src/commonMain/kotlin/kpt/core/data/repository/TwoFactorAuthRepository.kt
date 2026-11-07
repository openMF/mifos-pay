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
/** Repository for two-factor authentication. */
interface TwoFactorAuthRepository {
    // Phase-3 cutover — Flow-shaped 2FA reads/submissions on ScreenState.
    /** Streams the available OTP delivery methods. */
    suspend fun deliveryMethods(): ScreenStateStream<List<DeliveryMethod>>

    /** Requests an OTP through the given delivery method. */
    suspend fun requestOTP(deliveryMethod: String): ScreenStateStream<String>

    /** Validates the OTP token and streams the resulting [AccessToken]. */
    suspend fun validateToken(token: String): ScreenStateStream<AccessToken>
}
