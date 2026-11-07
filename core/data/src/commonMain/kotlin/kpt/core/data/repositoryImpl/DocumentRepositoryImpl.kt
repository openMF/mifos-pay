/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.repositoryImpl

import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.content.PartData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kpt.core.base.data.annotation.FromQualifier
import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.common.ScreenStateStream
import kpt.core.common.asScreenStateFlow
import kpt.core.data.repository.DocumentRepository
import kpt.core.model.network.entity.noncore.Document
import kpt.core.network.FineractApiManager
/** Default [DocumentRepository] backed by the document API. */
@RepositoryBinding(binds = DocumentRepository::class)
class DocumentRepositoryImpl(
    private val apiManager: FineractApiManager,
    @FromQualifier("IO") private val ioDispatcher: CoroutineDispatcher,
) : DocumentRepository {
    override suspend fun getDocuments(
        entityType: String,
        entityId: Int,
    ): ScreenStateStream<List<Document>> {
        return apiManager.documentApi
            .getDocuments(entityType, entityId)
            .asScreenStateFlow(isEmpty = { it.isEmpty() })
            .flowOn(ioDispatcher)
    }

    override suspend fun createDocument(
        entityType: String,
        entityId: Int,
        name: String,
        description: String,
        fileName: PartData.FileItem,
    ): ScreenStateStream<Unit> {
        return apiManager.documentApi
            .createDocument(entityType, entityId, name, description, fileName)
            .asScreenStateFlow()
            .flowOn(ioDispatcher)
    }

    override suspend fun createDocument(
        entityType: String,
        entityId: Long,
        name: String,
        description: String,
        file: ByteArray,
    ) {
        val formData = MultiPartFormDataContent(
            formData {
                // File part
                append(
                    "file",
                    file,
                    Headers.build {
                        append(HttpHeaders.ContentType, "multipart/form-data")
                        append(HttpHeaders.ContentDisposition, "filename=\"$name\"")
                    },
                )

                // Name and description fields
                append("name", name)
                append("description", description)
            },
        )

        withContext(ioDispatcher) {
            apiManager.documentApi.createDocumentFile(
                entityType = entityType,
                entityId = entityId,
                file = formData,
            )
        }
    }

    override suspend fun downloadDocument(
        entityType: String,
        entityId: Int,
        documentId: Int,
    ): ScreenStateStream<Document> {
        return apiManager.documentApi
            .downloadDocument(entityType, entityId, documentId)
            .asScreenStateFlow()
            .flowOn(ioDispatcher)
    }

    override suspend fun deleteDocument(
        entityType: String,
        entityId: Int,
        documentId: Int,
    ): ScreenStateStream<Unit> {
        return apiManager.documentApi
            .removeDocument(entityType, entityId, documentId)
            .asScreenStateFlow()
            .flowOn(ioDispatcher)
    }

    override suspend fun updateDocument(
        entityType: String,
        entityId: Int,
        documentId: Int,
        name: String,
        description: String,
        fileName: PartData.FileItem,
    ): ScreenStateStream<Unit> {
        return apiManager.documentApi
            .updateDocument(entityType, entityId, documentId, name, description, fileName)
            .asScreenStateFlow()
            .flowOn(ioDispatcher)
    }
}
