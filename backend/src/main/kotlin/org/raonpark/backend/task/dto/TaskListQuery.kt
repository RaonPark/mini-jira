package org.raonpark.backend.task.dto

import org.raonpark.backend.task.enums.TaskStatus
import java.util.*

data class TaskListQuery(
    val page: Int,
    val size: Int,
    val status: TaskStatus? = null,
    val assigneeId: UUID? = null,
    val keyword: String? = null,
    val sort: String,
)
