package org.raonpark.backend.task.repository

import org.raonpark.backend.task.entity.TaskEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TaskRepository: JpaRepository<TaskEntity, UUID> {
}
