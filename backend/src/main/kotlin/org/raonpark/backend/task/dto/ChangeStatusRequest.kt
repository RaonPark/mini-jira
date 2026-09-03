package org.raonpark.backend.task.dto

import org.raonpark.backend.task.enums.TaskStatus

data class ChangeStatusRequest(
    val status: TaskStatus,
)
