package org.raonpark.backend.task.dto

import org.raonpark.backend.task.enums.TaskPriority
import org.raonpark.backend.task.enums.TaskStatus
import org.raonpark.backend.users.dto.UserSummary
import java.time.Instant
import java.util.UUID

data class TaskDetail(
    val id: UUID,
    val title: String,
    val description: String?,
    val status: TaskStatus,
    val priority: TaskPriority,
    val assignee: UserSummary?,
    val createdAt: Instant,
    val updatedAt: Instant,
)
