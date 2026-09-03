package org.raonpark.backend.task.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.raonpark.backend.task.entity.TaskEntity
import org.raonpark.backend.task.enums.TaskPriority
import org.raonpark.backend.task.enums.TaskStatus
import java.util.UUID

data class CreateTaskRequest(
    @field:NotBlank @field:Size(max = 200)
    val title: String,
    val description: String?,
    val status: TaskStatus?,
    val priority: TaskPriority?,
    val assigneeId: UUID?
) {
    fun toEntity(): TaskEntity {
        return TaskEntity(
            title = title,
            description = description,
            status = status ?: TaskStatus.TODO,
            priority = priority ?: TaskPriority.MEDIUM,
            assigneeId = assigneeId
        )
    }
}
