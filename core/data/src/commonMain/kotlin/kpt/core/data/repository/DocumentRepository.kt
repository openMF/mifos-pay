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

import io.ktor.http.content.PartData
import kpt.core.common.ScreenStateStream
import kpt.core.model.network.entity.noncore.Document
/** Repository for documents attached to wallet entities such as clients. */
interface DocumentRepository {
    // Flow-shaped surfaces on ScreenState.
    /** Streams the documents attached to the given entity. */
    suspend fun getDocuments(entityType: String, entityId: Int): ScreenStateStream<List<Document>>

    /** Uploads a new document for an entity. */
    suspend fun createDocument(
        entityType: String,
        entityId: Int,
        name: String,
        description: String,
        fileName: PartData.FileItem,
    ): ScreenStateStream<Unit>

    // Message-only write: returns Unit and throws on error.
    /** Uploads a new document for an entity from a file. */
    suspend fun createDocument(
        entityType: String,
        entityId: Long,
        name: String,
        description: String,
        file: ByteArray,
    )

    /** Downloads a document of the given entity. */
    suspend fun downloadDocument(entityType: String, entityId: Int, documentId: Int): ScreenStateStream<Document>

    /** Deletes a document of the given entity. */
    suspend fun deleteDocument(entityType: String, entityId: Int, documentId: Int): ScreenStateStream<Unit>

    /** Updates an existing document. */
    suspend fun updateDocument(
        entityType: String,
        entityId: Int,
        documentId: Int,
        name: String,
        description: String,
        fileName: PartData.FileItem,
    ): ScreenStateStream<Unit>
}
