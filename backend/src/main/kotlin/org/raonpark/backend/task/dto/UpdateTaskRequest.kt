package org.raonpark.backend.task.dto

import jakarta.validation.constraints.Size
import org.raonpark.backend.task.enums.TaskPriority
import java.util.Optional
import java.util.UUID

data class UpdateTaskRequest(
    @field:Size(max = 200)
    val title: String?,
    val description: Optional<String>? = null,
    val priority: TaskPriority?,
    val assigneeId: Optional<UUID>? = null,
)
