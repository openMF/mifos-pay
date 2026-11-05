/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.coingecko.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kpt.core.model.crypto.CoinDetail

@Serializable
data class CoinDetailDto(
    val id: String,
    val name: String,
    val symbol: String,
    val image: CoinImageDto? = null,
    @SerialName("market_data") val marketData: MarketDataDto? = null,
    val description: DescriptionDto? = null,
) {
    fun toDomain(): CoinDetail = CoinDetail(
        id = id,
        name = name,
        symbol = symbol,
        imageUrl = image?.large.orEmpty(),
        currentPrice = marketData?.currentPrice?.get("usd") ?: 0.0,
        marketCap = marketData?.marketCap?.get("usd")?.toLong() ?: 0L,
        marketCapRank = marketData?.marketCapRank ?: 0,
        priceChangePercent24h = marketData?.priceChangePercentage24h ?: 0.0,
        high24h = marketData?.high24h?.get("usd") ?: 0.0,
        low24h = marketData?.low24h?.get("usd") ?: 0.0,
        circulatingSupply = marketData?.circulatingSupply ?: 0.0,
        maxSupply = marketData?.maxSupply,
        description = description?.en.orEmpty(),
    )
}

@Serializable
data class DescriptionDto(val en: String? = null)
