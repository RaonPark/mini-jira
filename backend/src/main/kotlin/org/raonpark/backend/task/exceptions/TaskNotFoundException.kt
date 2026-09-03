package org.raonpark.backend.task.exceptions

import java.util.UUID

class TaskNotFoundException(
    val taskId: UUID,
): RuntimeException("Task not found: $taskId")
