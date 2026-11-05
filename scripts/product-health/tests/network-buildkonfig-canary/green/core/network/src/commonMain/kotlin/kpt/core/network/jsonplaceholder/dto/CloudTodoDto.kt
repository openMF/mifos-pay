/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.jsonplaceholder.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kpt.core.model.cloudtodo.CloudTodo

/** Wire shape for jsonplaceholder `/todos`. */
@Serializable
data class CloudTodoDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String,
    @SerialName("completed") val completed: Boolean,
    @SerialName("userId") val userId: Int = 1,
) {
    fun toDomain(): CloudTodo = CloudTodo(id = id, title = title, completed = completed)

    companion object {
        fun fromDomain(todo: CloudTodo): CloudTodoDto =
            CloudTodoDto(id = todo.id, title = todo.title, completed = todo.completed)
    }
}
