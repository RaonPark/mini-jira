package org.raonpark.backend.task.repository

import org.raonpark.backend.task.entity.TaskEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface TaskRepository: JpaRepository<TaskEntity, UUID> {
}
