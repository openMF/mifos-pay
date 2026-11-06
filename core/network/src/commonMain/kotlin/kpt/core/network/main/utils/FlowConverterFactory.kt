/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.main.utils

import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.converter.Converter
import de.jensklingenberg.ktorfit.converter.KtorfitResult
import de.jensklingenberg.ktorfit.converter.TypeData
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Factory that enables the use of Flow<T> as return type
 */
public class FlowConverterFactory : Converter.Factory {
    override fun responseConverter(
        typeData: TypeData,
        ktorfit: Ktorfit,
    ): Converter.ResponseConverter<HttpResponse, *>? {
        if (typeData.typeInfo.type == Flow::class) {
            return object : Converter.ResponseConverter<HttpResponse, Flow<Any?>> {
                override fun convert(getResponse: suspend () -> HttpResponse): Flow<Any?> {
                    val requestType = typeData.typeArgs.first()
                    return flow {
                        val response = getResponse()
                        if (requestType.typeInfo.type == HttpResponse::class) {
                            emit(response)
                        } else {
                            val convertedBody =
                                ktorfit.nextSuspendResponseConverter(
                                    this@FlowConverterFactory,
                                    typeData.typeArgs.first(),
                                )?.convert(KtorfitResult.Success(response))
                                    ?: response.body(typeData.typeArgs.first().typeInfo)
                            emit(convertedBody)
                        }
                    }
                }
            }
        }
        return null
    }
}
