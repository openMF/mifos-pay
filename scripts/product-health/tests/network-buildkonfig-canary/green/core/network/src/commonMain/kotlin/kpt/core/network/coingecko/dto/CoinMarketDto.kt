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
import kpt.core.model.crypto.CoinMarket

@Serializable
data class CoinMarketDto(
    val id: String,
    val symbol: String,
    val name: String,
    val image: String,
    @SerialName("current_price") val currentPrice: Double,
    @SerialName("market_cap") val marketCap: Long,
    @SerialName("market_cap_rank") val marketCapRank: Int,
    @SerialName("price_change_percentage_24h") val priceChangePercentage24h: Double? = null,
    @SerialName("high_24h") val high24h: Double? = null,
    @SerialName("low_24h") val low24h: Double? = null,
) {
    fun toDomain(): CoinMarket = CoinMarket(
        id = id,
        symbol = symbol,
        name = name,
        imageUrl = image,
        currentPrice = currentPrice,
        marketCap = marketCap,
        marketCapRank = marketCapRank,
        priceChangePercent24h = priceChangePercentage24h ?: 0.0,
        high24h = high24h ?: currentPrice,
        low24h = low24h ?: currentPrice,
    )
}

@Serializable
data class CoinImageDto(val large: String? = null)

@Serializable
data class MarketDataDto(
    @SerialName("current_price") val currentPrice: Map<String, Double>? = null,
    @SerialName("market_cap") val marketCap: Map<String, Double>? = null,
    @SerialName("market_cap_rank") val marketCapRank: Int? = null,
    @SerialName("price_change_percentage_24h") val priceChangePercentage24h: Double? = null,
    @SerialName("high_24h") val high24h: Map<String, Double>? = null,
    @SerialName("low_24h") val low24h: Map<String, Double>? = null,
    @SerialName("circulating_supply") val circulatingSupply: Double? = null,
    @SerialName("max_supply") val maxSupply: Double? = null,
)
