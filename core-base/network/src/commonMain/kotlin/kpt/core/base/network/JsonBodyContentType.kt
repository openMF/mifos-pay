/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.network

import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.ContentType
import io.ktor.http.content.OutgoingContent
import io.ktor.http.contentType
import io.ktor.utils.io.ByteReadChannel

/**
 * Declares `Content-Type: application/json` on a request whose body is a typed object and that
 * declares no content type of its own.
 *
 * `ContentNegotiation` serializes a body only when the request already names a Content-Type it has a
 * converter for, and a Ktorfit `@Body` parameter names none. Without this, Ktor throws before
 * opening a socket ("Fail to prepare request body for sending … with Content-Type: null"), which a
 * sign-in screen reads as a wrong password. Runs in the request's State phase, before
 * ContentNegotiation's Transform phase, so the converter then finds the type.
 *
 * Scoped to typed bodies so nothing else changes: a bodyless GET still sends no Content-Type, a
 * caller's explicit type wins, and String / bytes / channels / prebuilt [OutgoingContent] (multipart,
 * form) keep the type they carry. `Accept` needs no help: ContentNegotiation adds it for every
 * registered converter.
 */
internal val JsonBodyContentType = createClientPlugin("JsonBodyContentType") {
    onRequest { request, content ->
        val typed = content !is OutgoingContent &&
            content !is String &&
            content !is ByteArray &&
            content !is ByteReadChannel
        if (typed && request.contentType() == null) request.contentType(ContentType.Application.Json)
    }
}
